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
package com.trilogy.app.crm.clean;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;


/**
 * Closes qualified de-activated zero balance subscriber (State Modification)
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class SubscriberZeroBalanceDormantToDeactivateAgent implements ContextAgent
{

    /**
     * Cleans up each SPID's inactive subscriber.
     * 
     * @author simar.singh@redknee.com
     */
    static final class CleanUpSpidSubscriberVisitor extends HomeVisitor
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = -3023015119124778637L;


        /**
         * Creates a new <code>CleanUpSpidSubscriberVisitor</code>.
         * 
         * @param subscriberHome
         *            The subscriber home.
         */
        public CleanUpSpidSubscriberVisitor(final Home subscriberHome)
        {
            super(subscriberHome);
        }


        /**
         * For the given service provider, close all of its deactive subscribers which
         * satifies its criteria for closure.
         * 
         * @param context
         *            The operating context.
         * @param object
         *            The service provider.
         */
        @Override
        public void visit(final Context context, final Object object)
        {
            final CRMSpid serviceProvider = (CRMSpid) object;
            final Date dormantToDectivateDate;
            {
                final Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -serviceProvider.getFinteBalanceDeactivationDays());
                calendar.set(Calendar.HOUR, 59);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 59);
                dormantToDectivateDate = calendar.getTime();
            }
            final Context subcontext = context.createSubContext();
            final And condition = new And();
            condition.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.DORMANT));
            condition.add(new EQ(SubscriberXInfo.SPID, Integer.valueOf(serviceProvider.getSpid())));
            condition.add(new LT(SubscriberXInfo.LAST_MODIFIED, dormantToDectivateDate));
            try
            {
                final Home filteredHome = getHome().where(subcontext, condition);
                filteredHome
                        .forEach(subcontext, new CloneingVisitor(new CloseZeroBlalanceSubscriberVisitor(getHome())));
            }
            catch (IllegalArgumentException exception)
            {
                // should not happen!!
                new MinorLogMsg(this, "Home is not set in visitor for deactivating Dormant Subscribers in SPID [" +serviceProvider.getSpid()  +"].", exception).log(subcontext);
            }
            catch (AbortVisitException exception)
            {
                new MinorLogMsg(this, "Error visiting home for deactivating Dormant Subscribers in SPID [" +serviceProvider.getSpid()  +"].", exception).log(subcontext);
            }
            catch (HomeException exception)
            {
                new MinorLogMsg(this, "Error selecting who are Dormant Subscribers in SPID [" +serviceProvider.getSpid()  +"].", exception).log(subcontext);
            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Uknown Error while processing deactivation of Dormant Subscribers in SPID [" +serviceProvider.getSpid()  +"].", t).log(subcontext);
            }
        }
    }
    /**
     * Closes a subscriber from home if the subscriber's balance is below the threshold
     * set in the context.
     * 
     * @author simar.singh@redknee.com
     */
    static final class CloseZeroBlalanceSubscriberVisitor extends HomeVisitor
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = -3456364276878686743L;


        /**
         * Create a new instance of <code>RemoveDeactivatedSubscriberVisitor</code>.
         * 
         * @param home
         *            The home to visit.
         */
        public CloseZeroBlalanceSubscriberVisitor(final Home home)
        {
            super(home);
            // add safeguard
            if (home == null)
            {
                throw new IllegalArgumentException("Home must not be null");
            }
        }


        /**
         * If the subscriber does not have remaining balance, set them to closed state
         * 
         * @param context
         *            The operating context.
         * @param object
         *            The subscriber to be closed.
         * @see com.redknee.framework.xhome.visitor.VisitorProxy#visit
         */
        @Override
        public void visit(final Context context, final Object object)
        {
            final Subscriber sub = (Subscriber) object;
            sub.setContext(context);
            if (sub.getBalanceRemaining(context) <= 0)
            {
                new DebugLogMsg(this, "Subscriber " + sub.getId() + " has been marked for closure", null).log(context);
                try
                {
                    sub.setState(SubscriberStateEnum.INACTIVE);
                    getHome().store(context, sub);
                }
                catch (Throwable t)
                {
                    new MinorLogMsg(this, "Couldn't Deactive the Subscriper-Subscrption [" + sub.getId() + "].", t)
                            .log(context);
                }
            }
        }
    }


    /**
     * For each SPID, query the subscriber home with the spid specific criteria for all
     * the subscribers eligible for clean up. Then, delete all selected subs from the
     * subscriber home.
     * 
     * @param context
     *            The operating context.
     * @throws AgentException
     *             Thrown if the agent cannot carry out its task.
     * @see com.redknee.framework.xhome.context.ContextAgent#execute
     */
    public void execute(final Context context) throws AgentException
    {
        final Home subHome = (Home) context.get(SubscriberHome.class);
        if (subHome == null)
        {
            throw new AgentException("System error: SubscriberHome not found in context");
        }
        final Home spidHome = (Home) context.get(CRMSpidHome.class);
        if (spidHome == null)
        {
            throw new AgentException("System error: CRMSpidHome not found in context");
        }
        try
        {
            spidHome.forEach(context, new CleanUpSpidSubscriberVisitor(subHome));
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(this, "Error getting data from spid table", exception).log(context);
            throw new AgentException("Cannot read SPID table", exception);
        }
    }
}
