package com.trilogy.app.crm.xdb;

import com.trilogy.app.crm.ContextHelper;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public abstract class MsisdnXDBSupport 
{
	public static Msisdn getMsisdn(String msisdn)
	throws HomeException 
	{
		Context ctx = ContextHelper.getContext().createSubContext(); 
		Home home = (Home)ctx.get(MsisdnHome.class); 
		home = home.where(ctx, new EQ(MsisdnXInfo.MSISDN, msisdn) );
		
		return (Msisdn) home.find(ctx); 
	}
	
	
}