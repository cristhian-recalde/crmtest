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
package com.trilogy.app.crm.move.processor.subscription.strategy;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.RemoveAllVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.web.service.CloneOldSubscriberSavedServicesVisitor;


/**
 * Responsible for moving service entities from the old subscription to the new one.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class ServiceCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public ServiceCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
        super.initialize(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        if (!ctx.has(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException(
                "Custom subscriber home not installed in context."));
        }

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        super.createNewEntity(ctx, request);

        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Subscriber newSubscription = request.getNewSubscription(ctx);

        Home subSvcHome = (Home) ctx.get(SubscriberServicesHome.class);
        subSvcHome = subSvcHome.where(ctx, new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, oldSubscription.getId()));
        try
        {
            new DebugLogMsg(this, "Copying service entries from subscription " + oldSubscription.getId() + " to "
                    + newSubscription.getId() + "...", null).log(ctx);
            subSvcHome.forEach(ctx, new CloneOldSubscriberSavedServicesVisitor(subSvcHome, newSubscription));
            new InfoLogMsg(this, "Copied service entries successfully from subscription " + oldSubscription.getId()
                    + " to " + newSubscription.getId() + ".", null).log(ctx);
        }
        catch (HomeException e)
        {
            throw new MoveException(request, "Error occured while trying to move services " + " from subscriber "
                    + oldSubscription.getId() + " to subscriber " + newSubscription.getId(), e);
        }
        // Initialize the transient Subscriber Services fields (at least
        // servicesForDisplay to indicate intent)
        new DebugLogMsg(this, "Initializing transient subscriber services fields for subscription "
                + newSubscription.getId() + "...", null).log(ctx);
        newSubscription.populateSubscriberServices(ctx, false);
        new DebugLogMsg(this, "Initialized transient subscriber services fields successfully for subscription "
                + newSubscription.getId() + "...", null).log(ctx);
        try
        {
            // For services to be updated properly
            Home subscriberHome = (Home) ctx.get(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY);
            new DebugLogMsg(this, "Updating subscription " + newSubscription.getId() + " in home...", null)
            .log(ctx);
            subscriberHome.store(ctx, newSubscription);
            new DebugLogMsg(this, "Updated subscription " + newSubscription.getId() + " successfully.", null)
            .log(ctx);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error occurred updating subscription " + newSubscription.getId()
                    + " with new subscriber services in place.  Proceeding with move.", e).log(ctx);
        }
    }
    
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);

        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Home subSvcHome = (Home) ctx.get(SubscriberServicesHome.class);
        subSvcHome = subSvcHome.where(ctx, new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, oldSubscription.getId()));
        try
        {
            // Remove all the services from the old Subscriber.
            new DebugLogMsg(this, "Removing service entries for subscription " + oldSubscription.getId() + "...", null).log(ctx);
            subSvcHome.forEach(ctx, new RemoveAllVisitor(subSvcHome));
            new InfoLogMsg(this, "Removed service entries successfully for subscription " + oldSubscription.getId(), null).log(ctx);
        }
        catch (HomeException e)
        {
            throw new MoveException(request, "Error removing subscriber services for moved subscription " + oldSubscription.getId(), e);
        }
    }
}
