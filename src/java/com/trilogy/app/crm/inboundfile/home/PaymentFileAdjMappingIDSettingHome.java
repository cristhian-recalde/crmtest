package com.trilogy.app.crm.inboundfile.home;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.PaymentFileAdjTypeMapping;
import com.trilogy.app.crm.bean.bank.Bank;
import com.trilogy.app.crm.bean.bank.BankHome;
import com.trilogy.app.crm.bean.bank.BankXInfo;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * The objective of this class is to set the
 * ID of FeeAndPenalty table by using identifiersequencer
 * @author skambab
 *
 */
public class PaymentFileAdjMappingIDSettingHome extends HomeProxy
{
	public PaymentFileAdjMappingIDSettingHome(Context ctx,Home delegate)
	{
		super(ctx, delegate);
	}
	
	@Override
	public Object create(Context ctx, Object obj) throws HomeException,HomeInternalException
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
			bean.setID((int) getNextIdentifier(ctx));
			bean.setLastUpdatedBy(getAgent(ctx));
			
			return super.create(ctx, bean);
		}
		return super.create(ctx, obj);
	}
	
	private long getNextIdentifier(Context ctx) throws HomeException
	{
		
		 IdentifierSequenceSupportHelper.get((Context)ctx).ensureSequenceExists(ctx, IdentifierEnum.PAYMENTFILEADJTYPEMAPPING_ID, 1, Long.MAX_VALUE);
		    
	     return IdentifierSequenceSupportHelper.get((Context)ctx).getNextIdentifier(ctx, IdentifierEnum.PAYMENTFILEADJTYPEMAPPING_ID, null);
	}
	
	public  String getAgent(final Context ctx)
    {
        final User principal = (User) ctx.get(java.security.Principal.class, new User());
        return (principal.getId().trim().equals("") ? CoreCrmConstants.SYSTEM_AGENT : principal.getId());
    }	

}
