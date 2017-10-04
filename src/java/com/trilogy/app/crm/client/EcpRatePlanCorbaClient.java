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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.osa.ecp.provision.ErrorCode;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.service.rating.RatePlanInfo;
import com.trilogy.service.rating.RatePlanProvisioner;
import com.trilogy.service.rating.RatePlanProvisionerHelper;
import com.trilogy.service.rating.RatePlanProvisionerPackage.RatePlanInfoSetHolder;
import com.trilogy.util.corba.ConnectionUpException;
import com.trilogy.util.corba.CorbaClientException;


/**
 * ECP rate plan client.
 *
 * @author yassir.pakran@redknee.com
 */
// TODO Set all the entry log messages correctly.
public class EcpRatePlanCorbaClient extends ContextAwareSupport implements EcpRatePlanClient
{

    /**
     * Create a new instance of <code>EcpRatePlanCorbaClient</code>.
     *
     * @param ctx
     *            The operating context.
     */
    public EcpRatePlanCorbaClient(final Context ctx)
    {
        setContext(ctx);
        init();
    }


    /**
     * Initialize this ECP rate plan CORBA client.
     */
    private void init()
    {
        try
        {
            Home corbaClientPropertyHome = null;
            corbaClientPropertyHome = (Home) getContext().get(CorbaClientPropertyHome.class);
            if (corbaClientPropertyHome == null)
            {
                new MinorLogMsg(this, "Corba client configuration does not exist", null).log(getContext());
                throw new IllegalArgumentException("Corba client configuration does not exist");
            }
            this.ecpRatePlanProperty_ = (CorbaClientProperty) corbaClientPropertyHome.find(getContext(),
                "EcpRatePlanClient");
            if (this.ecpRatePlanProperty_ == null)
            {
                new MinorLogMsg(this, "Configuration error: can not find CorbaClientProperty EcpRatePlanClient", null)
                    .log(getContext());
                throw new IllegalArgumentException(
                    "Configuration error: can not find CorbaClientProperty EcpRatePlanClients");
            }
            new InfoLogMsg(this, this.ecpRatePlanProperty_.toString(), null).log(getContext());
            this.service_ = null;
            this.corbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(), this.ecpRatePlanProperty_, this);
        }
        catch (final Throwable e)
        {
            // snmp external link down trap
            String host = "undefined";
            String port = "0";
            if (this.ecpRatePlanProperty_ != null)
            {
                host = this.ecpRatePlanProperty_.getNameServiceHost();
                port = String.valueOf(this.ecpRatePlanProperty_.getNameServicePort());
            }
            new EntryLogMsg(10617L, this, this.toString(), null, new String[]
            {
                host, port,
            }, e).log(getContext());
            new MinorLogMsg(this, "Unable to load corba proxy for EcpRatePlanClient", e).log(getContext());
        }
    }


    /**
     * Invalidates the connection.
     *
     * @param t
     *            Exception or error thrown.
     */
    public synchronized void invalidate(final Throwable t)
    {
        // only raise a SNMP trap if this is the first time we discover the connection is
        // gone
        if (this.service_ != null)
        {
            new EntryLogMsg(10617, this, "", this.ecpRatePlanProperty_.toString(), new String[]
            {
                this.ecpRatePlanProperty_.getNameServiceHost(),
                String.valueOf(this.ecpRatePlanProperty_.getNameServicePort()),
            }, t).log(getContext());
        }
        this.corbaProxy_.invalidate();
        this.service_ = null;
    }


    /**
     * {@inheritDoc}
     */
    public RatePlanInfo[] getRatePlans(final int spid) throws EcpRatePlanClientException
    {
        int result = -1;
        final RatePlanInfoSetHolder rpisHolder = new RatePlanInfoSetHolder();

        try
        {
            final RatePlanProvisioner service = getService();
            result = service.queryRatePlans(spid, rpisHolder);
            if (result != ErrorCode.SUCCESS)
            {
                throw new EcpRatePlanClientException("Error in getRatePlans ", result);
            }
        }
        catch (final org.omg.CORBA.COMM_FAILURE commFail)
        {
            invalidate(commFail);
            result = 301;
        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "Fail to increment balance: " + e, null).log(getContext());
        }
        new DebugLogMsg(this, "Get ECP rate plans successful", null).log(getContext());

        return rpisHolder.value;
    }


    /**
     * Retrieves the ECP rate plan CORBA service, and try to establish one if it doesn't
     * exist.
     *
     * @return The ECP rate plan CORBA service.
     */
    private synchronized RatePlanProvisioner getService()
    {
        org.omg.CORBA.Object objServant = null;
        if (this.service_ != null)
        {
            return this.service_;
        }
        if (this.corbaProxy_ == null)
        {
            // ORB orb = ORB.init(new String[]{}, null);
            try
            {
                this.corbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(), this.ecpRatePlanProperty_, this);
            }
            catch (final CorbaClientException ccEx)
            {
                invalidate(ccEx);
                return null;
            }
        }
        objServant = this.corbaProxy_.instance();
        if (objServant != null)
        {
            try
            {
                this.service_ = RatePlanProvisionerHelper.narrow(objServant);
                if (this.service_ != null)
                {
                    // snmp external link up trap
                    new EntryLogMsg(10618, this, "", this.ecpRatePlanProperty_.toString(), new String[]
                    {
                        this.ecpRatePlanProperty_.getNameServiceHost(),
                        String.valueOf(this.ecpRatePlanProperty_.getNameServicePort()),
                    }, null).log(getContext());
                }
                return this.service_;
            }
            catch (final Exception e)
            {
                invalidate(e);
                return null;
            }
        }
        invalidate(null);
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        if (this.ecpRatePlanProperty_ != null)
        {
            return this.ecpRatePlanProperty_.getKey();
        }
        return "EcpRatePlanClient";
    }


    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return "CORBA client for getting all the rate plans from ECP";
    }


    /**
     * {@inheritDoc}
     */
    public boolean isAlive()
    {
        return this.bConnected_;
    }


    /**
     * {@inheritDoc}
     */
    public void connectionUp() throws ConnectionUpException
    {
        // TODO change the alarm to a new one
        new EntryLogMsg(12720L, this, "", this.ecpRatePlanProperty_.toString(), new String[]
        {
            this.ecpRatePlanProperty_.getNameServiceHost(),
            String.valueOf(this.ecpRatePlanProperty_.getNameServicePort()),
        }, null).log(getContext());
        this.bConnected_ = true;
    }


    /**
     * {@inheritDoc}
     */
    public void connectionDown()
    {
        // TODO change the alarm to a new one
        this.bConnected_ = false;
        this.service_ = null;
        // snmp external link down trap
        if (this.ecpRatePlanProperty_ != null)
        {
            new EntryLogMsg(12719L, this, "", this.ecpRatePlanProperty_.toString(), new String[]
            {
                this.ecpRatePlanProperty_.getNameServiceHost(),
                String.valueOf(this.ecpRatePlanProperty_.getNameServicePort()),
            }, null).log(getContext());
        }
    }

    /**
     * ECP rate plan CORBA service.
     */
    private RatePlanProvisioner service_;

    /**
     * CORBA proxy of the ECP rate plan client.
     */
    private com.redknee.util.corba.CorbaClientProxy corbaProxy_;

    /**
     * CORBA client property of the ECP rate plan client.
     */
    private CorbaClientProperty ecpRatePlanProperty_;

    /**
     * Whether the client is connected.
     */
    private boolean bConnected_;

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(ecpRatePlanProperty_, isAlive());
    }


    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }

}
