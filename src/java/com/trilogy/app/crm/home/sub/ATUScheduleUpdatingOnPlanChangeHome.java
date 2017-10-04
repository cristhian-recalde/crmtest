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

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Removes the Recurring Top up schedule and balance threshold ATU as and when price plan changes for PREPAID subscribers.
 * This behavior is depending on configuration at SPID. 
 * 
 * @author vijay.gote
 * @since 9.9
 */
public class ATUScheduleUpdatingOnPlanChangeHome extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ATUScheduleUpdatingOnPlanChangeHome(Home delegate)
    {
        super(delegate);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        
        Subscriber newSub = (Subscriber) obj;
        
        /* If PREPAID subscriber and Price plan is changed then update CC ATU schedule */
        if ((SubscriberTypeEnum.POSTPAID.equals(newSub.getSubscriberType())) || (SubscriberStateEnum.AVAILABLE.equals(oldSub.getState())) ||
        		(SubscriberTypeEnum.PREPAID.equals(newSub.getSubscriberType()) && !(SubscriberStateEnum.ACTIVE.equals(newSub.getState()))))
        {
        	return super.store(ctx, obj);
        }
        
        Object updateObj = updateSubscriberATUSchedules(ctx, newSub, oldSub);
        super.store(ctx, updateObj);
        return newSub;
    }
    
    /**
     * To set subscribers ATU Balance threshold and ATU amount to default value and remove Top schedule depending on SPID configuration
     * 
     * @param ctx
     * @param newSub
     * @param oldSub
     * @return
     */
    private Subscriber updateSubscriberATUSchedules(Context ctx, Subscriber newSub, Subscriber oldSub)
    {
    	if(SubscriberStateEnum.ACTIVE.equals(newSub.getState()))
        {
        	try
        	{
        		if (oldSub != null && newSub != null)
        		{
        			//if PricePlan change has occurred. 
        			if(oldSub.getPricePlan() != newSub.getPricePlan())
        			{
        				PricePlan newPricePlan = PricePlanSupport.getPlan(ctx, newSub.getPricePlan());
        				CRMSpid spid = SpidSupport.getCRMSpid(ctx, newSub.getSpid());
        				switch (newPricePlan.getPricePlanSubType().getIndex()) 
        				{
        				case PricePlanSubTypeEnum.MRC_INDEX:
        					if(!spid.getRecurCCAtuAllowedForMrc())
        					{
        						removeRecurringATU(ctx, newSub);
        					}
        					if(!spid.getBalThresholdCCAtuAllowedForMrc())
        					{
        						newSub = removeBalanceThresholdATU(ctx, newSub);
        					}
        					break;
        				case PricePlanSubTypeEnum.PAYGO_INDEX:
        					if(!spid.getRecurCCAtuAllowedForPaygo())
        					{
        						removeRecurringATU(ctx, newSub);
        					}
        					if(!spid.getBalThresholdCCAtuAllowedForPaygo())
        					{
        						newSub = removeBalanceThresholdATU(ctx, newSub);
        					}
        					break;
        				case PricePlanSubTypeEnum.LIFETIME_INDEX:
        					if(!spid.getRecurCCAtuAllowedForLifetime())
        					{
        						removeRecurringATU(ctx, newSub);
        					}
        					if(!spid.getBalThresholdCCAtuAllowedForLifetime())
        					{
        						newSub = removeBalanceThresholdATU(ctx, newSub);
        					}
        					break;
        				case PricePlanSubTypeEnum.PICKNPAY_INDEX:
        					if(!spid.isRecurCCAtuAllowedForPickNPay())
        					{
        						removeRecurringATU(ctx, newSub);
        					}
        					if(!spid.getBalThresholdCCAtuAllowedForPickNPay())
        					{
        						newSub = removeBalanceThresholdATU(ctx, newSub);
        					}
        					break;
        				default:
        					break;
        				}
        			}
        		}
        	}
        	catch (Exception e)
        	{
        		new MinorLogMsg(this.getClass(), "Error occured when attempting to updtae ATU on price plan change for sub=" 
        				+ oldSub.getId(), e).log(ctx);
        	}
        }
    	return newSub;
    }
    
    /**
     * Setting ATU Balance Threshold and ATU Amount of subscriber to default value.
     * 
     * @param ctx
     * @param sub
     * @throws Exception
     */
    private Subscriber removeBalanceThresholdATU(Context ctx, Subscriber sub) throws Exception
    {
    	// sets the ATU Balance threshold and ATU amount to default value
    	sub.setAtuBalanceThreshold(Subscriber.DEFAULT_ATUBALANCETHRESHOLD);
    	sub.setAtuAmount(Subscriber.DEFAULT_ATUAMOUNT);
    	
    	return sub;
    }
    
    /**
     * Removing Top up schedule of a Subscriber
     * 
     * @param ctx
     * @param sub
     */
    private void removeRecurringATU(Context ctx, Subscriber sub)
    {
        TopUpSchedule topUpSchedule = null;
        try
        {
        	final Object conditionSubscriberId = new EQ(TopUpScheduleXInfo.SUBSCRIPTION_ID, sub.getId());
            topUpSchedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, conditionSubscriberId);
            
            if(topUpSchedule != null)
        	{
            	HomeSupportHelper.get(ctx).removeBean(ctx, topUpSchedule);
        	}
            else
            {
            	LogSupport.info(ctx, this, "Could not find Recurring ATU schedule for subscriber " + sub.getId());
            }
        }
        catch (HomeException e)
        {
        	LogSupport.major(ctx, this, "Unable to delete Recurring ATU schedule for subscriber : "+ sub.getId());
        }
    }
}
