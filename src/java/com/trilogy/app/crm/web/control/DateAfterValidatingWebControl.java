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
import com.trilogy.framework.xhome.webcontrol.DateTimeWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.GeneralConfig;

/**
 * Verifies the date entered is validated against certain criteria, default to the value of
 * {@link GeneralConfig#getValidDateDaysAfter()}.
 *
 * @author cindy.wong@redknee.com
 */
public class DateAfterValidatingWebControl extends ProxyWebControl
{

    /**
     * Create a new instance of <code>DateAfterValidatingWebControl</code> using the default
     * {@link DateTimeWebControl} and the default settings in {@link GeneralConfig}.
     */
    public DateAfterValidatingWebControl()
    {
        this(new DateTimeWebControl());
    }

    /**
     * Create a new instance of <code>DateAfterValidatingWebControl</code> using the supplied {@link WebControl} and
     * the default settings in {@link GeneralConfig}.
     *
     * @param delegate
     *            Web control to delegate this action.
     */
    public DateAfterValidatingWebControl(final WebControl delegate)
    {
        this(delegate, false, Calendar.DAY_OF_MONTH, 0);

    }

    /**
     * Create a new instance of <code>DateAfterValidatingWebControl</code> using the supplied {@link WebControl}.
     *
     * @param delegate
     *            Web control to delegate this action.
     * @param daysAfter
     *            If <code>override</code> is set to <code>true</code>, this value is the number of days after
     *            today a date is still considered valid. If <code>override</code> is <code>false</code>, this
     *            value is ignored.
     */
    public DateAfterValidatingWebControl(final WebControl delegate, final int daysAfter)
    {
        this(delegate, true, Calendar.DAY_OF_MONTH, daysAfter);
    }

    /**
     * Create a new instance of <code>DateAfterValidatingWebControl</code> using the supplied {@link WebControl},
     * overriding the default values in {@link GeneralConfig}.
     *
     * @param delegate
     *            Web control to delegate this action.
     * @param field
     *            The {@link Calendar} field <code>amount</code> is considered as. Since only the date part of the
     *            date is considered, this field should not have granularity finer than {@link Calendar#DAY_OF_MONTH}.
     * @param amount
     *            The number of units of <code>field</code> after today a date is still considered valid.
     */
    public DateAfterValidatingWebControl(final WebControl delegate, final int field, final int amount)
    {
        this(delegate, true, field, amount);
    }

    /**
     * Create a new instance of <code>DateAfterValidatingWebControl</code> using the supplied {@link WebControl}.
     *
     * @param delegate
     *            Web control to delegate this action.
     * @param override
     *            Whether to override the default values specified in {@link GeneralConfig}.
     * @param field
     *            If <code>override</code> is set to <code>true</code>, this value is the {@link Calendar} field
     *            <code>amount</code> is considered as. If <code>override</code> is <code>false</code>, this
     *            value is ignored.
     * @param amount
     *            If <code>override</code> is set to <code>true</code>, this value is the number of units of
     *            <code>field</code> after today a date is still considered valid. If <code>override</code> is
     *            <code>false</code>, this value is ignored.
     */
    public DateAfterValidatingWebControl(final WebControl delegate, final boolean override, final int field,
        final int amount)
    {
        super(delegate);
        this.overrideDefault_ = override;
        this.field_ = field;
        this.amount_ = amount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromWeb(final Context context, final ServletRequest request, String name)
    {
        final Date received = (Date) super.fromWeb(context, request, name);
        if (received != null)
        {
            final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
            final Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
            if (this.overrideDefault_)
            {
                calendar.add(this.field_, this.amount_);
            }
            else
            {
                calendar.add(Calendar.DAY_OF_MONTH, config.getValidDateDaysAfter());
            }
            if (received.after(calendar.getTime()))
            {
                throw new IllegalPropertyArgumentException(name, "Date \"" + received + "\" is out of allowed range.");
            }
        }
        return received;
    }

    /**
     * Whether the default value specified in general config is overriden.
     */
    private boolean overrideDefault_;

    /**
     * Number of units of the calendar field after today this date is considered valid. Default to days.
     */
    private int amount_;

    /**
     * The calendar field used.
     */
    private int field_;
}
