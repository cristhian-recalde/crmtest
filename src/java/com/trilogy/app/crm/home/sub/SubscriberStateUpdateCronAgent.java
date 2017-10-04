/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Provides a CronTask agent for executing the SubscriberStateUpdateAgent.
 *
 * @author jimmy.ng@redknee.com
 */
public class SubscriberStateUpdateCronAgent
    implements CronContextAgent
{
    // INHERIT
    public void execute(final Context context)
        throws AgentException
    {
        try
        {
            final SubscriberStateUpdateAgent agent = new SubscriberStateUpdateAgent(context);

            agent.execute(context);
        }
        catch (final Throwable throwable)
        {
            final String msg = "Failed to update Subscriber states and/or timers";
            
            new MajorLogMsg(this, msg, throwable).log(context);
            
            throw new CronContextAgentException(msg, throwable);
        }
    }


    // INHERIT
    public void stop()
    {
        // TODO - 2004-06-21 - Is this necessary?
    }

} // class
