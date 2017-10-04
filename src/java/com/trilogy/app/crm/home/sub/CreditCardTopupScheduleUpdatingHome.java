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
package com.trilogy.app.crm.home.sub;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.bean.paymentgatewayintegration.SubscriptionSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This home updates the Topup Schedule's nextApplication date in case of subscriber's 
 * Price Plan (version) changes.
 * 
 * @author mangaraj.sahoo@redknee.com
 * @since 9.3
 */

public class CreditCardTopupScheduleUpdatingHome extends HomeProxy
{

    private static final long serialVersionUID = 1L;
    

    public CreditCardTopupScheduleUpdatingHome(Home delegate)
    {
        super(delegate);
    }
    
    
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeInternalException, HomeException
    {
        Subscriber newSub = (Subscriber) obj;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (oldSub == null)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "OLD SUBSCRIBER not found from context. Retrieving from DB.");
            }
            oldSub = (Subscriber) find(ctx, newSub.getId());
        }
        
        newSub = (Subscriber) super.store(ctx, obj);
        
        boolean ismsisdnChanged = !SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN());
        
        if(newSub.getSubscriberType().getIndex() == SubscriberTypeEnum.PREPAID_INDEX)
        {
        	PricePlan newPP = PricePlanSupport.getPlan(ctx, newSub.getPricePlan());
        	if ((oldSub.getPricePlan() != newSub.getPricePlan() 
        			|| oldSub.getPricePlanVersion() != newSub.getPricePlanVersion()) && PricePlanSubTypeEnum.PAYGO.equals(newPP.getPricePlanSubType()))
        	{
        		updateTopUpScheduleForPayGoSubscriber(ctx, newSub, ismsisdnChanged);
        	}
        	else if (ismsisdnChanged)
        	{
        		updateTopupScheduleOnMsisdnSwap(ctx, newSub);
        	}
        	
        }
        else if(newSub.getSubscriberType().getIndex() == SubscriberTypeEnum.POSTPAID_INDEX && ismsisdnChanged)
    	{
        	updateTopupSchedule(ctx, newSub, ismsisdnChanged);
        }
        
        
        return newSub;
    }
    
    
    private void updateTopUpScheduleForPayGoSubscriber(Context ctx, Subscriber sub, boolean ismsisdnChanged) throws HomeInternalException, HomeException
    {
        new DebugLogMsg(this, "Updating nextApplicationDate for subscriber : " + sub.getId()).log(ctx);
        CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
        int oneDayPriorSchedule = crmSpid.isScheduleCCAtuOneDayPriorExpiry() ? -1 : 0;

        Calendar expiryCalendar = Calendar.getInstance();
        expiryCalendar.setTime(sub.getExpiryDate());
        expiryCalendar.add(Calendar.DAY_OF_YEAR, oneDayPriorSchedule);

        Date nextApplicationDate = expiryCalendar.getTime();

        TopUpSchedule existingSchedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class,
                new EQ(TopUpScheduleXInfo.BAN, sub.getBAN()));

        if (existingSchedule != null)
        {
        	if(!ismsisdnChanged)
        	{
        		Date existingNextApplicationDate = existingSchedule.getNextApplication();
        		existingSchedule.setNextApplication(nextApplicationDate);
        		new DebugLogMsg(this, "Updating TopUpSchedule nextApplicationDate for subscriber : " + sub.getId()
        				+ " from : " + existingNextApplicationDate + " to : " + nextApplicationDate).log(ctx);
        	}
        	else if(ismsisdnChanged)
        	{
        		existingSchedule.setMsisdn(sub.getMsisdn());
        	}
        	HomeSupportHelper.get(ctx).storeBean(ctx, existingSchedule);
        }
        else
        {
        	if(LogSupport.isDebugEnabled(ctx))
        	{	
        		new DebugLogMsg(this, "NO TopUpSchedule present cannot update nextApplicationDate for subscriber : "
                    + sub.getId()).log(ctx);
        	}
        }
    }
    
    private void updateTopupSchedule(Context ctx, Subscriber newSub, boolean ismsisdnChanged) throws HomeInternalException, HomeException
    {
        Predicate where = new EQ(TopUpScheduleXInfo.SUBSCRIPTION_ID, newSub.getId());
        TopUpSchedule schedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, where);
        if (schedule != null)
        {
        	if(!ismsisdnChanged)
        	{
	            if(schedule.isScheduleUserDefined())
	            {
	                schedule.setNextApplication(SubscriptionSupport.determineNextTopupScheduleDate(ctx, schedule.getNextApplication()));
	            }
	            else
	            {
	                schedule.setNextApplication(SubscriptionSupport.determineNextTopUpDate(ctx, newSub));
	            }
        	}
            schedule.setMsisdn(newSub.getMsisdn()); // In case of Msisdn Swap
            
            HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
        }
    }
    
    private void updateTopupScheduleOnMsisdnSwap(Context ctx, Subscriber newSub) throws HomeInternalException, HomeException
    {
        Predicate where = new EQ(TopUpScheduleXInfo.SUBSCRIPTION_ID, newSub.getId());
        TopUpSchedule schedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, where);
        if (schedule != null)
        {
            schedule.setMsisdn(newSub.getMsisdn()); // In case of Msisdn Swap
            
            HomeSupportHelper.get(ctx).storeBean(ctx, schedule);
        }
    }
}
