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

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountHistory;
import com.trilogy.app.crm.bean.AccountUsage;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AlcatelSSCProperty;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.BlackList;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.EarlyRewardConfiguration;
import com.trilogy.app.crm.bean.EarlyRewardExtensionProperty;
import com.trilogy.app.crm.bean.ExternalServiceType;
import com.trilogy.app.crm.bean.ExternalServiceTypeWebControl;
import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.LateFeeConfiguration;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.OICKMapping;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.SpidDirectDebitConfig;
import com.trilogy.app.crm.bean.SubBulkCreate;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.bank.Bank;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionIdentificationResult;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResult;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionSubscriptionResult;
import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.app.crm.bean.ui.AuxiliaryService;
import com.trilogy.app.crm.bean.ui.BundleProfile;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.bean.webcontrol.BundleProfileViewCustomizationWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMAccountCategoryWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMAccountHistoryWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMAccountUsageWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMAdjustmentTypeWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMAlcatelSSCPropertyTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMAlcatelSSCPropertyWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMAlcatelSSCServiceExtensionTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMAlcatelSSCServiceExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMBankWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMBlacklistWhitelistTemplateServiceExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMCallingGroupAuxSvcExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMCreditCategoryExtensionHolderTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMCreditCategoryExtensionHolderWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMDirectDebitRecordTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMDirectDebitRecordWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMDiscountAuxSvcExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMEarlyRewardCreditCategoryExtensionTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMEarlyRewardCreditCategoryExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMEarlyRewardExtensionPropertyTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMEarlyRewardExtensionPropertyWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMGroupChargingAuxSvcExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMKeyConfigurationWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMLateFeeCreditCategoryExtensionTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMLateFeeCreditCategoryExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMMsisdnBulkWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMMsisdnGroupTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMMsisdnGroupWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMMsisdnTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMMsisdnWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMPRBTAuxSvcExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMPricePlanWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMProvisionCommandTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMProvisionCommandWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMProvisionableAuxSvcExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMSPGAuxSvcExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMServiceExtensionHolderTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMServiceExtensionHolderWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMSpidDirectDebitConfigTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMSpidDirectDebitConfigWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMTransactionTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMTransactionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMUIMsisdnTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMUIMsisdnWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMURCSPromotionAuxSvcExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMURCSPromotionServiceExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMVPNAuxSvcExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CRMVoicemailAuxSvcExtensionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CustomBundleCategoryAssociationTableWebControl;
import com.trilogy.app.crm.bean.webcontrol.CustomBundleCategoryAssociationWebControl;
import com.trilogy.app.crm.bean.webcontrol.CustomBundleProfileWebControl;
import com.trilogy.app.crm.bean.webcontrol.CustomCallDetailWebControl;
import com.trilogy.app.crm.bean.webcontrol.CustomEarlyRewardConfigurationWebControl;
import com.trilogy.app.crm.bean.webcontrol.CustomLateFeeConfigurationWebControl;
import com.trilogy.app.crm.bean.webcontrol.CustomPricePlanVersionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CustomServicePackageVersionWebControl;
import com.trilogy.app.crm.bean.webcontrol.CustomServicePackageWebControl;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkloadHome;
import com.trilogy.app.crm.bundle.BundleCategoryAssociation;
import com.trilogy.app.crm.bundle.rateplan.AssociatedBundlesProxyWebControl;
import com.trilogy.app.crm.bundle.support.CRMBundleSupport;
import com.trilogy.app.crm.bundle.web.BundleProfileWebControlProxy;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.MoveAccountExtensionHolder;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.PoolExtensionViewCustomizationWebControl;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtensionHolder;
import com.trilogy.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.DiscountAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.GroupChargingAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.PRBTAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.ProvisionableAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.SPGAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.VPNAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.VoicemailAuxSvcExtension;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtensionHolder;
import com.trilogy.app.crm.extension.creditcategory.EarlyRewardCreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.LateFeeCreditCategoryExtension;
import com.trilogy.app.crm.extension.service.AlcatelSSCServiceExtension;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtension;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtensionWebControl;
import com.trilogy.app.crm.extension.service.ServiceExtensionHolder;
import com.trilogy.app.crm.extension.service.URCSPromotionServiceExtension;
import com.trilogy.app.crm.extension.spid.SpidExtension;
import com.trilogy.app.crm.extension.spid.SpidExtensionHolder;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionHolder;
import com.trilogy.app.crm.extension.usergroup.UserGroupExtension;
import com.trilogy.app.crm.extension.usergroup.UserGroupExtensionHolder;
import com.trilogy.app.crm.home.bulkload.MsisdnGenericBeanBulkloadHomeProxy;
import com.trilogy.app.crm.listener.AccountSpidSwitchFromWebListener;
import com.trilogy.app.crm.listener.BundleProfileSwitchFromWebListener;
import com.trilogy.app.crm.listener.ServiceSpidSwitchFromWebListener;
import com.trilogy.app.crm.log.CustomDunningActionERERSupport;
import com.trilogy.app.crm.log.CustomSubscriptionActivationERERSupport;
import com.trilogy.app.crm.log.CustomSubscriptionModificationERERSupport;
import com.trilogy.app.crm.log.DunningActionER;
import com.trilogy.app.crm.log.SubscriptionActivationER;
import com.trilogy.app.crm.log.SubscriptionModificationER;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.numbermgn.NumberMgmtHistory;
import com.trilogy.app.crm.numbermgn.PackageMgmtHistory;
import com.trilogy.app.crm.resource.SubscriptionCardAutoSelectWebControl;
import com.trilogy.app.crm.resource.SubscriptionResourceAutoSelectWebControl;
import com.trilogy.app.crm.subscriber.provision.PricePlanSwitchFromWebListener;
import com.trilogy.app.crm.support.AdjustmentTypeSupport;
import com.trilogy.app.crm.support.BeanLoaderSupport;
import com.trilogy.app.crm.support.BundleSupport;
import com.trilogy.app.crm.support.CRMAdjustmentTypeSupport;
import com.trilogy.app.crm.support.CRMBeanLoaderSupport;
import com.trilogy.app.crm.support.CRMChargingCycleSupport;
import com.trilogy.app.crm.support.CRMEmailKeywordConfigurationSupport;
import com.trilogy.app.crm.support.CRMExternalAppSupport;
import com.trilogy.app.crm.support.CRMKeyValueSupport;
import com.trilogy.app.crm.support.CRMServicePeriodSupport;
import com.trilogy.app.crm.support.CRMTaxAuthoritySupport;
import com.trilogy.app.crm.support.ChargingCycleSupport;
import com.trilogy.app.crm.support.EmailKeywordConfigurationSupport;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.KeyValueSupport;
import com.trilogy.app.crm.support.ServicePeriodSupport;
import com.trilogy.app.crm.support.SupportHelper;
import com.trilogy.app.crm.support.TaxAuthoritySupport;
import com.trilogy.app.crm.technology.SetTechnologyProxyWebControl;
import com.trilogy.app.crm.web.control.AccountActCustomWebControl;
import com.trilogy.app.crm.web.control.AccountCopyWebControl;
import com.trilogy.app.crm.web.control.AccountIdentificationGroupCustomWebControl;
import com.trilogy.app.crm.web.control.AccountViewCustomizationWebControl;
import com.trilogy.app.crm.web.control.AccountWebControlProxy;
import com.trilogy.app.crm.web.control.CRMBillCycleHistoryWebControl;
import com.trilogy.app.crm.web.control.ConvergeSubscriberWebControl;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;
import com.trilogy.app.crm.web.control.CustomBlackListWebControl;
import com.trilogy.app.crm.web.control.CustomClosedUserGroupWebControl;
import com.trilogy.app.crm.web.control.CustomDuplicateAccountDetectionIdentificationResultTableWebControl;
import com.trilogy.app.crm.web.control.CustomDuplicateAccountDetectionResultTableWebControl;
import com.trilogy.app.crm.web.control.CustomDuplicateAccountDetectionSubscriptionResultTableWebControl;
import com.trilogy.app.crm.web.control.CustomOICKMappingTableWebControl;
import com.trilogy.app.crm.web.control.DiscountClassCustomWebControl;
import com.trilogy.app.crm.web.control.ExtensionDescriptionWebControl;
import com.trilogy.app.crm.web.control.FacetRedirectingTableWebControl;
import com.trilogy.app.crm.web.control.FacetRedirectingWebControl;
import com.trilogy.app.crm.web.control.NotifyAfterFromWebWebControl;
import com.trilogy.app.crm.web.control.PricePlanWebControlProxy;
import com.trilogy.app.crm.web.control.SubBulkCreateWebControlEx;
import com.trilogy.app.crm.web.control.SubscriberSatWebControl;
import com.trilogy.app.crm.web.control.SubscriberViewCustomizationWebControl;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.support.SchemeSupport;
import com.trilogy.framework.xhome.support.XMLSupport;
import com.trilogy.framework.xhome.webcontrol.TableWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.er.ERSupport;

/**
 * Install Facets for XBeans.
 * @author joe.chen@redknee.com
 */
public class FacetInstall extends CoreSupport implements ContextAgent
{

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        SupportHelper.register(ctx, AdjustmentTypeSupport.class, CRMAdjustmentTypeSupport.instance());
        SupportHelper.register(ctx, BeanLoaderSupport.class, CRMBeanLoaderSupport.instance());
        SupportHelper.register(ctx, KeyValueSupport.class, CRMKeyValueSupport.instance());
        SupportHelper.register(ctx, ChargingCycleSupport.class, CRMChargingCycleSupport.instance());
        SupportHelper.register(ctx, ServicePeriodSupport.class, CRMServicePeriodSupport.instance());
        SupportHelper.register(ctx, ExternalAppSupport.class, CRMExternalAppSupport.instance());
        SupportHelper.register(ctx, EmailKeywordConfigurationSupport.class, CRMEmailKeywordConfigurationSupport.instance());
        SupportHelper.register(ctx, BundleSupport.class, CRMBundleSupport.instance());
        SupportHelper.register(ctx, TaxAuthoritySupport.class, CRMTaxAuthoritySupport.instance());
        
        final FacetMgr fMgr = (FacetMgr) ctx.get(FacetMgr.class);

        // Register custom ERSupport classes
        fMgr.register(ctx, SubscriptionActivationER.class, ERSupport.class, CustomSubscriptionActivationERERSupport.class);
        fMgr.register(ctx, SubscriptionModificationER.class, ERSupport.class, CustomSubscriptionModificationERERSupport.class);
        fMgr.register(ctx, DunningActionER.class, ERSupport.class, CustomDunningActionERERSupport.class);
        
        // Register MoveRequest facets
        fMgr.register(ctx, Account.class, MoveRequest.class, AccountMoveRequest.class);
        fMgr.register(ctx, AccountExtension.class, MoveRequest.class, AccountExtensionMoveRequest.class);
        fMgr.register(ctx, Subscriber.class, MoveRequest.class, SubscriptionMoveRequest.class);


        // Register Extension Holder facets
        fMgr.register(ctx, AccountExtension.class, ExtensionHolder.class, AccountExtensionHolder.class);
        fMgr.register(ctx, SubscriberExtension.class, ExtensionHolder.class, SubscriberExtensionHolder.class);
        fMgr.register(ctx, SpidExtension.class, ExtensionHolder.class, SpidExtensionHolder.class);
        fMgr.register(ctx, UserGroupExtension.class, ExtensionHolder.class, UserGroupExtensionHolder.class);
        fMgr.register(ctx, AuxiliaryServiceExtension.class, ExtensionHolder.class, AuxiliaryServiceExtensionHolder.class);
        
        fMgr.register(ctx, ClosedUserGroup.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        new CustomClosedUserGroupWebControl()));
        
        fMgr.register(ctx, OICKMapping.class, TableWebControl.class, new CustomOICKMappingTableWebControl());
        
        fMgr.register(ctx, PackageMgmtHistory.class, WebControl.class,new FacetRedirectingWebControl(ctx, NumberMgmtHistory.class));
        fMgr.register(ctx, PackageMgmtHistory.class, TableWebControl.class,new FacetRedirectingTableWebControl(ctx, NumberMgmtHistory.class));

        fMgr.register(ctx, Bank.class, WebControl.class, new  CRMBankWebControl());


        // Register custom web controls for UI beans
        fMgr.register(ctx, com.redknee.app.crm.bean.ui.Msisdn.class, WebControl.class, 
                new CRMUIMsisdnWebControl(ctx));
        fMgr.register(ctx, com.redknee.app.crm.bean.ui.Msisdn.class, TableWebControl.class, new CRMUIMsisdnTableWebControl(ctx));
        fMgr.register(ctx, com.redknee.app.crm.bean.MsisdnBulk.class, WebControl.class, 
                new CRMMsisdnBulkWebControl(ctx));
        
        // Register custom web controls to override those provided by data models in ModelAppCrm
        fMgr.register(ctx, AccountUsage.class, WebControl.class, new CRMAccountUsageWebControl());
        fMgr.register(ctx, AdjustmentType.class, WebControl.class, new CRMAdjustmentTypeWebControl());
        fMgr.register(ctx, AccountCategory.class, WebControl.class, new CRMAccountCategoryWebControl());
        fMgr.register(ctx, AlcatelSSCProperty.class, WebControl.class, new CRMAlcatelSSCPropertyWebControl());
        fMgr.register(ctx, AlcatelSSCServiceExtension.class, WebControl.class, new CRMAlcatelSSCServiceExtensionWebControl());
        fMgr.register(ctx, KeyConfiguration.class, WebControl.class, new CRMKeyConfigurationWebControl());
        fMgr.register(ctx, Msisdn.class, WebControl.class, new CRMMsisdnWebControl());
        fMgr.register(ctx, MsisdnGroup.class, WebControl.class, new CRMMsisdnGroupWebControl());        
        fMgr.register(ctx, ProvisionCommand.class, WebControl.class, new CRMProvisionCommandWebControl());
        fMgr.register(ctx, PricePlan.class, WebControl.class, 
                new PricePlanWebControlProxy(
                        new CurrencyContextSetupWebControl(
                                (WebControl) XBeans.getInstanceOf(ctx, PricePlan.class, WebControl.class, new CRMPricePlanWebControl()))));
        fMgr.register(ctx, PricePlanVersion.class, WebControl.class, 
                new AssociatedBundlesProxyWebControl(
                        (WebControl) XBeans.getInstanceOf(ctx, PricePlanVersion.class, WebControl.class, new CustomPricePlanVersionWebControl())));
        fMgr.register(ctx, ServiceExtensionHolder.class, WebControl.class, new CRMServiceExtensionHolderWebControl());
        fMgr.register(ctx, ServicePackage.class, WebControl.class, 
                new com.redknee.app.crm.web.control.ServicePackageTotalChargeWebControl(
                        new CurrencyContextSetupWebControl(
                                (WebControl) XBeans.getInstanceOf(ctx, ServicePackage.class, WebControl.class, new CustomServicePackageWebControl()))));
        fMgr.register(ctx, ServicePackageVersion.class, WebControl.class, 
                new com.redknee.app.crm.bundle.rateplan.FilterBundlesOnTypeProxyWebControl(
                        (WebControl) XBeans.getInstanceOf(ctx, ServicePackageVersion.class, WebControl.class, new CustomServicePackageVersionWebControl())));
        fMgr.register(ctx, Transaction.class, WebControl.class, new CRMTransactionWebControl());
        fMgr.register(ctx, EarlyRewardCreditCategoryExtension.class, WebControl.class, new CRMEarlyRewardCreditCategoryExtensionWebControl());
        fMgr.register(ctx, com.redknee.app.crm.extension.creditcategory.core.EarlyRewardCreditCategoryExtension.class, WebControl.class, new CRMEarlyRewardCreditCategoryExtensionWebControl());
        fMgr.register(ctx, EarlyRewardExtensionProperty.class, WebControl.class, new CRMEarlyRewardExtensionPropertyWebControl());
        fMgr.register(ctx, LateFeeCreditCategoryExtension.class, WebControl.class, new CRMLateFeeCreditCategoryExtensionWebControl());
        fMgr.register(ctx, com.redknee.app.crm.extension.creditcategory.core.LateFeeCreditCategoryExtension.class, WebControl.class, new CRMLateFeeCreditCategoryExtensionWebControl());
        fMgr.register(ctx, CreditCategoryExtensionHolder.class, WebControl.class, new CRMCreditCategoryExtensionHolderWebControl());

        fMgr.register(ctx, SpidDirectDebitConfig.class, WebControl.class, new CRMSpidDirectDebitConfigWebControl());
        fMgr.register(ctx, SpidDirectDebitConfig.class, TableWebControl.class, new CRMSpidDirectDebitConfigTableWebControl());
        
        fMgr.register(ctx, DirectDebitRecord.class, WebControl.class, new CRMDirectDebitRecordWebControl());
        fMgr.register(ctx, DirectDebitRecord.class, TableWebControl.class, new CRMDirectDebitRecordTableWebControl());

        fMgr.register(ctx, AccountHistory.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        new CRMAccountHistoryWebControl()));

		fMgr.register(ctx, BillCycleHistory.class, WebControl.class,
		    new CRMBillCycleHistoryWebControl());
        
		
        fMgr.register(ctx, DiscountAuxSvcExtension.class, WebControl.class, new CRMDiscountAuxSvcExtensionWebControl());
        fMgr.register(ctx, CallingGroupAuxSvcExtension.class, WebControl.class, new CRMCallingGroupAuxSvcExtensionWebControl());
        fMgr.register(ctx, PRBTAuxSvcExtension.class, WebControl.class, new CRMPRBTAuxSvcExtensionWebControl());
        fMgr.register(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.custom.PRBTAuxSvcExtension.class, WebControl.class, new CRMPRBTAuxSvcExtensionWebControl());
        fMgr.register(ctx, ProvisionableAuxSvcExtension.class, WebControl.class, new CRMProvisionableAuxSvcExtensionWebControl());
        fMgr.register(ctx, URCSPromotionAuxSvcExtension.class, WebControl.class, new CRMURCSPromotionAuxSvcExtensionWebControl());
        fMgr.register(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension.class, WebControl.class, new CRMURCSPromotionAuxSvcExtensionWebControl());
        fMgr.register(ctx, URCSPromotionServiceExtension.class, WebControl.class, new CRMURCSPromotionServiceExtensionWebControl());
        fMgr.register(ctx, com.redknee.app.crm.extension.service.core.URCSPromotionServiceExtension.class, WebControl.class, new CRMURCSPromotionServiceExtensionWebControl());
        fMgr.register(ctx, BlacklistWhitelistTemplateServiceExtension.class, WebControl.class, new CRMBlacklistWhitelistTemplateServiceExtensionWebControl());


        fMgr.register(ctx, VoicemailAuxSvcExtension.class, WebControl.class, new CRMVoicemailAuxSvcExtensionWebControl());
        fMgr.register(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.custom.VoicemailAuxSvcExtension.class, WebControl.class, new CRMVoicemailAuxSvcExtensionWebControl());
        fMgr.register(ctx, SPGAuxSvcExtension.class, WebControl.class, new CRMSPGAuxSvcExtensionWebControl());
        fMgr.register(ctx, VPNAuxSvcExtension.class, WebControl.class, new CRMVPNAuxSvcExtensionWebControl());
        fMgr.register(ctx, com.redknee.app.crm.extension.auxiliaryservice.core.custom.VPNAuxSvcExtension.class, WebControl.class, new CRMVPNAuxSvcExtensionWebControl());
        fMgr.register(ctx, GroupChargingAuxSvcExtension.class, WebControl.class, new CRMGroupChargingAuxSvcExtensionWebControl());
        fMgr.register(ctx, ExternalServiceTypeExtension.class, WebControl.class, new ExternalServiceTypeExtensionWebControl());
        fMgr.register(ctx, ExternalServiceType.class, WebControl.class, new ExternalServiceTypeWebControl());
        
        fMgr.register(ctx, BlackList.class, WebControl.class, new CustomBlackListWebControl());
        
        // the following is overridden in app.crm.troubleticket FacetInstall
        fMgr.register(ctx, CRMSpid.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        (WebControl) XBeans.getInstanceOf(ctx, CRMSpid.class, WebControl.class)));
        fMgr.register(ctx, PersonalListPlan.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        (WebControl) XBeans.getInstanceOf(ctx, PersonalListPlan.class, WebControl.class)));

        fMgr.register(ctx, AuxiliaryService.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        (WebControl) XBeans.getInstanceOf(ctx, AuxiliaryService.class, WebControl.class)));
        fMgr.register(ctx, CallDetail.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        (WebControl) XBeans.getInstanceOf(ctx, CallDetail.class, WebControl.class, new CustomCallDetailWebControl())));
        fMgr.register(ctx, EarlyRewardConfiguration.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        (WebControl) XBeans.getInstanceOf(ctx, EarlyRewardConfiguration.class, WebControl.class, new CustomEarlyRewardConfigurationWebControl())));
        fMgr.register(ctx, LateFeeConfiguration.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        (WebControl) XBeans.getInstanceOf(ctx, LateFeeConfiguration.class, WebControl.class, new CustomLateFeeConfigurationWebControl())));

        fMgr.register(ctx, DiscountClass.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        new DiscountClassCustomWebControl()));

        // Register custom table web controls to override those provided by data models in ModelAppCrm
        fMgr.register(ctx, AlcatelSSCProperty.class, TableWebControl.class, new CRMAlcatelSSCPropertyTableWebControl());
        fMgr.register(ctx, AlcatelSSCServiceExtension.class, TableWebControl.class, new CRMAlcatelSSCServiceExtensionTableWebControl());
        fMgr.register(ctx, Msisdn.class, TableWebControl.class, new CRMMsisdnTableWebControl());
        fMgr.register(ctx, MsisdnGroup.class, TableWebControl.class, new CRMMsisdnGroupTableWebControl());        
        fMgr.register(ctx, ProvisionCommand.class, TableWebControl.class, new CRMProvisionCommandTableWebControl());
        fMgr.register(ctx, ServiceExtensionHolder.class, TableWebControl.class, new CRMServiceExtensionHolderTableWebControl());
        fMgr.register(ctx, Transaction.class, TableWebControl.class, new CRMTransactionTableWebControl());
        fMgr.register(ctx, EarlyRewardCreditCategoryExtension.class, TableWebControl.class, new CRMEarlyRewardCreditCategoryExtensionTableWebControl());
        fMgr.register(ctx, EarlyRewardExtensionProperty.class, TableWebControl.class, new CRMEarlyRewardExtensionPropertyTableWebControl());
        fMgr.register(ctx, LateFeeCreditCategoryExtension.class, TableWebControl.class, new CRMLateFeeCreditCategoryExtensionTableWebControl());
        fMgr.register(ctx, CreditCategoryExtensionHolder.class, TableWebControl.class, new CRMCreditCategoryExtensionHolderTableWebControl());
        
        // Wrap whatever web controls are currently set up for some beans
        fMgr.register(ctx, Subscriber.class, WebControl.class,
                new SetTechnologyProxyWebControl(
                        new SubscriberViewCustomizationWebControl(
                                new CurrencyContextSetupWebControl(
                                        new ConvergeSubscriberWebControl(
                                                new NotifyAfterFromWebWebControl(new PricePlanSwitchFromWebListener(),
                                                        new SubscriberSatWebControl(
                                                                new SubscriptionCardAutoSelectWebControl(
                                                                        new SubscriptionResourceAutoSelectWebControl(
                                                                                (WebControl)XBeans.getInstanceOf(ctx, Subscriber.class, WebControl.class))))))))));

        fMgr.register(ctx, AccountExtensionHolder.class, WebControl.class, new ExtensionDescriptionWebControl(
                (WebControl)XBeans.getInstanceOf(ctx, AccountExtensionHolder.class, WebControl.class)));
        fMgr.register(ctx, MoveAccountExtensionHolder.class, WebControl.class, new ExtensionDescriptionWebControl(
                (WebControl)XBeans.getInstanceOf(ctx, MoveAccountExtensionHolder.class, WebControl.class)));
        fMgr.register(ctx, SubscriberExtensionHolder.class, WebControl.class, new ExtensionDescriptionWebControl(
                (WebControl)XBeans.getInstanceOf(ctx, SubscriberExtensionHolder.class, WebControl.class)));
        fMgr.register(ctx, ServiceExtensionHolder.class, WebControl.class, new ExtensionDescriptionWebControl(
                (WebControl)XBeans.getInstanceOf(ctx, ServiceExtensionHolder.class, WebControl.class)));
        fMgr.register(ctx, SpidExtensionHolder.class, WebControl.class, new ExtensionDescriptionWebControl(
                (WebControl)XBeans.getInstanceOf(ctx, SpidExtensionHolder.class, WebControl.class)));
        fMgr.register(ctx, UserGroupExtensionHolder.class, WebControl.class, new ExtensionDescriptionWebControl(
                (WebControl)XBeans.getInstanceOf(ctx, UserGroupExtensionHolder.class, WebControl.class)));
        
        fMgr.register(ctx, PoolExtension.class, WebControl.class,
                new PoolExtensionViewCustomizationWebControl(
                        (WebControl)XBeans.getInstanceOf(ctx, PoolExtension.class, WebControl.class)));


        fMgr.register(ctx, Account.class, WebControl.class, new AccountViewCustomizationWebControl(
                new CurrencyContextSetupWebControl(
                        new AccountCopyWebControl(
                        new NotifyAfterFromWebWebControl(new AccountSpidSwitchFromWebListener(),
                        new AccountActCustomWebControl(
                                new AccountWebControlProxy(
                                        new AccountIdentificationGroupCustomWebControl(
                                                (WebControl)XBeans.getInstanceOf(ctx, Account.class, WebControl.class)))))))));

        fMgr.register(ctx, BundleProfile.class, WebControl.class,
            new BundleProfileWebControlProxy(
                new BundleProfileViewCustomizationWebControl( new NotifyAfterFromWebWebControl(new BundleProfileSwitchFromWebListener(),
                        (WebControl)XBeans.getInstanceOf(ctx, BundleProfile.class, WebControl.class)))));
        fMgr.register(ctx, com.redknee.app.crm.bundle.BundleProfile.class, WebControl.class,
                new BundleProfileWebControlProxy(
                        (WebControl)XBeans.getInstanceOf(ctx, com.redknee.app.crm.bundle.BundleProfile.class, WebControl.class, new CustomBundleProfileWebControl())));

        fMgr.register(ctx, BundleCategoryAssociation.class, WebControl.class,
                 new CustomBundleCategoryAssociationWebControl());

        fMgr.register(ctx, BundleCategoryAssociation.class, TableWebControl.class,
                new CustomBundleCategoryAssociationTableWebControl());

        fMgr.register(ctx, Contract.class, WebControl.class,
                new CurrencyContextSetupWebControl(
                        (WebControl)XBeans.getInstanceOf(ctx, Contract.class, WebControl.class)));

        fMgr.register(ctx, ServiceActivationTemplate.class, WebControl.class,
                new SetTechnologyProxyWebControl(
                        new CurrencyContextSetupWebControl(
                                (WebControl)XBeans.getInstanceOf(ctx, ServiceActivationTemplate.class, WebControl.class))));

        fMgr.register(ctx, SubBulkCreate.class, WebControl.class,
                new SetTechnologyProxyWebControl(
                        new SubBulkCreateWebControlEx(
                                (WebControl)XBeans.getInstanceOf(ctx, SubBulkCreate.class, WebControl.class))));
        
        fMgr.register(ctx, Service.class, WebControl.class,
                new NotifyAfterFromWebWebControl(new ServiceSpidSwitchFromWebListener(),
                        (WebControl) XBeans.getInstanceOf(ctx, Service.class, WebControl.class)));

		fMgr.register(ctx, DuplicateAccountDetectionResult.class,
		    TableWebControl.class,
		    new CustomDuplicateAccountDetectionResultTableWebControl());
		fMgr.register(
		    ctx,
		    DuplicateAccountDetectionIdentificationResult.class,
		    TableWebControl.class,
		    new CustomDuplicateAccountDetectionIdentificationResultTableWebControl());
		fMgr.register(
		    ctx,
		    DuplicateAccountDetectionSubscriptionResult.class,
		    TableWebControl.class,
		    new CustomDuplicateAccountDetectionSubscriptionResultTableWebControl());
			/*
			* For Generic Bulkloader entity-home customizations
			*/
			fMgr.register(ctx, Msisdn.class, GenericBeanBulkloadHome.class, new MsisdnGenericBeanBulkloadHomeProxy(ctx));

		supportForFW6(ctx,fMgr);
		
		

    }
    
    /**
     * To make BSS support with FW6 
     * 
     * @param ctx
     * @param fMgr
     */
    private void supportForFW6(final Context ctx, final com.redknee.framework.xhome.beans.FacetMgr fMgr)
    {
        fMgr.register(ctx, com.redknee.app.crm.api.queryexecutor.AbstractQueryExecutor.class, XMLSupport.class,
                com.redknee.framework.xhome.support.ObjectXMLSupport.instance());
        fMgr.register(ctx, com.redknee.app.crm.api.queryexecutor.AbstractQueryExecutor.class, SchemeSupport.class,
                com.redknee.framework.xhome.support.ObjectSchemeSupport.instance());
    }
}
//test