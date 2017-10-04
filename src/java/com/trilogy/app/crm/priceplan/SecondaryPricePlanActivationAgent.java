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
package com.trilogy.app.crm.priceplan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.IsNull;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * @author rajith.attapattu@redknee.com
 */
public class SecondaryPricePlanActivationAgent implements ContextAgent
{
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    public static final long DEFAULT_SECONDARY_PRICE_PLAN_ID = -1l;

    public SecondaryPricePlanActivationAgent()
    {
    }

    private Date checkTime_;

    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx) throws AgentException
    {
        checkTime_ = getCheckTime(ctx);
        updateSubscribersWithSecondaryPricePlan(ctx);
    }

    private Date getCheckTime(final Context ctx)
    {
        final TaskEntry taskEntry = (TaskEntry) ctx.get(TaskEntry.class);
        Date inputDate = null;
        if (taskEntry != null)
        {
            final String dateToCheck = taskEntry.getParam0();
            if ((dateToCheck != null) && (!dateToCheck.trim().equals("")))
            {
                try
                {
                    synchronized (DATE_FORMAT)
                    {
                        inputDate = DATE_FORMAT.parse(dateToCheck);
                    }
                }
                catch (ParseException e)
                {
                    LogSupport.minor(ctx, this,
                            "The input param is not correct, it has to be of the format yyyy/MM/dd", e);
                }
            }
        }

        if (inputDate == null)
        {
            inputDate = new Date();
        }

        return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(inputDate);
    }

    private void updateSubscribersWithSecondaryPricePlan(final Context ctx)
    {
        final Home home = (Home) ctx.get(SubscriberHome.class);
        processSubscribersWithSecondaryPlanActivationDue(ctx, home);
    }

    private void processSubscribersWithSecondaryPlanActivationDue(final Context ctx, final Home home)
    {
        try
        {
            final And condition = new And();
            condition.add(new LTE(SubscriberXInfo.SECONDARY_PRICE_PLAN_START_DATE, checkTime_));
            final Or endDateContition = new Or();
            endDateContition.add(new IsNull(SubscriberXInfo.SECONDARY_PRICE_PLAN_END_DATE));
            endDateContition.add(new GTE(SubscriberXInfo.SECONDARY_PRICE_PLAN_END_DATE, checkTime_));
            condition.add(endDateContition);
            condition.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE_INDEX));
            condition.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.AVAILABLE_INDEX));
            condition.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.PENDING_INDEX));
            condition.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.SUSPENDED_INDEX));
            condition.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.LOCKED_INDEX));
            condition.add(new NEQ(SubscriberXInfo.SECONDARY_PRICE_PLAN, Long.valueOf(DEFAULT_SECONDARY_PRICE_PLAN_ID)));

            home.where(ctx, condition).forEach(ctx, new Visitor()
            {
                public void visit(final Context ctx, final Object obj)
                    throws AgentException, AbortVisitException
                {

                    final Subscriber sub = (Subscriber) obj;
                    // TODO 2008-10-06 remove the following line when the Cloning before adapting is fixed
                    sub.setContext(ctx);
                    long primaryPricePlan = sub.getPricePlan();
                    long secondaryPricePlan = sub.getSecondaryPricePlan();

                    try
                    {
                        if (secondaryPricePlan != DEFAULT_SECONDARY_PRICE_PLAN_ID)
                        {
                            // use this method for all price plan switching needs
                            sub.switchPricePlan(ctx, secondaryPricePlan);

                            sub.setSecondaryPricePlan(DEFAULT_SECONDARY_PRICE_PLAN_ID);

                            Date oneHundredYearsFromNow = CalendarSupportHelper.get(ctx).findDateYearsAfter(100, checkTime_);
                            
                            if (sub.getSecondaryPricePlanEndDate() != null)
                            {
                                sub.setSecondaryPricePlan(primaryPricePlan);
                                sub.setSecondaryPricePlanStartDate(sub.getSecondaryPricePlanEndDate());
                                sub.setSecondaryPricePlanEndDate(null);
                            }
                            else
                            {
                                sub.setSecondaryPricePlanStartDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(25, checkTime_));
                                sub.setSecondaryPricePlanEndDate(null);
                            }

                            home.store(ctx, sub);
                        }
                    }
                    catch (Throwable e)
                    {
                        LogSupport.minor(ctx, this, "Unable to update subscriber " + sub.getId()
                                + " with the secondary price plan", e);
                    }
                }

            });

        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this,
                    "Encountered problem when selecting subscribers for secondary price plan activation", e);
        }
    }
}
