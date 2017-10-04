package com.trilogy.app.crm.support;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.app.crm.bean.payment.ContractHome;

public class ContractSupport {

	public static Contract findContract(Context ctx, long id) throws HomeException
	{
		Contract contract = null;
		Home home = (Home) ctx.get(ContractHome.class);
		try
		{
			contract = (Contract) home.find(ctx, Long.valueOf(id));
		}
		catch(NullPointerException npe)
		{
			new DebugLogMsg(ContractSupport.class.getName(), "No Contract with ID " + id
					+ "configured ", null).log(ctx);
		}
		
		return contract;
	}
}
