/*
 * Created on Jul 4, 2005
 *
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
package com.trilogy.app.crm.support;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xlog.er.ER;
import com.trilogy.framework.xlog.format.ERLogMsgFormat;
import com.trilogy.framework.xlog.format.FastERLogMsgFormat;
import com.trilogy.framework.xlog.log.LogSupport;

public class DefaultER implements ER
{
	protected long timestamp_;
	protected int id;
	protected int cls;
	protected String sid;
	protected int spid;
	protected String[] fields;
    protected Date erDate;

	public long getTimestamp()
	{
		return timestamp_;
	}

	public void setTimestamp(final long timestamp)
	{
		timestamp_=timestamp;
        erDate = new Date(timestamp);
	}

	public int getId()
	{
		return id;
	}

	public void setId(final int id)
	{
		this.id=id;
	}

	public int getCls()
	{
		return cls;
	}

	public void setCls(final int cls)
	{
		this.cls=cls;
	}

	public String getSid()
	{
		return sid;
	}

	public void setSid(final String sid)
	{
		this.sid=sid;
	}

	public int getSpid()
	{
		return spid;
	}

	public void setSpid(final int spid)
	{
		this.spid=spid;
	}

	/**
	 * @return Returns the fields.
	 */
	public String[] getFields()
	{
		return fields;
	}

	/**
	 * @param fields The fields to set.
	 */
	public void setFields(final String[] fields)
	{
		this.fields = fields;
	}

	public void setFields(final Context ctx, final String[] fields)
	{
		setFields(fields);
	}

	public void setFields(final Context ctx, final StringSeperator seperator)
	{
		final List ret=new ArrayList();
		while(seperator.hasNext())
		{
			ret.add(seperator.next());
		}

		setFields((String[])ret.toArray(new String[]{}));
	}

	public String[] getFields(final Context ctx, final Object obj)
	{
		return ((DefaultER)obj).getFields();
	}

	public void parse(final Context ctx,final String line)
	{
		final Calendar cal = Calendar.getInstance();
		FastERLogMsgFormat format = (FastERLogMsgFormat) ctx.get(ERLogMsgFormat.class);
		if (format == null)
		{
			format = new FastERLogMsgFormat(ctx);
		}

		final StringSeperator seperator = new StringSeperator(line, format.getDelimiter());

		// timestamp
		final String date = seperator.next() + " " + seperator.next();
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try
        {
            cal.setTime(dateFormat.parse(date));
        }
        catch (final ParseException exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Invalid ER date format", exception);
            }

        }

		final int id = Integer.parseInt(seperator.next());

		setTimestamp(cal.getTimeInMillis());

		setId(id);
		setCls(Integer.parseInt(seperator.next()));
		setSid(seperator.next());
		setSpid(Integer.parseInt(seperator.next()));

		setFields(ctx, seperator);
	}

	public char[] getCharFields()
	{
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd,HH:mm:ss");
        final String dateInString = dateFormat.format(erDate);

        final StringBuilder ret = new StringBuilder(60 + 10 * fields.length);
        ret.append(dateInString);
        ret.append(',');
        ret.append(getId());
        ret.append(',');
        ret.append(getCls());
        ret.append(',');
        ret.append(getSpid());

        for (int i = 0; i < fields.length; i++)
        {
            ret.append(',');
            ret.append(fields[i]);
        }

        return ret.toString().toCharArray();
    }
}
