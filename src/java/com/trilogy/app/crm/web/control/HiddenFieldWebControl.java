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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.support.StringUtil;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.xenum.AbstractEnum;

import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroupXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerXInfo;

/**
 * Display the value of the provided field as a hidden input field. This web
 * control is useful when trying to pass sessional information between requests.
 * It was originally designed for duplicate detection (both when trying to copy
 * an account and when making the checked fields read-only), but can be used
 * without modification for primitive data types, strings, and framework enums.
 * It was also specifically modified to work with Java collections, but only if
 * the inner element is one of the supported types. More complicated beans can
 * be specially adapted -- see the implementation for handling of
 * AccountIdentification, AccountIdentificationGroup, and
 * SecurityQuestionAnswer.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-10-12
 */
public class HiddenFieldWebControl extends ProxyWebControl
{

	/**
	 * Constructor for HiddenFieldWebControl.
	 */
	public HiddenFieldWebControl(WebControl delegate)
	{
		super(delegate);
	}

	/**
	 * @param ctx
	 * @param out
	 * @param name
	 * @param obj
	 * @see OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context,
	 *      java.io.PrintWriter, java.lang.String, java.lang.Object)
	 */
	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		if (obj instanceof Collection)
		{
			toWeb(ctx, out, name, (Collection) obj);
		}
		else if (obj instanceof AccountIdentificationGroup)
		{
			toWeb(ctx, out, name, (AccountIdentificationGroup) obj);
		}
		else if (obj instanceof AccountIdentification)
		{
			toWeb(ctx, out, name, (AccountIdentification) obj);
		}
		else if (obj instanceof SecurityQuestionAnswer)
		{
			toWeb(ctx, out, name, (SecurityQuestionAnswer) obj);
		}
		else
		{
			out.print("<input type=\"hidden\" name=\"");
			out.print(name);
			out.print("\" value=\"");
			out.print(obj == null ? "" : StringUtil.forHTML(getValue(ctx, obj)));
			out.println("\" />");
		}
	}

	public void toWeb(Context ctx, PrintWriter out, String name,
	    SecurityQuestionAnswer sqa)
	{
		PropertyInfo[] properties =
		    new PropertyInfo[]
		    {
		        SecurityQuestionAnswerXInfo.QUESTION,
		        SecurityQuestionAnswerXInfo.ANSWER
		    };
		toWebBean(ctx, out, name, sqa, properties);

	}

	public void toWeb(Context ctx, PrintWriter out, String name,
	    AccountIdentification ai)
	{
		PropertyInfo[] properties =
		    new PropertyInfo[]
		    {
		        AccountIdentificationXInfo.ID_GROUP,
		        AccountIdentificationXInfo.ID_TYPE,
		        AccountIdentificationXInfo.ID_NUMBER
		    };
		toWebBean(ctx, out, name, ai, properties);

	}

	public void toWeb(Context ctx, PrintWriter out, String name,
	    AccountIdentificationGroup aig)
	{
		toWeb(ctx, out, name + OutputWebControl.SEPERATOR
		    + AccountIdentificationGroupXInfo.GROUP.getName() + "idGroup",
		    Integer.valueOf(aig.getIdGroup()));
		toWeb(ctx, out, name + OutputWebControl.SEPERATOR
		    + AccountIdentificationGroupXInfo.IDENTIFICATION_LIST.getName(),
		    aig.getIdentificationList());

	}

	public void toWeb(Context ctx, PrintWriter out, String name,
	    Collection collection)
	{
		int blanks =
		    ctx.getInt(AbstractWebControl.NUM_OF_BLANKS,
		        AbstractWebControl.DEFAULT_BLANKS);

		// The Math.max() bit is so that if blanks is set to 0 that you can
		// still add a row
		out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR
		    + "_count\" value=\"" + (collection.size() + Math.max(1, blanks))
		    + "\" />");
		if (ctx
		    .getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
		{
			out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR
			    + "_REORDER_KEY\" value=\"1\" />");
		}
		else
		{
			out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR
			    + "_REORDER_KEY\" value=\"0\" />");
		}
		int i = 0;
		for (Object elem : collection)
		{
			String elemName = name + OutputWebControl.SEPERATOR + i;
			toWeb(ctx, out, elemName + OutputWebControl.SEPERATOR + "_enabled",
			    "X");
			toWeb(ctx, out, elemName, elem);
			i++;
		}
	}

	protected void toWebBean(Context ctx, PrintWriter out, String prefix,
	    Object bean, PropertyInfo[] properties)
	{
		for (PropertyInfo property : properties)
		{
			toWeb(ctx, out,
			    prefix + OutputWebControl.SEPERATOR + property.getName(),
			    property.get(bean));
		}
	}

	protected String addSequence(String str)
	{
		return str.replaceAll("\"", "&quot;");
	}

	protected String getValue(Context ctx, Object obj)
	{
		if (obj instanceof AbstractEnum)
		{
			return getValue(ctx, (AbstractEnum) obj);
		}
		else if (obj instanceof Date)
		{
			return getValue(ctx, (Date) obj);
		}
		else if (obj instanceof Boolean)
		{
			return getValue(ctx, (Boolean) obj);
		}
		return obj.toString();
	}

	protected String getValue(Context ctx, Boolean bool)
	{
		return (bool.booleanValue() ? "y" : "n");
	}

	protected String getValue(Context ctx, AbstractEnum xenum)
	{
		return String.valueOf(xenum.getIndex());
	}

	protected String getValue(Context ctx, Date date)
	{
		String format;
		WebControl wc = delegate_;
		
		if(wc instanceof DateWebControl)
		{
			return ( (DateWebControl) wc).getFormatter(ctx).format(date);
		}
		
		while (wc instanceof ProxyWebControl)
		{
			wc = ((ProxyWebControl) wc).getDelegate(ctx);
		}

		MessageMgr mmgr = new MessageMgr(ctx, wc.getClass());
		format = mmgr.get("Format");
		if (format != null && !format.isEmpty())
		{
			DateFormat dateFormat = new SimpleDateFormat(format);
			return dateFormat.format(date);
		}
		return date.toString();
	}
}
