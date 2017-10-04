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
package com.trilogy.app.crm.move.dependency;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.support.MoveRequestSupport;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ConvertAccountBillingTypeDependencyManager<CABTR extends ConvertAccountBillingTypeRequest> extends AccountMoveDependencyManager<CABTR>
{
    public ConvertAccountBillingTypeDependencyManager(Context ctx, CABTR srcRequest)
    {
        super(ctx, srcRequest);
    }

    @Override
    protected MoveRequest getMoveRequestForChildAccount(Context ctx, CABTR srcRequest, Account childAccount)
    {
        return MoveRequestSupport.getMoveRequest(ctx, childAccount, ConvertAccountBillingTypeRequest.class);
    }

    @Override
    protected MoveRequest getMoveRequestForSubscription(Context ctx, CABTR srcRequest, Subscriber subscription)
    {
        MoveRequest request = MoveRequestSupport.getMoveRequest(ctx, subscription, ConvertSubscriptionBillingTypeRequest.class);
        if (request instanceof ConvertSubscriptionBillingTypeRequest)
        {
            ConvertSubscriptionBillingTypeRequest subConversionRequest = (ConvertSubscriptionBillingTypeRequest) request;

            subConversionRequest.setPricePlan(srcRequest.getPricePlan());
            subConversionRequest.setSubscriberType(srcRequest.getSystemType());
            subConversionRequest.setSubscriptionClass(srcRequest.getSubscriptionClass());
            subConversionRequest.setNewDepositAmount(srcRequest.getNewDepositAmount());
            subConversionRequest.setCreditLimit(srcRequest.getNewCreditLimit());
            subConversionRequest.setInitialAmount(srcRequest.getInitialAmount());
        }
        return request;
    }
}
