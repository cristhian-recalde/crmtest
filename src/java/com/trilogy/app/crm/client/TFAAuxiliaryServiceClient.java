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

import org.omg.CORBA.SystemException;

import com.trilogy.app.crm.client.bm.ConnectionProperties;
import com.trilogy.app.crm.client.bm.TimerCachedConnectionProperties;
import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.transferfund.corba.provision.ActionType;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListProvisionRequest;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListProvisionResponse;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListProvisionResponseListHolder;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListQueryResponse;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListQueryResponseListHolder;
import com.trilogy.app.transferfund.corba.provision.UserAuth;
import com.trilogy.app.transferfund.corba.provision.UserAuthHelper;
import com.trilogy.app.transferfund.corba.provision.WhiteBlackListProvisionService;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.util.corba.ConnectionListener;
import com.trilogy.util.corba.CorbaClientException;
import com.trilogy.util.corba.CorbaClientProxy;
import com.trilogy.util.snippet.log.Logger;


/*
 * Corba Client to invoke TFA API's 
 * 
 * @author piyush.shirke@redknee.com
 * 
 */
public class TFAAuxiliaryServiceClient implements  RemoteServiceStatus, ConnectionListener, ITFAAuxiliaryServiceClient 
{

	private final ContextAwareSupport fallbackContext_;

	private final ConnectionProperties connectionProperties_;

	private ConnectionState state_;

	private static final String SERVICE_DESCRIPTION = "CORBA client for provisioning TFA subscribers";

	private static final String CONNECTION_PROPERTIES_KEY = "AppTransferfund";

	private UserAuth userAuth2_;
	private WhiteBlackListProvisionService service_ = null; 

	private CorbaClientProxy corbaProxy_;
	
	private String username_ = null;
	private String password_ = null;
	
	public String getUsername()
	{
		if(username_ != null)
		{
			return username_;
		}else
		{
			return this.connectionProperties_.getProperties(fallbackContext_.getContext()).getUsername();
		}
		
	}
	
	
	public String getPassword()
	{
		if(password_ != null)
		{
			return password_;
		}else
		{
			return this.connectionProperties_.getProperties(fallbackContext_.getContext()).getPassword();
		}
	}
	
	public TFAAuxiliaryServiceClient(final Context ctx, final String username, final String password)
	{
		 fallbackContext_ = new ContextAwareSupport()
        {
        };

        this.username_ = username;
        this.password_ = password;
        
        fallbackContext_.setContext(ctx);

        connectionProperties_ = new TimerCachedConnectionProperties(CONNECTION_PROPERTIES_KEY);

        state_ = ConnectionState.UNINITIALIZED;
        /*
         * Just to connect at app start up
         */
        getService(ctx);

	}
	
	public TFAAuxiliaryServiceClient(final Context ctx)
	{
		 fallbackContext_ = new ContextAwareSupport()
        {
        };

        fallbackContext_.setContext(ctx);

        connectionProperties_ = new TimerCachedConnectionProperties(CONNECTION_PROPERTIES_KEY);

        state_ = ConnectionState.UNINITIALIZED;
        
        /*
         * Just to connect at app start up
         */
        getService(ctx);
        
	}

	
	@Override
	public void connectionDown() 
	{
		if (state_ == ConnectionState.DOWN)
        {
            return;
        }

        state_ = ConnectionState.DOWN;

        final Context ctx = fallbackContext_.getContext();

        // Note that the alive check above is also used to ensure that the
        // invalidate() call below does not recurse infinitely.
        invalidate(ctx);

        //  final String[] arguments = getEntryLogParameters(ctx);
        // new EntryLogMsg(12654L, this, CONNECTION_PROPERTIES_KEY, "", arguments, null).log(ctx);
		
	}

	
	@Override
	public void connectionUp()  
	{
		   if (state_ == ConnectionState.UP)
	        {
	            return;
	        }

	        state_ = ConnectionState.UP;

	      //  final Context context = fallbackContext_.getContext();
	     //   final String[] arguments = getEntryLogParameters(context);
	      //  new EntryLogMsg(12655L, this, CONNECTION_PROPERTIES_KEY, "", arguments, null).log(context);
		
	}

	@Override
	public ConnectionStatus[] getConnectionStatus() {
		
		  final Context context = fallbackContext_.getContext();
	      final CorbaClientProperty properties = connectionProperties_.getProperties(context);

	       return SystemStatusSupportHelper.get().generateConnectionStatus(properties, state_);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return SERVICE_DESCRIPTION;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		 return CONNECTION_PROPERTIES_KEY;
	}

	@Override
	public String getServiceStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		 return state_ == ConnectionState.UP;
	}
	
	  public synchronized void invalidate(final Context ctx)
	    {
	        connectionDown();

	        // looks like it's not needed
	        //corbaProxy_.invalidate();
	        service_ = null;
	    }
	  
	  private synchronized WhiteBlackListProvisionService getService(Context ctx)
	    {
	        if (connectionProperties_.updateAvailable(ctx))
	        {
	            if (Logger.isInfoEnabled())
	            {
	                Logger.info(ctx, this, "New " + CONNECTION_PROPERTIES_KEY
	                        + " connection properties available. Reinitializing.");
	            }

	            connectionProperties_.refreshProperties(ctx);
	            // Proxy only needs to be discarded when the properties change.
	            corbaProxy_ = null;
	            invalidate(ctx);
	            initializeService(ctx);
	        }
	        else if (service_ == null || service_._non_existent())
	        {
	            if (Logger.isInfoEnabled())
	            {
	                Logger.info(ctx, this, CONNECTION_PROPERTIES_KEY + " Null service. Reinitializing.");
	            }

	            invalidate(ctx);
	            initializeService(ctx);
	        }

	        return service_;
	    }


	  private void initializeService(final Context ctx)
	    {
		  final CorbaClientProperty properties = connectionProperties_.getProperties(ctx);
	        if (corbaProxy_ == null)
	        {
	            

	            if (properties != null)
	            {
	                try
	                {
	                    corbaProxy_ = CorbaSupportHelper.get(ctx).createProxy(ctx, properties, this);
	                }
	                catch (final CorbaClientException exception)
	                {
	                    Logger.major(ctx, this, "Failure to create CORBA proxy for " + properties, exception);
	                    invalidate(ctx);
	                }
	            }
	            else
	            {
	                Logger.major(ctx, this, "Failed to find CORBA properties for " + CONNECTION_PROPERTIES_KEY);
	                invalidate(ctx);
	            }
	        }

	        if (corbaProxy_ != null)
	        {
	            org.omg.CORBA.Object objServant = null;
	            try
	            {
	                objServant = corbaProxy_.instance();
	            }
	            catch (final SystemException exception)
	            {
	                Logger.major(ctx, this, "Failure while attempting to instantiate proxy.", exception);
	                invalidate(ctx);
	            }

	            if (objServant != null)
	            {
	                try
	                {
	                	userAuth2_ = UserAuthHelper.narrow(objServant);

	                    if (userAuth2_ != null)
	                    {
	                    	service_ = userAuth2_.requestWhiteBlackListProvisionService(getUsername(),getPassword());
	                    	connectionUp();
	                    }
	                }
	                catch (final Exception exception)
	                {
	                    Logger.major(ctx, this, "Failed to narrow to service.", exception);
	                    invalidate(ctx);
	                }
	            }
	            else
	            {
	                Logger.major(ctx, this, "Failed to instantiate proxy.");
	                invalidate(ctx);
	            }
	        }
	    }
	  
	  
	  /**
		 *Requests TFA to bulk load services
		 * 
		 * @param ctx
		 *            The operating context.
		 * @param request 
		 *             consists of Msisdns to add with role and restriction Type
		 * @throws TFAAuxiliarServiceClientException
		 */
	  
	  public boolean uploadWhiteBlackList (final Context ctx,String[] msisdns)  throws TFAAuxiliarServiceClientException
	  {
		  boolean retValue = false;
	        try
	        {
	            final WhiteBlackListProvisionService service = getService(ctx);
	            if (service == null)
	            {
	                throw new TFAAuxiliarServiceClientException("Unable to connect to TFA application!");
	            }
	            retValue = service.uploadWhiteBlackList (msisdns, com.redknee.app.transferfund.corba.provision.ActionType.ADD_APPEND, com.redknee.app.transferfund.corba.provision.SubsRole.CONTRIBUTOR , com.redknee.app.transferfund.corba.provision.RestrictionListType.ALLOWED);
	        }   
	        catch (SystemException e)
	        {
	        	String strMsg = "Could not upload in bulk due to exception: "+ e.getMessage();
	            invalidate(ctx);
	            throw new TFAAuxiliarServiceClientException(strMsg);
	        }
	        catch (TFAAuxiliarServiceClientException e)
	        {
	            throw e;
	        }
	        catch (Throwable e)
	        {
	            final String msg =  "Could not upload in bulk due to exception: "+ e.getMessage();
	            Logger.minor(ctx, this, msg, e);
	            throw new TFAAuxiliarServiceClientException(msg, e);
	        }
	        return retValue;
	  }

	  
	  /**
		 *Requests TFA for adding services
		 * 
		 * @param ctx
		 *            The operating context.
		 * @param request 
		 *             consists of Msisdns to add with role and restriction Type
		 * @throws TFAAuxiliarServiceClientException
		 */
	  
	   public BlackWhiteListProvisionResponse[]  addWhiteListBlackList(final Context ctx,BlackWhiteListProvisionRequest[] request)  throws TFAAuxiliarServiceClientException
	   {
	        try
	        {
	            final WhiteBlackListProvisionService service = getService(ctx);
	            boolean resStatus = false;
	            if (service == null)
	            {
	                throw new TFAAuxiliarServiceClientException("Unable to connect to TFA application!");
	            }
	            
	            BlackWhiteListProvisionResponseListHolder response =new BlackWhiteListProvisionResponseListHolder();
            	service.addWhiteListBlackList(request, ActionType.ADD_APPEND,response);
            	
            	  if(!resStatus && response.value.length == 0)
  	            	throw new TFAAuxiliarServiceClientException("Failed to perform operation on TFA server.");


            	return response.value;
	        } 
	        catch (SystemException e)
	        {
	            invalidate(ctx);
	            throw new TFAAuxiliarServiceClientException(" ", e);
	        }
	        catch (TFAAuxiliarServiceClientException e)
	        {
	            throw e;
	        }
	        catch (Throwable e)
	        {
	            final String msg = "t";
	            Logger.minor(ctx, this, msg, e);
	            throw new TFAAuxiliarServiceClientException(msg, e);
	        }

	  }

	   
	   /**
		 *Requests TFA for Querying to identify already existing services
		 * 
		 * @param ctx
		 *            The operating context.
		 * @param msisdn 
		 *             array of MSISDN for which to query TFA 
		 * @throws TFAAuxiliarServiceClientException
		 */
	   
	   public BlackWhiteListQueryResponse[] queryWhiteBlackList(final Context ctx,String[] msisdn) throws TFAAuxiliarServiceClientException
	   {
		   try
	        {
	            final WhiteBlackListProvisionService service = getService(ctx);
	            boolean resStatus = false;
	            
	            if (service == null)
	            {
	                throw new TFAAuxiliarServiceClientException("Unable to connect to TFA application!");
	            }
	            
	            BlackWhiteListQueryResponseListHolder response =new BlackWhiteListQueryResponseListHolder();
	            service.queryWhiteBlackList(msisdn,response);
	            
	            if(!resStatus && response.value.length == 0)
	            	throw new TFAAuxiliarServiceClientException("Failed to perform operation on TFA server.");

	            return response.value;
            	
	        }
		   catch (SystemException e)
	        {
	            invalidate(ctx);
	            throw new TFAAuxiliarServiceClientException(" ", e);
	        }
	        catch (TFAAuxiliarServiceClientException e)
	        {
	            throw e;
	        }
	        catch (Throwable e)
	        {
	            final String msg = "t";
	            Logger.minor(ctx, this, msg, e);
	            throw new TFAAuxiliarServiceClientException(msg, e);
	        }
	   }

	   
	   /**
		 *Removes services from TFA
		 * 
		 * @param ctx
		 *            The operating context.
		 * @param request
		 *            Contains array having MSISDN , SubsRole and Restriction Type 
		 * @return Response from TFA containing result codes
		 */
	   public BlackWhiteListProvisionResponse[] removeWhiteBlackList(final Context ctx, BlackWhiteListProvisionRequest[] request) throws TFAAuxiliarServiceClientException
	   {
		   try
	        {
	            final WhiteBlackListProvisionService service = getService(ctx);
	            boolean resStatus = false;
	            
	            if (service == null)
	            {
	                throw new TFAAuxiliarServiceClientException("Unable to connect to TFA application!");
	            }
	            
	            BlackWhiteListProvisionResponseListHolder response = new BlackWhiteListProvisionResponseListHolder();
	            service.removeWhiteBlackList(request, response);
	            
	            if(!resStatus && response.value.length == 0)
	            	throw new TFAAuxiliarServiceClientException("Failed to perform operation on TFA server.");

	            
	            return response.value;
	        }
		   catch (SystemException e)
	        {
	            invalidate(ctx);
	            throw new TFAAuxiliarServiceClientException(" ", e);
	        }
	        catch (TFAAuxiliarServiceClientException e)
	        {
	            throw e;
	        }
	        catch (Throwable e)
	        {
	            final String msg = "t";
	            Logger.minor(ctx, this, msg, e);
	            throw new TFAAuxiliarServiceClientException(msg, e);
	        }
	   }
	   
	   
	   /**
		 * Updates TFA services while MSISDN change. Removes from previous MSISDN and assign the same to new one
		 * 
		 * @param ctx
		 *            The operating context.
		 * @param oldMsisdn
		 *            MSISDN to remove services from.
		 * @param newMsisdn
		 *            MSISDN to add services to.
		 * @return Whether true if provisioned successfully.
		 */

	   public boolean changeMsisdn(final Context ctx, String oldMsisdn, String newMsisdn) throws TFAAuxiliarServiceClientException
	   {
		   try
	        {
	            final WhiteBlackListProvisionService service = getService(ctx);
	            if (service == null)
	            {
	                throw new TFAAuxiliarServiceClientException("Unable to connect to TFA application!");
	            }
	            
	           return service.changeMsisdn(oldMsisdn, newMsisdn);
	            
	        }
		   catch (SystemException e)
	        {
	            invalidate(ctx);
	            throw new TFAAuxiliarServiceClientException(" ", e);
	        }
	        catch (TFAAuxiliarServiceClientException e)
	        {
	            throw e;
	        }
	        catch (Throwable e)
	        {
	            final String msg = "t";
	            Logger.minor(ctx, this, msg, e);
	            throw new TFAAuxiliarServiceClientException(msg, e);
	        }
	   }

}
