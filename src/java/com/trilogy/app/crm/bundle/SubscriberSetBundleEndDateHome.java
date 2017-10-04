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
package com.trilogy.app.crm.bundle;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;

/**
 * Calculates and sets BundleFee EndDate based on No. of Payments from BundleFee
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriberSetBundleEndDateHome extends HomeProxy
{
    public SubscriberSetBundleEndDateHome(final Home delegate)
    {
        super(delegate);
    }

    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        updateBundleFeeEndDate(ctx, (Subscriber) obj);
        return getDelegate(ctx).create(ctx, obj);
    }

    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final Subscriber sub = (Subscriber) obj;
        if(oldSub.getState() == SubscriberStateEnum.AVAILABLE && sub.getState() == SubscriberStateEnum.ACTIVE)
        {
            updateBundleFeeStartDate(ctx, sub);
        }
        updateBundleFeeEndDate(ctx, (Subscriber) obj);
        return getDelegate(ctx).store(ctx, obj);
    }
    
    
    public void updateBundleFeeStartDate(final Context ctx, final Subscriber sub)
    {
        for (final Iterator it = sub.getBundles().entrySet().iterator(); it.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) it.next();
            final BundleFee fee = (BundleFee) entry.getValue();
            try
            {
                if (fee.getBundleProfile(ctx, sub.getSpid()).getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL))
                {   
                    fee.setStartDate(CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Updating start date of bundle : " + fee.getId()
                                + ", subscriber : " + sub.getId() + ", startDate : " + fee.getStartDate());
                    }
                }
            }
            catch (Exception e)
            {
                LogSupport.minor(ctx, this,
                        "Failed to update bundle start date for subscriber : " + sub.getId() + ", bundle : "
                                + fee.getId(),e);
            }
        }
    }

    public void updateBundleFeeEndDate(final Context ctx, final Subscriber sub) throws HomeException
    {
        for (final Iterator it = sub.getBundles().entrySet().iterator(); it.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) it.next();
            final BundleFee fee = (BundleFee) entry.getValue();
            if (fee.getPaymentNum() > 0)
            {
            	final Date start = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(fee.getStartDate());
            	Date end = CalendarSupportHelper.get(ctx).findDateMonthsAfter(fee.getPaymentNum(), start);
            	try 
            	{
            		if(fee.getBundleProfile(ctx).getChargingRecurrenceScheme() == ServicePeriodEnum.ONE_TIME)
		      	   	{	// for only one-time Aux/PP bundles, we want end date with exact timestamp, else legacy behaviour
            			end = CalendarSupportHelper.get(ctx).findDateMonthsAfter(fee.getPaymentNum(), fee.getStartDate());
		      	   	}
            		fee.setEndDate(end);
	                // clear the Payment Num property to prevent recalculation on next update
	                fee.setPaymentNum(0);
				} 
            	catch (Exception e) 
            	{
					throw new HomeException("Exception while updating End Date of bundle",e);
					
				}
            }
            else
            {
                BundleProfile bundle = null;
                try
                {
                    bundle = fee.getBundleProfile(ctx);
                    if (bundle != null && bundle.getRecurrenceScheme().isOneTime() && bundle.getInterval() == DurationTypeEnum.BCD_INDEX)
                    {
                        Date oneTimeBundleEndDate = CalendarSupportHelper.get(ctx).calculateBillCycleEndDate(getContext(), fee.getStartDate(), sub.getAccount(ctx).getBillCycleDay(ctx));
                        fee.setEndDate(oneTimeBundleEndDate);
                        if(LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Setting end date = "+oneTimeBundleEndDate+" " +
                            		" for one time bundle having id = "+bundle.getBundleId()+" for subscriber " +
                    				" with id = "+sub.getId());
                        }
                    }
                }
                catch (Exception e) 
    			{
                    LogSupport.minor(ctx, this, "Exception occured while setting end date " +
                    		"for one time bundle with id "+bundle.getBundleId()+" " +
            				"for subscriber with id  "+sub.getId()+". Now, Setting end date to : "+fee.getEndDate(), e);
                }
            }
        }
    }
}
