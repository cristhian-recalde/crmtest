/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * A Predicate used to find Subscribers (in Active/Suspended state) whose expiryDate has been reached.
 *
 * See com.redknee.app.crm.subscriber.elang.SubscriberExpiredXStatement for ELang version.
 * 
 * @author jimmy.ng@redknee.com
 */
public class SubscriberExpiredPredicate implements Predicate
{
    /**
     * Creates a new predicate.
     */ 
	public SubscriberExpiredPredicate()
	{
	    this(0);
	}
	
	public SubscriberExpiredPredicate(int adjExpiryDays)
	{
	    adjExpiryDays_ = adjExpiryDays;
	}

    /*
     * (non-Javadoc)
     * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
	public boolean f(Context ctx, final Object obj)
	{
	    // OID 36176
		final Subscriber subscriber = (Subscriber) obj;

		//wallets never expire
		SubscriptionType subscriptionType = subscriber.getSubscriptionType(ctx);
		if (subscriptionType == null)
		{
		    new MinorLogMsg(this, "Could not check the subscription type of: " + subscriber.getId(), null).log(ctx);
		    return false;
		}
		
		if (subscriptionType.isWallet())
		{
            new DebugLogMsg(this, "Subscription " + subscriber.getId() + " does not expire (type=" + subscriptionType.getTypeEnum(), null).log(ctx);
		    return false;
		}

        if (subscriber.getExpiryDate() != null && subscriber.isPrepaid()
                && (!SystemSupport.supportsUnExpirablePrepaidSubscription(ctx)))
        {
	        final Date todayDate =
	            CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(Calendar.getInstance().getTime());
	        final Date subscriberExpiryDate =
	            CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(getAdjustedExpiry(subscriber.getExpiryDate()));
	        
	        if ((SubscriberStateEnum.ACTIVE.equals(subscriber.getState()) ||
	                SubscriberStateEnum.SUSPENDED.equals(subscriber.getState())) &&
	            !todayDate.before(subscriberExpiryDate))
	        {
	            if( LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PREPAID_GROUP_POOLED_LICENSE_KEY ) )
	            {
	                try
	                {
	                    CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());
	                    if( crmSpid != null )
	                    {
	                        if( crmSpid.isPrepaidPooledExpiry()
	                                && subscriber.isPooled(ctx) )
                            {
                                // OID 36342
	                            // Prepaid leaders and members expire when they are part of a Group Pooled account
                                return true;
                            }
                            else if( (crmSpid.isPrepaidNonPooledExpiry()
                                        && !subscriber.isPooled(ctx)) )
                            {
                                // OID 36343
                                // Prepaid subscribers expire when they do not belong to a Group Pooled account
                                return true;
                            }
                            else if( subscriber.isPooledGroupLeader(ctx) )
                            {
                                // OID 36342
                                // Prepaid leader of Group Pooled account expires if SPID.PrepaidPooledExpiry is false.
                                // Prepaid members never expire in this case
                                return true;
                            }
	                        
	                        return false;
	                    }
	                }
                    catch (HomeException e)
                    {
                        new DebugLogMsg(this, HomeException.class.getSimpleName() + " occurred in SubscriberExpiredPredicate.f(): " + e.getMessage(), e).log(ctx);
                    }   
	            }

	            return true;
	        }
		}
        
		return false;
	}

    /**
	 * returns a new adjusted expiry date (Used for pre-expiry stuff)
	 * @param expiryDate
	 * @return
	 */
	public Date getAdjustedExpiry(Date expiryDate)
    {
	    Date dat = expiryDate;
	    if (adjExpiryDays_ != 0)
	    {
	        final Calendar calendar = Calendar.getInstance();
	        calendar.setTime(expiryDate);
			// The range should be [dat, expiryDate]. That's why we need to multiply by -1 
	        calendar.add(Calendar.DATE, -1 * Math.abs(adjExpiryDays_));
	        
	        dat = calendar.getTime(); 
	    }
        return dat;
    }
    
    private int adjExpiryDays_ = 0;
}
