/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
/**
 * @author unmesh.sonawane@redknee.com
 * since June 2014
 * 
 * This functionality is written for Afrimax oneNDS.
 * Feature Name : Afrimax : Provisioning
 * This class return the GRR of all provisioned and un-provisioned subscriber services.
 * It is work for service type as external price plan.
 */

package com.trilogy.app.crm.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.trilogy.app.crm.bean.ExternalServiceType;
import com.trilogy.app.crm.bean.ExternalServiceTypeXInfo;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtension;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;

public class GrrForAddRemoveServiceListCalculator {
	
	private static final String LOG_CLASS_NAME_CONSTANT 				= GrrForAddRemoveServiceListCalculator.class.getName();
    
	private static final String SINGLE_SERVICE_DATA_SEPERATOR      		= ":";
    private static final String SERVICE_LIST_SEPERATOR             		= "\\|";
    
    private static String VOICE_EXTERNAL_SERVICE 						= "VOICE";
    private static String SMS_EXTERNAL_SERVICE 							= "SMS";
    private static String DATA_EXTERNAL_SERVICE 						= "DATA";
    private static String GRR_SERVICE_ID								= "GRR_SERVICE_ID";
    private static String GRR_PROVISIONED_SERVICE_LIST					= "addServiceListIds";
    private static String GRR_UNPROVISIONED_SERVICE_LIST				= "deleteServiceListIds";
    private static String GRR_SUBSCRIBER_REMAINING_SERVICE_LIST			= "remainingServiceListIds";
    private static String EXTERNAL_SERVICE_TYPE_MOBILE_DATA_BAR			= "MobileDataBar";
    private static String EXTERNAL_SERVICE_TYPE_ROAMING_DATA_BAR		= "RoamingDataBar";
    
    private static String EXTERNAL_SERVICE_TYPE_OUTGOING_CALL_BAR		= "OutgoingCallBar";
    private static String EXTERNAL_SERVICE_TYPE_INTERNATIONAL_CALL_BAR	= "InternationalCallBar";
    
    private static int odbocServiceCount								= 0;
    private static int odbgprsServiceCount	 							= 0;
    private static int ZERO												= 0;
    
    private static int EMPTY_STRING_FOR_PART 							= 25;
    private static int EMPTY_STRING_FOR_NAME 							= 28;    
    private static int EMPTY_STRING_FOR_SERVICE_PART 					= 21;
    private static int EMPTY_STRING_FOR_SERVICE_NAME 					= 23;
    
    
/**
 * 	
 * @param ctx
 * @param newServiceList
 * @param oldServiceList
 * @param subriberMSISDN
 * @return
 * @throws HomeInternalException
 * @throws HomeException
 * 
 *  THIS METHOD IS USE TO CREATE GRR WHEN SUBSCRIBER SERVICES ARE ADDED OR REMOVED.
 * IT WILL GENERATE GRR OF ADDED AND REMOVED SUBSCRIBER SERVICES FOR EXTERNAL SYSTEM
 * IT GENERATE GRR OF EXTERNAL SERVICE TYPE ONLY.
 * IT AFFECT IN BSS WHEN VOICE,SMS OR DATA SERVICE COMES FOR UNPROVISION.THEN IT UNPROVISIONED THE RESPECTIVE EXTERNAL SERVICE ALSO.     
 */
     
    public static String getAddAndRemoveServiceList(Context ctx, String newServiceList, String oldServiceList,String subriberMSISDN) throws HomeInternalException, HomeException
    {
    	
    	if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "getAddAndRemoveServiceList() : 'newServiceList' - " + newServiceList + ", 'oldServiceList' - " + oldServiceList);
        }
    	//In context we have set osl= " " & nsl=" " if we send it blank. we have done this to run the SOAP request because request is not execute if we send blank parameter.
    	// This we need to fic in SPG or in ESB to replace blank request with xsi:nil="true" in request variable.
    	if(oldServiceList == null || oldServiceList.equalsIgnoreCase(" "))
    	{
    		oldServiceList = "";
    	}
    	
    	if(newServiceList == null || newServiceList.equalsIgnoreCase(" "))
    	{
    		newServiceList = "";
    	}
        
        newServiceList 										= newServiceList.trim();
        oldServiceList 										= oldServiceList.trim();        
        
        List<Long> newServiceListIds 						= null;
        Map<Long, String> newServiceListDataMap 			= null;
        List<Long> oldServiceListIds 						= null;
        Map<Long, String> oldServiceListDataMap 			= null;        
        List<Long> remainingServiceListIds 					= null;
        List<Long> unprovisionServiceFromSubscriberListIds 	= null;
        StringBuilder sb 									= new StringBuilder();        
       
        if (newServiceList != null && !newServiceList.isEmpty())
        {
            String newServicesDataList[] 	= newServiceList.split(SERVICE_LIST_SEPERATOR);
            newServiceListIds 				= new ArrayList<Long>(newServicesDataList.length);
            newServiceListDataMap 			= new HashMap<Long, String>();
            
            for (String serviceData : newServicesDataList)
            {
                String[] serviceDetails 	= serviceData.split(SINGLE_SERVICE_DATA_SEPERATOR);
                long serviceId 				= Long.parseLong(serviceDetails[ZERO]);
                
                newServiceListIds.add(serviceId);
                newServiceListDataMap.put(serviceId, serviceData);
            }
        }
        else
        {        	    
            newServiceListIds 		= new ArrayList<Long>();
            newServiceListDataMap 	= new HashMap<Long, String>();
        }
        
        if (oldServiceList != null && !oldServiceList.isEmpty())
        {
            String oldServicesDataList[] 	= oldServiceList.split(SERVICE_LIST_SEPERATOR);
            oldServiceListIds 				= new ArrayList<Long>(oldServicesDataList.length);
            oldServiceListDataMap 			= new HashMap<Long, String>();
            
            for (String serviceData : oldServicesDataList)
            {
                String[] serviceDetails 	= serviceData.split(SINGLE_SERVICE_DATA_SEPERATOR);
                long serviceId 				= Long.parseLong(serviceDetails[ZERO]);
                
                oldServiceListIds.add(serviceId);
                oldServiceListDataMap.put(serviceId, serviceData);
            }
        }
        else
        {
            oldServiceListIds 		= new ArrayList<Long>();
            oldServiceListDataMap 	= new HashMap<Long, String>();
        }
        
        remainingServiceListIds 				= new ArrayList<Long>();
        unprovisionServiceFromSubscriberListIds = new  ArrayList<Long>();
        remainingServiceListIds.addAll(oldServiceListIds);
        
        
        //GET PROVISIONE SERVICE LIST
        List<Long> addServiceListIds = new ArrayList<Long>(newServiceListIds);
        addServiceListIds.removeAll(oldServiceListIds);
        
        //GET THE UNPROVISION SERVICE LIST
        List<Long> deleteServiceListIds = new ArrayList<Long>(oldServiceListIds);
        deleteServiceListIds.removeAll(newServiceListIds);
        
        //GET THE REMAING PROVISIONED SERVICE LIST        
        remainingServiceListIds.removeAll(addServiceListIds);
        remainingServiceListIds.removeAll(deleteServiceListIds);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "addServiceListIds:" + addServiceListIds+":deleteServiceListIds:"+deleteServiceListIds+":remainingServiceListIds:"+remainingServiceListIds);
        }
        
       Context subctx = ctx.createSubContext();
       
       //PUT THE ALL SERVICE LIST IN SUBCONTEXT FOR FOLLOWING SERVICE 
       //odbocProvision SERVICE   -- INTERNATIONAL CALL BAR & OUTGOING CALL BARRING
       //odbgprsProvision SERVICE -- MOBILE DATA BAR & ROAMING DATABAR
       ctx.put(GRR_PROVISIONED_SERVICE_LIST, addServiceListIds);
       ctx.put(GRR_UNPROVISIONED_SERVICE_LIST, deleteServiceListIds);
       ctx.put(GRR_SUBSCRIBER_REMAINING_SERVICE_LIST, remainingServiceListIds);
       
        //CHECK WHETHER SERVICE COMES AS VOICE,SMS OR DATA SERVICE
        List<Short> mandatoryServiceList = getVoiceSmsDataServiceAvailableForUnprovision(ctx,deleteServiceListIds);
        
        if(mandatoryServiceList != null && mandatoryServiceList.size()> ZERO)
        {
        	
        	for(short serviceIdType:mandatoryServiceList)
        	{
        			//IF VOICE,SMS,DATA COMES FOR UNPROVISION THEN 
        			// STEP 1 : CHECK EXTERNAL SERVICE COMES FOR UNPROVISION
        			// STEP 2 : CHECK EXTERNAL SERVICE COMES FOR PROVISION
        			// STEP 3 : CHECK EXTERNAL SERVICE SERVICE ALREADY IN PROVISION STATE
        			
        			// STEP 1 - START
        			long voiceServiceIdInUnprovision = getExternalServiceAgainstVoiceSmsDataService(ctx,deleteServiceListIds,serviceIdType);
        			
        			// STEP 2 - START
        			if(voiceServiceIdInUnprovision == ZERO)
        			{
        				long voiceServiceIdInProvision = getExternalServiceAgainstVoiceSmsDataService(ctx,addServiceListIds,serviceIdType);
        				
        				if(voiceServiceIdInProvision > ZERO)
        				{
        					addServiceListIds.remove(voiceServiceIdInProvision);
        				}else
        				{
        					// STEP 3 - START
        					long voiceServiceIdInAlreadyProvisionState = getExternalServiceAgainstVoiceSmsDataService(ctx,remainingServiceListIds,serviceIdType);
        					
        					if(voiceServiceIdInAlreadyProvisionState > ZERO)
        					{
        						unprovisionServiceFromSubscriberListIds.add(voiceServiceIdInAlreadyProvisionState);
        					}
        				}
        			}        			
        	}
        	
        	//UN PROVISION THE SERVICES FROM SUBSCRIBER
        	deleteServiceListIds.addAll(unprovisionServiceFromSubscriberListIds);
        	
        	//UMPROVISION THE PROVISIONED EXTERNAL SERVICE FOR SYNC UP WITH VOICE,DATA & SMS SERVICE
        	//IF VOICE,SMS,DATA SERVICE WILL COME FOR UNPROVISION THEN NEED TO UNPROVISION THE RESPECTIVE VOICE-EXTERNAL,SMS-EXTERNAL AND DATA-EXTERNAL SUBSCRIBER SERVICE.
        	// UNPROVISION THE SUBSCRIBER SERVICES IN BSS
        	unprovisionSubscriberExternalService(unprovisionServiceFromSubscriberListIds,subriberMSISDN,ctx);
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx,LOG_CLASS_NAME_CONSTANT, "mandatoryServiceList_Voice_SMS_Data:" + mandatoryServiceList+":unprovisionServiceFromSubscriberListIds:"+unprovisionServiceFromSubscriberListIds+":addServiceListIds:"+addServiceListIds+":deleteServiceListIds:"+deleteServiceListIds);
        }
        
        ///////////////////////////////////////////////// SERVICE PROVISION START HERE ///////////////////////////////////////////
        
        odbocServiceCount 		= ZERO;
        odbgprsServiceCount 	= ZERO;
        if(addServiceListIds != null && addServiceListIds.size()>0)
        {
        	String provisionedGRR = getServiceGRRdetails(ctx,addServiceListIds,"P");    
        	sb.append(provisionedGRR);
        }
        ////////////////////////////////////////////////////// UNPROVISION SERVICES START HERE ////////////////////////////////////////////
         
        if(deleteServiceListIds != null && deleteServiceListIds.size()>0)
        {
        	String unprovisionGRR = getServiceGRRdetails(ctx,deleteServiceListIds,"U");
        	sb.append(unprovisionGRR);
        }
        
         return sb.toString();
        
    }//END OF METHOD
  
/**   
 * @param unprovisionServiceFromSubscriberListIds
 * @param subriberMSISDN
 *
 * UNPROVISION THE VOICE-EXTERNAL,SMS-EXTERNAL,DATA-EXTERNAL SUBSCRIBER SERVICES 
 * IT COMES IN FEATURE WHEN VOICE,SMS OR DATA SERVICE COMES FOR UNPROVISION.
 */    
 private static void unprovisionSubscriberExternalService(
			List<Long> serviceListIdsForUnprovision,
			String subriberMSISDN , Context ctx) throws HomeException {
	 
	 			if(serviceListIdsForUnprovision != null && serviceListIdsForUnprovision.size() > ZERO)
	 			{
	 				 List<SubscriberServices> subServices = new ArrayList<SubscriberServices>();
	 				Subscriber subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, subriberMSISDN);	 				
	 				SubscriberServices object = null;
	 				
	 				 for (long serviceId : serviceListIdsForUnprovision)
	 				 {
	 					 object =  SubscriberServicesSupport.getSubscriberServiceRecord(ctx, subscriber.getId(), serviceId, SubscriberServicesUtil.DEFAULT_PATH);	        			 
	        	         subServices.add(object);
	 				 }//END OF FOR
	 				 try
               		 {  
	 					 if(subServices != null && subServices.size()> ZERO && subscriber != null)
	 					 { 
	 						if (LogSupport.isDebugEnabled(ctx))
	 				        {
	 				            LogSupport.debug(ctx,LOG_CLASS_NAME_CONSTANT, "subscriber:"+subscriber+":subServices:"+subServices);
	 				        }
	 						 SubscriberServicesSupport.unprovisionSubscriberServices(ctx, subscriber, subServices, subscriber, new HashMap<ExternalAppEnum, ProvisionAgentException>());        	          
	 						 SubscriberServicesSupport.deleteSubscriberServiceRecord(ctx, object);  
	 						 subscriber.setSuspendingEntities(true);
	 					 }
                     
	        		 }
	                 catch (HomeException e)
	                 {
	                     LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, "Failed to unprovision subscriber serice with service id : "+e);
	                 }
	 				 
	 			}//END OF IF
	 }

/**
 * 
 * @param ctx
 * @param serviceListIds
 * @param serviceIdType
 * @return
 * @throws HomeException
 * 
 * IF VOICE,SMS OR DATA SERVICE COMES FOR UNPROVISION THEN FOLLOWING METHOD GET THE EXTERNAL VOICE,SMS OR DATA SERVICE RESPECTIVELY.
 * IT RETURN THE COLLECTION OF EXTERNAL SERVICE TYPE OF VOICE,SMS,DATA. 
 */
 private static long getExternalServiceAgainstVoiceSmsDataService(Context ctx,
			List<Long> serviceListIds, short serviceIdType) throws HomeException {
		
	 
	 Service serviceBean = null;
	  long resultServiceId  = ZERO;
	  String serviceTypeValue = "";
	 
	if(serviceListIds != null && serviceListIds.size()>0)
	{
		 for (long serviceId : serviceListIds)
		   {
		 	  serviceBean = ServiceSupport.getService(ctx, serviceId);
			 
			 if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
	         { 
				 ExternalServiceTypeExtension externalServiceTypeExtension = ExtensionSupportHelper.get(ctx)
	                     .getExtension(ctx, serviceBean, ExternalServiceTypeExtension.class);
				 
				 
				 ExternalServiceType externalServiceType = getExternalServiceType(ctx,externalServiceTypeExtension.getExternalServiceType());
	     		 serviceTypeValue = externalServiceType.getServiceTypeValue();
				 
				 if(serviceIdType == ServiceTypeEnum.VOICE_INDEX)
				 {
					 if(serviceTypeValue.equalsIgnoreCase(VOICE_EXTERNAL_SERVICE))
					 { 
						 resultServiceId = serviceId;
						 return resultServiceId; 
					 }
					 
				 }
				 if(serviceIdType == ServiceTypeEnum.SMS_INDEX)
				 {
					 if(serviceTypeValue.equalsIgnoreCase(SMS_EXTERNAL_SERVICE))
				      	{
				      		 resultServiceId = serviceId;
				      		 return resultServiceId;
				      	}
				 }
				 if(serviceIdType == ServiceTypeEnum.DATA_INDEX)
				 {
					 if(serviceTypeValue.equalsIgnoreCase(DATA_EXTERNAL_SERVICE))
				      	{
				      		 resultServiceId = serviceId;
				      		 return resultServiceId;
				      	}
				 }
	         }
		   }
	}
		return resultServiceId;
	}



	/**
     * 
     * @param deleteServiceListIds
     * @param serviceBean
     * @return
	 * @throws HomeException 
	 * GET THE LIST OF VOICE,SMS,DATA SERVICE COMES FOR UNPROVISION. IN UNPROVISION LIST
     */    
   
    private static List<Short> getVoiceSmsDataServiceAvailableForUnprovision(Context ctx,
		List<Long> ServiceListIds) throws HomeException 
    {
    	
    	Service serviceBean = null;
    	List<Short> mandatoryServiceList = new ArrayList<Short>();
    	
    	if(ServiceListIds != null && ServiceListIds.size()>0)
    	{
	    	 for (long serviceId : ServiceListIds)
	    	    {
	    	    	 	serviceBean = ServiceSupport.getService(ctx, serviceId);
	    				
	    	        	  if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
	    	        	  {    	        		  
	    	        		  continue;
	    	        	  }else
	    	        	  {
	    	        		  if (serviceBean.getType().getIndex() == ServiceTypeEnum.VOICE_INDEX || serviceBean.getType().getIndex() == ServiceTypeEnum.SMS_INDEX || serviceBean.getType().getIndex() == ServiceTypeEnum.DATA_INDEX)
	    	        		  {
		    	        		mandatoryServiceList.add(serviceBean.getType().getIndex());
	    	        		  }
	    	        	  }
	    	    }//for
    	}
	
    	return  mandatoryServiceList;
    }

/**
 * 
 * @param ctx
 * @param serviceIdsList
 * @param Type - P-PROVISION , U - UNPROVISION 
 * @return
 * @throws HomeInternalException
 * @throws HomeException
 * 
 * CREATE THE GRR FOR SERVICE PROVISION & UNPROVISION.
 * IF TYPE = "P" IT MEANS GET ALL GRR OF PROVISIONING COMMAND
 * IF TYPE = "U" IT MEANS GET ALL GRR OF UN-PROVISIONING COMMAND
 */
	private static String getServiceGRRdetails(Context ctx,
			List<Long> serviceIdsList,String Type) throws HomeInternalException, HomeException {
		
		
		 	Service serviceBean 		= null;
	        StringBuilder sb 			= new StringBuilder();
	        StringBuilder sbServiceGrr 	= new StringBuilder();
	        Context subCtx 				= ctx.createSubContext();
	        String hlrCommand			= null;
		
	     if(serviceIdsList != null && serviceIdsList.size()>0)
	     {
			 for (long serviceId : serviceIdsList)
		        {
		            serviceBean = ServiceSupport.getService(ctx, serviceId);
					 
		            if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
		             {
			        	 ExternalServiceTypeExtension externalServiceTypeExtension = ExtensionSupportHelper.get(ctx)
			                     .getExtension(ctx, serviceBean, ExternalServiceTypeExtension.class);
		        	 
		        
		        		 ExternalServiceType externalServiceType 	= getExternalServiceType(ctx,externalServiceTypeExtension.getExternalServiceType());
		        		 String serviceTypeValue 					= externalServiceType.getServiceTypeValue();
		        		 
		        		 if((serviceTypeValue.equalsIgnoreCase(EXTERNAL_SERVICE_TYPE_ROAMING_DATA_BAR) || serviceTypeValue.equalsIgnoreCase(EXTERNAL_SERVICE_TYPE_MOBILE_DATA_BAR)) && odbgprsServiceCount > ZERO)
		        			 continue;
		        		 
		        		 if(serviceTypeValue.equalsIgnoreCase(EXTERNAL_SERVICE_TYPE_ROAMING_DATA_BAR) || serviceTypeValue.equalsIgnoreCase(EXTERNAL_SERVICE_TYPE_MOBILE_DATA_BAR))
		        		 {
		        			 odbgprsServiceCount++;
		        		 }
		        		 
		        		 if((serviceTypeValue.equalsIgnoreCase(EXTERNAL_SERVICE_TYPE_OUTGOING_CALL_BAR) || serviceTypeValue.equalsIgnoreCase(EXTERNAL_SERVICE_TYPE_INTERNATIONAL_CALL_BAR)) && odbocServiceCount > ZERO)
		        			 continue;
		        		 
		        		 if(serviceTypeValue.equalsIgnoreCase(EXTERNAL_SERVICE_TYPE_OUTGOING_CALL_BAR) || serviceTypeValue.equalsIgnoreCase(EXTERNAL_SERVICE_TYPE_INTERNATIONAL_CALL_BAR))
		        		 {
		        			 odbocServiceCount++;
		        		 }
		        		  
			        	 if(Type.equalsIgnoreCase("P"))
		        		 {
		        			 hlrCommand = externalServiceTypeExtension.getServiceListProvisionCommand();
		        		 }else if(Type.equalsIgnoreCase("U"))
		        		 {
		        			 hlrCommand = externalServiceTypeExtension.getServiceListUnProvisionCommand(); 
		        		 }
			        	 
			        	 if (LogSupport.isDebugEnabled(ctx))
		        	     {
		        	            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "hlrCommand:" + hlrCommand+":serviceId:"+serviceId);
		        	     }
			        	 
			            	 subCtx.put(getSERVICE_ID_KEY(), serviceId);
			            	
				          	//EXECUTE THE KEY VALUE CALCULATOR COMMAND 
				              InternalKeyValueCalculator calc = new InternalKeyValueCalculator();
				              calc.setInternalKey(hlrCommand);
				              sbServiceGrr = (StringBuilder)calc.getValueAdvanced(subCtx);
				              
				              sb.append(sbServiceGrr);	             
			               }
		        }//for
	       }
		 return sb.toString();
	}

/**
 * 
 * @param spaceValue
 * @return
 * 
 * GET THE EMPTY STRING
 * IT IS USE FOR FORMATTING THE GRR	
 */
    static String getEMPTY_STRING(int spaceValue)
    {
    	StringBuilder result = new StringBuilder();

    	for(int i = ZERO ; i < spaceValue; i++)
    	{
    	   result = result.append(' ');
    	}
    	return (result.toString());
    }
  
    // EMTRY STRING FOR GRR PART
    static String getEMPTY_STRING_For_PART()
    {
    	return getEMPTY_STRING(EMPTY_STRING_FOR_PART);
    }    
    // EMTRY STRING FOR GRR NAME
    static String getEMPTY_STRING_For_NAME()
    {
    	return getEMPTY_STRING(EMPTY_STRING_FOR_NAME);
    }
    // EMTRY STRING FOR GRR SERVICE PART
    static String getEMPTY_STRING_For_SERVICE_PART()
    {
    	return getEMPTY_STRING(EMPTY_STRING_FOR_SERVICE_PART);
    }
    // EMTRY STRING FOR GRR SERVICE NAME
    static String getEMPTY_STRING_For_SERVICE_NAME()
    {
    	return getEMPTY_STRING(EMPTY_STRING_FOR_SERVICE_NAME);
    }
    // GET THE GRR SERVICE ID 
    static String getSERVICE_ID_KEY()
    {
    	return GRR_SERVICE_ID;
    }
    
    /**
     * @param string
     * @return
     * @throws HomeException
     * @throws HomeInternalException
     * 
     * GET THE EXTERNAL SERVICE TYPE
     * IT IS USE FOR GETTING EXTERNAL SERVICE TYPE VALUE
     */
 
    private static ExternalServiceType getExternalServiceType(Context ctx, long externalServiceTypeId)
            throws HomeInternalException, HomeException
    {
        final And condition = new And();
        condition.add(new EQ(ExternalServiceTypeXInfo.ID, externalServiceTypeId));

        return HomeSupportHelper.get(ctx).findBean(ctx, ExternalServiceType.class, condition);
    }
   
}
