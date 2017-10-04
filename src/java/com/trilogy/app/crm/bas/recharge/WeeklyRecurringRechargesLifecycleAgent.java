package com.trilogy.app.crm.bas.recharge;

import java.util.Date;

import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


public class WeeklyRecurringRechargesLifecycleAgent extends LifecycleAgentScheduledTask
{
    /**
     * Creates a DailyRecurringRechargesLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public WeeklyRecurringRechargesLifecycleAgent(Context ctx, final String agentId) throws AgentException
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
            sb.append("Applying WEEKLY recurring recharges for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("'.");
            LogSupport.debug(ctx, this, sb.toString());
        }
        
        final OptimizedRecurRecharge dailyRecurRecharge = new OptimizedRecurRecharge(ctx, date, ChargingCycleEnum.WEEKLY, AGENT_NAME, this);
        dailyRecurRecharge.execute();
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
        return WeeklyRecurChargeCronAgentSupport.getRunningDate(context);
    }    
    
    public static final String AGENT_NAME = "WeeklyCharges";

}
