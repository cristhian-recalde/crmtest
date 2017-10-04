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
package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.service.ServiceProvisioningError;
import com.trilogy.app.crm.bean.service.ServiceProvisioningErrorHome;
import com.trilogy.app.crm.extension.ExtensionAssociationHome;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtensionXInfo;
import com.trilogy.app.crm.extension.service.ServiceExtension;
import com.trilogy.app.crm.extension.service.ServiceExtensionXInfo;
import com.trilogy.app.crm.support.StorageSupportHelper;

import com.trilogy.framework.application.RemoteApplication;
import com.trilogy.framework.core.platform.Ports;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.LRUCachingHome;
import com.trilogy.framework.xhome.home.LastModifiedAwareHome;
import com.trilogy.framework.xhome.home.ValidatingHome;

/**
 * Creates the decorators for the SubscriberServices home pipeline.
 *
 * @author arturo.medina@redknee.com
 */
public class SubscriberServicesPipelineHome implements PipelineFactory
{

    public SubscriberServicesPipelineHome()
    {
        super();
    }

    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
        AgentException
    {
        final RemoteApplication basApp = StorageSupportHelper.get(ctx).retrieveRemoteBASAppConfig(ctx);
        final String server = basApp.getHostname();
        final int port = basApp.getBasePort() + Ports.RMI_OFFSET;

        /*
         * Please note that 500 means 500 services collections. So we will be keeping services for 500 subscribers not
         * just 500 service entries. So if you have about 5 services on average per subscriber then there will be about
         * 2500 records in memory
         */

        Home subscriberServicesHome = StorageSupportHelper.get(ctx).createHome(ctx, SubscriberServices.class, "SUBSCRIBERSERVICES");
        
        subscriberServicesHome = new ExtensionAssociationHome<ServiceExtension, SubscriberServices>(
                ctx, 
                ServiceExtension.class, 
                ServiceExtensionXInfo.SERVICE_ID, 
                SubscriberServicesXInfo.SERVICE_ID,
                Service.class,
                subscriberServicesHome);
        
        subscriberServicesHome = new SubscriberServicesPersonalizedFeeUpdateHome(ctx, subscriberServicesHome);
        subscriberServicesHome = new SubscriberServicesNextRecurChargeDateUpdateHome(ctx, subscriberServicesHome);
        subscriberServicesHome = new ServiceProvisioningErrorUpdateHome(subscriberServicesHome);
        subscriberServicesHome= new SubscriberServiceCountUpdateHome(ctx,subscriberServicesHome);
        subscriberServicesHome = new LRUCachingHome(ctx, SubscriberServices.class, true, subscriberServicesHome);
        final CompoundValidator validators = new CompoundValidator();

        // probably not needed here, since it *should* be validated in Subscriber pipeline already, but just in case.
        validators.add(SubscriberServicesDatesValidator.instance());

        subscriberServicesHome = new ValidatingHome(validators, subscriberServicesHome);

        //subscriberServicesHome = new PredicateAwareLRUCachingHome(ctx, SubscriberServices.class, false, subscriberServicesHome);
        
        ctx.put(SubscriberServicesHome.class, subscriberServicesHome);
        
        

        StorageSupportHelper.get(ctx).createRmiService(ctx, serverCtx, subscriberServicesHome, SubscriberServicesHome.class, server,
            port);

        createErrorPipeline(ctx, serverCtx, server, port);

        return subscriberServicesHome;
    }

    /**
     * Create the ServiceProvisionError pipeline 
     * @param ctx
     * @param serverCtx
     * @param server
     * @param port
     * @return
     * @throws RemoteException
     * @throws HomeException
     * @throws IOException
     * @throws AgentException
     */
    protected Home createErrorPipeline(Context ctx, Context serverCtx, String server, int port) throws RemoteException, HomeException, IOException,
    AgentException
    {
        Home errorHome = StorageSupportHelper.get(ctx).createHome(ctx, ServiceProvisioningError.class, "SERVICEPROVISIONINGERROR");
        errorHome = new LastModifiedAwareHome(errorHome);
        errorHome = new LRUCachingHome(ctx, ServiceProvisioningError.class, true, errorHome);

        ctx.put(ServiceProvisioningErrorHome.class, errorHome);
        
        StorageSupportHelper.get(ctx).createRmiService(ctx, serverCtx, errorHome, ServiceProvisioningError.class, server,
            port);
        
        return errorHome;
    }
}
