/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;

import com.trilogy.app.crm.bean.ConvergedAccountSubscriberTableWebControl;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
/**
 * Web control that colours the Converged Search based on state
 * @author amedina
 *
 */
public class ConvergedSearchStateWebControl extends
		ConvergedAccountSubscriberTableWebControl
{

	public ConvergedSearchStateWebControl() 
	{
		super();
	}

	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
	    Context subCtx = ctx.createSubContext();
	    subCtx.put(TableRenderer.class, getTableRenderer(subCtx));
	    super.toWeb(subCtx, out, name, obj);
	}

	  public TableRenderer getTableRenderer(Context ctx)
	  {
	    return new ConvergedSearchStateTableRenderer(ctx, FrameworkSupportHelper.get(ctx).getTableRenderer(ctx));
	  }

}
