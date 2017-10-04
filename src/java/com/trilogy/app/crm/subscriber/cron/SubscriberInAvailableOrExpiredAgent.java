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
package com.trilogy.app.crm.subscriber.cron;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberView;
import com.trilogy.app.crm.bean.SubscriberViewHome;
import com.trilogy.app.crm.bean.SubscriberViewXInfo;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author jchen
 *
 * Process timer variables for subscribers which are in available or expired states
 * 
 * @author sajid.memon@redknee.com
 * @since 9.3
 * 
 * Added support for deactivating subscriber in Barred / Locked who are expired,
 * On the basis of Expiry Timer configuration available at SPID level.  
 */
public class SubscriberInAvailableOrExpiredAgent implements ContextAgent
{

    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException 
    {
		Home spidHome = (Home) ctx.get(CRMSpidHome.class);
		if (spidHome == null) 
		{
			LogSupport.minor(ctx, this, "CRMSpidHome not found in context");
			throw new AgentException(
					"System error: CRMSpidHome not found in context");
		}

		try 
		{
			Collection<CRMSpid> spids = spidHome.selectAll();

			final Iterator<CRMSpid> spidIterator = spids.iterator();

			// for each spid
			while (spidIterator.hasNext()) 
			{
				CRMSpid crmSp = (CRMSpid) spidIterator.next();

				try {
					processSubscribersWithAvailableStateTimedOut(ctx, crmSp);

					processSubscribersWithExpiredStateTimedOut(ctx, crmSp);

					/*
					 * In 9.3, deactivating Barred / Locked (Expired)
					 * subscribers will use the same Expiry Timer and not
					 * different timer. Hence, commenting out the below method
					 * call. May be it would required in future to have
					 * different timer other than Expiry Timer and would need
					 * this method.
					 */

					// processSubscribersWithBarredStateTimedOut(ctx, crmSp);

				} catch (HomeException e) 
				{
					LogSupport.minor(ctx, this, "SubscriberInAvailableOrExpriedTimer error : " + e.getMessage());
					throw new AgentException(
							"SubscriberInAvailableOrExpriedTimer error : " + e, e);
				}
			}
		} 
		catch (HomeException e) 
		{
			LogSupport.minor(ctx, this, "Error getting data from spid table");
		}
	}

    
    /**
     * Process all subscribers (in a given service provider) whose Available state
     * have been expired.
     *
     * @param ctx The operating context.
     * @param serviceProvider The given service provider.
     *
     * @exception HomeException Thrown if there are problems accessing the Home
     * information in the given context.
     */
    private void processSubscribersWithAvailableStateTimedOut(
        Context ctx,
        final CRMSpid serviceProvider)
        throws HomeException
    {
    	Home subViewHome = (Home) ctx.get(SubscriberViewHome.class);

        if (subViewHome == null)
        {
            throw new HomeException("System error: no SubscriberViewHome found in ctx.");
        }
    	
        Date expirationDate = getExpirationDate(ctx, serviceProvider.getSubAvailableTimer());
        
        ctx = ctx.createSubContext();        
        ctx.put("Count", Integer.valueOf(0));
        
        try
        {
        
	        And where = new And();
	        where.add(new EQ(SubscriberViewXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID));
	        where.add(new EQ(SubscriberViewXInfo.STATE, SubscriberStateEnum.AVAILABLE));
	        where.add(new EQ(SubscriberViewXInfo.SPID, Integer.valueOf(serviceProvider.getSpid())));
	        where.add(new LTE(SubscriberViewXInfo.DATE_CREATED, expirationDate));
	        
	        // This bring a smaller view of Subscriber data (only subscriberid column) into memory
	        //subViewHome = (Home) subViewHome.where(ctx, where);
	        //Collection <SubscriberView> subscriberIds = subViewHome.selectAll(ctx);
	        Collection <SubscriberView> subscriberViews = subViewHome.select(ctx, where);
	        
	        Iterator<SubscriberView> i = subscriberViews.iterator();
	        Home subHome = (Home) ctx.get(SubscriberHome.class);
	        
	        while(i.hasNext())
	        {
	        	SubscriberView subscriberView = i.next(); 
	       	   	//Subscriber sub = (Subscriber) subHome.find(subscriberView.getId());
	        	Subscriber sub = HomeSupportHelper.get(ctx).findBean(ctx, 
	        			Subscriber.class, new EQ(SubscriberXInfo.ID, subscriberView.getId()));
	       	   	
	        	if(sub != null)
	        	{
	        		try
		            {
			        	SubscriberStateEnum oldState = sub.getState();
			            if(LogSupport.isDebugEnabled(ctx))
			            {
			            	LogSupport.debug(ctx, this, "Deactivating Subscriber : "+ sub.getBAN());
			            }
			            
		                // Do state change for subscriber.
		                sub.setState(SubscriberStateEnum.INACTIVE);
		                //subscriber.setAvailableTimer(0);
		               
		                subHome.store(ctx,sub);
		
		                Integer count = (Integer)ctx.get("Count");
		                ctx.put("Count", Integer.valueOf(count.intValue()+1));
		                
		                // Generate Subscriber Expired ER.
		                logSubscriberStateTimerExpireER(
		                    ctx,
		                    sub,
		                    sub.getState(),
		                    oldState,
		                    serviceProvider.getSubAvailableTimer(),
		                    serviceProvider.getSubExpiryTimer());    
		            }
	                catch (Exception exception)
	                {
	                	LogSupport.minor(ctx, this,
	        	                "Failed to update subscriber '" + sub.getId() + "' from Available to Deactivated: " + exception.getMessage(), exception);
	                }
	        	} 
	        }
        }
    	catch (Exception exception)
	    {
	        LogSupport.minor(ctx, this,
                    "An Exception occured while processing Subscribers With Available State Timed Out : " + exception.getMessage(), exception);
	    }
        
        LogSupport.info(ctx, this, 
            "Updated " + ctx.get("Count") + " subscribers (in spid=" + serviceProvider.getId() 
            + ") from Available to Deactivated.");
    }


    /**
     * @param ctx the operating context
     * @return
     */
    static public Date getExpirationDate(final Context ctx, final int days)
    {
        final Calendar cal = Calendar.getInstance();
        CalendarSupportHelper.get(ctx).clearTimeOfDay(cal);

        cal.add(Calendar.DAY_OF_MONTH, -days);
        return cal.getTime();
    }


    /**
     * Process all subscribers (in a given service provider) whose Expired state
     * have been expired.
     *
     * @since 9.3
     * Added support to process all subscribers (in a given service provider) whose Barred / Locked state
     * have been expired. Reads the same Expiry Timer configuration at SPID level.
     * 
     * Supports:
     * 
     * "Active" (Expired) to Deactivate transition
     * "Barred / Locked" (Expired) to Deactivate transition
     * 
     * @param ctx The operating context.
     * @param serviceProvider The given service provider.
     *
     * @exception HomeException Thrown if there are problems accessing the Home
     * information in the given context.
     */
    private void processSubscribersWithExpiredStateTimedOut(
        Context ctx,
        final CRMSpid serviceProvider)
        throws HomeException
    {

        Home subViewHome = (Home) ctx.get(SubscriberViewHome.class);

        if (subViewHome == null)
        {
        	LogSupport.minor(ctx, this, "SubscriberViewHome not found in context");
            throw new HomeException("System error: no SubscriberViewHome found in ctx.");
        }
        
        Date expirationDate = getExpirationDate(ctx, serviceProvider.getSubExpiryTimer());
        
        ctx = ctx.createSubContext();
        ctx.put("Count", Integer.valueOf(0));
        
        try
        {
	        final Set<SubscriberStateEnum> stateSet = new HashSet<SubscriberStateEnum>();
	        stateSet.add(SubscriberStateEnum.ACTIVE);
	        stateSet.add(SubscriberStateEnum.LOCKED);
	        stateSet.add(SubscriberStateEnum.EXPIRED);
	
	        final And where = new And();
	        where.add(new In(SubscriberViewXInfo.STATE, stateSet));
	        where.add(new LT(SubscriberViewXInfo.EXPIRY_DATE, expirationDate));
	        where.add(new GT(SubscriberViewXInfo.EXPIRY_DATE, Subscriber.NEVER_EXPIRE_CUTOFF_DATE));
	        where.add(new EQ(SubscriberViewXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID));
	        where.add(new EQ(SubscriberViewXInfo.SPID, Integer.valueOf(serviceProvider.getSpid())));
	        
	        Collection <SubscriberView> subscriberViews = subViewHome.select(ctx, where);
	        
	        Iterator<SubscriberView> i = subscriberViews.iterator();
	        Home subHome = (Home) ctx.get(SubscriberHome.class);
	        
	        while(i.hasNext())
	        {
	        	SubscriberView subscriberView = i.next();       
	        	
	        	Subscriber sub = HomeSupportHelper.get(ctx).findBean(ctx, 
	        			Subscriber.class, new EQ(SubscriberXInfo.ID, subscriberView.getId()));
	        	
	        	if(sub != null)
	        	{
	        		if(LogSupport.isDebugEnabled(ctx))
		            {
		            	LogSupport.debug(ctx, this, "Deactivating Subscriber : "+ sub.getBAN());
		            }
		            
		        	MSP.setBeanSpid(ctx, sub.getSpid());
		        	SubscriberStateEnum oldState = sub.getState();
		        	
		            try
		            {
		                // Do state change for subscriber.
		                sub.setState(SubscriberStateEnum.INACTIVE);
		                //subscriber.setAvailableTimer(0);
		                subHome.store(ctx, sub);
		
		                Integer count = (Integer)ctx.get("Count");
		                
		                ctx.put("Count", Integer.valueOf(count.intValue()+1));
		                
		                // Generate Subscriber Expired ER.
		                logSubscriberStateTimerExpireER(
		                    ctx,
		                    sub,
		                    sub.getState(),
		                    oldState,
		                    serviceProvider.getSubAvailableTimer(),
		                    serviceProvider.getSubExpiryTimer());                       
		            }
		            catch (Exception exception)
		            {
		                LogSupport.minor(ctx, this,
		                        "Failed to update subscriber '" + sub.getId() 
		                        + "' from Active (Expired) Or Barred / Locked (Expired) to Deactivated: " 
		                        + exception.getMessage(), exception);
		            }
	        	}
	        }
        }
        catch (Exception exception)
	    {
	        LogSupport.minor(ctx, this,
                    "An Exception occured while processing Subscribers With Expired or Barred/Locked State Timed Out : " 
                    + exception.getMessage(), exception);
	    }
           
		LogSupport.info(ctx, this,
						"Updated " + ctx.get("Count") + " subscribers (in spid=" + serviceProvider.getId()
								+ ") from Active (Expired) Or Barred / Locked (Expired) to Deactivated.");
    }


   
    
    
    /**
     * Logs the "Subscriber State Timer Expire" Event Record.
     *
     * @param context The operating context.
     * @param oldSubscriber Old version of the subscriber (before state change).
     * @param newSubscriber New version of the subscriber (after state change).
     */
    private void logSubscriberStateTimerExpireER(
        final Context context,
        final Subscriber subscriber,
        final SubscriberStateEnum newState,
        final SubscriberStateEnum oldState,
        final int subAvailableTimer,
        final int subExpiryTimer)
    {
        new ERLogMsg(
            1102,  // erId
            1100,  // erClass
            "Subscriber State Timer Expire",
            subscriber.getSpid(),
            new String[] {
                subscriber.getMSISDN(),
                oldState.toString(),
                newState.toString(),
                String.valueOf(subAvailableTimer),
                String.valueOf(subExpiryTimer)
            }).log(context);
    }

}
