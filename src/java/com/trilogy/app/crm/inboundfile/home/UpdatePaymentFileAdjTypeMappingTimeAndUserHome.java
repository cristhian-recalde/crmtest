package com.trilogy.app.crm.inboundfile.home;

import java.util.Date;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.PaymentFileAdjTypeMapping;
import com.trilogy.app.crm.bean.bank.Bank;
import com.trilogy.app.crm.bean.bank.BankHome;
import com.trilogy.app.crm.bean.bank.BankXInfo;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class UpdatePaymentFileAdjTypeMappingTimeAndUserHome extends HomeProxy {

	
	public UpdatePaymentFileAdjTypeMappingTimeAndUserHome(Context ctx,Home delegate)
	{
		super(ctx, delegate);
	}
	public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
	{
		if((obj != null)&&(obj instanceof PaymentFileAdjTypeMapping))
		{
			PaymentFileAdjTypeMapping bean = (PaymentFileAdjTypeMapping)obj;
			Home home = (Home)ctx.get(BankHome.class);
			And filter = new And();
			filter.add(new EQ(BankXInfo.SPID, bean.getSpid()));
			filter.add(new EQ(BankXInfo.BANK_ID, bean.getBankCode()));
			Bank bankBean = (Bank) home.find(ctx, filter);
			if(bankBean != null)
			{
				bean.setBankCode(bankBean.getBankCode());
			}
			bean.setLastUpdatedBy(getAgent(ctx));
			bean.setLastUpdatedDate(new Date());
			return super.store(ctx, bean);
		}else
		{
			return super.store(ctx, obj);
		}
		
	}
	public  String getAgent(final Context ctx)
    {
        final User principal = (User) ctx.get(java.security.Principal.class, new User());
        return (principal.getId().trim().equals("") ? CoreCrmConstants.SYSTEM_AGENT : principal.getId());
    }
}
