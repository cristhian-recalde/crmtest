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
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.ReadOnlyMoveProcessor;
import com.trilogy.app.crm.move.processor.generic.strategy.SupplementaryDataCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.BaseAccountChangeSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.BaseSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.ERSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.NotesSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.OMSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.ReverseActivationMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.SubscriptionCloningMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.strategy.AcquiredMsisdnPINCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.AmountOwingTransferCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.AuxServiceCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BMSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BalanceResetCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BaseSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BundleCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ChargingCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.CreditLimitUpdateBMCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.DepositCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ExpiryExtensionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ReverseActivationCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ServiceCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriberSubscriptionHistoryCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionContractCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionHomeCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionMsisdnCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionResourceDeviceCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SuspendedEntityCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.VPNCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.WalletSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.request.ReverseActivationMoveRequest;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Creates an instance of a MoveProcessor for Subscription Reverse Activation Move Request.
 * This MoveProcessor will DeActivate the original Subscription/Account from ACTIVE state
 * and then it will create another Subscription in AVAILABLE state under the new Account.
 *
 * @author Mangaraj Sahoo
 * @since 9.2
 */
class ReverseActivationMoveProcessorFactory
{
    private static String MODULE = ReverseActivationMoveProcessorFactory.class.getName();
    
    static <RAMR extends ReverseActivationMoveRequest> MoveProcessor<RAMR> getNewInstance(Context ctx, RAMR request)
    {
        boolean validationError = false;
        
        Subscriber subscription = request.getOriginalSubscription(ctx);
        if (subscription == null)
        {
            LogSupport.info(ctx, MODULE, "Subscription " + request.getOldSubscriptionId() + " does not exist.");
            validationError = true;
        }
        
        if (subscription != null && !SubscriberStateEnum.ACTIVE.equals(subscription.getState()))
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Subscription ");
            msg.append(request.getOldSubscriptionId());
            msg.append(" is not in ");
            msg.append(SubscriberStateEnum.ACTIVE.getDescription());
            msg.append(" state. [State: ");
            msg.append(subscription.getState());
            msg.append("].");
            LogSupport.info(ctx, MODULE, msg.toString());
            validationError = true;
        }
        
        Account oldAccount = request.getOldAccount(ctx);
        if (subscription != null && oldAccount == null)
        {
            LogSupport.info(ctx, MODULE, "Account for subscription " + subscription.getId() + " does not exist.");
            validationError = true;
        }
        
        if (oldAccount != null && SafetyUtil.safeEquals(oldAccount.getBAN(), request.getNewBAN()))
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Subscription ");
            msg.append(request.getOldSubscriptionId());
            msg.append(" already belongs to account (BAN: ");
            msg.append(request.getNewBAN());
            msg.append(").");
            LogSupport.info(ctx, MODULE, msg.toString());
            validationError = true;
        }

        // Keep dependent entity processor to end of pipeline
        MoveProcessor<RAMR> processor = new DependencyMoveProcessor<RAMR>(request);;
        
        // Add processor to clone the old subscription and execute the copy logic
        CopyMoveStrategy<RAMR> copyStrategy = getCopyStrategy(ctx, request);
        if (copyStrategy == null)
        {
            validationError = true;
        }
        else
        {
            processor = new SubscriptionCloningMoveProcessor<RAMR>(processor, copyStrategy);            
        }
        
        // Add processor to create Subscription move Notes
        processor = new NotesSubscriptionMoveProcessor<RAMR>(processor);
        
        // Add processor to create Subcription modification ERs
        processor = new ERSubscriptionMoveProcessor<RAMR>(processor);
        
        // Add processor to peg Subscription move OMs
        processor = new OMSubscriptionMoveProcessor<RAMR>(processor);

        // Add processor to perform common business logic validation for relocation
        processor = new BaseAccountChangeSubscriptionMoveProcessor<RAMR>(processor);

        processor = new ReverseActivationMoveProcessor<RAMR>(processor);
        
        // Add processor to perform common business logic validation
        processor = new BaseSubscriptionMoveProcessor<RAMR>(processor);
        
        if (validationError)
        {
            LogSupport.info(ctx, MODULE, "Error occurred while creating a move processor for request " + request
                    + ". Returning a read-only move processor so that validation can be run.");
            processor = new ReadOnlyMoveProcessor<RAMR>(processor,
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
    private static <RAMR extends ReverseActivationMoveRequest> CRMSpid getCRMSpid(Context ctx, RAMR request)
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
                LogSupport.info(ctx, MODULE, "Error retrieving service provider " + subscription.getSpid() 
                        + " for subscription " + request.getOldSubscriptionId(), e);
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
    private static <RAMR extends ReverseActivationMoveRequest> CopyMoveStrategy<RAMR> getCopyStrategy(Context ctx, RAMR request)
    {
        CopyMoveStrategy<RAMR> copyStrategy = null;
        Account oldAccount = request.getOldAccount(ctx);
        Subscriber originalSubscription = request.getOriginalSubscription(ctx);
        SubscriptionType subscriptionType = request.getSubscriptionType(ctx);
        if (originalSubscription == null || subscriptionType == null || oldAccount == null)
        {
            return null;
        }
        
        if (!SafetyUtil.safeEquals(oldAccount.getBAN(), request.getNewBAN()))
        {
            // Add copy logic to perform create/store Home operations on new/old copies of subscriptions
            copyStrategy = new SubscriptionHomeCopyMoveStrategy<RAMR>();
            
            copyStrategy = new SubscriptionMsisdnCopyMoveStrategy<RAMR>(copyStrategy);
            
            copyStrategy = new SubscriptionResourceDeviceCopyMoveStrategy<RAMR>(copyStrategy);

            copyStrategy = new BMSubscriptionCopyMoveStrategy<RAMR>(copyStrategy);
            
            // Copy Services to the new subscription
            copyStrategy = new ServiceCopyMoveStrategy<RAMR>(copyStrategy);
            
            if (subscriptionType.isVoice())
            {
                //Copy VPN to the new subscription
                copyStrategy = new VPNCopyMoveStrategy<RAMR>(copyStrategy);
            }
            
            // Copy non-VPN Auxiliary Services to the new subscription.
            // This must be executed after the VPN services are moved.
            copyStrategy = new AuxServiceCopyMoveStrategy<RAMR>(copyStrategy);
            
            // Copy suspended entities to the new subscription
            copyStrategy = new SuspendedEntityCopyMoveStrategy<RAMR>(copyStrategy);

            copyStrategy = new BalanceResetCopyMoveStrategy<RAMR>(copyStrategy);
            
            CRMSpid spid = getCRMSpid(ctx, request);
            // Transfer balances/charges
            if (originalSubscription.isPostpaid() && spid != null && spid.isCarryOverBalanceOnMove())
            {
                copyStrategy = new AmountOwingTransferCopyMoveStrategy<RAMR>(copyStrategy);
                copyStrategy = new SubscriberSubscriptionHistoryCopyMoveStrategy<RAMR>(copyStrategy);
            }
            else
            {
                copyStrategy = new ChargingCopyMoveStrategy<RAMR>(copyStrategy);   
            }

            copyStrategy = new SupplementaryDataCopyMoveStrategy<RAMR>(copyStrategy, request);
            
            copyStrategy = new AcquiredMsisdnPINCopyMoveStrategy<RAMR>(copyStrategy);
            
            if (subscriptionType.isService())
            {
				//TT#12052243052 Fixed. Not using the BundleMove strategy because buckets should not be removed as well as added. 
				//As in CPS, subscription ID remains same, Buckets remains intact.
               // copyStrategy = new BundleCopyMoveStrategy<RAMR>(copyStrategy);
            	
                // Create copy-style move logic pipeline for service-based subscriptions
                if (originalSubscription.isPostpaid())
                {
                    copyStrategy = new DepositCopyMoveStrategy<RAMR>(copyStrategy);  
                    copyStrategy = new CreditLimitUpdateBMCopyMoveStrategy<RAMR>(copyStrategy);
                    copyStrategy = new SubscriptionContractCopyMoveStrategy<RAMR>(copyStrategy);
                }
                else
                {
                    copyStrategy = new ExpiryExtensionCopyMoveStrategy<RAMR>(copyStrategy);
                }
            }
            else if (subscriptionType.isWallet())
            {
                // Create copy-style move logic pipeline for wallet subscriptions
                copyStrategy = new WalletSubscriptionCopyMoveStrategy<RAMR>(copyStrategy);
            }
            
            copyStrategy = new ReverseActivationCopyMoveStrategy<RAMR>(copyStrategy);
            
            copyStrategy = new BaseSubscriptionCopyMoveStrategy<RAMR>(copyStrategy);
        }
        
        return copyStrategy;
    }
}
