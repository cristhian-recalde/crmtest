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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.PricePlanMigration;
import com.trilogy.app.crm.bean.PricePlanMigrationHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.clean.CronConstants;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InternalLogSupport;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author skushwaha
 * 
 * Changes price plan for all the subscriber belonging to a price-plan to a new price plan
 * on completion of promotion duration
 */
public class AutoPricePlanMigration
{

    public AutoPricePlanMigration(final Context context)
    {
        context_ = context;
    }


    /**
     * Gets the operating context.
     * 
     * @return The operating context.
     */
    protected Context getContext()
    {
        return context_;
    }


    /**
     * Processes each of the activations.
     */
    public void processPricePlanMigration()
    {
        Date runningDate = null;
        Calendar rightNow = Calendar.getInstance();
        PricePlanMigration pricePlanMigration;
        boolean switchPricePlan = false;
        Collection subscribers = null;
        try
        {
            Home subsHome = (Home) getContext().get(SubscriberHome.class);
            if (subsHome == null)
            {
                throw new HomeException("SubscriberHome not found in the context");
            }
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "Processing PricePlan Migration--", null).log(getContext());
            }
            //CRM: remove the if(true) block
            if (true)
            {
                final Collection promotionPricePlans = getPromotionalPricePlans();
                for (Iterator i = promotionPricePlans.iterator(); i.hasNext();)
                {
                    pricePlanMigration = (PricePlanMigration) i.next();
                    runningDate = CalendarSupportHelper.get(getContext()).getRunningDate(getContext());
                    runningDate = CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(runningDate);
                    if (LogSupport.isDebugEnabled(getContext()))
                    {
                        new DebugLogMsg(this, "1st promotion pricePlan --" + pricePlanMigration.getCurrentPricePlan(),
                                null).log(getContext());
                    }
                    // switching the pricePlan if systemDate >= pricePlanMigrationDate or
                    // pricePlanMigrationDate = null
                    if (pricePlanMigration.getMigrationDate() == null)
                        switchPricePlan = true;
                    else if (pricePlanMigration.getMigrationDate().compareTo(runningDate) == 0)
                        switchPricePlan = true;
                    if (LogSupport.isDebugEnabled(getContext()))
                    {
                        new DebugLogMsg(this, "--switchPricePlan--" + switchPricePlan + "--PP Table switch date--"
                                + pricePlanMigration.getMigrationDate() + "--runningDate--" + runningDate, null)
                                .log(getContext());
                    }
                    if (switchPricePlan)
                    {
                        subscribers = getSubscribers(pricePlanMigration
								.getCurrentPricePlan());
						if (subscribers == null) {
							if (LogSupport.isDebugEnabled(getContext())) {
								new DebugLogMsg(this,
										"No subscriber for promotional price plan --"
												+ pricePlanMigration
														.getCurrentPricePlan(),
										null).log(getContext());
							}
							continue;
						}
                        for (Iterator j = subscribers.iterator(); j.hasNext();)
                        {
                            Subscriber subscriber = (Subscriber) j.next();
                            if (subscriber == null)
                            {
                                //throw new HomeException("subscriber is null ");
                            	InternalLogSupport
										.major(
												"AutoPricePlanMigration.processPricePlanMigration()",
												"subscriber is null ", null);
                            	continue;
                            }
                            if (subscriber.getStartDate() == null)
                            {
                                //throw new HomeException("subscriber startDate is null ");
                            	InternalLogSupport
										.major(
												"AutoPricePlanMigration.processPricePlanMigration()",
												"subscriber startDate/Activation Date is null ",
												null);
								continue;
                            }
                            if (LogSupport.isDebugEnabled(getContext()))
                            {
                                new DebugLogMsg(this, "1st promotion pricePlan --"
                                        + pricePlanMigration.getCurrentPricePlan(), null).log(getContext());
                            }
                            if (rightNow == null)
                            {
                                throw new Exception("Calendar object rightNow is null");
                            }
                            rightNow.setTime(subscriber.getStartDate());
                            rightNow.add(Calendar.MONTH, pricePlanMigration.getPromotionDuration());
                            if (LogSupport.isDebugEnabled(getContext()))
                            {
                                new DebugLogMsg(this, "subscribers --SPID--" + subscriber.getSpid()
                                        + "--PPM Table SPID--" + pricePlanMigration.getSpid() + "--subs Type--"
                                        + subscriber.getSubscriberType() + "--PPM table subs type--"
                                        + pricePlanMigration.getSubscriberType() + "--Subscriber PP usage duration--"
                                        + CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(rightNow.getTime()) + "--runningDate--"
                                        + runningDate, null).log(getContext());
                            }
                            if (subscriber.getSpid() == pricePlanMigration.getSpid()
                                    && subscriber.getSubscriberType() == pricePlanMigration.getSubscriberType()
                                    && runningDate.compareTo(CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(rightNow.getTime())) >= 0)
                            {
                                if (LogSupport.isDebugEnabled(getContext()))
                                {
                                    new DebugLogMsg(this, "switching subscriber from current price plan "
                                            + subscriber.getPricePlan(), null).log(getContext());
                                }
                                subscriber.setPricePlan(pricePlanMigration.getNewPricePlan());
                                try
                                {
                                	subsHome.store(subscriber);
                                }
                                catch ( Exception e)
                                {
                                	if (LogSupport.isDebugEnabled(getContext()))
                                    {
                                        new DebugLogMsg(this, "Fail to change price plan of subscriber -"
                                                + subscriber.getMSISDN(), e).log(getContext());
                                    }
                                }
                                if (LogSupport.isDebugEnabled(getContext()))
                                {
                                    new DebugLogMsg(this, " to new price plan " + subscriber.getPricePlan(), null)
                                            .log(getContext());
                                }
                            }
                        }
                    }
                    subscribers = null;
                }
            }
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(this, "Failed to look up price plans.", exception).log(getContext());
            return;
        }
        catch (final Exception exception)
        {
            new MajorLogMsg(this, "Exceptions--", exception).log(getContext());
        }
    }


    private Collection getPromotionalPricePlans() throws HomeException
    {
        Collection promotionsPricePlans = new ArrayList();
        Home home = (Home) getContext().get(PricePlanMigrationHome.class);
        if (home == null)
        {
            throw new HomeException("PricePlanMigrationHome is not found in the context");
        }
        promotionsPricePlans = home.selectAll();
        return promotionsPricePlans;
    }


    private Collection getSubscribers(final long pricePlan) throws HomeException
    {
        Collection subscriber = new ArrayList();
        Home subHome = (Home) getContext().get(SubscriberHome.class);
        if (subHome == null)
        {
            throw new HomeException("SubscriberHome is not found in the context");
        }
        /*
        String sqlQuery = "PRICEPLAN= " + pricePlan; //+ " and ";
        sqlQuery += " and state<>" + SubscriberStateEnum.INACTIVE_INDEX;
        sqlQuery += " and state<>" + SubscriberStateEnum.PENDING_INDEX;
        sqlQuery += " and state<>" + SubscriberStateEnum.AVAILABLE_INDEX;
        
        if (LogSupport.isDebugEnabled(getContext())) {
			new DebugLogMsg(this, " sqlQuery " + sqlQuery, null)
					.log(getContext());
		}
        Predicate test = new Predicate()
        {

            public boolean f(Object bean)
            {
                Subscriber subscriber = (Subscriber) bean;
				return (subscriber.getPricePlan() == pricePlan
						&& !(subscriber.getState().equals(SubscriberStateEnum.INACTIVE))
						&& !(subscriber.getState().equals(SubscriberStateEnum.PENDING)) && !(subscriber
						.getState().equals(SubscriberStateEnum.AVAILABLE)));
            }
        };
        subscriber = (Collection) subHome.cmd(new SelectHomePredicateCmd(new HomePredicate(test, sqlQuery)));
        */
        subscriber = subHome.select(getContext(), new And().add(new EQ(SubscriberXInfo.PRICE_PLAN,String.valueOf(pricePlan)))
        												   .add(new NEQ(SubscriberXInfo.STATE,SubscriberStateEnum.INACTIVE_INDEX))
        												   .add(new NEQ(SubscriberXInfo.STATE,SubscriberStateEnum.PENDING_INDEX))
        												   .add(new NEQ(SubscriberXInfo.STATE,SubscriberStateEnum.AVAILABLE_INDEX)));
       
        return subscriber;
    }

    /**
     * The operating context.
     */
    private final Context context_;
}