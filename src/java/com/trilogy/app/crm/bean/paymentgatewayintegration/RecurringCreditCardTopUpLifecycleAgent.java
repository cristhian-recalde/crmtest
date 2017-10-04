package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import com.trilogy.app.crm.bean.PaymentGatewayIntegrationConfig;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * This lifecycle agent will perform the actual scheduled top up.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class RecurringCreditCardTopUpLifecycleAgent extends
		LifecycleAgentScheduledTask 
{
	final ThreadPool ccTopUpThreadPool; 

	public RecurringCreditCardTopUpLifecycleAgent(Context ctx, String agentId)
			throws AgentException 
	{
		super(ctx, agentId);
		PaymentGatewayIntegrationConfig config = (PaymentGatewayIntegrationConfig)ctx.get(PaymentGatewayIntegrationConfig.class);
		ccTopUpThreadPool = new ThreadPool("TOPUP_THREADPOOL", config.getQueue(), config.getThreads(), new RecurringCreditCardTopUpAgent());
	}

	@Override
	protected void start(Context ctx) throws LifecycleException, HomeException 
	{
		Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
		
		And and = new And(); 
        
		and.add(new LTE(TopUpScheduleXInfo.NEXT_APPLICATION, today));
		and.add(new EQ(TopUpScheduleXInfo.SYSTEM_TYPE, SubscriberTypeEnum.PREPAID));
		
		Collection<TopUpSchedule> schedules = HomeSupportHelper.get(ctx).getBeans(ctx, TopUpSchedule.class,and);
		
		for(TopUpSchedule schedule : schedules)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this, "Applying recurring top up for schedule[ ID:" 
						+ schedule.getId()
						+ " BAN:"
						+ schedule.getBan()
						+ " MSISDN:"
						+ schedule.getMsisdn()
						+ " Subscriber:"
						+ schedule.getSubscriptionId());
			}
				
				Context subCtx = ctx.createSubContext();
				subCtx.put(TopUpSchedule.class, schedule);
				try
				{
					ccTopUpThreadPool.execute(subCtx);
				} catch (AgentException e)
				{
					String message = "Could not apply recurring top up for schedule id:" + schedule.getId() + " and subscription id: " + schedule.getSubscriptionId();
					if(e.getCause() instanceof PaymentGatewayException)
					{
						LogSupport.major(ctx, this, message, e.getCause());
					}
					else
					{
						LogSupport.major(ctx, this, message, e);
					}
				}
			
		}
	}

}
