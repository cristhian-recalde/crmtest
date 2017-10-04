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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;

/**
 * Validates birthdates to be between today and 1900 Jan 1.
 *
 * @author danny.ng@redknee.com
 */
public class DateOfBirthValidator implements Validator
{
    public DateOfBirthValidator()
    {
        super();
    }

    public static DateOfBirthValidator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Subscriber sub = (Subscriber) obj;
        // TODO 2008-08-21 date of birth no longer part of subscriber
        // THis Class to be removed
        final Date dateOfBirth = null;//sub.getDateOfBirth();

        if (sub.isPostpaid() && dateOfBirth == null)
        {
            // TODO change to IllegalPropertyArgumentException
            throw new IllegalStateException("date of birth must be entered for a Postpaid subscriber.");
        }

        if (dateOfBirth != null)
        {
            // Create calendar to represent date of birth
            // for comparison purposes
            final Calendar dateOfBirthCal = Calendar.getInstance();
            dateOfBirthCal.setTime(dateOfBirth);

            // Set the calendar to be tommorow (calendar is initialized to today by default)
            final Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);

            // TODO duplicate date checks with SubscriberDatesValidator
            if (calendar.before(dateOfBirthCal))
            {
                throw new IllegalStateException("Date of Birth is after current date.");
            }

            // Set the calendar to be 1900 Jan 1
            calendar.set(1900, 1, 1);

            if (calendar.after(dateOfBirthCal))
            {
                // TODO change to IllegalPropertyArgumentException
                throw new IllegalStateException("Date of Birth before 1900 Jan 1.");
            }
        }
    }

    private static final DateOfBirthValidator INSTANCE = new DateOfBirthValidator();
}
