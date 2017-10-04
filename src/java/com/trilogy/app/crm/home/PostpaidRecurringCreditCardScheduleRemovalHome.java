package com.trilogy.app.crm.home;

import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.CreditCardTokenXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * 
 * @author sgaidhani
 *
 * It executes super.create() first.
 */
public class PostpaidRecurringCreditCardScheduleRemovalHome extends HomeProxy 
{

	private static final long serialVersionUID = 1L;


	public PostpaidRecurringCreditCardScheduleRemovalHome(Context ctx, Home delegate) 
	{
		super(ctx, delegate);

	}


	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
	HomeInternalException {
		Object returnObj =  super.store(ctx, obj);

		Account account = (Account) obj;
		if (account.getState().getIndex() == AccountStateEnum.INACTIVE_INDEX && account.getSystemType().getIndex() == SubscriberTypeEnum.POSTPAID_INDEX)
		{
			boolean purgeCCDetails = SpidSupport.getCRMSpid(ctx, account.getSpid()).isDeleteCCTopUpDataOnPostpaidDeactivation();
			
			if(purgeCCDetails)
			{
				LogSupport.info(ctx, this, "Purging CC Details as CRMSpid#DeleteCCTopUpDataOnPostpaidDeactivation is " + purgeCCDetails );
				
				try
				{
					
					EQ scheduleFilter = new EQ(TopUpScheduleXInfo.BAN, account.getBAN());
					Collection<TopUpSchedule> scheduleCollection = HomeSupportHelper.get(ctx).getBeans(ctx, TopUpSchedule.class, scheduleFilter);
	
					for(TopUpSchedule schedule : scheduleCollection)
					{
						HomeSupportHelper.get(ctx).removeBean(ctx, schedule);
					}
				}
				catch(HomeException he)
				{
					new MajorLogMsg(this, "HomeException encountered while trying to Remove TopUpSchedule for BAN : " + account.getBAN(), he).log(ctx);
				}
				
				try
				{
					EQ tokenFilter = new EQ(CreditCardTokenXInfo.BAN, account.getBAN());
					Collection<CreditCardToken> tokenCollection = HomeSupportHelper.get(ctx).getBeans(ctx, CreditCardToken.class, tokenFilter);
	
					for(CreditCardToken schedule : tokenCollection)
					{
						HomeSupportHelper.get(ctx).removeBean(ctx, schedule);
					}
				}
				catch(HomeException he)
				{
					new MajorLogMsg(this, "HomeException encountered while trying to Remove CreditCardToken for BAN : " + account.getBAN(), he).log(ctx);
				}
			}
			else
			{
				LogSupport.info(ctx, this, "Retaining CC Details as CRMSpid#DeleteCCTopUpDataOnPostpaidDeactivation is " + purgeCCDetails );
			}
			
		}

		return returnObj;
	}
}

