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

import java.util.Date;

import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * Update the extension to execute the store logic.
 * If the subscriber is already bared and end date is
 * beyond a configured set of days, then it is deactivate it. 
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.2
 *
 */
public class FixedStopPricePlanSubscriberUpdateVisitor implements Visitor
{   
    protected final LifecycleAgentScheduledTask agent_;
    
    FixedStopPricePlanSubscriberUpdateVisitor(LifecycleAgentScheduledTask agent)
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
            
            PMLogMsg pm = new PMLogMsg(this.getClass().getName(), "visit()");
            try
            {
                pm.setDetails(fixedStop.toString());
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Processing fixed stop price plan for subscriber [" + fixedStop.getSubId() + 
                            "] with stop date [" + fixedStop.getEndDate() + "]", null).log(ctx);
                }

                updateExtension(ctx, fixedStop);
                
                //If the deactivateDate has passed, then deactivate the subscriber
                Date deactivationDate = getDeactivationDate(ctx, fixedStop.getSpid());
                if (deactivationDate != null && fixedStop.getEndDate().before(deactivationDate))
                {
                    deactivateExtension(ctx, fixedStop);
                }

            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "Unable to process: " + obj + ". Continuing with the task.", e).log(ctx);
            }
            finally
            {
                pm.log(ctx);
            }
        }
    }

    /**
     * Update the extension pipeline.
     * 
     * @param ctx
     * @param fixedStop
     */
    protected void updateExtension(Context ctx, FixedStopPricePlanSubExtension fixedStop)
    {
        Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, fixedStop);
        if (extensionHome != null)
        {
            try
            {
                
                extensionHome.store(ctx, fixedStop);
                
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error updating fixed stop price plan for subscriber [" + fixedStop.getSubId() + "]", e).log(ctx);
            }
        }
        else
        {
            final String msg = "Subscription Extension type not supported: " + fixedStop.getClass().getName() + " for subscriber " + fixedStop.getSubId();
            new MinorLogMsg(this, msg, null).log(ctx);
        }
    }

    /**
     * De-activate the subscriber if it is in Bared/Locked state and create a subscriber note.
     * 
     * @param ctx
     * @param fixedStop
     */
    protected void deactivateExtension(Context ctx, FixedStopPricePlanSubExtension fixedStop)
    {
        Subscriber sub = fixedStop.getSubscriber(ctx);

        if (sub.getState() != SubscriberStateEnum.LOCKED)
        {
            return;
        }
        
        sub.setState(SubscriberStateEnum.INACTIVE);
        
        try
        {
            sub = HomeSupportHelper.get(ctx).storeBean(ctx, sub);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error deactivating subscriber [" + fixedStop.getSubId() + "] with fixed stop price plan end date beyond configured days.", e).log(ctx);
            return;
        }
        
        final StringBuilder note = new StringBuilder();
        note.append("Subscriber deactivated due to fixed stop price plan end date [");
        note.append(fixedStop.getEndDate());
        note.append("] beyond configured days.");
       
        SubscriberNoteSupport.createSubscriberNote(ctx, this, SystemSupport.getAgent(ctx), sub.getId(), SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE, note);
    }

    /**
     * Get deactivate date based on configured number of days in past minus running date. 
     * If the configured number of days is 0, then return null.
     * 
     * @param ctx
     * @param spid
     * @return
     */
    private Date getDeactivationDate(Context ctx, int spid)
    {
        CRMSpid crmSpid;
        try
        {
            crmSpid = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, spid);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Unable to find spid [" + spid + "]", e).log(ctx);
            return null;
        }
        
        Date today = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        int daysToDeactivation = crmSpid.getFixedStopDeactivationDays();
        
        if (daysToDeactivation == 0 )
        {
            return null;
        }
        return CalendarSupportHelper.get(ctx).getDaysBefore(today, daysToDeactivation);
    }

}
