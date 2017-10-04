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

import java.util.Date;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.DeactivatedReasonEnum;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.UpdateReasonEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;


/**
 * This copy strategy is responsible for performing all validation that applies
 * to ANY subscription move scenario.  It is also responsible for performing any
 * intialization that is common to ANY subscription move scenario.  Any validation
 * that is specific to a subset of move scenarios should be implemented in
 * a different processor.
 * 
 * It does not implement any subscription move business logic, modify the request,
 * or modify the subscriptions/accounts involved.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class BaseSubscriptionCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public BaseSubscriptionCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {   
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        if (oldSubscription != null)
        {
            oldSubscription.setDeactivatedReason(DeactivatedReasonEnum.MOVE);
            try
            {
                dealerCode_ = getDealerCode(ctx, oldSubscription);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error retrieving dealer code for subscription " + oldSubscription.getId(), e).log(ctx);
            }

            if (dealerCode_ != null)
            {
                // Set the dealer code to that of the old subscriber's package
                oldSubscription.setDealerCode(dealerCode_);
            }
        }
        
        Subscriber newSubscription = request.getNewSubscription(ctx);
        if (newSubscription != null)
        {
            try
            {
                ppVersion_  = PricePlanSupport.getCurrentVersion(ctx, newSubscription.getPricePlan());
            }
            catch (HomeException e)
            {
                new MajorLogMsg(this, "Error retrieving current price plan version for price plan " + newSubscription.getPricePlan() + " for subscription " + oldSubscription.getId(), e).log(ctx);
            }
            
            newSubscription.setBAN(request.getNewBAN());
            newSubscription.setLastModified(new Date());
            newSubscription.setUpdateReason(UpdateReasonEnum.MOVE);
         
            
            // Set deposit amount to 0, deposit transaction will correct new subscriber's deposit
            newSubscription.setDeposit(0);

            if (dealerCode_ != null)
            {
                newSubscription.setDealerCode(dealerCode_);
            }
            
            if (ppVersion_ != null)
            {
                /*
                 * TT 6110741257 Set the new subscriber's price plan version to the most recent one.
                 * This is required since the new subscriber ID is not in PricePlanVersionUpdate table.
                 * 
                 * This prevents the situation where the price plan version of the old subscriber is
                 * slated for update, but the actual version update has not been carried over yet.
                 */
                new DebugLogMsg(this, 
                        "Setting Price Plan Version of new subscription (ID=" + newSubscription.getId() + ") to " + ppVersion_.getVersion()
                        + " (from subscription " + request.getOldSubscriptionId() + "'s Price Plan Version " + newSubscription.getPricePlanVersion() + ")", null).log(ctx);
                newSubscription.setPricePlanVersion(ppVersion_.getVersion());
            }
        }
        
        super.initialize(ctx, request);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);

        SubscriptionMoveValidationSupport.validateAllowMoveGroupLeader(ctx, request, cise);
        
        Subscriber newSubscription = SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        if (newSubscription != null
                && ppVersion_ == null)
        {
            cise.thrown(new IllegalStateException("Could not find current price plan version for price plan " + newSubscription.getPricePlan() + " for subscription " + newSubscription.getId()));
        }
                
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
        Subscriber newSubscription = request.getNewSubscription(ctx);

        SubscriptionTypeEnum subscriptionType = null;
        SubscriptionType type = request.getSubscriptionType(ctx);
        if (type != null)
        {
            subscriptionType = type.getTypeEnum();
        }
        
        new DebugLogMsg(this, "Creating " + subscriptionType + " subscription " + newSubscription.getId() + "...", null).log(ctx);
        super.createNewEntity(ctx, request); 
        new InfoLogMsg(this, subscriptionType + " subscription " + newSubscription.getId() + " (Account BAN=" + newSubscription.getBAN() + ") created successfully.", null).log(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        
        oldSubscription.setState(SubscriberStateEnum.INACTIVE);
        
        /*
         * NOTE - 2004-05-24 - Need to prevent the old subscriber from being found when
         * the roaming system searches by IMSI. There needs to be a better way of
         * controlling the ownership of Package/IMSI.
         */
        oldSubscription.setIMSI("0");
        oldSubscription.setEndDate(new Date());

        SubscriptionTypeEnum subscriptionType = null;
        SubscriptionType type = request.getSubscriptionType(ctx);
        if (type != null)
        {
            subscriptionType = type.getTypeEnum();
        }
        
        new DebugLogMsg(this, "Deactivating " + subscriptionType + " subscription " + oldSubscription.getId() + "...", null).log(ctx);
        super.removeOldEntity(ctx, request);
        new InfoLogMsg(this, subscriptionType + " subscription " + oldSubscription.getId() + " (Account BAN=" + oldSubscription.getBAN() + ") deactivated successfully.", null).log(ctx);
    }


    /**
     * Retrieves the dealer code of the subscriber's current package.
     *
     * @param context
     *            The operating context
     * @param subscriber
     *            The subscriber in question
     * @return The dealer code associated with the current package of the subscriber
     * @throws HomeException
     *             Thrown if the package is not found
     */
    private String getDealerCode(final Context context, final Subscriber subscriber) throws HomeException
    {
        GenericPackage pkg = null;
        if (TechnologyEnum.GSM == subscriber.getTechnology())
        {
            pkg = PackageSupportHelper.get(context).getGSMPackage(context, subscriber.getPackageId());
        }
        else
        {
            pkg = PackageSupportHelper.get(context).getTDMAPackage(context, subscriber.getPackageId(), subscriber.getSpid());
        }
        if (pkg != null)
        {
            return pkg.getDealer();
        }
        return "";
    }
    
    private String dealerCode_ = null;
    private PricePlanVersion ppVersion_ = null;
}
