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
package com.trilogy.app.crm.provision.soap;

import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.SoapServerConfig;
import com.trilogy.app.crm.bean.SoapServicesConfig;
import com.trilogy.app.crm.bean.SoapServicesConfigXInfo;

import electric.registry.Registry;
import electric.util.log.Log;
import electric.registry.RegistryException;
import electric.server.http.HTTP;
import electric.servlet.HTTPContext;


/** 
 * 
 * @author abaid,skushwaha,imahalingam
 * 
 */
public class WebServiceServer extends LifecycleAgentSupport {
    
	private static final long serialVersionUID = 1L;

	/**
	 * @param ctx
	 * @throws AgentException
	 */
	public WebServiceServer(Context ctx) throws AgentException
    {
        super(ctx);        
        init(ctx);
    } 
    
    
	/* (non-Javadoc)
	 * @see com.redknee.framework.lifecycle.LifecycleAgentSupport#doStop(com.redknee.framework.xhome.context.Context)
	 */
	public void doStop(Context ctx)throws LifecycleException
    {
        try
        {
            Registry.unpublish(soapServiceName);
        }
        catch (RegistryException e)
        {
            LogSupport.info(getContext(), this, "Exception caught stop SOAP service: " + e.getMessage(), e);
        }
        HTTP.shutdown();
        synchronized (monitor_)
        {
            monitor_.notifyAll();
        } 
    }
    
    
    /* (non-Javadoc)
     * @see com.redknee.framework.lifecycle.LifecycleAgentSupport#doStart(com.redknee.framework.xhome.context.Context)
     */
    public void doStart(Context ctx)throws LifecycleException
    {
        try
        {
            startServer( ctx);  
        }
        catch(Exception e)
        {
           throw new  LifecycleException(e);
        }
    }  
    
    /**
     * @param ctx
     * @throws AgentException
     */
    private void init(Context ctx) throws AgentException
    {
        try
        {    
             SoapServerConfig soapConfig=(SoapServerConfig)ctx.get(SoapServerConfig.class);
             soapURL = soapConfig.getServerUrl();       //Default - "http://localhost:9268/AppCrm/home/SoapService";;
             startUrl = soapURL;
             serverTimeout_ = soapConfig.getServerTimeout();//0;         
        }
        catch(Exception e)
        {
            throw new AgentException(e); 
        }
    }
    
    /**
     * @param ctx
     * @throws AgentException
     */
    private void startServer(Context ctx)throws AgentException
    {  
    	init(ctx);
    	try
    	{
    		SoapServerConfig soapConfig=(SoapServerConfig)ctx.get(SoapServerConfig.class);
    		
    		// set the end point
    		electric.util.Context electic_ctx = new electric.util.Context();
    		
    		if ( soapConfig.getGlueLog())
    		{
    			Log.startLogging("ERROR");  
    			Log.startLogging("EXCEPTION");
    			Log.startLogging("WARNING"); 
    		}
    		
    		{
    		HTTP.setMaxThreads(soapConfig.getThreadPoolSize()); 
    		HTTP.setMaxInboundKeepAlive(soapConfig.getMaxInboundKeepAlive());
    		HTTP.setMaxOutboundKeepAlive(soapConfig.getMaxOutboundKeepAlive() );
    		HTTP.setDefaultBacklog( soapConfig.getMaxQueue()); 
    		HTTPContext httpContext = HTTP.startup(startUrl);
    		httpContext.getServletEngine().getWebServer().setTimeout(soapConfig.getServerTimeout());
    		LogSupport.info(getContext(), this, "HTTP startup on [" + startUrl + "]");
    		}
    		
    		SubscriberProvisionInterface subProvisionInterface = new SubscriberProvisioner(ctx);
    		PricePlanProvisionInterface pricePlanProvisionInterface = new PricePlanProvisionImpl(ctx);
    		SubscriberInterface subServicesProvisionInterface = new SubscriberInterfaceImpl(ctx);
    		
    		
    		Home soapServicesCfgHome = (Home) getContext().get(SoapServicesConfig.class);
    		if (soapServicesCfgHome == null)
    		{
    			throw new AgentException("System Error: Soap Services Config Home does not exist in Context.");
    		}
    		
    		//Publishing Subscriber Deletion/Deactivation service
    		{
    			try
    			{
    				
    				SoapServicesConfig soapSvcsCfgForSubProv = (SoapServicesConfig) soapServicesCfgHome
    				.find(new EQ(SoapServicesConfigXInfo.INTERFACE_KEY,SUBSCRIBER_PROVISION_INTERFACE));
    				
    				if(soapSvcsCfgForSubProv != null)
    				{
    					soapServiceName = soapSvcsCfgForSubProv.getSoapService(); //soapServiceName = "SoapService";
    					String soapURLForSubProv = soapURL + "/" + soapServiceName;
    					electic_ctx.setProperty(END_POINT, soapURLForSubProv);
    					
    					Registry.publish(
    							soapServiceName,
    							subProvisionInterface,
    							SubscriberProvisionInterface.class,  
    							electic_ctx);
    				}
    			}
    			catch(Exception e)
    			{
    	    		LogSupport.major(getContext(), this, "Error during publishing SOAP service [" +soapServiceName+ 
    	    				" ,exception= " + e.getMessage() + "]");
    			}
    		}
    		
    		
    		// Publishing SecondaryPricePlanSwitch service
    		{
    			try
    			{
    				SoapServicesConfig soapServicesCfgForPPprov = (SoapServicesConfig)soapServicesCfgHome
    				.find(new EQ(SoapServicesConfigXInfo.INTERFACE_KEY,PRICEPLAN_PROVISION_INTERFACE));   
    				
    				if(soapServicesCfgForPPprov != null)
    				{
    					soapServiceName = soapServicesCfgForPPprov.getSoapService(); 
    					electic_ctx = new electric.util.Context();
    					String soapURLForPPprov = soapURL+"/"+soapServiceName;
    					electic_ctx.setProperty(END_POINT, soapURLForPPprov);
    					
    					Registry.publish(
    							soapServiceName,
    							pricePlanProvisionInterface,
    							PricePlanProvisionInterface.class,
    							electic_ctx);
    				}
    			}
    			catch(Exception e)
    			{
    				LogSupport.major(getContext(), this, "Error during publishing SOAP service [" +soapServiceName+ 
    						" ,exception= " + e.getMessage() + "]");
    			}

    		}
    		
    		// Publishing Subscriber service modification related services
    		{
    			try
    			{
    				SoapServicesConfig soapServicesCfgForSubSvcs = (SoapServicesConfig)soapServicesCfgHome
    				.find(new EQ(SoapServicesConfigXInfo.INTERFACE_KEY,SUBSCRIBER_INTERFACE));   
    				
    				if(soapServicesCfgForSubSvcs != null)
    				{
    					soapServiceName = soapServicesCfgForSubSvcs.getSoapService(); 
    					electic_ctx = new electric.util.Context();
    					String soapURLForPPprov = soapURL+"/"+soapServiceName;
    					electic_ctx.setProperty(END_POINT, soapURLForPPprov);
    					
    					
    					Registry.publish(soapServiceName,
    							subServicesProvisionInterface,
    							SubscriberInterface.class, electic_ctx);
    				}
    			}
    			catch(Exception e)
    			{
    				LogSupport.major(getContext(), this, "Error during publishing SOAP service [" +soapServiceName+ 
    						" ,exception= " + e.getMessage() + "]");
    			}
    		}
    		
    		
    		LogSupport.info(getContext(), this, "Publishing SoapService API at " + soapURL);
    		LogSupport.info(getContext(), this, "SOAP Server started successfully.");
    	}
    	catch (Exception e)
    	{
    		LogSupport.major(getContext(), this, "Error during SOAP server startup. " +
    				"exception=[" + e.getMessage() + "]", e);
    		
    		throw new AgentException(e);
    	} 
    }
    
    
    
    private static String soapURL;
    private Object monitor_ = new Object();
    private String startUrl;
    private int serverTimeout_;
    private String soapServiceName;
    private String SUBSCRIBER_PROVISION_INTERFACE = "SubscriberProvisionInterface.class";
    private String PRICEPLAN_PROVISION_INTERFACE = "PricePlanProvisionInterface.class";
    private String SUBSCRIBER_INTERFACE = "SubscriberInterface.class";
    private String END_POINT = "endpoint";
}
