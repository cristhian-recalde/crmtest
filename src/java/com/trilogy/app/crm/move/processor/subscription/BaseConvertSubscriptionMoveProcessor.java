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
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequestXInfo;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.PricePlanSupport;


/**
 * This processor is responsible for performing all validation that applies
 * to ANY subscription conversion scenario.  It is also responsible for performing any
 * setup that is common to ANY subscription conversion scenario.
 * 
 * It does not implement any subscription conversion business logic, modify the request,
 * or modify the subscriptions involved.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class BaseConvertSubscriptionMoveProcessor<CSBTR extends ConvertSubscriptionBillingTypeRequest> extends MoveProcessorProxy<CSBTR>
{
    public BaseConvertSubscriptionMoveProcessor(CSBTR request)
    {
        super(new DependencyMoveProcessor<CSBTR>(request));
    }
    
    public BaseConvertSubscriptionMoveProcessor(MoveProcessor<CSBTR> delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        CSBTR request = this.getRequest();
        
        Account newAccount = SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        if (newAccount != null
                && !SubscriberTypeEnum.HYBRID.equals(newAccount.getSystemType())
                && !newAccount.getSystemType().equals(request.getSubscriberType()))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    ConvertSubscriptionBillingTypeRequestXInfo.SUBSCRIBER_TYPE, 
                    "Account billing type must be " + request.getSubscriberType() + " or " + SubscriberTypeEnum.HYBRID + "."));
        }

        Subscriber oldSubscription = SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        if (oldSubscription != null)
        {
            if ( oldSubscription.getSubscriberType().equals(request.getSubscriberType()))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        ConvertSubscriptionBillingTypeRequestXInfo.SUBSCRIBER_TYPE, 
                        "Unnecessary conversion."));
            }

            if ( ! oldSubscription.getState().equals(SubscriberStateEnum.ACTIVE))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        ConvertSubscriptionBillingTypeRequestXInfo.OLD_SUBSCRIPTION_ID, 
                        "Subscription State has to be Active"));
            }
        }

        long ppId = request.getPricePlan();
        try
        {
            PricePlan pp = PricePlanSupport.getPlan(ctx, ppId);
            if (pp == null)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        ConvertSubscriptionBillingTypeRequestXInfo.PRICE_PLAN, 
                        "Price plan " + ppId + " not found."));
            }
            else if (!pp.getPricePlanType().equals(request.getSubscriberType()))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        ConvertSubscriptionBillingTypeRequestXInfo.PRICE_PLAN, 
                        "Price Plan type does not match the conversion billing type."));

            }
        }
        catch (HomeException e)
        {
            String msg = "Error occurred retrieving price plan " + ppId;
            new MinorLogMsg(this, msg, e).log(ctx);
            cise.thrown(new IllegalPropertyArgumentException(
                    ConvertSubscriptionBillingTypeRequestXInfo.PRICE_PLAN, 
                    msg + ". See logs for errors."));
        }
        
        if(SubscriberTypeEnum.POSTPAID.equals(request.getSubscriberType()))
        {
	        if(	request.getCreditLimit() <= 0 )
	        {
	        	LogSupport.major(ctx, this, "Credit Limit cannot be <= 0 for Postpaid Subscriber.");
	        	
	        	cise.thrown(new IllegalPropertyArgumentException(ConvertSubscriptionBillingTypeRequestXInfo.CREDIT_LIMIT, 
	        			"Credit Limit cannot be less than or equal to zero for Postpaid Subscriber."));
	        }
	        else if(request.getNewSubscription(ctx)!= null 
	        		&& request.getNewSubscription(ctx).getMonthlySpendLimit() > request.getCreditLimit())
	        {
	        	LogSupport.major(ctx, this, "Credit Limit can not be less than Monthly Spend Limit for postpaid subscriber.");
	        	
	        	cise.thrown(new IllegalPropertyArgumentException(ConvertSubscriptionBillingTypeRequestXInfo.CREDIT_LIMIT, 
	        			"Credit Limit can not be less than Monthly Spend Limit for postpaid subscriber."));
	        }
        }
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }
}
