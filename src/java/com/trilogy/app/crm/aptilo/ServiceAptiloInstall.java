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
package com.trilogy.app.crm.aptilo;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.client.ServiceAptiloClient;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.web.service.SystemStatusRequestServicer;

import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.service.aptilo.ServiceAptiloFactory;
import com.trilogy.service.aptilo.model.ServiceParametersMappingHome;
import com.trilogy.app.crm.bean.CRMSpid;

/**
 * Service Aptilo Installation logic.
 * 
 * 
 * @author anuradha.malvadkar@redknee.com @9.7.2
 *
 */
public class ServiceAptiloInstall 
{

    /**
     * Install the Aptilo Service in CRM.
     * The client serving the requests is controlled by Licensing.
     * @param ctx
     * @throws IOException 
     * @throws HomeException 
     * @throws RemoteException 
     */
    public static void execute(Context ctx) throws RemoteException, HomeException, IOException
    {
        ServiceAptiloFactory.install(ctx);

       ServiceAptiloClient serviceAclient = new ServiceAptiloClient(ctx);
        ctx.put(ServiceAptiloClient.class, serviceAclient);
       SystemStatusSupportHelper.get(ctx).registerExternalService(ctx, ServiceAptiloClient.class);   

        Home home = ServiceAptiloFactory.getServiceParametersMapHome(ctx);
        home = new RMIClusteredHome(ctx, ServiceParametersMappingHome.class.getName(), home);
        home = new AptiloMappingRemovalValidationHome(ctx, home);

        ctx.put(ServiceParametersMappingHome.class, home);

        new InfoLogMsg(ServiceAptiloFactory.class, "Aptilo service installed in CRM context.", null).log(ctx);
     }

}
