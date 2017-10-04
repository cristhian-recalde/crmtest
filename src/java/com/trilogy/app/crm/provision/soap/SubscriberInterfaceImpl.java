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

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;

import electric.util.holder.intOut;

/**
 * @author skushwaha
 * Code ported by amit.baid@redknee.com
 * 
 * Impl for SubscriberInterface to query Aux service and price plan
 * 
 */
public class SubscriberInterfaceImpl implements SubscriberInterface {

	public SubscriberInterfaceImpl(Context context) {
		context_ = context;
	}
	
	public SubscriberInterfaceImpl() {
	}
	
	public ArrayList getSubAuxSvcList(String userName, String password,
			long spid, String msisdn, String imsi, intOut retCode)
			throws Exception {
		if (LogSupport.isDebugEnabled(getContext())) {
			LogSupport.debug(getContext(), this, " start getSubAuxSvcList()");
		}
		Collection list = null;
    ArrayList subscriberAuxilaryServices = new ArrayList();

		try {
			int authCode = authenticateUser(userName, password);
			if (LogSupport.isDebugEnabled(getContext())) {
				LogSupport.debug(getContext(), this,
						" User authentication returnCode: " + authCode);
			}
			if (authCode == LOGIN_FAILED) {
				retCode.value = LOGIN_FAILED;
				return subscriberAuxilaryServices;
			}
			Subscriber subscriber = validateSubscriber(msisdn,imsi,spid,retCode);
			if (subscriber == null || retCode.value > 0)
			{
				return null;
			}
			list = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(getContext(), subscriber);
			for (Iterator i= list.iterator();i.hasNext();)
			{
				SubscriberAuxiliaryService subAuxSvc = (SubscriberAuxiliaryService)i.next();
				subscriberAuxilaryServices.add(Long.valueOf(subAuxSvc.getAuxiliaryServiceIdentifier()));
			}

		} catch (Throwable t) {
			LogSupport.info(getContext(), this,
					" Exception thrown in getSubAuxSvcList(): "
							+ t.getMessage(), t);
			retCode.value = INTERNAL_ERROR;
		}
		
		retCode.value = SUCCESSFUL;
		return subscriberAuxilaryServices;
	}

	public void provSubAuxSvcList(String userName, String password, long spid,
			String msisdn, String imsi, long auxServiceId, Date startDate,
			Date endDate, long numPayments,intOut retCode) throws Exception {
		if (LogSupport.isDebugEnabled(getContext())) {
			LogSupport.debug(getContext(), this, " start provSubAuxSvcList()");
		}
		SubscriberAuxiliaryService subscriberAuxiliaryService = null;
		try {
			int authCode = authenticateUser(userName, password);
			if (LogSupport.isDebugEnabled(getContext())) {
				LogSupport.debug(getContext(), this,
						" User authentication returnCode: " + authCode);
			}
			if (authCode == LOGIN_FAILED) {
          retCode.value = LOGIN_FAILED;
          return;
			}
			// Provision an Aux Service for a subscriber
			if (numPayments <= 0)
			{
				retCode.value = INVALID_PAYMENT_NUMBER;
				return;
			}
			if (startDate == null || (endDate == null && numPayments <= 0))
			{
				retCode.value = STARTDATE_OR_ENDDATE_NULL;
				return;
			}
			else if( startDate.after(endDate))
			{
				retCode.value = ENDDATE_LESS_THAN_STARTDATE;
				return;
			}
			else if (startDate.before(CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(new Date())))
			{
				retCode.value = STARTDATE_LESS_THAN_CURRENTDATE;
				return;
			}
			if (endDate == null)
			{
				endDate = CalendarSupportHelper.get(getContext()).findDateMonthsAfter((int)numPayments, startDate);
			}
			Subscriber subscriber = validateSubscriber(msisdn,imsi,spid,retCode);
			if ( subscriber == null || retCode.value > 0)
			{
				return;
			}

			if ( AuxiliaryServiceSupport.getAuxiliaryService(getContext(),auxServiceId) == null)
			{
				retCode.value = INVALID_AUX_SERVICE_ID;
				return;
			}
			
			if (isDupilcateSubscriberAuxService(subscriber,auxServiceId))
			{
				retCode.value = INTERNAL_ERROR;
				return;
			}

			subscriberAuxiliaryService = SubscriberAuxiliaryServiceSupport
					.createAssociation(getContext(), subscriber, auxServiceId,
							startDate, endDate,(int)numPayments);

		} catch (Throwable t) {
			LogSupport.info(getContext(), this,
					" Exception thrown in provSubAuxSvcList(): "
							+ t.getMessage(), t);
			retCode.value = INTERNAL_ERROR;
			return;
		}
		if (subscriberAuxiliaryService != null)
		{
			retCode.value = SUCCESSFUL;
			return;
		}
		else
		{
			retCode.value = FAILED_PROVISIONING;
			return;
		}
	}

	public void deProvSubAuxSvcList(String userName, String password, long spid,
			String msisdn, String imsi, long auxSvcId,intOut retCode) throws Exception {
		if (LogSupport.isDebugEnabled(getContext())) {
			LogSupport.debug(getContext(), this,
					" start deProvSubAuxSvcList()");
		}
		try {
			int authCode = authenticateUser(userName, password);
			if (LogSupport.isDebugEnabled(getContext())) {
				LogSupport.debug(getContext(), this,
						" User authentication returnCode: " + authCode);
			}
			if (authCode == LOGIN_FAILED) {
          retCode.value = LOGIN_FAILED;
          return;
			}
			// Provision an Aux Service for a subscriber
			Subscriber subscriber = validateSubscriber(msisdn,imsi,spid,retCode);
			if (subscriber == null || retCode.value > 0)
			{
				return;
			}
			if ( AuxiliaryServiceSupport.getAuxiliaryService(getContext(),auxSvcId) == null)
			{
				retCode.value = INVALID_AUX_SERVICE_ID;
				return;
			}
			SubscriberAuxiliaryServiceSupport.removeAssociationForSubscriber(getContext(),
					subscriber, auxSvcId, SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED);

		} catch (Throwable t) {
			LogSupport.info(getContext(), this,
					" Exception thrown in deProvSubAuxSvcList(): "
							+ t.getMessage(), t);
			retCode.value = INTERNAL_ERROR;
		}
		retCode.value = SUCCESSFUL;
	}

	public ArrayList getPricePlanList(final String userName,
			final String password, long spid, intOut retCode) throws Exception {
		if (LogSupport.isDebugEnabled(getContext())) {
			LogSupport.debug(getContext(), this, " start getPricePlanList()");
		}
		
		try {
			int authCode = authenticateUser(userName, password);
			if (LogSupport.isDebugEnabled(getContext())) {
				LogSupport.debug(getContext(), this,
						" User authentication returnCode: " + authCode);
			}
			if (authCode == LOGIN_FAILED) {
				retCode.value = LOGIN_FAILED;
				return null;
			}
		} catch (Throwable t) {
			LogSupport.info(getContext(), this,
					" Exception thrown in getPricePlanList(): "
							+ t.getMessage(), t);
			retCode.value = LOGIN_FAILED;
		}
		
    if (SpidSupport.getCRMSpid(getContext(),(int)spid) == null)
    {
      retCode.value = INVALID_SPID;
      return null;
    }
		Collection list = PricePlanSupport.getPricePlanList(getContext(), (int) spid);
		ArrayList pricePlanList = new ArrayList();
		try
		{
			for (Iterator i= list.iterator();i.hasNext();)
			{
				PricePlan pricePlan = (PricePlan)i.next();
				pricePlanList.add(Long.valueOf(pricePlan.getId()));
			}
		}
		catch (Throwable t) {
			LogSupport
					.info(getContext(), this,
							" Exception thrown in getPricePlanList(): "
									+ t.getMessage(), t);
			retCode.value = INTERNAL_ERROR;
		}
		return pricePlanList;
	}

	public long getSubPricePlan(final String userName, final String password,
			long spid, String msisdn, String imsi, intOut retCode)
			throws Exception {
    long pricePlanId = -1;
		if (LogSupport.isDebugEnabled(getContext())) {
			LogSupport.debug(getContext(), this, " start getSubPricePlan()");
		}
		Subscriber subscriber = null;
		try {
			int authCode = authenticateUser(userName, password);
			if (LogSupport.isDebugEnabled(getContext())) {
				LogSupport.debug(getContext(), this,
						" User authentication returnCode: " + authCode);
			}
			if (authCode == LOGIN_FAILED) {
				retCode.value = LOGIN_FAILED;
				return pricePlanId;
      }  
			subscriber = validateSubscriber(msisdn,imsi,spid,retCode);
			if (subscriber == null || retCode.value > 0)
			{
				return -1;
			}
		} catch (Throwable t) {
			LogSupport
					.info(getContext(), this,
							" Exception thrown in getSubPricePlan(): "
									+ t.getMessage(), t);
			retCode.value = INTERNAL_ERROR;
		}
		if (subscriber != null)
    {
      pricePlanId = subscriber.getPricePlan();
    }
		return pricePlanId;
	}

	public void switchSubPricePlan(final String userName, final String password,
			long spid, String msisdn, String imsi, long newPricePlanId, intOut retCode)
			throws Exception {
		if (LogSupport.isDebugEnabled(getContext())) {
			LogSupport
					.debug(getContext(), this, " start switchSubPricePlan()");
		}
     try
     {
			int authCode = authenticateUser(userName, password);
			if (LogSupport.isDebugEnabled(getContext())) {
				LogSupport.debug(getContext(), this,
						" User authentication returnCode: " + authCode);
			}
			if (authCode == LOGIN_FAILED) {
          retCode.value = LOGIN_FAILED;
          return;
			}
			
			Home subHome = (Home) getContext().get(SubscriberHome.class);
			if (subHome == null) {
				retCode.value = INTERNAL_ERROR;
				return;
			}
			Subscriber subscriber = validateSubscriber(msisdn,imsi,spid,retCode);
			if (subscriber == null || retCode.value > 0)
			{
				return;
			}
			PricePlan subPricePlan = PricePlanSupport.getPlan(getContext(), newPricePlanId);
			if ( subPricePlan == null || subPricePlan.getPricePlanType() != subscriber.getSubscriberType() || !(subPricePlan.isEnabled()) ) {
				// log message
				retCode.value = INVALID_PRICE_PLAN;
				return;
			}
			// check for old and new priceplan are not equal
			if (subscriber.getPricePlan() != newPricePlanId) {
                // TODO 2006-10-24 this will not work properly with default services
                subscriber.setPricePlan(newPricePlanId);
				//System.out.println("Subscriber with msisdn" +subscriber.getMSISDN() +" switch to price plan -- "+newPricePlanId);
				try
				{
					subHome.store(subscriber);
				}
				catch(Exception e)
				{
					if (LogSupport.isDebugEnabled(getContext())) {
						LogSupport
								.debug(getContext(), this, e.toString());
					}
					retCode.value = FAILED_PROVISIONING;
					return;
				}
				if (LogSupport.isDebugEnabled(getContext())) {
						LogSupport
								.debug(getContext(), this, " After switch the price plan id is "+subscriber.getPricePlan());
				}
				//System.out.println("After switch the price plan id is -- "+subscriber.getPricePlan());
			}
		} catch (Throwable t) {
			LogSupport.info(getContext(), this,
					" Exception thrown in switchSubPricePlan(): "
							+ t.getMessage(), t);
			retCode.value = INTERNAL_ERROR;
			return;
		}
		retCode.value = SUCCESSFUL;
	}

	public Context getContext() {
		return context_;
	}

	private int authenticateUser(final String login, final String passwd) {
      Context subCtx = getContext().createSubContext();
      int result = LOGIN_FAILED;
      if(LogSupport.isDebugEnabled(subCtx))
      {
          LogSupport.debug(subCtx, this, " start authenticateUser()");
      }
      
      try
      {
          Session.setSession(subCtx, subCtx);
          AuthSPI auth = (AuthSPI) subCtx.get(AuthSPI.class);
          auth.login(subCtx, login, passwd);
          if(AuthSupport.hasPermission(subCtx, permission_))
          {
              result = SUCCESSFUL;
          }          
          else
          {
              result = LOGIN_FAILED;
          }
      }
      catch (LoginException le)
      {
          result = LOGIN_FAILED;
      }
      return result;
	}

	private Context context_;

	private Permission permission_ = new SimplePermission(
			Common.PROVISIONERS_GROUP_PERMISSION);

	/* Validate the subscriber for specific msidn, imsi and spid */
	
	private Subscriber validateSubscriber(String msisdn, String imsi,
			long spid, intOut retCode) {
		Subscriber subscriber = null;
		try {
			// get the active subscriber

			subscriber = SubscriberSupport.lookupSubscriberForMSISDN(
					getContext(), msisdn);
			// get all AuxSvc's of the subscriber
			if (subscriber == null) {
				retCode.value = INVALID_MSISDN;
				return null;

			} 
			else if ( subscriber.getState() == SubscriberStateEnum.PENDING || subscriber.getState() == SubscriberStateEnum.INACTIVE)
			{
				retCode.value = INVALID_SUBSCRIBER_STATE;
				return null;
			}
			else if (!subscriber.getIMSI().equals(imsi)) {
			
				retCode.value = INVALID_IMSI;
				return null;

			} else if (subscriber.getSpid() != spid) {
				retCode.value = INVALID_SPID;
				return null;
			}
		} 
		catch (HomeException e) {
			LogSupport.info(getContext(), this,
					" Exception thrown in validateSubscriber(): "
							+ e.getMessage(), e);
			retCode.value = INVALID_MSISDN;
			return null;
		}
		catch (Exception e) {
			LogSupport.info(getContext(), this,
					" Exception thrown in validateSubscriber(): "
							+ e.getMessage(), e);
			retCode.value = INTERNAL_ERROR;
			return null;
		}
		return subscriber;
	}
	
	 private boolean isDupilcateSubscriberAuxService(Subscriber sub, long newAuxServiceId)
	    {
	    	  Collection auxServiceCol = sub.getAuxiliaryServices(getContext());
	          Home auxiliaryServiceHome = (Home) getContext().get(AuxiliaryServiceHome.class);

	          if (auxServiceCol != null)
	          {
	              for (Iterator i = auxServiceCol.iterator(); i.hasNext();)
	              {
	                  SubscriberAuxiliaryService subService = (SubscriberAuxiliaryService) i.next();

	                  long auxServiceId = subService.getAuxiliaryServiceIdentifier();

	                  try
	                  {
	                      AuxiliaryService service = (AuxiliaryService) auxiliaryServiceHome
	                              .find(getContext(), Long.valueOf(auxServiceId));
	                      if (service.getIdentifier() == newAuxServiceId)
	                      {
	                    	  LogSupport.info(getContext(), this,
	  	          					" Duplicate auxiliary service id: "
	  	          							+service.getIdentifier()+" for subscriber "+sub.getMSISDN(), null);
	                    	  return true;
	                      }
	                  }
	                  catch (HomeException e)
	                  {
	                	  LogSupport.info(getContext(), this,
	          					" Exception thrown in isDupilcateSubscriberAuxService(): "
	          							+ e.getMessage(), e);
	                  }

	              }
	          }
			return false;
	    }
	
}
