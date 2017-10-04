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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberWebControl;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.home.DateValidator;
import com.trilogy.app.crm.support.Lookup;

/**
 * Validates the dates in a {@link Subscriber}.
 *
 * @author cindy.wong@redknee.com
 */
public class SubscriberDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>SubscriberDatesValidator</code>.
     */
    protected SubscriberDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>SubscriberDatesValidator</code>.
     *
     * @return An instance of <code>SubscriberDatesValidator</code>.
     */
    public static SubscriberDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final Subscriber newSubscriber = (Subscriber) object;

        // try to find the existing subscriber
        final Subscriber oldSubscriber = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);

        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig not found in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();

        // Only check secondary priceplan start and end dates if they are selected
        if ( newSubscriber.getSecondaryPricePlan() != Subscriber.DEFAULT_SECONDARYPRICEPLAN )
        {
            final boolean secondaryPPChanged = oldSubscriber == null
                    || newSubscriber.getSecondaryPricePlan() != oldSubscriber.getSecondaryPricePlan();
            try
            {
                validNotNull(newSubscriber, SubscriberXInfo.SECONDARY_PRICE_PLAN_START_DATE);
				validAfter(context, oldSubscriber, newSubscriber,
				    SubscriberXInfo.SECONDARY_PRICE_PLAN_START_DATE,
                        Calendar.DAY_OF_MONTH, 0, secondaryPPChanged);
                // protect against really, really off dates.
                validBefore(context, oldSubscriber, newSubscriber,
                        SubscriberXInfo.SECONDARY_PRICE_PLAN_START_DATE, Calendar.YEAR, 100, secondaryPPChanged);
            }
        
            catch (IllegalPropertyArgumentException exception)
            {
                exceptions.thrown(exception);
            }

            if (newSubscriber.getSecondaryPricePlanEndDate() != null)
            {
                try
                {
					validAfter(context, oldSubscriber, newSubscriber,
					    SubscriberXInfo.SECONDARY_PRICE_PLAN_END_DATE,
                            Calendar.DAY_OF_MONTH, 0, secondaryPPChanged);
                    // protect against really, really off dates.
                    validBefore(context, oldSubscriber, newSubscriber,
                            SubscriberXInfo.SECONDARY_PRICE_PLAN_END_DATE, Calendar.YEAR, 100, secondaryPPChanged);

                    if (newSubscriber.getSecondaryPricePlanStartDate() != null)
                    {
                        validConsecutive(context, oldSubscriber, newSubscriber,
                                SubscriberXInfo.SECONDARY_PRICE_PLAN_START_DATE,
                                SubscriberXInfo.SECONDARY_PRICE_PLAN_END_DATE,
                                SubscriberWebControl.secondaryPricePlanStartDate_wc, secondaryPPChanged);
                    }
                }
                catch (IllegalPropertyArgumentException exception)
                {
                    exceptions.thrown(exception);
                }
            }
        }

        try
        {
			validAfter(context, oldSubscriber, newSubscriber,
			    SubscriberXInfo.END_DATE, Calendar.DAY_OF_MONTH, 0);
            // protect against really, really off dates.
			validBefore(context, oldSubscriber, newSubscriber,
			    SubscriberXInfo.END_DATE, Calendar.YEAR, 100);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        try
        {
            // protect against really, really off dates.
			validBefore(context, oldSubscriber, newSubscriber,
			    SubscriberXInfo.START_DATE, Calendar.YEAR, 100);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        //try
        //{
        // TODO 2008-08-21 date of birth no longer part of subscriber
            //validateDateOfBirth(oldSubscriber, newSubscriber, SubscriberXInfo.DATE_OF_BIRTH, config);
        //}
        //catch (IllegalPropertyArgumentException exception)
        //{
        //    exceptions.thrown(exception);
        //}
        exceptions.throwAll();

    }

    /**
     * Singleton instance.
     */
    private static SubscriberDatesValidator instance;
}
