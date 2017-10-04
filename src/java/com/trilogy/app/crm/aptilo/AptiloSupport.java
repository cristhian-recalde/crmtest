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
package com.trilogy.app.crm.aptilo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.bean.*;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.service.aptilo.IServiceAptilo;
import com.trilogy.service.aptilo.ServiceAptiloException;
import com.trilogy.service.aptilo.ServiceAptiloFactory;
import com.trilogy.service.aptilo.model.AptiloConfig;
import com.trilogy.service.aptilo.model.ServiceParameters;
import com.trilogy.service.aptilo.model.ServiceParametersMapping;
import com.trilogy.service.aptilo.model.ServiceParametersXInfo;
import com.trilogy.service.aptilo.model.SubscriberXInfo;
import com.trilogy.app.crm.esbconnect.DefaultEsbMessage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.trilogy.app.crm.esbconnect.ESBMessageResult;


/**
 * Support class for all things Aptilo
 * 
 * @author anuradha.malvadkar@redknee.com @9.7.2
 *
 */
public class AptiloSupport 
{
    /**
     * Error Code for CRM to track Provisioning Errors.
     */
    public static final int WIMAX_PROVISION_ERRORCODE = 3022;
    
    
    
    /**
     * Verifies whether or not subscriber has been already activated an can be activated at RIM.
     * @param subscriber Subscriber.
     * @return Value indicating whether or not subscriber has been already activated.
     */
    public static boolean subscriberHasBeenActivated(Subscriber subscriber)
    {
        return !SubscriberStateEnum.AVAILABLE.equals(subscriber.getState()) &&
            !SubscriberStateEnum.PENDING.equals(subscriber.getState());
    }


    /**
     * Verifies whether or not subscriber has a Aptilo service provisioned.
     * @param ctx Context object.
     * @param sub Subscriber object.
     * @return Value indicating whether or not subscriber has a Aptilo service provisioned.
     * @throws HomeException
     */
    public static boolean subscriberHasAptiloService(Context ctx, String subscriberId) throws HomeException
    {
        return getSubscriberAptiloService(ctx, subscriberId)!=null;

    }
    
    public static boolean subscriberHasValidAptiloService(Context ctx, String subscriberId) throws HomeException
    {
    	
    	Service service = getSubscriberAptiloService(ctx, subscriberId);
    	if(service!=null)
    	    return true;
        return false;

    }
    
    /**
     * Returns the subscriber's Aptilo services.
     * 
     * Based on the assumption that a subscriber may only have one CRM Service designated as Aptilo.
     * @param ctx Context object.
     * @param sub Subscriber object.
     * @return Set with subscriber's Aptilo services.
     * @throws HomeException
     */
    public static Service getSubscriberAptiloService(Context ctx, String subscriberId) throws HomeException
    {
        @SuppressWarnings("unchecked")
        Map<ServiceFee2ID, SubscriberServices> services = SubscriberServicesSupport.getSubscribersServices(ctx, subscriberId);
        
        return (getAptiloService(ctx, services));
    }
    
    public static Long getSubscriberAptiloServiceID(Context ctx, String subscriberId) throws HomeException
    {
        Long aptiloServiceId_ = 0L;
        
        Collection<SubscriberServices> subscriberServices = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class,
                new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
        for (SubscriberServices bean : subscriberServices)
        {
            Long serviceId = bean.getServiceId();
            Service s = ServiceSupport.getService(ctx, serviceId.longValue());
            if(s!=null)
            {
                ServiceTypeEnum type =s.getType();
                if(type.equals(ServiceTypeEnum.WIMAX))
                {
                    return bean.getServiceId();
                }
            }
        }

        return aptiloServiceId_;
    }
    

    private static Service getAptiloService(Context ctx, Map<ServiceFee2ID, SubscriberServices> services) throws HomeException
    {
        for (Iterator<ServiceFee2ID> iter = services.keySet().iterator(); iter.hasNext();)
        { 
        	ServiceFee2ID serviceFee2ID = iter.next();
            Service s = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());
            if(s!=null)
            {
                ServiceTypeEnum type =s.getType();
                

                if(type.equals(ServiceTypeEnum.WIMAX))
                {
                    
                    return s;
                }
            }
          
               
            
        }    
        return null;
    }    

    public static String generateUsername(TDMAPackage pack, AptiloConfig config)
    {
        return compilePattern(pack, config.getUsernamePattern());
    }
    
    public static String generatePassword(TDMAPackage pack, AptiloConfig config)
    {
        return compilePattern(pack, config.getPasswordPattern());
    }
    
    public static String compilePattern(TDMAPackage pack, String pattern)
    {
        if (pack == null)
        {
            throw new IllegalArgumentException("Subscriber package cannot be null");
        }
        return pattern.replaceAll("%PACK_ID%", pack.getPackId())
                      .replaceAll("%PACK_MIN%", pack.getMin())
                      .replaceAll("%PACK_SER%", pack.getSerialNo());
    }
    
    public static boolean isAptiloSupportEnabled(Context ctx,Subscriber sub) 
    {
    	 CRMSpid spid = null;
         try
         {
             spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
         }
         catch (HomeException e)
         {
            
         }
  
        return  spid.getWimaxSupport();
    }
    
    public static AptiloSubscriber getSubscriber(Context ctx, Subscriber subscriber) throws HomeException
    {
        
        AptiloSubscriber result = null;
        try
        {
        	DefaultEsbMessage esbMessageCall = new DefaultEsbMessage();
            esbMessageCall.setBssCommand("grr_template=viewUser");
            ESBMessageResult esbMessageResult =esbMessageCall.getDatefromEsb(ctx,subscriber,subscriber.getSpid());
            Map returnedSub = esbMessageResult.getResultMessageMap();
            
            
            if (returnedSub!=null && returnedSub.size()>1)
            {
                if(!returnedSub.containsKey("soapFaultString"))
                {
                result = new AptiloSubscriber();
                result.setOrganization(returnedSub.get("organization").toString());
                result.setFirstname(returnedSub.get("fname").toString());
                result.setLastname(returnedSub.get("lname").toString());
                result.setUsername(returnedSub.get("uname").toString());
                result.setBegindate(convertDateformat(returnedSub.get("termbegin").toString()));
                result.setEnddate(convertDateformat(returnedSub.get("termend").toString()));
                result.setAccesstype(Integer.parseInt(returnedSub.get("accesstype").toString()));
                result.setAccessprofile(returnedSub.get("accessprofile").toString());
                result.setPurgeafter(convertStringToDate(returnedSub.get("purgeafter").toString()));
                result.setAccounttype(returnedSub.get("accounttype").toString());
                result.setAccountinfo(returnedSub.get("accountinfo").toString());
                result.setConcurrentlogins(Integer.parseInt(returnedSub.get("concurrentlogins").toString()));
                result.setServiceprofiles(returnedSub.get("serviceprofiles").toString().replace(",", "<br>"));
                }
            }
        }
        catch (ServiceAptiloException e)
        {
            final String msg = "error when retrieving subscriber from Aptilo AAA"
                + " Subscriber ID= " + subscriber.getId()
                + " , ResultCode=" + String.valueOf(e.getResultCode())
                + ", Description=" + String.valueOf(e.getDescription());
            
            new MinorLogMsg(AptiloSupport.class, msg, e).log(ctx);
            
        }
        catch (NumberFormatException e)
        {
        	final String msg = "NumberFormatException when parsing accesstype for subscriber from Aptilo AAA";
          
        }
        catch (Exception e)
        {
        	final String msg = "error when parsing accesstype for subscriber from Aptilo AAA";
                    
            
            new MinorLogMsg(AptiloSupport.class, msg, e).log(ctx);
            
        }
        return result;

    }

    public static String getUsername(Context ctx, Subscriber subscriber) throws HomeException
    {
        AptiloConfig config = getAptiloConfig(ctx);
       
        TDMAPackage pack =PackageSupportHelper.get(ctx).getTDMAPackage(ctx, subscriber.getPackageId(), subscriber.getSpid());
        
        return generateUsername(pack, config);
        
    }

    public static com.redknee.service.aptilo.model.Subscriber adaptToAptiloSubscriberBean(Context ctx, Subscriber subscriber,
            Service service, ServiceParametersMapping mapping) 
        throws IOException, InstantiationException, HomeException
    {
        AptiloConfig config = getAptiloConfig(ctx);
    
        TDMAPackage pack =PackageSupportHelper.get(ctx).getTDMAPackage(ctx, subscriber.getPackageId(), subscriber.getSpid());
        //populate package information
        compileMapping(ctx, pack, config, mapping.getServiceParameters(), ServiceParametersXInfo.instance());

        com.redknee.service.aptilo.model.Subscriber apSub;
        //copy all fields from mapping to subscriber
        apSub = (com.redknee.service.aptilo.model.Subscriber) 
                XBeans.copy(ctx, ServiceParametersXInfo.instance(), mapping.getServiceParameters(), SubscriberXInfo.instance());
        
        //populate start and end dates
     
        Date startDate = subscriber.getStartDate();
        
        if (startDate == null || startDate.getTime() == 0)
        {
            startDate = new Date();
        }
        apSub.setBegindate(startDate);
        Date endDate = subscriber.getEndDate();
   
        if (endDate == null || endDate.before(startDate))
        {
            endDate = com.redknee.app.crm.support.ModelAppCrmBeanSupport.findDateYearsAfter(20, new Date());
        }
        apSub.setEnddate(endDate);
        
        //generate Username and password based on configuration
        String username =  generateUsername(pack, config);
        String password =  generatePassword(pack, config);
        
        apSub.setUsername(username);
        apSub.setPassword(password);
        
        return apSub;
    }
    
    private static void compileMapping(Context ctx, TDMAPackage pack, AptiloConfig config, ServiceParameters serviceParameters, XInfo xInfo)
    {        
        for(Iterator i = xInfo.getProperties(ctx).iterator(); i.hasNext(); )
        {
            PropertyInfo pi = (PropertyInfo) i.next();
            if (pi.getType() == String.class)
            {
                String value = compilePattern(pack, (String)pi.get(serviceParameters));
                pi.set(serviceParameters, value);
            }
        }

    }


    public static String generateAptiloUsername(Context ctx, Subscriber subscriber) throws HomeException
    {
        AptiloConfig config = getAptiloConfig(ctx);
    
        TDMAPackage pack =PackageSupportHelper.get(ctx).getTDMAPackage(ctx, subscriber.getPackageId(), subscriber.getSpid());
        return generateUsername(pack, config);
    }

    /**
     * Retrieve the ServiceAptilo configuration bean from the context.
     * 
     * @param ctx A framework context
     * @return The configuration bean
     */
    public static AptiloConfig getAptiloConfig(Context ctx)
    {
        return (AptiloConfig)ctx.get(AptiloConfig.class);
    }
    
    private static String convertDateformat(String inputdate)
    {
    	try {
    		
    		SimpleDateFormat sdfinput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		Date date = sdfinput.parse(inputdate);

    	SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	
	
		return formatter.format(date);
    } catch (Exception e) {
		e.printStackTrace();
	}
 return null;
	
    }
    
    private static Date convertStringToDate(String inputdate)
    {
        try {
            
            SimpleDateFormat sdfinput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdfinput.parse(inputdate);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    
        return formatter.parse(formatter.format(date));
        
    } catch (Exception e) {
        e.printStackTrace();
    }
 return null;
    
    }
}
