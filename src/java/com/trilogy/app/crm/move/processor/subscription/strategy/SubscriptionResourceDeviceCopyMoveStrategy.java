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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.ResourceDevice;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.util.snippet.log.Logger;


/**
 * Handles Resource Device related processing required to move subscriptions.
 * 
 * It must associate its Resource Device with the new subscription, which must happen in the createNewEntity method.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author victor.stratan@redknee.com
 * @since 8.3
 */
public class SubscriptionResourceDeviceCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public SubscriptionResourceDeviceCopyMoveStrategy(final CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(final Context ctx, final SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(final Context ctx, final SMR request) throws MoveException
    {
        super.createNewEntity(ctx, request);

        final Subscriber oldSubscription = request.getOldSubscription(ctx);
        final String resourceID = oldSubscription.getResourceID(ctx);
        if (resourceID != null && resourceID.length() > 0)
        {
            try
            {
                if (Logger.isDebugEnabled())
                {
                    Logger.debug(ctx, this, "Moving Resource [" + resourceID
                            + "] from Subscription [" + request.getOldSubscriptionId()
                            + "] to Subscription [" + request.getNewSubscriptionId() + "]");
                }
                ResourceDevice.switchSubscription(ctx, resourceID,
                        request.getOldSubscriptionId(), request.getNewSubscriptionId());
                if (Logger.isInfoEnabled())
                {
                    Logger.info(ctx, this, "Successfully moved Resource [" + resourceID
                            + "] from Subscription [" + request.getOldSubscriptionId()
                            + "] to Subscription [" + request.getNewSubscriptionId() + "]");
                }
            }
            catch (HomeException e)
            {
                Logger.minor(ctx, this, "Error occurred while Moving Resource [" + resourceID
                            + "] from Subscription [" + request.getOldSubscriptionId()
                            + "] to Subscription [" + request.getNewSubscriptionId() + "] : " + e.getMessage(), e);
            }
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(final Context ctx, final SMR request) throws MoveException
    {
        // Nothing to do, because we had update the Resource Device record in create.
        super.removeOldEntity(ctx, request);
    }
}
