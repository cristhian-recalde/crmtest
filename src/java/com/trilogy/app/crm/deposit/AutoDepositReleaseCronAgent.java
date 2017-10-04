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
package com.trilogy.app.crm.deposit;

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.CRMSpidHome;

/**
 * This agent will be run by scheduler as a nightly batch process to invoke <code>AutoDepositReleaseAgent</code>.
 *
 * @author cindy.wong@redknee.com
 */
public class AutoDepositReleaseCronAgent implements ContextAgent
{
    /**
     * Runs the cron agent.
     *
     * @param context
     *            The operating context.
     * @throws AgentException
     *             Thrown if there are problems executing the agent.
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public final void execute(final Context context) throws AgentException
    {
        new InfoLogMsg(this, "AutoDepositReleaseCronAgent started", null).log(context);
        try
        {
            final Date activeDate = new Date();
            final Home spidHome = (Home) context.get(CRMSpidHome.class);
            if (spidHome == null)
            {
                throw new AgentException("CRMSpidHome does not exist in context");
            }
            spidHome.forEach(context, new SpidReleaseVisitor(activeDate));
            new InfoLogMsg(this, "AutoDepositReleaseCronAgent finished", null).log(context);
        }
        catch (AgentException exception)
        {
            new MinorLogMsg(this, "AutoDepositReleaseCronAgent agent error", exception).log(context);
            throw new AgentException(exception.getMessage(), exception);
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(this, "AutoDepositReleaseCronAgent home error", exception).log(context);
            throw new AgentException(exception.getMessage(), exception);
        }
    }
}
