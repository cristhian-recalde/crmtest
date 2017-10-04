package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.LinkedList;

import com.trilogy.app.crm.bean.CampaignConfigXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCategoryXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class MarketingCamapignCustomerKeyWebControl extends AbstractKeyWebControl
{
	AbstractKeyWebControl delegate_ = null;
	 

	public MarketingCamapignCustomerKeyWebControl(AbstractKeyWebControl delegate)
	{
		delegate_ = delegate;
	}

	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		Home home = (Home) ctx.get(delegate_.getHomeKey());
		Context parentContext = (Context) ctx.get("..");
		Object object = null;
		while (parentContext!=null)
		{
			object = parentContext.get(AbstractWebControl.BEAN);
			if (object instanceof Subscriber)
				break;
			parentContext = (Context) parentContext.get("..");
		}
		Subscriber bean = (Subscriber) object;
		int spid = bean.getSpid();		
		Or condition = new Or().add(new EQ(CampaignConfigXInfo.SPID, Integer.valueOf(spid))).add(new EQ(CampaignConfigXInfo.CAMPAIGN_ID, Long.valueOf(0)));
		
		Home filteredHome = home.where(ctx, new EQ(CampaignConfigXInfo.DEPRECATED, Boolean.FALSE));
		filteredHome = filteredHome.where(ctx,condition);
		if (ctx.getInt("MODE", DISPLAY_MODE) == CREATE_MODE)
			obj = Long.valueOf(0);

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

