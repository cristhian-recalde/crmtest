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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCategory;
import com.trilogy.app.crm.bean.SubscriberCategoryXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class SubscriberCategoryCustomKeyWebControl extends AbstractKeyWebControl
{
	AbstractKeyWebControl delegate_ = null;
	 

	public SubscriberCategoryCustomKeyWebControl(AbstractKeyWebControl delegate)
	{
		delegate_ = delegate;
	}

	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		Home home = (Home) ctx.get(delegate_.getHomeKey());
		Subscriber bean = (Subscriber) ctx.get(AbstractWebControl.BEAN);
		int spid = bean.getSpid();
		Or condition = new Or().add(new EQ(SubscriberCategoryXInfo.SPID,Integer.valueOf(spid))).add(new EQ(SubscriberCategoryXInfo.CATEGORY_ID,Long.valueOf(0)));
		Home filteredHome = home.where(ctx,new EQ(SubscriberCategoryXInfo.DEPRECATED, Boolean.FALSE));
		filteredHome = filteredHome.where(ctx,condition);
		if (ctx.getInt("MODE", DISPLAY_MODE) == CREATE_MODE)
		{
			SubscriberCategory rankOneSubcat = null;
			try {
				rankOneSubcat = (SubscriberCategory) filteredHome.find(new EQ(SubscriberCategoryXInfo.RANK, Integer.valueOf(1)));
				if (rankOneSubcat != null)
					obj = Long.valueOf(rankOneSubcat.getCategoryId());
				else
					obj = Long.valueOf(0);
			} catch (HomeException e) {
				obj = Long.valueOf(0);
			}
		}
		
		Context subCtx = ctx.createSubContext();
		subCtx.put(delegate_.getHomeKey(), filteredHome);
		delegate_.toWeb(subCtx, out, name, obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl#getHomeKey()
	 */
	public Object getHomeKey()
	{
		return delegate_.getHomeKey();
	}

	public String getDesc(Context ctx, Object bean)
	{
		return delegate_.getDesc(ctx, bean);
	}

	public IdentitySupport getIdentitySupport()
	{
		return delegate_.getIdentitySupport();
	}
}
