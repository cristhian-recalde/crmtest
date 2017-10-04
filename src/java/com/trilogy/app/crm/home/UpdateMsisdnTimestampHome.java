package com.trilogy.app.crm.home;

import java.util.Date;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.*;

public class UpdateMsisdnTimestampHome
	extends HomeProxy
{
	public UpdateMsisdnTimestampHome(Home delegate)
	{
		super(delegate);
	}

	public Object store(Context ctx,Object obj)
		throws HomeException, HomeInternalException
	{
		if (obj instanceof Msisdn)
		{
			Msisdn msisdn = (Msisdn)obj;
			msisdn.setLastModified(new Date());
		}
		return super.store(ctx,obj);
	}
}
