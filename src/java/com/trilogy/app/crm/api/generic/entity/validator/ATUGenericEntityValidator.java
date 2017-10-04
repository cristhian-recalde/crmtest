/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.generic.entity.validator;

import java.util.Date;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bean.SubModificationSchedule;
import com.trilogy.app.crm.bean.SubModificationScheduleXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.core.cron.TaskStatusEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Validator for price place change schedule
 * @author ankit.nagpal
 * @since 9.9
 * @see ATUGenericEntityValidator
 */
public class ATUGenericEntityValidator extends AbstractGenericEntityValidator implements Validator {


	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException {
		
		SubModificationSchedule subModificationSchedule = (SubModificationSchedule)obj; 
		
		try {
			subModificationSchedule = HomeSupportHelper.get(ctx).findBean(ctx, SubModificationSchedule.class, new EQ(SubModificationScheduleXInfo.ID, subModificationSchedule.getID()));
			CompoundIllegalStateException el = new CompoundIllegalStateException();
			TaskEntry entry = TaskHelper.retrieve(ctx, CronConstant.SCHEDULED_SUBSCRIBER_MODIFICATION_EXECUTOR_NAME);
			Date runningDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
			
			if (subModificationSchedule != null && CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subModificationSchedule.getScheduledTime())
					.equals(runningDate) && entry.getStatus().equals(TaskStatusEnum.RUNNING))
			{
				StringBuffer message = new StringBuffer();
				message.append( "Cannot delete future dated price plan schedule id : " + subModificationSchedule.getID());
				message.append(" as the cron task " + CronConstant.SCHEDULED_SUBSCRIBER_MODIFICATION_EXECUTOR_NAME + " is in running state");
				if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, message.toString());
				}
				el.thrown(new IllegalPropertyArgumentException(SubModificationScheduleXInfo.PRIMARYKEY, message.toString()));
	            el.throwAll();
			}
		} catch (HomeException e) {
			StringBuffer message = new StringBuffer();
			message.append( "Unable to find schedule with id : " + subModificationSchedule.getID());
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this, message.toString());
			}
		}
	}

}
