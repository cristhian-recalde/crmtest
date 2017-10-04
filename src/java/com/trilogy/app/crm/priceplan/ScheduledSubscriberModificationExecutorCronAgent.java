package com.trilogy.app.crm.priceplan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;

import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.SubModificationSchedule;
import com.trilogy.app.crm.bean.SubModificationScheduleXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * @author amoll
 * @since 9.9
 */
public class ScheduledSubscriberModificationExecutorCronAgent implements CronContextAgent
{
	
    private static final String DATE_FORMAT_STRING = "yyyyMMdd";

	final ThreadPool scheduleExecutorThreadPool; 
    public ScheduledSubscriberModificationExecutorCronAgent(Context ctx)
    {
    	GeneralConfig generalConfig = getGeneralConfig(ctx);
    	scheduleExecutorThreadPool = new ThreadPool("SCHEDULE_EXECUTOR_THREADPOOL", 
    			generalConfig.getScheduledExecutorWorkpool(), generalConfig.getScheduledExecutorThreadpool(),
    			new ScheduledSubscriberModificationAgent());
    }


    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
    	
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            LogSupport.info(ctx, this, "Scheduled Priceplan Executor cron task initiated.");
        }
        try
        {
        	long timestamp = getCustomizedTime(ctx);
            And predicate = new And();
            java.util.Date startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new java.util.Date(timestamp));
            Calendar endCal = CalendarSupportHelper.get(ctx).dateToCalendar(startDate);
            endCal.add(Calendar.DAY_OF_YEAR, 1);
            predicate.add(new LT(SubModificationScheduleXInfo.SCHEDULED_TIME, 
            		CalendarSupportHelper.get(ctx).calendarToDate(endCal)));
            predicate.add(new GTE(SubModificationScheduleXInfo.SCHEDULED_TIME,
            		CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new java.util.Date(timestamp))));
            predicate.add(new EQ(SubModificationScheduleXInfo.STATUS, CronConstant.SCHEDULED_PENDING));
            
    		Collection<SubModificationSchedule> schedules = 
    				HomeSupportHelper.get(ctx).getBeans(ctx, SubModificationSchedule.class,predicate);
    		for(SubModificationSchedule schedule : schedules)
    		{
    			if(LogSupport.isDebugEnabled(ctx))
    			{
    				StringBuffer jobInfo = new StringBuffer();
    				jobInfo.append("Executing for schedule[ ID:");
    				jobInfo.append(schedule.getID());
    				jobInfo.append(" Subscription id:");
    				jobInfo.append(schedule.getSubscriptionId());
    				jobInfo.append(" Type:");
    				jobInfo.append(schedule.getType());
    				
    				LogSupport.debug(ctx, this, jobInfo.toString());
    			}
    				
    				Context subCtx = ctx.createSubContext();
    				subCtx.put(SubModificationSchedule.class, schedule);
    				try
    				{
    					scheduleExecutorThreadPool.execute(subCtx);
    				} catch (AgentException e)
    				{
    					String message = "Could not execute task for schedule id:" + 
    								schedule.getID() + " and subscription id: " + schedule.getSubscriptionId();
    					
    					LogSupport.major(ctx, this, message, e);
    				}
    			
    		}
    		
        }
        catch (final Exception exception)
        {
        	LogSupport.minor(ctx,this,
                    "Subscription Scheduled change execution exception.",
                    exception);
            throw new CronContextAgentException(
                    "Subscription Scheduled change execution exception.", exception);
        }
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            LogSupport.info(ctx, this, "Subscription Scheduled change cron task completed.");
        }
    }


    @Override
    public void stop()
    {

    }
    private GeneralConfig getGeneralConfig(Context ctx){
	    GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);
	    if (config == null)
	    {
	        new InfoLogMsg(this, "System Error: GeneralConfig does not exist in context, using default values.",
	                null).log(ctx);
	        config = new GeneralConfig();
	    }
	    return config;
    }
    private long getCustomizedTime(final Context ctx)
    {
        final TaskEntry taskEntry = (TaskEntry) ctx.get(TaskEntry.class);
        long inputTimeStamp = System.currentTimeMillis();
        
        try{
        	if (taskEntry != null)
        	{
        		final String dateToCheck = taskEntry.getParam0();
	            if ((dateToCheck != null) && (!dateToCheck.trim().equals("")))
	            {
	            	inputTimeStamp = new SimpleDateFormat(DATE_FORMAT_STRING).parse(dateToCheck).getTime();
	            }
        	}
        }catch(Exception e){
        	 inputTimeStamp = System.currentTimeMillis();
        }
       return inputTimeStamp;
    }

}
