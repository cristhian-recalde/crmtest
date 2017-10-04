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
package com.trilogy.app.crm.checking;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.priceplan.PricePlanVersionUpdateVisitor;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.util.SubscriberProcessingException;
import com.trilogy.app.crm.util.SubscriberProcessingInterruptionException;

/**
 * Validates the subscriber's price plan information.
 *
 * @author larry.xia@redknee.com
 * @author gary.anderson@redknee.com
 */
public class SubscriberPricePlanCheckFixer
    extends AbstractIntegrityValidation
{
    /**
     * Creates a new SubscriberPricePlanCheckFixer.
     */
    public SubscriberPricePlanCheckFixer()
    {
        totalSubscribersProcessed_ = 0;
        totalSubscriberValidationFailures_ = 0;
        totalSubscribersChanged_ = 0;
        totalSubscriberChangeFailures_ = 0;
        totalUpdatesRequired_ = 0;
    }


    /**
     * {@inheritDoc}
     */
    public void printResults()
    {
        print("TOTAL: " + getTotalSubscribersProcessed());
        print("Failed: " + getTotalSubscriberValidationFailures());

        if (isRepairEnabled())
        {
            print("Change: " + getTotalSubscribersChanged());
            print("Failed Changes: " + getTotalSubscriberChangeFailures());
        }
        else
        {
            print("Updates required: " + getTotalUpdatesRequired());
        }
    }


    /**
     * {@inheritDoc}
     */
    public void process(final Context context, final Subscriber subscriber)
        throws SubscriberProcessingInterruptionException
    {
        incrementTotalSubscribersProcessed();
        Context subContext = context;

        print("subscriber " + subscriber.getId() + " has price plan "
                + subscriber.getPricePlan() + "-" + subscriber.getPricePlanVersion());

        final PricePlanVersion version;

        try
        {
            version =
                PricePlanSupport.getCurrentVersion(context, subscriber.getPricePlan());
        }
        catch (final HomeException exception)
        {
            incrementTotalSubscriberValidationFailures();
            throw new SubscriberProcessingException(
                "Failed to look up current price plan version for plan "
                + subscriber.getPricePlan(),
                subscriber,
                exception);
        }

        boolean fixed = false;
        
        try
        {
	        if (version.getVersion() != subscriber.getPricePlanVersion())
	        {
	            fixed = true;
	
	            print("subscriber " + subscriber.getId() + " needs to change version from " + subscriber.getPricePlanVersion() + " to " + version.getVersion());
	
                subscriber.switchPricePlan(context, version.getId(), version.getVersion());
	        }
        else
        {
            final Set set = PricePlanVersionUpdateVisitor.getUpdatedSubscriberServicesSet(
                    context, subscriber, version);
	
            fixed = !subscriber.getServices().equals(set);
	
            subscriber.setServices(set);

            final Set intentToProvisionServices = subscriber.getUpdatedSubscriberServicesForIntentToProvisionSet(context);

            final Map toHave = SubscriberBundleSupport.getSubscribedBundlesWithPointsBundles(context, subscriber);
            final Collection has = SubscriberBundleSupport.getProvisionedOnBundleManager(context, subscriber.getMSISDN(),
                    (int) subscriber.getSubscriptionType());
            if (!has.equals(toHave.keySet()))
            {
                fixed = true;

                subContext = context.createSubContext();
                subContext.put(Common.FORCE_BM_PROVISION_CALL, Boolean.TRUE);
            }
        }

	        if (fixed && isRepairEnabled())
	        {
	            try
	            {
	                final Home home = (Home)context.get(SubscriberHome.class);
                home.store(subContext, subscriber);
	
	                incrementTotalSubscribersChanged();
	            }
	            catch (final HomeException exception)
	            {
	                incrementTotalSubscriberChangeFailures();
	                new MajorLogMsg(
	                        this,
	                        "Failed to store subscriber in Home," + subscriber +".",
	                        exception).log(context);
	            }
	        }
	        else if (fixed && !isRepairEnabled())
	        {
	            incrementTotalUpdatesRequired();
	            print("Subscriber " + subscriber.getId() + " requires updates.");
	        }
        }catch(NullPointerException e){
        	
        	//if the subscriber has no price plan version, then it needs to be repaired
        	incrementTotalUpdatesRequired();
        	print("Subscriber " + subscriber.getId() + " requires a valid price plan version.");
        	
        	new MinorLogMsg
        	(SubscriberPricePlanCheckFixer.class, "price plan version value null",
        			e)
        	.log(context);
        }
    }


    /**
     * Gets the total number of subscribers processed.
     *
     * @return The total number of subscribers processed.
     */
    protected int getTotalSubscribersProcessed()
    {
        return totalSubscribersProcessed_;
    }


    /**
     * Gets the total number of failed subscriber validations.
     *
     * @return The total number of failed subscriber validations.
     */
    protected int getTotalSubscriberValidationFailures()
    {
        return totalSubscriberValidationFailures_;
    }


    /**
     * Gets the total number of subscribers successfully repaired.
     *
     * @return The total number of subscribers successfully repaired.
     */
    protected int getTotalSubscribersChanged()
    {
        return totalSubscribersChanged_;
    }


    /**
     * Gets the total number of failed attempts at making repairs.
     *
     * @return The total number of failed attempts at making repairs.
     */
    protected int getTotalSubscriberChangeFailures()
    {
        return totalSubscriberChangeFailures_;
    }


    /**
     * Gets the total number of subscribers that need to be repaired.
     *
     * @return The total number of subscribers that need to be repaired.
     */
    protected int getTotalUpdatesRequired()
    {
        return totalUpdatesRequired_;
    }


    /**
     * Increments the total number of subscribers processed.
     */
    protected void incrementTotalSubscribersProcessed()
    {
        ++totalSubscribersProcessed_;
    }


    /**
     * Increments the total number of failed subscriber validations.
     */
    protected void incrementTotalSubscriberValidationFailures()
    {
        ++totalSubscriberValidationFailures_;
    }


    /**
     * Increments the total number of subscribers successfully repaired.
     */
    protected void incrementTotalSubscribersChanged()
    {
        ++totalSubscribersChanged_;
    }


    /**
     * Increments the total number of failed attempts at making repairs.
     */
    protected void incrementTotalSubscriberChangeFailures()
    {
        ++totalSubscriberChangeFailures_;
    }


    /**
     * Increments the total number of subscribers that need to be repaired.
     */
    protected void incrementTotalUpdatesRequired()
    {
        ++totalUpdatesRequired_;
    }


    /**
     * The total number of subscribers processed.
     */
    private int totalSubscribersProcessed_;

    /**
     * The total number of failed subscriber validations.
     */
    private int totalSubscriberValidationFailures_;

    /**
     * The total number of subscribers successfully repaired.
     */
    private int totalSubscribersChanged_;

    /**
     * The total number of failed attempts at making repairs.
     */
    private int totalSubscriberChangeFailures_;

    /**
     * The total number of subscribers that need to be repaired.
     */
    private int totalUpdatesRequired_;

} // class
