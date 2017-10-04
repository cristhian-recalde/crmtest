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
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.CreditCategorySupport;


/**
 * This copy strategy is responsible for updating the parent BAN (a.k.a. 'Subscriber ID')
 * of the subscription profile in BMGT.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class BMSubscriptionCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public BMSubscriptionCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
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
        
        bmClient_  = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
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
                        "Failed to retrieve subscription (ID=" + request.getOldSubscriptionId() + ") from Balance Management application."));
            }  
        }

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        Account newAccount = SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        if (newAccount != null)
        {
            if (newAccount.isBANSet()
                    && bmClient_ != null)
            {
                // Validate that new account profile exists in balance management application (if the new BAN is set)
                // This is most likely only applicable when doing a standalone subscription move, since the new
                // account won't exist yet during an account move (i.e. we are in the recursive validation).
                try
                {
                    Parameters profile = bmClient_.querySubscriberAccountProfile(ctx, newAccount);
                    if (profile == null)
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                SubscriptionMoveRequestXInfo.NEW_BAN, 
                                "Subscriber profile for account (BAN=" + newAccount.getBAN() +") does not exist in Balance Management application."));
                    }
                }
                catch (final Exception e)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            SubscriptionMoveRequestXInfo.NEW_BAN, 
                            "Failed to retrieve subscriber profile for account (BAN=" + newAccount.getBAN() +") from Balance Management application."));
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(
                            this,
                            "Skipping validation of new account's presence in Balance Management application "
                                    + "because it hasn't been created yet (would normally be done during the move execution).",
                            null).log(ctx);
                }
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
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        super.createNewEntity(ctx, request); 

        Subscriber newSubscription = request.getNewSubscription(ctx);

        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Updating BAN to " + newSubscription.getBAN()
                        + " in Balance Management application for subscription " + newSubscription.getId() + "(MSISDN="
                        + newSubscription.getMSISDN() + ")...", null).log(ctx);
            }
            bmClient_.updateBAN(ctx, newSubscription, newSubscription.getBAN());
            if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
            {
                new InfoLogMsg(this, "Updated BAN to " + newSubscription.getBAN()
                        + " successfully in Balance Management application for subscription " + newSubscription.getId()
                        + "(MSISDN=" + newSubscription.getMSISDN() + ")", null).log(ctx);
            }
            checkAndUpdateMonthlySpendLimit(ctx, request);
            checkAndUpdateGroupScreeningTemplateId(ctx, request);
        }
        catch (final Exception e)
        {
            String bmUpdateErrorMsg = "Failed to update BAN of BM subscription profile for "
                + newSubscription.getId() + " (MSISDN=" + newSubscription.getMSISDN() + ") to "
                + newSubscription.getBAN();
            new MinorLogMsg(this, bmUpdateErrorMsg, e).log(ctx);
            throw new MoveException(request, bmUpdateErrorMsg, e);
        }
    }
    
    
    /**
     * @param ctx
     * @param request
     * @throws MoveException
     */
    private void checkAndUpdateGroupScreeningTemplateId(Context ctx, SMR request) throws MoveException
    {
        Subscriber newSubscription = request.getNewSubscription(ctx);

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Updating GroupScreeningTemplateID to : " + newSubscription.getGroupScreeningTemplateId()
                    + " in Balance Management application for subscription " + newSubscription.getId() + "(MSISDN="
                    + newSubscription.getMSISDN() + ")", null).log(ctx);
        }

        try
        {
            bmClient_.updateGroupScreeningTemplateId(ctx, newSubscription,
                    newSubscription.getGroupScreeningTemplateId());
        }
        catch (Exception e)
        {
            String errorMsg = "Unable to update GroupScreeningTemplateId for subscriber : " + newSubscription.getId();
            new MinorLogMsg(this, errorMsg, e).log(ctx);
            throw new MoveException(request, errorMsg, e);
        }
    }


    private void checkAndUpdateMonthlySpendLimit(Context ctx, SMR request) throws MoveException
    {
        Subscriber newSubscription = request.getNewSubscription(ctx);
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        boolean isNewSubPooled = newSubscription.isPooledMemberSubscriber(ctx);
        boolean isOldSubPooled = oldSubscription.isPooledMemberSubscriber(ctx);
        
        if (isNewSubPooled != isOldSubPooled)
        {

            try
            {
                if (isNewSubPooled)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Updating monthly spend limit to -1 "
                                + " in Balance Management application for subscription " + newSubscription.getId() + "(MSISDN="
                                + newSubscription.getMSISDN() + ")...", null).log(ctx);
                    }
                    bmClient_.updateSubscriptionMonthlySpendLimit(ctx, newSubscription, -1);
                }
                else
                {
                    Account newAccount = request.getNewAccount(ctx);
                    try
                    {
                        CreditCategory cc = CreditCategorySupport.findCreditCategory(ctx, newAccount
                                .getCreditCategory());
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "Updating monthly spend limit to " + cc.getMonthlySpendLimit() 
                                    + " in Balance Management application for subscription " + newSubscription.getId() + "(MSISDN="
                                    + newSubscription.getMSISDN() + ")...", null).log(ctx);
                        }
                        bmClient_.updateSubscriptionMonthlySpendLimit(ctx, newSubscription, cc.getMonthlySpendLimit());
                    }
                    catch (HomeException homeEx)
                    {
                        String errorMsg = "Unable to find credit category for =>" + newAccount.getCreditCategory();
                        new MinorLogMsg(this, errorMsg, homeEx).log(ctx);
                        throw new MoveException(request, errorMsg, homeEx);
                    }
                }
            }
            catch (Exception e)
            {
                String errorMsg = " Unable to set the monthly spend limit for new moved account "
                        + newSubscription.getId();
                new MinorLogMsg(this, errorMsg, e).log(ctx);
                throw new MoveException(request, errorMsg, e);
            }
        }
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }

    private SubscriberProfileProvisionClient bmClient_ = null;
}
