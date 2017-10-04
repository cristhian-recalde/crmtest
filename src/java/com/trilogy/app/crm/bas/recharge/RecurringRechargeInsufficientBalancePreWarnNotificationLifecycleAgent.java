/**
 * 
 */
package com.trilogy.app.crm.bas.recharge;

import java.util.Date;

import bsh.ParseException;

import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 *	Lifecycle Agent to send prewarn notifications for recurring recharge on insufficient balance.
 *
 *	@author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 */
public class RecurringRechargeInsufficientBalancePreWarnNotificationLifecycleAgent
		extends LifecycleAgentScheduledTask {

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
    public RecurringRechargeInsufficientBalancePreWarnNotificationLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void start(Context ctx) throws LifecycleException
    {
        Date currentDate=null;
		try {
			currentDate = InsufficientBalanceNotificationSupport.insufficientBalanceNotificationDate(ctx);
		} catch (final ParseException e) {
			LogSupport.minor(ctx, RecurringRechargeInsufficientBalancePreWarnNotificationLifecycleAgent.class.getName(), e);			
		}

        // !!! Agent name must be less than 30 characters, otherwise will cause exception when creating transaction
        Context subCtx = ctx.createSubContext();
        subCtx.put(RecurringRechargeSupport.INSUFFICIENT_BALANCE_NOTIFICATION, true);
        RecurringRechargePreWarnNotification preWarnNotification = new RecurringRechargePreWarnNotification(subCtx, currentDate, "PreWarningNotification", this);
        preWarnNotification.execute();
    }
    
    
}
