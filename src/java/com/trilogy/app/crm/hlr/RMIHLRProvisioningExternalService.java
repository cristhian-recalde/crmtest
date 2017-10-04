/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.hlr;

import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.interfaces.crm.hlr.RMIHLRProvisioningClient;


/**
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class RMIHLRProvisioningExternalService extends RMIHLRProvisioningClient implements RemoteServiceStatus
{

    private static final String SERVICE_DESCRIPTION = "RMI client for HLR provisioning interface";

    public RMIHLRProvisioningExternalService(Context ctx, String hostname, int port, String service)
    {
        super(ctx, hostname, port, service);
    }

    public RMIHLRProvisioningExternalService(Context ctx, String service) throws HomeException
    {
        super(ctx, service);
    }

    public RMIHLRProvisioningExternalService(Context ctx) throws IllegalArgumentException
    {
        super(ctx);
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getRemoteInfo()
     */
    public String getRemoteInfo()
    {
        if( this.getHostname() != null && this.getHostname().trim().length() > 0 )
        {
            return getHostname() + ":" + getPort();
        }
        return "";
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceDescription()
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#isServiceAlive()
     */
    public boolean isAlive()
    {
        return connected_;
    }

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(getRemoteInfo(), isAlive());
    }

    @Override
    public String getName()
    {
        return getServiceName();
    }

    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }
    

}
