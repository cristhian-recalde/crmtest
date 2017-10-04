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
package com.trilogy.app.crm.api.rmi.agent;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.trilogy.app.crm.api.queryexecutor.QueryExecutorsInvocationHandler;
import com.trilogy.app.crm.api.rmi.impl.AccountsImpl;
import com.trilogy.app.crm.api.rmi.impl.ApiAuthImpl;
import com.trilogy.app.crm.api.rmi.impl.CallingGroupsImpl;
import com.trilogy.app.crm.api.rmi.impl.GeneralProvisioningImpl;
import com.trilogy.app.crm.api.rmi.impl.GracefulShutdownGenericProxy;
import com.trilogy.app.crm.api.rmi.impl.ServicesAndBundlesImpl;
import com.trilogy.app.crm.api.rmi.impl.SubscribersImpl;
import com.trilogy.app.crm.integration.pc.CsServiceImpl;
import com.trilogy.app.crm.api.rmi.impl.PaymentSupportImpl;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.rmi.RMIRegistry;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.ToSkeletonGenericProxy;
import com.trilogy.util.crmapi.wsdl.v2_1.api.ApiAuth;
import com.trilogy.util.crmapi.wsdl.v3_0.api.AccountService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.AccountServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.BundleQueryService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.BundleQueryServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.BundleQueryServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CallDetailService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CallDetailServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CallDetailServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CallingGroupService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CallingGroupServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CardPackageService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CardPackageServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CardPackageServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CommandInteractionService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CommandInteractionServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CommandInteractionServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CsService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CsServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CsServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.DepositManagementService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.DepositManagementServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.DepositManagementServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.DisputeManagementService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.DisputeManagementServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.DisputeManagementServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.GeneralProvisioningService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.GeneralProvisioningServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.InvoiceService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.InvoiceServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.InvoiceServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.LoyaltyService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.LoyaltyServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.LoyaltyServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.MobileNumberService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.MobileNumberServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.MobileNumberServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.NoteService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.NoteServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.NoteServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PaymentGatewayIntegrationService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PaymentGatewayIntegrationServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PaymentGatewayIntegrationServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PaymentSupportService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PaymentSupportServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PinManagementService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PinManagementServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PinManagementServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.ProvisioningCommandService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.ProvisioningCommandServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.ProvisioningCommandServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.QuotationService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.QuotationServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.QuotationServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.ServicesAndBundlesService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.ServicesAndBundlesServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.SubscriptionService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.SubscriptionServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.TCBGeneralIntegrationService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.TCBGeneralIntegrationServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.TCBGeneralIntegrationServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.TransactionService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.TransactionServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.TransactionServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.EnterpriseIntegrationService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.EnterpriseIntegrationServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.EnterpriseIntegrationServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.GenericEntityService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.GenericEntityServiceProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.api.GenericEntityServiceSkeletonInterface;

/**
 * Registers all API interfaces.
 *
 * @author victor.stratan@redknee.com
 */
public class Install implements ContextAgent
{
    public void execute(Context ctx) throws AgentException
    {
        try
        {
            registerApiService(ctx, 
                    ApiAuth.class, 
                    new ApiAuthImpl(ctx));
            
            registerApiService(ctx, 
                    AccountService.class, 
                    new AccountServiceProxy(
                            ToSkeletonGenericProxy.newInstance(ctx, 
                                    GracefulShutdownGenericProxy.newInstance(ctx, new AccountsImpl(ctx)), 
                                    AccountService.class)));
            
            registerApiService(ctx, 
                    BundleQueryService.class, 
                    new BundleQueryServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, BundleQueryServiceSkeletonInterface.class)), 
                                BundleQueryService.class)));
            
            registerApiService(ctx, 
                    CallDetailService.class, 
                    new CallDetailServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, CallDetailServiceSkeletonInterface.class)), 
                                CallDetailService.class)));
            
            registerApiService(ctx, 
                    CallingGroupService.class, 
                    new CallingGroupServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, new CallingGroupsImpl(ctx)), 
                                CallingGroupService.class)));
            
            registerApiService(ctx, 
                    CardPackageService.class, 
                    new CardPackageServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, CardPackageServiceSkeletonInterface.class)),
                                CardPackageService.class)));
            
            registerApiService(ctx, 
                    CommandInteractionService.class, 
                    new CommandInteractionServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, CommandInteractionServiceSkeletonInterface.class)), 
                                CommandInteractionService.class)));
            
            registerApiService(ctx, 
                    GeneralProvisioningService.class, 
                    new GeneralProvisioningServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, new GeneralProvisioningImpl(ctx)), 
                                GeneralProvisioningService.class)));
            
            registerApiService(ctx, 
                    InvoiceService.class, 
                    new InvoiceServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, InvoiceServiceSkeletonInterface.class)), 
                                InvoiceService.class)));
            
            registerApiService(ctx, 
                    MobileNumberService.class, 
                    new MobileNumberServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, MobileNumberServiceSkeletonInterface.class)), 
                                MobileNumberService.class)));
            
            registerApiService(ctx, 
                    NoteService.class, 
                    new NoteServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, NoteServiceSkeletonInterface.class)), 
                                NoteService.class)));
            
            registerApiService(ctx, 
                    ServicesAndBundlesService.class, 
                    new ServicesAndBundlesServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, new ServicesAndBundlesImpl(ctx)), 
                                ServicesAndBundlesService.class)));
            
            registerApiService(ctx, 
                    SubscriptionService.class, 
                    new SubscriptionServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, new SubscribersImpl(ctx)), 
                                SubscriptionService.class)));
            
            registerApiService(ctx, 
                    TransactionService.class, 
                    new TransactionServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, TransactionServiceSkeletonInterface.class)), 
                                TransactionService.class)));
            
            registerApiService(ctx, 
                    LoyaltyService.class, 
                    new LoyaltyServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, LoyaltyServiceSkeletonInterface.class)),
                                LoyaltyService.class)));
            
            registerApiService(ctx, 
                    PinManagementService.class, 
                    new PinManagementServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, PinManagementServiceSkeletonInterface.class)), 
                                        PinManagementService.class)));
            
            registerApiService(ctx, 
                    PaymentGatewayIntegrationService.class, 
                    new PaymentGatewayIntegrationServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, PaymentGatewayIntegrationServiceSkeletonInterface.class)), 
                                        PaymentGatewayIntegrationService.class)));
            
            registerApiService(ctx, 
                    DisputeManagementService.class, 
                    new DisputeManagementServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, DisputeManagementServiceSkeletonInterface.class)), 
                                        DisputeManagementService.class)));
            
            registerApiService(ctx, 
                    TCBGeneralIntegrationService.class, 
                    new TCBGeneralIntegrationServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, TCBGeneralIntegrationServiceSkeletonInterface.class)), 
                                        TCBGeneralIntegrationService.class)));

            registerApiService(ctx, 
                    EnterpriseIntegrationService.class, 
                    new EnterpriseIntegrationServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, EnterpriseIntegrationServiceSkeletonInterface.class)), 
                                        EnterpriseIntegrationService.class)));            
            
            registerApiService(ctx, 
                    GenericEntityService.class, 
                    new GenericEntityServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, GenericEntityServiceSkeletonInterface.class)), 
                                        GenericEntityService.class)));
            
            registerApiService(ctx, 
                    ProvisioningCommandService.class, 
                    new ProvisioningCommandServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, ProvisioningCommandServiceSkeletonInterface.class)), 
                                        ProvisioningCommandService.class)));
            
            registerApiService(ctx, 
            		CsService.class, 
                    new CsServiceProxy(
                            ToSkeletonGenericProxy.newInstance(ctx, 
                                    GracefulShutdownGenericProxy.newInstance(ctx, new CsServiceImpl(ctx)), 
                                    CsService.class)));
            
            /*registerApiService(ctx, 
            		CsService.class, 
                    new CsServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, CsServiceSkeletonInterface.class)), 
                                CsService.class)));*/
            
            registerApiService(ctx, 
            		DepositManagementService.class, 
                    new DepositManagementServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, DepositManagementServiceSkeletonInterface.class)), 
                                        DepositManagementService.class)));
            
            registerApiService(ctx, 
            		QuotationService.class, 
                    new QuotationServiceProxy(
                        ToSkeletonGenericProxy.newInstance(ctx, 
                                GracefulShutdownGenericProxy.newInstance(ctx, 
                                        QueryExecutorsInvocationHandler.newInstance(ctx, QuotationServiceSkeletonInterface.class)), 
                                        QuotationService.class)));
            
            registerApiService(ctx, 
            		PaymentSupportService.class, 
                    new PaymentSupportServiceProxy(
                            ToSkeletonGenericProxy.newInstance(ctx, 
                                    GracefulShutdownGenericProxy.newInstance(ctx, new PaymentSupportImpl(ctx)), 
                                    PaymentSupportService.class)));
            
        }
        catch (IllegalArgumentException e)
        {
            LogSupport.crit(ctx, this, "Unable to register API interface", e);
            throw new AgentException(e);
        }
        catch (RemoteException e)
        {
            LogSupport.crit(ctx, this, "Unable to register API interface", e);
            throw new AgentException(e);
        }
    }
    
    public static <T> void registerApiService(Context ctx, Class<T> ifc, T service) throws AgentException
    {
        if (service instanceof Remote)
        {
            try
            {
                final String name = getServiceName(ifc);

                ctx.put(name, service);
                ctx.put(ifc, service);

                ((RMIRegistry)ctx.get(RMIRegistry.class)).register(name, (Remote) service);
            }
            catch (IllegalArgumentException e)
            {
                LogSupport.crit(ctx, Install.class, "Unable to register API interface", e);
                throw new AgentException(e);
            }
            catch (RemoteException e)
            {
                LogSupport.crit(ctx, Install.class, "Unable to register API interface", e);
                throw new AgentException(e);
            }
        }
        else
        {
            throw new AgentException("Service class must implement java.rmi.Remote");
        }
    }

    private static String getServiceName(final Class aClass)
    {
        final String name = "api." + aClass.getSimpleName();
        return name;
    }
}
