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

import org.omg.CORBA.LongHolder;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.product.s2100.ErrorCode;


/**
 * This copy strategy is responsible for updating the fields in BMGT according to
 * paid type conversion requirements.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class ConvertBMSubscriptionCopyMoveStrategy<CSBTR extends ConvertSubscriptionBillingTypeRequest> extends CopyMoveStrategyProxy<CSBTR>
{
    public ConvertBMSubscriptionCopyMoveStrategy(CopyMoveStrategy<CSBTR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, CSBTR request)
    {
        super.initialize(ctx, request);
        
        bmClient_  = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, CSBTR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        if (bmClient_ == null)
        {
            cise.thrown(new IllegalStateException(
                    "No Balance Management provisioning client installed.  Unable to update external profile."));
        }

        Subscriber oldSubscription = SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        if (oldSubscription != null
                && bmClient_ != null)
        {
            // Validate that old subscription profile exists in balance management application
            try
            {                
                Parameters profile = bmClient_.querySubscriptionProfile(ctx, oldSubscription);
                if (profile == null)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                            "Subscription (ID=" + request.getOldSubscriptionId() + ") does not exist in Balance Management application."));
                }
            }
            catch (final Exception e)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                        "Failed to retrieve subscription (ID=" + request.getOldSubscriptionId()
                        + ") from Balance Management application."));
            }
        }

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        Account newAccount = SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        if (newAccount != null)
        {
            if (newAccount.isBANSet()
                    && bmClient_ != null)
            {
                // Validate that new account profile exists in balance management application
                try
                {
                    Parameters profile = bmClient_.querySubscriberAccountProfile(ctx, newAccount);
                    if (profile == null)
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                SubscriptionMoveRequestXInfo.NEW_BAN, 
                                "Subscriber profile for account (BAN=" + newAccount.getBAN()
                                +") does not exist in Balance Management application."));
                    }
                }
                catch (final Exception e)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            SubscriptionMoveRequestXInfo.NEW_BAN, 
                            "Failed to retrieve subscriber profile for account (BAN=" + newAccount.getBAN()
                            +") from Balance Management application."));
                }
            }
            else
            {
                new DebugLogMsg(this, "Skipping validation of new account's presence in Balance Management "
                        + "application because it hasn't been created yet (would normally be done during "
                        + "the conversion execution).", null).log(ctx);
            }
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, CSBTR request) throws MoveException
    {
        try
        {
            bmClient_.deleteSubscriptionProfile(ctx, request.getOldSubscription(ctx));
        }
        catch (HomeException homeEx)
        {
            new MinorLogMsg(this, " Unable remove subscription " + request.getOldSubscriptionId() + " from URCS",
                    homeEx).log(ctx);
        }
        catch (SubscriberProfileProvisionException subEx)
        {
            new MinorLogMsg(this, " Unable remove subscription " + request.getOldSubscriptionId() + " from URCS", subEx)
                    .log(ctx);
        }
        try
        {
            Subscriber newSub = request.getNewSubscription(ctx);
            if (newSub.isPrepaid())
            {
                newSub.setState(SubscriberStateEnum.AVAILABLE);
                bmClient_.addSubscriptionProfile(ctx, request.getNewSubscription(ctx));
                newSub.setState(SubscriberStateEnum.ACTIVE);
                activatePrepaidSubscription(ctx, newSub, request);
                try
                {
                    final Parameters profile2 = bmClient_.querySubscriptionProfile(ctx, newSub);
                    if (profile2 != null)
                    {
                        newSub.setExpiryDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
                                profile2.getExpiryDate(), newSub.getTimeZone(ctx)));
                        
                    }
                }
                catch (HomeException exception)
                {
                    new MinorLogMsg(this, "Failed to query BM for subscription " + newSub.getId(), exception).log(ctx);
                }
                catch (SubscriberProfileProvisionException exception)
                {
                    new MinorLogMsg(this, "Failed to query BM for subscription " + newSub.getId(), exception).log(ctx);
                }
            }
            else
            {
                bmClient_.addSubscriptionProfile(ctx, request.getNewSubscription(ctx));
            }
            if (CallingGroupSupport.hasProvisionedCallingGroupService(ctx, request.getOldSubscription(ctx)))
            {
                CallingGroupSupport.setFriendsAndFamilyEnabled(ctx, request.getNewSubscription(ctx).getId(), true);
            }
        }
        catch (HomeException homeEx)
        {
            new MinorLogMsg(this, " Unable remove subscription " + request.getOldSubscriptionId() + " from URCS",
                    homeEx).log(ctx);
        }
        catch (SubscriberProfileProvisionException subEx)
        {
            new MinorLogMsg(this, " Unable remove subscription " + request.getOldSubscriptionId() + " from URCS", subEx)
                    .log(ctx);
        }
        super.createNewEntity(ctx, request);
    }

    private void activatePrepaidSubscription(Context ctx, Subscriber newSub, CSBTR request)
    {
        newSub.setState(SubscriberStateEnum.ACTIVE);
        
        //Trying to activate the subscriber
        int result = ErrorCode.NO_ERROR;
        final LongHolder outputBalance = new LongHolder();
        final AppOcgClient client = (AppOcgClient) ctx.get(AppOcgClient.class);
        try
        {
            result = client.activateSubscriber(newSub.getMSISDN(), newSub.getSubscriberType(), newSub.getCurrency(ctx),
                    false, "",
                    outputBalance, newSub.getSubscriptionType(ctx).getId());
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error trying to get subscription type.", e).log(ctx);
        }
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, CSBTR request) throws MoveException
    {
        super.removeOldEntity(ctx, request); 
    }

    private SubscriberProfileProvisionClient bmClient_ = null;
}
