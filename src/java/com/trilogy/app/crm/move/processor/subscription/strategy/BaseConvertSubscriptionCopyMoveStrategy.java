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
package com.trilogy.app.crm.move.processor.subscription.strategy;

import java.util.Date;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ActivationReasonCode;
import com.trilogy.app.crm.bean.DeactivatedReasonEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.UpdateReasonEnum;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.ActivationReasonCodeSupport;


/**
 * This copy strategy is responsible for performing all validation that applies
 * to ANY subscription conversion scenario.  It is also responsible for performing any
 * intialization that is common to ANY subscription conversion scenario.  Any validation
 * that is specific to a subset of conversion scenarios should be implemented in
 * a different processor.
 * 
 * It does not implement any subscription conversion business logic, modify the request,
 * or modify the subscriptions/accounts involved.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class BaseConvertSubscriptionCopyMoveStrategy<CSBTR extends ConvertSubscriptionBillingTypeRequest> extends CopyMoveStrategyProxy<CSBTR>
{
    public BaseConvertSubscriptionCopyMoveStrategy(CopyMoveStrategy<CSBTR> delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Context ctx, CSBTR request)
    {
        Subscriber newSubscription = request.getNewSubscription(ctx);
        if (newSubscription != null)
        {
            newSubscription.setUpdateReason(UpdateReasonEnum.CONVERSION);
            newSubscription.setStartDate(new Date());
            
            //Nulling out the contract values
            newSubscription.setSubscriptionContract(-1);
            newSubscription.setSubscriptionContractEndDate(null);
            newSubscription.setSubscriptionContractStartDate(null);

            try
            {
                // Settign the activation reason code as "Conversion"
                ActivationReasonCode reasonCode = ActivationReasonCodeSupport.getConversionActivationReasonCode(ctx,
                        newSubscription.getSpid());
                if (reasonCode != null)
                {
                    newSubscription.setReasonCode(reasonCode.getReasonID());
                }
            }
            catch (HomeException ex)
            {
                new MinorLogMsg(this,"Unable find activation reason code, during conversion of subId" + request.getOldSubscriptionId(), ex).log(ctx);
            }
        }

        Subscriber oldSubscription = request.getOldSubscription(ctx);
        if (oldSubscription != null)
        {
            oldSubscription.setDeactivatedReason(DeactivatedReasonEnum.CONVERSION);
        }
        
        super.initialize(ctx, request);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, CSBTR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
                
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }
}
