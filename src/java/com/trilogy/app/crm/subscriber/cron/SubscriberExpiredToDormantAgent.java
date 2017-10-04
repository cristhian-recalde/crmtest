/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.cron;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.subscriber.state.PoolHandlingSubscriberStateMutator;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Brings the Subscribers into Dormant state if they have been in expired state
 * for a while.
 *
 * @author simar.singh@redknee.com
 */
public class SubscriberExpiredToDormantAgent implements ContextAgent
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.
     * xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        try                
        {
                        
            final Home subHome = (Home) ctx.get(SubscriberHome.class);
            if (subHome == null)
            {
                throw new HomeException("System error: no SubscriberHome found in context.");
            }
            new InfoLogMsg(this,
                    "Attempting to update states for all the expired subscribers that need to be made dormant", null)
                    .log(ctx);
            ((Home) ctx.get(CRMSpidHome.class)).forEach(ctx, new Visitor()
            {

                /**
                 * Execute this task for ever SPID
                 */
                private static final long serialVersionUID = 1L;


                @Override
                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                {
                    CRMSpid crmSpid = (CRMSpid) obj;
                    
                    if (crmSpid.isDormancyEnabled(ctx))
                    {
                    // process subscribers of each SPID
                    try
                    {
                        /*
                         * Find out the expiry Date latter. All subscribers having expiry
                         * date latter than it should be dormant
                         */
                        final Set<SubscriberStateEnum> stateSet = new HashSet<SubscriberStateEnum>();
                        stateSet.add(SubscriberStateEnum.ACTIVE);
                        stateSet.add(SubscriberStateEnum.SUSPENDED);
                        stateSet.add(SubscriberStateEnum.EXPIRED);

                        final Date dormantEligibleDate = getDormantMarkDate(ctx, crmSpid.getExpiredToDormantDays());
                        And condition = new And();
                        condition.add(new In(SubscriberXInfo.STATE, stateSet));
                        condition.add(new LT(SubscriberXInfo.EXPIRY_DATE, dormantEligibleDate));
                        condition.add(new GT(SubscriberXInfo.EXPIRY_DATE, Subscriber.NEVER_EXPIRE_CUTOFF_DATE));
                        condition.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID));
                        condition.add(new EQ(SubscriberXInfo.SPID, crmSpid.getSpid()));
                        subHome.forEach(ctx, new PoolHandlingSubscriberStateMutator(SubscriberStateEnum.DORMANT),
                                condition);
                    }
                    catch (HomeException e)
                    {
                        new MinorLogMsg(this, "Error Processing SPID [" + crmSpid.getSpid() + "].", e).log(ctx);
                    }
                    }
                    else
                    {
                        new InfoLogMsg(this, " Dormancy state is disabled for spid " + crmSpid.getName() + "["
                                + crmSpid.getId() + "]  .  Or DORMANT_STATE_LICENSE_KEY license is disabled.", null).log(ctx);
                    }
                }
            });
        }
        catch (HomeException e)
        {
            throw new AgentException("SubscriberExpiredToDormantAgent failed to complete." + e, e);
        }
    }


    /**
     * Returns Time insensitive (00 Hours) ; dormantDays number of days before today.
     * 
     * @param dormantDays
     * @return
     */
    private Date getDormantMarkDate(final Context ctx, final int dormantDays)
    {
        final Calendar dormantMarkCalendar = Calendar.getInstance();
        CalendarSupportHelper.get(ctx).clearTimeOfDay(dormantMarkCalendar);

        dormantMarkCalendar.add(Calendar.DATE, -dormantDays);

        return dormantMarkCalendar.getTime();
    }
}
