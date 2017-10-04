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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.clean.CronConstants;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.CommonTime;

/**
 * This home executes different context agents on every transition. You have the ability
 * to:
 * <UL>
 * <LI>Define actions to be executed on any transition</LI>
 * <LI>Define actions to be executed before or after the new profile is saved in the DB</LI>
 * <LI>Define actions to be executed from the ANY state or to the ANY state</LI>
 * <LI>Define actions to be executed from the NONE state (on create) or to the NONE state
 * (remove)</LI>
 * </UL>
 * This home happens after all the external services have been provisioned. None of the
 * context agents that fail will stop the provisioning, so the context agents have to keep
 * their state somewhere else. This is a good place to put functionality that doesn't kill
 * the provisioning if it fails but still has to be executed in a subscriber provisioning
 * context and only on some transitions. <BR>
 * This home offers support for 256-2=254 states, with 255 being the ANY stae and 254 the
 * NONE state.
 *
 * @author paul.sperneac@redknee.com
 */
public class SubscriberTransitionHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constant defining that an agent is to be executed before the profile is saved in
     * the DB.
     */
    public static final int BEFORE = 0;

    /**
     * Constant defining that an agent is to be executed after the profile is saved in the
     * DB
     */
    public static final int AFTER = 1;

    /**
     * Constant defining that the agent is to be executed for any state
     */
    public static final int ANY = 255;

    /**
     * Constant defining the NONE state, the state where the sub doesn't have a profile
     */
    public static final int NONE = 254;

    /**
     * Maximum number of supported states
     */
    public static final int MAX = 256;

    /**
     * The 'before' action map
     */
    public ContextAgent[][] beforeMap = new ContextAgent[MAX][MAX];

    /**
     * The 'after' action map
     */
    public ContextAgent[][] afterMap = new ContextAgent[MAX][MAX];


    public SubscriberTransitionHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public Object create(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	final Subscriber sub = (Subscriber) obj;

        if (sub != null && sub.getState() != null)
        {
            executeBefore(ctx, NONE, sub.getState().getIndex());
        }

        final Object ret = super.create(ctx, obj);

        if (sub != null && sub.getState() != null)
        {
            executeAfter(ctx, NONE, sub.getState().getIndex());
        }

        return ret;
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeProxy#remove(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public void remove(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	final Subscriber sub = (Subscriber) obj;

        if (sub != null && sub.getState() != null)
        {
            executeBefore(ctx, sub.getState().getIndex(), NONE);
        }

        super.remove(ctx, obj);

        if (sub != null && sub.getState() != null)
        {
            executeAfter(ctx, sub.getState().getIndex(), NONE);
        }
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeProxy#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public Object store(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final Subscriber sub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        if (oldSub != null && oldSub.getState() != null && sub != null && sub.getState() != null)
        {
            executeBefore(ctx, oldSub.getState().getIndex(), sub.getState().getIndex());
        }

        final Object ret = super.store(ctx, obj);

        if (oldSub != null && oldSub.getState() != null && sub != null && sub.getState() != null)
        {
            executeAfter(ctx, oldSub.getState().getIndex(), sub.getState().getIndex());

            performTransitionSpecificInstructionsForSubAuxiliaryServices(ctx, oldSub, sub, oldSub.getState(), sub
                .getState());

        }

        return ret;
    }


    /**
     * Manda - New method added Here we do all the subscriber State transition specific
     * tasks required for all the Subscriber Auxiliary Services for the Business
     * Promotional Plan RFF. Basically we modify the start and end dates of the Auxiliary
     * Service subscribed by the subscriber.
     *
     * @param context
     *            Context object
     * @param current
     *            Subscriber Object
     * @param next
     *            Subscriber Object
     * @throws HomeException
     */
    private void performTransitionSpecificInstructionsForSubAuxiliaryServices(final Context context,
        final Subscriber current, final Subscriber next, final SubscriberStateEnum currentState,
        final SubscriberStateEnum nextState)
    {
        if ((currentState.equals(SubscriberStateEnum.AVAILABLE) || currentState.equals(SubscriberStateEnum.PENDING))
            && nextState.equals(SubscriberStateEnum.ACTIVE))
        {

            // Retrieve the AuxServices.
            final Collection subsAuxServicesCollection = next.getAuxiliaryServices(context);

            if (subsAuxServicesCollection != null && subsAuxServicesCollection.size() > 0)
            {

                final int DAYS_IN_WEEK = 7;
                final Date runningDate = CalendarSupportHelper.get(context).getRunningDate(context);
                final Home home = (Home) context.get(SubscriberAuxiliaryServiceHome.class);

                for (final Iterator auxServicesIter = subsAuxServicesCollection.iterator(); auxServicesIter.hasNext();)
                {
                    final SubscriberAuxiliaryService subAuxObj = (SubscriberAuxiliaryService) auxServicesIter.next();
                    subAuxObj.setContext(context);

                    final SubscriberAuxiliaryService existingSubAuxObj = SubscriberAuxiliaryServiceSupport
                        .getSubscriberAuxiliaryServicesWithIdentifiers(context, subAuxObj.getSubscriberIdentifier(),
                            subAuxObj.getAuxiliaryServiceIdentifier(), subAuxObj.getSecondaryIdentifier());

                    if (existingSubAuxObj != null)
                    {
                        subAuxObj.setIdentifier(existingSubAuxObj.getIdentifier());
                    }

                    final Date subStartDt = next.getStartDate();
                    subAuxObj.setStartDate(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(subStartDt != null
                        ? subStartDt
                        : runningDate));
                    subAuxObj.setProvisioned(true);

                    final short chargingMode = SubscriberAuxiliaryServiceSupport.getChargingModeType(context, subAuxObj
                        .getAuxiliaryServiceIdentifier());

                    Date endDate = subAuxObj.getEndDate();
                    if (ServicePeriodEnum.MONTHLY_INDEX == chargingMode)
                    {
                        endDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).findDateMonthsAfter(subAuxObj
                            .getPaymentNum(), subAuxObj.getStartDate()));
                    }
                    else if (ServicePeriodEnum.WEEKLY_INDEX == chargingMode)
                    {
                        endDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).findDateDaysAfter(subAuxObj
                            .getPaymentNum()
                            * DAYS_IN_WEEK, subAuxObj.getStartDate()));
                    }

                    /*
                     * If for some reason, start date equals end date (Num of Payments =
                     * 0) We set the end date to 20 yrs from now, considering this aux
                     * service should be there forever. Fix for TT 7010443235
                     */
                    if (endDate.equals(subAuxObj.getStartDate()))
                    {
                        endDate = CalendarSupportHelper.get(context).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, runningDate);
                    }

                    if (!endDate.equals(subAuxObj.getEndDate()))
                    {
                        subAuxObj.setEndDate(endDate);
                    }

                    if (LogSupport.isDebugEnabled(context))
                    {
                        new DebugLogMsg(this, "Subscriber [" + next.getBAN() + "] aux svc ["
                            + subAuxObj.getIdentifier() + "] start date = " + subAuxObj.getStartDate()
                            + " and End Date " + subAuxObj.getEndDate()
                            + " Modified for the SubscriberAuxiliaryService selected", null).log(context);
                    }

                    try
                    {
                        home.store(context,subAuxObj);
                    }
                    catch (final HomeException e)
                    {
                        new MinorLogMsg(this, "Error while updating the Subscriber [" + next.getBAN() + "] aux svc ["
                            + subAuxObj.getIdentifier() + "] ", e).log(context);
                    }

                }
            }
        }
    }


    public void add(final Context ctx, final int type, final int from, final int to, final ContextAgent agent)
    {
        final ContextAgent[][] map = getMap(ctx, type);
        if (map == null)
        {
            return;
        }

        if (from == ANY && to == ANY)
        {
            for (int i = 0; i < MAX; i++)
            {
                for (int j = 0; j < MAX; j++)
                {
                    map[i][j] = agent;
                }
            }
        }
        else if (from == ANY)
        {
            for (int i = 0; i < MAX; i++)
            {
                map[i][to] = agent;
            }
        }
        else if (to == ANY)
        {
            for (int i = 0; i < MAX; i++)
            {
                map[from][i] = agent;
            }
        }
        else
        {
            map[from][to] = agent;
        }
    }


    private void executeBefore(final Context ctx, final int source, final int dest)
    {
        if (source >= 0 && source < MAX && dest >= 0 && dest < MAX)
        {
            if (beforeMap[source][dest] != null)
            {
                try
                {
                    beforeMap[source][dest].execute(ctx.createSubContext());
                }
                catch (final AgentException e)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, e.getMessage(), e).log(ctx);
                    }
                }
            }
        }
    }


    private void executeAfter(final Context ctx, final int source, final int dest)
    {
        if (source >= 0 && source < MAX && dest >= 0 && dest < MAX)
        {
            if (afterMap[source][dest] != null)
            {
                try
                {
                    afterMap[source][dest].execute(ctx.createSubContext());
                }
                catch (final AgentException e)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, e.getMessage(), e).log(ctx);
                    }
                }
            }
        }
    }


    public ContextAgent get(final Context ctx, final int type, final int from, final int to)
    {
        final ContextAgent[][] map = getMap(ctx, type);
        if (map == null)
        {
            return null;
        }

        return map[from][to];
    }


    public void remove(final Context ctx, final int type, final int from, final int to, final ContextAgent agent)
    {
        final ContextAgent[][] map = getMap(ctx, type);
        if (map == null)
        {
            return;
        }

        if (from == ANY && to == ANY)
        {
            for (int i = 0; i < MAX; i++)
            {
                for (int j = 0; j < MAX; j++)
                {
                    map[i][j] = null;
                }
            }
        }
        else if (from == ANY)
        {
            for (int i = 0; i < MAX; i++)
            {
                map[i][to] = null;
            }
        }
        else if (to == ANY)
        {
            for (int i = 0; i < MAX; i++)
            {
                map[from][i] = null;
            }
        }
        else
        {
            map[from][to] = null;
        }
    }


    private ContextAgent[][] getMap(final Context ctx, final int type)
    {
        if (type == AFTER)
        {
            return afterMap;
        }

        if (type == BEFORE)
        {
            return beforeMap;
        }

        return null;
    }
}
