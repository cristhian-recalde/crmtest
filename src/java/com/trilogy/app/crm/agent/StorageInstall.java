
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

package com.trilogy.app.crm.agent;

import java.rmi.RemoteException;

import com.trilogy.framework.application.RemoteApplication;
import com.trilogy.framework.core.cron.TaskEntryHome;
import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.core.locale.CurrencyHome;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.core.platform.Ports;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.cluster.RMIClusteredMetaBean;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.entity.EntityInfoHome;
import com.trilogy.framework.xhome.entity.EntityInfoXInfo;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.CachingHome;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.CreatedAwareHome;
import com.trilogy.framework.xhome.home.CreatedByAwareHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeCmdEnum;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.LRUCachingHome;
import com.trilogy.framework.xhome.home.LastModifiedAwareHome;
import com.trilogy.framework.xhome.home.LastModifiedByAwareHome;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xhome.home.RMIHomeClient;
import com.trilogy.framework.xhome.home.RMIHomeServer;
import com.trilogy.framework.xhome.home.ReadOnlyHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.TestSerializabilityHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.home.WhereHome;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xhome.msp.SpidHome;
import com.trilogy.framework.xhome.relationship.RemoveRelationshipsOnRemoveHome;
import com.trilogy.framework.xhome.xdb.XDBMgr;
import com.trilogy.framework.xhome.xdb.XDBSupport;
import com.trilogy.framework.xlog.log.CritLogMsg;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.ContextHelper;
import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.AccountOverPaymentHistoryAccountNotesHome;
import com.trilogy.app.crm.account.AttachmentSettingsGenerationHome;
import com.trilogy.app.crm.account.BANAwareHome;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIHome;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIUserGroupAdapter;
import com.trilogy.app.crm.api.queryexecutor.ApiInterfaceHome;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodHome;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorHome;
import com.trilogy.app.crm.bas.promotion.home.CallDetailSummaryAdaptedHome;
import com.trilogy.app.crm.bas.promotion.home.HandsetPromotionHistoryHomePipelineFactory;
import com.trilogy.app.crm.bas.tps.PrefixMapping;
import com.trilogy.app.crm.bas.tps.PrefixMappingHome;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateHome;
import com.trilogy.app.crm.bas.tps.TPSAdjMap;
import com.trilogy.app.crm.bas.tps.TPSAdjMapHome;
import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.bean.account.AccountAttachmentHome;
import com.trilogy.app.crm.bean.account.AccountIdentificationHome;
import com.trilogy.app.crm.bean.account.AccountRoleHome;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerHome;
import com.trilogy.app.crm.bean.account.SpidAwareAccountIdentificationHome;
import com.trilogy.app.crm.bean.account.SpidAwareSecurityQuestionAnswerHome;
import com.trilogy.app.crm.bean.account.SubscriptionClassHome;
import com.trilogy.app.crm.bean.account.SubscriptionTypeHome;
import com.trilogy.app.crm.bean.bank.BankHome;
import com.trilogy.app.crm.bean.bank.DDROutputWriterType;
import com.trilogy.app.crm.bean.bank.DDROutputWriterTypeHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailSummaryHome;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetailHome;
import com.trilogy.app.crm.bean.ecp.rateplan.ECPRatePlan;
import com.trilogy.app.crm.bean.ecp.rateplan.ECPRatePlanHome;
import com.trilogy.app.crm.bean.ipc.IpcRatePlanHome;
import com.trilogy.app.crm.bean.payment.ContractHome;
import com.trilogy.app.crm.bean.payment.PaymentExceptionHome;
import com.trilogy.app.crm.bean.payment.PaymentPlanHome;
import com.trilogy.app.crm.bean.paymentgatewayintegration.CreateTokenHomePipelineFactory;
import com.trilogy.app.crm.bean.paymentgatewayintegration.CreditCardPrefixRateMapHomePipelineFactory;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayIntegrationConstants;
import com.trilogy.app.crm.bean.priceplan.CalldetailExtensionHome;
import com.trilogy.app.crm.bean.priceplan.CalldetailExtensionXDBHome;
import com.trilogy.app.crm.bean.price.PriceHome;
import com.trilogy.app.crm.bean.price.OneTimePriceHome;
import com.trilogy.app.crm.bean.price.RecurringPriceHome;
import com.trilogy.app.crm.bean.priceplan.SubscriptionLevelHome;
import com.trilogy.app.crm.bean.provision.ExternalProvisioningConfig;
import com.trilogy.app.crm.bean.ringbacktone.ProvCommandBean;
import com.trilogy.app.crm.bean.ui.CharSpecs;
import com.trilogy.app.crm.bean.ui.CharSpecsHome;
import com.trilogy.app.crm.bean.ui.CompatibilityGroup;
import com.trilogy.app.crm.bean.ui.CompatibilityGroupHome;
import com.trilogy.app.crm.bean.ui.PricingTemplateHome;
import com.trilogy.app.crm.bean.ui.PricingVersionHome;
import com.trilogy.app.crm.bean.ui.ProductHome;
import com.trilogy.app.crm.bean.ui.ProductHomePipelineFactory;
import com.trilogy.app.crm.bean.ui.ResourceProductHome;
import com.trilogy.app.crm.bean.ui.ServicePricing;
import com.trilogy.app.crm.bean.ui.ServicePricingHome;
import com.trilogy.app.crm.bean.ui.ServicePricingVersionNHomePipelineFactory;
import com.trilogy.app.crm.bean.ui.ServiceProductHome;
import com.trilogy.app.crm.bean.ui.SubGLCodeNHome;
import com.trilogy.app.crm.bean.ui.SubGLCodeNHomePipelineFactory;
import com.trilogy.app.crm.bean.ui.SubGLCodeVersionNHome;
import com.trilogy.app.crm.bean.ui.SubGLCodeVersionNHomePipelineFactory;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateHome;
import com.trilogy.app.crm.home.CoreUnifiedCatalogSpidConfigHomePipelineFactory;
import com.trilogy.app.crm.home.CatalogEntityHistoryHomePipelineFactory;
import com.trilogy.app.crm.bucket.BucketHistoryPipelineFactory;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkloadModuleInstall;
import com.trilogy.app.crm.bundle.BundleAuxiliaryService;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceHome;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.home.ReadWriteDelegatingHome;
import com.trilogy.app.crm.home.DiscountTransactionHistPipelineFactory;
import com.trilogy.app.crm.calculation.home.BundleProfileHomePipelineFactory;
import com.trilogy.app.crm.client.ringbacktone.PRBTConfiguration;
import com.trilogy.app.crm.client.ringbacktone.PRBTConfigurationHome;
import com.trilogy.app.crm.client.xmlhttp.XMLHTTPSupport;
import com.trilogy.app.crm.cltc.SubCltcServiceProvisionHome;
import com.trilogy.app.crm.config.AccountRequiredFieldConfig;
import com.trilogy.app.crm.config.AccountRequiredFieldConfigHome;
import com.trilogy.app.crm.config.AppEcpClientConfig;
import com.trilogy.app.crm.config.AppSmsbClientConfig;
import com.trilogy.app.crm.config.CRMConfigInfoForVRA;
import com.trilogy.app.crm.config.CRMConfigInfoForVRAHome;
import com.trilogy.app.crm.config.CallDetailConfig;
import com.trilogy.app.crm.config.IPCGPollerConfig;
import com.trilogy.app.crm.config.NGRCClientConfig;
import com.trilogy.app.crm.config.ProductAbmClientConfig;
import com.trilogy.app.crm.config.TFAClientConfig;
import com.trilogy.app.crm.config.TFACorbaClientConfigHome;
import com.trilogy.app.crm.config.TFACorbaClientConfigXMLHome;
import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.DunningPolicy;
import com.trilogy.app.crm.dunning.DunningReportHome;
import com.trilogy.app.crm.dunning.DunningReportRecordHome;
import com.trilogy.app.crm.dunning.config.DunningConfigHome;
import com.trilogy.app.crm.dunnningpolicyui.validator.DunningLevelValidator;
import com.trilogy.app.crm.dunnningpolicyui.validator.DunningPolicyValidator;
import com.trilogy.app.crm.extension.ExtensionForeignKeyAdapter;
import com.trilogy.app.crm.extension.ExtensionHandlingHome;
import com.trilogy.app.crm.extension.ExtensionInstallationHome;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtensionHome;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtensionHome;
import com.trilogy.app.crm.extension.account.LoyaltyCardExtension;
import com.trilogy.app.crm.extension.account.LoyaltyCardExtensionAdapter;
import com.trilogy.app.crm.extension.account.LoyaltyCardExtensionHome;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.PoolExtensionHome;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtension;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.AddMsisdnAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.DiscountAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.GroupChargingAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.HomeZoneAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.MultiSimAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.NGRCOptInAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.PRBTAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.ProvisionableAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.SPGAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.URCSPromotionAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.VPNAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.VoicemailAuxSvcExtensionHome;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.HomeZoneAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.MultiSimAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.NGRCOptInAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.PRBTAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.SPGAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.VPNAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.VoicemailAuxSvcExtension;
import com.trilogy.app.crm.extension.service.AlcatelSSCServiceExtensionHome;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtensionHome;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtension;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtensionHome;
import com.trilogy.app.crm.extension.spid.AlcatelSSCSpidExtension;
import com.trilogy.app.crm.extension.spid.AlcatelSSCSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.AlcatelSSCSpidExtensionXInfo;
import com.trilogy.app.crm.extension.spid.BANGenerationSpidExtension;
import com.trilogy.app.crm.extension.spid.BANGenerationSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.BANGenerationSpidExtensionTransientHome;
import com.trilogy.app.crm.extension.spid.CreditCardTopUpTypeSpidExtension;
import com.trilogy.app.crm.extension.spid.CreditCardTopUpTypeSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.CreditCardTopUpTypeSpidExtensionTransientHome;
import com.trilogy.app.crm.extension.spid.DuplicateAccountDetectionSpidExtension;
import com.trilogy.app.crm.extension.spid.DuplicateAccountDetectionSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.MinimumAgeLimitSpidExtension;
import com.trilogy.app.crm.extension.spid.MinimumAgeLimitSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.MinimumAgeLimitV2SpidExtension;
import com.trilogy.app.crm.extension.spid.MinimumAgeLimitV2SpidExtensionHome;
import com.trilogy.app.crm.extension.spid.MsisdnSwapLimitSpidExtension;
import com.trilogy.app.crm.extension.spid.MsisdnSwapLimitSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.MsisdnSwapLimitSpidExtensionTransientHome;
import com.trilogy.app.crm.extension.spid.NotificationMethodSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtension;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtensionTransientHome;
import com.trilogy.app.crm.extension.spid.PricePlanSwitchLimitSpidExtension;
import com.trilogy.app.crm.extension.spid.PricePlanSwitchLimitSpidExtensionHome;
import com.trilogy.app.crm.extension.spid.PricePlanSwitchLimitSpidExtensionTransientHome;
import com.trilogy.app.crm.extension.spid.TaxAdaptersSpidExtension;
import com.trilogy.app.crm.extension.spid.TaxAdaptersSpidExtensionHome;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtensionHome;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.DualBalanceSubExtension;
import com.trilogy.app.crm.extension.subscriber.DualBalanceSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtensionAuxSvcFlatteningHome;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.OverdraftBalanceSubExtension;
import com.trilogy.app.crm.extension.subscriber.OverdraftBalanceSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSubscriberExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplate;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterChargingTemplateHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterScreenTemplate;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterScreenTemplateHome;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.SubscriberAdvancedFeaturesHome;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionXInfo;
import com.trilogy.app.crm.extension.usergroup.AdjustmentTypeLimitUserGroupExtension;
import com.trilogy.app.crm.extension.usergroup.AdjustmentTypeLimitUserGroupExtensionHome;
import com.trilogy.app.crm.extension.usergroup.AdjustmentTypeLimitUserGroupExtensionXInfo;
import com.trilogy.app.crm.external.ecp.EcpServiceHome;
import com.trilogy.app.crm.external.ecp.EcpSubscriberHome;
import com.trilogy.app.crm.external.smsb.SmsbServiceHome;
import com.trilogy.app.crm.external.smsb.SmsbSubscriberHome;
import com.trilogy.app.crm.filter.EnabledTaskEntryPredicate;
import com.trilogy.app.crm.grr.ClientToXMLTemplateConfig;
import com.trilogy.app.crm.grr.ClientToXMLTemplateConfigHome;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfig;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfigHome;
import com.trilogy.app.crm.grr.VendorConfigHome;
import com.trilogy.app.crm.grr.XMLTemplateConfigHome;
import com.trilogy.app.crm.grr.XSDVersionConfigHome;
import com.trilogy.app.crm.home.AddressHomePipelineFactory;
import com.trilogy.app.crm.home.BasAdapter;
import com.trilogy.app.crm.home.BillCycleHistoryHomePipelineFactory;
import com.trilogy.app.crm.home.ChargingTypeIDSettingHome;
import com.trilogy.app.crm.home.ClosedUserGroupTemplateAuxiliaryServiceHome;
import com.trilogy.app.crm.home.ClosedUserGroupTemplateERLogHome;
import com.trilogy.app.crm.home.ClosedUserGroupTemplateServiceHome;
import com.trilogy.app.crm.home.ClosedUserGroupTemplateValidator;
import com.trilogy.app.crm.home.ConvergedStateMappingFactory;
import com.trilogy.app.crm.home.DeleteInvoiceHome;
import com.trilogy.app.crm.home.DependencyGroupPipelineFactory;
import com.trilogy.app.crm.home.DepositTypeHomePipelineFactory;
import com.trilogy.app.crm.home.DiscountClassTemplateInfoPipelineFactory;
import com.trilogy.app.crm.home.DiscountRulePipelineFactory;
import com.trilogy.app.crm.home.DunningLevelIdGenerator;
import com.trilogy.app.crm.home.DunningLevelRemovalHome;
import com.trilogy.app.crm.home.EcareAdapter;
import com.trilogy.app.crm.home.EnumerationConfigInitializationHome;
import com.trilogy.app.crm.home.FreeCallTimeAbmUpdateHome;
import com.trilogy.app.crm.home.FreeCallTimeInUseCheckingHome;
import com.trilogy.app.crm.home.FreeCallTimeLoggingHome;
import com.trilogy.app.crm.home.HistoryEventHomePipelineFactory;
import com.trilogy.app.crm.home.IPCGBufferFlushingHome;
import com.trilogy.app.crm.home.IdentificationHomePipelineFactory;
import com.trilogy.app.crm.home.InvoicePaymentHomesPipelineFactory;
import com.trilogy.app.crm.home.MergedBalanceHistoryHome;
import com.trilogy.app.crm.home.MergedHistoryHome;
import com.trilogy.app.crm.home.MsisdnHomePipelineFactory;
import com.trilogy.app.crm.home.MsisdnOwnershipHomePipelineFactory;
import com.trilogy.app.crm.home.MsisdnPrefixERLogHome;
import com.trilogy.app.crm.home.MsisdnPrefixValidationHome;
import com.trilogy.app.crm.home.MsisdnZonePrefixRemovalValidatorProxyHome;
import com.trilogy.app.crm.home.NotesAutoPushAllowedHome;
import com.trilogy.app.crm.home.OICKMappingUnprovisionCommandClearingHome;
import com.trilogy.app.crm.home.PTPUpdatingHome;
import com.trilogy.app.crm.home.PricingTemplateHomePipelineFactory;
import com.trilogy.app.crm.home.ProvinceHomePipelineFactory;
import com.trilogy.app.crm.home.ProvisionCommandInAdvanceSettingHome;
import com.trilogy.app.crm.home.PrvCmdIDSettingHome;
import com.trilogy.app.crm.home.RegistrationLicenseFilteringHome;
import com.trilogy.app.crm.home.ServiceActivationTemplateAuxiliaryBundleHome;
import com.trilogy.app.crm.home.ServiceActivationTemplateAuxiliaryServiceHome;
import com.trilogy.app.crm.home.SubModificationSchedulePipelineFactory;
import com.trilogy.app.crm.home.SubscriberServicesPipelineHome;
import com.trilogy.app.crm.home.SyncPTPHome;
import com.trilogy.app.crm.home.TEICHomePipelineFactory;
import com.trilogy.app.crm.home.TransactionHomePipelineFactory;
import com.trilogy.app.crm.home.UnappliedTransactionHomePipelineFactory;
import com.trilogy.app.crm.home.UsageTypeIDSettingHome;
import com.trilogy.app.crm.home.UsageTypePreventDefaultItemDeleteHome;
import com.trilogy.app.crm.home.account.AccountHomePipelineFactory;
import com.trilogy.app.crm.home.account.AccountsGroupScreeningTemplatePipelineFactory;
import com.trilogy.app.crm.home.account.ContactHomeFactory;
import com.trilogy.app.crm.home.account.extension.AccountExtensionInstallationHome;
import com.trilogy.app.crm.home.account.extension.FriendsAndFamilyExtensionHomePipelineFactory;
import com.trilogy.app.crm.home.accountmanager.AccountManagerHomePipelineFactory;
import com.trilogy.app.crm.home.agent.CreateDefaultUsageTypeEntry;
import com.trilogy.app.crm.home.calldetail.CallDetailHomePipelineFactory;
import com.trilogy.app.crm.home.calldetail.MTCallDetailHomePipelineFactory;
import com.trilogy.app.crm.home.calldetail.PrepaidCallingCardCallDetailHomePipelineFactory;
import com.trilogy.app.crm.home.calldetail.RerateCallDetailHomePipelineFactory;
import com.trilogy.app.crm.home.grr.ClientToXMLTemplateConfigHomeProxy;
import com.trilogy.app.crm.home.pipelineFactory.*;
import com.trilogy.app.crm.home.pipelineFactory.grr.GrrGeneratorGeneralConfigHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.grr.VendorConfigHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.grr.XMLTemplateConfigHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.grr.XSDVersionConfigHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.ui.AuxiliaryServiceUIHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.ui.BundleProfileUIHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.ui.ChargingTemplateUIHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.ui.CreditCategoryUIHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.ui.MsisdnUIHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.ui.ServiceUIHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.ui.SubscriptionContractUIHomePipelineFactory;
import com.trilogy.app.crm.home.sub.DebuggingNotSelectAllHome;
import com.trilogy.app.crm.home.sub.DeleteCatagoryHome;
import com.trilogy.app.crm.home.sub.NoDeleteMarketingCampaign;
import com.trilogy.app.crm.home.sub.PreloadLazyValuesHome;
import com.trilogy.app.crm.home.sub.SubscriberHomeFactory;
import com.trilogy.app.crm.home.sub.extension.CustomPPSMSupporteeSubExtensionXDBHome;
import com.trilogy.app.crm.home.sub.extension.PPSMSupporterSubscriberExtensionTemplatesMappingSavingHome;
import com.trilogy.app.crm.home.transfer.TransferContractPipelineFactory;
import com.trilogy.app.crm.home.transfer.TransferTransactionAdapterProxyHome;
import com.trilogy.app.crm.home.transfer.TransfersFilterHome;
import com.trilogy.app.crm.integration.pc.TechnicalServiceTemplateHomePipelineFactory;
import com.trilogy.app.crm.invoice.bean.InvoiceSpid;
import com.trilogy.app.crm.invoice.bean.InvoiceSpidHome;
import com.trilogy.app.crm.log.DunningActionERHome;
import com.trilogy.app.crm.log.DunningActionHomeFactory;
import com.trilogy.app.crm.log.SubscriptionActivationERHome;
import com.trilogy.app.crm.log.SubscriptionActivationHomeFactory;
import com.trilogy.app.crm.log.SubscriptionModificationERHome;
import com.trilogy.app.crm.log.SubscriptionModificationHomeFactory;
import com.trilogy.app.crm.bean.Address;
import com.trilogy.app.crm.bean.ChargedBundleInfoHome;
import com.trilogy.app.crm.bean.ChargedBundleInfoXDBHome;
import com.trilogy.app.crm.bean.ui.DiscountRuleHome;
import com.trilogy.app.crm.numbermgn.HistoryEventHome;
import com.trilogy.app.crm.numbermgn.ImsiMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.ImsiMgmtHistoryXDBHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.PackageMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.PackageMgmtHistoryXDBHome;
import com.trilogy.app.crm.poller.home.ErPollerConfigHomeChangeListener;
import com.trilogy.app.crm.pos.AccountAccumulator;
import com.trilogy.app.crm.pos.AccountAccumulatorHome;
import com.trilogy.app.crm.pos.PointOfSaleConfiguration;
import com.trilogy.app.crm.pos.SubscriberAccumulator;
import com.trilogy.app.crm.pos.SubscriberAccumulatorHome;
import com.trilogy.app.crm.priceplan.PricePlanMigrationHomePipelineFactory;
import com.trilogy.app.crm.priceplan.validator.PricePlanGroupListValidator;
import com.trilogy.app.crm.provision.gateway.SPGServiceHome;
import com.trilogy.app.crm.provision.gateway.SPGServiceHomeFactory;
import com.trilogy.app.crm.sat.SATValidatorFactory;
import com.trilogy.app.crm.sat.ServiceActivationTemplateERCreationHome;
import com.trilogy.app.crm.sat.ServiceActivationTemplateIdentifierSettingHome;
import com.trilogy.app.crm.sat.ServiceActivationTemplateOMCreationHome;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.service.ServicePackageVersionHomePipelineFactory;
import com.trilogy.app.crm.subscriber.provision.ecp.EcpRatePlanCorbaHome;
import com.trilogy.app.crm.subscriber.provision.ipc.IpcRatePlanSwitchHome;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionsPipelineFactory;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriptionProvisioningHistoryPipelineFactory;
import com.trilogy.app.crm.subscriber.validator.SubscriberMultiSimValidator;
import com.trilogy.app.crm.support.AutoIncrementSupport;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.support.Tracer;
import com.trilogy.app.crm.technology.TechnologyAwareHome;
import com.trilogy.app.crm.transfer.ContractGroupMembershipPipelineFactory;
import com.trilogy.app.crm.transfer.TransferDisputeHomePipelineFactory;
import com.trilogy.app.crm.transfer.Transfers;
import com.trilogy.app.crm.transfer.TransfersHome;
import com.trilogy.app.crm.transfer.TransfersViewHome;
import com.trilogy.app.crm.util.cipher.SpidAwareEncryptingAdapter;
import com.trilogy.app.crm.xhome.adapter.BeanAdapter;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.app.crm.xhome.home.ContextRedirectingHome;
import com.trilogy.app.crm.xhome.home.KeyValueEntryFlatteningHome;
import com.trilogy.app.crm.xhome.home.SetPrincipalHome;
import com.trilogy.app.crm.xhome.home.SetPrincipalRmiServerHome;
import com.trilogy.app.crm.xhome.home.TotalCachingHome;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;
import com.trilogy.app.smsb.ProvisionedRatePlansHome;
import com.trilogy.service.home.operations.access.AuthenticatingRMIHomeClient;
//import com.trilogy.app.crm.home.pipelineFactory.SmsDisputeNotificationConfigHomePipelineFactory;
//import com.trilogy.app.crm.home.pipelineFactory.SubPaymnetMsgConfigPipelineFactory;
//import com.trilogy.app.crm.home.pipelineFactory.SubPreWarnMsgConfigPipelineFactory;
//import com.trilogy.app.crm.home.pipelineFactory.SubProfChgMsgConfigPipelineFactory;
//import com.trilogy.app.crm.home.pipelineFactory.SubSuspendMsgConfigPipelineFactory;
import com.trilogy.app.crm.dunning.DunningWaiverHome;
import com.trilogy.app.crm.dunning.LevelInfo;
import com.trilogy.app.crm.dunning.LevelInfoHome;
import com.trilogy.app.crm.dunning.DunningPolicy;
import com.trilogy.app.crm.dunning.DunningPolicyHome;
import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.DunningLevelHome;
import com.trilogy.app.crm.dunning.DunningLevelXMLHome;
import com.trilogy.app.crm.dunning.DunningPolicyXMLHome;
import com.trilogy.app.crm.bean.ui.PriceTemplateHome;
import com.trilogy.app.crm.integration.pc.OneTimePriceHomePipelineFactory;
import com.trilogy.app.crm.integration.pc.PriceHomePipelineFactory;
import com.trilogy.app.crm.integration.pc.PriceTemplateHomePipelineFactory;
import com.trilogy.app.crm.integration.pc.RecurringPriceHomePipelineFactory;
import com.trilogy.app.crm.home.pipelineFactory.PaymentFileTrackerRecordHomePipeLineFactory;
import com.trilogy.app.crm.bean.account.*;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.home.core.CoreGLCodeNHomePipelineFactory;
import com.trilogy.app.crm.home.core.CoreGLCodeHomePipelineFactory;
import com.trilogy.app.crm.bean.ui.GLCodeNVersionHomePipelineFactory;
import com.trilogy.app.crm.bean.ui.PackageProductHome;
import com.trilogy.app.crm.home.core.GLCodeAdapter;


// [CW] TODO separate this file into the proper BAS and ECARE version
/**
 * This agent initializes all the storage specific to the CRM applicationg
 *
 * @author     Kevin Greer
 * @since    Aug 14, 2003
 */
public class StorageInstall extends CoreSupport implements ContextAgent
{
    /**
     * Initializes all the storage specific to the CRM applicationg
     * @param  ctx Description of the Parameter
     * @exception  AgentException  Description of the Exception
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        Context serverCtx = (Context) ctx.get(RMI_SERVER_CTX_KEY);
        if (serverCtx == null)
        {
            serverCtx = ctx.createSubContext(RMI_SERVER_CTX_KEY);
            ctx.put(RMI_SERVER_CTX_KEY,serverCtx);
        }

        ContextHelper.setContext(ctx); 
        
        // this gives us access to the RMIHomeServers and the context that is passed by BAS to the homes.
        ctx.put(RMI_SERVER_CTX_KEY, serverCtx);

        final RemoteApplication basApp = StorageSupportHelper.get(ctx).retrieveRemoteBASAppConfig(ctx);
        final int basRemotePort = basApp!=null? basApp.getBasePort()+ Ports.RMI_OFFSET : CoreCrmConstants.BAS_PORT;
        new InfoLogMsg(this, "BAS is configured to be "+basApp, null).log(ctx);

        try
        {
        	// this sets up the database alias to be used with Crm
            // will make these exceptions go away:
            // 2005-06-06 16:00:28,437 [poller_thread_0] MAJOR com.redknee.framework.xhome.xdb.XDBMgr - no XDB alias was found in context, using default configuration.
            XDBSupport.putXDBAlias(ctx, XDBMgr.XDB_ALIAS_DEFAULT);
            
            new SubscriberStateActionHomePipelineFactory().createPipeline(ctx, serverCtx);

            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                final RMIHomeServer server =
                    new RMIHomeServer(
                            serverCtx,
                            (Home)ctx.get(IdentifierSequenceHome.class),
                            IdentifierSequenceHome.class.getName());
                server.register();
            }
            else if (DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
            {
                // override installed home with a RMIHomeClient
                ctx.put(IdentifierSequenceHome.class,
                        new TestSerializabilityHome(ctx,
                                new RMIHomeClient(
                                        ctx,
                                        basApp.getHostname(),
                                        basRemotePort,
                                        IdentifierSequenceHome.class.getName())));
            }
            ctx.put(WriteOffConfigHome.class,CoreSupport.bindHome(ctx,WriteOffConfig.class));
            //ctx.put(CronStatHome.class,CoreSupport.bindHome(ctx,CronStat.class));
            ctx.put(CronStatHome.class,new CronStatXMLHome(ctx,getFile(ctx, "CronStats.xml"),true));

            // Keeping complete TaskEntryHome in the context for CronInstall.
            ctx.put(CronInstall.CRON_INSTALL_TASK_ENTRY_HOME, ctx.get(TaskEntryHome.class));

            // Filtering disabled tasks.
            ctx.put(TaskEntryHome.class, new WhereHome(ctx, (Home) ctx.get(TaskEntryHome.class), EnabledTaskEntryPredicate.getInstance()));
            
            //HLRConfigurationHomePipelineFactory.instance().createPipeline(ctx, serverCtx);
            
            // [CW] required by ECARE. Direct conn to DB
            // Subscriber's Balance History home
            ctx.put(
                    BalanceHistoryHome.class,
                    new NoSelectAllHome( new MergedBalanceHistoryHome()));

            ctx.put(PricePlanMigrationHome.class, PricePlanMigrationHomePipelineFactory.instance().createPipeline(ctx,
                    serverCtx));

            // [jhughes] remote by Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        ctx,
                                        BalanceHistoryHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(BalanceHistoryHome.class))),
                                        BalanceHistoryHome.class.getName()).register();
            }

            // [CW] required by ECARE. Direct conn to DB
            // Account History home
            ctx.put( AccountHistoryHome.class, new MergedHistoryHome());

            // [jhughes] remote by Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        ctx,
                                        AccountHistoryHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(AccountHistoryHome.class))),
                                        AccountHistoryHome.class.getName()).register();
            }

            // [CW] clustered by all
            ctx.put(SpidHome.class,
                    new ReadOnlyHome(ctx,
                        new AdapterHome(ctx,
                            new ContextRedirectingHome(ctx, CRMSpidHome.class),
                            new BeanAdapter<CRMSpid, Spid>(CRMSpid.class, Spid.class))));
            ctx.put(InvoiceSpidHome.class,
                    new ReadOnlyHome(ctx,
                        new AdapterHome(ctx,
                            new ContextRedirectingHome(ctx, CRMSpidHome.class),
                            new BeanAdapter<CRMSpid, InvoiceSpid>(CRMSpid.class, InvoiceSpid.class))));
            
            ctx.put(InvoiceHome.class, new DeleteInvoiceHome(ctx, (Home) ctx.get(InvoiceHome.class)));
            ctx.put(WalletReportHome.class, new DeleteInvoiceHome(ctx, (Home) ctx.get(WalletReportHome.class)));
			ctx.put(DiscountTransactionHistHome.class,
					new DiscountTransactionHistPipelineFactory().createPipeline(ctx, serverCtx));
            // Install any configurations
            installConfigurationStorage(ctx, serverCtx);
            
            // Install spid extensions
            installSpidExtensions(ctx, serverCtx, basApp, basRemotePort);

			//installTechnicalServiceTemplateExtensions(ctx, serverCtx, basApp, basRemotePort);
			
			installServiceExtensions(ctx, serverCtx, basApp, basRemotePort);

            installAuxiliaryServiceExtensions(ctx, serverCtx, basApp, basRemotePort);
            
            // Install user group extensions
            installUserGroupExtensions(ctx, serverCtx, basApp, basRemotePort);

            // Install Adjustment Type Enhanced GUI
            installAdjustmentTypeEnhancedGUI(ctx, serverCtx);

            ctx.put(IpcRatePlanHome.class, new IpcRatePlanSwitchHome(ctx));

            ctx.put(ECPRatePlanHome.class,new LRUCachingHome(ctx, ECPRatePlan.class,true,new EcpRatePlanCorbaHome(ctx)));

            /* MSISDN home pipeline creator*/
            ctx.put(MsisdnHome.class, new MsisdnHomePipelineFactory().createPipeline(ctx, serverCtx));

            ctx.put(com.redknee.app.crm.bean.ui.MsisdnHome.class, new MsisdnUIHomePipelineFactory().createPipeline(ctx,serverCtx));
            /*
             * MSISDN Ownership home pipeline creator.
             * Note that the MsisdnOwnershipHome is dependent on the MsisdnHome as it uses adaption
             * */
            new MsisdnOwnershipHomePipelineFactory().createPipeline(ctx, serverCtx);
            /* MsisdnGroupHome popeline creator*/
            new MsisdnGroupHomePipelineFactory().createPipeline(ctx, serverCtx);
            
            ctx.put(GrrGeneratorGeneralConfigHome.class, new GrrGeneratorGeneralConfigHomePipelineFactory().createPipeline(ctx,serverCtx));
            
            ctx.put(VendorConfigHome.class, new VendorConfigHomePipelineFactory().createPipeline(ctx,serverCtx));
            
            ctx.put(XSDVersionConfigHome.class, new XSDVersionConfigHomePipelineFactory().createPipeline(ctx,serverCtx));
            
            ctx.put(XMLTemplateConfigHome.class, new XMLTemplateConfigHomePipelineFactory().createPipeline(ctx,serverCtx));
            
            ctx.put(ClientToXMLTemplateConfigHome.class, new ClientToXMLTemplateConfigHomeProxy(ctx,
            		bindHome(ctx,ClientToXMLTemplateConfig.class)));
            
            ctx.put(DDOutboundFileTrackHome.class, new DDOutboundFileTrackXDBHome(ctx, "DDOUTBOUNDFILETRACK"));
            
            ctx.put(AdjustmentTypeHome.class, new AdjustmentTypeHomePipelineFactory().createPipeline(ctx,serverCtx));

            new ExternalAppMappingHomePipelineFactory().createPipeline(ctx, serverCtx);
            
            // [CW] clustered by all
            ctx.put(ServiceHome.class, new ServiceHomePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(TSParameterTemplateHome.class,
					StorageSupportHelper.get(ctx).createHome(ctx, TSParameterTemplate.class, "TSPTEMPLATE"));
			ctx.put(TechnicalServiceTemplateHome.class,
					new TechnicalServiceTemplateHomePipelineFactory().createPipeline(ctx, serverCtx));

            ctx.put(UserDailyAdjustmentLimitHome.class, new UserDailyAdjustmentLimitHomePipelineFactory().createPipeline(ctx, serverCtx));

            ctx.put(ApiInterfaceHome.class, new ApiInterfaceHomePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(ApiMethodQueryExecutorHome.class, new ApiMethodQueryExecutorHomePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(ApiMethodHome.class, new ApiMethodHomePipelineFactory().createPipeline(ctx, serverCtx));

            ctx.put(SPGServiceHome.class, new SPGServiceHomeFactory().createPipeline(ctx, serverCtx));

            ctx.put(com.redknee.app.crm.bean.ui.ServiceHome.class, new ServiceUIHomePipelineFactory().createPipeline(ctx, serverCtx));
				ctx.put(PricingTemplateHome.class, new PricingTemplateHomePipelineFactory().createPipeline(ctx, serverCtx));
			ctx.put(CharSpecsHome.class, StorageSupportHelper.get(ctx).createHome(ctx, CharSpecs.class, "CHARSPECS"));

            // A home that holds Alcatel SSC Data
            // Alcatel System is license controlled
            //        if(LicensingUtil.isLicensed(ctx, LicenseConstants.ALCATEL_LICENSE))
            //        {
            //           new AlcatelDataHomePipelineFactory().createPipeline(ctx, serverCtx);
            //        }

            // [RP] Corba based homes for RatePlans
            RatePlanHomePipelineFactory.instance().createPipeline(ctx, serverCtx);

            // [CW] required for BAS
            // Stores requests to update a subscriber based on the propagation of a
            // new version of their price plan being updated.
            ctx.put(PricePlanVersionUpdateRequestHome.class,
                    StorageSupportHelper.get(ctx).createHome(ctx, PricePlanVersionUpdateRequest.class,"PPVUPDATEREQ"));
            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    (Home)ctx.get(PricePlanVersionUpdateRequestHome.class),
                    PricePlanVersionUpdateRequestHome.class,
                    basApp.getHostname(),
                    basRemotePort);


            ctx.put(Common.PRICE_PLAN_VERSION_UPDATE_REQUEST_ERROR_HOME,
                    StorageSupportHelper.get(ctx).createHome(ctx, PricePlanVersionUpdateRequest.class,"PPVUPDATEREQ_ERR"));

            // [CW] controlled by BAS
            ctx.put(
                    PricePlanHome.class,
                    new PricePlanHomePipelineFactory().createPipeline(ctx, serverCtx));

            //simar paul singh
            Home subscriberCategoryHome =new RMIClusteredHome(ctx, SubscriberCategory.class.getName(),new DeleteCatagoryHome(ctx, CoreSupport.bindHome(ctx, SubscriberCategory.class)));
            subscriberCategoryHome = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, subscriberCategoryHome, SubscriberCategory.class);
            final Home campaignConfigHome =new RMIClusteredHome(ctx, CampaignConfig.class.getName(), new NoDeleteMarketingCampaign(ctx, CoreSupport.bindHome(ctx, CampaignConfig.class)));

            ctx.put(SubscriberCategoryHome.class, subscriberCategoryHome);
            ctx.put(CampaignConfigHome.class, campaignConfigHome);

            ctx.put(SubscriptionClassHome.class,
                    SubscriptionClassHomePipelineFactory.instance().createPipeline(ctx, serverCtx));

            ctx.put(SubscriptionTypeHome.class,
                    SubscriptionTypeHomePipelineFactory.instance().createPipeline(ctx, serverCtx));

            ctx.put(SubscriptionLevelHome.class,
                    SubscriptionLevelHomePipelineFactory.instance().createPipeline(ctx, serverCtx));

            ctx.put(SecurityQuestionAnswerHome.class, SecurityQuestionAnswerHomePipelineFactory.instance().createPipeline(ctx, serverCtx));
            
            ctx.put(SpidAwareSecurityQuestionAnswerHome.class,SpidAwareSecurityQuestionAnswerHomePipelineFactory.instance().createPipeline(ctx, serverCtx));
			
            ctx.put(AccountIdentificationHome.class,
			    AccountIdentificationHomePipelineFactory.instance()
			        .createPipeline(ctx, serverCtx));
            
            ctx.put(SpidAwareAccountIdentificationHome.class,
            		SpidAwareAccountIdentificationHomePipelineFactory.instance()
            		.createPipeline(ctx, serverCtx));

            ctx.put(AccountRoleHome.class,
                    AccountRoleHomePipelineFactory.instance().createPipeline(ctx, serverCtx));

            //-----------ADD DUNNING---------------------------------------------------
      
            
            Home levelInfoHome = LevelInfoHomePipelineFactory.instance().createPipeline(ctx, serverCtx);
			levelInfoHome = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, levelInfoHome,
					LevelInfo.class);

			ctx.put(LevelInfoHome.class, levelInfoHome);

			Home dunningLevelHome = new DunningLevelXMLHome(ctx, CoreSupport.getFile(ctx, "DunningLevelConfig.xml"));
			dunningLevelHome = new SortingHome(dunningLevelHome);
			final CompoundValidator dunningLevelValidator = new CompoundValidator();
			dunningLevelValidator.add(DunningLevelValidator.instance());
			dunningLevelHome = new ValidatingHome(dunningLevelValidator,
					new DunningLevelIdGenerator(ctx, dunningLevelHome));
			dunningLevelHome = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx,
					dunningLevelHome, DunningLevel.class);
			ctx.put(DunningLevelHome.class, dunningLevelHome);

			ctx.put(DunningNotificationConfigHome.class,
					new DunningNotificationConfigHomePipelineFactory().createPipeline(ctx, serverCtx));

			Home dunningPolicyHome = new SyncPTPHome(ctx, new DunningLevelRemovalHome(ctx,
					new DunningPolicyXMLHome(ctx, CoreSupport.getFile(ctx, "DunningPolicyConfig.xml"), true)));
			final CompoundValidator dunningPolicyValidator = new CompoundValidator();
			dunningPolicyValidator.add(DunningPolicyValidator.instance());
			dunningPolicyHome = new ValidatingHome(dunningPolicyValidator, dunningPolicyHome);
			dunningPolicyHome = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx,
					dunningPolicyHome, DunningPolicy.class);
			ctx.put(DunningPolicyHome.class, dunningPolicyHome);

			
            // -------------------------------------------------------------------------
            ctx.put(SupplementaryDataReqFieldsHome.class,
                    SupplementaryDataRequiredFieldsHomePipelineFactory.instance().createPipeline(ctx, serverCtx));

            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    (Home)ctx.get(PricePlanHome.class),
                    PricePlanHome.class,
                    basApp.getHostname(),
                    basRemotePort);

            // [CW] required by all

            // [CW] controlled by BAS
            ctx.put(
                    PricePlanVersionHome.class,
                    new PricePlanVersionHomePipelineFactory().createPipeline(ctx, serverCtx));

            ctx.put(
                    PPVModificationRequestHome.class,
                    new PricePlanVersionModificationRequestHomePipelineFactory().createPipeline(ctx, serverCtx));

            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    (Home)ctx.get(PricePlanVersionHome.class),
                    PricePlanVersionHome.class,
                    basApp.getHostname(),
                    basRemotePort);


            // TODO this should be deleted
            ctx.put(
                    FreeCallTimeHome.class,
                    new NoSelectAllHome(
                            new SpidAwareHome(ctx,
                                    new FreeCallTimeLoggingHome(ctx,
                                            new FreeCallTimeInUseCheckingHome(ctx,
                                                    new FreeCallTimeAbmUpdateHome(ctx,
                                                            StorageSupportHelper.get(ctx).createHome(ctx,FreeCallTime.class,"FREECALLTIME")))))));

            // [jhughes] remote for Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        serverCtx,
                                        SubscriberCycleUsageHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(SubscriberCycleUsageHome.class))),
                                        SubscriberCycleUsageHome.class.getName()).register();
            }



            // [jhughes] remote for Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        serverCtx,
                                        SubscriberCycleBundleUsageHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(SubscriberCycleBundleUsageHome.class))),
                                        SubscriberCycleBundleUsageHome.class.getName()).register();
            }

			ctx.put(DealerCodeHome.class, new DealerCodeHomePipelineFactory().createPipeline(ctx, serverCtx));

            ctx.put(DunningActionERHome.class,
                    new DunningActionHomeFactory().createPipeline(ctx, serverCtx));
            ctx.put(SubscriptionActivationERHome.class,
                    new SubscriptionActivationHomeFactory().createPipeline(ctx, serverCtx));
            ctx.put(SubscriptionModificationERHome.class,
                    new SubscriptionModificationHomeFactory().createPipeline(ctx, serverCtx));

			ctx.put(GSMPackageHome.class, new GSMPackagePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(TDMAPackageHome.class, new TDMAPackagePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(VSATPackageHome.class, new VSATPackagePipelineFactory().createPipeline(ctx, serverCtx));
            
			ctx.put(ServiceCategoryHome.class, new CustomizedServiceCategoryHomePipelineFactory().createPipeline(ctx, serverCtx)); 
            
            new PackageGroupPipelineFactory().createPipeline(ctx, serverCtx);
            
            new PackageTypePipelineFactory().createPipeline(ctx, serverCtx);
           
            new PackageBulkLoaderPipelineFactory().createPipeline(ctx, serverCtx);
            new PackageDealerBulkUpdatePipelineFactory().createPipeline(ctx, serverCtx);
            new ResourceDealerBulkUpdatePipelineFactory().createPipeline(ctx, serverCtx);
            new GenericBeanBulkLoaderPipelineFactory().createPipeline(ctx, serverCtx);

            Home subXdbHome = StorageSupportHelper.get(ctx).createHome(ctx, Subscriber.class, "SUBSCRIBER");
            //subXdbHome = new CreditCardTopupScheduleUpdatingHome(subXdbHome);
            ctx.put(SubscriberXDBHome.class, subXdbHome);
            
            Home subscriberHome = new SubscriberHomeFactory(subXdbHome).createPipeline(ctx, serverCtx);

            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx))
            {
                subscriberHome = new SetPrincipalRmiServerHome(subscriberHome);
                // A work-around to avoid performance issue occurred in TT9031200065
                subscriberHome = new DebuggingNotSelectAllHome(subscriberHome);

                //add the new adapting home TT:7030745954
                subscriberHome = new AdapterHome(subscriberHome, new BasAdapter());
            }
            ctx.put(SubscriberHome.class, subscriberHome);

            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    subscriberHome,
                    SubscriberHome.class,
                    basApp.getHostname(),
                    basRemotePort);

            if (DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
            {
                subscriberHome = (Home)ctx.get(SubscriberHome.class);
                // A work-around to avoid performance issue occurred in TT9031200065
                subscriberHome = new DebuggingNotSelectAllHome(subscriberHome);

                //add the new adapting home TT:7030745954
                subscriberHome = new AdapterHome(subscriberHome, new EcareAdapter());
                subscriberHome = new SetPrincipalHome(subscriberHome);
                subscriberHome = new ContextualizingHome(ctx, subscriberHome);
                ctx.put(SubscriberHome.class, subscriberHome);
            }
            else
            {
                // In some cases, WSC needs a version of the subscriber home that
                // preloads lazy-loaded properties since it cannot.
                final String serverKey = "PRELOAD" + SubscriberHome.class.getName();
                Home preloadHome = new PreloadLazyValuesHome(ctx, subscriberHome);
                preloadHome = new PMHome(ctx, serverKey + ".rmiserver", preloadHome);
                final RMIHomeServer preloadServer =
                    new RMIHomeServer(serverCtx, preloadHome, serverKey);

                preloadServer.register();
            }

            // Install subscriber extensions
            installSubscriberExtensions(ctx, serverCtx, basApp, basRemotePort);

            /*
             * Invoice payment record.
             */
            new InvoicePaymentHomesPipelineFactory().createPipeline(ctx, serverCtx);

            //new ConvergeSubscriberStateTransitionSupport().installDefaultStateTransitions(ctx);

            /*
             * Creates the decorators for the SubscriberServices home pipeline
             *
             */
            new SubscriberServicesPipelineHome().createPipeline(ctx, serverCtx);

            new SubscriberSubscriptionsPipelineFactory().createPipeline(ctx, serverCtx);
            new SubscriptionProvisioningHistoryPipelineFactory().createPipeline(ctx, serverCtx);
            
            enableAutoIncrement(ctx,(Home)ctx.get(ReasonCodeHome.class));

            // direct connection to DB
			ctx.put(ActivationReasonCodeHome.class,
			    new ActivationReasonCodeHomePipelineFactory().createPipeline(
			        ctx, serverCtx));


            // [jhughes] remote for Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        serverCtx,
                                        ReasonCodeHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(ReasonCodeHome.class))),
                                        ReasonCodeHome.class.getName()).register();

                // [Angie Li: remote for Invoice Server]
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        serverCtx,
                                        ActivationReasonCodeHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(ActivationReasonCodeHome.class))),
                                        ActivationReasonCodeHome.class.getName()).register();
            }

            // [CW] direct connection to DB
            // create a Handset Promotion History for Usage Report Generation Record
            ctx.put(HandsetPromotionHistoryHome.class, HandsetPromotionHistoryHomePipelineFactory.instance()
                    .createPipeline(ctx, serverCtx));

            // [CW] controlled by BAS
            // a history table for cron task update per spid
            ctx.put(PromotionCronTaskHistoryHome.class,
                    new AuditJournalHome(ctx,
                            new PromotionCronTaskHistoryXDBHome(ctx,"PROMOTIONCRONTASKHISTORY")));
            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    (Home)ctx.get(PromotionCronTaskHistoryHome.class),
                    PromotionCronTaskHistoryHome.class,
                    basApp.getHostname(),
                    basRemotePort);

            // [CW] direct connection to DB
            // TODO: remove the table from Db
            // for summary report
            ctx.put(CallDetailSummaryHome.class,new CallDetailSummaryAdaptedHome(ctx,NullHome.instance()));

            // [CW] direct connection to DB
            ctx.put(SubscriberCltcHome.class,
                    new NoSelectAllHome(
                            new SubCltcServiceProvisionHome(ctx,
                                    new SubscriberCltcXDBHome(ctx,"SUBSCRIBERCLTC"))));

            // [CW] controlled by BAS
            ctx.put(SubscriberSmsQueHome.class,
                    new SubscriberSmsQueXDBHome(ctx,"SUBSCRIBERSMSQUE"));


            // [CW] clustered. Only needed by BAS
            ctx.put(
                    SmscTxConnectionConfigHome.class,
                    new RMIClusteredHome(ctx, SmscTxConnectionConfigHome.class.getName(),
                            (Home) ctx.get(SmscTxConnectionConfigHome.class)));
            
            // was true, autoinc

            // direct debit
            ctx.put(DDROutputWriterTypeHome.class,CoreSupport.bindHome(ctx, DDROutputWriterType.class));

            // [CW] clustered required by all
            ctx.put(
                    PrefixMappingHome.class,
                    new RMIClusteredHome(ctx, PrefixMappingHome.class.getName(),
                            new AuditJournalHome(ctx, CoreSupport.bindHome(ctx, PrefixMapping.class))));

            // [CW] clustered required by all
            new RMIClusteredMetaBean(
                    ctx,
                    SmppConfig.class.getName(),
                    SmppConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            final ContactHomeFactory contactHomeFactory = ContactHomeFactory.instance();
            final Home contactHome = contactHomeFactory.createPipeline(ctx, serverCtx);
            ctx.put(ContactHome.class, contactHome);

            //Tbaytel requirement multiplay discount
            ctx.put(AccountRelationshipHome.class, new AccountRelationshipHomePipelineFactory().createPipeline(ctx, serverCtx));
            
			// [CW] clustered required by all
			final Home acctCategoryHome =
			    AccountCategoryHomePipelineFactory.instance().createPipeline(
			        ctx, serverCtx);
			ctx.put(AccountCategoryHome.class, acctCategoryHome);
			enableAutoIncrement(ctx, (Home) ctx.get(AccountCategoryHome.class));

            // We need this in the MoveAccountRequestServicer's condensed Account pipeline
            // So that the cache between the 2 pipelines will be consistent
            ctx.put(Common.ACCOUNT_CACHED_HOME,
                    // Reset lazy-loaded fields that could become stale before storing accounts to the cache.
                    new TransientFieldResettingHome(ctx,
                            // This LRUCachingHome isn't just for performance reasons.
                            // It is required in order to avoid infinite recursions.
                            new LRUCachingHome(ctx, Account.class, true,
                                    StorageSupportHelper.get(ctx).createHome(ctx, Account.class, "ACCOUNT")),
                                    //AccountXInfo.ACCOUNT_EXTENSIONS,
                                    AccountXInfo.ACCUMULATED_BUNDLE_MINUTES,
                                    AccountXInfo.ACCUMULATED_BUNDLE_MESSAGES,
                                    AccountXInfo.ACCUMULATED_BALANCE,
                                    AccountXInfo.ACCUMULATED_MDUSAGE,
                                    AccountXInfo.ACCOUNT_EXTENSIONS,
                                    AccountXInfo.ACCOUNT_IDENTIFICATION_LOADED,
                                    AccountXInfo.SECURITY_QUESTION_AND_ANSWER_LOADED,
                                    AccountXInfo.SUPPLEMENTARY_DATA_LOADED,
                                    AccountXInfo.CONTACTS_LOADED));

            /* Need to install rmi server for the account chached home so that
             * account moves from ecare do not result in a stale cache on ecare node.
             */
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx))
            {
                final RMIHomeServer server =
                    new RMIHomeServer(
                            serverCtx,
                            new ContextualizingHome(ctx, new PMHome(ctx, Common.ACCOUNT_CACHED_HOME + ".rmiserver", (Home)ctx.get(Common.ACCOUNT_CACHED_HOME))),
                            Common.ACCOUNT_CACHED_HOME);
                server.register();
            }
            else if (DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
            {
                // override the default home with RMIHomeClient
                ctx.put(Common.ACCOUNT_CACHED_HOME,
                        new NoSelectAllHome(
                                new ContextualizingHome(ctx,
                                        new TestSerializabilityHome(ctx,
                                                new PMHome(ctx, Common.ACCOUNT_CACHED_HOME + ".rmiclient",
                                                        new RMIHomeClient(
                                                                ctx,
                                                                basApp.getHostname(),
                                                                basRemotePort,
                                                                Common.ACCOUNT_CACHED_HOME))))));

                LogSupport.info(ctx, this, "Cached Account Home installed successfuly");
            }

            /* Larry: who put this here, didn't see the PUTUpdateHome already installed below.
      if(!isBas(ctx) && !isEcare(ctx))
      {
          home_ = new PTPUpdatingHome(ctx, home_);
      }
             */
            
            Home requiredFieldHome = CoreSupport.bindHome(ctx, AccountRequiredFieldConfig.class);
            requiredFieldHome = new RegistrationLicenseFilteringHome(ctx, requiredFieldHome);
            ctx.put(AccountRequiredFieldConfigHome.class, requiredFieldHome);
            
            final AccountHomePipelineFactory accountHomeFactory = AccountHomePipelineFactory.instance();
            final Home accountHome = accountHomeFactory.decorateHome((Home) ctx.get(Common.ACCOUNT_CACHED_HOME), ctx,
                    serverCtx);

            ctx.put(AccountHome.class, accountHome);

            // Install account extensions
            installAccountExtensions(ctx, serverCtx, basApp, basRemotePort);

            // new AccountStateTransitionSupport().installDefaultStateTransitions(ctx);
            // [CW] controlled by BAS.

            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                final RMIHomeServer server =
                    new RMIHomeServer(
                            serverCtx,
                            new ContextualizingHome(ctx,
                                    new SetPrincipalRmiServerHome(new PMHome(ctx, AccountHome.class.getName() + ".rmiserver", (Home)ctx.get(AccountHome.class)))),
                                    AccountHome.class.getName());
                server.register();

                //PTP
                ctx.put(AccountHome.class,
                        new PTPUpdatingHome(ctx,
                                (Home)ctx.get(AccountHome.class)));
            }
            else
            {
                // override the default home with RMIHomeClient
                // TT fix: 7030745954 Added adapter on Ecare side
                ctx.put(AccountHome.class,
                        new NoSelectAllHome(
                                new SpidAwareHome(ctx, 
                                new PTPUpdatingHome(ctx, 
                                new ContextualizingHome(ctx, 
                                        new TestSerializabilityHome(ctx, 
                                                new SetPrincipalHome(
                                                        new PMHome(ctx, AccountHome.class.getName() + ".rmiclient", 
                                                                new RMIHomeClient(ctx, basApp.getHostname(), basRemotePort, AccountHome.class.getName())))))))));

                LogSupport.info(ctx, this, "Account Home installed successfuly");
            }

            // this is for disabling Delete button for subscriber profile bean under Account
            ctx.put("com.redknee.app.crm.bean.Subscriber.delete", Boolean.FALSE);

            // [CW] clustered by all
            CoreSupport.bindBean(ctx, AppSmsbClientConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    AppSmsbClientConfig.class.getName(),
                    AppSmsbClientConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [CW] clustered by all
            CoreSupport.bindBean(ctx, ProductAbmClientConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    ProductAbmClientConfig.class.getName(),
                    ProductAbmClientConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [CW] clustered by all
            CoreSupport.bindBean(ctx, AppEcpClientConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    AppEcpClientConfig.class.getName(),
                    AppEcpClientConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [CW] clustered by all
            CoreSupport.bindBean(ctx, FFRmiServiceConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    FFRmiServiceConfig.class.getName(),
                    FFRmiServiceConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            //       For AAA
            CoreSupport.bindBean(ctx,AAARmiServiceConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    AAARmiServiceConfig.class.getName(),
                    AAARmiServiceConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));


            // For Transfer Fund
            CoreSupport.bindBean(ctx, TfaRmiConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    TfaRmiConfig.class.getName(),
                    TfaRmiConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            CoreSupport.bindBean(ctx, TFAClientConfig.class);
            
            // NGRC Subscriber Provision SOAP Client
            CoreSupport.bindBean(ctx, NGRCClientConfig.class);

            // [CW] clustered by all
            ctx.put(
            		CRMConfigInfoForVRAHome.class,
            			new RMIClusteredHome(ctx, CRMConfigInfoForVRAHome.class.getName(),
            					new SpidAwareHome(ctx, CoreSupport.bindHome(ctx, CRMConfigInfoForVRA.class))));

         // [CW] clustered by all
            CoreSupport.bindBean(ctx, PackageBatchConfigruation.class);
            new RMIClusteredMetaBean(
                    ctx,
                    PackageBatchConfigruation.class.getName(),
                    PackageBatchConfigruation.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));
            
            // [DL] local to each node. Do not cluster
            //         final Home ErPollerProcessorPackageHome = new NotifyingHome(new PollerProcessorPackageXDBHome(ctx));
            final Home ErPollerProcessorPackageHome = new NotifyingHome(CoreSupport.bindHome(ctx, PollerProcessorPackage.class));
            ErPollerProcessorPackageHome.cmd(ctx, HomeCmdEnum.AUTOINC_ENABLE);
            ctx.put(
                    PollerProcessorPackageHome.class,
                    new AuditJournalHome(ctx, ErPollerProcessorPackageHome));

            // [CW] local to each node. Do not cluster
            final Home erPollerConfigHome = new NotifyingHome(CoreSupport.bindHome(ctx, ErPollerConfig.class));
            ((NotifyingHome)erPollerConfigHome).addHomeChangeListener(new ErPollerConfigHomeChangeListener(ctx));
            ctx.put(ErPollerConfigHome.class, 
                    new SortingHome(ctx, new AuditJournalHome(ctx, new RemoveRelationshipsOnRemoveHome(ctx,
                            ErPollerConfigXInfo.PollerProcessorPackagesRelationship, erPollerConfigHome))));
            // [CW] clustered by all
            CallDetailConfig cdc = (CallDetailConfig) CoreSupport.bindBean(ctx, CallDetailConfig.class);
            ConfigChangeRequestSupportHelper.get(ctx).registerBeanForConfigSharing(ctx, cdc);
            new RMIClusteredMetaBean(
                    ctx,
                    CallDetailConfig.class.getName(),
                    CallDetailConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));
            


            ctx.put(   IPCGDataHome.class,
                    new IPCGBufferFlushingHome(
                            StorageSupportHelper.get(ctx).createHome(
                                    ctx,
                                    IPCGData.class,
                                    IPCGDataXInfo.DEFAULT_TABLE_NAME), ctx));

            ctx.put(TaxAuthorityHome.class, new TaxAuthorityHomePipelineFactory().createPipeline(ctx, serverCtx));
            
            ctx.put(TaxExemptionInclusionHome.class, new TEICHomePipelineFactory().createPipeline(ctx, serverCtx));

            //Set the Tracer.T to false to avoid the look up of the hashmap
            ctx.put(Tracer.T,False.instance());

            if (DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
            {
                ctx.put(CallDetailHome.class, new PMHome(ctx, CallDetailHome.class.getName() + ".rmiclient", new RMIHomeClient(ctx,
                        basApp.getHostname(),basRemotePort,CallDetailHome.class.getName())));

                ctx.put(Common.MT_CALL_DETAIL_HOME, new PMHome(ctx, Common.MT_CALL_DETAIL_HOME + ".rmiclient", new RMIHomeClient(ctx,
                        basApp.getHostname(), basRemotePort, Common.MT_CALL_DETAIL_HOME)));

                ctx.put(Common.PREPAID_CALLING_CARD_CALL_DETAIL_HOME, new PMHome(ctx, Common.PREPAID_CALLING_CARD_CALL_DETAIL_HOME + ".rmiclient", new RMIHomeClient(ctx,
                        basApp.getHostname(), basRemotePort, Common.PREPAID_CALLING_CARD_CALL_DETAIL_HOME)));

			ctx.put(BalanceBundleUsageSummaryHome.class, new BalanceBundleUsageSummaryHomePipelineFactory().createPipeline(ctx, serverCtx));
        
        }
            else
            {
                // BAS or single
                try
                {
                    ctx.put(CallDetailHome.class, CallDetailHomePipelineFactory.instance().createPipeline(ctx,
                            serverCtx));
                }
                catch (final Exception e)
                {
                    new MajorLogMsg(this, e.getMessage(), e).log(ctx);
                }

                RMIHomeServer server = new RMIHomeServer(serverCtx, new PMHome(ctx, CallDetailHome.class.getName() + ".rmiserver", (Home) ctx.get(CallDetailHome.class)),
                        CallDetailHome.class.getName());

                server.register();

                try
                {
                    MTCallDetailHomePipelineFactory.instance().createPipeline(ctx, serverCtx);
                }
                catch (final Exception e)
                {
                    new MajorLogMsg(this, e.getMessage(), e).log(ctx);
                }

                server = new RMIHomeServer(serverCtx, new PMHome(ctx, Common.MT_CALL_DETAIL_HOME + ".rmiserver", (Home) ctx.get(Common.MT_CALL_DETAIL_HOME)),
                        Common.MT_CALL_DETAIL_HOME);

                server.register();

                try
                {
                    PrepaidCallingCardCallDetailHomePipelineFactory.instance().createPipeline(ctx, serverCtx);
                }
                catch (final Exception e)
                {
                    new MajorLogMsg(this,"Unable to install Prepaid calling card home ", e).log(ctx);
                }

                server = new RMIHomeServer(serverCtx, new PMHome(ctx, Common.PREPAID_CALLING_CARD_CALL_DETAIL_HOME
                        + ".rmiserver", (Home) ctx.get(Common.PREPAID_CALLING_CARD_CALL_DETAIL_HOME)),
                        Common.PREPAID_CALLING_CARD_CALL_DETAIL_HOME);

                server.register();
            }

            if (DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
            {
                ctx.put(RerateCallDetailHome.class, new PMHome(ctx, RerateCallDetailHome.class.getName() + ".rmiclient", new RMIHomeClient(ctx,
                        basApp.getHostname(),basRemotePort,RerateCallDetailHome.class.getName())));
            }
            else
            {
                // BAS or single
                try
                {
                    RerateCallDetailHomePipelineFactory.instance().createPipeline(ctx, serverCtx);
                }
                catch (final Exception e)
                {
                    new MajorLogMsg(this, e.getMessage(), e).log(ctx);
                }

                final RMIHomeServer server = new RMIHomeServer(serverCtx, new PMHome(ctx, RerateCallDetailHome.class.getName() + ".rmiserver", (Home) ctx.get(RerateCallDetailHome.class)),
                        RerateCallDetailHome.class.getName());

                server.register();
            }

            new IdentificationHomePipelineFactory().createPipeline(ctx, serverCtx);

			ctx.put(SpidIdentificationGroupsHome.class,
			    new SpidIdentificationGroupsHomePipelineFactory()
			        .createPipeline(ctx, serverCtx));

            
            // [CW] clustered by all
			ctx.put(OccupationHome.class, new OccupationHomePipelineFactory()
			    .createPipeline(ctx, serverCtx));

            // [CW] clustered by all
            ctx.put(CreditCategoryHome.class, new CreditCategoryHomePipelineFactory().createPipeline(ctx, serverCtx));
            
            ctx.put(DepositTypeHome.class, new DepositTypeHomePipelineFactory().createPipeline(ctx, serverCtx));

            // Credit category ui pipeline
            ctx.put(com.redknee.app.crm.bean.ui.CreditCategoryHome.class, new CreditCategoryUIHomePipelineFactory().createPipeline(ctx,serverCtx));

            ctx.put(ChargingTemplateHome.class, new ChargingTemplateHomePipelineFactory().createPipeline(ctx,serverCtx));
            
            ctx.put(AccntGrpScreeningTempHome.class, 
            		new AccountsGroupScreeningTemplatePipelineFactory().createPipeline(ctx, serverCtx));

            ctx.put(ScreeningTemplateHome.class, new ScreeningTemplateHomePipelineFactory().createPipeline(ctx,serverCtx));

            ctx.put(GroupScreeningTemplateHome.class, new GroupScreeningTemplateHomePipelineFactory().createPipeline(ctx,serverCtx));

            ctx.put(SubscriberSegmentHome.class, new SubscriberSegmentHomePipelineFactory().createPipeline(ctx,serverCtx));
            
            ctx.put(ChargingTemplateAdjTypeHome.class, new ChargingTemplateAdjTypeHomePipelineFactory().createPipeline(ctx,serverCtx));

            ctx.put(DunningConfigHome.class, new DunningConfigHomePipelineFactory().createPipeline(ctx,serverCtx));

            ctx.put(DunningReportRecordHome.class, new DunningReportRecordHomePipelineFactory().createPipeline(ctx,serverCtx));

            ctx.put(DunningReportHome.class, new DunningReportHomePipelineFactory().createPipeline(ctx,serverCtx));

            ctx.put(DunningWaiverHome.class, new DunningWavierHomePipeline().createPipeline(ctx, serverCtx));

            ctx.put(com.redknee.app.crm.bean.ui.ChargingTemplateHome.class, new ChargingTemplateUIHomePipelineFactory().createPipeline(ctx,serverCtx));

            ctx.put(AutoDepositReleaseCriteriaHome.class, AutoDepositReleaseCriteriaHomePipelineFactory.instance()
                    .createPipeline(ctx, serverCtx));

            ctx.put(CreditLimitAdjustmentHome.class, CreditLimitAdjustmentHomePipelineFactory.getInstance()
                    .createPipeline(ctx, serverCtx));

            ctx.put(BearerTypeHome.class, BearerTypeHomePipelineFactory.instance().createPipeline(ctx, serverCtx));

            ctx.put(OverdraftBalanceLimitHome.class, OverdraftBalanceLimitHomePipelineFactory.instance().createPipeline(ctx, serverCtx));
            // [CW] clustered by all
            ctx.put(BillCycleHome.class, BillCycleHomePipelineFactory.instance().createPipeline(ctx, serverCtx));
            
            ctx.put(BillCycleHistoryHome.class, new BillCycleHistoryHomePipelineFactory().createPipeline(ctx, serverCtx));
            
            ctx.put(AccountModificationHistoryHome.class, new AccountModificationHistoryHomePipelineFactory().createPipeline(ctx,serverCtx));

            /*if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
        {
            new RMIHomeServer(serverCtx,
                      new ReadOnlyHome(
                        new PMHome(
                          serverCtx,
                          BillCycleHome.class.getName(),
                          (Home) ctx.get(BillCycleHome.class))),
                          BillCycleHome.class.getName()).register();
        }*/

            // [CW] clustered by all
            ctx.put(CallTypeHome.class, new CallTypeHomePipelineFactory().createPipeline(ctx, serverCtx));

            /*  // [CW] clustered by all
        ctx.put(PaymentNotificationMsgHome.class,
                   new RMIClusteredHome(ctx, PaymentNotificationMsgHome.class.getName(),
                CoreSupport.bindHome(ctx, PaymentNotificationMsg.class)));*/

            // [CW] clustered by all
			ctx.put(ProvinceHome.class, new ProvinceHomePipelineFactory()
			    .createPipeline(ctx, serverCtx));

            
            ctx.put(DiscountClassHome.class,
            		new AdapterHome(ctx, new com.redknee.app.crm.home.DiscountClassAdapter(),
            		new DiscountClassHomePipelineFactory().createPipeline(ctx, serverCtx)));
            
            ctx.put(DiscountGradeHome.class, new DiscountGradeHomePipelineFactory().createPipeline(ctx, serverCtx));
           
            // [CW] clustered by all
            ctx.put(
                    ProvisionCommandHome.class,
                    new PrvCmdIDSettingHome(ctx, "PrvCmdID_seq", 
                    new ProvisionCommandInAdvanceSettingHome(ctx,
                    new RMIClusteredHome(ctx, ProvisionCommandHome.class.getName(),
                            new SortingHome(
                                    new AuditJournalHome(ctx, new com.redknee.app.crm.bean.ProvisionCommandXDBHome(ctx, "ProvisionCommand")))))));

            // [sk] DIVA
            ctx.put(DIVAConfigHome.class,
                    new SortingHome(
                            new AuditJournalHome(ctx, CoreSupport.bindHome(ctx, DIVAConfig.class))));

            // [CW] direct connection to DB
			ctx.put(BlackListHome.class, new BlackListHomePipelineFactory()
			    .createPipeline(ctx, serverCtx));

            ctx.put(BlackListConfigHome.class,
                    new AuditJournalHome(ctx, CoreSupport.bindHome(ctx, BlackListConfig.class)));

			ctx.put(DebtCollectionAgencyHome.class,
			    new DebtCollectionAgencyHomePipelineFactory().createPipeline(
			        ctx, serverCtx));

            // [jhughes] remote for Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        serverCtx,
                                        BlackListHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(BlackListHome.class))),
                                        BlackListHome.class.getName()).register();

                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        serverCtx,
                                        BlackListConfigHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(BlackListConfigHome.class))),
                                        BlackListConfigHome.class.getName()).register();
            }

            // [CW] clustered by all
            // The NotifyingHome is required by PrefixServiceImpl
            final Home destZoneHome =
                new RMIClusteredHome(ctx, DestinationZoneHome.class.getName(),
                        new AuditJournalHome(ctx,
                                new NotifyingHome(CoreSupport.bindHome(ctx, DestinationZone.class))));
            ctx.put(DestinationZoneHome.class, destZoneHome);
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
            {
                // [CW] auto increment doesn't work across clustering so use IdentifierSequenceHome instead
                ctx.put(DestinationZoneHome.class,
                        new IdentifierSettingHome(
                                ctx,
                                destZoneHome,
                                IdentifierEnum.DESTINATIONZONE_ID, null));
                IdentifierSequenceSupportHelper.get(ctx).ensureNextIdIsLargeEnough(ctx, IdentifierEnum.DESTINATIONZONE_ID, destZoneHome);
            }
            else
            {
                // [CW] no need to burden the IdentifierSequenceHome in a single-node deploy
                enableAutoIncrement(ctx,DestinationZoneHome.class);
            }

            final SortingHome sortedHome=new SortingHome(ctx,
                    new SpidAwareHome(ctx,
                            new IdentifierSettingHome(ctx,
                                    new AuditJournalHome(ctx,
                                            new NotifyingHome(
                                                    new CachingHome(
                                                            ctx,
                                                            BillingOptionMapping.class,
                                                            new BillingOptionMappingTransientHome(ctx),
                                                            StorageSupportHelper.get(ctx).createHome(ctx, BillingOptionMapping.class,"BILLINGOPTIONMAPPING")))),
                                                            IdentifierEnum.BILLING_OPTION_RULE_ID, null)),new BillingOptionMappingPriorityComparator());
            sortedHome.setSortOnForEach(true);

            // [CW] direct connection to DB
            ctx.put(BillingOptionMappingHome.class,sortedHome);

            StorageSupportHelper.get(ctx).createRmiService(ctx,serverCtx,BillingOptionMappingHome.class,basApp.getHostname(),basRemotePort);

            // [CW] direct connection to DB
            ctx.put(UsageTypeHome.class,
                    new UsageTypeIDSettingHome(ctx, new SortingHome(
                            new UsageTypePreventDefaultItemDeleteHome(
                                    new WhereHome(ctx,
                                            StorageSupportHelper.get(ctx).createHome(ctx, UsageType.class,"USAGETYPE"),
                                            new EQ(UsageTypeXInfo.STATE, UsageTypeStateEnum.ACTIVE_INDEX))))));

            // [jhughes] remote for Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        serverCtx,
                                        UsageTypeHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(UsageTypeHome.class))),
                                        UsageTypeHome.class.getName()).register();
            }

            new CreateDefaultUsageTypeEntry().execute(ctx);
            enableAutoIncrement(ctx,UsageTypeHome.class);

            // [CW] direct connection to DB
            // cached on the server side
            ctx.put(MsisdnZonePrefixHome.class,
                    new IdentifierSettingHome(ctx,
                            new MsisdnZonePrefixRemovalValidatorProxyHome(
                                    new AuditJournalHome(ctx,
                                            new NotifyingHome(
                                                    new SortingHome(ctx,
                                                            new CachingHome(
                                                                    ctx,
                                                                    MsisdnZonePrefix.class,
                                                                    new MsisdnZonePrefixTransientHome(ctx),
                                                                    StorageSupportHelper.get(ctx).createHome(ctx, MsisdnZonePrefix.class,"MSISDNZONEPREFIX")))))),
                                                                    IdentifierEnum.MSISDN_PREFIX_SET_ID, null));

            /*
          new LRUCachingHome(
                           StorageSupportHelper.get(ctx).getCacheConfig(ctx, MsisdnZonePrefix.class).getSize(), true,
                           StorageSupportHelper.get(ctx).createHome(ctx, MsisdnZonePrefix.class,"MSISDNZONEPREFIX"))
             */

            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    (Home)ctx.get(MsisdnZonePrefixHome.class),
                    MsisdnZonePrefixHome.class,
                    basApp.getHostname(),
                    basRemotePort);

            // Install the MsisdnPrefix -- Create database table
            // cached completely on the server side
            ctx.put(MsisdnPrefixHome.class,
            		new NotifyingHome(
                    new MsisdnPrefixERLogHome(
                            new MsisdnPrefixValidationHome(ctx,
                                    new CachingHome(
                                            ctx,
                                            MsisdnPrefix.class,
                                            new MsisdnPrefixTransientHome(ctx),
                                            new MsisdnPrefixXDBHome(ctx))))));
            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    (Home)ctx.get(MsisdnPrefixHome.class),
                    MsisdnPrefixHome.class,
                    basApp.getHostname(),
                    basRemotePort);
            /* Create Principal Setting Homes after the RMI Service, as the Homes in the Ecare become
             * completely replaced.  This way the decorating will still work. */
            StorageSupportHelper.get(ctx).createPrincipalSettingHomes(ctx, MsisdnPrefixHome.class);

            // [CW] clustered by all
            CoreSupport.bindBean(ctx, TPSConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    TPSConfig.class.getName(),
                    TPSConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [CW] direct conn to DB
            ctx.put(
                    TPSAdjMapHome.class,
                    new SortingHome(
                            new AuditJournalHome(ctx, StorageSupportHelper.get(ctx).createHome(ctx, TPSAdjMap.class, "TPSAdjMap"))));

            new TransactionHomePipelineFactory().createPipeline(ctx, serverCtx);

            new AccountTransactionHomeFactory().createPipeline(ctx, serverCtx);

            new TransferDisputeHomePipelineFactory().createPipeline(ctx, serverCtx);

            /* installing homes for SMS and Message Support
             * 
             */
            // SMS Message Stores

            new SubMsgConfigPipelineFactory<SmsDisputeNotificationConfig, SmsDisputeNotificationConfigID>(
                    SmsDisputeNotificationConfig.class, SmsDisputeNotificationConfigID.class, "SMSDISPUTECONFIG",
                    new SmsDisputeNotificationConfigID(SmsDisputeNotificationConfig.DEFAULT_SPID,
                            SmsDisputeNotificationConfig.DEFAULT_LANGUAGE)).createPipeline(ctx, serverCtx);
            new SubMsgConfigPipelineFactory<StateNotificationMsg, StateNotificationMsgID>(StateNotificationMsg.class,
                    StateNotificationMsgID.class, "SMSSTATECONFIG", new StateNotificationMsgID(
                            StateNotificationMsg.DEFAULT_SPID, StateNotificationMsg.DEFAULT_LANGUAGE,
                            StateNotificationMsg.DEFAULT_PREVIOUSSTATE, StateNotificationMsg.DEFAULT_NEWSTATE))
                    .createPipeline(ctx, serverCtx);
            new SubMsgConfigPipelineFactory<SubServiceSuspendMsg, SubServiceSuspendMsgID>(SubServiceSuspendMsg.class,
                    SubServiceSuspendMsgID.class, "SMSSUSPENDCONFIG", new SubServiceSuspendMsgID(
                            SubServiceSuspendMsg.DEFAULT_SPID, SubServiceSuspendMsg.DEFAULT_LANGUAGE)).createPipeline(
                    ctx, serverCtx);
            new SubMsgConfigPipelineFactory<SubPreWarnMsg, SubPreWarnMsgID>(SubPreWarnMsg.class, SubPreWarnMsgID.class,
                    "SMSPREWARNCONFIG", new SubPreWarnMsgID(SubPreWarnMsg.DEFAULT_SPID, SubPreWarnMsg.DEFAULT_LANGUAGE))
                    .createPipeline(ctx, serverCtx);
            new SubMsgConfigPipelineFactory<SubProfileNotificationMsg, SubProfileNotificationMsgID>(
                    SubProfileNotificationMsg.class, SubProfileNotificationMsgID.class, "SMSPROFCHANGECONFIG",
                    new SubProfileNotificationMsgID(SubProfileNotificationMsg.DEFAULT_SPID,
                            SubProfileNotificationMsg.DEFAULT_LANGUAGE)).createPipeline(ctx, serverCtx);
            new SubMsgConfigPipelineFactory<PaymentNotificationMsg, PaymentNotificationMsgID>(
                    PaymentNotificationMsg.class, PaymentNotificationMsgID.class, "SMSPAYMENTNOTIFICATION",
                    new PaymentNotificationMsgID(PaymentNotificationMsg.DEFAULT_SPID,
                            PaymentNotificationMsg.DEFAULT_LANGUAGE, PaymentNotificationMsg.DEFAULT_GRACEDAYS))
                    .createPipeline(ctx, serverCtx);
            
            // SMS Message Views
            /*ctx.put(LangMsgSummaryConfigHome.class, new RMIClusteredHome(ctx, LangMsgSummaryConfigHome.class.getName(),
              new SortingHome(new AuditJournalHome(ctx, CoreSupport.bindHome(ctx, LangMsgSummaryConfig.class)))));
      ctx.put(SpidMsgSummaryConfigHome.class, new RMIClusteredHome(ctx, SpidMsgSummaryConfigHome.class.getName(),
              new SpidAwareHome(ctx,new SortingHome(new AuditJournalHome(ctx, CoreSupport.bindHome(ctx, SpidMsgSummaryConfig.class))))));*/

            ctx.put(SubModificationScheduleHome.class, new SubModificationSchedulePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(TransfersViewHome.class, new TransfersFilterHome(ctx, new TransferTransactionAdapterProxyHome(ctx, (Home)ctx.get(TransactionHome.class))));
            ctx.put(TransfersHome.class, StorageSupportHelper.get(ctx).createHome(ctx, Transfers.class, "TRANSFERS"));

            new RecurringChargeErrorReportPipelineFactory().createPipeline(ctx, serverCtx);

            ctx.put(PaymentPlanHome.class, new PaymentPlanHomePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(AccountStateChangeReasonHome.class, new AccountStateChangeReasonHomePipeLineFactory().createPipeline(ctx, serverCtx));
            ctx.put(AccountReasonCodeMappingHome.class,new AccountReasonCodeMappingHomePipeLineFactory().createPipeline(ctx, serverCtx));
            ctx.put(RefundAdjustmentTypeMappingHome.class, new RefundAdjustmentTypeMappingHomePipeLineFactory().createPipeline(ctx, serverCtx));
            ctx.put(FeeAndPenaltyHome.class, new FeeAndPenaltyHomePipeLineFactory().createPipeline(ctx, serverCtx));
            ctx.put(OperatorNotificationHome.class, new OperatorNotificationHomePipeLineFactory().createPipeline(ctx, serverCtx));
            ctx.put(PaymentFileAdjTypeMappingHome.class, new PaymentFileAdjTypeMappingHomePipeLineFactory().createPipeline(ctx, serverCtx));
            ctx.put(PaymentFTRecordsHome.class, new PaymentFileTrackerRecordHomePipeLineFactory().createPipeline(ctx, serverCtx));
            // [CW] clustered by all
			ctx.put(ContractHome.class, new ContractHomePipelineFactory()
			    .createPipeline(ctx, serverCtx));

            // [CW] clustered by all
            CoreSupport.bindBean(ctx, IPCGPollerConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    IPCGPollerConfig.class.getName(),
                    IPCGPollerConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));



         // clustered by all
        CoreSupport.bindBean(ctx, RerateConfig.class);
          new RMIClusteredMetaBean(
                ctx,
                RerateConfig.class.getName(),
                RerateConfig.class,
                true,
                CoreSupport.getProjectHome(ctx),
                CoreSupport.getHostname(ctx));

            // clustered by all
            CoreSupport.bindBean(ctx, RerateConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    RerateConfig.class.getName(),
                    RerateConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // clustered by all
            CoreSupport.bindBean(ctx, RerateCleanUpConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    RerateCleanUpConfig.class.getName(),
                    RerateCleanUpConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // clustered by all
            CoreSupport.bindBean(ctx, RerateAlarmConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    RerateAlarmConfig.class.getName(),
                    RerateAlarmConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));
            

            CoreSupport.bindBean(ctx, LNPBulkloadConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    LNPBulkloadConfig.class.getName(),
                    LNPBulkloadConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [CW] clustered by all
            CoreSupport.bindBean(ctx, PointOfSaleConfiguration.class);
            new RMIClusteredMetaBean(
                    ctx,
                    PointOfSaleConfiguration.class.getName(),
                    PointOfSaleConfiguration.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [CW] direct connection to DB
            ctx.put(ChargingTypeHome.class,
                    new ChargingTypeIDSettingHome(ctx, new NoSelectAllHome(
                            new AuditJournalHome(
                                    ctx,
                                    StorageSupportHelper.get(ctx).createHome(ctx, ChargingType.class, "CHARGINGTYPE")))));

            // [CW] direct connection to DB
            ctx.put(OICKMappingHome.class,
                    new SortingHome(
                            new OICKMappingUnprovisionCommandClearingHome(ctx,
                                    new AuditJournalHome(ctx,
                                            StorageSupportHelper.get(ctx).createHome(ctx, OICKMapping.class, "OICKMAPPING")))));

            // [jhughes] remote for Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new com.redknee.framework.xhome.xdb.remote.RMIRemoteXDBServer(serverCtx).register();
            }

			ctx.put(BundleProfileHome.class,
			    new BundleProfileHomePipelineFactory().createPipeline(ctx,
			        serverCtx));
			ctx.put(com.redknee.app.crm.bean.ui.BundleProfileHome.class,
			    new BundleProfileUIHomePipelineFactory().createPipeline(ctx,
			        serverCtx));

            // [CW] direct connection to DB
            ctx.put(AuxiliaryServiceHome.class, new AuxiliaryServiceHomePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(com.redknee.app.crm.bean.ui.AuxiliaryServiceHome.class, new AuxiliaryServiceUIHomePipelineFactory().createPipeline(ctx, serverCtx));

            ctx.put(CompatibilityGroupHome.class,
					StorageSupportHelper.get(ctx).createHome(ctx, CompatibilityGroup.class, "COMPATIBILITYGROUP"));
            ctx.put(SuspendedEntityHome.class, new NoSelectAllHome(new SortingHome(
                    StorageSupportHelper.get(ctx).createHome(ctx,SuspendedEntity.class,"SUSPENDEDENTITY"))));

            // [jhughes] remote for Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        serverCtx,
                                        AuxiliaryServiceHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(AuxiliaryServiceHome.class))),
                                        AuxiliaryServiceHome.class.getName()).register();
            }

            // This home is required in subscriber move->MoveSubscriberRequestServicer
            // to change the subscriber idenntifier in Subscriberauxiliaryservices to new
            // Sub-id, no decorators actions are required while moving subscriber

            final Home directSubAuxHome = new LastModifiedAwareHome(StorageSupportHelper.get(ctx).createHome(ctx,SubscriberAuxiliaryService.class, "SUBSCRIBERAUXILIARYSERVICE"));
            ctx.put(SubscriberAuxiliaryServiceXDBHome.class,directSubAuxHome);

            ctx.put(SubscriberAuxiliaryServiceHome.class, SubscriberAuxiliaryServiceHomePipelineFactory.instance()
                    .createPipeline(ctx, serverCtx));

            // [jhughes] remote for Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        serverCtx,
                                        SubscriberAuxiliaryServiceHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(SubscriberAuxiliaryServiceHome.class))),
                                        SubscriberAuxiliaryServiceHome.class.getName()).register();
            }

            ctx.put(SctAuxiliaryServiceHome.class,
                    SctAuxiliaryServiceHomePipelineFactory.instance().createPipeline(ctx, serverCtx));

            ctx.put(SctAuxiliaryBundleHome.class,
                    SctAuxiliaryBundleHomePipelineFactory.instance().createPipeline(ctx, serverCtx));

            // [CW] clustered by all
            Home subBilllingLangHome =  new RMIClusteredHome(ctx, SubBillingLanguageHome.class.getName(),
                            new SortingHome(
                                    new AuditJournalHome(ctx,
                                            CoreSupport.bindHome(ctx, SubBillingLanguage.class))));
            
                                            ctx.put(SubBillingLanguageHome.class,ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx,
                    subBilllingLangHome, SubBillingLanguage.class));

            // [CW] controlled by BAS
            new PersonalListPlanHomeFactory().createPipeline(ctx, serverCtx);

            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    (Home)ctx.get(PersonalListPlanHome.class),
                    PersonalListPlanHome.class,
                    basApp.getHostname(),
                    basRemotePort);

            // [CW] controlled by BAS
            new ClosedUserGroupHomeFactory().createPipeline(ctx, serverCtx);
            
            new ClosedUserGroupHomeFactory().createMovePipeline(ctx, serverCtx);

            ctx.put(ClosedUserGroupTemplateHome.class,
                    new ValidatingHome(
                            new NoSelectAllHome(
                                    new SpidAwareHome(ctx,
                                    new ClosedUserGroupTemplateERLogHome( ctx,
                                            new ClosedUserGroupTemplateAuxiliaryServiceHome(ctx,
                                                    new ClosedUserGroupTemplateServiceHome(ctx))))),
                                                    new ClosedUserGroupTemplateValidator()));

            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    (Home)ctx.get(ClosedUserGroupHome.class),
                    ClosedUserGroupHome.class,
                    basApp.getHostname(),
                    basRemotePort);

            new BirthdayPlanHomeFactory().createPipeline(ctx, serverCtx);
 
            new BlacklistWhitelistTemplateHomePipelineFactory().createPipeline(ctx, serverCtx);
            
            // [CW] clustered by all
            ctx.put(
                    SubscriberStateConfigHome.class,
                    new RMIClusteredHome(ctx, SubscriberStateConfigHome.class.getName(),
                            new EnumerationConfigInitializationHome(
                                    ctx,
                                    SubscriberStateConfig.class,
                                    SubscriberStateEnum.COLLECTION,
                                    new SortingHome(
                                            new AuditJournalHome(
                                                    ctx,
                                                    CoreSupport.bindHome(ctx, SubscriberStateConfig.class))))));


            // [CW] direct connection to DB
            ctx.put(ServiceActivationTemplateHome.class,
                    new SpidAwareHome(ctx,
                            new TechnologyAwareHome(ctx,new ValidatingHome(SATValidatorFactory.createSATValidator(),
                                    new ServiceActivationTemplateIdentifierSettingHome(ctx,
                                            new ServiceActivationTemplateOMCreationHome(ctx,
                                                    new ServiceActivationTemplateERCreationHome(ctx,
                                                            new ServiceActivationTemplateAuxiliaryBundleHome(ctx,
                                                                    new ServiceActivationTemplateAuxiliaryServiceHome(ctx,
                                                                            StorageSupportHelper.get(ctx).createHome(ctx,ServiceActivationTemplate.class,"SERVICEACTIVATIONTEMPLATE"))))))))));

			ctx.put(AccountCreationTemplateHome.class,
			    new AccountCreationTemplateHomePipelineFactory()
			        .createPipeline(ctx, serverCtx));

            // a home to keep the list of BANKIDs
            // [CW] clustered by all
			//ctx.put(BankHome.class,
			//    new BankHomePipelineFactory().createPipeline(ctx, serverCtx));

            // [jhughes] remote by Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        ctx,
                                        BankHome.class.getName() + ".rmiserver",
                                        (Home) ctx.get(BankHome.class))),
                                        BankHome.class.getName()).register();
            }

            ctx.put(HlrTemplateHome.class, new HlrTemplateXDBHome(ctx, "simcardhlrtempalte")); 
            ctx.put(HlrCommandTemplateHome.class, new HlrCommandTemplateXDBHome(ctx, "hlrCommnadTemplate")); 
            ctx.put(HlrProfileHome.class, new HlrProfileXDBHome(ctx, "HlrProfile"));
            HlrIDHomePipelineFactory.instance().createPipeline(ctx, serverCtx);
            
            // [CW] direct connection to DB
            ctx.put(PackageMgmtHistoryHome.class,
                    new NoSelectAllHome(
                            new SortingHome(
                                    new PackageMgmtHistoryXDBHome(ctx, "PACKAGEMGMTHISTORY"))));

            // [CW] direct connection to DB
            ctx.put(MsisdnMgmtHistoryHome.class, new MsisdnMgmtHistoryHomePipelineFactory().createPipeline(ctx, serverCtx));

            // [CW] direct connection to DB
            ctx.put(ImsiMgmtHistoryHome.class,
                    new NoSelectAllHome(
                            new SortingHome(
                                    new ImsiMgmtHistoryXDBHome(ctx, "IMSIMGMTHISTORY"))));


            ctx.put(HistoryEventHome.class,
                    HistoryEventHomePipelineFactory.instance().createPipeline(ctx, ctx));

            // cluster Currency
            final Home currencyHome = (Home)ctx.get(CurrencyHome.class);
            ctx.put(CurrencyHome.class,
                    new RMIClusteredHome(ctx, CurrencyHome.class.getName(), currencyHome));

            ctx.put(EcpSubscriberHome.class,new EcpServiceHome(ctx));

            ctx.put(SmsbSubscriberHome.class,new SmsbServiceHome(ctx));

            // [jhughes] remote by Selfcare
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                new RMIHomeServer(serverCtx,
                        new ReadOnlyHome(
                                new PMHome(
                                        ctx,
                                        com.redknee.framework.license.LicenseHome.class.getName()+".rmiserver",
                                        (Home) ctx.get(com.redknee.framework.license.LicenseHome.class))),
                                        com.redknee.framework.license.LicenseHome.class.getName()).register();
            }


            //installing SubscriberHomezone home
            //This home is used to store subscriber specific info like
            //msisdn,homezonID,x,y,priority
            ctx.put(SubscriberHomezoneHome.class,
                    new NoSelectAllHome(
                            new AuditJournalHome(ctx,
                                    new SubscriberHomezoneXDBHome(ctx,"SUBSCRIBERHOMEZONE"))));

            //direct connection to db
            //home to store the number of homezones each subscriber has subscribed to
            ctx.put(HomezoneCountHome.class,
                    new NoSelectAllHome(
                            new AuditJournalHome(ctx,
                                    new HomezoneCountXDBHome(ctx,"HOMEZONECOUNT"))));

            ctx.put(ServicePackageVersionHome.class, new ServicePackageVersionHomePipelineFactory().createPipeline(ctx, serverCtx));

            Home spHome = new ServicePackageHomePipelineFactory().createPipeline(ctx, serverCtx);
            ctx.put(ServicePackageHome.class,spHome);
                    
            IdentifierSequenceSupportHelper.get(ctx).ensureNextIdIsLargeEnough(ctx, IdentifierEnum.SERVICEPACKAGE_ID, (Home) ctx.get(ServiceHome.class));

            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                Home remoteHome = (Home)ctx.get(ServicePackageHome.class);
                remoteHome = new ReadOnlyHome(remoteHome);
                remoteHome = new PMHome(ctx, ServicePackageHome.class.getName()+".rmiserver", remoteHome);

                final RMIHomeServer server =
                    new RMIHomeServer(
                            serverCtx,
                            remoteHome,
                            ServicePackageHome.class.getName());

                server.register();
            }

            // direct conn to DB
            ctx.put(AccountAccumulatorHome.class,
                    new LastModifiedAwareHome(
                            StorageSupportHelper.get(ctx).createHome(ctx,AccountAccumulator.class,"ACCOUNTACCUMULATOR")));

            // direct conn to DB
            ctx.put(SubscriberAccumulatorHome.class,
                    new LastModifiedAwareHome(
                            StorageSupportHelper.get(ctx).createHome(ctx,SubscriberAccumulator.class,"SUBSCRIBERACCUMULATOR")));
            // direct conn to DB
            ctx.put(AccountAttachmentHome.class, new LastModifiedByAwareHome(new CreatedAwareHome(
                    new CreatedByAwareHome(new LastModifiedAwareHome(
                            new BANAwareHome(ctx, new AttachmentSettingsGenerationHome(ctx, new LRUCachingHome(ctx,
                                    AccountAttachment.class, true, StorageSupportHelper.get(ctx).createHome(ctx,
                                            AccountAttachment.class, "ACCOUNTATTACHMENT")))))))));
            //for testing
            /*ctx.put(AccountAttachmentHome.class, new LastModifiedByAwareHome(new CreatedAwareHome(
                   new CreatedByAwareHome(new LastModifiedAwareHome(new LRUCachingHome(ctx,
                           SubscriberAccumulator.class, true, (new AccountAttachmentXMLHome(ctx,"AccountAttachment.xml"))))))));*/

            ctx.put(BundleAuxiliaryServiceHome.class,
                    new NoSelectAllHome(
                            StorageSupportHelper.get(ctx).createHome(ctx, BundleAuxiliaryService.class,"BUNDLEAUXSERV")));

			ctx.put(CreditCardTypeHome.class,
			    new CreditCardTypeHomePipelineFactory().createPipeline(ctx,
			        serverCtx));

            ctx.put(CreditCardInfoHome.class, new LRUCachingHome(ctx, CreditCardInfo.class, true,
                    new AdapterHome(ctx,
                            StorageSupportHelper.get(ctx).createHome(ctx, CreditCardInfo.class,"CREDITCARDINFO"), 
                            new SpidAwareEncryptingAdapter())));

            // [DZ] clustered by all
            CoreSupport.bindBean(ctx, HLRProvisioningGateway.class);
            new RMIClusteredMetaBean(
                    ctx,
                    HLRProvisioningGateway.class.getName(),
                    HLRProvisioningGateway.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            ctx.put(OcgAdj2CrmAdjHome.class,
                    StorageSupportHelper.get(ctx).createHome(ctx, OcgAdj2CrmAdj.class, "OCGADJ2CRMADJ"));

            ctx.put(Spid2DefaultAdjHome.class,
                    StorageSupportHelper.get(ctx).createHome(ctx, Spid2DefaultAdj.class, "SPID2DEFAULTADJ"));

            //skushwaha SOAP Service
            ctx.put(SoapServicesConfig.class, new AuditJournalHome(ctx,
                    bindHome(ctx, SoapServicesConfig.class,
                            new SoapServicesConfigXMLHome(ctx, getFile(ctx,
                            "SoapServices.xml")))));

            CoreSupport.bindBean(ctx, SoapServerConfig.class);
                        
            ctx.put(ConvergedAccountSubscriberHome.class, new ConvergedAccountSubscriberTransientHome(ctx));
            //ConvergedAccountSubscriber Search- set Default search type based on User login.

            //         [st] SMSB rate plan drop down

            PRBTBulkProvisioningConfigPipeLineFactory.instance().createPipeline(ctx, serverCtx);
            ctx.put(ProvisionedRatePlansHome.class, new PMHome(ctx, ProvisionedRatePlansHome.class.getName() + ".rmiclient", new AuthenticatingRMIHomeClient(ctx, "RatePlanClient")));

            new ConvergedStateMappingFactory().createPipeline(ctx, serverCtx);

            CoreSupport.bindBean(ctx, SmsbErIndicesConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    SmsbErIndicesConfig.class.getName(),
                    SmsbErIndicesConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [jeff] Install RBT Service related Homes.
            CoreSupport.bindHome(ctx, ProvCommandBean.class);
            CoreSupport.bindHome(ctx, ServiceTemplate.class);
            ctx.put(PRBTConfigurationHome.class, CoreSupport.bindHome(ctx, PRBTConfiguration.class));
            
            new PaymentExceptionHomePipelineFactory().createPipeline(ctx, serverCtx);

            new AccountManagerHomePipelineFactory().createPipeline(ctx, serverCtx);

            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    (Home) ctx.get(PaymentExceptionHome.class),
                    PaymentExceptionHome.class,
                    basApp.getHostname(),
                    basRemotePort);


            new CrmVmPlanHomePipeLineFactory().createPipeline(ctx, serverCtx);

            CoreSupport.bindBean(ctx, ExternalProvisioningConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    ExternalProvisioningConfig.class.getName(),
                    ExternalProvisioningConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));


            new TransferExceptionHomePipelineFactory().createPipeline(ctx, serverCtx);

            ctx.put(DependencyGroupHome.class,
                    new SpidAwareHome(ctx,
                            new AdapterHome(ctx, new com.redknee.app.crm.home.DependencyGroupAdapter(),
                                    (new DependencyGroupPipelineFactory()).createPipeline(ctx, serverCtx))));
            
            ctx.put(DiscountRuleHome.class,
                            new AdapterHome(ctx, new com.redknee.app.crm.home.DiscountRuleEngineAdapter(),
                                    (new DiscountRulePipelineFactory()).createPipeline(ctx, serverCtx)));
            
            ctx.put(DiscountClassTemplateInfoHome.class, new DiscountClassTemplateInfoPipelineFactory().createPipeline(ctx, serverCtx));

            
            ctx.put(PrerequisiteGroupHome.class,
                    new SpidAwareHome(ctx,
                            new AdapterHome(ctx, new com.redknee.app.crm.home.PrerequisiteGroupAdapter(),
                                    (new PrerequisiteGroupPipelineFactory()).createPipeline(ctx, serverCtx))));

            ctx.put(PricePlanGroupHome.class,
                    new ValidatingHome(new PricePlanGroupListValidator(),
                            new SpidAwareHome(ctx,
                                    new AdapterHome(ctx, new com.redknee.app.crm.home.PricePlanGroupAdapter(),
                                            (new PricePlanGroupPipelineFactory()).createPipeline(ctx, serverCtx)))));

            new AcquiredMsisdnHomePipelineFactory().createPipeline(ctx, serverCtx);

            new TransferContractPipelineFactory().createPipeline(ctx, serverCtx);

            new ContractGroupMembershipPipelineFactory().createPipeline(ctx, serverCtx);

            /* Installation of the following pipelines are in this module install: GenericBeanBulkloadModuleInstall
             * GenericBeanBulkload, SearchableSubscriber, SearchableSubscriberAuxiliaryService */
            new GenericBeanBulkloadModuleInstall().execute(ctx);

            new XMLProvisionableTypePipelineFactory().createPipeline(ctx, serverCtx);

            new XMLProvisionableServiceTypePipelineFactory().createPipeline(ctx, serverCtx);            

            new SimcardHlrBulkloadTaskHomePipelineFactory().createPipeline(ctx, serverCtx); 
            
            new BackgroundTaskInternalPPMHomeFactory().createPipeline(ctx, serverCtx);
            
            new BackgroundTaskInternalLifeCycleAgentHomeFactory().createPipeline(ctx, serverCtx);
            
            SubscriptionContractPipeLineFactory.instance().createPipeline(ctx, serverCtx);
            SubscriptionContractTermPipeLineFactory.instance().createPipeline(ctx, serverCtx);
            ctx.put(com.redknee.app.crm.bean.ui.SubscriptionContractTermHome.class, new SubscriptionContractUIHomePipelineFactory().createPipeline(ctx, serverCtx));
            
            new SubscriberNoteHomePipelineFactory().createPipeline(ctx,serverCtx);
            new AccountNoteHomePipelineFactory().createPipeline(ctx, serverCtx);
            
            ctx.put(CoreCrmConstants.ACCOUNT_NOTE_HOME,
                    new NotesAutoPushAllowedHome(ctx, (Home) ctx.get(CoreCrmConstants.ACCOUNT_NOTE_HOME), NoteOwnerTypeEnum.ACCOUNT));
            ctx.put(CoreCrmConstants.SUBSCRIBER_NOTE_HOME,
                    new NotesAutoPushAllowedHome(ctx, (Home) ctx.get(CoreCrmConstants.SUBSCRIBER_NOTE_HOME), NoteOwnerTypeEnum.SUBSCRIPTION));
            
            ctx.put(OnDemandSequenceHome.class, OnDemandSequencePipeLineFactory.instance().createPipeline(ctx, serverCtx));
            new InfoLogMsg(this, "StorageInstall completed", null).log(ctx);


            ctx.put(Common.UNAPPLIED_TRANSACTION_HOME, new UnappliedTransactionHomePipelineFactory().createPipeline(ctx, serverCtx));
            
            ctx.put(BucketHistoryHome.class, new BucketHistoryPipelineFactory().createPipeline(ctx, serverCtx));
            
            ctx.put(CreditCardPrefixRateMapHome.class, new CreditCardPrefixRateMapHomePipelineFactory().createPipeline(ctx, serverCtx));
            
            ctx.put(CreditCardTokenHome.class, new CreditCardTokenXDBHome(ctx, PaymentGatewayIntegrationConstants.TOKEN_TABLE_NAME));
            
            ctx.put(PaymentGatewayIntegrationConstants.CREATE_TOKEN_HOME_PIPELINE_KEY , new CreateTokenHomePipelineFactory().createPipeline(ctx, serverCtx));
            
            ctx.put(TopUpScheduleHome.class, new TopUpScheduleXDBHome(ctx, PaymentGatewayIntegrationConstants.SCHEDULE_TABLE_NAME));
            
            XMLHTTPSupport.install(ctx);
            
            ctx.put(TFACorbaClientConfigHome.class, new TFACorbaClientConfigXMLHome(ctx, CoreSupport.getFile(ctx,"TFACorbaClientConfig.xml")));
            
            ctx.put(TaxAdaptersHome.class, new TaxAdaptersPipelineFactory().createPipeline(ctx, serverCtx));
            
            Home SPGServiceStateMappingConfigHome = CoreSupport.bindHome(ctx, SPGServiceStateMappingConfig.class);
            ctx.put(SPGServiceStateMappingConfigHome.class, SPGServiceStateMappingConfigHome);
			
			ctx.put(SubscriberViewHome.class, new SubscriberViewHomePipelineFactory().createPipeline(ctx, serverCtx));
			
			installGLCodePipeline(ctx, serverCtx);
			
			ctx.put(SubGLCodeNHome.class, new SubGLCodeNHomePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(SubGLCodeVersionNHome.class,
                         new SubGLCodeVersionNHomePipelineFactory().createPipeline(ctx, serverCtx));
            ctx.put(ServicePricingHome.class,
                         StorageSupportHelper.get(ctx).createHome(ctx, ServicePricing.class, "ServicePricingN"));
            ctx.put(PricingVersionHome.class,
                         new ServicePricingVersionNHomePipelineFactory().createPipeline(ctx, serverCtx));
			ctx.put(ProductHome.class, new ProductHomePipelineFactory().createPipeline(ctx, serverCtx));
			
            ctx.put(ServiceProductHome.class, new ServiceProductHomePipelineFactory().createPipeline(ctx, serverCtx));
           
            ctx.put(PackageProductHome.class, new PackageProductHomePipelineFactory().createPipeline(ctx, serverCtx));
            
            ctx.put(ResourceProductHome.class, new ResourceProductHomePipelineFactory().createPipeline(ctx, serverCtx));

            ctx.put(ProductPriceHome.class, new ProductPriceHomePipelineFactory().createPipeline(ctx, serverCtx));


			// BSS-4441 -- SERVICE PROVIDER DEFAULTS FOR PRODUCT CATALOG
						ctx.put(UnifiedCatalogSpidConfigHome.class,
								new CoreUnifiedCatalogSpidConfigHomePipelineFactory().createPipeline(ctx, serverCtx));
			ctx.put(PriceTemplateHome.class, new PriceTemplateHomePipelineFactory().createPipeline(ctx, serverCtx));

			//Install Price related Pipelines
			ctx.put(PriceHome.class, new PriceHomePipelineFactory().createPipeline(ctx, serverCtx));
			ctx.put(OneTimePriceHome.class, new OneTimePriceHomePipelineFactory().createPipeline(ctx, serverCtx));
			ctx.put(RecurringPriceHome.class, new RecurringPriceHomePipelineFactory().createPipeline(ctx, serverCtx));

			
			ctx.put(AccountOverPaymentHistoryHome.class, new AccountOverPaymentHistoryAccountNotesHome(ctx, (Home) ctx.get(AccountOverPaymentHistoryHome.class)));
			

			
			ctx.put(SubscriberCleanupViewHome.class, new SubscriberCleanupViewHomePipelineFactory().createPipeline(ctx, serverCtx));
			ctx.put(ChargedBundleInfoHome.class, new ChargedBundleInfoXDBHome(ctx, "ChargedBundleInfo"));
			ctx.put(BalanceBundleUsageSummaryHome.class, new BalanceBundleUsageSummaryHomePipelineFactory().createPipeline(ctx, serverCtx));
			ctx.put(CalldetailExtensionHome.class,new CalldetailExtensionXDBHome(ctx, "CALLDETAILEXTENSION"));
			
			ctx.put(AddressHome.class, new AddressHomePipelineFactory().createPipeline(ctx, serverCtx));
			
			//install enternal credit check pipeline
			ctx.put(ExternalCreditCheckHome.class, new ExternalCreditCheckHomePipelineFactory().createPipeline(ctx, serverCtx));
			ctx.put(DDImpactingAdjustmentTypeHome.class, new DDImpactingAdjustmentTypeHomePipelineFactory().createPipeline(ctx, serverCtx));
        }
        catch (final Throwable t)
        {
            new CritLogMsg(this, "fail to install", t).log(ctx);
            throw new AgentException("Fail to complete StorageInstall", t);
        }

        setSubscriberCategoryAndCampaignConfig(ctx);
    }

    /**
	 * Adaptation of new model for COM Integration
	 * 
	 * @param ctx
	 */
	private void installGLCodePipeline(Context ctx, Context serverCtx) {
		// Adaptation of new model for COM Integration -Start
		// checking the lincence for the Com Integration model
		final boolean isLicensed = LicensingSupportHelper.get(ctx).isLicensed(ctx,
				CoreCrmLicenseConstants.COM_INTEGRATION);
		LogSupport.info(ctx, this, "COM Integration :" + isLicensed);
		try {

			if (isLicensed) {
				LogSupport.debug(ctx, this, "COM Integration ");
				ctx.put(GLCodeMappingHome.class, new CoreGLCodeNHomePipelineFactory().createPipeline(ctx, serverCtx));
				ctx.put(GLCodeNHome.class, new GLCodeNUpdateHome(ctx,
						StorageSupportHelper.get(ctx).createHome(ctx, GLCodeN.class, "GLCodeN")));
				ctx.put(GLCodeVersionNHome.class,
						new GLCodeNVersionHomePipelineFactory().createPipeline(ctx, serverCtx));
				GLCodeAdapter.addEntryToOldGLCodeMapping(ctx);
				//ctx.put(CatalogEntityHistoryHome.class, new CatalogEntityHistoryAdapterHome(ctx, StorageSupportHelper.get(ctx).createHome(ctx, CatalogEntityHistory.class, "CatalogEntityHistory")));
				ctx.put(CatalogEntityHistoryHome.class, new CatalogEntityHistoryHomePipelineFactory().createPipeline(ctx, serverCtx));

			} else {
				LogSupport.debug(ctx, this, "Without COM Integration ");
				ctx.put(GLCodeMappingHome.class, new CoreGLCodeHomePipelineFactory().createPipeline(ctx, serverCtx));
			}
		} catch (Exception e) {
			// TODO: handle exception
			new MajorLogMsg(this, "Failure while installing GLCode Pipeline Home", e).log(ctx);
		}
		// Adaptation of new model for COM Integration -End
	}
    
    /**
     * @param ctx
     */
    private void installConfigurationStorage(final Context ctx, final Context serverCtx)
    {
        ctx.put(
                ExternalServiceTypeHome.class, ExternalServiceTypeHomePipelineFactory.getInstance().createPipeline(ctx, serverCtx));
    }



    /**
     * Sets appropriate data for both SubscriberCAtegory and Campaign Config
     *
     * @param ctx Context
     */
    private void setSubscriberCategoryAndCampaignConfig( final Context ctx)
    {
        try{
            final Home subscriberCategoryHome = (Home) ctx.get(SubscriberCategoryHome.class);
            final Home campaignConfigHome = (Home) ctx.get(CampaignConfigHome.class);
            if(subscriberCategoryHome.find(new EQ(SubscriberCategoryXInfo.CATEGORY_ID, Long.valueOf(0)))==null)
            {
                final SubscriberCategory noneSubCat = new SubscriberCategory();
                noneSubCat.setCategoryId(0);
                noneSubCat.setCategoryName("None");
                noneSubCat.setDeprecated(false);
                noneSubCat.setRank(0);
                subscriberCategoryHome.create(noneSubCat);
            }

            if(campaignConfigHome.find(new EQ(CampaignConfigXInfo.CAMPAIGN_ID, Long.valueOf(0)))==null)
            {
                final CampaignConfig noneCampaignConfig = new CampaignConfig();
                noneCampaignConfig.setCampaignId(0);
                noneCampaignConfig.setCampaignName("None");
                noneCampaignConfig.setDeprecated(false);
                campaignConfigHome.create(noneCampaignConfig);
            }
        }
        catch (final Exception e)
        {
            //e.printStackTrace();
        }

    }

    private void installAccountExtensions(final Context ctx, final Context serverCtx, final RemoteApplication basApp, final int basRemotePort) throws RemoteException
    {
        try
        {
            // TODO: Handle BAS/ECare deployments
            final Home entityInfoHome = (Home)ctx.get(EntityInfoHome.class);
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, SubscriberLimitExtension.class.getName())) != null )
            {
            	Home subscriberLimitExtensionHome =  new NoSelectAllHome(
            					new ValidatingHome(
            							new AccountExtensionInstallationHome(ctx,
            									new LRUCachingHome(
            											ctx,
            											SubscriberLimitExtension.class,
            											true,
            											StorageSupportHelper.get(ctx).createHome(ctx, SubscriberLimitExtension.class, "ACTEXTSUBSLIMIT")))));
                
            	subscriberLimitExtensionHome =  ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, subscriberLimitExtensionHome, SubscriberLimitExtension.class);
                
                ctx.put(SubscriberLimitExtensionHome.class,subscriberLimitExtensionHome);
                
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(SubscriberLimitExtensionHome.class),
                        SubscriberLimitExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, SubscriberLimitExtension.class, SubscriberLimitExtension.class.getName());
            }
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, GroupPricePlanExtension.class.getName())) != null )
            {
            	Home groupPricePlanExtensionHome =  new NoSelectAllHome(
            			new ValidatingHome(
            					new ExtensionInstallationHome(ctx,
            							new LRUCachingHome(
            									ctx,
            									GroupPricePlanExtension.class,
            									true,
            									StorageSupportHelper.get(ctx).createHome(ctx, GroupPricePlanExtension.class, "ACTEXTGROUPPRICEPLAN")))));

            	groupPricePlanExtensionHome =  ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, groupPricePlanExtensionHome, GroupPricePlanExtension.class);

            	ctx.put(GroupPricePlanExtensionHome.class,groupPricePlanExtensionHome);

            	StorageSupportHelper.get(ctx).createRmiService(
            			ctx,
            			serverCtx,
            			(Home) ctx.get(GroupPricePlanExtensionHome.class),
            			GroupPricePlanExtensionHome.class,
            			basApp.getHostname(),
            			basRemotePort);
            	ExtensionSupportHelper.get(ctx).registerExtension(ctx, GroupPricePlanExtension.class, GroupPricePlanExtension.class.getName());
            }
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, FriendsAndFamilyExtension.class.getName())) != null )
            {
                FriendsAndFamilyExtensionHomePipelineFactory.instance().createPipeline(ctx, serverCtx);

                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(FriendsAndFamilyExtensionHome.class),
                        FriendsAndFamilyExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, FriendsAndFamilyExtension.class, FriendsAndFamilyExtension.class.getName());
            }
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, PoolExtension.class.getName())) != null )
            {       
            	Home poolExtensionHome =new NoSelectAllHome(
            			new ValidatingHome(
            					new ExtensionInstallationHome(ctx,
            							new ContextualizingHome(ctx, 
            									new LRUCachingHome(
            											ctx,
            											PoolExtension.class,
            											true,
            											StorageSupportHelper.get(ctx).createHome(ctx, PoolExtension.class, "ACTEXTPOOL"))))));
            	
            	poolExtensionHome =  ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, poolExtensionHome, PoolExtension.class);
            	
            	ctx.put(PoolExtensionHome.class,poolExtensionHome);
            	StorageSupportHelper.get(ctx).createRmiService(
            			ctx,
            			serverCtx,
            			(Home) ctx.get(PoolExtensionHome.class),
            			PoolExtensionHome.class,
            			basApp.getHostname(),
            			basRemotePort);
            	ExtensionSupportHelper.get(ctx).registerExtension(ctx, PoolExtension.class, PoolExtension.class.getName());
            }
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, LoyaltyCardExtension.class.getName())) != null )
            {
                ctx.put(LoyaltyCardExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        new AccountExtensionInstallationHome(ctx,
                                                new AdapterHome(ctx, new LoyaltyCardExtensionAdapter(),
                                                        new ReadOnlyHome(ctx, 
                                                                (Home)ctx.get(LoyaltyCardHome.class)))))));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(LoyaltyCardExtensionHome.class),
                        LoyaltyCardExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, LoyaltyCardExtension.class, LoyaltyCardExtension.class.getName());
            }            
        }
        catch( final HomeException he )
        {
            new MajorLogMsg(this, "Failed to install one or more account extensions." , he).log(ctx);
        }
    }

    private void installSubscriberExtensions(final Context ctx, final Context serverCtx, final RemoteApplication basApp, final int basRemotePort) throws RemoteException
    {
        ctx.put(SubscriberAdvancedFeaturesHome.class,
                new AdapterHome(new ExtensionHandlingHome<SubscriberExtension>(
                        ctx, 
                        SubscriberExtension.class, 
                        SubscriberExtensionXInfo.SUB_ID,
                        new NullHome(ctx)), new ExtensionForeignKeyAdapter(
                                SubscriberExtensionXInfo.SUB_ID)));

        try
        {
            // TODO: Handle BAS/ECare deployments
            final Home entityInfoHome = (Home)ctx.get(EntityInfoHome.class);
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, AlcatelSSCSubscriberExtension.class.getName())) != null )
            {
                ctx.put(AlcatelSSCSubscriberExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        new ExtensionInstallationHome(ctx,
                                                new KeyValueEntryFlatteningHome(
                                                        ctx,
                                                        (Home) ctx.get(KeyValueEntryHome.class),
                                                        AlcatelSSCSubscriberExtensionXInfo.KEY_VALUE_PAIRS,
                                                        AlcatelSSCPropertyXInfo.KEY,
                                                        AlcatelSSCPropertyXInfo.VALUE)))));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(AlcatelSSCSubscriberExtensionHome.class),
                        AlcatelSSCSubscriberExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, AlcatelSSCSubscriberExtension.class, LicenseConstants.ALCATEL_LICENSE);
            }

            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, PPSMSubscriberExtension.class.getName())) != null )
            {
                ctx.put(PPSMSubscriberExtensionHome.class, new NullHome(ctx));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(PPSMSubscriberExtensionHome.class),
                        PPSMSubscriberExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, PPSMSubscriberExtension.class, LicenseConstants.PPSM_LICENSE);
            }

            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, PPSMSupporterSubExtension.class.getName())) != null )
            {
                ctx.put(PPSMSupporterChargingTemplateHome.class,
                        new NoSelectAllHome(
                                StorageSupportHelper.get(ctx).createHome(ctx, PPSMSupporterChargingTemplate.class, "PPSMSUPPORTERCHARGINGTEMPLATE")));

                ctx.put(PPSMSupporterScreenTemplateHome.class,
                        new NoSelectAllHome(
                                StorageSupportHelper.get(ctx).createHome(ctx, PPSMSupporterScreenTemplate.class, "PPSMSUPPORTERSCREENTEMPLATE")));

                ctx.put(PPSMSupporterSubExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        new ExtensionInstallationHome(ctx,
                                                new PPSMSupporterSubscriberExtensionTemplatesMappingSavingHome(ctx,
                                                new LRUCachingHome(
                                                        ctx,
                                                        PPSMSupporterSubExtension.class, 
                                                        true,
                                                        StorageSupportHelper.get(ctx).createHome(ctx, PPSMSupporterSubExtension.class, "SUBEXTPPSMSUPPORTER")))))));
                
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(PPSMSupporterSubExtensionHome.class),
                        PPSMSupporterSubExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, PPSMSupporterSubExtension.class);
            }
            
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, PPSMSupporteeSubExtension.class.getName())) != null )
            {
                // The write home must be created in advance, as the created table will be used by the custom read home to create a view.
                Home ppsmSupporteeSubExtensionWriteHome = StorageSupportHelper.get(ctx).createHome(ctx, PPSMSupporteeSubExtension.class, "SUBEXTPPSMSUPPORTEE");
                ctx.put(PPSMSupporteeSubExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        new ExtensionInstallationHome(ctx,
                                                new LRUCachingHome(
                                                        ctx,
                                                        PPSMSupporteeSubExtension.class, 
                                                        true,
                                                        new ReadWriteDelegatingHome(ctx, 
                                                        new CustomPPSMSupporteeSubExtensionXDBHome(ctx, "SUBEXTPPSMSUPPORTEEMSISDN"),
                                                        ppsmSupporteeSubExtensionWriteHome))))));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(PPSMSupporteeSubExtensionHome.class),
                        PPSMSupporteeSubExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, PPSMSupporteeSubExtension.class);
            }
            
            
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, DualBalanceSubExtension.class.getName())) != null )
            {
                ctx.put(DualBalanceSubExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        new ExtensionInstallationHome(ctx,
                                                new LRUCachingHome(
                                                        ctx,
                                                        DualBalanceSubExtension.class, 
                                                        true,                                                       
                                                        StorageSupportHelper.get(ctx).createHome(ctx, DualBalanceSubExtension.class, "SUBEXTDUALBALANCE"))))));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(DualBalanceSubExtensionHome.class),
                        DualBalanceSubExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, DualBalanceSubExtension.class, DualBalanceSubExtension.class.getName());
            }
            
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, MultiSimSubExtension.class.getName())) != null )
            {
                ctx.put(MultiSimSubExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        SubscriberMultiSimValidator.instance(),
                                        new CompoundValidator(),
                                        new ExtensionInstallationHome(ctx,
                                                new MultiSimSubExtensionAuxSvcFlatteningHome(ctx)))));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(MultiSimSubExtensionHome.class),
                        MultiSimSubExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, MultiSimSubExtension.class, CoreCrmLicenseConstants.MULTI_SIM_LICENSE);
            }

            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, OverdraftBalanceSubExtension.class.getName())) != null )
            {
                ctx.put(OverdraftBalanceSubExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        new ExtensionInstallationHome(ctx,
                                                new LRUCachingHome(
                                                        ctx,
                                                        OverdraftBalanceSubExtension.class, 
                                                        true,                                                       
                                                        StorageSupportHelper.get(ctx).createHome(ctx, OverdraftBalanceSubExtension.class, "SUBEXTOVERDRAFTBALANCE"))))));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(OverdraftBalanceSubExtensionHome.class),
                        OverdraftBalanceSubExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, OverdraftBalanceSubExtension.class, LicenseConstants.OVERDRAFT_BALANCE_LICENSE);
            }
            
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, FixedStopPricePlanSubExtension.class.getName())) != null )
            {
                Home home = StorageSupportHelper.get(ctx).createHome(ctx, FixedStopPricePlanSubExtension.class, "SUBEXTFIXEDSTOPPRICEPLAN");
                home =  ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, home, FixedStopPricePlanSubExtension.class);
                ctx.put(FixedStopPricePlanSubExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        new ExtensionInstallationHome(ctx,
                                                new LRUCachingHome(
                                                        ctx,
                                                        FixedStopPricePlanSubExtension.class, 
                                                        true,                                                       
                                                            home)))));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        home,
                        FixedStopPricePlanSubExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, FixedStopPricePlanSubExtension.class, LicenseConstants.SUBSCRIBER_FIXED_STOP_PRICEPLAN_LICENSE);
            }            
        }
        catch( final HomeException he )
        {
            new MajorLogMsg(this, "Failed to install one or more subscriber extensions." , he).log(ctx);
        }
    }

    private void installServiceExtensions(final Context ctx, final Context serverCtx, final RemoteApplication basApp, final int basRemotePort) throws RemoteException
    {
        if (ctx.has(AlcatelSSCServiceExtensionHome.class))
        {
            StorageSupportHelper.get(ctx).createRmiService(
                    ctx,
                    serverCtx,
                    new ContextRedirectingHome(ctx, AlcatelSSCServiceExtensionHome.class),
                    AlcatelSSCServiceExtensionHome.class,
                    basApp.getHostname(),
                    basRemotePort);
        }

        
        ctx.put(ExternalServiceTypeExtensionHome.class, new ExternalServiceTypeExtensionHomePipelineFactory().createPipeline(ctx, serverCtx));
        ExtensionSupportHelper.get(ctx).registerExtension(ctx, ExternalServiceTypeExtension.class);

        
        ctx.put(BlacklistWhitelistTemplateServiceExtensionHome.class, new BlacklistWhitelistTemplateServiceExtensionHomePipelineFactory().createPipeline(ctx, serverCtx));
        ExtensionSupportHelper.get(ctx).registerExtension(ctx, BlacklistWhitelistTemplateServiceExtension.class);

    }
    
    private void installAuxiliaryServiceExtensions(final Context ctx, final Context serverCtx, final RemoteApplication basApp, final int basRemotePort) throws RemoteException
    {
            // TODO: Handle BAS/ECare deployments
            final Home entityInfoHome = (Home)ctx.get(EntityInfoHome.class);

            if (ctx.has(AddMsisdnAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(AddMsisdnAuxSvcExtensionHome.class);

                ctx.put(AddMsisdnAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.AddMsisdnAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.AddMsisdnAuxSvcExtension.class), 
                                        home));

                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(AddMsisdnAuxSvcExtensionHome.class),
                        AddMsisdnAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);

                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, AddMsisdnAuxSvcExtension.class);
                
            }

            if (ctx.has(MultiSimAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(MultiSimAuxSvcExtensionHome.class);

                ctx.put(MultiSimAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.MultiSimAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.MultiSimAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.MultiSimAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.MultiSimAuxSvcExtension.class), 
                                        home));

                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(MultiSimAuxSvcExtensionHome.class),
                        MultiSimAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);

                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.MultiSimAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, MultiSimAuxSvcExtension.class);
            }

            if (ctx.has(NGRCOptInAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(NGRCOptInAuxSvcExtensionHome.class);

                ctx.put(NGRCOptInAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.NGRCOptInAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.NGRCOptInAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.NGRCOptInAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.NGRCOptInAuxSvcExtension.class), 
                                        home));

                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(NGRCOptInAuxSvcExtensionHome.class),
                        NGRCOptInAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);

                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.NGRCOptInAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, NGRCOptInAuxSvcExtension.class);
            }

            if (ctx.has(CallingGroupAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(CallingGroupAuxSvcExtensionHome.class);

                ctx.put(CallingGroupAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension.class), 
                                        home));

                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(CallingGroupAuxSvcExtensionHome.class),
                        CallingGroupAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);

                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, CallingGroupAuxSvcExtension.class);
            }
            
            if (ctx.has(DiscountAuxSvcExtensionHome.class))
            {
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(DiscountAuxSvcExtensionHome.class),
                        DiscountAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
            }

            if (ctx.has(HomeZoneAuxSvcExtensionHome.class))
            {
                Home home = null;
                Home coreHome = (Home) ctx.get(HomeZoneAuxSvcExtensionHome.class);
                while (coreHome != null && coreHome instanceof HomeProxy)
                {
                    if (coreHome instanceof ReadOnlyHome)
                    {
                        home = ((ReadOnlyHome) coreHome).getDelegate(ctx);
                        break;
                    }
                    coreHome = ((HomeProxy) coreHome).getDelegate(ctx);
                }
                
                if (home==null)
                {
                    home = (Home) ctx.get(HomeZoneAuxSvcExtensionHome.class);
                }
                
                ctx.put(HomeZoneAuxSvcExtensionHome.class,
                                new ValidatingHome(
                                        new ExtensionInstallationHome(ctx,
                                                new AdapterHome(ctx, 
                                                        new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.HomeZoneAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.HomeZoneAuxSvcExtension>(
                                                                com.redknee.app.crm.extension.auxiliaryservice.core.HomeZoneAuxSvcExtension.class, 
                                                                com.redknee.app.crm.extension.auxiliaryservice.core.custom.HomeZoneAuxSvcExtension.class), 
                                                home))));
                
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(HomeZoneAuxSvcExtensionHome.class),
                        HomeZoneAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);

                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.HomeZoneAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, HomeZoneAuxSvcExtension.class);
            }

            if (ctx.has(PRBTAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(PRBTAuxSvcExtensionHome.class);

                ctx.put(PRBTAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.PRBTAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.PRBTAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.PRBTAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.PRBTAuxSvcExtension.class), 
                                        home));

                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(PRBTAuxSvcExtensionHome.class),
                        PRBTAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);

                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.PRBTAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, PRBTAuxSvcExtension.class);
            }
            
            if (ctx.has(ProvisionableAuxSvcExtensionHome.class))
            {
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(ProvisionableAuxSvcExtensionHome.class),
                        ProvisionableAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
            }

            if (ctx.has(SPGAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(SPGAuxSvcExtensionHome.class);

                ctx.put(SPGAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.SPGAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.SPGAuxSvcExtension.class), 
                                        home));

                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(SPGAuxSvcExtensionHome.class),
                        SPGAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);

                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, SPGAuxSvcExtension.class);
            }

            if (ctx.has(VPNAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(VPNAuxSvcExtensionHome.class);

                ctx.put(VPNAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.VPNAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.VPNAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.VPNAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.VPNAuxSvcExtension.class), 
                                        home));

                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(VPNAuxSvcExtensionHome.class),
                        VPNAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);

                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.VPNAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, VPNAuxSvcExtension.class);
            }

            if (ctx.has(GroupChargingAuxSvcExtensionHome.class))
            {
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(GroupChargingAuxSvcExtensionHome.class),
                        GroupChargingAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
            }

            if (ctx.has(VoicemailAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(VoicemailAuxSvcExtensionHome.class);

                ctx.put(VoicemailAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.VoicemailAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.VoicemailAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.VoicemailAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.VoicemailAuxSvcExtension.class), 
                                        home));

                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(VoicemailAuxSvcExtensionHome.class),
                        VoicemailAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);

                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.VoicemailAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, VoicemailAuxSvcExtension.class);
            }

            if (ctx.has(URCSPromotionAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(URCSPromotionAuxSvcExtensionHome.class);

                ctx.put(URCSPromotionAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.URCSPromotionAuxSvcExtension, com.redknee.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.URCSPromotionAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension.class), 
                                        home));


                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(URCSPromotionAuxSvcExtensionHome.class),
                        URCSPromotionAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                
                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.URCSPromotionAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, URCSPromotionAuxSvcExtension.class);

            }

            if (ctx.has(com.redknee.app.crm.extension.auxiliaryservice.PromOptOutAuxSvcExtensionHome.class))
            {
                Home home = (Home) ctx.get(com.redknee.app.crm.extension.auxiliaryservice.PromOptOutAuxSvcExtensionHome.class);

                ctx.put(com.redknee.app.crm.extension.auxiliaryservice.PromOptOutAuxSvcExtensionHome.class,
                                        new AdapterHome(ctx, 
                                                new ExtendedBeanAdapter<com.redknee.app.crm.extension.auxiliaryservice.core.PromOptOutAuxSvcExtension, 
                                                com.redknee.app.crm.extension.auxiliaryservice.core.custom.PromOptOutAuxSvcExtension>(
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.PromOptOutAuxSvcExtension.class, 
                                                        com.redknee.app.crm.extension.auxiliaryservice.core.custom.PromOptOutAuxSvcExtension.class), 
                                        home));


                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(com.redknee.app.crm.extension.auxiliaryservice.PromOptOutAuxSvcExtensionHome.class),
                        com.redknee.app.crm.extension.auxiliaryservice.PromOptOutAuxSvcExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                
                ExtensionSupportHelper.get(ctx).unRegisterExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.PromOptOutAuxSvcExtension.class);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.custom.PromOptOutAuxSvcExtension.class);

            }

    }
    

    private void installSpidExtensions(final Context ctx, final Context serverCtx, final RemoteApplication basApp, final int basRemotePort) throws RemoteException
    {
        try
        {
            // TODO: Handle BAS/ECare deployments
            final Home entityInfoHome = (Home)ctx.get(EntityInfoHome.class);
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, AlcatelSSCSpidExtension.class.getName())) != null )
            {
                ctx.put(AlcatelSSCSpidExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        new ExtensionInstallationHome(ctx,
                                                new KeyValueEntryFlatteningHome(
                                                        ctx,
                                                        (Home) ctx.get(KeyValueEntryHome.class),
                                                        AlcatelSSCSpidExtensionXInfo.KEY_VALUE_PAIRS,
                                                        AlcatelSSCPropertyXInfo.KEY,
                                                        AlcatelSSCPropertyXInfo.VALUE)))));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(AlcatelSSCSpidExtensionHome.class),
                        AlcatelSSCSpidExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, AlcatelSSCSpidExtension.class, LicenseConstants.ALCATEL_LICENSE);
            }
            
            if (ctx.has(NotificationMethodSpidExtensionHome.class))
            {
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        new ContextRedirectingHome(ctx, NotificationMethodSpidExtensionHome.class),
                        NotificationMethodSpidExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
            }
            
            if (entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, DuplicateAccountDetectionSpidExtension.class.getName())) != null)
            {
            	Home home = StorageSupportHelper.get(ctx).createHome(ctx,DuplicateAccountDetectionSpidExtension.class, "DuplicateDetection");
            	
            	home = new NoSelectAllHome(home);
            	
                ctx.put(DuplicateAccountDetectionSpidExtensionHome.class, home);
                
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(DuplicateAccountDetectionSpidExtensionHome.class),
                        DuplicateAccountDetectionSpidExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, DuplicateAccountDetectionSpidExtension.class, DuplicateAccountDetectionSpidExtension.class.getName());
            }
            
            if (entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, PricePlanSwitchLimitSpidExtension.class.getName())) != null)
            {
                Home home = StorageSupportHelper.get(ctx).createHome(ctx, PricePlanSwitchLimitSpidExtension.class, "PricePlanSwitchLimit");

                
                home = new CachingHome(ctx, 
                        PricePlanSwitchLimitSpidExtension.class,
                        new PricePlanSwitchLimitSpidExtensionTransientHome(ctx),
                        home);
                home = new NoSelectAllHome(home);
                
                ctx.put(PricePlanSwitchLimitSpidExtensionHome.class, home);
                
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(PricePlanSwitchLimitSpidExtensionHome.class),
                        PricePlanSwitchLimitSpidExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, PricePlanSwitchLimitSpidExtension.class, PricePlanSwitchLimitSpidExtension.class.getName());
            }
            
            if (entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, MinimumAgeLimitSpidExtension.class.getName())) != null)
            {
				Home home =
				    StorageSupportHelper.get(ctx).createHome(ctx,
				        MinimumAgeLimitSpidExtension.class,
				        "OldMinimumAgeLimit");

            	home = new NoSelectAllHome(home);
            	
                ctx.put(MinimumAgeLimitSpidExtensionHome.class, home);
                
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(MinimumAgeLimitSpidExtensionHome.class),
				    MinimumAgeLimitSpidExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
				ExtensionSupportHelper.get(ctx).registerExtension(ctx,
				    MinimumAgeLimitSpidExtension.class,
				    MinimumAgeLimitSpidExtension.class.getName());

            }
            
            if (entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, TaxAdaptersSpidExtension.class.getName())) != null)
            {
                Home home = StorageSupportHelper.get(ctx).createHome(ctx, TaxAdaptersSpidExtension.class, "TaxAdaptersSpidExtension"); 
                ctx.put(TaxAdaptersSpidExtensionHome.class, home);
                
                ExtensionSupportHelper.get(ctx).registerExtension(ctx,
                        TaxAdaptersSpidExtension.class, TaxAdaptersSpidExtension.class.getName()
                        );
            }

			if (entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME,
			    MinimumAgeLimitV2SpidExtension.class.getName())) != null)
			{
				Home home =
				    StorageSupportHelper.get(ctx).createHome(ctx,
				        MinimumAgeLimitV2SpidExtension.class,
				            "MinimumAgeLimit");

				home = new NoSelectAllHome(home);

				ctx.put(MinimumAgeLimitV2SpidExtensionHome.class, home);

				StorageSupportHelper.get(ctx).createRmiService(ctx, serverCtx,
				    (Home) ctx.get(MinimumAgeLimitV2SpidExtensionHome.class),
				    MinimumAgeLimitV2SpidExtensionHome.class,
				    basApp.getHostname(), basRemotePort);
				ExtensionSupportHelper.get(ctx).registerExtension(ctx,
				    MinimumAgeLimitV2SpidExtension.class,
				    MinimumAgeLimitV2SpidExtension.class.getName());

			}

            if (entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, BANGenerationSpidExtension.class.getName())) != null)
            {
                Home home = StorageSupportHelper.get(ctx).createHome(ctx, BANGenerationSpidExtension.class, "CUSTOMBANGENERATOR");
                home = new TotalCachingHome(ctx, new BANGenerationSpidExtensionTransientHome(ctx),home);
                
                ctx.put(BANGenerationSpidExtensionHome.class, home);
                
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(BANGenerationSpidExtensionHome.class),
                        BANGenerationSpidExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx,
                        BANGenerationSpidExtension.class,
                    BANGenerationSpidExtension.class.getName());

            }

            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, OverdraftBalanceSpidExtension.class.getName())) != null )
            {
                Home home = new TotalCachingHome(ctx, new OverdraftBalanceSpidExtensionTransientHome(ctx),
                                        StorageSupportHelper.get(ctx).createHome(ctx, OverdraftBalanceSpidExtension.class, "SPIDEXTOVERDRAFTBALANCE"));
                ctx.put(OverdraftBalanceSpidExtensionHome.class, home);
                
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(OverdraftBalanceSpidExtensionHome.class),
                        OverdraftBalanceSpidExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, OverdraftBalanceSpidExtension.class, LicenseConstants.OVERDRAFT_BALANCE_LICENSE);
            }

            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, CreditCardTopUpTypeSpidExtension.class.getName())) != null )
            {
                Home home = new TotalCachingHome(ctx, new CreditCardTopUpTypeSpidExtensionTransientHome(ctx),
                                        StorageSupportHelper.get(ctx).createHome(ctx, CreditCardTopUpTypeSpidExtension.class, "SPIDCCTOPUPTYPE"));
                ctx.put(CreditCardTopUpTypeSpidExtensionHome.class, home);
                
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(CreditCardTopUpTypeSpidExtensionHome.class),
                        CreditCardTopUpTypeSpidExtension.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, CreditCardTopUpTypeSpidExtension.class);
            }    
            if (entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, MsisdnSwapLimitSpidExtension.class.getName())) != null)
    	    {
    	    	Home home = new TotalCachingHome(ctx, new MsisdnSwapLimitSpidExtensionTransientHome(ctx),
    	    			StorageSupportHelper.get(ctx).createHome(ctx, MsisdnSwapLimitSpidExtension.class, "SPIDEXTMSISDNSWPLIMIT"));
    	    	ctx.put(MsisdnSwapLimitSpidExtensionHome.class, home);

    	    	StorageSupportHelper.get(ctx).createRmiService(
    	    			ctx,
    	    			serverCtx,
    	    			(Home) ctx.get(MsisdnSwapLimitSpidExtensionHome.class),
    	    			MsisdnSwapLimitSpidExtension.class,
    	    			basApp.getHostname(),
    	    			basRemotePort);
    	    	ExtensionSupportHelper.get(ctx).registerExtension(ctx, MsisdnSwapLimitSpidExtension.class, LicenseConstants.MSISDN_SWAP_LIMIT_SPID_EXTENSION);

    	    }
            
        }
        catch( final HomeException he )
        {
            new MajorLogMsg(this, "Failed to install one or more spid extensions." , he).log(ctx);
        }
    }

	private void installTechnicalServiceTemplateExtensions(Context ctx,
			Context serverCtx, RemoteApplication basApp, int basRemotePort) {
		
		try{
			Home entityInfoHome = (Home)ctx.get(EntityInfoHome.class);
			if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, com.redknee.app.crm.extension.service.BlackberryTServiceExtension.class.getName())) != null )
	        {
	            ctx.put(com.redknee.app.crm.extension.service.BlackberryTServiceExtensionHome.class, new com.redknee.app.crm.bean.pipeline.BlackberryServiceExtensionHomePipelineFactory().createPipeline(ctx, serverCtx));
	            ExtensionSupportHelper.get(ctx).registerExtension(ctx, com.redknee.app.crm.extension.service.BlackberryTServiceExtension.class, CoreCrmLicenseConstants.BLACKBERRY_LICENSE);
	            if(LogSupport.isDebugEnabled(ctx))
	            {
	                new DebugLogMsg(this,"TechnicalServiceTemplateExtensions installed").log(ctx);
	            }
	        } 
		}catch (final Exception he) {
			new MajorLogMsg(this, "Failed to install technical service extensions.", he).log(ctx);
		}
	}
    
    private void installUserGroupExtensions(final Context ctx, final Context serverCtx, final RemoteApplication basApp, final int basRemotePort) throws RemoteException
    {
        try
        {
            // TODO: Handle BAS/ECare deployments
            final Home entityInfoHome = (Home)ctx.get(EntityInfoHome.class);
            if( entityInfoHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, AdjustmentTypeLimitUserGroupExtension.class.getName())) != null )
            {
                ctx.put(AdjustmentTypeLimitUserGroupExtensionHome.class,
                        new NoSelectAllHome(
                                new ValidatingHome(
                                        new ExtensionInstallationHome(ctx,
                                                new KeyValueEntryFlatteningHome(
                                                        ctx,
                                                        (Home) ctx.get(KeyValueEntryHome.class),
                                                        AdjustmentTypeLimitUserGroupExtensionXInfo.LIMITS,
                                                        AdjustmentTypeLimitPropertyXInfo.ADJUSTMENT_TYPE,
                                                        AdjustmentTypeLimitPropertyXInfo.LIMIT)))));
                StorageSupportHelper.get(ctx).createRmiService(
                        ctx,
                        serverCtx,
                        (Home) ctx.get(AdjustmentTypeLimitUserGroupExtensionHome.class),
                        AdjustmentTypeLimitUserGroupExtensionHome.class,
                        basApp.getHostname(),
                        basRemotePort);
                ExtensionSupportHelper.get(ctx).registerExtension(ctx, AdjustmentTypeLimitUserGroupExtension.class, AdjustmentTypeLimitUserGroupExtension.class.getName());
            }
        }
        catch( final HomeException he )
        {
            new MajorLogMsg(this, "Failed to install one or more user group extensions." , he).log(ctx);
        }
    }

    private void installAdjustmentTypeEnhancedGUI(final Context ctx, final Context serverCtx)
    {
        ctx.put(AdjustmentTypeEnhancedGUIHome.class,
                new AdapterHome((Home) ctx.get(CRMGroupHome.class),
                        new AdjustmentTypeEnhancedGUIUserGroupAdapter()));
    }
    
    
    /**
     * Enables autoincrementing in transient homes.
     *
     * @param ctx registry
     * @param home the home to enable
     */
    private void enableAutoIncrement(final Context ctx,final Home home)
    {
        try
        {
            home.cmd(ctx,HomeCmdEnum.AUTOINC_ENABLE);
        }
        catch (final HomeException e)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,e.getMessage(),e).log(ctx);
            }
        }
    }

    /**
     * Enables autoincrementing in transient homes
     *
     * @param ctx registry
     * @param key key to the home to enable
     */
    private void enableAutoIncrement(final Context ctx,final Class key)
    {
        AutoIncrementSupport.enableAutoIncrement(ctx,(Home)ctx.get(key));
    }

    public static final String RMI_SERVER_CTX_KEY = com.redknee.app.crm.core.agent.StorageInstall.RMI_SERVER_CTX_KEY;
}
