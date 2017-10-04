package com.trilogy.app.crm.home.account;

import java.util.Collection;

import com.trilogy.app.crm.bean.AccountReasonCodeMapping;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingHome;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class AccountReasonCodeMappingUpdateHome extends HomeProxy{

	public AccountReasonCodeMappingUpdateHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}
	@Override
	public Object store(Context ctx, Object obj) throws HomeException,HomeInternalException
	{
		if((obj instanceof AccountReasonCodeMapping)&& (obj !=null))
		{
			AccountReasonCodeMapping arcmBean = (AccountReasonCodeMapping)obj;
			Home home = (Home) ctx.get(AccountReasonCodeMappingHome.class);
			And filter = new And();
			filter.add(new EQ(AccountReasonCodeMappingXInfo.SPID,arcmBean.getSpid()));
			filter.add(new EQ(AccountReasonCodeMappingXInfo.ACCOUNT_STATE,arcmBean.getAccountState()));
			filter.add(new EQ(AccountReasonCodeMappingXInfo.REASON_CODE,arcmBean.getReasonCode()));
			Collection<AccountReasonCodeMapping> col = home.select(ctx, filter);
			if(col.size()>0)
			{
				for(AccountReasonCodeMapping dbBean:col)
				{
					if(dbBean.getID()==arcmBean.getID())
					{
						super.store(ctx, arcmBean);
					}else
					{
						throw new HomeException("Reason Code already exists please select other reasoncode");
					}
					
				}
				
			}else
			{
				return super.store(ctx, arcmBean);
			}
		}
		return super.store(ctx, obj);
	}
}
