/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.account;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;

/**
 * @author ali
 *
 * This home makes sure that Payment Plan Balance is always updated.
 * 
 * There is a need for this since updating to install/remove the Payment Plan 
 * requires numerous steps that could throw errors.  We want the payment plan 
 * remaining balance to always update.  
 * 
 * See TT 5112127045 for other details:
 * "-In the event of an error during an update to the account profile (i.e. credit 
 * limit decrease/increase) due to enrollment/removal of Payment Plan on the 
 * account, the account profile should not be saved.
 * The reason for this is that the error message will show up (i.e. "Payment Plan
 * Failed to modify credit limit for subscriber...") and the CSR will have to
 * do that manually, and then they will have to remove the payment plan manually too. 
 * 
 * BUT, although the account profile is not supposed to successfully update to
 * remove the payment plan (during error), it is important to update
 * the Payment Plan Bucket Balance (on the account profile), since the payment
 * transactions have already gone through. "
 * 
 * The sole purpose of this class is to wrap the AccountPaymentPlanTransferBalanceHome 
 * and make sure that if any errors occur because of that Home, that a attempt 
 * be made to update the Payment Plan Bucket Balance (by 'store'-ing the account profile
 * in memory with the update to that attribute.
 * 
 */
public class AccountPaymentPlanBalanceUpdateHome extends HomeProxy {

    public AccountPaymentPlanBalanceUpdateHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    /**
     * Check for the value put into the context. Save the stored amount in to the account.
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        try
        {
            return super.store(ctx, obj);
        }
        catch (HomeException ex)
        {
            if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx))
            {
                //Continue to store the payment plan balance in the context.
                try
                {
                    /* It is worth noting that if an AccountHome store failed thus far,
                     * there is no reason to believe a second AccountHome store (as we 
                     * will do below) will work.
                     * However, the second AccountHome store is attempted so that we 
                     * can at least recover from the possible SubscriberHome store errors
                     * that can occur from AccountPaymentPlanTransferBalanceHome.
                     */
                    Account account = (Account) ctx.get(Account.class);
                    Long amount = (Long) ctx.get(Lookup.PAYMENTPLAN_BALANCE);
                    if (amount != null && account != null)
                    {
                        if(LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this,
                                    "Attempting to save account profile to update PAYMENTPLAN_BALANCE to " + amount, null).log(ctx);
                        }
                        account.setPaymentPlanAmount(amount.longValue());
                        super.store(ctx, account);
                        if(LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this,
                                    "Update of PAYMENTPLAN_BALANCE to this account succeeded.", null).log(ctx);
                        }
                    }
                }
                catch (HomeException e)
                {
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this,
                                "Failed to save account profile to update PAYMENTPLAN_BALANCE.", e).log(ctx);
                    }
                }
            }
            //Throw original errors
            throw ex;
        }
    }
    
}
