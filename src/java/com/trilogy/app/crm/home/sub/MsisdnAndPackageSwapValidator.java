/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServiceSupport;

/**
 * Provides validation for the swap of MSISDN and Packages.
 * @author marcio.marques@redknee.com
 *
 */
public class MsisdnAndPackageSwapValidator implements Validator
{
	
    private MsisdnAndPackageSwapValidator()
    {
    }

    public static MsisdnAndPackageSwapValidator instance()
    {
        if (instance == null)
        {
            instance = new MsisdnAndPackageSwapValidator();
        }

        return instance;
    }
    
    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object object)
    {
        final Subscriber newSub = (Subscriber)object;
        final Subscriber oldSub = (Subscriber)context.get(Lookup.OLDSUBSCRIBER);
        
        if (newSub!=null && oldSub !=null && 
            (!SafetyUtil.safeEquals(newSub.getPackageId(),oldSub.getPackageId()) || !SafetyUtil.safeEquals(newSub.getMSISDN(),oldSub.getMSISDN()))
           )
        {
            try
            {
            	boolean pricePlanChanged = newSub.getPricePlan() != oldSub.getPricePlan();
            	boolean subscriberStateChanged = !SafetyUtil.safeEquals(newSub.getState(),oldSub.getState());
            	boolean serviceProvisionExpected = isServiceProvisionExpected(context, newSub, oldSub, ServiceTypeEnum.BLACKBERRY);
            	boolean serviceUnprovisionExpected = isServiceUnprovisionExpected(context, newSub, oldSub, ServiceTypeEnum.BLACKBERRY);
            	boolean multiSimUnprovisionExpected = isServiceUnprovisionExpected(context, newSub, oldSub, AuxiliaryServiceTypeEnum.MultiSIM);
            	
                if (((pricePlanChanged || subscriberStateChanged) && BlackberrySupport.subscriberHasBlackberryService(context, newSub.getId())) ||
                		serviceProvisionExpected || serviceUnprovisionExpected || multiSimUnprovisionExpected)
                {
                    final CompoundIllegalStateException compound = new CompoundIllegalStateException();
                	String message = "";

                    if (serviceProvisionExpected)
                    {
                        message = "BlackBerry service is added";
                    } 
                    else if (serviceUnprovisionExpected)
                    {
                        message = "BlackBerry service is removed";
                    } 
                    else if (multiSimUnprovisionExpected)
                    {
                        message = "Multi-SIM auxiliary service is removed";
                    }
                    else if (pricePlanChanged)
                    {
                        message = "price plan is modified";
                    }
                	
                    if (subscriberStateChanged)
                    {
                        if (message.length()==0)
                        {
                            message = "subscriber state is modified";
                        } 
                        else
                        {
                            message = "1) " + message + " or 2) subscriber state is modified";
                        }
                    }

                    boolean packageChangeBlocked = (serviceProvisionExpected || serviceUnprovisionExpected || pricePlanChanged || subscriberStateChanged);
                    if (packageChangeBlocked
                            && !SafetyUtil.safeEquals(newSub.getPackageId(),oldSub.getPackageId()))
                    {
                        compound.thrown(
                                new IllegalPropertyArgumentException(
                                    SubscriberXInfo.PACKAGE_ID, "SIM Package ID cannot be modified if " + message + "."));
                    }

                    boolean msisdnChangeBlocked = (multiSimUnprovisionExpected || BlackberrySupport.isParamTrackingEnabled(context));
                    if (msisdnChangeBlocked
                            && !SafetyUtil.safeEquals(newSub.getMSISDN(),oldSub.getMSISDN()))
                    {
                        compound.thrown(
                        new IllegalPropertyArgumentException(
                                SubscriberXInfo.MSISDN, "Mobile Number cannot be modified if " + message + "."));
                    }
    
                    compound.throwAll();
                }
            } 
            catch (HomeException e)
            {
                new MajorLogMsg(this,"Can not get the services from serviceIDs", e).log(context);
                final CompoundIllegalStateException compound = new CompoundIllegalStateException();
                compound.thrown(
                        new IllegalPropertyArgumentException(
                            SubscriberXInfo.PRICE_PLAN, "Services not found."));
                compound.throwAll();
            }
        }
    }
    
    private boolean isServiceProvisionExpected(Context ctx, Subscriber newSub, Subscriber oldSub, ServiceTypeEnum serviceType) throws HomeException
    {
        List<ServiceFee2ID> services = new ArrayList<ServiceFee2ID>();

        services.addAll(newSub.getServices());
        services.removeAll(oldSub.getServices());
        
        for (Iterator<ServiceFee2ID> iter = services.iterator(); iter.hasNext();)
        { 
        	ServiceFee2ID serviceFee2ID = iter.next();
            Service service = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());
            if (service.getType().equals(serviceType))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isServiceUnprovisionExpected(Context ctx, Subscriber newSubscriber, Subscriber oldSubscriber, ServiceTypeEnum serviceType) throws HomeException
    {
        List<ServiceFee2ID> services = new ArrayList<ServiceFee2ID>();

        //try to calculate service needs provision

        services.addAll(oldSubscriber.getServices(ctx));
        services.removeAll(newSubscriber.getServices(ctx));
        
        for (Iterator<ServiceFee2ID> iter = services.iterator(); iter.hasNext();)
        { 
        	ServiceFee2ID serviceFee2ID = iter.next();
            Service service = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());
            if (service.getType().equals(serviceType))
            {
                return true;
            }
        }
        
        return false;        
    }
    
    private boolean isServiceUnprovisionExpected(Context ctx, Subscriber newSubscriber, Subscriber oldSubscriber, AuxiliaryServiceTypeEnum serviceType) throws HomeException
    {
        List<Long> services = new ArrayList<Long>();

        //try to calculate service needs provision

        services.addAll(oldSubscriber.getAuxiliaryServiceIds(ctx));
        services.removeAll(newSubscriber.getAuxiliaryServiceIds(ctx));
        
        for (Long serviceId : services)
        {
            AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(ctx, serviceId);
            if (service.getType().equals(serviceType))
            {
                return true;
            }
        }
        
        return false;        
    }
    
    private static MsisdnAndPackageSwapValidator instance;    
}
