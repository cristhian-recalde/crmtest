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
package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.support.LicensingSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Evaluates if the SubscriberTypeEnum can be displayed based on the pooled account criteria
 * @author arturo.medina@redknee.com
 *
 */
public class SubscriberTypeInPooledAccountEnumPredicateEvaluator extends
        AbstractPredicateEvaluator implements EnumPredicateEvaluator
{

    /**
     * 
     * @param delegate
     */
    public SubscriberTypeInPooledAccountEnumPredicateEvaluator(EnumPredicateEvaluator delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    public boolean evaluate(Context ctx, AbstractEnum value)
    {
        boolean result = false;
        
        if (value == null || !(value instanceof SubscriberTypeEnum))
        {
            return result;
        }
        
        Subscriber subscriber = null;        
        Object obj = ctx.get(AbstractWebControl.BEAN);
        
        if ( obj instanceof ConvertSubscriptionBillingTypeRequest )
        {
            ConvertSubscriptionBillingTypeRequest conversion = (ConvertSubscriptionBillingTypeRequest) obj;
            subscriber = conversion.getOldSubscription(ctx);
        }
        else
        {
            subscriber = (Subscriber) obj;
        }
        



        SubscriberTypeEnum subType = (SubscriberTypeEnum)value;
        
        if (subscriber != null)
        {
            try
            {
                Account account = subscriber.getAccount(ctx);
                if( account.isPooled(ctx) )
                {
                    if( subType.equals(SubscriberTypeEnum.PREPAID) && subType.equals(account.getSystemType())
                            && LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PREPAID_GROUP_POOLED_LICENSE_KEY) )
                    {
                        // OID 36148 - If the account is PREPAID, then it must be a PREPAID subscriber.
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "account type is prepaid and the license for prepaod group pooled account is set");
                        }
                        result = true;
                    }
                    else if( (subType.equals(SubscriberTypeEnum.POSTPAID) && subType.equals(account.getSystemType()))
                            || SubscriberTypeEnum.HYBRID.equals(account.getSystemType()) )
                    {
                        // OID 36148 - If the account is POSTPAID or HYBRID, then it must be a POSTPAID subscriber.
                        if( (subType.equals(SubscriberTypeEnum.POSTPAID) && subType.equals(account.getSystemType()))
                                || subscriber.isPooledGroupLeader(ctx)
                                || !subscriber.isPooled(ctx))
                        {
                            // This subscriber is one of:
                            // a) in a postpaid group-pooled account
                            // b) the group leader of a hybrid group-pooled account
                            // c) the first subscriber in a hybrid group-pooled account (which hasn't had its MSISDN set yet)
                            if (LogSupport.isDebugEnabled(ctx))
                            {
                                LogSupport.debug(ctx, this, "account type is postpaid ");
                            }
                            result = true;
                        }
                    }
                }
                else
                {
                    result = delegate(ctx, value);
                }
            }
            catch (HomeException e)
            {
                LogSupport.major(ctx, this, "Home Exception occured while evaluating the pooled account ", e);
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Subscriber is null setting the flag as false");
            }
        }
        
        return result;
    }

}
