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
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRendererProxy;

import com.trilogy.app.crm.bean.ConvergedAccountSubscriber;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.SearchTypeEnum;

/**
 * @author msubramanyam
 *
 */
public class MsisdnGroupSearchStateTableRenderer extends TableRendererProxy
{
	/**
     * @param delegate
     */
    public MsisdnGroupSearchStateTableRenderer(Context ctx, TableRenderer delegate)
    {
	    super(delegate);
	    ctx_ = ctx;
    }
    
    
    
    @Override
    public TableRenderer TR(final Context ctx, PrintWriter out, Object obj, int intVal)
    {
    	MsisdnGroup grp = (MsisdnGroup) obj;

    	if (grp.getAvailableMsisdns() <= grp.getMinSize())
    	{
    		out.print("<tr bgcolor=\"yellow\">");
    		return this;
    	}
    	else
    	{
    	    return super.TR(ctx, out, obj, intVal);
    	}
    }
	protected Context ctx_;
}
