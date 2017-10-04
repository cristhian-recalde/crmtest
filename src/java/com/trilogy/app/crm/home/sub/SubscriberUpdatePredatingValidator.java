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
package com.trilogy.app.crm.home.sub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.home.DateValidator;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;


/**
 * Validates the creation predating dates in a {@link Subscriber}.
 *
 * @author Marcio Marques
 * @since 8.6
 */
public class SubscriberUpdatePredatingValidator extends DateValidator
{
    /**
     * Create a new instance of <code>SubscriberUpdatePredatingValidator</code>.
     */
    protected SubscriberUpdatePredatingValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>SubscriberUpdatePredatingValidator</code>.
     *
     * @return An instance of <code>SubscriberUpdatePredatingValidator</code>.
     */
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new SubscriberUpdatePredatingValidator();
        }
        return instance_;
    }
    
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final Subscriber subscriber = (Subscriber) object;
        
        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();

        if (subscriber.isPostpaid())
        {

            Set<SubscriberServices> services = new HashSet<SubscriberServices>();
            
            // Don't validate already provisioned services.
            services.addAll(subscriber.getIntentToProvisionServices(context));
            services.removeAll(ServiceChargingSupport.getCrossed(context, services, SubscriberServicesSupport.getSubscribersServices(context, subscriber.getId()).values()));
            
            
            /**
             * No date validation needed if the subscriber state is deactivated/expired.
             * TT#12051158013
             */
            if(subscriber.getState() == SubscriberStateEnum.INACTIVE || subscriber.getState() == SubscriberStateEnum.EXPIRED)
                services.clear();
            
            for (SubscriberServices service : services)
            {
                try
                {
                	// TT#13041321003 Fixed. For bulkload, the period for startDate can be supplied via context from the bean-shell. 
                	// Otherwise it will default as per existing implementation.
                	int period = context.getInt("ALLOWED_PERIOD_FOR_SUBSCRIBER_START_DATE_BULKLOAD", -2);
					validAfter(context, null, service,
					    SubscriberServicesXInfo.START_DATE,
                        Calendar.YEAR, period);
                }
                catch (IllegalPropertyArgumentException exception)
                {
                    exceptions.thrown(exception);
                    break;
                }
            }
            
            List<SubscriberAuxiliaryService> auxServices = new ArrayList<SubscriberAuxiliaryService>();

            // Don't validate already provisioned aux services.
            auxServices.addAll(subscriber.getAuxiliaryServices(context));
            auxServices.removeAll(AuxServiceChargingSupport.getCrossedByAuxServiceId(context, auxServices, 
                    SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(context, subscriber.getId())));

            if(subscriber.getState() == SubscriberStateEnum.INACTIVE || subscriber.getState() == SubscriberStateEnum.EXPIRED)
                auxServices.clear();
            
            for (SubscriberAuxiliaryService auxService : auxServices)
            {
                try
                {
                    if (AuxiliaryServiceSupport.supportsPreDating(context, auxService.getAuxiliaryService(context))
                            && auxService.getIdentifier()<=0)
                    {
                        try
                        {
                        	// TT#13041321003 Fixed. For bulkload, the period for startDate can be supplied via context from the bean-shell. 
                        	// Otherwise it will default as per existing implementation.
                        	int period = context.getInt("ALLOWED_PERIOD_FOR_SUBSCRIBER_START_DATE_BULKLOAD", -2);
							validAfter(context, null, auxService,
							    SubscriberAuxiliaryServiceXInfo.START_DATE,
                                Calendar.YEAR, period);
                        }
                        catch (IllegalPropertyArgumentException exception)
                        {
                            exceptions.thrown(exception);
                            break;
                        }

                    }
                }
                catch (HomeException e)
                {
                    LogSupport.minor(context, this, "Unable to retrieve auxiliary service for SubscriberAuxiliaryService. SubscriberId = " + auxService.getSubscriberIdentifier() + ", AuxiliaryServiceId=" + auxService.getAuxiliaryServiceIdentifier());
                }
            }        
        }

        validateStartDate(context, subscriber, exceptions);
        
        exceptions.throwAll();
    }

    private void validateStartDate(Context ctx, Subscriber newSub, CompoundIllegalStateException exceptions)
    {
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLD_FROZEN_SUBSCRIBER);
        
        if (oldSub!=null && !SafetyUtil.safeEquals(oldSub.getState(), SubscriberStateEnum.AVAILABLE))
        {
            Collection<SubscriberServices> oldProvisionedServices = oldSub.getProvisionedServicesBackup(ctx).values();
            Collection<SubscriberServices> newProvisionedServices = new HashSet(newSub.getIntentToProvisionServices(ctx));
            newProvisionedServices.retainAll(ServiceChargingSupport.getCrossed(ctx, newProvisionedServices, oldProvisionedServices));
            for (SubscriberServices service : newProvisionedServices)
            {
                for (SubscriberServices oldService : oldProvisionedServices)
                {
                    if (oldService.getServiceId() == service.getServiceId())
                    {
                        if (!service.getMandatory()
                                && (oldService.getProvisionedState().equals(ServiceStateEnum.PROVISIONED) || oldService
                                        .getProvisionedState().equals(ServiceStateEnum.PROVISIONEDWITHERRORS)))
                        {
                            if (!CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(oldService.getStartDate()).equals(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(service.getStartDate())))
                            {
                                String name = String.valueOf(oldService.getServiceId());
                                if (oldService.getService(ctx)!=null)
                                {
                                    name += " - " + oldService.getService(ctx).getName();
                                }
                                exceptions.thrown(new IllegalPropertyArgumentException(SubscriberServicesXInfo.START_DATE, "Service '" + name + "' is already provisioned and cannot have its start date modified."));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private static Validator instance_;
}
