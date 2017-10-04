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

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.StringHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.EntryLogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.product.abm.Account;
import com.trilogy.product.abm.AccountHelper;
import com.trilogy.product.abm.param.Parameter;
import com.trilogy.product.abm.param.ParameterSetHolder;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.util.corba.ConnectionListener;
import com.trilogy.util.corba.ConnectionUpException;
import com.trilogy.util.corba.CorbaClientException;
import com.trilogy.util.corba.CorbaClientProxy;

/**
 * @author amedina
 *
 * Handles all the ABM calls for the Account interface of ABM
 */
public class ProductABMAccountClient extends ContextAwareSupport 
implements	RemoteServiceStatus, ConnectionListener 
{

    private static final String SERVICE_NAME = "ProductABMAccountClient";
    private static final String SERVICE_DESCRIPTION = "CORBA client for ABM services on Account interface";

    public ProductABMAccountClient(Context ctx)
    {
        setContext(ctx);
        init();
    }

	
    private void init()
    {
        Home corbaClientPropertyHome = null;

        corbaClientPropertyHome = (Home)getContext().get(CorbaClientPropertyHome.class);

        if (corbaClientPropertyHome == null)
        {
            throw new IllegalStateException("Corba client configuration does not exist");
        }

        try
        {
        	abmProperty_ = (CorbaClientProperty)corbaClientPropertyHome.find(getContext(),SERVICE_NAME);
            service_ = null;
            //ORB orb = ORB.init(new String[]{}, null);
            corbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(),abmProperty_,this);
        }
        catch (Exception e)
        {
         if(abmProperty_!=null)
         {
            // snmp external link down trap
             throw new IllegalArgumentException(EntryLogSupport.entry(getContext(), 10340, this, "",
                     abmProperty_.toString(),
                     new String[]{abmProperty_.getNameServiceHost(), String.valueOf(abmProperty_.getNameServicePort())},
                     e));
         }
         else
         {
            IllegalArgumentException iaEx = new IllegalArgumentException("Fail to instantiate CORBA proxy for ProductABMAccountClient");
            iaEx.initCause(e);
            throw iaEx;
         }
        }
    }

    
    private synchronized Account getService()
    {
        org.omg.CORBA.Object objServant = null;

        if (service_ != null)
        {
            return service_;
        }

        if (corbaProxy_ == null)
        {
            //ORB orb = ORB.init(new String[]{}, null);
            try
            {
                corbaProxy_ = CorbaSupportHelper.get(getContext()).createProxy(getContext(),abmProperty_,this);


            }
            catch (CorbaClientException  ccEx)
            {
                invalidate(ccEx);
                return null;
            }
        }
        objServant = corbaProxy_.instance();
        if (objServant != null)
        {
            try
            {
                // attempt to derive SubscriberProvision
                service_ = AccountHelper.narrow(objServant);
                return service_;
            }
            catch (Exception e)
            {
                invalidate(e);
                return null;
            }
        }
        invalidate(null);
        return null;
    }

    
    public synchronized void invalidate(Throwable t)
    {
        corbaProxy_.invalidate();
        service_ = null;
    }

	/* (non-Javadoc)
	 * @see com.redknee.app.crm.client.ExternalService#getServiceName()
	 */
	public String getName() 
	{
        if(abmProperty_!=null)
        {
            return abmProperty_.getKey();
        }
        
        return SERVICE_NAME;
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
		return bConnected;
	}

	/* (non-Javadoc)
	 * @see com.redknee.util.corba.ConnectionListener#connectionUp()
	 */
	public void connectionUp() throws ConnectionUpException 
	{
		bConnected=true;

		// snmp external link up trap
        new EntryLogMsg(10341, this, "", abmProperty_.toString(), new String[] {abmProperty_.getNameServiceHost(), String.valueOf(abmProperty_.getNameServicePort())}, null).log(getContext());
	}

	/* (non-Javadoc)
	 * @see com.redknee.util.corba.ConnectionListener#connectionDown()
	 */
	public void connectionDown() 
	{
		bConnected=false;
		service_=null;

        if (abmProperty_ != null)
        {
            new EntryLogMsg(10340, this, "", abmProperty_.toString(), new String[]{abmProperty_.getNameServiceHost(), String.valueOf(abmProperty_.getNameServicePort())},null).log(getContext());
        }
	}

    public int credit(String msisdn, 
    		int amount, 
			String currency, 
			boolean useServiceId,
			int serviceId,
			String erReference, 
			boolean updExpiry, 
			short extension)
    {
            final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "credit()");

            IntHolder newBalance = new IntHolder();
			StringHolder newExpiry = new StringHolder();
            
            int result = AbmResultCode.SUCCESS;
            Account service = getService();

            if (service != null)
            {
                try
                {
                    result = service.credit(msisdn,
                    		amount,
							currency,
							useServiceId, 
							serviceId, 
							erReference, 
							updExpiry, 
							extension,
							newBalance,
							newExpiry,
							new Parameter[]{},
							new ParameterSetHolder());
                }
                catch (org.omg.CORBA.COMM_FAILURE commFail)
                {
                    invalidate(commFail);
                    result = COMMUNICATION_FAILURE;
                }
                catch (Exception e)
                {
                    new MinorLogMsg(this, "Fail to set debit adjustment " + msisdn, e).log(getContext());
                    result = 1;
                }
            }
            else
            {
                // connection not available
                result = COMMUNICATION_FAILURE;
            }

            pmLogMsg.log(getContext());

            return result;
   
    }
    
    public int debit(String msisdn, 
    		int amount, 
			String currency, 
			boolean useServiceId, 
			int serviceId, 
			boolean debitFlag, 
			String erRerference) 
    {
            final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "debit()");

			IntHolder shortfall = new IntHolder();
			IntHolder newBalance = new IntHolder();
            
            int result = AbmResultCode.SUCCESS;
            Account service = getService();

            if (service != null)
            {
                try
                {
                    result = service.debit(msisdn,
                    		amount,
							currency,
							useServiceId, 
							serviceId,
							debitFlag,
							erRerference, 
							shortfall,
							newBalance,
                            new Parameter[]{},
                            new ParameterSetHolder());
                }
                catch (org.omg.CORBA.COMM_FAILURE commFail)
                {
                    invalidate(commFail);
                    result = COMMUNICATION_FAILURE;
                }
                catch (Exception e)
                {
                    new MinorLogMsg(this, "Fail to set credit adjustment " + msisdn, e).log(getContext());
                    result = 1;
                }
            }
            else
            {
                // connection not available
                result = COMMUNICATION_FAILURE;
            }

            pmLogMsg.log(getContext());

            return result;
    }


	
    public static final int COMMUNICATION_FAILURE = 301;

	private CorbaClientProxy corbaProxy_;
    private CorbaClientProperty abmProperty_;
    private Account service_;

    private static final String PM_MODULE = ProductABMAccountClient.class.getName();

    private boolean bConnected=false;

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(abmProperty_, isAlive());
    }


    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }

}
