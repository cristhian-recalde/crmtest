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
package com.trilogy.app.crm.bulkloader;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.language.MessageMgrSPI;
import com.trilogy.framework.xhome.util.format.ThreadLocalSimpleDateFormat;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;

/**
 * @author cindy.wong@redknee.com
 * @since 2011-03-23
 */
public class BulkLoadIdentification extends AbstractBulkLoadIdentification
{
	/** the default format for the data in this web control */
	public final static String DEFAULT_FORMAT_PATTERN = "yyyy-MM-dd";

	public final static ThreadLocalSimpleDateFormat DEFAULT_FORMAT =
	    new ThreadLocalSimpleDateFormat(DEFAULT_FORMAT_PATTERN, Locale.CANADA);

	protected static ThreadLocal threadLocalFormatMap = new ThreadLocal()
	{
		@Override
		public Object initialValue()
		{
			return new HashMap();
		}
	};

	public static ThreadLocalSimpleDateFormat getDateFormat(final Context ctx)
	{
		final MessageMgrSPI mmgr = (MessageMgrSPI) ctx.get(MessageMgrSPI.class);

		final String format =
		    mmgr.get(ctx, DateWebControl.MSG_MGR_KEY,
		        BulkLoadIdentification.class, (Lang) ctx.get(Lang.class), null,
		        null);

		if (format == null)
		{
			return DEFAULT_FORMAT;
		}

		// Look for a cached entry of a ThreadLocalSimpleDateFormat. Really
		// because the Map is ThreadLocal
		// we didn't need to have a ThreadLocalSimpleDateFormat stored but
		// because this factory and
		// is shared with the ConstantContextFactory it needed to be a
		// ThreadLocalSimpleDateFormat.
		// Sept.13/2005 DMAC
		final Map map = (Map) (threadLocalFormatMap.get());
		ThreadLocalSimpleDateFormat threadLocalFormat =
		    (ThreadLocalSimpleDateFormat) map.get(format);

        if (threadLocalFormat == null)
		{
			threadLocalFormat = new ThreadLocalSimpleDateFormat(format);
			((Map) threadLocalFormatMap.get()).put(format, threadLocalFormat);
		}

		return threadLocalFormat;
	}
}
