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
package com.trilogy.app.crm.client.ipcg;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.product.s5600.ipcg.rating.provisioning.RatePlan;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcRatePlan;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.util.corba.ConnectionListener;
import com.trilogy.util.corba.CorbaClientProxy;

/**
 * 
 * This class has all the common connection elements for connecting to 
 * IpcgRatingProv and the IprcRatingProv services.
 * 
 * Key which we'll use to get the CORBA connection configuration for IPCG/IPC Rate Plan Clients.
 *  
 * Prior to CRM 7.7 and the introduction of the IprcRatingProv interface we only used 
 * ProductS5600IpcgRatingProvClient as the key. 
 * @author ali
 *
 */
public class UrcsDataRatingProvClient 
    extends ContextAwareSupport
    implements RemoteServiceStatus, ConnectionListener
{

    private static final String SERVICE_DESCRIPTION = "CORBA client for URCS Data Rate Plan Look-up Services";

    protected void init() throws IllegalArgumentException
    {
        final Home corbaClientPropertyHome;
        corbaClientPropertyHome = (Home) getContext().get(CorbaClientPropertyHome.class);

        if (corbaClientPropertyHome == null)
        {
            throw new IllegalArgumentException("Corba client configuration Home does not exist");
        }

        try
        {
            ratingProvCorbaProperty_ = (CorbaClientProperty)
                    corbaClientPropertyHome.find(getContext(), propertiesKey_);
            if (ratingProvCorbaProperty_ == null)
            {
                throw new IllegalArgumentException(
                        "Corba client configuration " + propertiesKey_ + " does not exist");
            }
            new InfoLogMsg(this, ratingProvCorbaProperty_.toString(), null).log(getContext());

            ratingProvusername_ = ratingProvCorbaProperty_.getUsername();
            ratingProvpassword_ = ratingProvCorbaProperty_.getPassword();
            ratingProvCorbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(), ratingProvCorbaProperty_, this);
        }
        catch (Exception e)
        {
            connectionDown();
            if (ratingProvCorbaProperty_ != null)
            {
                // snmp external link down trap
                throw new IllegalArgumentException(ratingProvCorbaProperty_.toString()
                        + " Host:" + ratingProvCorbaProperty_.getNameServiceHost()
                        + "Port:" + String.valueOf(ratingProvCorbaProperty_.getNameServicePort()));
            }
            throw new IllegalArgumentException(
                    "Unable to load corba proxy for UrcsDataRatingProvClient");
        }
    }

    public synchronized void invalidate()
    {
        connectionDown();
        if (ratingProvCorbaProxy_ != null)
        {
            ratingProvCorbaProxy_.invalidate();
        }
    }

    public String getName()
    {
        return propertiesKey_;
    }

    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }

    public boolean isAlive()
    {
        return bConnected_;
    }

    public synchronized void connectionUp()
    {
        bConnected_ = true;

        // snmp external link up trap
        if (ratingProvCorbaProperty_ != null && !upTrapSent_)
        {
            upTrapSent_ = true;
            downTrapSent_ = false;
            new EntryLogMsg(11774, this, "",
                    ratingProvCorbaProperty_.toString(),
                    new String[]{ratingProvCorbaProperty_.getNameServiceHost(),
                            String.valueOf(ratingProvCorbaProperty_.getNameServicePort())},
                    null).log(getContext());
        }
    }

    public synchronized void connectionDown()
    {
        bConnected_ = false;

        // SNMP external link down trap
        if (ratingProvCorbaProperty_ != null && !downTrapSent_)
        {
            upTrapSent_ = false;
            downTrapSent_ = true;
            new EntryLogMsg(11773, this, "",
                    ratingProvCorbaProperty_.toString(),
                    new String[]{ratingProvCorbaProperty_.getNameServiceHost(),
                            String.valueOf(ratingProvCorbaProperty_.getNameServicePort())},
                    null).log(getContext());
        }
    }
    
    /**
     * To support this method overwrite it in the inheriting Client
     * @param context operating Context
     * @return
     * @throws IpcgRatingProvException
     */
    public RatePlan[] retrieveAllRatingPlans(final Context context)
        throws IpcgRatingProvException
    {
        throw new IpcgRatingProvException(
                "The retrieveAllRatingPlans(Context) method is not supported by the configured " +
                "URCS Data Rate Provisioning Interface.  Use the IpcgRatingProv interface.");
    }
    
    /**
     * To support this method overwrite it in the inheriting Client
     * @param context operating Context
     * @param spid Service Provider 
     * @return
     * @throws IpcgRatingProvException
     */
    public IprcRatePlan[] queryRatePlans(final Context context, final int spid)
    throws IpcgRatingProvException
    {
        throw new IpcgRatingProvException(
                "The queryRatePlans(Context, int) method is not supported by the configured " +
                "URCS Data Rate Provisioning Interface.  Use the IprcRatingProv interface.");
    }
    
    
    protected boolean downTrapSent_ = false;
    protected boolean upTrapSent_ = false;
    
    protected CorbaClientProxy ratingProvCorbaProxy_;
    protected CorbaClientProperty ratingProvCorbaProperty_ = null;
    protected String ratingProvusername_;
    protected String ratingProvpassword_;

    protected boolean bConnected_ = false;
    
    protected String propertiesKey_;

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(ratingProvCorbaProperty_, isAlive());
    }

    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }
}
