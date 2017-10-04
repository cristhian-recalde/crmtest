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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.cron;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.filter.EitherPredicate;
import com.trilogy.app.crm.filter.SubscriberAvailableStateToActivePredicate;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;

/**
 * @author amedina
 *
 * Changes the state from available to AActive ONLY if the activation date is set
 * This class is deprecated, function moved to SubscriberFutureActivationAgent.
 */
public class SubscriberFromAvailableToActiveAgent implements ContextAgent 
{

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context context) throws AgentException 
	{
		try 
		{
	        final Collection serviceProviders = getServiceProviders(context);
	        
	        for (final Iterator i = serviceProviders.iterator(); i.hasNext();)
	        {
	            final CRMSpid serviceProvider = (CRMSpid) i.next();
	            
	            processSubscribersWithAvailableStateToActive(context, serviceProvider);
	        }
		}
		catch(HomeException e)
		{
			throw new AgentException("SubscriberInAvailableOrExpriedTimer error:" + e, e);
		}

	}
	
    /**
	 * @param context
	 * @param serviceProvider
     * @throws HomeException
     * @throws UnsupportedOperationException
	 */
	private void processSubscribersWithAvailableStateToActive(Context context, CRMSpid serviceProvider) 
	throws HomeException
	{
        final Home home = (Home) context.get(SubscriberHome.class);

        if (home == null)
        {
            throw new HomeException("System error: no SubscriberHome found in context.");
        }
        Date today = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(new Date());
        final String sqlQuery =
            "state = " + SubscriberStateEnum.AVAILABLE.getIndex() +
            " AND spid = " + serviceProvider.getId() +
            " AND startdate >= " + today.getTime();
        
        final Collection subscribers =
            home.select(context,
                    new EitherPredicate(
                        new SubscriberAvailableStateToActivePredicate(serviceProvider, today),
                        sqlQuery));
        
        new InfoLogMsg(
            this,
            "Attempting to update states for those subscribers (in spid=" + serviceProvider.getId() +
            ") whose Available Timers need to be active",
            null).log(context);
        
        for (final Iterator i = subscribers.iterator(); i.hasNext();)
        {
            final Subscriber subscriber = (Subscriber) i.next();
            
            // Do state change for subscriber.
            subscriber.setState(SubscriberStateEnum.ACTIVE);
            home.store(context,subscriber);
            
        }
        
        new InfoLogMsg(
            this,
            "Updated " + subscribers.size() + " subscribers (in spid=" + serviceProvider.getId() +
            ") from Available to Active",
            null).log(context);

		
	}

	/**
     * Get a list of all the service providers.
     *
     * @param context The operating context.
     * 
     * @return A list of all the service providers.
     *
     * @exception HomeException Thrown if there are problems accessing the Home
     * information in the given context.
     */
    private Collection getServiceProviders(final Context context)
        throws HomeException
    {
        final Home home = (Home)context.get(CRMSpidHome.class);

        if (home == null)
        {
            throw new HomeException("System error: no CRMSpidHome found in context.");
        }

        return home.selectAll(context);
    }

}
