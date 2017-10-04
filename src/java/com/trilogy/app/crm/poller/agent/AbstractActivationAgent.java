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
package com.trilogy.app.crm.poller.agent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.language.LangHome;
import com.trilogy.framework.xhome.language.LangXInfo;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.util.snippet.log.Logger;

/**
 * Base-class for agents that need to activate subscribers.
 * 
 * @author ravi.patel@redknee.com
 * 
 */
public abstract class AbstractActivationAgent
{
    public void activateSubscriber(final Context ctx, final Subscriber subscriber, final Date activationDate,
            final Date expiryDate) throws HomeException
    {
        // Change subscriber state only if the original subscriber
        // state is Available.
        if (EnumStateSupportHelper.get(ctx).stateEquals(subscriber, SubscriberStateEnum.AVAILABLE))
        {
            // Check if subscriber AutoActivation i.e billingDay = Activationday required ? if yes then set it
            if (SubscriberSupport.isAutoActivationRequired(ctx, subscriber))
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(activationDate);
                int activationDay = cal.get(Calendar.DAY_OF_MONTH);
                SubscriberSupport.setSubscribersBillingDayToActivationDay(ctx, subscriber, activationDay);
            }

            /*
             * Manda - Set the End Date of an Auxilary service correctly i.e relative to activation date
             */
            setUpdatedSubscriberAuxiliaryServices(ctx, subscriber, activationDate);
            subscriber.setFirstActivation(true);
            subscriber.setState(SubscriberStateEnum.ACTIVE);

            if (expiryDate != null && expiryDate.after(subscriber.getExpiryDate()))
            {
                subscriber.setExpiryDate(expiryDate);
            }

            try
            {
            	/**
            	 * TT#13071615011
            	 * Setting lang of subscriber in context. Its been used at HomeMessageMgrSPI.get(..) to fetch lang of 
            	 * subscriber. Default date format is then fetched based on the langauage.
            	 */
            	Home langHome = (Home) ctx.get(LangHome.class);
            	if (langHome!=null){
            		Lang lang = (Lang) langHome.find(ctx, new EQ(LangXInfo.CODE, subscriber.getBillingLanguage()));
            		if (lang!=null){
            			ctx.put(Lang.class, lang);
            		} else {
            			ctx.put(Lang.class, Lang.DEFAULT);
            		}
            	}
            	final Home home = (Home) ctx.get(SubscriberHome.class);
            	home.store(ctx, subscriber);
            }
            catch (HomeException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Failed to activate subscriber \"{0}\". Caused by {1}.",
                        new Object[] {subscriber.getId(), e.getMessage()});

                throw new HomeException(formattedMsg, e);
            }
        }
        else
        {
            new DebugLogMsg(this,"Subscriber MSISDN [" + subscriber.getMSISDN() + "] is not in AVAILABLE state in CRM. Skipping activation.",null).log(ctx);
        }
    }

    /**
     * Manda - This method updates the subscriber Auxiliary services with the activation start and end dates and updates
     * the subscriber object
     * 
     * @param ctx
     *            Context object
     * @param subscriber
     *            Subscriber Object
     * @param activationTime
     *            Time at which the subscriber activated
     */
    private void setUpdatedSubscriberAuxiliaryServices(final Context ctx, final Subscriber subscriber,
            final Date activationTime)
    {
        final List<SubscriberAuxiliaryService> auxServiceCol = subscriber.getAuxiliaryServices(ctx);
        if (auxServiceCol != null && auxServiceCol.size() > 0)
        {
            List<SubscriberAuxiliaryService> newAuxServiceCol = new ArrayList<SubscriberAuxiliaryService>();

            for (SubscriberAuxiliaryService subService : auxServiceCol)
            {
                // start date for the Aux. Service should always be the activation date

                subService.setStartDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(activationTime));

                final GregorianCalendar endDateCal = new GregorianCalendar();
                endDateCal.setTime(activationTime);
                try
                {
                    endDateCal.set(Calendar.DATE, SubscriberSupport.getBillCycleDay(ctx, subscriber));
                }
                catch (HomeException e)
                {
                    Logger.major(ctx, this, "Error while getting the bill Cycle day for Subscriber = "
                            + subscriber.getId() + " While Activating the subscriber by URS First Call Activation", e);
                }

                // if the aux. service is configured to only be valid for x payments, then calculate the end date by
                // adding x months
                if (subService.getPaymentNum() > 0)
                {
                    endDateCal.add(Calendar.MONTH, subService.getPaymentNum());
                }
                // if the aux. service doesn't have the "PaymentNum" set then let the service run for 20 years
                else
                {
                    endDateCal.add(Calendar.YEAR, 20);
                }

                final Date endDate = new Date(endDateCal.getTimeInMillis());

                subService.setEndDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(endDate));
                subService.setProvisioned(true);
                newAuxServiceCol.add(subService);

                if (Logger.isDebugEnabled())
                {
                    Logger.debug(ctx, this, "StartDate = " + subService.getStartDate() + " End Date = "
                            + subService.getEndDate());
                }
            }

            subscriber.setAuxiliaryServices(newAuxServiceCol);
        }

        /**
         * Manda - Set the Start/Activation Date of the Subscriber with the date coming from ER
         */
        subscriber.setStartDate(activationTime);
    }
}