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

package com.trilogy.app.crm.web.control;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;

/**
 * Web control for date of birth. By default, this web control does not allow birth dates 150 years before today or
 * after today.
 *
 * @author cindy.wong@redknee.com
 */
public class DateOfBirthWebControl extends ProxyWebControl
{
    /**
     * Default number of years is 150.
     */
    public static int DEFAULT_YEARS_BEFORE_TODAY = 150;

    /**
     * Create a new instance of <code>DateOfBirthWebControl</code> with the default years.
     */
    public DateOfBirthWebControl()
    {
        this(DEFAULT_YEARS_BEFORE_TODAY);
    }

    /**
     * Create a new instance of <code>DateOfBirthWebControl</code>.
     *
     * @param yearsBeforeToday
     *            Number of years before today a date of birth is still considered valid.
     */
    public DateOfBirthWebControl(final int yearsBeforeToday)
    {
        super(new DateWebControl());
        this.yearsBeforeToday_ = yearsBeforeToday;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromWeb(final Context context, final ServletRequest request, final String name)
    {
        final Date date = (Date) super.fromWeb(context, request, name);
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -yearsBeforeToday_);
        if (calendar.getTimeInMillis() > date.getTime())
        {
            throw new IllegalPropertyArgumentException(name, "Date of birth is out of allowed range.");
        }
        return date;
    }

    /**
     * Years before today a date-of-birth is still considered valid.
     */
    private int yearsBeforeToday_;
}
