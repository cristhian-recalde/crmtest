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
package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.SPGProvService;
import com.trilogy.app.crm.bean.SPGServiceStateMappingConfig;
import com.trilogy.app.crm.bean.SPGServiceStateMappingConfigHome;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.provision.service.param.ParameterID;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;

/**
 * 
 *
 * @author isha.aderao
 * @since 
 */

/**
 * Utility methods for bulk update of subscriber services
 * Introduced as part of Verizon Integration Feature
 * 
 * @author isha.aderao
 * @since Aug 2013
 */

public class SubscriberServicesBulkUpdateSupport{
	
	/**
	 * Method that decides if this is bulk service update provisioning call flow
	 * 
	 * @param ctx
	 * @param subscriber
	 * @return
	 * @throws HomeException
	 */
	
    public static boolean isBulkServiceUpdate(Context ctx, Subscriber subscriber) throws HomeException
    {
        ProvisionCommand provisionCommand = null;

        if (SystemSupport.needsHlr(ctx))
        {
            provisionCommand = SubscriberServicesBulkUpdateSupport.findBulkServiceUpdateCommand(ctx, subscriber);

            // if bulk service update is configured for subscriber spid or global spid, then continue
            if (provisionCommand != null)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(),
                            "Configuration found for bulkServiceUpdate.");
                }
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
		
	
	
	/**
	 * method that obtains HLR command for bulkServiceUpdate 
	 * @param ctx
	 * @param subscriber
	 * @return
	 * @throws HomeException
	 */
	
	
	public static ProvisionCommand findBulkServiceUpdateCommand(Context ctx, Subscriber subscriber) throws HomeException
	{
		ProvisionCommand provisionCommand = null;
		String commandType = HLRConstants.PRV_CMD_TYPE_BULK_SERVICE_UPDATE;
		try
	    {
	   	 	provisionCommand = HlrSupport.findCommand(ctx, subscriber, commandType);
	    }
	    catch(HomeException he)
	    {
	   	 LogSupport.info(ctx, SubscriberServicesBulkUpdateSupport.class.getName(),
						"Skipping "	+ SubscriberServicesBulkUpdateSupport.class.getName()
								+ " as exception occured while looking up for bulkServiceUpdate provision command for service provider "+subscriber.getSpid());
		
	    }
		return provisionCommand;
	}
	
	
	 /**
     * Method that returns SPG provisioned service state mapping config
     * @param ctx
     * @param subscriber
     * @return
     * @throws HomeException
     */
    public static SPGServiceStateMappingConfig getSPGProvisionedServiceStateMapping(Context ctx, Subscriber subscriber) throws HomeException
    {
    	int spid = subscriber.getSpid();
    	
    	Home spgServiceStateMappingConfigHome  = (Home) ctx.get(SPGServiceStateMappingConfigHome.class); 
    	SPGServiceStateMappingConfig spgServiceStateMappingConfig = (SPGServiceStateMappingConfig) spgServiceStateMappingConfigHome.find(ctx, spid);
    	
    	if(spgServiceStateMappingConfig == null)
    	{
    		LogSupport.major(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "Provisioned service state config for SPG not found for SPID "+subscriber.getSpid());
    	}
    	else
    	{
	    	if(LogSupport.isDebugEnabled(ctx))
	    	{
	    		LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "Retrived SPGServiceStateMappingConfig "+spgServiceStateMappingConfig+" for spid "+subscriber.getSpid());
	    	}
    	}
	    	
    	return spgServiceStateMappingConfig;
    }
	
	
	

   /**
     * Calculates Old SPG-Provisioned SubscriberServices List
     * @param ctx
     * @param subscriber
     * @param OSLMap
     * @param oldProvisionedServicesList
     * @param provServiceStates
     * @return
     * @throws HomeException
     */
    
    public static List<SubscriberServices> calculateOPSL(Context ctx, Subscriber subscriber, Map<ServiceFee2ID, SubscriberServices>  OSLMap, List <SubscriberServices> oldProvisionedServicesList, Map<Integer, SPGProvService> provServiceStates) throws HomeException
    {
    	if(LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "old Services List for subscriber [ "+subscriber.getId()+ "] is : "+OSLMap);
    	}
    	if (OSLMap != null && !OSLMap.isEmpty())
    	{
	    	for (final Iterator<SubscriberServices> iter = OSLMap.values().iterator(); iter.hasNext();)
	        {
	    		SubscriberServices subService = iter.next();
	    		
	    		for(final Iterator<SPGProvService> stateIter = provServiceStates.values().iterator(); stateIter.hasNext();)
	    		{
	    			if(subService.getProvisionedState().getDescription().equals(stateIter.next().getServiceState().getDescription()))
	    			{
	    				oldProvisionedServicesList.add(subService); // Add only provisioned services to list
	    				break;
	    			}
	    		}
	        }
	    	if(LogSupport.isDebugEnabled(ctx))
	    	{
	    		LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "Old Services List with SPG state 'Provisioned' for subscriber [ "+subscriber.getId()+ "] is : "+oldProvisionedServicesList);
	    	}
    	}
		return oldProvisionedServicesList;
    }
    
    /**
     * Calculates New SPG-Provisioned SubscriberServices List
     * @param ctx
     * @param subscriber
     * @param NSLMap
     * @param newProvisionedServicesList
     * @param provServiceStates
     * @return
     */
    
    public static List<SubscriberServices> calculateNPSL(Context ctx, Subscriber subscriber, Map<ServiceFee2ID, SubscriberServices>  NSLMap, List <SubscriberServices> newProvisionedServicesList, Map<Integer, SPGProvService> provServiceStates)
    {
    	if(LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "New Services List for subscriber [ "+subscriber.getId()+ "] is : "+NSLMap);
    	}
    	if(NSLMap != null && !NSLMap.isEmpty())
    	{
	    	for (final Iterator<SubscriberServices> iter = NSLMap.values().iterator(); iter.hasNext();)
	        {
	    		SubscriberServices subService = iter.next();
	    		for(final Iterator<SPGProvService> stateIter = provServiceStates.values().iterator(); stateIter.hasNext();)
	    		{
	    			if(subService.getProvisionedState().getDescription().equals(stateIter.next().getServiceState().getDescription()))
	    			{
	    				newProvisionedServicesList.add(subService); // Add only provisioned services to list
	    			}
	    		}
	        }
	    	if(LogSupport.isDebugEnabled(ctx))
			{
	    		LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "New Services List with SPG state 'Provisioned' for subscriber [ "+subscriber.getId()+ "] is : "+newProvisionedServicesList);
			}
    	}
		return newProvisionedServicesList;
    }
    
   /**
    * Method that determines if subscriber update is for service change 
    * @param ctx
    * @param OPSL
    * @param NPSL
    * @return
    */
    
    public static boolean isSubscriberServicesChange(Context ctx, final List <SubscriberServices> OPSL, final List <SubscriberServices> NPSL)
    {
		short compareValue = comapreListSizes(OPSL, NPSL);
		
		if(compareValue != 0)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "Old and new service list sizes differ.");
			}
			
			return Boolean.TRUE;  // is service change, since there's diff in list
		}
		else
		{  // if sizes same, further need to check actual services
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "Comapring old and new service lists.");
			}
			return (calculateDifference(OPSL, NPSL,ctx));
		}
    }
    
    
    /**
     * compare list sizes as first criteria
     * @param oldProvisionedServicesList
     * @param newProvisionedServicesList
     * @return 0 is sizes are same, 1 if OPSL is greater in size otherwise -1
     */
    public static short comapreListSizes(List<SubscriberServices> oldProvisionedServicesList, List<SubscriberServices> newProvisionedServicesList)
    {
		if(oldProvisionedServicesList.size() > newProvisionedServicesList.size())
		{
			return 1;
		}
		if(oldProvisionedServicesList.size() < newProvisionedServicesList.size())
		{
			return -1;
		}
		else 
			return 0;
    }
    
    /**
     * Method to check if actual service lists differ (along with their SPG provisioned state)
     * @param oldProvisionedServicesList
     * @param newProvisionedServicesList
     * @return
     */
    public static boolean calculateDifference(List<SubscriberServices> oldProvisionedServicesList, List<SubscriberServices> newProvisionedServicesList,Context ctx)
    {
    	boolean isDiff = Boolean.FALSE;
    	boolean matchFound = Boolean.FALSE;
    	
    	for (final Iterator<SubscriberServices> iter1 = oldProvisionedServicesList.iterator(); iter1.hasNext() && isDiff!=Boolean.TRUE;)
        {
    		SubscriberServices service1 = iter1.next();
    		matchFound = Boolean.FALSE;
    		for (final Iterator<SubscriberServices> iter2 = newProvisionedServicesList.iterator(); iter2.hasNext() && matchFound!=Boolean.TRUE;)
        	{
    			SubscriberServices service2 = iter2.next();
            	if(service1.getServiceId() == service2.getServiceId())
            	{
            		matchFound = Boolean.TRUE;
            	}
        	}
    		if(!matchFound)
    		{
    			isDiff = Boolean.TRUE;
    		}
        }
    	return isDiff;
    }
    
    
    public static void setProvisionCommand(final Context ctx, ProvisionCommand cmd)
    {
        ctx.put(ProvisionCommand.class, cmd);
    }
    
    /**
     * send the command to SPG along with params
     * @param ctx
     * @param sub
     * @param cmdId
     * @param OSLMap
     * @param NSLMap
     * @throws HomeException
     */
	public static boolean sendCommandToSPG(final Context ctx,
			final Subscriber sub, Map<ServiceFee2ID, SubscriberServices> OSLMap, Map<ServiceFee2ID, SubscriberServices> NSLMap) throws HomeException, ProvisioningHomeException 
   {
		String OSL_values = "";
		String NSL_values = "";
		boolean isHlrUpdated = true;
		
		ArrayList<SubscriberServices> subOldServicesList = new ArrayList<SubscriberServices>();		
		final String hlrCommand = HLRConstants.PRV_CMD_TYPE_BULK_SERVICE_UPDATE;
		
		ProvisionCommand command = null;
		
	    if (hlrCommand != null && hlrCommand.length() > 0)
	    {
	    	command = HlrSupport.findCommand(ctx, sub, hlrCommand);
	    	
	        if (command!=null)
	        {
	            setProvisionCommand(ctx, command);
	            if(LogSupport.isDebugEnabled(ctx))
	 			{
	            	LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "Invoking SPG for "+hlrCommand);
	 			}
		
				for (final Iterator<SubscriberServices> iter = OSLMap.values().iterator(); iter.hasNext();)
		        {
					SubscriberServices oldSubscriberService = iter.next();
					subOldServicesList.add(oldSubscriberService);
					
					OSL_values = OSL_values + oldSubscriberService.getServiceId()+":"+oldSubscriberService.getProvisionedState().getDescription()+":"+oldSubscriberService.getService().getType().getDescription();
					if(iter.hasNext())
					{
						OSL_values = OSL_values+ "|";
					}
		        }
				
				for (final Iterator<SubscriberServices> iter = NSLMap.values().iterator(); iter.hasNext();)
		        {
					SubscriberServices newSubscriberService = iter.next();
					NSL_values = NSL_values + newSubscriberService.getServiceId()+":"+newSubscriberService.getProvisionedState().getDescription()+":"+newSubscriberService.getService().getType().getDescription();
					if(iter.hasNext())
					{
						NSL_values = NSL_values + "|";
					}
		        }
				
				if(LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "Sending to SPG : command= "+hlrCommand+ "with service params: OSL= "+OSL_values+ "and NSL="+NSL_values);
				}
					
				// This was intentionally doing because we can not send empty values in GRR
				//This is bad coding and need to handle in SPG or in ESB.
				if(OSL_values.isEmpty())
				{
					OSL_values= " ";
				}
				if(NSL_values.isEmpty())
				{
					NSL_values= " ";
				}
				
				// parameters to SPG
				ctx.put(ParameterID.OSL_ID, OSL_values);
				ctx.put(ParameterID.NSL_ID, NSL_values);
				
		       try
		       {
		           isHlrUpdated = HlrSupport.updateHlr(ctx, sub, hlrCommand);
		           
		           return isHlrUpdated;
		       }
		       catch (ProvisionAgentException e)
		       {
		           SubscriberProvisionResultCode.setProvisionHlrResultCode(ctx, -1);
		           throw new ProvisioningHomeException(e.getMessage() + " (Command = " + hlrCommand + ")", -1, Common.OM_HLR_PROV_ERROR, e);
		       }
       
	        }
	    }
	    else
	    {
	        LogSupport.major(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "Cannot find bulkServiceUpdate command.");
	    }
		return isHlrUpdated;
   }
    

    
    /**
     * @param ctx
     * @param e
     * @param caller
     * @return result code
     */
    public static int getHlrResponsibleCode(final Context ctx, final Exception e, final Object caller)
    {
        int hlrCode = 301;
        try
        {
            if (e instanceof AgentException)
            {
                String responseCodeStr = "";

                //should contain ==>  RESPONSECODE:1111 ]
                final String errorString = e.getMessage();
                if (errorString.indexOf("RESPONSECODEFROMHLR:") != 0)
                {
                    final int startPos = errorString.indexOf("RESPONSECODEFROMHLR:") + 20;
                    final char endSign = ']';
                    final int endSignPos = errorString.lastIndexOf(endSign);

                    responseCodeStr = errorString.substring(startPos, endSignPos);
                    hlrCode = Integer.parseInt(responseCodeStr.trim());
                }
            }
        }
        catch (Exception exp)
        {
            LogSupport.minor(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "Failed to parse hlr error, hlrError" + e + ", parsing error:" + exp);
        }

        return hlrCode;
    }

    
    
    public static List<SubscriberServices> preprocessing(Context ctx, Map<ServiceFee2ID, SubscriberServices> OSLMap, Subscriber subscriber) throws HomeException
    {
    	List<SubscriberServices> oldProvisionedServicesList = new ArrayList<SubscriberServices>();
    	
		SPGServiceStateMappingConfig spgServiceStateMappingConfig = getSPGProvisionedServiceStateMapping(ctx, subscriber);
		if(null == spgServiceStateMappingConfig)
		{
			throw new HomeException(SubscriberServicesBulkUpdateSupport.class.getName() +" Error in retrieving Provisioned service state config for SPG, Please check the configuration. ");
		}
		
		Map<Integer, SPGProvService> provServiceStates =  spgServiceStateMappingConfig.getSPGProvServices(); 
	
		oldProvisionedServicesList = SubscriberServicesBulkUpdateSupport.calculateOPSL(ctx, subscriber, OSLMap, oldProvisionedServicesList, provServiceStates);
		return oldProvisionedServicesList;
    }
    

    
    public static boolean postProcessing(Context ctx, Subscriber subscriber, Map <ServiceFee2ID, SubscriberServices> OSLMap, List<SubscriberServices> oldProvisionedServicesList, List <SubscriberServices> newProvisionedServicesList) throws HomeException,ProvisioningHomeException
    {
    	
    	boolean isHlrUpdated = true;
    	
		Map<ServiceFee2ID, SubscriberServices>  NSLMap =  SubscriberServicesSupport.getAllSubscribersServicesRecords(ctx, subscriber.getId());
		SPGServiceStateMappingConfig spgServiceStateMappingConfig = getSPGProvisionedServiceStateMapping(ctx, subscriber);
		Map<Integer, SPGProvService> provServiceStates =  spgServiceStateMappingConfig.getSPGProvServices();
		
		newProvisionedServicesList = SubscriberServicesBulkUpdateSupport.calculateNPSL(ctx, subscriber, NSLMap, newProvisionedServicesList, provServiceStates);
		
		boolean serviceChange = SubscriberServicesBulkUpdateSupport.isSubscriberServicesChange(ctx, oldProvisionedServicesList, newProvisionedServicesList);
    	boolean forceSync = ctx.getBoolean(Common.SYNC_EXTERNAL_SERVICES);
    	
    	LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "OSLMap............................................. "+OSLMap);
    	
		if(serviceChange || forceSync)
		{
			if(LogSupport.isDebugEnabled(ctx))
 			{
			    if(serviceChange)
			    {
			        LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "is service change.. ");
			    }
			    if(forceSync)
			    {
			        LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), " force sync generic parameter value is true. ");
			    }
 			}
			
			isHlrUpdated = sendCommandToSPG(ctx, subscriber, OSLMap, NSLMap);
		}
		else 
		{
			if(LogSupport.isDebugEnabled(ctx))
 			{
				LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "No Service change encountered, won't be calling SPG.");
 			}
		}
		return isHlrUpdated;
		
    }
  

}
