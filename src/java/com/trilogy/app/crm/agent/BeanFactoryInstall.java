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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHistory;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargingType;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearch;
import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.bean.OICKMapping;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCycleBundleUsage;
import com.trilogy.app.crm.bean.SubscriberCycleUsage;
import com.trilogy.app.crm.bean.SubscriberTechnologyConversion;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.template.SubscriberTemplate;
import com.trilogy.app.crm.bean.template.TransactionTemplate;
import com.trilogy.app.crm.bean.usage.BalanceUsage;
import com.trilogy.app.crm.calculator.AccountValueCalculator;
import com.trilogy.app.crm.calculator.SubscriberValueCalculator;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.HomeZoneAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.MultiSimAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.NGRCOptInAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.PRBTAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.VPNAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.VoicemailAuxSvcExtension;
import com.trilogy.app.crm.factory.AccountAttachmentFactory;
import com.trilogy.app.crm.factory.AccountExtensionFactory;
import com.trilogy.app.crm.factory.AccountFactory;
import com.trilogy.app.crm.factory.AccountHistoryFactory;
import com.trilogy.app.crm.factory.AccountIdentificationFactory;
import com.trilogy.app.crm.factory.BalanceUsageFactory;
import com.trilogy.app.crm.factory.BeanAdaptingContextFactory;
import com.trilogy.app.crm.factory.CRMSpidFactory;
import com.trilogy.app.crm.factory.CallDetailFactory;
import com.trilogy.app.crm.factory.ChargingTypeFactory;
import com.trilogy.app.crm.factory.ContextRedirectingContextFactory;
import com.trilogy.app.crm.factory.ConvergedAccountSubscriberSearchFactory;
import com.trilogy.app.crm.factory.CoreBeanAdaptingContextFactory;
import com.trilogy.app.crm.factory.CustomPropertyValueCalculatorFactory;
import com.trilogy.app.crm.factory.MsisdnOwnershipFactory;
import com.trilogy.app.crm.factory.OICKMappingFactory;
import com.trilogy.app.crm.factory.PrototypeContextFactory;
import com.trilogy.app.crm.factory.ProvisionCommandFactory;
import com.trilogy.app.crm.factory.SubscriberFactory;
import com.trilogy.app.crm.factory.SubscriberTechnologyConversionFactory;
import com.trilogy.app.crm.factory.TransactionFactory;
import com.trilogy.app.crm.factory.TransferDisputeFactory;
import com.trilogy.app.crm.factory.core.AuxiliaryServiceFactory;
import com.trilogy.app.crm.factory.core.MsisdnFactory;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.CritLogMsg;

public class BeanFactoryInstall
    extends com.redknee.app.crm.core.agent.BeanFactoryInstall
    implements ContextAgent
{
    /**
     * Installs custom bean factories. 
     *
     * @param ctx context where the components will be installed
     */
    @Override
    public void execute(final Context ctx)
    {
        try
        {
            installCoreBean(ctx, 
                    CallDetail.class, 
                    com.redknee.app.crm.bean.core.custom.CallDetail.class, 
                    new CoreBeanAdaptingContextFactory<CallDetail, com.redknee.app.crm.bean.core.custom.CallDetail>(
                            CallDetail.class, 
                            com.redknee.app.crm.bean.core.custom.CallDetail.class, 
                            CallDetailFactory.instance()));

            installCoreBean(ctx, 
                    AuxiliaryService.class, 
                    com.redknee.app.crm.bean.core.custom.AuxiliaryService.class,
                    new CoreBeanAdaptingContextFactory<AuxiliaryService, com.redknee.app.crm.bean.core.custom.AuxiliaryService>(
                            AuxiliaryService.class, 
                            com.redknee.app.crm.bean.core.custom.AuxiliaryService.class, 
                            AuxiliaryServiceFactory.instance()));
                    
            installCoreBean(ctx, 
                    AddMsisdnAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.AddMsisdnAuxSvcExtension.class);
            installCoreBean(ctx, 
                    CallingGroupAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension.class);
            installCoreBean(ctx, 
                    HomeZoneAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.HomeZoneAuxSvcExtension.class);
            installCoreBean(ctx, 
                    MultiSimAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.MultiSimAuxSvcExtension.class);
            installCoreBean(ctx, 
                    NGRCOptInAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.NGRCOptInAuxSvcExtension.class);
            installCoreBean(ctx, 
                    PRBTAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.PRBTAuxSvcExtension.class);
            installCoreBean(ctx, 
                    SPGAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.SPGAuxSvcExtension.class);
            installCoreBean(ctx, 
                    URCSPromotionAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension.class);
            installCoreBean(ctx, 
                    VoicemailAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.VoicemailAuxSvcExtension.class);
            installCoreBean(ctx, 
                    VPNAuxSvcExtension.class, 
                    com.redknee.app.crm.extension.auxiliaryservice.core.custom.VPNAuxSvcExtension.class);

            installCoreBean(ctx, 
                    SubscriberCycleUsage.class, 
                    com.redknee.app.crm.bean.core.custom.SubscriberCycleUsage.class);
                    
            installCoreBean(ctx, 
                    SubscriberCycleBundleUsage.class, 
                    com.redknee.app.crm.bean.core.custom.SubscriberCycleBundleUsage.class);
            
            // [CW] required by all
            XBeans.putBeanFactory(ctx, Account.class, new AccountFactory());
            XBeans.putBeanFactory(ctx, AccountIdentification.class, new AccountIdentificationFactory());
            XBeans.putBeanFactory(ctx, Subscriber.class, new SubscriberFactory(new PrototypeContextFactory(SubscriberTemplate.class)));
                    
            XBeans.putBeanFactory(ctx, Transaction.class, new TransactionFactory(new PrototypeContextFactory(TransactionTemplate.class)));
            XBeans.putBeanFactory(ctx, ConvergedAccountSubscriberSearch.class,
                    new ConvergedAccountSubscriberSearchFactory());

            XBeans.putBeanFactory(ctx, AccountHistory.class, new AccountHistoryFactory());
            XBeans.putBeanFactory(ctx, BalanceUsage.class, new BalanceUsageFactory());
            XBeans.putBeanFactory(ctx, ChargingType.class, new ChargingTypeFactory());
            XBeans.putBeanFactory(ctx, MsisdnOwnership.class, new MsisdnOwnershipFactory());
            XBeans.putBeanFactory(ctx, Msisdn.class, new CoreBeanAdaptingContextFactory<Msisdn, com.redknee.app.crm.bean.custom.Msisdn>(Msisdn.class, com.redknee.app.crm.bean.custom.Msisdn.class, MsisdnFactory.instance()));
            XBeans.putBeanFactory(ctx, com.redknee.app.crm.bean.ui.Msisdn.class, new BeanAdaptingContextFactory<Msisdn, com.redknee.app.crm.bean.ui.Msisdn>(Msisdn.class, com.redknee.app.crm.bean.ui.Msisdn.class, MsisdnFactory.instance()));
            XBeans.putBeanFactory(ctx, OICKMapping.class, new OICKMappingFactory());
            XBeans.putBeanFactory(ctx, ProvisionCommand.class, new ProvisionCommandFactory());
            XBeans.putBeanFactory(ctx, SubscriberTechnologyConversion.class,
                    new SubscriberTechnologyConversionFactory());
            XBeans.putBeanFactory(ctx, CRMSpid.class, new CRMSpidFactory());
            XBeans.putBeanFactory(ctx, TransferDispute.class, new TransferDisputeFactory());
            XBeans.putBeanFactory(ctx, AccountAttachment.class, new AccountAttachmentFactory());
            XBeans.putBeanFactory(ctx, PoolExtension.class, new AccountExtensionFactory<PoolExtension>(PoolExtension.class));

            XBeans.putBeanFactory(ctx, AccountValueCalculator.class, new CustomPropertyValueCalculatorFactory(AccountValueCalculator.class, com.redknee.app.crm.bean.Account.class));
            XBeans.putBeanFactory(ctx, SubscriberValueCalculator.class, new CustomPropertyValueCalculatorFactory(SubscriberValueCalculator.class, com.redknee.app.crm.bean.Subscriber.class));

            installMiniBeanContextKeys(ctx);
        }
        catch (Throwable t)
        {
            new CritLogMsg(this, "fail to install AppCrm Bean Factories [" + t.getMessage() + "]", t).log(ctx);
        }
    }

    private void installMiniBeanContextKeys(final Context ctx)
    {
        ctx.put(com.redknee.app.crm.invoice.bean.InvoiceSpid.class, new BeanAdaptingContextFactory<com.redknee.app.crm.bean.CRMSpid, com.redknee.app.crm.invoice.bean.InvoiceSpid>(
                com.redknee.app.crm.bean.CRMSpid.class, 
                com.redknee.app.crm.invoice.bean.InvoiceSpid.class,
                new ContextRedirectingContextFactory(com.redknee.app.crm.bean.CRMSpid.class)));

        ctx.put(com.redknee.app.crm.invoice.bean.Account.class, new BeanAdaptingContextFactory<com.redknee.app.crm.bean.Account, com.redknee.app.crm.invoice.bean.Account>(
                com.redknee.app.crm.bean.Account.class, 
                com.redknee.app.crm.invoice.bean.Account.class,
                new ContextRedirectingContextFactory(com.redknee.app.crm.bean.Account.class)));

        ctx.put(com.redknee.app.crm.calculation.bean.Account.class, new BeanAdaptingContextFactory<com.redknee.app.crm.bean.Account, com.redknee.app.crm.calculation.bean.Account>(
                com.redknee.app.crm.bean.Account.class, 
                com.redknee.app.crm.calculation.bean.Account.class,
                new ContextRedirectingContextFactory(com.redknee.app.crm.bean.Account.class)));

        ctx.put(com.redknee.app.crm.invoice.bean.PoolExtension.class, new BeanAdaptingContextFactory<com.redknee.app.crm.extension.account.PoolExtension, com.redknee.app.crm.invoice.bean.PoolExtension>(
                com.redknee.app.crm.extension.account.PoolExtension.class, 
                com.redknee.app.crm.invoice.bean.PoolExtension.class,
                new ContextRedirectingContextFactory(com.redknee.app.crm.extension.account.PoolExtension.class)));

        ctx.put(com.redknee.app.crm.invoice.bean.Contact.class, new BeanAdaptingContextFactory<com.redknee.app.crm.bean.account.Contact, com.redknee.app.crm.invoice.bean.Contact>(
                com.redknee.app.crm.bean.account.Contact.class, 
                com.redknee.app.crm.invoice.bean.Contact.class,
                new ContextRedirectingContextFactory(com.redknee.app.crm.bean.account.Contact.class)));

        ctx.put(com.redknee.app.crm.invoice.bean.Subscriber.class, new BeanAdaptingContextFactory<com.redknee.app.crm.bean.Subscriber, com.redknee.app.crm.invoice.bean.Subscriber>(
                com.redknee.app.crm.bean.Subscriber.class, 
                com.redknee.app.crm.invoice.bean.Subscriber.class,
                new ContextRedirectingContextFactory(com.redknee.app.crm.bean.Subscriber.class)));

        ctx.put(com.redknee.app.crm.invoice.bean.PPSMSupporteeSubExtension.class, new BeanAdaptingContextFactory<com.redknee.app.crm.extension.subscriber.PPSMSupporteeSubExtension, com.redknee.app.crm.invoice.bean.PPSMSupporteeSubExtension>(
                com.redknee.app.crm.extension.subscriber.PPSMSupporteeSubExtension.class, 
                com.redknee.app.crm.invoice.bean.PPSMSupporteeSubExtension.class,
                new ContextRedirectingContextFactory(com.redknee.app.crm.extension.subscriber.PPSMSupporteeSubExtension.class)));

        ctx.put(com.redknee.app.crm.invoice.bean.SubscriberAuxiliaryService.class, new BeanAdaptingContextFactory<com.redknee.app.crm.bean.SubscriberAuxiliaryService, com.redknee.app.crm.invoice.bean.SubscriberAuxiliaryService>(
                com.redknee.app.crm.bean.SubscriberAuxiliaryService.class, 
                com.redknee.app.crm.invoice.bean.SubscriberAuxiliaryService.class,
                new ContextRedirectingContextFactory(com.redknee.app.crm.bean.SubscriberAuxiliaryService.class)));
    }

}
