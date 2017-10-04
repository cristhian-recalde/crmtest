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
package com.trilogy.app.crm.move.processor.factory;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.ReadOnlyMoveProcessor;
import com.trilogy.app.crm.move.processor.extension.MovableExtensionMoveProcessor;
import com.trilogy.app.crm.move.processor.extension.subscriber.SubscriptionExtensionCreationMoveProcessor;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequest;


/**
 * Creates an appropriate instance of an SubscriptionExtensionMoveRequest processor.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
class SubscriptionExtensionMoveProcessorFactory
{
    static <SEMR extends SubscriptionExtensionMoveRequest> MoveProcessor<SEMR> getNewInstance(Context ctx, SEMR request)
    {
        boolean validationError = false;
        
        Subscriber sub = request.getOriginalSubscription(ctx);
        if (sub == null)
        {
            new InfoLogMsg(SubscriptionExtensionMoveProcessorFactory.class, 
                    "Subscription with ID " + request.getOldSubscriptionId() + " does not exist.", null).log(ctx);
            validationError = true;
        }
        
        Subscriber newSub = request.getNewSubscription(ctx);
        if (newSub == null
                || !newSub.isSubscriberIdSet())
        {
            new InfoLogMsg(SubscriptionExtensionMoveProcessorFactory.class, 
                    "New subscription ID not set properly.  This move request can be validated but not executed.", null).log(ctx);
            validationError = true;
        }

        // Create move processor to store the request
        MoveProcessor<SEMR> processor = new DependencyMoveProcessor<SEMR>(request);

        // Add processors that perform actual move logic
        processor = new MovableExtensionMoveProcessor<SEMR>(processor);

        if (!SafetyUtil.safeEquals(request.getOldSubscriptionId(), request.getNewSubscriptionId()))
        {
            // If the extension is changing subscriptions, we need to create a new entry in the extension's home.
            processor = new SubscriptionExtensionCreationMoveProcessor<SEMR>(processor);
        }
        
        if (validationError)
        {
            new InfoLogMsg(SubscriptionExtensionMoveProcessorFactory.class, 
                    "Error occurred while creating a move processor for request " + request
                    + ".  Returning a read-only move processor so that validation can be run.", null).log(ctx);
            processor = new ReadOnlyMoveProcessor<SEMR>(
                    processor,
                    "Error occurred while creating a move processor for request " + request.toString());
        }
        
        return processor;
    }
}
