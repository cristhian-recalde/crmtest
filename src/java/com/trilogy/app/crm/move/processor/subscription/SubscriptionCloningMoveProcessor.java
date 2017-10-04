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
package com.trilogy.app.crm.move.processor.subscription;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.home.ContextFallbackFindHome;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.CopyMoveProcessor;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;

public class SubscriptionCloningMoveProcessor<SMR extends SubscriptionMoveRequest> extends CopyMoveProcessor<SMR>
{
    public SubscriptionCloningMoveProcessor(SMR request, CopyMoveStrategy<SMR> strategy)
    {
        this(new BaseSubscriptionMoveProcessor<SMR>(request), strategy);
    }


    public SubscriptionCloningMoveProcessor(MoveProcessor<SMR> delegate, CopyMoveStrategy<SMR> strategy)
    {
        super(delegate, strategy);
    }
    

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        SMR request = this.getRequest();

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {        
        SMR request = getRequest();
        
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Subscriber newSubscription = null;
        if (oldSubscription != null)
        {            
            try
            {
                new DebugLogMsg(this, "Cloning subscription " + oldSubscription.getId() + " in order to move it to account " + request.getNewBAN() + "...", null).log(ctx);
                newSubscription = (Subscriber) oldSubscription.deepClone();
            }
            catch (Exception e)
            {
                throw new MoveException(request, "Error occurred initializing copy of subscription " + oldSubscription.getId() + ".", e);
            }
            
            if (newSubscription != null)
            {
                // Lazy-load everything that needs to be moved with the account before setting the new Subscription ID.
                lazyLoadPropertiesToMove(ctx, request, newSubscription);
                newSubscription.setId(request.getNewSubscriptionId());
                request.setNewSubscriptionId(newSubscription);
            
                new DebugLogMsg(this, "Subscription " + oldSubscription.getId() + " cloned and initialized successfully.", null).log(ctx);
            }
        }

        Context moveCtx = super.setUp(ctx);

        newSubscription = request.getNewSubscription(ctx);
        if (newSubscription != null)
        {
            // Give the illusion to the rest of CRM that this subscription already exists
            Object id = newSubscription.ID();
            moveCtx.put(id.getClass().getName() + "_" + id, newSubscription);
            
            Home subHome = (Home) moveCtx.get(SubscriberHome.class);
            if (!(subHome instanceof HomeProxy
                    && ((HomeProxy)subHome).hasDecorator(ContextFallbackFindHome.class)))
            {
                subHome = new ContextFallbackFindHome(moveCtx, subHome);
                moveCtx.put(SubscriberHome.class, subHome);
                new InfoLogMsg(this, "Installed home to retrieve transient copies of subscriptions involved in move requests.", null).log(ctx);
            }
        }
        
        return moveCtx;
    }


    private void lazyLoadPropertiesToMove(Context ctx, SMR request, Subscriber newSubscription)
    {
        Subscriber originalSubscription = request.getOriginalSubscription(ctx);
        if (originalSubscription == null)
        {
            originalSubscription = request.getOldSubscription(ctx);
        }
        if (originalSubscription != null)
        {
            newSubscription.setSubExtensions(originalSubscription.getSubExtensions(ctx));
        }
    }
}
