package com.trilogy.app.crm.bas.recharge;

import java.util.Date;

import com.trilogy.app.crm.bas.recharge.multiday.MultiDayRecurRecharge;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
/**
 * 
 * @author kabhay
 *
 */

public class MultiDayRecurringRechargesLifecycleAgent extends LifecycleAgentScheduledTask
{
    /**
     * Creates a MultiDayRecurringRechargesLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public MultiDayRecurringRechargesLifecycleAgent(Context ctx, final String agentId) throws AgentException
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
            sb.append("Applying Multi-Day recurring recharges for date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("'.");
            LogSupport.debug(ctx, this, sb.toString());
        }
        
        final MultiDayRecurRecharge multiDayRecurRecharge = new MultiDayRecurRecharge(ctx, date, ChargingCycleEnum.MULTIDAY, AGENT_NAME,  this);
        multiDayRecurRecharge.execute();
        
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
        
        //TODO : need to check if we really need this code below
//        if (runningDate.after(CalendarSupportHelper.get(context).findDateDaysAfter(2, CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).getRunningDate(context)))))
//        {
//            StringBuilder sb = new StringBuilder();
//            sb.append("Requested billing date '");
//            sb.append(CoreERLogger.formatERDateDayOnly(runningDate));
//            sb.append("' is beyond 2 days in the future. The billing date has to be within 2 days in the future.");
//            LogSupport.major(context, this, sb.toString());
//            throw new CronContextAgentException(sb.toString());
//        }

        return runningDate;
    }    
    
    public static final String AGENT_NAME = "MultiDayCharges";
}
