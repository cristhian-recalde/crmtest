/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.move.processor.factory;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.ReadOnlyMoveProcessor;
import com.trilogy.app.crm.move.processor.generic.strategy.SupplementaryDataCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.BaseConvertSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.BaseSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.ConvertSubscriptionInitializingMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.ERSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.NotesSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.OMSubscriptionMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.SubscriptionCloningMoveProcessor;
import com.trilogy.app.crm.move.processor.subscription.strategy.AcquiredMsisdnPINCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.AuxServiceCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BalanceResetCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BaseConvertSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BaseSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.BundleConversionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ChargingCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ConversionServiceCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ConvertBMSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.ConvertBalanceTransferMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.CreditLimitUpdateBMCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.DepositCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.InitialAmountSubscriptionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.OverdraftBalanceResetCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.PPSMSupporterCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionContractCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionHomeCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionImsiCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionMsisdnCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.SubscriptionResourceDeviceCopyMoveStrategy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * Creates an appropriate instance of a ConvertSubscriptionMoveRequest processor.
 * 
 * @author Kumaran sivasubramaniam
 * @since 8.1
 */
class ConvertSubscriptionProcessorFactory
{

    static <CSBTR extends ConvertSubscriptionBillingTypeRequest> MoveProcessor<CSBTR> getNewInstance(Context ctx,
            CSBTR request)
    {
        boolean validationError = false;
        Subscriber subscription = request.getOriginalSubscription(ctx);
        Account oldAccount = null;
        if (subscription == null)
        {
            new InfoLogMsg(ConvertSubscriptionProcessorFactory.class, "Subscription " + request.getOldSubscriptionId()
                    + " does not exist.", null).log(ctx);
            validationError = true;
        }
        else
        {
            oldAccount = request.getOldAccount(ctx);
            if (oldAccount == null)
            {
                new InfoLogMsg(ConvertSubscriptionProcessorFactory.class, "Account " + subscription.getBAN()
                        + " does not exist.", null).log(ctx);
                validationError = true;
            }
        }
        MoveProcessor<CSBTR> processor = null;
        // Add processor to clone the old subscription and execute the copy logic
        CopyMoveStrategy<CSBTR> copyStrategy = null;
        if (oldAccount.isHybrid() && ctx.getBoolean(MoveConstants.NO_BILLCYCLE_CHANGE, false))
        {
            // empty dependency
            processor = new DependencyMoveProcessor<CSBTR>(request);
            processor = new ConvertSubscriptionInitializingMoveProcessor<CSBTR>(processor);
            
            // Add processor to clone the old subscription and execute the copy logic
            copyStrategy = getCopyStrategyForNoBillCycleChange(ctx, request);

        }
        else
        {
            Account newAccount = request.getNewAccount(ctx);
            if (newAccount == null)
            {
                new InfoLogMsg(ConvertSubscriptionProcessorFactory.class, "Account " + request.getNewBAN()
                        + " does not exist.", null).log(ctx);
                validationError = true;
            }
            String newBAN = request.getNewBAN();
            if (newBAN.startsWith(MoveConstants.DEFAULT_MOVE_PREFIX))
            {
                new InfoLogMsg(SubscriptionMoveProcessorFactory.class,
                        "New BAN not set properly.  This move request can be validated but not executed.", null)
                        .log(ctx);
                validationError = true;
            }
            // empty dependency
            processor = new DependencyMoveProcessor<CSBTR>(request);
            processor = new ConvertSubscriptionInitializingMoveProcessor<CSBTR>(processor);
            // Add processor to clone the old subscription and execute the copy logic
            copyStrategy = getCopyStrategy(ctx, request);

        }
        
        if (copyStrategy == null)
        {
            validationError = true;
        }
        else
        {
            processor = new SubscriptionCloningMoveProcessor<CSBTR>(processor, copyStrategy);
        }
        
        // Add processor to create Subscription move Notes
        processor = new NotesSubscriptionMoveProcessor<CSBTR>(processor);
        // Add processor to create Subscription modification ERs
        processor = new ERSubscriptionMoveProcessor<CSBTR>(processor);
        // Add processor to peg Subscription move OMs
        processor = new OMSubscriptionMoveProcessor<CSBTR>(processor, Common.OM_SUBSCRIBER_CONVERSION_ATTEMPT,
                Common.OM_SUBSCRIBER_CONVERSION_SUCCESS, Common.OM_SUBSCRIBER_CONVERSION_FAIL);
        // Add processor to perform common business logic validation for conversion
        processor = new BaseConvertSubscriptionMoveProcessor<CSBTR>(processor);
        // Add processor to perform common business logic validation
        processor = new BaseSubscriptionMoveProcessor<CSBTR>(processor);
        if (validationError)
        {
            new InfoLogMsg(ConvertSubscriptionProcessorFactory.class,
                    "Error occurred while creating a conversion processor for request " + request
                            + ".  Returning a read-only conversion processor so that validation can be run.", null)
                    .log(ctx);
            processor = new ReadOnlyMoveProcessor<CSBTR>(processor,
                    "Error occurred while creating a conversion processor for request " + request);
        }
        return processor;
    }


    /**
     * Get the copy strategy for the given request and subscription type.
     * 
     * @param ctx
     *            Move context
     * @param request
     *            Move request
     * @return Proper copy strategy for the given request and subscription type.
     */
    private static <CSBTR extends ConvertSubscriptionBillingTypeRequest> CopyMoveStrategy<CSBTR> getCopyStrategy(
            Context ctx, CSBTR request)
    {
        CopyMoveStrategy<CSBTR> copyStrategy = null;
        // Add copy logic to perform create/store Home operations on new/old copies of
        // subscriptions
        copyStrategy = new SubscriptionHomeCopyMoveStrategy<CSBTR>();
        copyStrategy = new SubscriptionMsisdnCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new SubscriptionImsiCopyMoveStrategy<CSBTR>(copyStrategy); 
        copyStrategy = new SubscriptionResourceDeviceCopyMoveStrategy<CSBTR>(copyStrategy);
        // Need to my own bundle management
        copyStrategy = new ConvertBMSubscriptionCopyMoveStrategy<CSBTR>(copyStrategy);
        // Copy Services, Bundles, and VPN to the new subscription
        copyStrategy = new ConversionServiceCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new AuxServiceCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new BalanceResetCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new DepositCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new SubscriptionContractCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new PPSMSupporterCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new CreditLimitUpdateBMCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new ConvertBalanceTransferMoveStrategy<CSBTR>(copyStrategy,
                AdjustmentTypeEnum.ConversionBalanceTransfer, AdjustmentTypeEnum.ConversionBalanceTransferDebit);
        copyStrategy = new InitialAmountSubscriptionCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new OverdraftBalanceResetCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new AcquiredMsisdnPINCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new ChargingCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new SupplementaryDataCopyMoveStrategy<CSBTR>(copyStrategy, request);
        copyStrategy = new BundleConversionCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new BaseConvertSubscriptionCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new BaseSubscriptionCopyMoveStrategy<CSBTR>(copyStrategy);
        return copyStrategy;
    }


    /**
     * Get the copy strategy for the given request and subscription type.
     * 
     * @param ctx
     *            Move context
     * @param request
     *            Move request
     * @return Proper copy strategy for the given request and subscription type.
     */
    private static <CSBTR extends ConvertSubscriptionBillingTypeRequest> CopyMoveStrategy<CSBTR> getCopyStrategyForNoBillCycleChange(
            Context ctx, CSBTR request)
    {
        CopyMoveStrategy<CSBTR> copyStrategy = null;
        // Add copy logic to perform create/store Home operations on new/old copies of
        // subscriptions
        copyStrategy = new SubscriptionHomeCopyMoveStrategy<CSBTR>();
        copyStrategy = new SubscriptionMsisdnCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new SubscriptionImsiCopyMoveStrategy<CSBTR>(copyStrategy); 
        // Need to my own bundle management
        copyStrategy = new ConvertBMSubscriptionCopyMoveStrategy<CSBTR>(copyStrategy);
        // Copy Services, Bundles, and VPN to the new subscription
        copyStrategy = new ConversionServiceCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new AuxServiceCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new BalanceResetCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new DepositCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new SubscriptionContractCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new PPSMSupporterCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new ConvertBalanceTransferMoveStrategy<CSBTR>(copyStrategy,
                AdjustmentTypeEnum.ConversionBalanceTransfer, AdjustmentTypeEnum.ConversionBalanceTransferDebit);
        copyStrategy = new InitialAmountSubscriptionCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new OverdraftBalanceResetCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new AcquiredMsisdnPINCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new ChargingCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new SupplementaryDataCopyMoveStrategy<CSBTR>(copyStrategy, request);
        copyStrategy = new BundleConversionCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new BaseConvertSubscriptionCopyMoveStrategy<CSBTR>(copyStrategy);
        copyStrategy = new BaseSubscriptionCopyMoveStrategy<CSBTR>(copyStrategy);
        return copyStrategy;
    }
}
