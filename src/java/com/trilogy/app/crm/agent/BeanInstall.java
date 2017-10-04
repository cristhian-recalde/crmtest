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


import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CallDetailProcessConfig;
import com.trilogy.app.crm.bean.AlcatelSSCConfig;
import com.trilogy.app.crm.bean.EcpStateMap;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.MsisdnBulk;
import com.trilogy.app.crm.bean.PaymentGatewayIntegrationConfig;
import com.trilogy.app.crm.bean.SpidObjectsTemplate;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.SystemFeatureThreadpoolConfig;
import com.trilogy.app.crm.bean.TestBackgroundTask;
import com.trilogy.app.crm.bean.TestBackgroundTaskHome;
import com.trilogy.app.crm.bean.TestBackgroundTaskTransientHome;
import com.trilogy.app.crm.bean.VoicemailServiceConfig;
import com.trilogy.app.crm.bean.account.AccountAttachmentManagementConfig;
import com.trilogy.app.crm.bean.hlr.nortel.NortelServiceMapConfig;
import com.trilogy.app.crm.bean.ipc.IpcProvConfig;
import com.trilogy.app.crm.bean.template.AdjustmentTemplate;
import com.trilogy.app.crm.bean.template.AdjustmentTypeTemplate;
import com.trilogy.app.crm.bean.template.GSMPackageTemplate;
import com.trilogy.app.crm.bean.template.TDMAPackageTemplate;
import com.trilogy.app.crm.bean.template.TransactionTemplate;
import com.trilogy.app.crm.bean.template.VSATPackageTemplate;
import com.trilogy.app.crm.config.AppHomezoneClientConfig;
import com.trilogy.app.crm.delivery.email.KeywordConfiguration;
import com.trilogy.app.crm.invoice.config.InvoiceServerRemoteServicerConfig;
import com.trilogy.app.crm.support.ConfigChangeRequestSupport;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.voicemail.MpathixConnectionInfoPropertyListener;
import com.trilogy.driver.voicemail.mpathix.xgen.MpathixConnectionInfo;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredMetaBean;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.CritLogMsg;

/**
 * This class is for installing Global Bean instance and BeanFactories
 *
 * @author joe.chen@redknee.com
 */
public class BeanInstall extends com.redknee.app.crm.core.agent.BeanInstall implements ContextAgent
{
    public final static String ACCOUNT_TEMPLATE = "AccountTemplate";
    public final static String SUBSCRIBER_TEMPLATE = "SubscriberTemplate";
    //public final static String SPIDOBJECTS_TEMPLATE = "SpidObjectsTemplate";
    public final static String MSISDNBULKLOAD_TEMPLATE = "MsisdnBulkLoadTemplate";

    /**
     * The key under which the BulkLoadAccount template is saved in the
     * Context.
     */
    public final static String BULK_ACCOUNT_TEMPLATE = "BulkAccountTemplate";

    /**
     * The key under which the BulkLoadSubscriber template is saved in the
     * Context.
     */
    public final static String BULK_SUBSCRIBER_TEMPLATE = "BulkSubscriberTemplate";

    /**
     * The key under which the BulkLoadPackage template is saved in the
     * Context.
     */
    public final static String BULK_PACKAGE_TEMPLATE = "BulkPackageTemplate";

    /**
     * The key under which the BulkLoadMsisdn template is saved in the
     * Context.
     */
    public final static String BULK_MSISDN_TEMPLATE = "BulkMsisdnTemplate";

    /* (non-Javadoc)
    * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
    */
    @Override
    public void execute(Context ctx) throws AgentException
    {
        ConfigChangeRequestSupport configSharingSupport = ConfigChangeRequestSupportHelper.get(ctx);
        
        try
        {
            
            // [CW] clustered by all
            GeneralConfig genConfig = (GeneralConfig) CoreSupport.bindBean(ctx, GeneralConfig.class);
            CoreSupport.bindBean(ContextLocator.locate(), GeneralConfig.class);
            configSharingSupport.registerBeanForConfigSharing(ctx, genConfig);
            new RMIClusteredMetaBean(
                   ctx,
                   GeneralConfig.class.getName(),
                   GeneralConfig.class,
                   true,
                   CoreSupport.getProjectHome(ctx),
                   CoreSupport.getHostname(ctx));
            
            NortelServiceMapConfig nortelConfig = (NortelServiceMapConfig) CoreSupport.bindBean(ctx,
                    NortelServiceMapConfig.class);
            configSharingSupport.registerBeanForConfigSharing(ctx, nortelConfig);
            new RMIClusteredMetaBean(ctx, NortelServiceMapConfig.class.getName(), NortelServiceMapConfig.class, true,
                    CoreSupport.getProjectHome(ctx), CoreSupport.getHostname(ctx));
             
            //CoreSupport.bindBeanToXML(ctx, SYSFEATURECFG_TEMPLATE, SysFeatureCfg.class, "SysFeatureCfg.xml");
            // [CW] clustered by all
            CoreSupport.bindBean(ctx, SysFeatureCfg.class);
            new RMIClusteredMetaBean(
                    ctx,
                    SysFeatureCfg.class.getName(),
                    SysFeatureCfg.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [CW] clustered by all
            CoreSupport.bindBeanToXML(ctx, com.redknee.app.crm.bean.AccountCreationTemplate.class, Account.class, "AccountTemplate.xml");
            
            /*new RMIClusteredMetaBean(
                    ctx,
                    ACCOUNT_TEMPLATE,                    
                    com.redknee.app.crm.bean.AccountCreationTemplate.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));
            */
            
            CoreSupport.bindBean(ctx, InvoiceServerRemoteServicerConfig.class);

            // [CW] clustered by all
            CoreSupport.bindBeanToXML(ctx, com.redknee.app.crm.bean.template.SubscriberTemplate.class, Subscriber.class, "SubscriberTemplate.xml");
            
            /*new RMIClusteredMetaBean(
                    ctx,
                    SUBSCRIBER_TEMPLATE,
                    com.redknee.app.crm.bean.template.SubscriberTemplate.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));
            */
            
            //required by all
            CoreSupport.bindBeanToXML(ctx, EcpStateMap.class, "EcpStateMap.xml");
            new RMIClusteredMetaBean(
                    ctx,
                    EcpStateMap.class.getName(),
                    EcpStateMap.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [CW] required by all

            // [CW] clustered by all
            CoreSupport.bindBeanToXML(ctx, SpidObjectsTemplate.class, "SpidObjectsTemplate.xml");
            new RMIClusteredMetaBean(
                    ctx,
                    SpidObjectsTemplate.class.getName(),
                    SpidObjectsTemplate.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            // [CW] required by all

            // [CW] clustered by all
            CoreSupport.bindBeanToXML(ctx, com.redknee.app.crm.bean.template.MsisdnTemplate.class, MsisdnBulk.class, "MsisdnBulkLoadTemplate.xml");
            new RMIClusteredMetaBean(
                    ctx,
                    MSISDNBULKLOAD_TEMPLATE,
                    com.redknee.app.crm.bean.template.MsisdnTemplate.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            CoreSupport.bindBean(ctx, IpcProvConfig.class);

            //installing homezone client configurationbean
            CoreSupport.bindBean(ctx, AppHomezoneClientConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    AppHomezoneClientConfig.class.getName(),
                    AppHomezoneClientConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));
            
            //installing account attachment configurations
            CoreSupport.bindBean(ctx, AccountAttachmentManagementConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    AccountAttachmentManagementConfig.class.getName(),
                    AccountAttachmentManagementConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            MpathixConnectionInfoPropertyListener connInfoLsnr = new MpathixConnectionInfoPropertyListener(ctx);
            ctx.put(MpathixConnectionInfoPropertyListener.class, connInfoLsnr);

            //installing MPathix COnnection cofiguration
            CoreSupport.bindBean(ctx, MpathixConnectionInfo.class);
            new RMIClusteredMetaBean(
                    ctx,
                    MpathixConnectionInfo.class.getName(),
                    MpathixConnectionInfo.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));
            //Add a propertchangeListener to this bean because whenever the IP,Port and other
            //connection related parameters are changed we need to take new connection for VM
            ((MpathixConnectionInfo) ctx.get(MpathixConnectionInfo.class)).addPropertyChangeListener(connInfoLsnr);

            //installing MPathix Service cofiguration bean
            CoreSupport.bindBean(ctx, VoicemailServiceConfig.class);
            new RMIClusteredMetaBean(
                    ctx,
                    VoicemailServiceConfig.class.getName(),
                    VoicemailServiceConfig.class,
                    true,
                    CoreSupport.getProjectHome(ctx),
                    CoreSupport.getHostname(ctx));

            
            ctx.put(TestBackgroundTaskHome.class, new HomeProxy(ctx,new TestBackgroundTaskTransientHome(ctx))
            {
                @Override
                public Object create(Context ctx, Object bean) throws HomeException
                {
                    Object returnObject = super.create(ctx, bean);
                    TestBackgroundTask mockBean = (TestBackgroundTask) bean;
                    try
                    {
                        mockBean.getTask().execute(ctx);
                    }
                    catch (AgentException e)
                    {
                        // TODO Auto-generated catch block
                        throw new HomeException(e);
                    }
                    return returnObject;
                }
            });
            
            
            // [CW] clustered by all
            CoreSupport.bindBean(ctx, BULK_ACCOUNT_TEMPLATE, Account.class);
            //new RMIClusteredMetaBean(ctx,BULK_ACCOUNT_TEMPLATE,BULK_ACCOUNT_TEMPLATE,true,
            //      CoreSupport.getProjectHome(ctx),CoreSupport.getHostname(ctx));
            Account acct = (Account) ctx.get(BULK_ACCOUNT_TEMPLATE);
            acct.setSubscriber(null);
            acct.setContext(null);

            // [CW] clustered by all
            CoreSupport.bindBean(ctx, BULK_SUBSCRIBER_TEMPLATE, Subscriber.class);
            //new RMIClusteredMetaBean(ctx,BULK_SUBSCRIBER_TEMPLATE,BULK_SUBSCRIBER_TEMPLATE,true,
            //      CoreSupport.getProjectHome(ctx),CoreSupport.getHostname(ctx));
            Subscriber sub = (Subscriber) ctx.get(BULK_SUBSCRIBER_TEMPLATE);
            sub.setContext(null);
            
            
            // A bean that holds Alcatel SSC Client Configuation
            // Alcatel System is license controlled
            if(LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.ALCATEL_LICENSE))
            {
                CoreSupport.bindBean(ctx, AlcatelSSCConfig.class);
                new RMIClusteredMetaBean(ctx, AlcatelSSCConfig.class.getName(), AlcatelSSCConfig.class, true,
                        CoreSupport.getProjectHome(ctx), CoreSupport.getHostname(ctx));
            }

            CoreSupport.bindBean(ctx, PaymentGatewayIntegrationConfig.class);
            
            CoreSupport.bindBean(ctx, SystemFeatureThreadpoolConfig.class);
            // Cluster all beans that were installed by core
            CoreSupport.bindBeanToXML(ctx, com.redknee.app.crm.bean.CallDetailProcessConfig.class,"CallDetailProcessConfig.xml");
            clusterCoreBeans(ctx);

        }
        catch (Throwable t)
        {
            new CritLogMsg(this, "fail to install", t).log(ctx);
            throw new AgentException("Fail to complete BeanInstall", t);
      }
   }

    private void clusterCoreBeans(Context ctx) throws RemoteException, HomeException, IOException
    {
        // [CW] clustered by all
        new RMIClusteredMetaBean(
                ctx,
                ADJUSTMENT_TEMPLATE,
                AdjustmentTemplate.class,
                true,
                CoreSupport.getProjectHome(ctx),
                CoreSupport.getHostname(ctx));
        
        // [CW] clustered by all
        new RMIClusteredMetaBean(
                ctx,
                ADJUSTMENTTYPE_TEMPLATE,
                AdjustmentTypeTemplate.class,
                true,
                CoreSupport.getProjectHome(ctx),
                CoreSupport.getHostname(ctx));

        // [CW] clustered by all
        new RMIClusteredMetaBean(
                ctx,
                GSM_PACKAGE_TEMPLATE,
                GSMPackageTemplate.class,
                true,
                CoreSupport.getProjectHome(ctx),
                CoreSupport.getHostname(ctx));

        // [CW] clustered by all
        new RMIClusteredMetaBean(
                ctx,
                TDMA_PACKAGE_TEMPLATE,
                TDMAPackageTemplate.class,
                true,
                CoreSupport.getProjectHome(ctx),
                CoreSupport.getHostname(ctx));

        // [CW] clustered by all
        new RMIClusteredMetaBean(
                ctx,
                VSAT_PACKAGE_TEMPLATE,
                VSATPackageTemplate.class,
                true,
                CoreSupport.getProjectHome(ctx),
                CoreSupport.getHostname(ctx));

        // [CW] clustered by all
        new RMIClusteredMetaBean(
                ctx,
                TRANSACTION_TEMPLATE,
                TransactionTemplate.class,
                true,
                CoreSupport.getProjectHome(ctx),
                CoreSupport.getHostname(ctx));
        
        new RMIClusteredMetaBean(
                ctx,
                KeywordConfiguration.class.getName(),
                KeywordConfiguration.class,
                true,
                CoreSupport.getProjectHome(ctx),
                CoreSupport.getHostname(ctx));
    }

}
