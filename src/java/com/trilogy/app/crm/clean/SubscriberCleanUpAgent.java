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
package com.trilogy.app.crm.clean;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCleanupView;
import com.trilogy.app.crm.bean.SubscriberCleanupViewHome;
import com.trilogy.app.crm.bean.SubscriberCleanupViewXInfo;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * Removes qualified inactive subscriber from database.
 *
 * @author rajith.attapattu@redknee.com
 * @author cindy.wong@redknee.com
 * @author isha.aderao@redknee.com
 * 
 * Refactored code to address database issue observed in TT#13102214034
 */
public class SubscriberCleanUpAgent implements ContextAgent
{
    
    /**
     * Process all Service Providers 
     *
     * @param context
     *            The operating context.
     * @throws AgentException
     *             Thrown if the agent cannot carry out its task.
     * @see com.redknee.framework.xhome.context.ContextAgent#execute
     */
    public void execute(final Context context) throws AgentException
    {
        try
        {
	        final Home spidHome = (Home) context.get(CRMSpidHome.class);
	        if (spidHome == null)
	        {
	            throw new AgentException("CRMSpidHome not found in context");
	        }
            Collection<CRMSpid> spids = null;
			try 
			{
				context.put(CronConstants.SUBSCRIBER_CLEANUP_TASK,true);
				spids = spidHome.selectAll();
			} 
			catch (HomeException e) 
			{
				throw new AgentException("Failed to retrive service provider records", e);
			}
			catch (UnsupportedOperationException e) 
			{
				throw new AgentException("Exception while retriving service provider records", e);
			}
			if(spids != null)
			{
	            Iterator<CRMSpid> i = spids.iterator();
	            
	            while(i.hasNext())
	            {
	            	final CRMSpid serviceProvider = (CRMSpid) i.next();
	            	processSubscribersForCleanup(context, serviceProvider);
	            }
			}
			else
			{
				LogSupport.info(context, this, "No service providers found.");
			}
        }
        catch(AgentException exception)
        {
        	throw new AgentException("Exception occured while running Subscriber CleanUp Task ", exception);
        }
        
    }
    
    /**
     * For each SPID, query the SubscriberCleanupView home with the spid specific criteria and fetch all the subscribers eligible for
     * clean up in smaller SubscriberCleanupView beans. Then, delete selected subs from the subscriber home one by one.
     *
     * @param context
     * @param serviceProvider
     * @throws AgentException
     */
    
    private void processSubscribersForCleanup(Context context, CRMSpid serviceProvider) throws AgentException
    {
    	
        Calendar cleanupDate = Calendar.getInstance();
        cleanupDate.add(Calendar.DAY_OF_MONTH, -serviceProvider.getSubCleanupDay());	                
        long cleanupBalThreshold = serviceProvider.getSubCleanUpBalThreshold();
        
        final And condition = new And();
        condition.add(new EQ(SubscriberCleanupViewXInfo.STATE, SubscriberStateEnum.INACTIVE));
        condition.add(new EQ(SubscriberCleanupViewXInfo.SPID, Integer.valueOf(serviceProvider.getSpid())));
        condition.add(new LT(SubscriberCleanupViewXInfo.LAST_MODIFIED, cleanupDate.getTime()));
       
        final Home subscriberHome = (Home) context.get(SubscriberHome.class);
        if (subscriberHome == null)
        {
            throw new AgentException("System error: subscriberHome not found in context");
        }
        
        final Home subscriberCleanupViewHome = (Home) context.get(SubscriberCleanupViewHome.class);
        if (subscriberCleanupViewHome == null)
        {
            throw new AgentException("System error: subscriberCleanupViewHome not found in context");
        }
        
        Collection<SubscriberCleanupView> subscribersView = null;
        try 
		{
        	subscribersView = (Collection<SubscriberCleanupView>) subscriberCleanupViewHome.select(context, condition);
		} 
		catch (HomeException e) 
		{
			throw new AgentException("Failed to retrive subscribers for SPID = "+serviceProvider.getSpid(), e);
		}
		catch (UnsupportedOperationException e) 
		{
			throw new AgentException("Exception while retriving subscribers for SPID = "+serviceProvider.getSpid(), e);
		}
		
		if(subscribersView != null)
		{
			Iterator<SubscriberCleanupView> subIterator =  subscribersView.iterator();
			
            while(subIterator.hasNext())
            {
            	SubscriberCleanupView subscriberCleanupView = (SubscriberCleanupView) subIterator.next();
            	Subscriber subscriber = null;
				try 
				{
					subscriber = HomeSupportHelper.get(context).findBean(context, 
							Subscriber.class, new EQ(SubscriberXInfo.ID, subscriberCleanupView.getId()));
				} 
				catch (HomeException e) 
				{
					throw new AgentException("Unable to find subscriber "+subscriberCleanupView.getId());
				}
				
				long amountOwing = subscriber.getAmountOwing(context);
            	if(amountOwing <= cleanupBalThreshold)
            	{
            		if(LogSupport.isDebugEnabled(context))
            		{
            			LogSupport.debug(context, this, "Subscriber " + subscriber.getId() + " has been marked for deletion."
            					+" Amount owing = "+amountOwing +" is less than or equal to SubCleanupBalanceThreshhold = "+cleanupBalThreshold);
            		}
        			try
                    {
        				subscriberHome.remove(context, subscriber);
                    }
                    catch (Throwable throwable)
                    {
                        LogSupport.minor(context, this, "Couldn't remove the Subscriber with ID [" + subscriber.getId() + "] : "+ throwable);
                    }
            	}
            }
		}
		else
		{
			LogSupport.info(context, this, "No eligible subscriber found for cleanup for service provider "+serviceProvider.getSpid());
		}
    }
}
