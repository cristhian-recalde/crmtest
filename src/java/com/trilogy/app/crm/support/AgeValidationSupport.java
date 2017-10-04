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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.support;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.MinimumAgeLimitProperty;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.spid.MinimumAgeLimitSpidExtension;

/**
 * @author cindy.wong@redknee.com
 * @since 9.0
 */
public class AgeValidationSupport
{
	public static final MinimumAgeLimitSpidExtension
	    getMinimumAgeLimitExtension(Context ctx, int spid)
	{
		CRMSpid serviceProvider = null;

		try
		{
			serviceProvider = SpidSupport.getCRMSpid(ctx, spid);
		}
		catch (HomeException exception)
		{
			LogSupport.minor(ctx, AgeValidationSupport.class,
			    "Exception caught: Cannot locate SPID " + spid, exception);
		}

		if (serviceProvider == null)
		{
			LogSupport.info(ctx, AgeValidationSupport.class, "SPID " + spid
			    + " not found");
			return null;
		}

		for (Object obj : serviceProvider.getExtensions())
		{
			Extension extension = (Extension) obj;
			if (extension instanceof MinimumAgeLimitSpidExtension)
			{
				return (MinimumAgeLimitSpidExtension) extension;
			}
		}

		// no extension found ; skip detection
		return null;
	}

	public static final int getMinimumAge(Context ctx, int spid,
	    SubscriberTypeEnum billingType, boolean fallbackToGeneralConfig)
	{
		int minAge = -1;
		MinimumAgeLimitSpidExtension extension =
		    getMinimumAgeLimitExtension(ctx, spid);
		if (extension != null)
		{
			MinimumAgeLimitProperty property =
			    (MinimumAgeLimitProperty) extension.getAgeLimits().get(
			        Integer.valueOf(billingType.getIndex()));
			if (property != null)
			{
				minAge = property.getMinimumAge();
				if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, AgeValidationSupport.class,
					    "Minimum age required for " + billingType + " in SPID "
					        + spid + " is " + minAge);
				}
			}
			else
			{
				if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, AgeValidationSupport.class,
					    "Minimum age limit for " + billingType
					        + " is not set in SPID " + spid);
				}
			}
		}

		if (minAge < 0 && fallbackToGeneralConfig)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport
				    .debug(
				        ctx,
				        AgeValidationSupport.class,
				        "SPID "
				            + spid
				            + " does not have minimum age limit extension installed; falling back to GeneralConfig");
			}
			GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);
			if (config == null)
			{
				LogSupport.minor(ctx, AgeValidationSupport.class,
				    "GeneralConfig doest not exist in context");
			}
			else
			{
				minAge = config.getMinimumAge();
			}
		}
		else
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport
				    .debug(
				        ctx,
				        AgeValidationSupport.class,
				        "SPID "
				            + spid
				            + " does not have minimum age limit extension installed; skipping age validation");
			}

		}
		return minAge;
	}

	public static final int getMaximumAge(Context ctx, int spid,
	    SubscriberTypeEnum billingType, boolean fallbackToGeneralConfig)
	{
		int maxAge = -1;

		if (fallbackToGeneralConfig)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport
				    .debug(
				        ctx,
				        AgeValidationSupport.class,
				        "SPID "
				            + spid
				            + " does not have maximum age limit extension installed; falling back to GeneralConfig");
			}
			GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);
			if (config == null)
			{
				LogSupport.minor(ctx, AgeValidationSupport.class,
				    "GeneralConfig doest not exist in context");
			}
			else
			{
				maxAge = config.getMaximumAge();
			}
		}
		else
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport
				    .debug(
				        ctx,
				        AgeValidationSupport.class,
				        "SPID "
				            + spid
				            + " does not have maximum age limit extension installed; skipping age validation");
			}

		}
		return maxAge;
	}

	public static final Date getEarliestInvalidDateOfBirth(Context ctx,
	    int minAge)
	{
		Calendar calendar = Calendar.getInstance();
		CalendarSupportHelper.get(ctx).clearTimeOfDay(calendar);
		calendar.add(Calendar.YEAR, -minAge);
		return calendar.getTime();
	}

	public static final Date
	    getEarliestValidDateOfBirth(Context ctx, int maxAge)
	{
		Calendar calendar = Calendar.getInstance();
		CalendarSupportHelper.get(ctx).clearTimeOfDay(calendar);
		calendar.add(Calendar.YEAR, -maxAge);
		return calendar.getTime();
	}

	public static final boolean isOldEnough(Context ctx, int spid,
	    SubscriberTypeEnum billingType, Date dateOfBirth,
	    boolean fallbackToGeneralConfig)
	{
		int minAge =
		    getMinimumAge(ctx, spid, billingType, fallbackToGeneralConfig);
		if (minAge < 0)
		{
			return true;
		}
        
        if (dateOfBirth == null)
        {
            return false;
        }
        
		Date date = getEarliestInvalidDateOfBirth(ctx, minAge);
		return dateOfBirth.before(date);
	}

	public static final boolean isYoungEnough(Context ctx, int spid,
	    SubscriberTypeEnum billingType, Date dateOfBirth,
	    boolean fallbackToGeneralConfig)
	{
		int maxAge =
		    getMaximumAge(ctx, spid, billingType, fallbackToGeneralConfig);
		if (maxAge < 0)
		{
			return true;
		}
		
		if (dateOfBirth == null)
		{
		    return false;
		}
		
		Date date = getEarliestValidDateOfBirth(ctx, maxAge);
		return !dateOfBirth.before(date);
	}

	public static final boolean validateAge(Context ctx, int spid,
	    SubscriberTypeEnum billingType, Date dateOfBirth,
	    boolean fallbackToGeneralConfig)
	{
		return isOldEnough(ctx, spid, billingType, dateOfBirth,
		    fallbackToGeneralConfig)
		    && isYoungEnough(ctx, spid, billingType, dateOfBirth,
		        fallbackToGeneralConfig);
	}
}
