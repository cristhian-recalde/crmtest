package com.trilogy.app.crm.bas.recharge;

import java.util.Date;

import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;


public class RecurringRechargePreWarnNotificationLifecycleAgent extends LifecycleAgentScheduledTask
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a RecurringRechargePreWarnNotificationLifecycleAgent object.
     * 
     * @param ctx
     * @param agentId
     * @throws AgentException
     */
    public RecurringRechargePreWarnNotificationLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void start(Context ctx) throws LifecycleException
    {
        Date currentDate = WeeklyRecurChargeCronAgentSupport.getRunningDate(ctx);

        // !!! Agent name must be less than 30 characters, otherwise will cause exception when creating transaction
        RecurringRechargePreWarnNotification preWarnNotification = new RecurringRechargePreWarnNotification(ctx, currentDate, "PreWarningNotification", this);
        preWarnNotification.execute();
    }

}