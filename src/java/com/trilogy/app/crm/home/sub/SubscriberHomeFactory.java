/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.PrepaidAccountDeactivationHome;
import com.trilogy.app.crm.amsisdn.SubscriberAMsisdnValidator;
import com.trilogy.app.crm.bas.recharge.SubscriberClearSuspendedEntities;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SupplementaryDataEntityEnum;
import com.trilogy.app.crm.bean.paymentgatewayintegration.AutoTopUpTriggerAgentHome;
import com.trilogy.app.crm.bundle.BucketProvHome;
import com.trilogy.app.crm.bundle.ProvisionBundleSubscriberHome;
import com.trilogy.app.crm.bundle.SubscriberSetBundleEndDateHome;
import com.trilogy.app.crm.bundle.SubscriberSetBundleNextRecurringChargeDateHome;
import com.trilogy.app.crm.bundle.validator.MemberGroupBundleValidator;
import com.trilogy.app.crm.subscriber.provision.SubscriberWimaxAptiloUpdateHome;
import com.trilogy.app.crm.client.alcatel.AlcatelFunctions;
import com.trilogy.app.crm.client.alcatel.AlcatelSubscriberManagementHome;
import com.trilogy.app.crm.contract.SubscriptionContractUpdateHome;
import com.trilogy.app.crm.extension.ExtensionForeignKeyAdapter;
import com.trilogy.app.crm.extension.ExtensionHandlingHome;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionXInfo;
import com.trilogy.app.crm.home.BypassCreatePipelineHome;
import com.trilogy.app.crm.home.BypassValidationHome;
import com.trilogy.app.crm.home.LicenseHome;
import com.trilogy.app.crm.home.MovePipelineCreator;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.PooledGroupAccountFalseSubscriberProvisioningHome;
import com.trilogy.app.crm.home.SubscriberLastModifiedHiddenHome;
import com.trilogy.app.crm.home.SubscriberMsisdnLanguageSettingHome;
import com.trilogy.app.crm.home.SubscriberStaleValidator;
import com.trilogy.app.crm.home.SubscriberSuspensionReasonUpdateHome;
import com.trilogy.app.crm.home.SupplementaryDataHandlingHome;
import com.trilogy.app.crm.home.SynchronizedKeyHome;
import com.trilogy.app.crm.home.UpdateTopUpScheduleOnATUHome;
import com.trilogy.app.crm.home.account.PendingBillCycleChangeCancellingHome;
import com.trilogy.app.crm.home.validator.AlcatelSSCSubscriptionValidator;
import com.trilogy.app.crm.home.validator.HomeValidator;
import com.trilogy.app.crm.home.validator.LoggingValidatorHome;
import com.trilogy.app.crm.home.validator.NullValidatorHome;
import com.trilogy.app.crm.home.validator.ValidatedHome;
import com.trilogy.app.crm.priceplan.validator.PricePlanGroupValidator;
import com.trilogy.app.crm.provision.gateway.SubscriberServiceProvisionGatewayHome;
import com.trilogy.app.crm.resource.SubscriberResourceDealerCodeUpdateDecorator;
import com.trilogy.app.crm.resource.SubscriberResourceDeviceSaveHome;
import com.trilogy.app.crm.resource.SubscriptionResourceDeviceValidator;
import com.trilogy.app.crm.secondarybalance.validator.SingleSecondaryBalanceBundleValidator;
import com.trilogy.app.crm.subscriber.SubscriberBundleFeeDatesValidator;
import com.trilogy.app.crm.subscriber.agent.AccountRegistrationActivationHome;
import com.trilogy.app.crm.subscriber.agent.AccountSubscriptionCountUpdateHome;
import com.trilogy.app.crm.subscriber.agent.EnforceAccountState;
import com.trilogy.app.crm.subscriber.agent.ResaveSubscriber;
import com.trilogy.app.crm.subscriber.agent.SuspendSubscriber;
import com.trilogy.app.crm.subscriber.charge.MsisdnGroupChargingHome;
import com.trilogy.app.crm.subscriber.charge.SubscriberChargingHome;
import com.trilogy.app.crm.subscriber.home.SubscriberERHome;
import com.trilogy.app.crm.subscriber.provision.BulkServiceUpdateHome;
import com.trilogy.app.crm.subscriber.provision.ProvisionableAuxServiceProvisionHlrHome;
import com.trilogy.app.crm.subscriber.provision.ProvisionableAuxServiceUnprovisionHlrHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionBMHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionEndHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionHlrGatewayHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionRBTHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberServicesBackupHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberServicesProvisioningHome;
import com.trilogy.app.crm.subscriber.provision.TFAAuxServiceProvisionUpdationHome;
import com.trilogy.app.crm.subscriber.provision.blackberry.SubscriberBlackberryMsisdnAndPackageUpdateHome;
import com.trilogy.app.crm.subscriber.provision.ecp.SubscriberEcpClassOfServiceUpdateHome;
import com.trilogy.app.crm.subscriber.provision.ecp.SubscriberEcpGroupMsisdnUpdateHome;
import com.trilogy.app.crm.subscriber.provision.ecp.SubscriberEcpPackageUpdateHome;
import com.trilogy.app.crm.subscriber.provision.ecp.SubscriberEcpProfileUpdateHome;
import com.trilogy.app.crm.subscriber.provision.ecp.SubscriberEcpStateUpdateHome;
import com.trilogy.app.crm.subscriber.provision.ipc.SubscriberIpcProfileUpdateHome;
import com.trilogy.app.crm.subscriber.provision.smsb.SubscriberSmsbUpdateHome;
import com.trilogy.app.crm.subscriber.validator.DealerCodeValidator;
import com.trilogy.app.crm.subscriber.validator.StartEndDateValidator;
import com.trilogy.app.crm.subscriber.validator.SubscriberMultiSimValidator;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.util.SimpleLocks;
import com.trilogy.app.crm.validator.SubscriberBalanceThresholdConfigurationValidator;
import com.trilogy.app.crm.validator.SubscriberServicesPermissionsValidator;
import com.trilogy.app.crm.vpn.SubscriberVpnHome;
import com.trilogy.app.crm.vpn.VpnSubscriberValidator;
import com.trilogy.app.crm.web.function.IsSubscriberPoolLeaderPredicate;
import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.LastModifiedAwareHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.util.snippet.home.PredicateSkipHome;
import com.trilogy.app.crm.home.sub.SubscriptionDiscountEventHome;


/**
 * Provides a PipelineCreator for creating SubscriberHome pipelines on demand.
 *
 * @author gary.anderson@redknee.com
 */
public class SubscriberHomeFactory implements PipelineFactory, MovePipelineCreator
{

    /**
     * Creates a new Home pipeline for the SubscriberHome.
     *
     * @param delegate
     *            The Home to which this pipeline leads.
     */
    public SubscriberHomeFactory(final Home delegate)
    {
        this.delegate_ = delegate;
    }

    /**
     * @(inheritDoc)
     */
    @Override
    public Home createMovePipeline(Context ctx, Context serverCtx)
    {
        Home home = this.delegate_;
        
        home = new SubscriberCategoryHome(ctx, home);
        
        home = new SubscriberPaymentPlanCreditLimitUpdateHome(home);
        
        home = new PooledGroupAccountOwnerMsisdnResettingHome(home);
        
        home = new OverPaymentDistributionDeactivateLastSubscriberHome(ctx,home);
		/*
		 * Added For CR 117 Multi-Line Activation Feature 9.5.1 (AccountSubscriptionCountUpdateHome)
		 */
        home = new AccountSubscriptionCountUpdateHome(ctx, home);
        
        home = new SubscriberIdAssignHome(home);
      
        home = new SubscriberPipeLineContextPrepareHome(home);
        
        home = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, home, Subscriber.class);       
        
        return home;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(final Context context, final Context serverCtx)
    {
        /*
         * don't use context.createSubContext(this.getClass.getName()), otherwise we can
         * not guarantee the context is the one we need.
         */
        final Context subContext = context.createSubContext();
		context.put(SUBSCRIBERXDB_HOME, delegate_); 
		context.put(SUBCRIBER_LOCKER, SimpleLocks.newLock()); 
        /*
         * Note that the order of home creation is opposite to the order of Homes in the
         * pipeline -- last home created here is the first home of the pipeline.
         */
        Home home = this.delegate_;

        home = new PMHome(subContext, SubscriberHome.class.getName() + ".factorybottom", home);
        
        home = new SubscriberHLRHome(home);
        

        // General framework decorators.
        home = new ContextualizingHome(subContext, home);
        
        // SubscriberLastModifiedHiddenHome has to always be executed after LastModifiedAwareHome.
        home = new SubscriberLastModifiedHiddenHome(home);
        home = new LastModifiedAwareHome(home);
        
        /*
         * TODO - 2006-06-13 - Disable the AAA home until it is fixed properly.
         * TT6061335502.
         */

        home = new AAAProfileUpdateHome(home);
        
        home = new PMHome(subContext, SubscriberHome.class.getName() + ".factorybefore.aaaprofileupdatehome", home);
        
        home = new PendingBillCycleChangeCancellingHome(context, home);

        home = new ExtensionHandlingHome<SubscriberExtension>(
                subContext, 
                SubscriberExtension.class, 
                SubscriberExtensionXInfo.SUB_ID, 
                home);
        home = new AdapterHome(home, 
                new ExtensionForeignKeyAdapter(
                        SubscriberExtensionXInfo.SUB_ID));
        
        // This home needs to be in the pipeline anywhere before ExtensionHandlingHome.
        // It makes changes to Multi-SIM extension entries so that it can manage the MSISDN change on update.
        home = new MultiSimMsisdnChangeHome(subContext, home);

        // backs up the services information into different fields in the subscriber
        // object.
        home = new SubscriberServicesBackupHome(home);
        home = new PMHome(subContext, SubscriberHome.class.getName() + ".factorybefore.subscriberservicesbackuphome", home);

        home = new ImsiChangeAppendNumberMgmtHistoryHome(home);

        // TODO: Add RBT Home.
        home = new SubscriberProvisionRBTHome(subContext, home);

        home = new SubscriberBundleAuxiliaryServiceSaveHome(subContext, home);
        home = new SortingHome(home);

        // home = new NoSelectAllHome(home);
        home = new SubscriberCategoryHome(subContext, home);

        /* Provisioning homes end */

        // this executes all sorts of actions based on the transition being executed
        home = createSubscriberTransitionHome(subContext, home);
        
        home = new BypassURCSAwareSubExtensionInstallationHome(subContext, home);

        /*
         * TODO 2006-02-14: move this home higher in the pipeline to avoid side effects
         * (create and store called) receives special shortcut cmd() from
         * SubscriberProvisionBMHome() to create subscriber checks if a create has the
         * sub already in the home and if it does, forwards to store handles commands to
         * create and store subscribers. lame :( [psperneac]
         */
        home = new SubscriberProvisionEndHome(home);

        home = new StateTransitionNotificationHome(subContext, home);
        // home = new SubscriberClctChangeHome(context, home);

        /*
         * This home must be installed *later* than SubscriberDepositHome in the pipeline,
         * since it depends on depositDate being updated by SubsciberDepositHome.
         */
        //home = new AutoDepositReleaseScheduleHome(subContext, home);
        home = new SubscriberDepositHome(subContext, home);

        home = new PMHome(subContext, SubscriberHome.class.getName() + ".factorybefore.deposithome", home);
        
        /* Consume overpayment when last subscriber is deactivated */
        home = new OverPaymentDistributionDeactivateLastSubscriberHome(subContext,home);

        // Bundle Manager Provisioning
        home = new BucketProvHome(subContext, home);
        home = new ProvisionBundleSubscriberHome(subContext, home);
        home = new PMHome(subContext, SubscriberHome.class.getName() + ".factorybefore.provisionbundlehome", home);
        
        /* It is recommended to keep SubscriberPinManagerUpdateHome execution after 
         * SubscriberProvisionServicesHome, because we are relying on the provisioning of an
         * SMS service to succeed with Pin Generation.  Also, to maintain that Pin Generation
         * is done in order after the attempts at MsisdnManagement.claim, it is recommended
         * that SubscriberPinManagerUpdateHome be placed after SubscriberVoiceMsisdnHome, 
         * SubscriberDataMsisdnHome and SubscriberFaxMsisdnHome in the pipeline execution.  
         * This will also make sure that, upon MSISDN change, by the time SubscriberPinManagerUpdateHome 
         * is run, MSISDN association/disassociation has already taken place.*/
        home = new LicenseHome(subContext, LicenseConstants.PIN_MANAGER_LICENSE_KEY,
            new SubscriberPinManagerUpdateHome(subContext, home));

        home = new LicenseHome(subContext, LicenseConstants.MULTI_LANGUAGE, new SubscriberMsisdnLanguageSettingHome(subContext,home));
        // home that propagates update to Alcatel related properties
        home = new LicenseHome(subContext, LicenseConstants.ALCATEL_LICENSE, new AlcatelSubscriberManagementHome(
                subContext, new AlcatelFunctions.AlcatalSubscriberFunction<Subscriber>(), home));
        /*
         * make sure this after than SubscriberProvisionServicesAgent, so we detect if
         * smsb has been provisioned. All IPC parameters go here.
         */
        home = new SubscriberIpcProfileUpdateHome(subContext, home);
        home = new SubscriberVpnHome(subContext, home);
        home = new TFAAuxServiceProvisionUpdationHome(subContext,home); 

        /*
         * make sure this after than SubscriberProvisionServicesAgent, so we detect if
         * SMSB has been provisioned All SMSB parameters goes here.
         */
        home = new SubscriberSmsbUpdateHome(subContext, home);

        /*
         * make sure this after than SubscriberProvisionServicesAgent, so we detect if ECP
         * has been provisioned / all ECP parameters goes here.
         */
        home = new SubscriberEcpProfileUpdateHome(subContext, home);
        home = new SubscriberEcpPackageUpdateHome(subContext, home);
        home = new SubscriberEcpGroupMsisdnUpdateHome(subContext, home);
        home = new SubscriberEcpClassOfServiceUpdateHome(subContext, home);
        home = new SubscriberEcpStateUpdateHome(subContext, home);

        home = new SubscriberBlackberryMsisdnAndPackageUpdateHome(subContext, home);

       
        // auxiliary service should be unprovisioned from HLR before service.and provisioned after 
        // service, These two homes control auxiliary service HLR provisioning in case service full
        // provisioning is triggered when msisdn switch and price plan change. It is ugly, but no 
        // other choice before we can figure out how to provision HLR more efficiently. 
        home = new ProvisionableAuxServiceProvisionHlrHome(home);
        home = new SubscriberServicesProvisioningHome(subContext, home);
        home = new UpdateTopUpScheduleOnATUHome(home);
        home = new AutoTopUpTriggerAgentHome(subContext, home);

        home = new ProvisionableAuxServiceUnprovisionHlrHome(home);
        home = new SubscriberWimaxAptiloUpdateHome(subContext, home);


        
        home = new SubscriberServicesCleanupHome(subContext, home);

        /*
         * make sure this prior to SubscriberProvisionServicesAgent, gateway will
         * supersede individual HLR service unprovisioning.
         */
        //home = new SubscriberProvisionHlrGatewayHome(subContext, home);
        home = new PredicateSkipHome(subContext, new IsSubscriberPoolLeaderPredicate(), new SubscriberProvisionHlrGatewayHome(subContext, home));

        // auxiliary service that use the URCS Profile must come after Profile - SubscriberProvisionBMHome
        home = new PersonalListPlanUpdateHome(home);
        home = new SubscriberAuxiliaryServiceCreationHome(home);

        home = new SubscriberServiceProvisionGatewayHome(home);
        
        // bulk updation of service when bulkServiceUpdate provision cmd is configured
        home = new BulkServiceUpdateHome(context, home);

        //Provision Charging home
        home = new SubscriberChargingHome(context, home); 
        
        // This home needs to come somewhere before the provision charging home, as it
        // will put the PPSM supportee extension in the context in case it's a
        // deactivation to allow the refund transactions to be generated to the supporter.
        home = new PPSMSupporteeExtensionDeactivationHome(context, home);
        
        home = new SubscriptionContractUpdateHome(context, home);
        //home = new SubChargingTestingHome(context, home);  //a debug home 
        home = new SubscriberERHome(context, home); 

        //reset Subscriber SuspensionReason
        home = new SubscriberSuspensionReasonUpdateHome(home);
        
        /*
         * SubscriberClosedUserGroupMsisdnUpdateHome should appear before the
         * SubscriberAuxiliaryServiceCreationHome in the pipeline, as it needs the
         * auxiliary services of both the old and new subscriber.
         */
        home = new SubscriberClosedUserGroupMsisdnUpdateHome(home);
        
        home = new SubscriberClosedUserGroupStateChangeHome(home);

        // The External Application Provisioning pipeline
        /*
         * For now it will only be the ABM provisioning client, but in the future we will
         * add all the other external applications.
         */
        HomeValidator externalClients = new LoggingValidatorHome(subContext, NullValidatorHome.instance());
        externalClients = new SubscriberProvisionBMHome(subContext, externalClients);

        // Register the External Application Provisioning pipeline, a validated pipeline,
        // which delegates directly to the Home pipeline
        home = new ValidatedHome(subContext, externalClients, home, true);
        
        home = new SubscriberPackageHome(home);
        
        home = new PooledGroupAccountFalseSubscriberProvisioningHome(subContext, home);

        // This home must precede the External Application (ABM, BM, HLR, and Services)
        // Provisioning
        home = new SubscriberPreCreateHome(home);
        home = new PMHome(subContext, SubscriberHome.class.getName() + ".factorybefore.precreatehome", home);
        
        home = new SubscriberProvisionLogHome(home);

        // Creates/Modifies/Deletes the notes associated with a subscriber on every
        // subscriber operation
        home = new SubscriberHomeNoteHome(home);

        /*
         * [CW] rearrange order of pipeline because we need to depend on
         * SubscriberPackageHome, SubscriberVoiceMsisdnHome, SubscriberDataMsisdnHome, and
         * SubscriberFaxMsisdnHome for data fill the subscriber profile.
         */        
        home = new SubscriberVoiceMsisdnHome(home);
        home = new SubscriberDataMsisdnHome(home);
        home = new SubscriberFaxMsisdnHome(home);

        home = new SubscriberResourceDeviceSaveHome(home);

        //Msisdn group charge before assigning 
        home = new MsisdnGroupChargingHome(context, home);
        
        // ** provisioning homes starts
        home = new SubscriberStateProfileChangeHome(home);
        home = new SubscriberConversionProfileChangeHome(home);

        // This home has to be placed after SubscriberProvisionBMHome
        home = new SubscriberPaymentPlanCreditLimitUpdateHome(home);

        // Delete records from suspendedentity table if entity is unprovisioned
        home = new SubscriberClearSuspendedEntities(home);

        home = new PMHome(subContext, SubscriberHome.class.getName() + ".factoryafter.plphome", home);

        home = new SubscriberHomezoneCountCreationHome(home);
        home = new PooledGroupAccountOwnerMsisdnResettingHome(home);

        home = new SubscriberServicesSaveHome(home);
        
        home = new SubscriberSetBundleEndDateHome(home);

        home = new SubscriberSetBundleNextRecurringChargeDateHome(home);
        
        home = new SubscriberIdAssignHome(home);

        /*
         * Fetch Dealer Code from subscriber's package on adapt and Update sub's package's
         * dealer code on unAdapt.
         */
        home = new SubscriberPackageDealerCodeHome(home);
        home = new SubscriberResourceDealerCodeUpdateDecorator(home);

        // GUI fix
        home = new SubscriberMsisdnTypeResetHome(home);

//        home = new SubscriberGroupPricePlanHome(home);

        // Validation and look-up support.
        home = new PricePlanChangeProhibitingHome(home);

        // After Subscriber update/create, if the subscriber needs to be expired/suspended, then save the subscriber again.
        final SubscriberTransitionHome th = new SubscriberTransitionHome(home);
        th.add(subContext, SubscriberTransitionHome.AFTER, SubscriberStateEnum.LOCKED_INDEX,
            SubscriberStateEnum.ACTIVE_INDEX, new SuspendSubscriber(new ResaveSubscriber()));
   
        // Before subscriber update, if going from suspended to active, check account state to see if subscriber should go to dunning state.
        th.add(subContext, SubscriberTransitionHome.BEFORE, SubscriberStateEnum.SUSPENDED_INDEX, SubscriberStateEnum.ACTIVE_INDEX, new EnforceAccountState());
        home = th;

        //The Subscriber State Service Update provisioning module.
        //Trigger updates to Subscriber Services if a subscriber state change has occurred. 
        home = new SubscriberStateChangeUpdateHome(context, home);
        
        // After activation, ensure account registration flag is set properly.
        home = new AccountRegistrationActivationHome(context, home);
        
        home = new AccountSubscriptionCountUpdateHome(context, home);
        
        home = new SupplementaryDataHandlingHome(context, home, SubscriberXInfo.SUPPLEMENTARY_DATA_LIST, SubscriberXInfo.ID, SupplementaryDataEntityEnum.SUBSCRIPTION);
        
        home = new FixedStopPricePlanBalanceExpiryHome(context, home);
        
        home = new PMHome(subContext, SubscriberHome.class.getName() + ".factoryafter.validators", home);

        
        
        // home validators

        // validates transition specific properties
        final SubscriberTransitionValidatorHome t = new SubscriberTransitionValidatorHome(home);
        t.add(subContext, SubscriberStateEnum.PENDING, SubscriberStateEnum.SUSPENDED, StartEndDateValidator.instance());
        home = t;
        
        home = new BypassCreatePipelineHome(context, home);

        home = new BypassValidationHome(SubscriberCreationPredatingValidator.instance(), SubscriberUpdatePredatingValidator.instance(), home);
        
        /*
         * TODO: remove the validating code from these homes and have Validator classes
         * for them. This way we can keep a single instance of them. [psperneac]
         */
        home = new BypassValidationHome(new SubscriberCheckHome(null), home);

        // bean validators
        final CompoundValidator validator = new CompoundValidator();
        //validator.add(DepositValidator.instance());
        validator.add(CreditLimitValidator.instance());
        validator.add(MonthlySpendLimitValidator.instance());
        validator.add(SubscriberIdValidator.instance());
        validator.add(IdNumber1Validator.instance());
        validator.add(new SubscriptionPricePlanValidator(SubscriberXInfo.PRICE_PLAN));
        validator.add(new SubscriptionPricePlanValidator(SubscriberXInfo.SECONDARY_PRICE_PLAN));
        validator.add(ParentAccountTypeValidator.instance());
        validator.add(new AvailablePackageValidator());
        validator.add(SubscriberSelectedServicesValidator.instance());
        validator.add(MsisdnAndPackageSwapValidator.instance());
        // TODO 2008-08-19 commenting for Mobile Wallet
//        validator.add(FirstLastNameValidator.instance());
//        validator.add(DateOfBirthValidator.instance());
        validator.add(DealerCodeValidator.instance());
        validator.add(SubscriberStateTypeValidator.instance());
        // Added on 10/10/2013 for TT#12030619014
        validator.add(SubscriberStateValidator.instance);
        
        validator.add(SubscriptionDisputeValidator.instance());
        validator.add(SubscriptionDeactivateValidator.instance());
        validator.add(DeactivateGroupLeaderValidator.instance());
        validator.add(VpnSubscriberValidator.instance());
        validator.add(VoicemailValidator.instance());
        validator.add(MemberGroupBundleValidator.instance());
        validator.add(SubscriberMsisdnValidator.instance());
        validator.add(SubscriberMsisdnTechnologyValidator.getInstance());
        validator.add(SubscriberMsisdnChangeValidator.instance());
        validator.add(MsisdnSwapLimitValidaor.instance());
        validator.add(SubscriberDatesValidator.instance());
        validator.add(SubscriberLicenseValidator.instance());
        validator.add(SubscriberAMsisdnValidator.instance());
        validator.add(SubscriberMultiSimValidator.instance());
        validator.add(SubscriberBillingLanguageValidator.instance());
        validator.add(SubscriberCategoryValidator.instance());
        validator.add(SubscriberDiscountClassValidator.instance());
        validator.add(SubscriberBirthdayPlanValidator.instance());
        validator.add(PlpMaxNumValidator.instance());

        // MarketingCampaignBean is embedded inside Subscriber
        validator.add(MarketingCampaignBeanDatesValidator.instance());

        // Map of BundleFee is embedded inside Subscriber
        validator.add(SubscriberBundleFeeDatesValidator.instance());

        /*
         * SubscriberServiceDisplay should be verified in this stage, instead of in
         * SubscriberServicesHome pipeline, which is not accessed until somewhere in the
         * middle of the subscriber pipeline in SubscriberServicesSaveHome.
         */
        validator.add(SubscriberServiceDisplayDatesValidator.instance());

        /*
         * TT7083100012: Moved SubscriberAuxiliaryService to this stage, instead of in
         * SubscriberAuxiliaryServiceHome pipeline, which is not accessed until somewhere
         * in the middle of the subscriber pipeline in
         * SubscriberAuxiliaryServiceCreationHome.
         */
        validator.add(SubscriberAuxiliaryServiceDatesValidator.instance());

        /*
         * Validating Initial Balance against Adjustment Limits.
         */
        validator.add(SubscriberInitialBalanceDailyAdjustmentLimitValidator.instance());
        /*
         * Check credit limit against user group adjustment limit.
         */
        validator.add(UserAdjustmentLimitValidator.instance());

        /*
         * Price Plan Validation at the Subscriber Service provisioning level.
         */
        validator.add(new PricePlanGroupValidator());

        /*
         * Subscription Class imposes restrictions on Segment type and Technology type
         */
        validator.add(SubscriptionClassSegmentAndTechnologyValidator.instance());

        /*
         * There can be only one subscription of a given type in an account
         */
       // validator.add(SubscriptionUniqueOnTypeValidator.instance());

        /*
         * Subscription can be created only in Accounts of type Subscriber
         */
        validator.add(SubscriptionInSubscriberAccountValidator.instance());
        
        validator.add(new SubscriptionResourceDeviceValidator());
        
        // Subscription notification method validation.
        validator.add(SubscriptionNotificationMethodValidator.instance());
        
        validator.add(AlcatelSSCSubscriptionValidator.instance());
        

        validator.add(SubscriberServicesPermissionsValidator.instance());
        

        /**
         * A subscription can have only one Secondary Balance Bundle(either Aux or PP).
         */
        validator.add(SingleSecondaryBalanceBundleValidator.instance());
        
        validator.add(SubscriberBalanceThresholdConfigurationValidator.instance());
        
        home = new BypassValidationHome(validator, home);

        // Set MSISDN for non-msisdn-aware subscription types
        home = new DefaultMsisdnSettingHome(home);

        home = new SubscriberRemoveValidator(subContext, home);
        
        home = new PricePlanChangeNotificationHome(home);

        home = new PricePlanSwitchCounterHome(home);
        
        // price plan version update home
        home = new SubscriberPricePlanUpdateHome(home);
        
        //Home to update the DiscountActivityTrigger table
        home = new SubscriptionDiscountEventHome(home);

        /*
         * [Cindy Wong] 2008-09-08: Add subscriber limit validator. This validator is only
         * needed on create but not store, hence is installed separately.
         */
        home = new BypassValidationHome(SubscriberLimitHomeValidator.instance(), home);
        
        //TT Fix for Feature Group Account conversion TT#13011058005 
        home = new BypassValidationHome(SubscriberCounterHomeValidator.instance(), home);
        
        
        /*
         * Depending on the subscriber type it sets the destination state on create. For
         * postpaid that state is ACTIVATED, for prepaid it is AVAILABLE.
         */
        home = new DestinationStateByTypeCreateHome(home);

        /*
         * TT7083100012: Set the end date before validation.
         */
        home = new SubscriberAuxiliaryServicePreparationHome(home);
        
        home = new BypassValidationHome(ServicesAndBundlesUpdateValidator.instance(),home);
        
        /*
         * [FlyC SgR.BSS.2259] Prevent expired subscribers from adding OPTIONAL services or bundles.
         **/
        home = new BypassValidationHome(ExpiredSubscriberSelectedServicesBundlesValidator.instance(), home);
        
        home = new BypassValidationHome(null, SubscriberStaleValidator.getInstance(), home);
        
        home = new CreditCardTopupScheduleUpdatingHome(home);
        
        home = new MsisdnDeletionOnSubscriberDeactivationHome(home);
        
        home = new ATUScheduleUpdatingOnPlanChangeHome(home);

        // put SubscriberPipeLineContextPrepareHome as early as possible
        home = new SubscriberPipeLineContextPrepareHome(home);

        // synchronize all the updates to an individual subscriber
        // TT 7070450507
        home = new SynchronizedKeyHome(home, SUBCRIBER_LOCKER);

        // This home must be outside of the PipeLineContextPrepareHome because the context
        // prepare home
        // must put the old subscriber in the context
        // no longer necessary in CRM 8.1 MM 2 because of the new Pool implementation
        // home = new SubscriberAutoActivatingHome(subContext, home);

        home = new PrepaidEntitySuspensionPreventionHome(context, home);
        
        home = new PostpaidEntitySuspensionPreventionHome(context, home);
        
        home = new PrepaidAccountDeactivationHome(context, home);
        
        home = new PMHome(subContext, SubscriberHome.class.getName() + ".factorytop", home);

        home =
		    ConfigChangeRequestSupportHelper.get(subContext)
		        .registerHomeForConfigSharing(subContext, home,
		            Subscriber.class);
        return home;
    }


    /**
     * Decorates a home with a SubscriberTransitionHome.
     *
     * @param context
     *            The operating context.
     * @param home
     *            The home being decorated.
     * @return The home decorated with SubscriberTransitionHome.
     */
    private Home createSubscriberTransitionHome(final Context context, final Home home)
    {
        final SubscriberTransitionHome t = new SubscriberTransitionHome(home);

        return t;
    }

    /**
     * The Home to which this pipeline leads.
     */
    private final Home delegate_;
	public final static String SUBSCRIBERXDB_HOME = "SUBSCRIBERXDB_HOME"; 
    public final static String SUBCRIBER_LOCKER = "SUBCRIBER_LOCKER"; 
} // class
