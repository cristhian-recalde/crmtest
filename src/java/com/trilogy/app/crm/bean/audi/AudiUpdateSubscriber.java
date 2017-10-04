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
package com.trilogy.app.crm.bean.audi;

import java.util.Date;

import com.trilogy.framework.xhome.beans.FieldValueTooLongException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * @author cindy.wong@redknee.com
 * @since 2011-05-03
 */
public class AudiUpdateSubscriber extends AbstractAudiUpdateSubscriber
{
	public SubscriberTypeEnum parseCSVForSubscriberTypeEnum(String csv)
	{
		try
		{
			return SubscriberTypeEnum.get(Short.parseShort(csv));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	public SubscriberStateEnum parseCSVForSubscriberStateEnum(String csv)
	{
		try
		{
			return SubscriberStateEnum.get(Short.parseShort(csv));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	public Date parseCSVForDate(String csv)
	{
		try
		{
			if (csv.length() == 0)
			{
				// Force it to return today's date
				return new Date();
			}
			else
			{
				try
				{
					return (new java.text.SimpleDateFormat("yyyy/MM/dd"))
					    .parse(csv);
				}
				catch (java.text.ParseException e)
				{
					return null;
				}
			}
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	public long parseCSVForLong(String csv)
	{
		try
		{
			return Long.parseLong(csv);
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public long parseCSVForLong(String csv, long default_long)
	{
		try
		{
			return Long.parseLong(csv);
		}
		catch (NumberFormatException e)
		{
			return default_long;
		}
	}

	public int parseCSVForInteger(String csv)
	{
		try
		{
			return Integer.parseInt(csv);
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public int parseCSVForInteger(String csv, int default_int)
	{
		try
		{
			return Integer.parseInt(csv);
		}
		catch (NumberFormatException e)
		{
			return default_int;
		}
	}

	public TechnologyEnum parseCSVForSubscriberTechnologyEnum(String csv)
	{
		try
		{
			return TechnologyEnum.get(Short.parseShort(csv));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	@Override
	public void assertIdNumber1(String idNumber1)
	    throws IllegalArgumentException
	{
		try
		{
			super.assertIdNumber1(idNumber1);
		}
		catch (FieldValueTooLongException e)
		{
			Context ctx = ContextLocator.locate();
			if (ctx != null)
			{
				LogSupport.debug(ctx, this,
				    "The field has exceeded the maximum length", e);
			}
		}
	}
}
