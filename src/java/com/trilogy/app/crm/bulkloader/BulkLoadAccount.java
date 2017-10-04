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
package com.trilogy.app.crm.bulkloader;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.trilogy.framework.xhome.beans.FieldValueTooLongException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.language.MessageMgrSPI;
import com.trilogy.framework.xhome.util.format.ThreadLocalSimpleDateFormat;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author victor.stratan@redknee.com
 */
public class BulkLoadAccount extends AbstractBulkLoadAccount
{
    /** the default format for the data in this web control */
    public final static String DEFAULT_FORMAT_PATTERN = "MM/dd/yyyy HH:mm:ss";

    public final static ThreadLocalSimpleDateFormat DEFAULT_FORMAT = new ThreadLocalSimpleDateFormat(
        DEFAULT_FORMAT_PATTERN, Locale.CANADA);

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

        final String format = mmgr.get(ctx, DateWebControl.MSG_MGR_KEY, BulkLoadAccount.class,
                (Lang) ctx.get(Lang.class), null, null);

        if (format == null)
        {
            return DEFAULT_FORMAT;
        }

        // Look for a cached entry of a ThreadLocalSimpleDateFormat. Really because the Map is ThreadLocal
        // we didn't need to have a ThreadLocalSimpleDateFormat stored but because this factory and
        // is shared with the ConstantContextFactory it needed to be a ThreadLocalSimpleDateFormat.
        // Sept.13/2005 DMAC
        final Map map = (Map) (threadLocalFormatMap.get());
        ThreadLocalSimpleDateFormat threadLocalFormat = (ThreadLocalSimpleDateFormat) map.get(format);

        if (threadLocalFormat == null)
        {
            threadLocalFormat = new ThreadLocalSimpleDateFormat(format);
            ((Map) threadLocalFormatMap.get()).put(format, threadLocalFormat);
        }

        return threadLocalFormat;
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