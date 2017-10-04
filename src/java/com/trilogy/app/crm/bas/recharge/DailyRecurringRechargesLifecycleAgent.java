package com.trilogy.app.crm.bas.recharge;

import java.util.Date;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


public class DailyRecurringRechargesLifecycleAgent extends LifecycleAgentScheduledTask
{
    /**
     * Creates a DailyRecurringRechargesLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public DailyRecurringRechargesLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void start(Context ctx) throws LifecycleException
    {
        final Date date = getRunningDate(ctx);
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Applying DAILY recurring recharges for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("'.");
            LogSupport.debug(ctx, this, sb.toString());
        }
        
        final OptimizedRecurRecharge dailyRecurRecharge = new OptimizedRecurRecharge(ctx, date, ChargingCycleEnum.DAILY, AGENT_NAME, this);
        dailyRecurRecharge.execute();
    }


    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Context ctx)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.DAILY_RECURRING_RECHARGES);
    }


    /**
     * Gets the dunning report generation date.
     * 
     * @param context
     *            The operating context.
     * @return The "current date" for the dunning report generation run.
     * @throws AgentException
     *             thrown if any Exception is thrown during date parsing. Original
     *             Exception is linked.
     */
    private Date getRunningDate(final Context context)
    {
        Date runningDate = getParameter1(context, Date.class);
        if (runningDate==null)
        {
            runningDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).getRunningDate(context));
        }
        
        if (runningDate.after(CalendarSupportHelper.get(context).findDateDaysAfter(2, CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).getRunningDate(context)))))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Requested billing date '");
            sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
            sb.append("' is beyond 2 days in the future. The billing date has to be within 2 days in the future.");
            LogSupport.major(context, this, sb.toString());
            throw new CronContextAgentException(sb.toString());
        }

        return runningDate;
    }    

    public static final String AGENT_NAME = "DailyCharges";
}
