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
package com.trilogy.app.crm.subscriber.provision;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.TFAAuxiliaryServiceExtensionTypeEnum;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.client.ITFAAuxiliaryServiceClient;
import com.trilogy.app.crm.client.TFAAuxiliarServiceClientException;
import com.trilogy.app.crm.client.TFAAuxiliaryServiceClientFactory;
import com.trilogy.app.crm.extension.auxiliaryservice.core.TFAAuxSvcExtension;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListProvisionRequest;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListProvisionResponse;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListQueryResponse;
import com.trilogy.app.transferfund.corba.provision.RestrictionListType;
import com.trilogy.app.transferfund.corba.provision.SubsRole;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/*
 * Support Class to invoke TFA API's 
 * 
 * @author bhagyashree.dhavalshankh@redknee.com
 * 
 */
public class TFAAuxServiceSupport {

	private static final String MODULE = TFAAuxServiceSupport.class.getSimpleName();
	
	private static final Collection<SubscriberStateEnum> UNPROVISION_STATES = new HashSet<SubscriberStateEnum>(
			Arrays.asList(SubscriberStateEnum.PENDING,
					SubscriberStateEnum.INACTIVE));

	private static final Collection<SubscriberStateEnum> PROVISIONED_STATES = new HashSet<SubscriberStateEnum>(
			Arrays.asList(SubscriberStateEnum.ACTIVE,
					SubscriberStateEnum.SUSPENDED));
	
	public static final int TFA_SAME_TYPE_AUX_SERVICE_EXCEPTION = 147;//Arbitrary number just to ensure that it is specific TFA exception. 

	//private static TFAAuxiliaryServiceClient tfaAuxiliaryServiceClient = null;
	
	/**
	 *Provision service on TFA.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param subscriber
	 *            The subscriber to be provisioned.
	 * @param tfaAuxServiceList
	 *            List of services to be provisioned.
	 * @throws TFAAuxiliarServiceClientException
	 * @throws HomeException 
	 */
	public static void createTFAService(Context ctx, Subscriber sub, List<AuxiliaryService> tfaAuxServiceList) throws TFAAuxiliarServiceClientException, HomeException 
	{

		
		TFAAuxiliaryServiceClientFactory tfaClientFactory = TFAAuxiliaryServiceClientFactory.getInstance(ctx);
		ITFAAuxiliaryServiceClient tfaAuxiliaryServiceClient = tfaClientFactory.getTfaClient(ctx, sub.getSpid());
	
		LogSupport.info(ctx, MODULE, "Attempting provisioning on TFA...");
		HTMLExceptionListener el= (HTMLExceptionListener)ctx.get(ExceptionListener.class); 

		
		BlackWhiteListProvisionRequest [] request = null;
		List<BlackWhiteListProvisionRequest> requestList = new ArrayList<BlackWhiteListProvisionRequest>();

		
		AuxiliaryServiceTypeEnum serviceType = null;
		BlackWhiteListProvisionRequest blackWhiteProvRequestObject = null;
		BlackWhiteListProvisionResponse[] res = null;

		int resultCode = -1;
		for (AuxiliaryService auxiliaryService : tfaAuxServiceList) 
		{
			 TFAAuxSvcExtension tfaExtension = TFAAuxSvcExtension.getTfaServiceExtention(ctx, auxiliaryService.getIdentifier());
		        	    		
			if (shouldProvision(ctx, sub, auxiliaryService)) 
			{
				serviceType = auxiliaryService.getType(); 

				blackWhiteProvRequestObject = getBlackWhiteReqObject(ctx,sub,serviceType, tfaExtension);

				requestList.add(blackWhiteProvRequestObject);
				request = requestList.toArray(new BlackWhiteListProvisionRequest[requestList.size()]);

				res = tfaAuxiliaryServiceClient.addWhiteListBlackList(ctx, request);
						
				resultCode = res[0].responseCode;
				
				if(resultCode != 0)
				{
					String msg = "Failed to Provision Auxiliary Service: "+auxiliaryService.getName()+" on TFA Server.";;
					if(resultCode == 3){
						msg = msg + "Please verify Auxiliary Service type is configured on TFA Server.";
					}
				
					
					if(LogSupport.isDebugEnabled(ctx))
					{
						LogSupport.debug(ctx, MODULE, "Failed to provision TFA Service"+auxiliaryService.getName()+". Returned response code:"+resultCode);
					}

					TFAAuxiliarServiceClientException t = new TFAAuxiliarServiceClientException("Failed to provision TFA Services:"+auxiliaryService.getName());
					if(el != null){
						el.thrown(new Exception(msg,t));					
					}
				
				}
				else
				{
					if(LogSupport.isDebugEnabled(ctx))
					{
						LogSupport.debug(ctx, MODULE, "TFA services provisioned :"+auxiliaryService.getName()+"successfully.");
					}
				}

			}
		}

	}

	
	/**
	 *To query TFA to check existence of MSISDN in case of service addition/updation.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param subscriber
	 *            The subscriber to be provisioned.
	 * @param subRole 
	 * 			  Contributer or Recipient or Combination
	 * @param restList
	 *            Allowed or Restricted.
	 * @throws TFAAuxiliarServiceClientException
	 * @throws HomeException 
	 */
	
	public static boolean queryMsisdn(final Context ctx, Subscriber sub ,SubsRole subRole,RestrictionListType restList) throws TFAAuxiliarServiceClientException, HomeException{
		
		TFAAuxiliaryServiceClientFactory tfaClientFactory = TFAAuxiliaryServiceClientFactory.getInstance(ctx);
		ITFAAuxiliaryServiceClient tfaAuxiliaryServiceClient = tfaClientFactory.getTfaClient(ctx, sub.getSpid());
		
		String [] msisdnList = {sub.getMsisdn()};
		try 
		{
			BlackWhiteListQueryResponse[] response = tfaAuxiliaryServiceClient.queryWhiteBlackList(ctx, msisdnList);
			SubsRole querysubRole = null;
			RestrictionListType queryRestListType = null;
			 
			if(response.length > 0)
			{
	     		for (BlackWhiteListQueryResponse b: response)
	     		{
	     			querysubRole = b.subrole;
	     			queryRestListType = b.listtype;
	     			
	     			if(subRole.equals(querysubRole) && restList.equals(queryRestListType))
	     			{
	     				return true;
	     			}
				}
			}
     		return false;
		}
		catch (TFAAuxiliarServiceClientException e) 
		{
			String msg = "Failed to query msisdn on TFA server: " + e.getMessage();
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, MODULE, msg );
			}
			throw new TFAAuxiliarServiceClientException(msg);
		}
	}
	
	
	/**
	 *To unprovision TFA services for a subscriber .
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param subscriber
	 *            The subscriber to be provisioned.
	 * @param tfaAuxServiceList 
	 * 			  List of services to unprovision.
	 * @throws TFAAuxiliarServiceClientException
	 * @throws HomeException 
	 */
	public static void removeWhiteBlackList(final Context ctx,Subscriber subscriber ,List<AuxiliaryService> tfaAuxServiceList ) throws TFAAuxiliarServiceClientException, HomeException 
	{
		
		
		
		
		TFAAuxiliaryServiceClientFactory tfaClientFactory = TFAAuxiliaryServiceClientFactory.getInstance(ctx);
		ITFAAuxiliaryServiceClient tfaAuxiliaryServiceClient = tfaClientFactory.getTfaClient(ctx, subscriber.getSpid());
		
		if(tfaAuxServiceList.isEmpty())
		{
			return;
		}

		HTMLExceptionListener el= (HTMLExceptionListener)ctx.get(ExceptionListener.class); 
		BlackWhiteListProvisionRequest [] request =null;
		List<BlackWhiteListProvisionRequest> requestList = new ArrayList<BlackWhiteListProvisionRequest>();
		
		BlackWhiteListProvisionRequest blackwhiteProvRequest = null;
		BlackWhiteListProvisionResponse[] response = null;
	
		AuxiliaryServiceTypeEnum serviceType = null;
		try
		{
			int resultCode = -1;
			Map<AuxiliaryService, Integer> mpResultForIndividualService = new HashMap<AuxiliaryService, Integer>();
			
			for (AuxiliaryService auxiliaryService : tfaAuxServiceList) 
			{
				TFAAuxSvcExtension tfaExtension = TFAAuxSvcExtension.getTfaServiceExtention(ctx, auxiliaryService.getIdentifier());
				if (shouldUnProvision(ctx, subscriber, auxiliaryService)) 
				{
					serviceType = auxiliaryService.getType();

					blackwhiteProvRequest = getBlackWhiteReqObject(ctx, subscriber, serviceType, tfaExtension);
					requestList.add(blackwhiteProvRequest);
					request = requestList.toArray(new BlackWhiteListProvisionRequest[requestList.size()]);

					response = tfaAuxiliaryServiceClient.removeWhiteBlackList(ctx, request);
					resultCode = response[0].responseCode;
					
					mpResultForIndividualService.put(auxiliaryService, resultCode);
					if(resultCode != 0)
					{
						String msg = "Failed to Provision Auxiliary Service: "+auxiliaryService.getName()+" on TFA Server.";;
						if(resultCode == 3){
							msg = msg + "Please see TFA SPID level configuration.";
						}
						if(LogSupport.isDebugEnabled(ctx))
						{
							LogSupport.debug(ctx, MODULE, msg+". Returned result code :"+resultCode);
						}
						if(el != null){
							el.thrown(new Exception(msg,new TFAAuxiliarServiceClientException("")));					
						}
					}
					else
					{
						String msg = "Unprovisioned TFA service(s):"+auxiliaryService.getName()+" successfully.";
						if(LogSupport.isDebugEnabled(ctx))
						{
							LogSupport.debug(ctx, MODULE, msg);
						}
					}
					
				}
			}
					
		}
		catch (TFAAuxiliarServiceClientException e) 
		{
			String msg = "Provisioning for TFA service(s) failed due to "+e.getMessage();
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, MODULE, msg );
			}
			throw e;
		}
	}


	/**
	 * Updates TFA services while MSISDNB change. Removes from previous MSISDN and assign the same to new one
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param oldMsisdn
	 *            MSISDN to remove services from.
	 * @param newMsisdn
	 *            MSISDN to add services to.
	 * @return Whether this Auxiliary service can be provisioned to TFA or not.
	 * @throws HomeException 
	 */
	
	public static void changeMsisdn(final Context ctx, Subscriber oldSubscriber, Subscriber newSubscriber) throws TFAAuxiliarServiceClientException 
	{
		
		TFAAuxiliaryServiceClientFactory tfaClientFactory = TFAAuxiliaryServiceClientFactory.getInstance(ctx);
		ITFAAuxiliaryServiceClient tfaAuxiliaryServiceClient = tfaClientFactory.getTfaClient(ctx, newSubscriber.getSpid());
		
		try 
		{
			boolean success = tfaAuxiliaryServiceClient.changeMsisdn(ctx, oldSubscriber.getMsisdn(), newSubscriber.getMsisdn());
			if(!success)
			{
				throw new TFAAuxiliarServiceClientException("Auxiliary Services provisioning while Change MSISDN failed for MSISDNs: "+oldSubscriber.getMsisdn()+" and "+newSubscriber.getMsisdn());
			}
			else
			{
				LogSupport.info(ctx, MODULE, "TFA services provisioned while changeMSISDN for MSISDNs: "+oldSubscriber.getMsisdn()+" and "+newSubscriber.getMsisdn());
			}
		}
		catch (TFAAuxiliarServiceClientException e) 
		{
			String msg = "Provisioning for TFA service(s) failed while changeMSISDN due to "+e.getMessage();
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, MODULE, msg );
			}
			throw e;
		}
	}
	
	 
	
	/**
	 * Whether the given association can be provisioned.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param subscriber
	 *            The subscriber to be provisioned.
	 * @param auxService
	 *            The auxiliary service to be provisioned.
	 * @return Whether this Auxiliary service can be provisioned to TFA or not.
	 */
	private static boolean shouldProvision(final Context ctx,
			final Subscriber subscriber, final AuxiliaryService auxService) 
	{
		boolean provision = true;
		if (auxService == null || subscriber == null) 
		{
			provision = false;
		}
		else if (EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber, UNPROVISION_STATES)) 
		{
			provision = false;
		}

		return provision;
	}
	
	
	
	/**
	 * Whether the given association can be unprovisioned.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param subscriber
	 *            The subscriber to be provisioned.
	 * @param auxService
	 *            The auxiliary service to be provisioned.
	 * @return Whether this Auxiliary service can be unprovisioned to TFA or not.
	 */
	private static boolean shouldUnProvision(final Context ctx,
			final Subscriber subscriber, final AuxiliaryService auxService)
	{
		boolean unprovision = true;
		if (auxService == null || subscriber == null)
		{
			unprovision = false;
		}
		else if (EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber,
				PROVISIONED_STATES))
		{
			unprovision = true;
		}
		return unprovision;

	}
	
	
	
	/**
	 * Remove TFA service on TFA server for a subscriber.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param sub
	 *           Services of given subscriber 
	 * @param auxSrv
	 * 			 Auxiliary Service to be unprovisioned. 
	 * @throws HomeException 
	 */
	
	public static void unprovisionSuspendedService(final Context ctx, Subscriber sub , AuxiliaryService auxSrv) throws TFAAuxiliarServiceClientException, HomeException{

		TFAAuxiliaryServiceClientFactory tfaClientFactory = TFAAuxiliaryServiceClientFactory.getInstance(ctx);
		ITFAAuxiliaryServiceClient tfaAuxiliaryServiceClient = tfaClientFactory.getTfaClient(ctx, sub.getSpid());
		
		AuxiliaryServiceTypeEnum serviceType = auxSrv.getType();

		TFAAuxSvcExtension tfaExtension = TFAAuxSvcExtension.getTfaServiceExtention(ctx, auxSrv.getIdentifier());
		BlackWhiteListProvisionRequest [] request = { getBlackWhiteReqObject(ctx,sub, serviceType, tfaExtension)};
		
		if(LogSupport.isDebugEnabled(ctx))
			LogSupport.debug(ctx, MODULE, "Unprovisioning suspended service :"+auxSrv.getType()+" on TFA server.");		

		tfaAuxiliaryServiceClient.removeWhiteBlackList(ctx, request);
	}
	
	/**
	 * Get BlacWhite list request object.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param sub
	 *           Services of given subscriber 
	 * @param serviceType
	 * 			 Auxiliary Service Type. 
	 * @param tfaExtension 
	 * @return BlackWhiteListProvisionRequest object.
	 */
	
	public static BlackWhiteListProvisionRequest getBlackWhiteReqObject(final Context ctx,Subscriber sub, AuxiliaryServiceTypeEnum serviceType, TFAAuxSvcExtension tfaExtension){
	
		
		SubsRole subsRole = null;
		RestrictionListType restListType = null;
		
		switch (tfaExtension.getTfaServiceExtention().getIndex()) {
		case TFAAuxiliaryServiceExtensionTypeEnum.TFA_CONTR_BLACKLIST_INDEX:
			subsRole = SubsRole.CONTRIBUTOR;
			restListType = RestrictionListType.RESTRICTED;
			break;
		case TFAAuxiliaryServiceExtensionTypeEnum.TFA_CONTR_WHITELIST_INDEX:
			subsRole = SubsRole.CONTRIBUTOR;
			restListType = RestrictionListType.ALLOWED;
			break;
		case TFAAuxiliaryServiceExtensionTypeEnum.TFA_RECP_BLACKLIST_INDEX:
			subsRole = SubsRole.RECIPIENT;
			restListType = RestrictionListType.RESTRICTED;
			break;
		case TFAAuxiliaryServiceExtensionTypeEnum.TFA_RECP_WHITELIST_INDEX:
			subsRole = SubsRole.RECIPIENT;
			restListType = RestrictionListType.ALLOWED;
			break;
		}
		
		if(LogSupport.isDebugEnabled(ctx))
			LogSupport.debug(ctx, MODULE, "Creating BlackWhite List request object.");		
		
		BlackWhiteListProvisionRequest request = new BlackWhiteListProvisionRequest(sub.getMsisdn(),subsRole,restListType);
	
		return request;
		
	}
	
    /**
    * Add TFA service on TFA server for a subscriber.
    * 
     * @param ctx
    *            The operating context.
    * @param sub
    *           Services of given subscriber 
     * @param auxSrv
    *                    Auxiliary Service to be provisioned via resume service trigger. 
     * @throws HomeException 
     */
    
    public static void provisionSuspendedService(final Context ctx, Subscriber sub , AuxiliaryService auxSrv) throws TFAAuxiliarServiceClientException, HomeException{
           
    		TFAAuxiliaryServiceClientFactory tfaClientFactory = TFAAuxiliaryServiceClientFactory.getInstance(ctx);
    		ITFAAuxiliaryServiceClient tfaAuxiliaryServiceClient = tfaClientFactory.getTfaClient(ctx, sub.getSpid());
		
           AuxiliaryServiceTypeEnum serviceType = auxSrv.getType();

           TFAAuxSvcExtension tfaExtension = TFAAuxSvcExtension.getTfaServiceExtention(ctx, auxSrv.getIdentifier());
           BlackWhiteListProvisionRequest [] request = { getBlackWhiteReqObject(ctx,sub, serviceType, tfaExtension)};
           
           if(LogSupport.isDebugEnabled(ctx))
                  LogSupport.debug(ctx, MODULE, "provisioning suspended service :"+auxSrv.getType()+" on TFA server.");          

           tfaAuxiliaryServiceClient.addWhiteListBlackList(ctx, request);
    }

    
    public static List <AuxiliaryService> getTFAAuxiliaryServices(final Context ctx, Subscriber sub) 
	{

    	List<AuxiliaryService> list = new ArrayList<AuxiliaryService>();

		final Collection<SubscriberAuxiliaryService> auxServiceCol = sub.getProvisionedAuxiliaryServices(ctx);
	
		if(LogSupport.isDebugEnabled(ctx))
			LogSupport.debug(ctx, MODULE, "Fetch TFA services for subscriber"+sub.getId());	
		
		if (auxServiceCol != null) 
		{
			for (SubscriberAuxiliaryService subService : auxServiceCol) 
			{
				try 
				{
					
					final AuxiliaryService service = subService
							.getAuxiliaryService(ctx);
					AuxiliaryServiceTypeEnum auxSvEnum = service.getType();
					if (auxSvEnum == AuxiliaryServiceTypeEnum.TFA)
					{
						list.add(service);
					}
				} catch (final HomeException e) 
				{
					new InfoLogMsg(StateChangeAuxiliaryServiceSupport.class,
							"AuxilaryService ["
									+ subService
											.getAuxiliaryServiceIdentifier()
									+ "] for Subscriber [" + sub.getId()
									+ "] is not available", e).log(ctx);
				}
			}
		}
		return list;
	}

}
