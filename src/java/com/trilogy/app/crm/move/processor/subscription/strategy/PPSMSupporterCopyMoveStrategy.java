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
 */package com.trilogy.app.crm.move.processor.subscription.strategy;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Copy move strategy used to validate if subscription is not a PPSM Supporter before allowing
 * a conversion.
 * @author Marcio Marques
 * @since 8.5
 *
 * @param <SMR>
 */
public class PPSMSupporterCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public PPSMSupporterCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        Subscriber oldSubscription = request.getOldSubscription(ctx);
        try
        {
            if (oldSubscription.isPostpaid() && PPSMSupporterSubExtension.getPPSMSupporterSubscriberExtension(ctx, oldSubscription.getId()) != null)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                        "Subscription (ID=" + request.getOldSubscriptionId() + ") is a PPSM Supporter and therefore cannot be converted to PREPAID."));
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving PPSM Supporter subscriber extension for subscription '" + request.getOldSubscriptionId() + "'.", e).log(ctx);
        }

        cise.throwAll();
        
        super.validate(ctx, request);
    }
    }
