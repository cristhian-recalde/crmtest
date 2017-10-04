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

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;

import com.trilogy.app.crm.bean.MsisdnGroupTableWebControl;
import com.trilogy.app.crm.support.FrameworkSupportHelper;

/**
 * Customer web control to color the row in the Msisdn Group table
 * when the number of free msisdns falls below the allowed limit
 * 
 * @author manda.subramanyam@redknee.com
 *
 */
public class MsisdnGroupSearchCustomWebControl extends MsisdnGroupTableWebControl
{

	public MsisdnGroupSearchCustomWebControl() 
	{
		super();
	}

	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
	    Context subCtx = ctx.createSubContext();
	    subCtx.put(TableRenderer.class, getTableRenderer(subCtx));
	    super.toWeb(subCtx, out, name, obj);
	}

	/**
	 * This method gets the default table renderer wrapped with the 
	 * customer table renderer.
	 * 
	 * @param ctx Context object
	 * @return TablRenderer object
	 */
	public TableRenderer getTableRenderer(Context ctx)
	{
		return new MsisdnGroupSearchStateTableRenderer(ctx, FrameworkSupportHelper.get(ctx).getTableRenderer(ctx));
	}
}
