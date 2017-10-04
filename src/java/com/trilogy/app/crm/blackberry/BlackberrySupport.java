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
package com.trilogy.app.crm.blackberry;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.ipc.IpcProvConfig;
import com.trilogy.app.crm.blackberry.error.ERErrorHandler;
import com.trilogy.app.crm.blackberry.error.ErrorHandler;
import com.trilogy.app.crm.extension.service.BlackberryServiceExtension;
import com.trilogy.app.crm.extension.service.BlackberryServiceExtensionHome;
import com.trilogy.app.crm.extension.service.BlackberryServiceExtensionXInfo;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.service.blackberry.model.Attributes;
import com.trilogy.service.blackberry.model.BBConfig;
import com.trilogy.service.blackberry.model.BBConfigXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
/**
 * Support class for all things Blackberry
 * 
 * @author angie.li
 *
 */
public class BlackberrySupport 
{
    /**
     * Error Code for CRM to track Provisioning Errors.
     */
    public static final int BLACKBERRY_PROVISION_ERRORCODE = 3021;
    
    
    /**
     * Returns TRUE, if Blackberry license is enabled.
     * @param ctx
     * @return
     */
    public static boolean isBlackberryEnabled(Context ctx) 
    {
        LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        return lMgr.isLicensed(ctx, LicenseConstants.BLACKBERRY_LICENSE);
    }
    
    
    /**
     * Verifies whether or not subscriber has been already activated an can be activated at RIM.
     * @param subscriber Subscriber.
     * @return Value indicating whether or not subscriber has been already activated.
     */
    public static boolean subscriberHasBeenActivated(Subscriber subscriber)
    {
    	return !SafetyUtil.safeEquals(subscriber.getState(), SubscriberStateEnum.AVAILABLE) &&
    		!SafetyUtil.safeEquals(subscriber.getState(), SubscriberStateEnum.PENDING);
    }


    /**
     * Verifies whether or not subscriber has a blackberry service provisioned.
     * @param ctx Context object.
     * @param sub Subscriber object.
     * @return Value indicating whether or not subscriber has a blackberry service provisioned.
     * @throws HomeException
     */
    public static boolean subscriberHasBlackberryService(Context ctx, String subscriberId) throws HomeException
    {
    	return getSubscriberBlackberryService(ctx, subscriberId)!=null;

    }
    
    
    /**
     * Returns the subscriber's blackberry services.
     * Based on the assumption that a subscriber may only have one CRM Service designated as Blackberry.
     * @param ctx Context object.
     * @param sub Subscriber object.
     * @return Set with subscriber's blackberry services.
     * @throws HomeException
     */
    public static Service getSubscriberBlackberryService(Context ctx, String subscriberId) throws HomeException
    {
        Map<ServiceFee2ID, SubscriberServices> services = SubscriberServicesSupport.getSubscribersServices(ctx, subscriberId);
        
        return (getBlackberryService(ctx, services));
    }
    

    private static Service getBlackberryService(Context ctx, Map<ServiceFee2ID, SubscriberServices> services) throws HomeException
    {
        for (Iterator<ServiceFee2ID> iter = services.keySet().iterator(); iter.hasNext();)
        { 
        	ServiceFee2ID serviceFee2ID = iter.next();
            Service s = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());
            if (s!=null && s.getType().equals(ServiceTypeEnum.BLACKBERRY))
            {
            	return s;
            }
        }    
        return null;
    }
    
    /**
     * Verifies whether or not subscriber has a blackberry service suspended.
     * @param ctx Context object.
     * @param subscriber Subscriber object.
     * @return Value indicating if subscriber has a blackberry service suspended.
     * @throws HomeException
     */
    public static boolean subscriberHasSuspendedBlackberryService(Context ctx, Subscriber subscriber) throws HomeException
    {
    	Map<Long, ServiceFee2> suspendedServices = subscriber.getSuspendedServices();
    	
    	return (getBlackberryServiceFee2(ctx, suspendedServices)!=null);
    }

    private static ServiceFee2 getBlackberryServiceFee2(Context ctx, Map<Long, ServiceFee2> services) throws HomeException
    {
        for (Iterator<Long> iter = services.keySet().iterator(); iter.hasNext();)
        { 
        	ServiceFee2 serviceFee = services.get(iter.next());
            Long serviceId = serviceFee.getServiceId();
            Service service = ServiceSupport.getService(ctx, serviceId.longValue());
            if (service!=null && service.getType().equals(ServiceTypeEnum.BLACKBERRY))
            {
            	return serviceFee;
            }
        }
        return null;
    }
    
    /**
     * Return the list of Blackberry Service IDs in an array of primitive long. 
     * @return
     */
    public static boolean areBlackberryServicesProvisionedToIPC(Context ctx)
    {
        IpcProvConfig config = (IpcProvConfig)ctx.get(IpcProvConfig.class); 
        return config.isProvisionBlackberryToIPC();
    }
    
    public static Map<Long,LongHolder> getBlackberryServicesForService(Context ctx, long serviceId)
    {
        Home h = (Home) ctx.get(BlackberryServiceExtensionHome.class);
        Map<Long,LongHolder> result = new TreeMap();
        try
        {
            BlackberryServiceExtension serviceExtension = (BlackberryServiceExtension) h.find(ctx, new EQ(BlackberryServiceExtensionXInfo.SERVICE_ID, Long.valueOf(serviceId)));
            if (serviceExtension!=null)
            {
                result = serviceExtension.getBlackberryServices();
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, BlackberrySupport.class, "Unable to retrieve BlackBerry services for service " + serviceId + ": " + e.getMessage(), e);
        }
        return result;
    }

    public static long[] getBlackberryServicesIdsForService(Context ctx, long serviceId)
    {
        Map<Long,LongHolder> blackberryServices = getBlackberryServicesForService(ctx, serviceId);
        long[] result = new long[blackberryServices.size()];
        Iterator iter = blackberryServices.values().iterator();
        for(int i=0; iter.hasNext(); i++)
        {
            long rimServiceId = ((LongHolder) iter.next()).getValue();
            result[i] = rimServiceId;
        }
        return result;
    }

    
    /**
     * Return the chosen implementation of the BlackBerry ErrorHandler
     * @return
     */
    public static ErrorHandler getErrorHandler()
    {
        /* If we change the implementation of the Error Handler (for example,
         * changing upgrading to include Error handing done in CRM 8.0 after Service Refactoring)
         * change the invocation of the ErrorHandler here. */
        return new ERErrorHandler();
    }
    
    /**
     * Determine whether or not Extra parameter tracking to the RIM Provisioning System 
     * is enabled for CRM.
     * @param ctx
     * @return
     */
    public static boolean isParamTrackingEnabled(Context ctx)
    {
        /* We considered placing this configuration within a configuration bean, but
         * there wasn't any other configuration grouping this type of choice would
         * fall under.  Keep this configuration as a License, until a suitable 
         * configuration grouping (bean) needs to be created. */
        LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        return !lMgr.isLicensed(ctx, LicenseConstants.BLACKBERRY_DISABLE_PARAM_TRACKING);
    }

    
    public static Attributes getBlackberryAttributesBasicAttributes(final Context ctx, final Subscriber sub) throws HomeException
    {
        Attributes attr = new Attributes(sub.getIMSI());
        try
        {
            GSMPackage pkg = HomeSupportHelper.get(ctx).findBean(ctx, GSMPackage.class,
                    new EQ(GSMPackageXInfo.IMSI, sub.getIMSI()));
            if (pkg != null)
            {
                BBConfig bbConfig = HomeSupportHelper.get(ctx).findBean(ctx, BBConfig.class, new EQ(BBConfigXInfo.SPID,Integer.valueOf(sub.getSpid())));
                final CompoundIllegalStateException compound = new CompoundIllegalStateException();
                if(bbConfig == null) 
                {
                	compound.thrown(
                            new IllegalPropertyArgumentException(
                                SubscriberXInfo.INTENT_TO_PROVISION_SERVICES, "Missing Blackberry provision configuration for SPID - " + sub.getSpid()));
                	compound.throwAll();
                }
                if(bbConfig != null)
                {
                	 if(bbConfig.isSendICCIDinBillingId())
                     {
                         attr.setBillingId(pkg.getSerialNo());
                     }                
                	 if(bbConfig.isSendICCID())
                     {
                         attr.setIccid(pkg.getSerialNo());
                     }    
                }
            }
            else
            {
                LogSupport.minor(ctx, BlackberrySupport.class, " Blackberry Service. Unable to obtain SIM Package ["
                        + sub.getIMSI() + " ] for deactivateAttributes " + sub.getId());
                throw new HomeException(" Unable to find SIM card package");
            }
        }
        catch (HomeException homeEx)
        {
            LogSupport.minor(ctx, BlackberrySupport.class, " Blackberry Service. Unable to obtain SIM Package ["
                    + sub.getIMSI() + " ] for sub " + sub.getId());
            throw new HomeException(" Unable to find SIM card package");
        }
        return attr;
    }
}
