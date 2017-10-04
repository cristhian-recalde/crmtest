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

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.webcontrol.HiddenWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-10-05
 */
public class ConditionallyReadOnlyByFieldNameWebControl extends ProxyWebControl
{

	/**
	 * Constructor for ConditionallyReadOnlyWebControl.
	 * 
	 * @param delegate
	 *            Delegate of the web control.
	 * @param predicate
	 *            Predicate on the field name.
	 */
	public ConditionallyReadOnlyByFieldNameWebControl(WebControl delegate,
	    Predicate predicate)
	{
		super(delegate);
		predicate_ = predicate;
		readOnlyWebControl_ =
		    new HiddenWebControl(new ReadOnlyWebControl(delegate));
		hiddenFieldWebControl_ = new HiddenFieldWebControl(delegate);
	}

	/**
	 * If the predicate returns true on the field name, make the field
	 * read-only.
	 * 
	 * @param ctx
	 *            Operating context.
	 * @param out
	 *            Print writer.
	 * @param name
	 *            Field name.
	 * @param obj
	 *            Value of the field.
	 * @see com.redknee.framework.xhome.webcontrol.ReadOnlyWebControl#toWeb
	 */
	@Override
	public void toWeb(final Context ctx, final PrintWriter out,
	    final String name, final Object obj)
	{
		if (predicate_.f(ctx, name))
		{
			Context subCtx = ctx;
			// Read-Only
			int mode = ctx.getInt("MODE", DISPLAY_MODE);
			if (mode == EDIT_MODE)
			{
				subCtx = ctx.createSubContext();
				subCtx.put("MODE", DISPLAY_MODE);
			}

			readOnlyWebControl_.toWeb(subCtx, out, name, obj);

			if (mode == EDIT_MODE || mode == CREATE_MODE)
			{
				hiddenFieldWebControl_.toWeb(subCtx, out, name, obj);
			}
		}
		else
		{
			// Read-Write
			delegate_.toWeb(ctx, out, name, obj);
		}
	}

	/**
	 * Always delegate to the web control that can retrieve the value.
	 * 
	 * @param ctx
	 *            the operating context
	 * @param req
	 *            initial request
	 * @param name
	 *            name of the field
	 * @return field value object
	 */
	@Override
	public synchronized Object fromWeb(final Context ctx,
	    final ServletRequest req, final String name)
	{
		try
		{
			return delegate_.fromWeb(ctx, req, name);
		}
		catch (NullPointerException e)
		{
			return readOnlyWebControl_.fromWeb(ctx, req, name);
		}
	}

	private final Predicate predicate_;
	private final WebControl readOnlyWebControl_;
	private final WebControl hiddenFieldWebControl_;
}
