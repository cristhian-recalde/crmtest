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
package com.trilogy.app.crm.move.dependency;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.MoveRequestSupport;


/**
 * Given a subscription move request, this class calculates its dependencies.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SubscriptionMoveDependencyManager<SMR extends SubscriptionMoveRequest> extends AbstractMoveDependencyManager<SMR>
{
    public SubscriptionMoveDependencyManager(Context ctx, SMR srcRequest)
    {
        super(ctx, srcRequest);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected Collection<? extends MoveRequest> getDependencyRequests(Context ctx, SMR request) throws MoveException
    {
        Collection<MoveRequest> dependencies = new ArrayList<MoveRequest>();
        
        Subscriber newSub = request.getNewSubscription(ctx);
        if (newSub != null)
        {
            dependencies.addAll(
                    getExtensionDependencies(
                            ctx,
                            request));
        }

        return dependencies;
    }


    private Collection<? extends MoveRequest> getExtensionDependencies(Context ctx, SMR srcRequest)
    {
        Collection<MoveRequest> extensionDependencies = new ArrayList<MoveRequest>();
        
        MoveRequest dependency = getMoveRequestForExtensions(ctx, srcRequest);

        if (dependency instanceof SubscriptionExtensionMoveRequest)
        {
            SubscriptionExtensionMoveRequest extRequest = (SubscriptionExtensionMoveRequest) dependency;
            
            extRequest.setNewSubscriptionId(srcRequest.getNewSubscription(ctx));
        }
        
        if (dependency != null)
        {
            extensionDependencies.add(dependency);   
        }
        
        return extensionDependencies;
    }

    
    protected MoveRequest getMoveRequestForExtensions(Context ctx, SMR srcRequest)
    {
        // Don't pass the frozen original subscription to the move request factory.
        // It may want to store it as the non-frozen "old" subscription and create
        // its own frozen original copy.
        return MoveRequestSupport.getMoveRequest(ctx, srcRequest.getOldSubscription(ctx), SubscriptionExtensionMoveRequest.class);
    }
}
