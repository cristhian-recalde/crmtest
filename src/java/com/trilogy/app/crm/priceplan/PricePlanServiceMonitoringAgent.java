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
package com.trilogy.app.crm.priceplan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberServiceCharger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Activate and deactivate price plan services based on start date and end date.
 *
 * @author rajith.attapattu@redknee.com
 * @author cindy.wong@redknee.com
 */
public class PricePlanServiceMonitoringAgent implements ContextAgent
{

    /**
     * Returns the subscriber ID of a {@link SubscriberServices}.
     *
     * @author cindy.wong@redknee.com
     * @since Aug 23, 2007
     */
    static final class SubscriberIdFunction implements Function
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;


        /**
         * {@inheritDoc}
         */
        public Object f(final Context ctx, final Object obj)
        {
            final SubscriberServices subService = (SubscriberServices) obj;
            return subService.getSubscriberId();
        }
    }

    /**
     * @author cindy.wong@redknee.com
     * @since Aug 23, 2007
     */
    static final class PricePlanServiceProvisioningVisitor implements Visitor
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;


        /**
         * Create a new instance of <code>PricePlanServiceProvisioningVisitor</code>.
         *
         * @param startDate
         *            Date to use as start date.
         * @param endDate
         *            Date to use as end date.
         */
        public PricePlanServiceProvisioningVisitor(final Date startDate, final Date endDate)
        {
            this.startDate_ = startDate;
            this.endDate_ = endDate;
        }


        /**
         * {@inheritDoc}
         */
        public void visit(final Context ctx, final Object obj)
        {
            final Subscriber sub = (Subscriber) obj;
            sub.setContext(ctx);
            handleProvisioningAndCharging(ctx, sub);
        }


        /**
         * Attempts to provisions and charges any price plan services going into or out of
         * active state.
         *
         * @param ctx
         *            The operating context.
         * @param sub
         *            The subscriber being provisioned.
         */
        private void handleProvisioningAndCharging(final Context ctx, final Subscriber sub)
        {
            /*
             * 2007-08-23 [Cindy]: Moved the method comment to below. Not quite sure what
             * it means now.
             */
            /*
             * I have no choice but to let it go through the pipeline The added benift is
             * it will do the charging as well.
             */

            /*
             * Need to handle the charging and provisioning outside the pipeline and also
             * update the subs-services table.
             */
            try
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this,
                            "Begin handleProvisioningAndCharging. Working on subscriber : " + sub.getId());
                }
                Date currentDate = new Date();
                if (currentDate.after(sub.getStartDate()) && (!sub.getState().equals(SubscriberStateEnum.PENDING)))
                {
                    final Collection<SubscriberServices> servicesToStart = getServicesStartingTodayOrBefore(ctx,
                            sub.getId());
                    final Collection<SubscriberServices> servicesToStop = getServicesEndingTodayOrBefore(ctx,
                            sub.getId());
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "servicesToStart " + servicesToStart);
                        LogSupport.debug(ctx, this, "servicesToStop " + servicesToStop);
                    }
                    handleProvisioning(ctx, sub, servicesToStart);
                    handleUnProvisioning(ctx, sub, servicesToStop);
                    final Map<Long, SubscriberServices> provisionedServices = getProvSuccessServices(ctx, sub,
                            servicesToStart);
                    final Map<Long, SubscriberServices> unprovisionedServices = getUnProvSuccessServices(ctx, sub,
                            servicesToStop);
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "provisionedServices " + provisionedServices.values());
                        LogSupport.debug(ctx, this, "unprovisionedServices " + unprovisionedServices.values());
                    }
                    handleCharging(ctx, sub, provisionedServices);
                    handleRefunds(ctx, sub, unprovisionedServices);
                    updateServicesState(ctx, provisionedServices.values(), ServiceStateEnum.PROVISIONED, sub);
                    updateServicesState(ctx, unprovisionedServices.values(), ServiceStateEnum.UNPROVISIONED, sub);
                }
                

            }
            catch (final Exception e)
            {
                LogSupport
                    .minor(ctx, this, "Couldn't provision/unprovision services for subscriber " + sub.getId(), e);
            }
        }


        /**
         * Returns the subset of services which were successfully unprovisioned.
         *
         * @param sub
         *            The subscriber provisioned.
         * @param services
         *            The set of services attempted for unprovisioning.
         * @return The subset of services which were successfully unprovisioned.
         */
        private Map<Long, SubscriberServices> getUnProvSuccessServices(final Context ctx, final Subscriber sub,
            final Collection<SubscriberServices> services)
        {
            final Map<Long, SubscriberServices> successful = new HashMap<Long, SubscriberServices>();
            final Set<Long> set = sub.getProvisionedServices(ctx);
            for (final SubscriberServices subService : services)
            {
                final Long serviceID = Long.valueOf(subService.getServiceId());
                if (!set.contains(serviceID))
                {
                    // the service has got unprovisioned properly
                    successful.put(serviceID, subService);
                }
            }

            return successful;
        }


        /**
         * Returns the subset of services which were successfully provisioned.
         *
         * @param sub
         *            The subscriber provisioned.
         * @param services
         *            The set of services attempted for provisioning.
         * @return The subset of services which were successfully provisioned.
         */
        private Map<Long, SubscriberServices> getProvSuccessServices(final Context ctx, final Subscriber sub,
            final Collection<SubscriberServices> services)
        {
            final Map<Long, SubscriberServices> successful = new HashMap<Long, SubscriberServices>();
            final Set<Long> set = sub.getProvisionedServices(ctx);
            for (final SubscriberServices subService : services)
            {
                final Long serviceID = Long.valueOf(subService.getServiceId());
                if (set.contains(serviceID))
                {
                    // the service has got provisioned properly
                    successful.put(serviceID, subService);
                }
            }
            return successful;
        }


        /**
         * Updates all services which were ended to {@link SubscriberServicesHome}.
         *
         * @param ctx
         *            The operating context.
         * @param services
         *            Set of services which were ended.
         * @param state
         *            State to set the services as.
         */
        private void updateServicesState(final Context ctx, final Collection<SubscriberServices> services,
                final ServiceStateEnum state, final Subscriber sub)
        {
            for (final SubscriberServices service : services)
            {
                service.setProvisionedState(state);
                try
                {
                	// Delegate to the Support methods
                    SubscriberServicesSupport.createOrModifySubcriberService(ctx, sub, service.getServiceId(), 
                            state, service.getSuspendReason(), service);
                }
                catch (final HomeException e)
                {
                	// A detail error message is provided by SubscriberServicesSupport
                    LogSupport.minor(ctx, this, e.getMessage(), e);
                }
            }
        }


        /**
         * Charges the services.
         *
         * @param ctx
         *            The operating context.
         * @param sub
         *            The subscriber to be charged.
         * @param services
         *            The services the subscriber is charged for.
         */
        private void handleCharging(final Context ctx, final Subscriber sub, 
                final Map<Long, SubscriberServices> serviceToBeStarted)
        {
        	for (SubscriberServices subService: serviceToBeStarted.values())
        	{
        		CrmCharger charger = new SubscriberServiceCharger(sub, subService);
        		// use default handler
        		charger.charge(ctx, null); 
        	}
        	
        }


        /**
         * Refunds the services.
         *
         * @param ctx
         *            The operating context.
         * @param sub
         *            The subscriber to be charged.
         * @param services
         *            The services the subscriber is charged for.
         */
        private void handleRefunds(final Context ctx, final Subscriber sub, 
                final Map<Long, SubscriberServices> servicesToBeStopped)
        {
          	for (SubscriberServices subService: servicesToBeStopped.values())
        	{
        		CrmCharger charger = new SubscriberServiceCharger(sub, subService);
        		// use default handler
        		charger.refund(ctx, null); 
        	}

         }


        /**
         * Unprovisions subscriber services.
         *
         * @param ctx
         *            The operating context.
         * @param sub
         *            The subscriber to be provisioned.
         * @param services
         *            The services to be provisioned for the subscriber.
         */
        private void handleUnProvisioning(final Context ctx, final Subscriber sub,
            final Collection<SubscriberServices> services)
        {
            final List<Long> serviceIds = getServiceIdList(services);
            try
            {
                LogSupport.info(ctx, this, " Unprovisioning services " + serviceIds.toString());
                SubscriberServicesSupport.unprovisionSubscriberServices(ctx, sub, new ArrayList<SubscriberServices>(services), null, new HashMap<ExternalAppEnum, ProvisionAgentException>());
            }
            catch (final Exception e)
            {
                LogSupport.major(ctx, this, "Exception caught when unprovisioning services " + serviceIds.toString()
                    + " for subscriber " + sub.getId(), e);
            }
        }


        /**
         * Provisions subscriber services.
         *
         * @param ctx
         *            The operating context.
         * @param sub
         *            The subscriber to be provisioned.
         * @param services
         *            The services to be provisioned for the subscriber.
         */
        private void handleProvisioning(final Context ctx, final Subscriber sub, final Collection<SubscriberServices> services)
        {
            final List<Long> serviceIds = getServiceIdList(services);

            try
            {
                LogSupport.info(ctx, this, " Provisioning services " + serviceIds.toString());
                
                SubscriberServicesSupport.provisionSubscriberServices(ctx, null, new ArrayList<SubscriberServices>(services), sub, new HashMap<ExternalAppEnum, ProvisionAgentException>());
            }
            catch (final Exception e)
            {
                LogSupport.major(ctx, this, "Exception caught when provisioning services " + serviceIds.toString()
                    + " for subscriber " + sub.getId(), null);
            }

        }


        /**
         * Returns the set of service IDs from a collection of subscriber services.
         *
         * @param services
         *            A collection of subscriber services.
         * @return A set of service IDs corresponding to the the services.
         */
        private List<Long> getServiceIdList(final Collection<SubscriberServices> services)
        {
            final List<Long> serviceIds = new ArrayList<Long>();
            for (final SubscriberServices subscriberServices : services)
            {
                serviceIds.add(Long.valueOf(subscriberServices.getServiceId()));
            }
            return serviceIds;
        }


        /**
         * Returns the services which should be started.
         *
         * @param ctx
         *            The operating context.
         * @param subId
         *            Subscriber ID.
         * @return Set of services to be started.
         */
        private Collection<SubscriberServices> getServicesStartingTodayOrBefore(final Context ctx, final String subId)
        {
            Collection<SubscriberServices> result = new ArrayList<SubscriberServices>();
            try
            {
                final And filter = new And();
                filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId));
                filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PENDING));
                filter.add(new LT(SubscriberServicesXInfo.START_DATE, this.endDate_));
                
                result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, filter);
            }
            catch (final Exception exception)
            {
                LogSupport.minor(ctx, SubscriberServicesSupport.class,
                    "Error getting services to be started for subscriber " + subId, exception);
            }
            return result;
        }


        /**
         * Returns the services which should be ended.
         *
         * @param ctx
         *            The operating context.
         * @param subId
         *            Subscriber ID.
         * @return Set of services to be ended.
         */
        private Collection<SubscriberServices> getServicesEndingTodayOrBefore(final Context ctx, final String subId)
        {
            Collection<SubscriberServices> result = new ArrayList<SubscriberServices>();
            try
            {
                final And filter = new And();
                filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId));
                filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PROVISIONED));
                filter.add(new LTE(SubscriberServicesXInfo.END_DATE, this.endDate_));
                filter.add(new EQ(SubscriberServicesXInfo.MANDATORY, Boolean.FALSE));
                
                result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, filter);  
            }
            catch (final Exception exception)
            {
                LogSupport.minor(ctx, SubscriberServicesSupport.class,
                    "Error getting services to be ended for subscriber " + subId, exception);
            }
            return result;
        }

        /**
         * Date to use as start time.
         */
        private final Date startDate_;
        /**
         * Date to use as end time.
         */
        private final Date endDate_;
    }


    /**
     * Create a new instance of <code>PricePlanServiceMonitoringAgent</code>, using
     * midnight today as start time, and midnight tomorrow as end time.
     */
     public PricePlanServiceMonitoringAgent()
     {
      
     }


    /**
     * Create a new instance of <code>PricePlanServiceMonitoringAgent</code>.
     *
     * @param startTime
     *            Date to use as start time.
     * @param endTime
     *            Date to use as end time.
     */
    public PricePlanServiceMonitoringAgent(final Date startTime, final Date endTime)
    {
        this.startTime_ = startTime;
        this.endTime_ = endTime;
    }


    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx)
    {
        final Context subContext = ctx.createSubContext();
        this.startTime_ = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
        this.endTime_ = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).findDatesAfter(1)); 
        processSubscribersWithServiceChanges(subContext);
    }


    /**
     * Find all the subscribers with service changes and process each of them.
     *
     * @param ctx
     *            The operating context.
     */
    private void processSubscribersWithServiceChanges(final Context ctx)
    {
        final Home h = (Home) ctx.get(SubscriberHome.class);

       
        LogSupport.info(ctx, this, " Price Plan Service Monitoring Agent: BEGINS startDate:"+this.startTime_+" endDate:"+this.endTime_);
       

        try
        {
            h.where(ctx, getPredicate(ctx)).forEach(
                new PricePlanServiceProvisioningVisitor(this.startTime_, this.endTime_));

        }
        catch (final Exception e)
        {
            LogSupport.minor(ctx, SubscriberServicesSupport.class, "Error getting services for subscriber ", e);
        }
        LogSupport.info(ctx, this, " Price Plan Service Monitoring Agent: ENDS");
    }


    /**
     * Returns the predicate needed to select all subscribers who have services to
     * provision/unprovision.
     *
     * @param ctx
     *            The operating context.
     * @return The predicate needed to select subscribers to be processed.
     */
    private SimpleXStatement getPredicate(final Context ctx)
    {
        /*
         * The querry will select all services that are supposed to start today (or
         * services that were supposed to start before but didn't due to the system being
         * down). It will also expire services that are supposed to expire today and also
         * expirations that were missed due to system failures. We check for the service
         * state to identify the services that were already started or expired and will
         * prevent any service from being started/expired more than once. The cutoff time
         * is end of day today. However from now on I am saving the start and end dates
         * for services with no time of day.
         */

        /*
         * 2007-08-23 [Cindy]: Refactored the following statements to use elang.
         * 2009-09-30 [Marcio]: TT#9082640018 -> We have issues with using the IN statement
         * when a huge number of Subscriber Ids is retrieved by the inner select. Changed
         * back to use XStatement while we don't have JOIN support in elang.
         */

        /*
         * String sql = "id IN (SELECT a.subscriberId FROM subscriberservices a WHERE
         * a.mandatory='n' " + " AND (" + " (a.startDate <=" + endTime + " AND a.state=" +
         * ServiceStateEnum.PENDING_INDEX + " )" + " OR " + " (a.endDate <=" + endTime + "
         * AND a.state=" + ServiceStateEnum.ACTIVE_INDEX + " )" + " )" + ")" + " AND state<>" +
         * SubscriberStateEnum.INACTIVE_INDEX;
         */

        // build the inner select
        /*
        final And subscriberServiceHomePredicate = new And();
        subscriberServiceHomePredicate.add(new EQ(SubscriberServicesXInfo.MANDATORY, Boolean.FALSE));
        final Or or = new Or();
        final And servicesToStart = new And();
        servicesToStart.add(new LTE(SubscriberServicesXInfo.START_DATE, this.endTime_));
        servicesToStart.add(new EQ(SubscriberServicesXInfo.STATE, ServiceStateEnum.PENDING));
        or.add(servicesToStart);
        final And servicesToEnd = new And();
        servicesToEnd.add(new LTE(SubscriberServicesXInfo.END_DATE, this.endTime_));
        servicesToEnd.add(new EQ(SubscriberServicesXInfo.STATE, ServiceStateEnum.PROVISIONED));
        or.add(servicesToEnd);
        subscriberServiceHomePredicate.add(or);
        final Home subscriberServicesHome = (Home) ctx.get(SubscriberServicesHome.class);
        final Select select = new Select(new SubscriberIdFunction(), subscriberServicesHome.where(ctx,
            subscriberServiceHomePredicate));
        final And subscriberPredicate = new And();
        subscriberPredicate.add(new In(SubscriberXInfo.ID, select));
        subscriberPredicate.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
        */
        
        
        SimpleXStatement subscriberPredicate = new SimpleXStatement("id IN (SELECT a."
                + SubscriberServicesXInfo.SUBSCRIBER_ID.getSQLName() + " FROM subscriberservices a WHERE a."
                + SubscriberServicesXInfo.MANDATORY.getSQLName() + "='n' " + " AND" + " (" + " (a."
                + SubscriberServicesXInfo.START_DATE.getSQLName() + " <" + this.endTime_.getTime() + " AND a."
                + SubscriberServicesXInfo.PROVISIONED_STATE.getSQLName() + "=" + ServiceStateEnum.PENDING_INDEX + " )"
                + " OR " + " (a." + SubscriberServicesXInfo.END_DATE.getSQLName() + " <" + this.endTime_.getTime()
                + " AND a." + SubscriberServicesXInfo.PROVISIONED_STATE.getSQLName() + "="
                + ServiceStateEnum.PROVISIONED_INDEX + " )" + " )" + ")" + " AND " + SubscriberXInfo.STATE.getSQLName()
                + " <> " + SubscriberStateEnum.INACTIVE_INDEX);
             
        return subscriberPredicate;
    }

    /**
     * Date to use as start time.
     */
    private Date startTime_;
    /**
     * Date to use as end time.
     */
    private Date endTime_;

}
