/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.function;

import com.trilogy.app.crm.bean.BackgroundTaskAware;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentControl;
import com.trilogy.framework.lifecycle.LifecycleAgentControlXInfo;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Determines if the MSISDN is different from Pooled Group MSISDN of the current account.
 * 
 * @author simar.singh@redknee.com
 */
public class IsBulkTaskAgentExists implements Predicate
{

    private static final long serialVersionUID = 1L;


    public IsBulkTaskAgentExists()
    {
    }


    /**
     * {@inheritDoc}
     */
    public boolean f(final Context ctx, final Object obj)
    {
        if (obj instanceof BackgroundTaskAware)
        {
            final BackgroundTaskAware backgroundTaskAware = (BackgroundTaskAware) obj;
            final String key = backgroundTaskAware.getTask().getAgentId();
            try
            {
                LifecycleAgentControl agentControl = HomeSupportHelper.get(ctx).findBean(ctx,
                        LifecycleAgentControl.class, new EQ(LifecycleAgentControlXInfo.AGENT_ID, key));
                if (null == agentControl)
                {
                    new DebugLogMsg(this, "Agent Control [" + key + "] does not exist.", null).log(ctx);
                    return false;
                }
                else if (LifecycleStateEnum.UNKNOWN == agentControl.getState())
                {
                    new DebugLogMsg(this, "Life Cycle Agent with [" + key + "] does not exist.", null).log(ctx);
                    return false;
                }
            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Could not query agents for key [" + key + "]", t).log(ctx);
                return false;
            }
        }
        return true;
    }
}