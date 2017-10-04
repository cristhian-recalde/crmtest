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
import java.util.Collection;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.home.DateValidator;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Validates the dates of all the {@link SubscriberAuxiliaryService} associated with a
 * single subscriber.
 *
 * @author cindy.wong@redknee.com
 */
public class SubscriberAuxiliaryServiceDatesValidator extends DateValidator
{

    /**
     * Create a new instance of <code>SubscriberAuxiliaryServiceDatesValidator</code>.
     */
    protected SubscriberAuxiliaryServiceDatesValidator()
    {
        super();
    }


    /**
     * Returns an instance of <code>SubscriberAuxiliaryServiceDatesValidator</code>.
     *
     * @return An instance of <code>SubscriberAuxiliaryServiceDatesValidator</code>.
     */
    public static SubscriberAuxiliaryServiceDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberAuxiliaryServiceDatesValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final Subscriber subscriber = (Subscriber) object;
        
        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();

        final Collection<SubscriberAuxiliaryService> associations = subscriber.getAuxiliaryServices(context);
        for (final SubscriberAuxiliaryService association : associations)
        {
            association.setContext(context);
            validateAssociation(context, subscriber, association, exceptions);
        }

        /* Also validate future associations, unless/until the distinction is removed. */
        final Collection<SubscriberAuxiliaryService> futureAssociations = subscriber.getFutureAuxiliaryServices();
        for (final SubscriberAuxiliaryService association : futureAssociations)
        {
            association.setContext(context);
            validateAssociation(context, subscriber, association, exceptions);
        }

    }


    /**
     * Validate the dates of the provided association.
     *
     * @param context
     *            The operating context.
     * @param newAssociation
     *            The association being validated.
     * @param exceptions
     *            Exceptions listener; all exceptions to be thrown by this method should
     *            be added to this.
     */
    protected void validateAssociation(final Context context, final Subscriber subscriber, final SubscriberAuxiliaryService newAssociation,
        final CompoundIllegalStateException exceptions)
    {
        SubscriberAuxiliaryService oldAssociation = null;
        try
        {
            And and = new And();
            and.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, newAssociation
                    .getAuxiliaryServiceIdentifier()));
            and.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, newAssociation
                    .getSubscriberIdentifier()));
            oldAssociation = HomeSupportHelper.get(context).findBean(context, SubscriberAuxiliaryService.class, and);
        }
        catch (final HomeException exception)
        {
            new DebugLogMsg(this, "Cannot find old SubscriberAuxiliaryService; assume null", exception).log(context);
        }
        
        try
        {
            AuxiliaryService service = null;
            try
            {
                service = newAssociation.getAuxiliaryService(context);
            }
            catch (HomeException e)
            {
                LogSupport.minor(context, this, "Unable to retrieve auxiliary service for SubscriberAuxiliaryService. SubscriberId = " + newAssociation.getSubscriberIdentifier() + ", AuxiliaryServiceId=" + newAssociation.getAuxiliaryServiceIdentifier());
            }

            if (subscriber.isPrepaid() || !AuxiliaryServiceSupport.supportsPreDating(context, service))
            {
            	// TT#13041321003 Fixed. For bulkload, the period for startDate can be supplied via context from the bean-shell. 
            	// Otherwise it will default as per existing implementation.
            	int period = context.getInt("ALLOWED_PERIOD_FOR_SUBSCRIBER_START_DATE_BULKLOAD", 0);
            	
				validAfter(context, oldAssociation, newAssociation,
				    SubscriberAuxiliaryServiceXInfo.START_DATE,
                        Calendar.DAY_OF_MONTH, period);
            }
            
            // protect against really, really off dates.
			validBefore(context, oldAssociation, newAssociation,
			    SubscriberAuxiliaryServiceXInfo.START_DATE, Calendar.YEAR, 100);
        }
        catch (final IllegalArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        try
        {
			validAfter(context, oldAssociation, newAssociation,
			    SubscriberAuxiliaryServiceXInfo.END_DATE,
			    Calendar.DAY_OF_MONTH,
                0);
            // protect against really, really off dates.
			validBefore(context, oldAssociation, newAssociation,
			    SubscriberAuxiliaryServiceXInfo.END_DATE, Calendar.YEAR, 100);
        }
        catch (final IllegalArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        if (newAssociation.getStartDate().after(newAssociation.getEndDate()))
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberAuxiliaryServiceXInfo.END_DATE,
                "End date must not be before start date"));
        }
        exceptions.throwAll();
    }



    /**
     * Singleton instance.
     */
    private static SubscriberAuxiliaryServiceDatesValidator instance;
}
