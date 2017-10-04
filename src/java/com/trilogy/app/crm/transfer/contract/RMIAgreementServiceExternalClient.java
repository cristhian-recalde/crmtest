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
package com.trilogy.app.crm.transfer.contract;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.trilogy.app.crm.bean.TfaRmiConfigXInfo;
import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.transferfund.rmi.api.agreement.RMIAgreementServiceClient;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * An external service implementation for TFA Agreement so that it is visible on the system status screen
 */
public class RMIAgreementServiceExternalClient extends RMIAgreementServiceClient
        implements RemoteServiceStatus, PropertyChangeListener
{

    private static final String SERVICE_DESCRIPTION = "RMI client for transfer funds agreament provisioning interface";

    public RMIAgreementServiceExternalClient(Context ctx, String hostname, int port, String service)
    {
        super(ctx, hostname, port, service);
    }

    public RMIAgreementServiceExternalClient(Context ctx, String service) throws HomeException
    {
        super(ctx, service);
    }

    public RMIAgreementServiceExternalClient(Context ctx) throws IllegalArgumentException
    {
        super(ctx);
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getRemoteInfo()
     */
    public String getRemoteInfo()
    {
        if (this.getHostname() != null && this.getHostname().trim().length() > 0)
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

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        Object newValue = evt.getNewValue();

        // If a known property was updated, then make the client think it is disconnected so that
        // it will reconnect to the new service
        if (TfaRmiConfigXInfo.HOSTNAME.getName().equals(evt.getPropertyName())
                && newValue instanceof String)
        {
            super.setHostname((String) newValue);
            super.connected_ = false;
        }
        else if (TfaRmiConfigXInfo.PORT.getName().equals(evt.getPropertyName())
                && newValue instanceof Integer)
        {
            super.setPort((Integer) newValue);
            super.connected_ = false;
        }
        else if (TfaRmiConfigXInfo.TRANSFER_AGREEMENT_SERVICE_NAME.getName().equals(evt.getPropertyName())
                && newValue instanceof String)
        {
            super.setServiceName((String) newValue);
            super.connected_ = false;
        }
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