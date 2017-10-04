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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.request.factory;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequest;
import com.trilogy.app.crm.move.request.ReverseActivationMoveRequest;
import com.trilogy.framework.xhome.context.Context;


/**
 * This is the main version of the move request factory for CRM.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class CRMMoveRequestFactory extends DefaultMoveRequestFactory
{
    private static MoveRequestFactory CRMInstance_ = null;
    public static MoveRequestFactory instance()
    {
        if (CRMInstance_ == null)
        {
            CRMInstance_ = new CRMMoveRequestFactory();
        }
        return CRMInstance_;
    }
    
    protected CRMMoveRequestFactory()
    {
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public MoveRequest getInstance(Context ctx, Object bean, Class<? extends MoveRequest> preferredType)
    {
        MoveRequest request = super.getInstance(ctx, bean, preferredType);
        
        if (bean instanceof Account)
        {
            if (AccountExtensionMoveRequest.class.isAssignableFrom(preferredType))
            {
                request = AccountExtensionMoveRequestFactory.getInstance(ctx, (Account)bean);
            }
            else if (ConvertAccountBillingTypeRequest.class.isAssignableFrom(preferredType))
            {
                request = ConvertAccountBillingRequestFactory.getInstance(ctx, (Account) bean);
            }
            else if (ConvertAccountGroupTypeRequest.class.isAssignableFrom(preferredType))
            {
                request = ConvertAccountGroupRequestFactory.getInstance(ctx, (Account) bean);
            }
            else
            {
                request = AccountMoveRequestFactory.getInstance(ctx, (Account)bean);
            }
        }
        else if (bean instanceof AccountExtension)
        {
            request = AccountExtensionMoveRequestFactory.getInstance(ctx, (AccountExtension)bean);
        }
        else if (bean instanceof Subscriber)
        {
            if (SubscriptionExtensionMoveRequest.class.isAssignableFrom(preferredType))
            {
                request = SubscriptionExtensionMoveRequestFactory.getInstance(ctx, (Subscriber)bean);
            }
            else if (ConvertSubscriptionBillingTypeRequest.class.isAssignableFrom(preferredType))
            {
                request = ConvertSubscriptionBillingRequestFactory.getInstance(ctx, (Subscriber)bean);
            }
            else if (ReverseActivationMoveRequest.class.isAssignableFrom(preferredType))
            {
                request = ReverseActivationMoveRequestFactory.getInstance(ctx, (Subscriber)bean);
            }
            else
            {
                request = SubscriptionMoveRequestFactory.getInstance(ctx, (Subscriber)bean);
            }
        }
        else if (bean instanceof SubscriberExtension)
        {
            request = SubscriptionExtensionMoveRequestFactory.getInstance(ctx, (SubscriberExtension)bean);
        }
        
        return request;
    }
}
