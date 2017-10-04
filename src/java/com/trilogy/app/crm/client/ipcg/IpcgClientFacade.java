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
package com.trilogy.app.crm.client.ipcg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.product.s5600.ipcg.rating.provisioning.RatePlan;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcRatePlan;


/**
 * Provides a simple facade class for the IpcgClient.
 *
 * @author gary.anderson@redknee.com
 */
public
class IpcgClientFacade
    implements IpcgClient
{
    /**
     * Creates a new IpcgClientFacade.
     *
     * @param subscriberProfileDelegate The subscriber profile interface to
     * which this facade delegates.
     *
     * @param ratePlanDelegate The rate plan provisioning interface to which
     * this facade delegates.
     *
     * @param balanceResetDelegate The balance reset interface to which this
     * facade delegates.
     */
    public IpcgClientFacade(
        final Class<ProductS5600IpcgClient> subscriberProfileKey,
        final UrcsDataRatingProvClient ratePlanDelegate)
    {
        subscriberProfileKey_ = subscriberProfileKey;
        ratePlanDelegate_ = ratePlanDelegate;
    }


    /**
     * Gets the subscriber profile interface to which this facade delegates.
     *
     * @return The subscriber profile interface to which this facade delegates.
     */
    public ProductS5600IpcgClient getSubscriberProfileDelegate(Context ctx)
    {
        return UrcsClientInstall.getClient(ctx, subscriberProfileKey_);
    }


    /**
     * Gets the rate plan provisioning interface to which this facade delegates.
     *
     * @return The rate plan provisioning interface to which this facade delegates.
     */
    public UrcsDataRatingProvClient getRatePlanDelegate()
    {
        return ratePlanDelegate_;
    }


    /**
     * {@inheritDoc}
     */
    public int addSub(
        final Context context,
        final Subscriber subscriber,
        final short billingCycleDate,
        final String timeZone,
        final int ratePlan,
        final int scpId,
        final boolean subBasedRatingEnabled,
        final int serviceGrade)
        throws IpcgSubProvException
    {
        return getSubscriberProfileDelegate(context).addSub(context,
            subscriber,
            billingCycleDate,
            timeZone,
            ratePlan,
            scpId,
            subBasedRatingEnabled,
            serviceGrade);
    }


    /**
     * {@inheritDoc}
     */
    public int addChangeSub(
        final Context context,
        final Subscriber subscriber,
        final short billingCycleDate,
        final int ratePlan,
        final int serviceGrade)
        throws IpcgSubProvException
    {
        return getSubscriberProfileDelegate(context).addChangeSub(context, subscriber,billingCycleDate, ratePlan, serviceGrade);
    }


    /**
     * {@inheritDoc}
     */
    public int addChangeSubBillCycleDate(
        final Context context,
        final Subscriber subscriber,
        final short billCycleDate)
        throws IpcgSubProvException
    {
        return getSubscriberProfileDelegate(context).addChangeSubBillCycleDate(subscriber, billCycleDate);
    }


    /**
     * {@inheritDoc}
     */
    public int setSubscriberEnabled(
        final Context context,
        final Subscriber subscriber,
        final boolean enabled)
        throws IpcgSubProvException
    {
        // UMP-3348: Allow Data service for broadband and voice for wireline subscriptions
        // hack fix to support Multiplay capability, as legacy interfaces did not consider subscription type other than AIRTIME
        // msisdn|subscriptionType will be passed to URCS
		
		String paramGetSub = null;
		if(allowMultiSubPerAccount(context, subscriber))
		{
			paramGetSub = subscriber.getMsisdn()+"|"+subscriber.getSubscriptionType();
		}
		else
		{
			paramGetSub = subscriber.getMsisdn();
		}
    	
        return getSubscriberProfileDelegate(context).enableSubscriber(paramGetSub, enabled);
    }


    /**
     * {@inheritDoc}
     */
    public boolean isSubscriberProfileAvailable(
        final Context context,
        final Subscriber subscriber)
        throws IpcgSubProvException
    {
    	// UMP-3348: Allow Data service for broadband and voice for wireline subscriptions
        // hack fix to support Multiplay capability, as legacy interfaces did not consider subscription type other than AIRTIME
        // msisdn|subscriptionType will be passed to URCS
		
		String paramGetSub = null;
		if(allowMultiSubPerAccount(context, subscriber))
		{
			paramGetSub = subscriber.getMsisdn()+"|"+subscriber.getSubscriptionType();
		}
		else
		{
			paramGetSub = subscriber.getMsisdn();
		}
    	
        return getSubscriberProfileDelegate(context).getSub(paramGetSub) == 0;
    }


    /**
     * {@inheritDoc}
     */
    public int removeSubscriber(final Context context, final Subscriber subscriber)
        throws IpcgSubProvException
    {
    	// UMP-3348: Allow Data service for broadband and voice for wireline subscriptions
        // hack fix to support Multiplay capability, as legacy interfaces did not consider subscription type other than AIRTIME
        // msisdn|subscriptionType will be passed to URCS
		
		String dataUnprovParams = null;
		if(allowMultiSubPerAccount(context, subscriber))
		{
			dataUnprovParams = subscriber.getMsisdn()+"|"+subscriber.getSubscriptionType();
		}
		else
		{
			dataUnprovParams = subscriber.getMsisdn();
		}
    	
        return getSubscriberProfileDelegate(context).deleteSub(dataUnprovParams);
    }
    
    /**
	 * Multiplay capability
	 * @param context
	 * @param subscriberAccount
	 * @return
	 * @throws IpcgSubProvException
	 */
	private boolean allowMultiSubPerAccount(final Context context, final Subscriber subscriberAccount)
    throws IpcgSubProvException
	{
	    final int spid = subscriberAccount.getSpid();
	
	    final Home home = (Home)context.get(CRMSpidHome.class);
	    try
	    {
	    	final CRMSpid serviceProvider = (CRMSpid)home.find(context, Integer.valueOf(spid));
	    	if (serviceProvider == null)
		    {
		        throw new IpcgSubProvException(
		            "Failed to locate service provider profile " + spid + " for account " + subscriberAccount.getBAN());
		    }
	    	return serviceProvider.isAllowMultiSubForOneAccount();
	    }
	    catch(HomeException he)
	    {
	    	throw new IpcgSubProvException(
		            "Exception while looking for spid " + spid + " for account " + subscriberAccount.getBAN() +" "+ he.getMessage());
	    }
	}


    /**
     * {@inheritDoc}
     */
    public RatePlan[] getAllRatePlans(final Context context)
        throws IpcgRatingProvException
    {
        return getRatePlanDelegate().retrieveAllRatingPlans(context);
    }
    
    /**
     * {@inheritDoc}
     */
    public IprcRatePlan[] getAllRatePlans(final Context context, final int spid)
    	throws IpcgRatingProvException
    {
    	return getRatePlanDelegate().queryRatePlans(context, spid);
    }



    /**
     * The rate plan provisioning interface to which this facade delegates.
     */
    private final UrcsDataRatingProvClient ratePlanDelegate_;
    
    private Class<ProductS5600IpcgClient> subscriberProfileKey_;


} // class
