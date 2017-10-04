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

import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.AccountHierarchyService;
import com.trilogy.app.crm.account.AccountHierarchyServiceImpl;
import com.trilogy.app.crm.aptilo.ServiceAptiloInstall;
import com.trilogy.app.crm.api.generic.entity.adapter.CustomMsisdnGenericEntityAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.CustomSubModificationScheduleGenericEntityAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.CustomUserGenericEntityAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.DefaultGenericEntityAdapter;
import com.trilogy.app.crm.api.generic.entity.interceptor.DefaultGenericEntityInterceptor;
import com.trilogy.app.crm.api.generic.entity.adapter.DisputeCreationAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.DisputeUpdationAdapter;
import com.trilogy.app.crm.api.generic.entity.validator.ATUGenericEntityValidator;
import com.trilogy.app.crm.api.generic.entity.validator.AddressGenericEntityValidator;
import com.trilogy.app.crm.api.generic.entity.validator.DefaultGenericEntityValidator;
import com.trilogy.app.crm.api.generic.entity.validator.DisputeCreationValidator;
import com.trilogy.app.crm.api.generic.entity.validator.DisputeUpdationValidator;
import com.trilogy.app.crm.api.generic.entity.validator.MsisdnGenericEntityValidator;
import com.trilogy.app.crm.api.generic.entity.validator.ScheduledPricePlanGenericEntityValidator;
import com.trilogy.app.crm.api.rmi.agent.ConcurrentConfigshareRequestPushAgent;
import com.trilogy.app.crm.audi.AUDIDeleteLogicProcess;
import com.trilogy.app.crm.audi.AUDILoadLogicProcess;
import com.trilogy.app.crm.audi.AUDIUpdateLogicProcess;
import com.trilogy.app.crm.bas.promotion.PromotionFactory;
import com.trilogy.app.crm.bas.tps.TPSProvisioningLifecycleAgent;
import com.trilogy.app.crm.bas.tps.pipe.AccountPaymentCalculateAgent;
import com.trilogy.app.crm.bas.tps.pipe.AccountTaxCalculateAgent;
import com.trilogy.app.crm.bas.tps.pipe.AccountTotalOwingComputeAgent;
import com.trilogy.app.crm.bas.tps.pipe.CurrencyLookupAgent;
import com.trilogy.app.crm.bas.tps.pipe.DepositReleaseTransactionAgent;
import com.trilogy.app.crm.bas.tps.pipe.InvoiceLookupAgent;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberCreditLimitUpdateAgent;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberDepositSettingAgent;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberInvoiceLookupAgent;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberPaymentCalculateAgent;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberTaxCalculateAgent;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberTotalOwingComputeAgent;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberUpdateUpsAgent;
import com.trilogy.app.crm.bas.tps.pipe.TaxrateLookupAgent;
import com.trilogy.app.crm.bas.tps.pipe.TransactionContextPresetAgent;
import com.trilogy.app.crm.bas.tps.pipe.UpdateTransactionAgent;
import com.trilogy.app.crm.bean.HLRProvisioningGateway;
import com.trilogy.app.crm.bean.SystemFeatureThreadpoolConfig;
import com.trilogy.app.crm.bean.TfaRmiConfig;
import com.trilogy.app.crm.bean.calldetail.CallCategorization;
import com.trilogy.app.crm.bean.calldetail.CallCategorizationImpl;
import com.trilogy.app.crm.blackberry.ServiceBlackberryInstall;
import com.trilogy.app.crm.bulkloader.AUDIDeleteProcess;
import com.trilogy.app.crm.bulkloader.AUDIProcess;
import com.trilogy.app.crm.bulkloader.AUDIUpdateProcess;
import com.trilogy.app.crm.ccr.corba.CreditCardRetrievalService;
import com.trilogy.app.crm.client.AppHomezoneClient;
import com.trilogy.app.crm.client.AppTFAClient;
import com.trilogy.app.crm.client.EcpRatePlanClient;
import com.trilogy.app.crm.client.EcpRatePlanClientFactory;
import com.trilogy.app.crm.client.InvoiceServerRMIServicePropertyChangeListener;
import com.trilogy.app.crm.client.TFAAuxiliaryServiceClientFactory;
import com.trilogy.app.crm.client.VpnClient;
import com.trilogy.app.crm.client.aaa.AAAClientFactory;
import com.trilogy.app.crm.client.alcatel.AlcatelProvisioning;
import com.trilogy.app.crm.client.alcatel.AlcatelSSCProvisioningImpl;
import com.trilogy.app.crm.client.alcatel.TestAlcatelProvisioningImpl;
import com.trilogy.app.crm.client.ipcg.IpcgClientFactory;
import com.trilogy.app.crm.client.ngrc.AppNGRCClient;
import com.trilogy.app.crm.client.ngrc.AppNGRCClientDebug;
import com.trilogy.app.crm.client.ngrc.AppNGRCClientImpl;
import com.trilogy.app.crm.client.ngrc.AppNGRCClientPM;
import com.trilogy.app.crm.client.ringbacktone.RBTClientFactory;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.collection.PrefixService;
import com.trilogy.app.crm.collection.PrefixServiceImpl;
import com.trilogy.app.crm.creditcheck.adapter.ExternalCreditCheckAdapter;
import com.trilogy.app.crm.creditcheck.validator.ExternalCreditCheckRetrieveValidator;
import com.trilogy.app.crm.creditcheck.validator.ExternalCreditCheckValidator;
import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.dunning.DunningProcessServer;
import com.trilogy.app.crm.dunning.RMIDunningProcessClient;
import com.trilogy.app.crm.dunning.RMIDunningProcessServer;
import com.trilogy.app.crm.hlr.RMIHLRProvisioningExternalService;
import com.trilogy.app.crm.invoice.config.InvoiceServerRemoteServicerConfig;
import com.trilogy.app.crm.invoice.config.InvoiceServerRemoteServicerConfigXInfo;
import com.trilogy.app.crm.invoice.service.ConfigureInvoiceServerService;
import com.trilogy.app.crm.invoice.service.ERLoggingRMIConfigureInvoiceServerServiceClient;
import com.trilogy.app.crm.invoice.service.ERLoggingRMIInvoiceRunServiceClient;
import com.trilogy.app.crm.invoice.service.ERLoggingRMIInvoiceServerServiceClient;
import com.trilogy.app.crm.invoice.service.InvoiceRunService;
import com.trilogy.app.crm.invoice.service.InvoiceServerService;
import com.trilogy.app.crm.invoice.service.InvoiceServiceSupport;
import com.trilogy.app.crm.invoice.service.LoggingConfigureInvoiceServerService;
import com.trilogy.app.crm.invoice.service.LoggingInvoiceRunService;
import com.trilogy.app.crm.invoice.service.LoggingInvoiceServerService;
import com.trilogy.app.crm.move.dependency.factory.CRMMoveDependencyManagerFactory;
import com.trilogy.app.crm.move.dependency.factory.MoveDependencyManagerFactory;
import com.trilogy.app.crm.move.processor.AccountMoveConsumerAgent;
import com.trilogy.app.crm.move.processor.ConvertAccountGroupTypeConsumerAgent;
import com.trilogy.app.crm.move.processor.ConvertAccountGroupTypeProducerAgent;
import com.trilogy.app.crm.move.processor.AcountMoveProducerAgent;
import com.trilogy.app.crm.move.processor.factory.CRMMoveProcessorFactory;
import com.trilogy.app.crm.move.processor.factory.MoveProcessorFactory;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.request.factory.CRMMoveRequestFactory;
import com.trilogy.app.crm.move.request.factory.MoveRequestFactory;
import com.trilogy.app.crm.notification.ConcurrentNotificationAgent;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.poller.agent.PollerInstall;
import com.trilogy.app.crm.provision.AccountServiceServer;
import com.trilogy.app.crm.provision.BASServiceServer;
import com.trilogy.app.crm.provision.SubscriberServiceServer;
import com.trilogy.app.crm.provision.corba.ECareServiceFactoryService;
import com.trilogy.app.crm.provision.corba.LanguageSupportServiceFactoryService;
import com.trilogy.app.crm.provision.corba.api.ecareservices.AccountServicesCorbaServer;
import com.trilogy.app.crm.provision.corba.api.ecareservices.BillingServicesCorbaServer;
import com.trilogy.app.crm.provision.gateway.ServiceProvisionGatewayClient;
import com.trilogy.app.crm.provision.gateway.ServiceProvisionGatewayOldCorbaClient;
import com.trilogy.app.crm.provision.soap.WebServiceServer;
import com.trilogy.app.crm.provision.xgen.AccountService;
import com.trilogy.app.crm.provision.xgen.BASService;
import com.trilogy.app.crm.provision.xgen.RMIAccountService;
import com.trilogy.app.crm.provision.xgen.RMIAccountServiceServer;
import com.trilogy.app.crm.provision.xgen.RMIBASService;
import com.trilogy.app.crm.provision.xgen.RMIBASServiceServer;
import com.trilogy.app.crm.provision.xgen.RMISubscriberService;
import com.trilogy.app.crm.provision.xgen.RMISubscriberServiceServer;
import com.trilogy.app.crm.provision.xgen.SubscriberService;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandlerFactory;
import com.trilogy.app.crm.subscriber.charge.handler.DefaultChargeRefundHandlerFactory;
import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.app.crm.transfer.contract.RMIAgreementServiceExternalClient;
import com.trilogy.app.crm.transfer.contract.RMITransferContractServiceExternalClient;
import com.trilogy.app.crm.transfer.contract.RMITransferGroupServiceExternalClient;
import com.trilogy.app.crm.transfer.contract.RMITransferTypeServiceExternalClient;
import com.trilogy.app.crm.transfer.contract.TransferAgreementFacade;
import com.trilogy.app.crm.transfer.contract.TransferContractFacade;
import com.trilogy.app.crm.transfer.contract.TransferContractGroupFacade;
import com.trilogy.app.crm.transfer.contract.TransferTypeFacade;
import com.trilogy.app.crm.transfer.contract.tfa.RMITransferAgreementImpl;
import com.trilogy.app.crm.transfer.contract.tfa.RMITransferContractGroupImpl;
import com.trilogy.app.crm.transfer.contract.tfa.RMITransferContractImpl;
import com.trilogy.app.crm.transfer.contract.tfa.RMITransferTypeImpl;
import com.trilogy.app.crm.transfer.membergroup.RMIMemberGroupExternalService;
import com.trilogy.app.crm.voicemail.VoiceMailServer;
import com.trilogy.app.crm.voicemail.client.VoiceMailClientFactory;
import com.trilogy.app.crm.web.agent.RestrictWebUserAuthSPI;
import com.trilogy.app.transferfund.rmi.api.agreement.AgreementService;
import com.trilogy.app.transferfund.rmi.api.membergroup.MemberGroupService;
import com.trilogy.app.transferfund.rmi.api.transfercontract.TransferContractService;
import com.trilogy.app.transferfund.rmi.api.transfergroup.TransferGroupService;
import com.trilogy.app.transferfund.rmi.api.transfertype.TransferTypeService;
import com.trilogy.framework.application.RemoteApplication;
import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.core.platform.Ports;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.rmi.RMIProperty;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.service.etl.poller.agent.ServiceEtlPollerModuleInstaller;
/**
 * This class installs the services used by the app. The services include Corba
 * connections to ECP, URS, SMSB and IPCW. This class also creates the RMI servers
 * (services) offered by CRM for the outside world.
 *
 * @author candy.wong@redknee.com
 */
public class ServiceInstall extends CoreSupport implements ContextAgent
{

    /**
     * Installs all the services for CRM.
     *
     * @param ctx
     *            A context
     * @exception AgentException
     *                thrown if one of the services fails to initialize
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        ctx.put(AuthSPI.class, new RestrictWebUserAuthSPI((AuthSPI)ctx.get(AuthSPI.class)));
        
        installChargerHandlerFactory(ctx);
        
        installMoveFeature(ctx);

        installAUDI(ctx);
        
        // installs the cached prefix map in BAS. DestinationZoneSupport call this
        // service,
        // and only walks the home if this service proves to be null (ECARE). OTOH nobody
        // should try to match this on the ECARE side.
        if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || !DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
        {
            ctx.put(PrefixService.class, new PrefixServiceImpl(ctx));
        }

        PromotionFactory.initialize(ctx);

        ctx.put(TPSPipeConstant.PIPELINE_SUBSCRIBER_BILL_KEY, new TransactionContextPresetAgent(
                new SubscriberInvoiceLookupAgent(new InvoiceLookupAgent(new SubscriberTotalOwingComputeAgent(
                    new AccountTotalOwingComputeAgent(new TaxrateLookupAgent(new CurrencyLookupAgent(
                        new SubscriberTaxCalculateAgent(new AccountTaxCalculateAgent(new SubscriberPaymentCalculateAgent(
                            new AccountPaymentCalculateAgent(
                                new SubscriberUpdateUpsAgent(new UpdateTransactionAgent(null
                                   ))))))))))))));

        ctx.put(TPSPipeConstant.PIPELINE_SUBSCRIBER_DEPOSIT_KEY, new TransactionContextPresetAgent(
                new CurrencyLookupAgent(
                    new UpdateTransactionAgent(null))));
        

        installServiceEtlPollers(ctx);

        // installs, configures and starts the pollers
        if (!DeploymentTypeSupportHelper.get(ctx).isBas(ctx))
        {
            new PollerInstall().execute(ctx);
        }

        installTFARMIServices(ctx);

        // Install the lifecycle agent that propagates TPS provisioning.
        new TPSProvisioningLifecycleAgent(ctx).execute(ctx);

        // install soap service
        installSoapService(ctx);

        installSOAPClients(ctx);

        // configures the RMI servers for external provisioning interfaces
        installRMIServices(ctx);

        // call categorization service
        ctx.put(CallCategorization.class, new CallCategorizationImpl());

        // put instance in context
        ctx.put(HistoryEventSupport.class, new HistoryEventSupport(ctx));

        // Install Voicemail service
        installVoicemailService(ctx);

        // DZ Install the HLR Provisioning Gateway Service
        final HLRProvisioningGateway hlrGateway = (HLRProvisioningGateway) ctx.get(HLRProvisioningGateway.class);
        RMIHLRProvisioningExternalService provisioningExternalService = new RMIHLRProvisioningExternalService(ctx,
                hlrGateway.getHostname(), hlrGateway.getPort(), hlrGateway.getServiceName());
        ctx.put(RMIHLRProvisioningExternalService.class, provisioningExternalService);
        SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, provisioningExternalService);
         
        // Install AccountHierarchy service
        ctx.put(AccountHierarchyService.class, new AccountHierarchyServiceImpl(ctx));

        installInvoiceServerClients(ctx);
        
        installCorbaClients(ctx);
        
        try
        {
            UrcsClientInstall.INSTANCE.execute(ctx);
        }
        catch (AgentException exception)
        {
            LogSupport.major(ctx, this, "Failure detected during installation of URCS clients.", exception);
        }

        installCorbaServers(ctx);

        RBTClientFactory.installClient(ctx);

        installAlcatelSSCClient(ctx);

        installServiceBlackberryClient(ctx);
        
        installGenericEntityApiService(ctx);
        
        installAynchronousConfigshareRequestPushFeature(ctx);
        
        try
        {
            ServiceAptiloInstall.execute(ctx);
        }
        catch (Exception e)
        {
            new MajorLogMsg(this, "Failed to install Aptilo Services.", e).log(ctx);

            throw new AgentException(e);
        }
        
        
    }

	private void installAynchronousConfigshareRequestPushFeature(Context ctx) {
		
        SystemFeatureThreadpoolConfig tpCfg = (SystemFeatureThreadpoolConfig) ctx.get(com.redknee.app.crm.bean.SystemFeatureThreadpoolConfig.class); 
        if(tpCfg == null)
        {
        	tpCfg = new SystemFeatureThreadpoolConfig();
        }
		
        ThreadPool pool = new ThreadPool(
	                "ASYNC CONFIGSHARE REQUEST PUSH", 
	                tpCfg.getAsynchronousConfigshareRequestPushQueueSize(), 
	                tpCfg.getAsynchronousConfigshareRequestThreadCount(), 
	                new ConcurrentConfigshareRequestPushAgent());
	        ctx.put(ASYNC_CONFIGSHARE_REQUEST_PUSH, pool);
		
	}

	private void installServiceEtlPollers(Context ctx) throws AgentException
    {
        new InfoLogMsg(this,"START Starting Service ETL Pollers",null).log(ctx);
        ServiceEtlPollerModuleInstaller.startPollers(ctx);
        new InfoLogMsg(this,"COMPLETED starting Service ETL Pollers",null).log(ctx);
        
    }
    
    private void installInvoiceServerClients(Context ctx)
    {
        InvoiceServerRemoteServicerConfig invoiceClientConfig = (InvoiceServerRemoteServicerConfig) ctx.get(InvoiceServerRemoteServicerConfig.class);
        
        PropertyChangeListener rmiPropertyListener = new InvoiceServerRMIServicePropertyChangeListener(ctx);
        invoiceClientConfig.addPropertyChangeListener(InvoiceServerRemoteServicerConfigXInfo.HOST_NAME.getName(), rmiPropertyListener);
        invoiceClientConfig.addPropertyChangeListener(InvoiceServerRemoteServicerConfigXInfo.BASE_PORT.getName(), rmiPropertyListener);
        
        RMIProperty prop;
        try
        {
            prop = (RMIProperty) XBeans.instantiate(RMIProperty.class, ctx);
        }
        catch (Exception e)
        {
            prop = new RMIProperty(); 
        }
        prop.setHost(invoiceClientConfig.getHostName());
        prop.setPort(Integer.valueOf(invoiceClientConfig.getBasePort()) + Ports.RMI_OFFSET);
        
        HomeSupport homeSupport = HomeSupportHelper.get(ctx);
        String invoiceServerServiceName = InvoiceServiceSupport.INVOICE_SERVER_SERVICE_NAME;
        String invoiceRunServiceName = InvoiceServiceSupport.INVOICE_RUN_SERVICE_NAME;
        String configureIsServiceName = InvoiceServiceSupport.CONFIGURE_IS_SERVICE_NAME;
        try
        {
            if (!homeSupport.hasBeans(ctx, RMIProperty.class, invoiceServerServiceName))
            {
                prop.setService(invoiceServerServiceName);
                homeSupport.createBean(ctx, prop);
            }
            else
            {
                prop.setService(invoiceServerServiceName);
                homeSupport.storeBean(ctx, prop);
            }
                
            ctx.put(InvoiceServerService.class, 
                    new LoggingInvoiceServerService(ctx, 
                            new ERLoggingRMIInvoiceServerServiceClient(ctx, invoiceServerServiceName)));
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Failed to install '" + invoiceServerServiceName + "' RMI client to Invoice Server.", e).log(ctx);
        }

        try
        {
            if (!homeSupport.hasBeans(ctx, RMIProperty.class, invoiceRunServiceName))
            {
                prop.setService(invoiceRunServiceName);
                homeSupport.createBean(ctx, prop);
            }
            else
            {
                prop.setService(invoiceRunServiceName);
                homeSupport.storeBean(ctx, prop);
            }
            
            ctx.put(InvoiceRunService.class, 
                    new LoggingInvoiceRunService(ctx, 
                            new ERLoggingRMIInvoiceRunServiceClient(ctx, invoiceRunServiceName)));
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Failed to install '" + invoiceRunServiceName + "' RMI client to Invoice Server.", e).log(ctx);
        }

        try
        {
            if (!homeSupport.hasBeans(ctx, RMIProperty.class, configureIsServiceName))
            {
                prop.setService(configureIsServiceName);
                homeSupport.createBean(ctx, prop);
            }
            else
            {
                prop.setService(configureIsServiceName);
                homeSupport.storeBean(ctx, prop);
            }
            
            ctx.put(ConfigureInvoiceServerService.class, 
                    new LoggingConfigureInvoiceServerService(ctx, 
                            new ERLoggingRMIConfigureInvoiceServerServiceClient(ctx, configureIsServiceName)));
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Failed to install '" + configureIsServiceName + "' RMI client to Invoice Server.", e).log(ctx);
        }
    }

    /**
     * @param ctx
     */
    private void installMoveFeature(Context ctx)
    {
        ctx.put(MoveDependencyManagerFactory.class, CRMMoveDependencyManagerFactory.instance());
        ctx.put(MoveProcessorFactory.class, CRMMoveProcessorFactory.instance());
        ctx.put(MoveRequestFactory.class, CRMMoveRequestFactory.instance());
        

        //Install Account Move Threadpool
        AcountMoveProducerAgent accountMoveRunAgent = new AcountMoveProducerAgent(ctx,
                new AccountMoveConsumerAgent(),
                "Account Move PROCESS",
                2,2);
        ctx.put(AcountMoveProducerAgent.class, accountMoveRunAgent);
        
        //Install Account Convert Threadpool
        SystemFeatureThreadpoolConfig tpCfg = (SystemFeatureThreadpoolConfig) ctx.get(com.redknee.app.crm.bean.SystemFeatureThreadpoolConfig.class); 
        if(tpCfg == null)
        {
        	tpCfg = new SystemFeatureThreadpoolConfig();
        }
        ConvertAccountGroupTypeProducerAgent<ConvertAccountGroupTypeRequest> accountConvertRunAgent = new ConvertAccountGroupTypeProducerAgent<ConvertAccountGroupTypeRequest>(ctx,
                new ConvertAccountGroupTypeConsumerAgent<ConvertAccountGroupTypeRequest>(),
                "Account Group Convert Process",
                tpCfg.getConvertToGroupQueueSize(),tpCfg.getConvertToGroupThreadCount());
        ctx.put(ConvertAccountGroupTypeProducerAgent.class, accountConvertRunAgent);

    }

    /**
     * installs all the RMI Clients and Services for TransferContract and Contract group logic.
     * @param ctx
     */
    private void installTFARMIServices(Context ctx)
    {
        final TfaRmiConfig config = (TfaRmiConfig)ctx.get(TfaRmiConfig.class);
        
        if( config != null )
        {
            RMITransferGroupServiceExternalClient rmiContractrGrouService = new RMITransferGroupServiceExternalClient(ctx,
                    config.getHostname(),
                    config.getPort(),
                    config.getContractGroupServiceName());
            RMITransferContractServiceExternalClient contractService = new RMITransferContractServiceExternalClient(ctx,
                    config.getHostname(),
                    config.getPort(),
                    config.getTransferContractServiceName());
            RMITransferTypeServiceExternalClient transferTypeService = new RMITransferTypeServiceExternalClient(ctx,
                    config.getHostname(),
                    config.getPort(),
                    config.getTransferTypeServiceName());
            RMIAgreementServiceExternalClient transferAgreemtService = new RMIAgreementServiceExternalClient(ctx,
                    config.getHostname(),
                    config.getPort(),
                    config.getTransferAgreementServiceName());
            
    
            final TransferContractFacade service = new RMITransferContractImpl();
            final TransferContractGroupFacade groupService = new RMITransferContractGroupImpl();
            final TransferTypeFacade transferTypeServ = new RMITransferTypeImpl();
            final TransferAgreementFacade trnferAgreementServ = new RMITransferAgreementImpl();
            
            ctx.put(TransferContractService.class, contractService);
            ctx.put(TransferGroupService.class, rmiContractrGrouService);
            ctx.put(TransferContractFacade.class, service);
            ctx.put(TransferContractGroupFacade.class, groupService);
            ctx.put(TransferTypeFacade.class, transferTypeServ);
            ctx.put(TransferTypeService.class, transferTypeService);
            ctx.put(TransferAgreementFacade.class, trnferAgreementServ);
            ctx.put(AgreementService.class, transferAgreemtService);
            

            
            // install member group service
            RMIMemberGroupExternalService rmiMemberGroupService = new RMIMemberGroupExternalService(
                    ctx,
                    config.getHostname(), 
                    config.getPort(), 
                    config.getMemberServiceName());
            
            // Add property change listener so that restart is not required due to TFA connection config changes
            config.addPropertyChangeListener(rmiMemberGroupService);
            
            ctx.put(MemberGroupService.class, rmiMemberGroupService);
            SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, rmiMemberGroupService);   
            SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, rmiContractrGrouService);   
            SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, contractService);   
            SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, transferTypeService);   
            SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, transferAgreemtService);   
        }
        else
        {
            new MinorLogMsg(this, "failed to install TFA client because no configuration exists", null).log(ctx);
        }
    }

    protected void installVoicemailService(Context ctx)
    {
        ctx.put(VoiceMailServer.class, new VoiceMailServer(ctx));
        new VoiceMailClientFactory().install(ctx); 
    }

    /**
     * Installs RMI services.
     *
     * @param ctx
     *            The operating context.
     * @throws AgentException
     *             Thrown if one or more services fail to initialize.
     */
    private void installRMIServices(final Context ctx) throws AgentException
    {
        try
        {
            final Context ctx1 = ctx.createSubContext();

            final RMIProperty rmiProp = new RMIProperty();
            rmiProp.setHost(CoreSupport.getHostname(ctx));
            rmiProp.setPort(CoreSupport.getPort(ctx, Ports.RMI_OFFSET));
            rmiProp.setService("AccountService");
            ctx1.put(RMIProperty.class, rmiProp);

            final AccountServiceServer acctServer = new AccountServiceServer(ctx1);
            final RMIAccountServiceServer account = new RMIAccountServiceServer(ctx1, acctServer, "AccountService");
            ctx.put(RMIAccountService.class, account);
            ctx.put(AccountService.class, acctServer);
            account.register();
        }
        catch (final RemoteException e)
        {
            throw new AgentException(e);
        }

        try
        {
            final Context ctx1 = ctx.createSubContext();

            final RMIProperty rmiProp = new RMIProperty();
            rmiProp.setHost(CoreSupport.getHostname(ctx));
            rmiProp.setPort(CoreSupport.getPort(ctx, Ports.RMI_OFFSET));
            ctx1.put(RMIProperty.class, rmiProp);

            final BASServiceServer basServer = new BASServiceServer(ctx1);
            final RMIBASServiceServer bas = new RMIBASServiceServer(ctx1, basServer, "BASService");
            ctx.put(RMIBASService.class, bas);
            ctx.put(BASService.class, basServer);
            bas.register();
        }
        catch (final IllegalArgumentException e1)
        {
            throw new AgentException(e1);
        }
        catch (final RemoteException e1)
        {
            throw new AgentException(e1);
        }

        final RemoteApplication basApp = retrieveRemoteBASAppConfig(ctx);
        final int basRemotePort;
        if (basApp != null)
        {
            basRemotePort = basApp.getBasePort() + Ports.RMI_OFFSET;
        }
        else
        {
            basRemotePort = Common.BAS_PORT;
        }

        try
        {
            final Context ctx1 = ctx.createSubContext();

            final RMIProperty rmiProp = new RMIProperty();
            rmiProp.setHost(CoreSupport.getHostname(ctx));
            rmiProp.setPort(CoreSupport.getPort(ctx, Ports.RMI_OFFSET));
            ctx1.put(RMIProperty.class, rmiProp);

            final SubscriberServiceServer subServer = new SubscriberServiceServer(ctx1);
            final RMISubscriberServiceServer sub = new RMISubscriberServiceServer(ctx1, subServer, "SubscriberService");

            ctx.put(RMISubscriberService.class, sub);
            ctx.put(SubscriberService.class, subServer);

            sub.register();
        }
        catch (final IllegalArgumentException e2)
        {
            throw new AgentException(e2);
        }
        catch (final RemoteException e2)
        {
            throw new AgentException(e2);
        }

        // TODO Dunning Process as RMI Server
        try
        {

            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx))
            {
                new InfoLogMsg(this, "installing Dunning Process Server", null).log(ctx);
                ctx.put(DunningProcess.class, new DunningProcessServer(ctx));
                Context serverCtx = ctx.createSubContext();

                RMIProperty rmiProp = new RMIProperty();
                rmiProp.setHost(CoreSupport.getHostname(ctx));
                rmiProp.setPort(CoreSupport.getPort(ctx, Ports.RMI_OFFSET));
                serverCtx.put(RMIProperty.class, rmiProp);

                final RMIDunningProcessServer server = new RMIDunningProcessServer(serverCtx, (DunningProcess) ctx
                    .get(DunningProcess.class), DunningProcess.class.getName());
                server.register();
                new InfoLogMsg(this, "registered Dunning Process RMI Server", null).log(ctx);
            }
            else if (DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
            {
                new InfoLogMsg(this, "installing Dunning Process Client", null).log(ctx);
                ctx.put(DunningProcess.class, new RMIDunningProcessClient(ctx, basApp.getHostname(), basRemotePort,
                    DunningProcess.class.getName()));
            }
            else
            {
                new InfoLogMsg(this, "installing Dunning Process", null).log(ctx);
                // single-node
                ctx.put(DunningProcess.class, new DunningProcessServer(ctx));
            }
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(this, "fail to install Dunning Process", t).log(ctx);
            throw new AgentException("fail to install Dunning Process", t);
        }

        final com.redknee.app.crm.api.rmi.agent.Install apiInstall = new com.redknee.app.crm.api.rmi.agent.Install();
        apiInstall.execute(ctx);
    }


    /**
     * Install CORBA services to context.
     *
     * @param ctx
     *            The operating context.
     */
    private void installCorbaServers(final Context ctx)
    {
        // It will not start
        if (DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
        {
            return;
        }

        // starts the provisioning corba server
        // needs to have the nameservice configured in the CorbaServer page
        try
        {
            new ECareServiceFactoryService(ctx).execute(ctx);
        }
        catch (final Throwable th)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, th.getMessage(), th).log(ctx);
            }
            // Install.failAndContinue(ctx, "ECareServiceFactoryService", th);
        }
        try
        {
            new LanguageSupportServiceFactoryService(ctx).execute(ctx);
        }
        catch (final Throwable th)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, th.getMessage(), th).log(ctx);
            }
            // Install.failAndContinue(ctx, "ECareServiceFactoryService", th);
        }

        try
        {
            new AccountServicesCorbaServer(ctx).execute(ctx);
        }
        catch (final Throwable th)
        {
            new InfoLogMsg(this, "Failed to start the Account Services. " + th.getMessage(), th).log(ctx);
        }

        try
        {
            new BillingServicesCorbaServer(ctx).execute(ctx);
        }
        catch (final Throwable th)
        {
            new InfoLogMsg(this, "Failed to start the Billing Services. " + th.getMessage(), th).log(ctx);
        }

        try
        {
            new CreditCardRetrievalService(ctx).execute(ctx);
        }
        catch (final Throwable th)
        {
            new InfoLogMsg(this, "Failed to start the Billing Services. " + th.getMessage(), th).log(ctx);
        }

    }


    /**
     * Install CORBA clients.
     *
     * @param ctx
     *            The operating context.
     */
    protected void installCorbaClients(final Context ctx)
    {
//        // failing connection to SMSB should be reported but should not stop the app.
//        try
//        {
//            ctx.put(AppSmsbClient.class, new AppSmsbClient(ctx));
//            SystemStatusSupport.registerExternalService(ctx, AppSmsbClient.class);
//        }
//        catch (final Throwable e)
//        {
//            Install.failAndContinue(ctx, "AppSmsbClient", e);
//        }
//
//        // failing connection to ECP should be reported but should not stop the app.
//        try
//        {
//            ctx.put(AppEcpClient.class, new AppEcpClientImpl(ctx));
//            SystemStatusSupport.registerExternalService(ctx, AppEcpClient.class);
//        }
//        catch (final Throwable e)
//        {
//            Install.failAndContinue(ctx, "AppEcpClient", e);
//        }

        try
        {
            VpnClient vpnClient = new VpnClient(ctx);
            ctx.put(VpnClient.class, vpnClient);
            SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, vpnClient);
        }
        catch (final Throwable e)
        {
            Install.failAndContinue(ctx, "AppVpnClient", e);
        }

        try
        {
            EcpRatePlanClientFactory ecpRatePlanClientFactory = new EcpRatePlanClientFactory(ctx);
            ctx.put(EcpRatePlanClient.class, ecpRatePlanClientFactory);
            SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, EcpRatePlanClient.class);
        }
        catch (final Throwable e)
        {
            Install.failAndContinue(ctx, "AppEcpClient", e);
        }
     
        try
        {
           
            ctx.put(TFAAuxiliaryServiceClientFactory.class, TFAAuxiliaryServiceClientFactory.getInstance(ctx));
        }
        catch (final Throwable e)
        {
            Install.failAndContinue(ctx, "AppTFAClient", e);
        } 


        // failing connection to IPCG should be reported but should not stop the app.
        try
        {
            IpcgClientFactory.installIpcgClient(ctx);
        }
        catch (final Throwable e)
        {
            Install.failAndContinue(ctx, "IpcgClient", e);
        }

        // this is an RMI client
        try
        {
            AAAClientFactory.installClient(ctx);
        }
        catch (final Throwable e)
        {
            Install.failAndContinue(ctx, "AAAClient", e);
        }

        // failing connection to OCG should be reported but should not stop the app.
//        try
//        {
//            ctx.put(AppOcgClient.class, new AppOcgClient(ctx));
//            SystemStatusSupport.registerExternalService(ctx, AppOcgClient.class);
//        }
//        catch (final Throwable e)
//        {
//            Install.failAndContinue(ctx, "AppOcgClient", e);
//        }

        // failing connection to balance management should be reported but should not stop the app.
//        try
//        {
//            BalanceManagementSupport.installSubscriberProfileProvisionClient(ctx);
//        }
//        catch (Throwable e)
//        {
//            Install.failAndContinue(ctx, "BalanceManagement", e);
//        }

        // failing connection to balance management should be reported but should not stop the app.
//        try
//        {
//            UrcsPromotionSupport.installPromotionClients(ctx);
//        }
//        catch (Throwable e)
//        {
//            Install.failAndContinue(ctx, "URCS Promotions", e);
//        }

        // HomezoneClient used for testing
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Going to install AppHomezoneClient", null);
            }
            AppHomezoneClient appHomezoneClient = new AppHomezoneClient(ctx);
            ctx.put(AppHomezoneClient.class, appHomezoneClient);
            SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, appHomezoneClient);
            if (LogSupport.isDebugEnabled(ctx))
            {
                if (ctx.get(com.redknee.app.crm.client.AppHomezoneClient.class) == null)
                {
                    new DebugLogMsg(this, "Installing AppHomezoneClient was unsuccessfull, got null in context", null);
                }
            }

        }
        catch (final Throwable e)
        {
            Install.failAndContinue(ctx, "AppHomezoneClient", e);
        }

//        try
//        {
//            if (LogSupport.isDebugEnabled(ctx))
//            {
//                new DebugLogMsg(this, "Going to install AppPinManagerClient", null);
//            }
//            ctx.put(AppPinManagerClient.class, new AppPinManagerCorbaClient(ctx));
//            SystemStatusSupport.registerExternalService(ctx, AppPinManagerClient.class);
//            if (LogSupport.isDebugEnabled(ctx))
//            {
//                if (ctx.get(AppPinManagerClient.class) == null)
//                {
//                    new DebugLogMsg(this, "Installing AppPinManagerClient was unsuccessful, got null in context", null);
//                }
//            }
//            if ( !ctx.has(UserAndGroupAuthSPI.class) )
//            {
//                ctx.put(UserAndGroupAuthSPI.class, new UserAndGroupAuthSPI(ctx));
//            }
//            
//        }
//        catch (final Throwable e)
//        {
//            Install.failAndContinue(ctx, "AppPinManagerClient", e);
//        }
        
//        try
//        {
//            if (LogSupport.isDebugEnabled(ctx))
//            {
//                new DebugLogMsg(this, "Going to install PricePlanMgmtClient", null);
//            }
//            ctx.put(PricePlanMgmtClient.class, new PricePlanMgmtCorbaClient(ctx));
//            SystemStatusSupport.registerExternalService(ctx, PricePlanMgmtClient.class);
//            if (LogSupport.isDebugEnabled(ctx))
//            {
//                if (ctx.get(PricePlanMgmtClient.class) == null)
//                {
//                    new DebugLogMsg(this, "Installing PricePlanMgmtClient was unsuccessfull, got null in context", null);
//                }
//            }
//        }
//        catch (final Throwable exception)
//        {
//            Install.failAndContinue(ctx, PricePlanMgmtClient.class.getSimpleName(), exception);
//        }

      try
      {
          if (LogSupport.isDebugEnabled(ctx))
          {
              new DebugLogMsg(this, "Going to install ServiceProvisionGatewayCorbaClient", null);
          }
          ctx.put(ServiceProvisionGatewayClient.class, new ServiceProvisionGatewayOldCorbaClient(ctx));
          SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, ServiceProvisionGatewayClient.class);
          if (LogSupport.isDebugEnabled(ctx))
          {
              if (ctx.get(ServiceProvisionGatewayClient.class) == null)
              {
                  new DebugLogMsg(this, "Installing ServiceProvisionGatewayCorbaClient was unsuccessfull, got null in context", null);
              }
          }
      }
      catch (final Throwable exception)
      {
          Install.failAndContinue(ctx, ServiceProvisionGatewayClient.class.getSimpleName(), exception);
      }
    }



    /**
     * Install AUDI tools.
     *
     * @param ctx
     *            The operating context.
     */
    private void installAUDI(final Context ctx)
    {
        ctx.put(AUDILoadLogicProcess.class, new AUDIProcess(ctx));
        ctx.put(AUDIUpdateLogicProcess.class, new AUDIUpdateProcess(ctx));
        ctx.put(AUDIDeleteLogicProcess.class, new AUDIDeleteProcess(ctx));
    }


    /**
     * Install SOAP services.
     *
     * @param ctx
     *            The operating context.
     */
    private void installSoapService(final Context ctx)
    {
        try
        {
            final WebServiceServer soapServer = new WebServiceServer(ctx);

            soapServer.execute(ctx);
        }
        catch (final AgentException ae)
        {
            new MajorLogMsg(this, ae.getMessage(), ae).log(ctx);
        }
    }

    /**
     *  Install SOAP Clients
     *  
     * @param ctx
     *            The operating context.
     */
    protected void installSOAPClients(final Context ctx)
    {
        try
        {
            ctx.put(AppTFAClient.class, new AppTFAClient(ctx));
        }
        catch(Exception e)
        {
            Install.failAndContinue(ctx, "AppTFAClient", e);
        }
        
        try
        {
            ctx.put(AppNGRCClient.class, new AppNGRCClientPM(ctx, 
                                            new AppNGRCClientDebug(ctx, 
                                                new AppNGRCClientImpl(ctx))));
        }
        catch(Exception e)
        {
            Install.failAndContinue(ctx, "AppNGRCClient", e);
        }        
    }

    /**
     * Retrieves BAS configuration from context.
     *
     * @param ctx
     *            The operating context.
     * @return Remote BAS configuration.
     */
    protected static RemoteApplication retrieveRemoteBASAppConfig(final Context ctx)
    {
        return (RemoteApplication) ctx.get(Common.BAS_APPNAME);
    }

    /**
     * Install the Alcatel SSC Client under the key AlcatelProvisioning.class
     * @param ctx
     */
    private void installAlcatelSSCClient(Context ctx) 
    {
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, TestAlcatelProvisioningImpl.LICENSE))
        {
            // Install the test client
            ctx.put(AlcatelProvisioning.class, new TestAlcatelProvisioningImpl());
        }
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.ALCATEL_LICENSE))
        {
            ctx.put(AlcatelProvisioning.class, new AlcatelSSCProvisioningImpl());
        }
    }
    
    private void installServiceBlackberryClient(Context ctx) throws AgentException
    {
        try
        {
            ServiceBlackberryInstall.execute(ctx);
        }
        catch (Exception e)
        {
            new MajorLogMsg(this, "Failed to install Blackberry Services.", e).log(ctx);

            throw new AgentException(e);
        }
    }

    private void installChargerHandlerFactory(Context ctx)
	{
       DefaultChargeRefundHandlerFactory chargingHandler = new DefaultChargeRefundHandlerFactory(ctx);
       ctx.put(ChargeRefundResultHandlerFactory.class, chargingHandler);

	}
    
    
    private void installGenericEntityApiService(Context ctx) {
    	
    	ctx.put(DefaultGenericEntityAdapter.class, new DefaultGenericEntityAdapter());
    	ctx.put(CustomMsisdnGenericEntityAdapter.class, new CustomMsisdnGenericEntityAdapter());
    	ctx.put(DefaultGenericEntityValidator.class, new DefaultGenericEntityValidator());
    	ctx.put(MsisdnGenericEntityValidator.class, new MsisdnGenericEntityValidator());
    	ctx.put(ATUGenericEntityValidator.class, new ATUGenericEntityValidator());
    	ctx.put(ScheduledPricePlanGenericEntityValidator.class, new ScheduledPricePlanGenericEntityValidator());
    	ctx.put(CustomSubModificationScheduleGenericEntityAdapter.class, new CustomSubModificationScheduleGenericEntityAdapter());
    	ctx.put(CustomUserGenericEntityAdapter.class, new CustomUserGenericEntityAdapter());
    	ctx.put(DefaultGenericEntityInterceptor.class, new DefaultGenericEntityInterceptor());
    	ctx.put(com.redknee.app.crm.api.generic.entity.adapter.CustomPackageGenericEntityAdapter.class, 
    			new com.redknee.app.crm.api.generic.entity.adapter.CustomPackageGenericEntityAdapter());
    	ctx.put(AddressGenericEntityValidator.class, new AddressGenericEntityValidator());
    	ctx.put(ExternalCreditCheckValidator.class, new ExternalCreditCheckValidator());
        ctx.put(ExternalCreditCheckRetrieveValidator.class, new ExternalCreditCheckRetrieveValidator());
		ctx.put(ExternalCreditCheckAdapter.class, new ExternalCreditCheckAdapter());
		ctx.put(DisputeCreationAdapter.class, new DisputeCreationAdapter());
		ctx.put(DisputeCreationValidator.class, new DisputeCreationValidator());
		ctx.put(DisputeUpdationAdapter.class, new DisputeUpdationAdapter());
		ctx.put(DisputeUpdationValidator.class, new DisputeUpdationValidator());
	}
    
    public static final String ASYNC_CONFIGSHARE_REQUEST_PUSH = "ASYNC_CONFIGSHARE_REQUEST_PUSH";



}
