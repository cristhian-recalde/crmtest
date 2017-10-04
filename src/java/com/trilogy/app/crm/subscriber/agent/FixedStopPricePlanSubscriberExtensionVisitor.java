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
package com.trilogy.app.crm.subscriber.agent;

import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.ExtensionSupportHelper;


/**
 * 
 *
 * @author asim.mahmood@redknee.com
 * @since 9.2
 */
public class FixedStopPricePlanSubscriberExtensionVisitor implements Visitor
{   
    protected final LifecycleAgentScheduledTask agent_;
    
    FixedStopPricePlanSubscriberExtensionVisitor(LifecycleAgentScheduledTask agent)
    {
        agent_ = agent;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(Context parentCtx, Object obj) throws AgentException, AbortVisitException
    {
        Context ctx = parentCtx.createSubContext();
        
        if (!LifecycleStateEnum.RUNNING.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running.  Remaining fixed stop price plan subscribers will be processed next time it is run.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }
        
        if (obj instanceof FixedStopPricePlanSubExtension)
        {
            FixedStopPricePlanSubExtension fixedStop = (FixedStopPricePlanSubExtension) obj;
            
            PMLogMsg pm = new PMLogMsg(FixedStopPricePlanSubscriberExtensionVisitor.class.getName(), "visit()");
            try
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Processing fixed stop price plan for subscriber [" + fixedStop.getSubId() + 
                            "] with stop date [" + fixedStop.getEndDate() + "]", null).log(ctx);
                }

                //ctx.put(CommonTime.RUNNING_DATE, startOfNextBillingPeriod);
                //ctx.put(Common.DURING_BILL_CYCLE_CHANGE, true);

                Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, fixedStop);
                if (extensionHome != null)
                {
                    try
                    {
                        extensionHome.store(ctx, fixedStop);
                    }
                    catch (HomeException e)
                    {
                        new MinorLogMsg(this, "Error processing fixed stop price plan for subscriber [" + fixedStop.getSubId() + "]", e).log(ctx);
                    }
                }
                else
                {
                    final String msg = "Subscription Extension type not supported: " + fixedStop.getClass().getName() + " for subscriber " + fixedStop.getSubId();
                    new MinorLogMsg(this, msg, null).log(ctx);
                }
            }
            finally
            {
                pm.log(ctx);
            }
        }
    }


}
