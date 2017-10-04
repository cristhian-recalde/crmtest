/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.priceplan;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bean.SubModificationSchedule;


/**
 * @author amoll
 * @since 9.9
 */
public class ScheduledSubscriberModificationAgent  implements ContextAgent
{
	ScheduledPriceplanChangeExecutor scheduledPriceplanChangeExecutor = null;

	/**
	 * {@inheritDoc}
	 */
	public void execute(Context ctx) throws AgentException {

		SubModificationSchedule schedule = (SubModificationSchedule)ctx.get(SubModificationSchedule.class);
		getScheduleTaskExecutor(schedule).execute(ctx, schedule);
	}
	
	private ScheduleTaskExecutor getScheduleTaskExecutor(SubModificationSchedule schedule){
		if(schedule != null){
			if(schedule.getType() == CronConstant.SCHEDULED_PRICEPLAN_CHANGE_EXECUTOR){
				if(scheduledPriceplanChangeExecutor == null){
					scheduledPriceplanChangeExecutor = new ScheduledPriceplanChangeExecutor();
				}
				return scheduledPriceplanChangeExecutor;
			}
			//else if : For other schedule jobs
		}
		return null;
		
	}
}
