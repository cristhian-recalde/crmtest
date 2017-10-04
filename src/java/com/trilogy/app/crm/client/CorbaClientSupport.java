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
package com.trilogy.app.crm.client;

import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.util.corba.ConnectionListener;
import com.trilogy.util.corba.CorbaClientException;
import com.trilogy.util.corba.CorbaClientProxy;

/**
 * All the common elements to create and maintain a working CORBA Client Connection.
 *  
 * All the code was moved here from EcpBearerServiceCorbaClient.
 * 
 * @author angie.li@redknee.com
 * @author cindy.wong@redknee.com
 * @since CRM 8.2
 *
 */
public abstract class CorbaClientSupport extends ContextAwareSupport implements ConnectionListener, RemoteServiceStatus
{
    /**
     * Initializes the client.
     */
    public void init(final String clientName)
    {
        Throwable exceptionCaught = null;
        try
        {
            final Home corbaClientPropertyHome = (Home) getContext().get(CorbaClientPropertyHome.class);
            if (corbaClientPropertyHome == null)
            {
                final IllegalArgumentException exception = new IllegalArgumentException(
                    "Corba client configuration does not exist");
                throw exception;
            }
            this.corbaClientProperty_ = (CorbaClientProperty) corbaClientPropertyHome.find(getContext(), clientName);
            if (this.corbaClientProperty_ == null)
            {
                final IllegalArgumentException exception = new IllegalArgumentException(
                    "Configuration error: can not find CorbaClientProperty " + clientName);
                throw exception;
            }
            new InfoLogMsg(this, this.corbaClientProperty_.toString(), null).log(getContext());
            this.service_ = null;
            this.corbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(), this.corbaClientProperty_, this);
        }
        catch (final HomeException exception)
        {
            exceptionCaught = exception;
            new MinorLogMsg(this, exception.getMessage(), exception).log(getContext());
        }
        catch (final CorbaClientException exception)
        {
            exceptionCaught = exception;
            new MinorLogMsg(this, exception.getMessage(), exception).log(getContext());
        }
        catch (final IllegalArgumentException exception)
        {
            exceptionCaught = exception;
            new MinorLogMsg(this, exception.getMessage(), exception).log(getContext());
        }

        if (exceptionCaught != null)
        {
            // snmp external link down trap
            String host = "undefined";
            String port = "0";
            if (this.corbaClientProperty_ != null)
            {
                host = this.corbaClientProperty_.getNameServiceHost();
                port = String.valueOf(this.corbaClientProperty_.getNameServicePort());
            }
            new EntryLogMsg(10617L, this, this.toString(), null, new String[]
            {
                host, port,
            }, exceptionCaught).log(getContext());
            new MinorLogMsg(this, "Unable to load CORBA proxy for " + clientName, exceptionCaught).log(getContext());
        }
    }
    
    /**
     * Invalidates the proxy.
     *
     * @param throwable
     *            Exception thrown.
     */
    public synchronized void invalidate(final Throwable throwable)
    {
        // only raise a SNMP trap if this is the first time we discover the connection is
        // gone
        if (this.service_ != null)
        {
            new EntryLogMsg(10617L, this, "", this.corbaClientProperty_.toString(),
                new String[]
                {
                    this.corbaClientProperty_.getNameServiceHost(),
                    String.valueOf(this.corbaClientProperty_.getNameServicePort()),
                }, throwable).log(getContext());
        }
        this.corbaProxy_.invalidate();
        this.service_ = null;
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.urcs.RemoteServiceStatus#getConnectionStatus()
     */
    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(corbaClientProperty_, isAlive());
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.urcs.RemoteServiceStatus#getServiceStatus()
     */
    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAlive()
    {
        return this.isConnected_;
    }

    /**
     * {@inheritDoc}
     */
    public void connectionDown()
    {
        this.isConnected_ = false;
        this.service_ = null;
        // snmp external link down trap
        if (this.corbaClientProperty_ != null)
        {
            new EntryLogMsg(12719L, this, "", this.corbaClientProperty_.toString(), new String[]
            {
                this.corbaClientProperty_.getNameServiceHost(),
                String.valueOf(this.corbaClientProperty_.getNameServicePort()),
            }, null).log(getContext());
        }
    }


    /**
     * {@inheritDoc}
     */
    public void connectionUp()
    {
        new EntryLogMsg(12720L, this, "", this.corbaClientProperty_.toString(), new String[]
        {
            this.corbaClientProperty_.getNameServiceHost(),
            String.valueOf(this.corbaClientProperty_.getNameServicePort()),
        }, null).log(getContext());
        this.isConnected_ = true;
    }
    
    /**
     * CORBA client property.
     */
    protected CorbaClientProperty corbaClientProperty_;

    /**
     * CORBA service.  Needs to be cast later on.
     */
    protected Object service_;

    /**
     * CORBA proxy.
     */
    protected CorbaClientProxy corbaProxy_;

    /**
     * Whether the connection is up.
     */
    protected boolean isConnected_;
}
