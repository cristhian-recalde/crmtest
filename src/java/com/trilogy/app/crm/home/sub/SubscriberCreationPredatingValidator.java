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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.home.DateValidator;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;


/**
 * Validates the creation predating dates in a {@link Subscriber}.
 *
 * @author Marcio Marques
 * @since 8.6
 */
public class SubscriberCreationPredatingValidator extends DateValidator
{
    /**
     * Create a new instance of <code>SubscriberCreationPredatingValidator</code>.
     */
    protected SubscriberCreationPredatingValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>SubscriberCreationPredatingValidator</code>.
     *
     * @return An instance of <code>SubscriberCreationPredatingValidator</code>.
     */
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new SubscriberCreationPredatingValidator();
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
            if (subscriber.getStartDate()!=null)
            {
                try
                {
                	// TT#13041321003 Fixed. For bulkload, the period for startDate can be supplied via context from the bean-shell. 
                	// Otherwise it will default as per existing implementation.
                	int period = context.getInt("ALLOWED_PERIOD_FOR_SUBSCRIBER_START_DATE_BULKLOAD", -2);
					validAfter(context, null, subscriber,
					    SubscriberXInfo.START_DATE,
                        Calendar.YEAR, period);
                }
                catch (IllegalPropertyArgumentException exception)
                {
                    exceptions.thrown(exception);
                }
                
                Date startDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(subscriber.getStartDate());
                Date today = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(Calendar.getInstance().getTime());
                
                Set<SubscriberServices> services = subscriber.getIntentToProvisionServices(context);
                for (SubscriberServices service : services)
                {
                    Date serviceDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(service.getStartDate());
                    if (!serviceDate.equals(startDate) && serviceDate.before(today))
                    {
                        String serviceName = String.valueOf(service.getServiceId());
                        if (service.getService(context)!=null)
                        {
                            serviceName += " - " + service.getService(context).getName();
                        }
                        exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.START_DATE,
                                "Service '" + serviceName + "' START DATE cannot be set to a different date in the past when ACTIVATION DATE is set to a different date in the past."));
                    }
                }
                
                List<SubscriberAuxiliaryService> auxServices = subscriber.getAuxiliaryServices(context);
                for (SubscriberAuxiliaryService auxService : auxServices)
                {
                    Date serviceDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(auxService.getStartDate());
                    if (!serviceDate.equals(startDate) && serviceDate.before(today))
                    {
                        String serviceName = String.valueOf(auxService.getAuxiliaryServiceIdentifier());
                        AuxiliaryService service = null;
                        
                        try
                        {
                            service = auxService.getAuxiliaryService(context);
                        }
                        catch (HomeException ignored)
                        {
                            
                        }
                        
                        if (service!=null)
                        {
                            serviceName += " - " + service.getName();
                        }
                        exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.START_DATE,
                                "Auxiliary service '" + serviceName + "' START DATE cannot be set to a date in the past when ACTIVATION DATE is set to a different date in the past."));
                    }
                }
            }
            else
            {
                Set<SubscriberServices> services = subscriber.getIntentToProvisionServices(context);
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
                
                List<SubscriberAuxiliaryService> auxServices = subscriber.getAuxiliaryServices(context);
                for (SubscriberAuxiliaryService auxService : auxServices)
                {
                    try
                    {
                        if (AuxiliaryServiceSupport.supportsPreDating(context, auxService.getAuxiliaryService(context)))
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
            
            exceptions.throwAll();
        }
        
        
    }

    private static Validator instance_;
}
