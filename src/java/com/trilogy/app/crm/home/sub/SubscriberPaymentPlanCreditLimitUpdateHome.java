/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;

/**
 * This home checks whether the subscriber is being created into an Account
 * that is under Payment Plan.  Subscribers that are in an account with payment 
 * plan have their credit limit reduced by a preconfigured percentage. When the
 * account exits out of payment plan all the subscribers under that account have
 * their credit limit raised back to the original amount.  Hence, the need to
 * create this sub with lowered credit limit (if being created under an account
 * with payment plan).
 * @author Angie Li 
 */
public class SubscriberPaymentPlanCreditLimitUpdateHome extends HomeProxy {

    public SubscriberPaymentPlanCreditLimitUpdateHome(Home delegate)
    {
        super(delegate);
    }
    
    @Override
    public Object create(Context ctx, Object obj) throws HomeException,    HomeInternalException 
    {
        Subscriber newSub = (Subscriber) obj;
        //Only do the check if the Payment Plan feature is Licensed within CRM
        if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx))
        {
            Subscriber movedFromSub = (Subscriber) ctx.get(Common.MOVE_SUBSCRIBER);
            if (movedFromSub != null )
            {
                /* This subscriber is being created because it is being moved from one account to another
                 * (the movedFromSub is deactivated and a new subscriber is created under the new BAN).
                 * We need to restore the original credit limit to this subscriber (like removing it from 
                 * payment plan).  In the event that the destination account is on a valid payment, the 
                 * subscriber's credit limit will be adjusted in part 2 of this method. */
                newSub = restoreOriginalCreditLimit(ctx, movedFromSub, newSub);
            }
            
            newSub = setPaymentPlanCreditLimit(ctx, newSub);
        }
        return super.create(ctx, newSub);
    }
    
    /*
     * We're going to overwrite the create() method, but not the store method. 
     * The store method is being used by the AccountPaymentPlanTransferBalance 
     * home to lower all subs' credit limits after first externally modifying 
     * the subscriber to be stored.
     * 
     * If the store method were to be implemented here to modify a subscriber who's
     * account is being moved onto a Payment Plan, instead of having to externally 
     * modify the subscriber before storing, then would that fix possible issues with 
     * credit limit lowering during a subscriber move/conversion? 
     * 
     * Actually, subscriber move and conversion (postpaid to prepaid) are all done
     * by deactivating the old Subscriber and create()-ing a new one.  So writing the 
     * create() method is all that is necesarry for fixing conversion and subscriber 
     * move.  
     * 
     * Prepaid to postpaid subscriber conversion still uses a store() though.
     */
    
    
    /**
     * Looks up the credit limit lowering factor and reverses the credit limit lowering
     * on the newSub. 
     * @param ctx
     * @param movedFromSub old Moved Subscriber 
     * @param newSub subcriber being created in the pipeline
     */
    private Subscriber restoreOriginalCreditLimit(Context ctx, Subscriber movedFromSub, Subscriber newSub)
    throws HomeException
    {
        Account movedFromAccount = movedFromSub.getAccount(ctx);
        //TT6010628877: We want to use the responsible parent account for all payment plan comparisons 
        movedFromAccount = movedFromAccount.getResponsibleParentAccount(ctx);
        if (PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, movedFromAccount.getPaymentPlan()))
        {
            double clAdjustment = PaymentPlanSupportHelper.get(ctx).getPaymentPlanCreditLimitLoweringFactor(ctx, movedFromAccount.getPaymentPlan());
            //Multiply by the reciprocal of the lowering factor to receive the original amount.
            long origCreditLimit = Math.round(movedFromSub.getCreditLimit(ctx) * Math.pow(clAdjustment, -1));

            writeSubscriberNote(ctx, newSub, newSub.getCreditLimit(ctx), origCreditLimit, (clAdjustment * 100), false);

            //Set subscriber's credit limit to the original credit limit.
            newSub.setCreditLimit(origCreditLimit);
        }
        return newSub;
    }
    
    /**
     * Looks up the credit limit lowering factor of the destination account and applies it 
     * to the credit limit lowering
     * on the newSub. 
     * @param ctx
     * @param newSub subcriber being created in the pipeline
     */
    private Subscriber setPaymentPlanCreditLimit(Context ctx, Subscriber newSub)
    throws HomeException
    {
        Account account = (Account) ctx.get(Account.class);
        if (account==null || !account.getBAN().equals(newSub.getBAN()))
        {
            account = newSub.getAccount(ctx);
        }
        //TT6010628877: We want to use the responsible parent account for all payment plan comparisons 
        account = account.getResponsibleParentAccount(ctx);
        
        if (PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, account.getPaymentPlan()))
        {
            // Credit Limit Decrease Factor
            double clAdjustment = PaymentPlanSupportHelper.get(ctx).getPaymentPlanCreditLimitLoweringFactor(ctx, account.getPaymentPlan());
            
            long newCreditLimit = Math.round(newSub.getCreditLimit(ctx) * clAdjustment);
            
            writeSubscriberNote(ctx, newSub, newSub.getCreditLimit(ctx), newCreditLimit, (clAdjustment * 100), true);
            
            newSub.setCreditLimit(newCreditLimit);
        }
        return newSub;
    }


    /**
     * Writes the appropriate subscriber note depending on the whether this action is to 
     * increase or decrease the credit limit.
     * @param decrease TRUE means the action is decrease, FALSE means it is an increase CL action
     */
    private void writeSubscriberNote(
            Context ctx, 
            Subscriber newSub, 
            long origCL, 
            long newCL, 
            double percentage, 
            boolean decrease)
    throws HomeException
    {
        String notemsg = "";
        if (decrease)
        {
            notemsg = "Subscriber is enrolling in an account on Payment Plan. "
                + "Credit Limit Adjustment for subscriber=" + newSub.getId()
                + " from credit limit=" 
                + formatAmount(ctx, origCL, newSub.getBAN())
                + " to "
                + formatAmount(ctx, newCL, newSub.getBAN()) 
                + ". Decreasing by: "
                + percentage + "%.";
        }
        else
        {
            notemsg = "Subscriber has moved away from an account on Payment Plan. "
                + "Credit Limit Adjustment for subscriber=" + newSub.getId()
                + " from credit limit=" 
                + formatAmount(ctx, origCL, newSub.getBAN())
                + " to "
                + formatAmount(ctx, newCL, newSub.getBAN())
                + " Increasing by: "
                + percentage + "%.";
        }
        
        NoteSupportHelper.get(ctx).addSubscriberNote(ctx, newSub.getId(), notemsg, SystemNoteTypeEnum.EVENTS,
                   SystemNoteSubTypeEnum.SUBUPDATE);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, notemsg, null).log(ctx);
        }
    }
    
    /**
     * Formats the cents amount to a dollar and cents amount using a lookup to the 
     * currency precision in the account profile.
     * @param ctx
     * @param amount
     * @param ban
     * @return String format in dollars and cents (0.00)
     */
    private String formatAmount(Context ctx, long amount, String ban)
    {
        String transAmount = String.valueOf(amount);
        try 
        {
            Account account = AccountSupport.getAccount(ctx, ban);
            if (account != null)
            {
                Currency currency = (Currency) ((Home) ctx.get(CurrencyHome.class)).find(account.getCurrency());
                if (currency != null)
                {
                    transAmount = currency.formatValue(amount);
                }
            }
            else
            {
                new MajorLogMsg(this, "Failed to look up this account=" + ban, null).log(ctx);
            }
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Failed to look up this account's currency. account=" + ban, e).log(ctx);
        }
        return transAmount;
    }
}
