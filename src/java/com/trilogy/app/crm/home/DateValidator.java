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

package com.trilogy.app.crm.home;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
/**
 * Provides date validation based on the {@link GeneralConfig} settings.
 *
 * @author cindy.wong@redknee.com
 */
public abstract class DateValidator implements Validator
{

	/**
	 * Determines whether a property of a new bean should be validated.
	 * 
	 * @param oldBean
	 *            The old bean if exists, or <code>null</code> if none.
	 * @param newBean
	 *            The new bean being validated.
	 * @param property
	 *            The property being validated.
	 * @return <code>true</code> if the property should be validated,
	 *         <code>false</code> otherwise.
	 */
    protected static boolean needsValidation(final Object oldBean, final Object newBean, final PropertyInfo property)
    {
        boolean validate = false;
        if (property == null)
        {
            throw new IllegalArgumentException("Property must not be null!");
        }
        else if (oldBean != null && !property.getBeanClass().isAssignableFrom(oldBean.getClass()))
        {
            throw new IllegalArgumentException(property.getName() + " of " + property.getBeanClass()
                + " is not compatible with bean class " + oldBean.getClass());
        }
        else if (newBean != null && !property.getBeanClass().isAssignableFrom(newBean.getClass()))
        {
            throw new IllegalArgumentException(property.getName() + " of " + property.getBeanClass()
                + " is not compatible with bean class " + newBean.getClass());
        }
        else if (!Date.class.isAssignableFrom(property.getType()))
        {
            throw new IllegalArgumentException(property.getName() + " of " + property.getBeanClass()
                + " is not a date!");
        }
        else if (newBean == null)
        {
            validate = false;
        }
        else if (property.get(newBean) == null)
        {
            validate = false;
        }
        else if (oldBean == null)
        {
            validate = true;
        }
        else
        {
            final Date newDate = (Date) property.get(newBean);
            final Date newDateWithNoTime;
            if (newDate == null)
            {
                newDateWithNoTime = null;
            }
            else
            {
                newDateWithNoTime = CalendarSupportHelper.get().getDateWithNoTimeOfDay(newDate);
            }

            final Date oldDate = (Date) property.get(oldBean);
            final Date oldDateWithNoTime;
            if (oldDate == null)
            {
                oldDateWithNoTime = null;
            }
            else
            {
                oldDateWithNoTime = CalendarSupportHelper.get().getDateWithNoTimeOfDay(oldDate);
            }

            if (newDate != null)
            {
                validate = !SafetyUtil.safeEquals(newDateWithNoTime, oldDateWithNoTime);
            }
        }
        return validate;
    }

    /**
     * <p>
     * Verifies the property of a new bean is not null.
     *
     * @param newBean
     *            The new version of the bean.
     * @param property
     *            The property being verified.
     */
    protected static void validNotNull(final Object newBean, final PropertyInfo property)
    {
        final Date date = (Date) property.get(newBean);
        if (date == null)
        {
            throw new IllegalPropertyArgumentException(property, property.getLabel() + " should be specified.");
        }
    }

    /**
     * <p>
     * Verifies the property of a new bean, if needs to be validated, is before the allowed sliding window. The size of
     * the sliding window is <code>amount</code> units of <code>calendarField</code> (see the constants of
     * {@link Calendar} for available fields).
     * </p>
     * <p>
     * Only the date part of the the property is verified.
     * </p>
     *
     * @param oldBean
     *            The old version of the bean.
     * @param newBean
     *            The new version of the bean.
     * @param property
     *            The property being verified.
     * @param calendarField
     *            The {@link Calendar} field.
     * @param amount
     *            Number of <code>calendarField</code> to add to today. When a negative value is specified, this
     *            method would verify the property against a date in the past.
     */
	protected static void validBefore(Context context, final Object oldBean,
	    final Object newBean, final PropertyInfo property,
        final int calendarField, final int amount)
    {
        validBefore(context, oldBean, newBean, property, calendarField, amount, false);
    }

	/**
	 * <p>
	 * Verifies the property of a new bean, if needs to be validated or if
	 * forced to validate, is before the allowed sliding window. The size of the
	 * sliding window is <code>amount</code> units of <code>calendarField</code>
	 * (see the constants of {@link Calendar} for available fields).
	 * </p>
	 * <p>
	 * Only the date part of the the property is verified.
	 * </p>
	 * 
	 * @param ctx
	 *            Operating context.
	 * @param oldBean
	 *            The old version of the bean.
	 * @param newBean
	 *            The new version of the bean.
	 * @param property
	 *            The property being verified.
	 * @param calendarField
	 *            The {@link Calendar} field.
	 * @param amount
	 *            Number of <code>calendarField</code> to add to today. When a
	 *            negative value is specified, this
	 *            method would verify the property against a date in the past.
	 * @param forced
	 *            Used to force the validation check even if the date properties
	 *            did not change value.
	 */
    protected static void validBefore(Context ctx, final Object oldBean, final Object newBean,
        final PropertyInfo property, final int calendarField, final int amount, final boolean forced)
    {
        if (forced || needsValidation(oldBean, newBean, property))
        {
            final Calendar calendar = getEndOfDay();
            final Date date = (Date) property.get(newBean);
            calendar.add(calendarField, amount);
            if (date.getTime() > calendar.getTimeInMillis())
            {
                SimpleDateFormat df = DateWebControl.instance().getFormatter(ctx);
                final TimeZone tz = (TimeZone) ctx.get(TimeZone.class);
                if (tz != null)
                {
                    df.setTimeZone(tz);
                }
				throw new IllegalPropertyArgumentException(property, "\""
				    + df.format(date.getTime())
				    + "\" is later than allowed (latest: "
				    + df.format(calendar.getTimeInMillis()) + ")");
            }
        }
    }

    /**
     * <p>
     * Verifies the property of a new bean, if needs to be validated, is after the allowed sliding window. The size of
     * the sliding window is <code>amount</code> units of <code>calendarField</code> (see the constants of
     * {@link Calendar} for available fields).
     * </p>
     * <p>
     * Only the date part of the the property is verified.
     * </p>
     *
     * @param oldBean
     *            The old version of the bean.
     * @param newBean
     *            The new version of the bean.
     * @param property
     *            The property being verified.
     * @param calendarField
     *            The {@link Calendar} field.
     * @param amount
     *            Number of <code>calendarField</code> to add to today. When a negative value is specified, this
     *            method would verify the property against a date in the past.
     */
	protected static void validAfter(Context context, final Object oldBean,
	    final Object newBean, final PropertyInfo property,
        final int calendarField, final int amount)
    {
		validAfter(context, oldBean, newBean, property, calendarField, amount,
		    false);
    }

	/**
	 * <p>
	 * Verifies the property of a new bean, if needs to be validated or if
	 * forced to validate, is after the allowed sliding window. The size of the
	 * sliding window is <code>amount</code> units of <code>calendarField</code>
	 * (see the constants of {@link Calendar} for available fields).
	 * </p>
	 * <p>
	 * Only the date part of the the property is verified.
	 * </p>
	 * 
	 * @param ctx
	 *            Operating context.
	 * @param oldBean
	 *            The old version of the bean.
	 * @param newBean
	 *            The new version of the bean.
	 * @param property
	 *            The property being verified.
	 * @param calendarField
	 *            The {@link Calendar} field.
	 * @param amount
	 *            Number of <code>calendarField</code> to add to today. When a
	 *            negative value is specified, this
	 *            method would verify the property against a date in the past.
	 * @param forced
	 *            Used to force the validation check even if the date properties
	 *            did not change value.
	 */
    protected static void validAfter(Context ctx, final Object oldBean,
	    final Object newBean, final PropertyInfo property,
        final int calendarField, final int amount, final boolean forced)
    {
        if (forced || needsValidation(oldBean, newBean, property))
        {
            final Calendar calendar = getBeginningOfDay();
            final Date date = (Date) property.get(newBean);
            calendar.add(calendarField, amount);
            if (date.getTime() < calendar.getTimeInMillis())
            {
                SimpleDateFormat df = DateWebControl.instance().getFormatter(ctx);
                final TimeZone tz = (TimeZone) ctx.get(TimeZone.class);
                if (tz != null)
                {
                    df.setTimeZone(tz);
                }
				throw new IllegalPropertyArgumentException(property, "\""
				    + df.format(date.getTime())
				    + "\" is earlier than allowed (earliest: "
				    + df.format(calendar.getTimeInMillis()) + ")");
            }
        }
    }

	/**
	 * <p>
	 * Verifies the properties of a new bean, if need to be validated, is after
	 * the second property value.
	 * </p>
	 * 
	 * @param ctx
	 *            The operating context
	 * @param oldBean
	 *            The old version of the bean.
	 * @param newBean
	 *            The new version of the bean.
	 * @param property
	 *            The property being verified.
	 * @param consecutiveProperty
	 *            The {@link java.util.Calendar} field.
	 * @param webControl
	 *            The web control of teh field used to format the error message.
	 */
    protected static void validConsecutive(final Context ctx, final Object oldBean, final Object newBean,
            final PropertyInfo property, final PropertyInfo consecutiveProperty, final DateWebControl webControl)
    {
        validConsecutive(ctx, oldBean, newBean, property, consecutiveProperty, webControl, false);
    }

	/**
	 * <p>
	 * Verifies the properties of a new bean, if need to be validated or forced
	 * to validate, is after the second property value.
	 * </p>
	 * 
	 * @param ctx
	 *            The operating context
	 * @param oldBean
	 *            The old version of the bean.
	 * @param newBean
	 *            The new version of the bean.
	 * @param property
	 *            The property being verified.
	 * @param consecutiveProperty
	 *            The {@link java.util.Calendar} field.
	 * @param webControl
	 *            The web control of the field used to format the error message.
	 * @param forced
	 *            Used to force the validation check even if the date properties
	 *            did not change value.
	 */
    protected static void validConsecutive(final Context ctx, final Object oldBean, final Object newBean,
            final PropertyInfo property, final PropertyInfo consecutiveProperty, final WebControl webControl,
            final boolean forced)
    {
        if (forced
                || needsValidation(oldBean, newBean, property)
                || needsValidation(oldBean, newBean, consecutiveProperty))
        {
            final Date date = (Date) property.get(newBean);
            final Date consecutiveDate = (Date) consecutiveProperty.get(newBean);
            if (!consecutiveDate.after(date))
            {
                SimpleDateFormat df = DateWebControl.instance().getFormatter(ctx);
                final TimeZone tz = (TimeZone) ctx.get(TimeZone.class);
                if (tz != null)
                {
                    df.setTimeZone(tz);
                }
				throw new IllegalPropertyArgumentException(property, "\""
                        + df.format(date.getTime()) + " should be earlier than " + consecutiveProperty.getLabel() + " "
                        + df.format(consecutiveDate.getTime()));
            }
        }
    }

    private static DateWebControl getDateWebControl(WebControl control)
    {
        DateWebControl result = null;

        if (control instanceof ProxyWebControl)
        {
            ProxyWebControl delegate = (ProxyWebControl) control;
            while (result == null && delegate != null)
            {
                control = delegate.getDelegate();
                if (control instanceof DateWebControl)
                {
                    result = (DateWebControl) control;
                    break;
                }
                else if (control instanceof ProxyWebControl)
                {
                    delegate = (ProxyWebControl) control;
                }
            }
        }

        if (result == null)
        {
            result = new DateWebControl();
        }

        return result;
    }

    /**
     * Validate a date field of a bean is later than or equal to the allowed minimum. Only the date part of the date is
     * validated; time is not.
     *
     * @param oldBean
     *            The older version of the bean, <code>null</code> if none exists.
     * @param newBean
     *            The new bean being validated.
     * @param property
     *            The property of the bean being validated.
     * @param config
     *            Configuration of the date range.
     * @throws IllegalPropertyArgumentException
     *             Thrown if the date being validated is earlier than the allowed minimum.
     */
	protected static void validatePrior(Context ctx, final Object oldBean,
	    final Object newBean, final PropertyInfo property,
        final GeneralConfig config) throws IllegalPropertyArgumentException
    {
        if (needsValidation(oldBean, newBean, property))
        {
            final Calendar calendar = getBeginningOfDay();
            final Date date = (Date) property.get(newBean);
            calendar.add(Calendar.YEAR, -config.getValidDateYearsPrior());
            if (date.getTime() < calendar.getTimeInMillis())
            {
                SimpleDateFormat df = DateWebControl.instance().getFormatter(ctx);
                final TimeZone tz = (TimeZone) ctx.get(TimeZone.class);
                if (tz != null)
                {
                    df.setTimeZone(tz);
                }
				throw new IllegalPropertyArgumentException(property,
				    property.getLabel() + " " + df.format(date.getTime())
                    + " is earlier than the allowed range");
            }
        }
    }

    /**
     * Validate a date field of a bean is earlier than or equal to the allowed maximum. Only the date part of the date
     * is validated; time is not.
     *
     * @param oldBean
     *            The older version of the bean, <code>null</code> if none exists.
     * @param newBean
     *            The bean being validated.
     * @param property
     *            The property of the bean being validated.
     * @param config
     *            Configuration of the date range.
     * @throws IllegalPropertyArgumentException
     *             Thrown if the date being validated is later than the allowed maximum.
     */
	protected static void validateAfter(Context ctx, final Object oldBean,
	    final Object newBean, final PropertyInfo property,
        final GeneralConfig config) throws IllegalPropertyArgumentException
    {
        if (needsValidation(oldBean, newBean, property))
        {
            final Calendar calendar = getEndOfDay();
            final Date date = (Date) property.get(newBean);
            calendar.add(Calendar.DAY_OF_MONTH, config.getValidDateDaysAfter());
            if (date.getTime() > calendar.getTimeInMillis())
            {
                SimpleDateFormat df = DateWebControl.instance().getFormatter(ctx);
                final TimeZone tz = (TimeZone) ctx.get(TimeZone.class);
                if (tz != null)
                {
                    df.setTimeZone(tz);
                }
				throw new IllegalPropertyArgumentException(property,
				    property.getLabel() + " " + df.format(date.getTime())
                    + " is later than the allowed range");
            }
        }
    }

	/**
     * Returns the calendar specifying the end of today.
     *
     * @return The calendar with its date set to end of today.
     */
    protected static Calendar getEndOfDay()
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        return calendar;
    }

    /**
     * Returns the calendar specifying the beginning of today.
     *
     * @return The calendar with its date set to beginning of today.
     */
    protected static Calendar getBeginningOfDay()
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        return calendar;
    }

}
