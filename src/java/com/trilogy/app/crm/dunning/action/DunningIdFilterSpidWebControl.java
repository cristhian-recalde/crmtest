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
package com.trilogy.app.crm.dunning.action;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.WhereHome;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;

import com.trilogy.app.crm.core.ruleengine.BusinessRule;
import com.trilogy.app.crm.dunning.DunningPolicyHome;
import com.trilogy.app.crm.dunning.DunningPolicyXInfo;
import com.trilogy.util.snippet.context.ContextUtils;

/**
 * Defining web control for spid based actions
 * @author gaurav.ranjan@redknee.com
 */
public class DunningIdFilterSpidWebControl extends ProxyWebControl
{
	public DunningIdFilterSpidWebControl()
	{
		super(new com.redknee.app.crm.dunning.DunningPolicyKeyWebControl());
	}
	
	public void toWeb(Context ctx, PrintWriter out, String p2, Object p3)
	{
		BusinessRule rule = ContextUtils.getBeanInContextByType(ctx, AbstractWebControl.BEAN, BusinessRule.class);
		Context subctx = ctx.createSubContext();
		int spid  = rule.getSpid();
		Home home = (Home)ctx.get(DunningPolicyHome.class);
		Home spidFiltredHome = new WhereHome(ctx, home, new EQ(DunningPolicyXInfo.SPID, spid));
		ctx.put(DunningPolicyHome.class, spidFiltredHome);
		super.toWeb(subctx, out, p2, p3);
	}
}
