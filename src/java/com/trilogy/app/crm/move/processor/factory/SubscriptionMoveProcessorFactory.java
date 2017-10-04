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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.ReadOnlyMoveProcessor;
import com.trilogy.app.crm.move.processor.generic.strategy.SupplementaryDataCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.NullCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.BaseAccountChangeSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.BaseSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.ERSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.NotesSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.OMSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.SubscriptionCloningMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.strategy.AcquiredMsisdnPINCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.AmountOwingTransferCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.AuxServiceCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BMSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BalanceResetCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BalanceTransferCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BaseSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BundleCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ChargingCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.CreditLimitUpdateBMCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.DepositCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ExpiryExtensionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.GroupAccountCugCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.GroupMsisdnCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ServiceCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriberSubscriptionHistoryCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionContractCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionGroupScreeningTemplateCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionHomeCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionMsisdnCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionResourceDeviceCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SuspendedEntityCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.VPNCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.WalletSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * Creates an appropriate instance of a SubscriptionMoveRequest processor.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
class SubscriptionMoveProcessorFactory
{
    static <SMR extends SubscriptionMoveRequest> MoveProcessor<SMR> getNewInstance(Context ctx, SMR request)
    {
        boolean validationError = false;
        
        Subscriber subscription = request.getOriginalSubscription(ctx);
        if (subscription == null)
        {
            new InfoLogMsg(SubscriptionMoveProcessorFactory.class, 
                    "Subscription " + request.getOldSubscriptionId() + " does not exist.", null).log(ctx);
            validationError = true;
        }

        Account oldAccount = request.getOldAccount(ctx);
        if (subscription != null && oldAccount == null)
        {
            new InfoLogMsg(SubscriptionMoveProcessorFactory.class, 
                    "Account " + subscription.getBAN() + " does not exist.", null).log(ctx);
            validationError = true;
        }
        
        Account newAccount = request.getNewAccount(ctx);
        if (newAccount == null)
        {
            new InfoLogMsg(SubscriptionMoveProcessorFactory.class, 
                    "Account " + request.getNewBAN() + " does not exist.", null).log(ctx);
            validationError = true;
        }
        
        String newBAN = request.getNewBAN();
        if (newBAN.startsWith(MoveConstants.DEFAULT_MOVE_PREFIX))
        {
            new InfoLogMsg(SubscriptionMoveProcessorFactory.class, 
                    "New BAN not set properly.  This move request can be validated but not executed.", null).log(ctx);
            validationError = true;
        }

        MoveProcessor<SMR> processor = null;

        // Add dependent entity processor to end of pipeline
        // See {@link com.redknee.app.crm.move.dependency.SubscriptionMoveDependencyManager}) for dependencies.
        processor = new DependencyMoveProcessor<SMR>(request);
        
        // Add processor to clone the old subscription and execute the copy logic
        CopyMoveStrategy<SMR> copyStrategy = getCopyStrategy(ctx, request);
        if (copyStrategy == null)
        {
            validationError = true;
        }
        else
        {
            processor = new SubscriptionCloningMoveProcessor<SMR>(processor, copyStrategy);            
        }
        
        // Add processor to create Subscription move Notes
        processor = new NotesSubscriptionMoveProcessor<SMR>(processor);
        
        // Add processor to create Subcription modification ERs
        processor = new ERSubscriptionMoveProcessor<SMR>(processor);
        
        // Add processor to peg Subscription move OMs
        processor = new OMSubscriptionMoveProcessor<SMR>(processor);

        if (oldAccount == null
                || !SafetyUtil.safeEquals(oldAccount.getBAN(), newBAN))
        {
            // Add processor to perform common business logic validation for relocation
            processor = new BaseAccountChangeSubscriptionMoveProcessor<SMR>(processor);
        }

        // Add processor to perform common business logic validation
        processor = new BaseSubscriptionMoveProcessor<SMR>(processor);
        
        if (validationError)
        {
            new InfoLogMsg(SubscriptionMoveProcessorFactory.class, 
                    "Error occurred while creating a move processor for request " + request
                    + ".  Returning a read-only move processor so that validation can be run.", null).log(ctx);
            processor = new ReadOnlyMoveProcessor<SMR>(
                    processor, 
                    "Error occurred while creating a move processor for request " + request);
        }
        
        return processor;
    }

    /**
     * Get the original subscription's subscription type.
     * 
     * @param ctx Move context
     * @param request Move request
     * @return Returns the original subscription's subscription type.
     */
    private static <SMR extends SubscriptionMoveRequest> CRMSpid getCRMSpid(Context ctx, SMR request)
    {
        CRMSpid result = null;
        
        Subscriber subscription = request.getOriginalSubscription(ctx);
        
        if (subscription != null)
        {
            try
            {
                result = SpidSupport.getCRMSpid(ctx, subscription.getSpid());
            }
            catch (HomeException e)
            {
                new InfoLogMsg(SubscriptionMoveProcessorFactory.class, 
                        "Error retrieving service provider " + subscription.getSpid() + " for subscription " + request.getOldSubscriptionId(), e);
            }
        }
        
        return result;
    }

    /**
     * Get the copy strategy for the given request and subscription type.
     * 
     * @param ctx Move context
     * @param request Move request
     * @return Proper copy strategy for the given request and subscription type.
     */
    private static <SMR extends SubscriptionMoveRequest> CopyMoveStrategy<SMR> getCopyStrategy(Context ctx, SMR request)
    {
        return getCopyStrategy(ctx, getCRMSpid(ctx, request), request, request.getSubscriptionType(ctx));
    }

    /**
     * Get the copy strategy for the given request and subscription type.
     * 
     * @param ctx Move context
     * @param spid Service Provider configuration
     * @param request Move request
     * @param subscriptionType Type of subscription being moved.
     * @return Proper copy strategy for the given request and subscription type.
     */
    private static <SMR extends SubscriptionMoveRequest> CopyMoveStrategy<SMR> getCopyStrategy(Context ctx, CRMSpid spid, SMR request, SubscriptionType subscriptionType)
    {
        CopyMoveStrategy<SMR> copyStrategy = null;

        if (subscriptionType != null)
        {
            Subscriber originalSubscription = request.getOriginalSubscription(ctx);
            Subscriber newSubscription = request.getNewSubscription(ctx);
            Account originalAccount = request.getOldAccount(ctx);
            String newSubscriptionId = request.getNewSubscriptionId();
            
            final Account oldAccount = request.getOldAccount(ctx);
            final String newBAN = request.getNewBAN();
            if (oldAccount == null
                    || !SafetyUtil.safeEquals(oldAccount.getBAN(), newBAN))
            {
                // Add copy logic to perform create/store Home operations on new/old copies of subscriptions
                copyStrategy = new SubscriptionHomeCopyMoveStrategy<SMR>();
                
                copyStrategy = new SubscriptionGroupScreeningTemplateCopyMoveStrategy<SMR>(copyStrategy);
                
                copyStrategy = new SubscriptionMsisdnCopyMoveStrategy<SMR>(copyStrategy);
                
                copyStrategy = new SubscriptionResourceDeviceCopyMoveStrategy<SMR>(copyStrategy);

                copyStrategy = new BMSubscriptionCopyMoveStrategy<SMR>(copyStrategy);
                
                // Copy Services and VPN to the new subscription
                copyStrategy = new ServiceCopyMoveStrategy<SMR>(copyStrategy);
                
                // Move Subscriber to new Account Cug
                copyStrategy = new GroupAccountCugCopyMoveStrategy<SMR>(copyStrategy);
                
                if (subscriptionType.isVoice())
                {
                    copyStrategy = new VPNCopyMoveStrategy<SMR>(copyStrategy);
                }
                
                // Move non-VPN Auxiliary Services to the new subscription.
                // This must be executed after the VPN services are moved.
                copyStrategy = new AuxServiceCopyMoveStrategy<SMR>(copyStrategy);
                
                // Copy suspended entities to the new subscription
                copyStrategy = new SuspendedEntityCopyMoveStrategy<SMR>(copyStrategy);

                boolean isPooled = false;
                boolean wasPooled = false;
                boolean samePool = false;
                boolean isGroupConversion = ctx.getBoolean(ConvertAccountGroupTypeRequest.CONVERT_ACCOUNT_GROUP_TYPE, false);
                if (originalSubscription != null)
                {
                    String oldGroupMSISDN = null;
                    String newGroupMSISDN = null;
                    
                    Account newAccount = request.getNewAccount(ctx);
                    if (newAccount != null)
                    {
                        newGroupMSISDN = newAccount.getGroupMSISDN(ctx, originalSubscription.getSubscriptionType());
                        isPooled = (newGroupMSISDN != null && newGroupMSISDN.trim().length() > 0);
                    }
                    
                    if (oldAccount != null)
                    {
                        oldGroupMSISDN = oldAccount.getGroupMSISDN(ctx, originalSubscription.getSubscriptionType());
                        wasPooled = (oldGroupMSISDN != null && oldGroupMSISDN.trim().length() > 0);
                    }
                    
                    if (isPooled && wasPooled)
                    {
                        samePool = SafetyUtil.safeEquals(oldGroupMSISDN, newGroupMSISDN);
                    }
                    
                }

                if (!samePool && (isPooled || wasPooled))
                {
                    // Update the group MSISDN of the new subscription when moving to/from pooled accounts.
                    copyStrategy = new GroupMsisdnCopyMoveStrategy<SMR>(copyStrategy);
                }
                
                copyStrategy = new BalanceResetCopyMoveStrategy<SMR>(copyStrategy);

                if(originalSubscription != null
                        && originalSubscription.isPrepaid())
                {
                    if (isPooled && !wasPooled)
                    {
                        // Transfer prepaid balance to pool
                        copyStrategy = new BalanceTransferCopyMoveStrategy<SMR>(copyStrategy);
                    }   
                    /**
                     * Added during TT fix TT#14022016015. Prepaid Account Move from 
                     * 1) Group Pooled to Group Pooled
                     * 2) Moving out from Group Pooled as an individual account
                     * Gives exception for charging.
                     * Reason: While moving a prepaid account from GP, It deactivates the old subscription and creates new subscription.
                     * And Balance transfer was being skipped due to restriction done in above if statement.
                     * Therefore, Adding else if statements to support the mentioned 2 Use cases . 
                     * I have kept it as separate else if statements only because in case it breaks other move scenario during
                     * regression for 9.7.2 , and we have no time to fix, then we should revert the change 
                     * by removing below 2 else if statements and commit the code.
                     **/
                    else if(isPooled && wasPooled) //Prepaid account is being moved from GP to another GP account
                    {
                        copyStrategy = new BalanceTransferCopyMoveStrategy<SMR>(copyStrategy);
                    }
                    else if(wasPooled && !isPooled) //Prepaid account is being moved out of a GP account as an individual account
                    {
                        Account newAccount = request.getNewAccount(ctx);
                        if(newAccount.isIndividual(ctx))
                        {
                            copyStrategy = new BalanceTransferCopyMoveStrategy<SMR>(copyStrategy);
                        }
                    }
                }

                // Transfer balances/charges
                if (originalSubscription != null && originalSubscription.isPostpaid()
                        && ((spid != null && spid.isCarryOverBalanceOnMove()) || isGroupConversion))
                {
                    copyStrategy = new AmountOwingTransferCopyMoveStrategy<SMR>(copyStrategy);
                    copyStrategy = new SubscriberSubscriptionHistoryCopyMoveStrategy<SMR>(copyStrategy);
                }
                else
                {
                    copyStrategy = new ChargingCopyMoveStrategy<SMR>(copyStrategy);   
                }

                copyStrategy = new SupplementaryDataCopyMoveStrategy<SMR>(copyStrategy, request);
                copyStrategy = new AcquiredMsisdnPINCopyMoveStrategy<SMR>(copyStrategy);
                
                if (subscriptionType.isService())
                {
                    copyStrategy = new BundleCopyMoveStrategy<SMR>(copyStrategy);

                    // Create copy-style move logic pipeline for service-based subscriptions
                    if (originalSubscription != null)
                    {
                        if (originalSubscription.isPostpaid())
                        {
                            copyStrategy = new DepositCopyMoveStrategy<SMR>(copyStrategy, true);  
                            copyStrategy = new CreditLimitUpdateBMCopyMoveStrategy<SMR>(copyStrategy);
                            copyStrategy = new SubscriptionContractCopyMoveStrategy<SMR>(copyStrategy);
                        }
                        else
                        {
                            copyStrategy = new ExpiryExtensionCopyMoveStrategy<SMR>(copyStrategy);
                        }
                    }
                }
                else if (subscriptionType.isWallet())
                {
                    // Create copy-style move logic pipeline for wallet subscriptions
                    copyStrategy = new WalletSubscriptionCopyMoveStrategy<SMR>(copyStrategy);
                }
                
                copyStrategy = new BaseSubscriptionCopyMoveStrategy<SMR>(copyStrategy);
            }
            else
            {
                // The subscription is not actually changing accounts.  Execute copy-logic for responsible account move scenarios.
                copyStrategy = NullCopyMoveStrategy.instance();

                if (subscriptionType.isVoice())
                {
                    copyStrategy = new VPNCopyMoveStrategy<SMR>(copyStrategy);
                }
            }
        }
        
        return copyStrategy;
    }
}
