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

package com.trilogy.app.crm.deposit;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.ParallelVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bas.directDebit.EnhancedParallVisitor;
import com.trilogy.app.crm.bean.AutoDepositReleaseConfigurationEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ReleaseScheduleConfigurationEnum;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.DepositSupport;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Automatically releases deposits of eligible subscribers of a service provider.
 *
 * @author cindy.wong@redknee.com
 */
public class AutoDepositRelease implements ContextAgent
{

    /**
     * Number of threads to run auto deposit release with.
     */
    public static final int NUMBER_OF_THREADS = 30;

    /**
     * Creates a new <code>AutoDepositRelease</code> object with custom <code>AccountReleaseVisitor</code>,
     * <code>SubscriberReleaseVisitor</code>, and <code>DepositReleaseTransactionCreator</code>.
     *
     * @param serviceProvider
     *            The service provider to operate on.
     * @param activeDate
     *            The date to act upon.
     * @param subscriberVisitorPrototype
     *            Custom prototype of <code>SubscriberReleaseVisitor</code>.
     * @param transactionCreator
     *            Custom creator of deposit release transaction.
     */
    public AutoDepositRelease(final CRMSpid serviceProvider, final Date activeDate,
        final SubscriberReleaseVisitor subscriberVisitorPrototype,
        final DepositReleaseTransactionCreator transactionCreator)
    {
        serviceProvider_ = serviceProvider;
        transactionCreator_ = transactionCreator;
        activeDate_.setTime(activeDate);
        subscriberVisitorPrototype_ = subscriberVisitorPrototype;
        /*
         * TT 6121842829
         * Disable parallel processing for now, since ParallelVisitor is actually not serializable, despite
         * Visitor interface extending Serializable. This causes NotSerializableException when running
         * auto deposit release on E-Care node. Running parallel on BAS node is fine, because the visitor does not
         * need to be serialized.
         */
        enableParallel_ = false;
    }

    /**
     * Creates a new <code>AutoDepositRelease</code> object with custom <code>AccountReleaseVisitor</code>,
     * <code>SubscriberReleaseVisitor</code>, and <code>DepositReleaseTransactionCreator</code>.
     *
     * @param serviceProvider
     *            The service provider to operate on.
     * @param activeDate
     *            The date to act upon.
     * @param subscriberVisitorPrototype
     *            Custom prototype of <code>SubscriberReleaseVisitor</code>.
     * @param transactionCreator
     *            Custom creator of deposit release transaction.
     * @param enableParallel
     *            Whether parallel processing should be enabled.
     */
    public AutoDepositRelease(final CRMSpid serviceProvider, final Date activeDate,
        final SubscriberReleaseVisitor subscriberVisitorPrototype,
        final DepositReleaseTransactionCreator transactionCreator, final boolean enableParallel)
    {
        serviceProvider_ = serviceProvider;
        transactionCreator_ = transactionCreator;
        activeDate_.setTime(activeDate);
        subscriberVisitorPrototype_ = subscriberVisitorPrototype;
        enableParallel_ = enableParallel;
    }

    /**
     * Determines if this is the deposit release day, and, if so, processes the automatic deposit release.
     *
     * @param context
     *            The operating context.
     * @throws AgentException
     *             Thrown if there are errors processing the automatic deposit release.
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public final void execute(final Context context) throws AgentException
    {
        // step 1: determine if auto deposit release is enabled
        if (DepositSupport.isAutoDepositReleaseEnabled(context, serviceProvider_)
            && serviceProvider_.getUseAutoDepositRelease().equals(AutoDepositReleaseConfigurationEnum.YES))
        {
            final AutoDepositReleaseCriteria criteria;
            try
            {
                criteria = DepositSupport.getServiceProviderCriteria(context, serviceProvider_);
            }
            catch (HomeException exception)
            {
                throw new AgentException("Cannot retrieve criteria", exception);
            }

            boolean runDepositRelease = false;
            if (criteria.getReleaseScheduleConfiguration() == ReleaseScheduleConfigurationEnum.DAY_OF_MONTH
                && activeDate_.get(Calendar.DAY_OF_MONTH) == criteria.getReleaseSchedule())
            {
                runDepositRelease = true;
            }
            else if (criteria.getReleaseScheduleConfiguration()
                == ReleaseScheduleConfigurationEnum.DAYS_BEFORE_BILL_CYCLE)
            {
                runDepositRelease = true;
            }

            if (runDepositRelease)
            {
                // step 2: filter subscriber home based on SPID, deposit, and nextDepositReleaseDate
                final And and = new And();
                and.add(new EQ(SubscriberXInfo.SPID, Integer.valueOf(serviceProvider_.getSpid())));
                and.add(new LTE(SubscriberXInfo.NEXT_DEPOSIT_RELEASE_DATE, activeDate_.getTime()));
                and.add(new GT(SubscriberXInfo.DEPOSIT, Long.valueOf(0L)));

                // step 3: run on all subscribers satisfying the query
                subscriberVisitor_ = subscriberVisitorPrototype_.prototype();
                ((SubscriberReleaseVisitor) subscriberVisitor_).initalize(context, criteria,
                    serviceProvider_.getSpid(), transactionCreator_, activeDate_);
                try
                {
                    final Home whereHome = ((Home) context.get(SubscriberHome.class)).where(context, and);
                    if (enableParallel_)
                    {
                        subscriberVisitor_ = new EnhancedParallVisitor(NUMBER_OF_THREADS, subscriberVisitor_);
                    }
                    whereHome.forEach(subscriberVisitor_);
                }
                catch (HomeException exception)
                {
                    final AgentException ae = new AgentException("Account visit failed", exception);
                    new DebugLogMsg(this, ae.getMessage(), ae).log(context);
                    throw ae;
                }
                finally
                {
   	        	 try
	             {
   	        		 if(enableParallel_){
   	   	        		EnhancedParallVisitor  pv = (EnhancedParallVisitor)subscriberVisitor_;
   	   	        		pv.shutdown(EnhancedParallVisitor.TIME_OUT_FOR_SHUTTING_DOWN);

   	        		 }
	             }
	             catch (final Exception e)
	             {
	                 LogSupport.major(context, this, "Exception caught during wait for completion of all Auto Deposit Release Threads", e);
	             }
                }
            }
        }
    }

    /**
     * Returns number of subscribers visited. This value is for development/debugging purposes only -- it <em>will</em>
     * be wrong when executed on E-Care. Use the OM for actual accounting purposes.
     *
     * @return Number of subscribers visited.
     */
    public final synchronized int getNumVisitedSubscribers()
    {
        int count = 0;
        if (subscriberVisitor_ != null)
        {
            count = ((SubscriberReleaseVisitor) subscriberVisitor_).getNumVisits();
        }
        return count;
    }

    /**
     * Returns the subscriber visitor returned by the home.
     *
     * @return The subscriber visitor returned by the home.
     */
    public final Visitor getSubscriberVisitor()
    {
        return subscriberVisitor_;
    }

    /**
     * The service provider to operate on.
     */
    private final CRMSpid serviceProvider_;

    /**
     * The date to act upon. This allows users to run automatic deposit release retroactively or as in a future date.
     */
    private final Calendar activeDate_ = Calendar.getInstance();

    /**
     * Prototype of subscriber release visitor.
     */
    private final SubscriberReleaseVisitor subscriberVisitorPrototype_;

    /**
     * Creator of deposit release transactions.
     */
    private final DepositReleaseTransactionCreator transactionCreator_;

    /**
     * Actual subscriber release visitor used.
     */
    private Visitor subscriberVisitor_;

    /**
     * Whether parallel processing should be enabled.
     */
    private final boolean enableParallel_;

}
