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
package com.trilogy.app.crm.move.processor.subscription;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AbstractConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequestXInfo;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.CreditCategorySupport;
import com.trilogy.app.crm.support.PricePlanSupport;


/**
 * This processor is responsible initializing the new subscription instance with values from the request.
 * 
 * It does not implement any subscription conversion business logic or modify the request.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ConvertSubscriptionInitializingMoveProcessor<CSBTR extends ConvertSubscriptionBillingTypeRequest> extends MoveProcessorProxy<CSBTR>
{
    public ConvertSubscriptionInitializingMoveProcessor(MoveProcessor<CSBTR> delegate)
    {
        super(delegate);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {
        Context moveCtx = super.setUp(ctx);
        
        CSBTR request = this.getRequest();
        
        Subscriber newSubscription = request.getNewSubscription(ctx);
        if (newSubscription != null)
        {
            long ppId = request.getPricePlan();
            newSubscription.setSubscriberType(request.getSubscriberType());
            newSubscription.setPricePlan(ppId);
            if(request.getSubscriptionClass() > 0)
            { // update only if requested, else will be retained same as old subscriber
            	newSubscription.setSubscriptionClass(request.getSubscriptionClass());
            }
            
            long newDepositAmount = request.getNewDepositAmount();
            long creditLimit = request.getCreditLimit();
            try
            {
                PricePlanVersion ppv = PricePlanSupport.getCurrentVersion(ctx, newSubscription.getPricePlan());
                if (ppv != null)
                {
                    newSubscription.setPricePlanVersion(ppv.getVersion());
                    if (newSubscription.isPostpaid()
                            && creditLimit == AbstractConvertSubscriptionBillingTypeRequest.DEFAULT_CREDITLIMIT)
                    {
                        creditLimit = ppv.getCreditLimit();                        
                    }
                    
                    if (newSubscription.isPostpaid()
                            && newDepositAmount == AbstractConvertSubscriptionBillingTypeRequest.DEFAULT_NEWDEPOSITAMOUNT)
                    {
                        newDepositAmount = ppv.getDeposit();
                    }
                    
                }
                else
                {
                    new MinorLogMsg(this, "Price Plan Version does not exist for Price Plan " + newSubscription.getPricePlan(), null).log(ctx);
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error retrieving Price Plan Version for Price Plan " + newSubscription.getPricePlan(), e).log(ctx);
            }

                
            newSubscription.setInitialBalance((request.getInitialAmount()));
            newSubscription.setMonthlySpendLimit(AbstractSubscriber.DEFAULT_MONTHLYSPENDLIMIT);
            if (newSubscription.isPostpaid())
            {
                newSubscription.setDeposit(newDepositAmount);
                newSubscription.setCreditLimit(creditLimit);
                Account newAccount = request.getNewAccount(ctx);
                
                if (newAccount != null)
                {
                    try
                    {
                        CreditCategory cc = CreditCategorySupport.findCreditCategory(ctx, newAccount
                                .getCreditCategory());
                        newSubscription.setMonthlySpendLimit(cc.getMonthlySpendLimit());
                    }
                    catch (HomeException homeEx)
                    {
                        new MinorLogMsg(this, " Unable to find  credit category " + newAccount.getCreditCategory(), homeEx)
                                .log(ctx);
                    }
                }
            }
            else if(newSubscription.isPrepaid())
            {
                newSubscription.setMonthlySpendLimit(-1);
                newSubscription.setDeposit(0);
                newSubscription.setCreditLimit(0);
            }
            
            newSubscription.setBalanceRemaining(0);
        }
        
        return moveCtx;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        CSBTR request = this.getRequest();
        
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        PricePlanVersion ppv = null; 
        try
        {
            ppv = PricePlanSupport.getCurrentVersion(ctx, request.getPricePlan());
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving Price Plan Version for Price Plan " + request.getPricePlan(), e).log(ctx);
        }
        if (ppv == null)
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    ConvertSubscriptionBillingTypeRequestXInfo.PRICE_PLAN, 
                    "Price Plan Version for Price Plan (ID='" + request.getPricePlan() + "') does not exist."));
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }
    
    
}
