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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.AgentHelper;
import com.trilogy.framework.core.cron.SchedulerConfigException;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.core.cron.TaskStatusEnum;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.SetBuildingVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Activate and deactivate price plan services based on start date and end date.
 * This is a work around to support variable service intervals by continuously renewing
 * one time services with a fixed interval.
 * 
 * The major difference between this cron agent and the original one is
 * after deactivating the expiry services with some specific IDs, re-associate the same 
 * service to the subscriber.
 *
 * @author rchen
 * 
 */
public class PricePlanServiceMonitoringAgentWithVariableServiceIntervalSupport implements ContextAgent
{
    private static final String COMMENT_SIGN = "#";
    private static final String DEFAULT_DELIM = ",";
    private static final String SERVICE_ID_FILE_NAME = "ServiceIdsToRenew.csv";
    private static final String PRICE_PLAN_SERVICE_MONITORING_NAME = "Services monitoring process with variable service interval support";
    private static final String PRICE_PLAN_SERVICE_MONITORING_DESCRIPTION = "Provisioned or unprovisioned services based on the start and end date - with variable service interval support";

    public static void install(Context ctx)
    {
        final AgentEntry entry = new AgentEntry();
        entry.setName(PRICE_PLAN_SERVICE_MONITORING_NAME);
        entry.setAgent(new PricePlanServiceMonitoringAgentWithVariableServiceIntervalSupport());
        entry.setContext(ctx);

        try
        {
            AgentHelper.add(ctx, entry);
        }
        catch (SchedulerConfigException e)
        {
            e.printStackTrace();
            new MajorLogMsg(PricePlanServiceMonitoringAgentWithVariableServiceIntervalSupport.class.getName(), e.getMessage(), e).log(ctx);
        }

        final TaskEntry task = new TaskEntry();
        AgentHelper.makeAgentEntryConfig(task, PRICE_PLAN_SERVICE_MONITORING_NAME);
        task.setName(PRICE_PLAN_SERVICE_MONITORING_NAME);
        task.setCronEntry("midnight");
        task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
        task.setStatus(TaskStatusEnum.AVAILABLE);
        task.setDescription(PRICE_PLAN_SERVICE_MONITORING_DESCRIPTION);

        task.setHelp(PRICE_PLAN_SERVICE_MONITORING_DESCRIPTION);

        if (TaskHelper.retrieve(ctx, task.getName()) == null)
        {
            TaskHelper.add(ctx, task);
        }
    }
    
    private static ArrayList<Long> serviceIdsToRenew = new ArrayList<Long>(0);


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
                LogSupport.debug(ctx, this, "Begin handleProvisioningAndCharging. Working on subscriber : " + sub.getId());

                final Collection<SubscriberServices> servicesToStart = getServicesStartingTodayOrBefore(ctx, sub.getId());
                final Collection<SubscriberServices> servicesToStop = getServicesEndingTodayOrBefore(ctx, sub.getId());

                if ( LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "servicesToStart " + servicesToStart);
                    LogSupport.debug(ctx, this, "servicesToStop " + servicesToStop);
                }
                
                handleProvisioning(ctx, sub, servicesToStart);
                handleUnProvisioning(ctx, sub, servicesToStop);

                final Map<ServiceFee2ID, SubscriberServices> provisionedServices = getProvSuccessServices(ctx, sub, servicesToStart);
                final Map<ServiceFee2ID, SubscriberServices> unprovisionedServices = getUnProvSuccessServices(ctx, sub, servicesToStop);

                Collection<SubscriberServices> provisionedServicesSet = provisionedServices.values();
                Collection<SubscriberServices> unprovisionedServicesSet = unprovisionedServices.values();
                if ( LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "provisionedServices " + provisionedServicesSet);
                    LogSupport.debug(ctx, this, "unprovisionedServices " + unprovisionedServicesSet);
                }
                
                handleCharging(ctx, sub, provisionedServices);
                handleRefunds(ctx, sub, unprovisionedServices);

                updateServicesState(ctx, provisionedServicesSet, ServiceStateEnum.PROVISIONED, sub);
                updateServicesState(ctx, unprovisionedServicesSet, ServiceStateEnum.UNPROVISIONED, sub);


                /*
                 * TT 7081600017: subscriber should be updated in home to store changes in
                 * provisionedServices.
                 */
                final Home home = (Home) ctx.get(SubscriberHome.class);
//                // TT9100559036: update the subscriber with the provisioned services before updating
//                for (SubscriberServices ss : provisionedServicesSet)
//                {
//                    Set<Long> services = sub.getServices();
//                    long serviceId = ss.getServiceId();
//                    services.add(serviceId);
//                }
//                home.store(sub);
                
                // Don't renew the one-time service for suspended subs, leave it to CSR.
                if (sub.getState()!=SubscriberStateEnum.SUSPENDED)
                {
                    renewRecurringService(ctx, sub, unprovisionedServicesSet, home);
                }
            }
            catch (final Exception e)
            {
                LogSupport
                    .minor(ctx, this, "Couldn't provision/unprovision services for subscriber " + sub.getId(), e);
            }
        }

        @SuppressWarnings("unchecked")
        private void renewRecurringService(final Context ctx, final Subscriber sub,
                final Collection<SubscriberServices> unprovisionedServices, final Home home) throws HomeException
        {
            Set<SubscriberServices> renewServices = lookForServicesToRenew(unprovisionedServices);
            if (renewServices!=null && renewServices.size()>0)
            {
                // to simulate GUI input
                Map svcMap = SubscriberServicesSupport.getSubscribersServices(ctx, sub.getId());
                Set<SubscriberServices> svcDisplay = new HashSet<SubscriberServices>(svcMap.values());
                
                for (SubscriberServices subSvc : renewServices)
                {
                    long serviceId = subSvc.getServiceId();
                    Set<ServiceFee2ID> services = sub.getServices();
                    services.remove(serviceId);
                    
                    svcDisplay.remove(subSvc);
                    sub.setIntentToProvisionServices(svcDisplay);
                }
                
                home.store(sub);
                
               
                // This is a hack-around to handle the case that the end date of previous service is not current date
                // This may occur if the cron task is not running in time. If the previouse service end date is used
                // the renewed service will not be charged through subscriber home pipe line, because the start date is not today
                Map<Long, Date> actualStartDates = new HashMap<Long, Date>();
                
                for (SubscriberServices subSvc : renewServices)
                {
                    Date previousEnd = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subSvc.getEndDate());
                    
                    long serviceId = subSvc.getServiceId();
                    Service service = ServiceSupport.getService(ctx, serviceId);
                    if (service.getChargeScheme()!=ServicePeriodEnum.ONE_TIME || 
                        service.getRecurrenceType()!=OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL)
                    {
                        continue;
                    }
                    
                    FixedIntervalTypeEnum intervalType = service.getFixedInterval();
                    int interval = service.getValidity();
                    
                    Calendar endDate = Calendar.getInstance();
                    endDate.setTime(previousEnd);
                    if (intervalType==FixedIntervalTypeEnum.DAYS)
                    {
                        endDate.add(Calendar.DAY_OF_YEAR, interval);
                    }
                    else if (intervalType==FixedIntervalTypeEnum.MONTHS)
                    {
                        endDate.add(Calendar.MONTH, interval);
                    }
                    else
                    {
                        throw new UnsupportedOperationException("Interval type can only be DAYS or MONTHS.");
                    }
                    
                    Date currentDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
                    if (!currentDate.equals(previousEnd))
                    {
                        actualStartDates.put(serviceId, previousEnd);
                    }
                    subSvc.setStartDate(currentDate);   // must use current date other than previous end date to get charged
                    subSvc.setEndDate(endDate.getTime());
                    //subSvc.setState(com.redknee.app.crm.bean.ServiceStateEnum.ACTIVE);
                    subSvc.setProvisionedState(ServiceStateEnum.PROVISIONED);
                    subSvc.setServicePeriod(ServicePeriodEnum.ONE_TIME);
                    
                    Set<ServiceFee2ID> services = sub.getServices();
                    services.add(new ServiceFee2ID(serviceId,SubscriberServicesUtil.DEFAULT_PATH));
                    svcDisplay.add(subSvc);
                    sub.setIntentToProvisionServices(svcDisplay);
                    
                    //proratedRefund4NonChargePeriod(ctx, sub, service, today);
                }
                
                home.store(sub);
                
                // Have to refund after charging, because if the refund goes first it will
                // prevent the charging in the sub pipeline where duplicate txn is checked.
                // Better limit the code change in this cron job, as it's customer specific workaround
                for (SubscriberServices subSvc : renewServices)
                {
                    long serviceId = subSvc.getServiceId();
                    Service service = ServiceSupport.getService(ctx, serviceId);
                    
                    Date actualStart = actualStartDates.get(serviceId);
                    if (actualStart!=null)
                    {
                        subSvc.setStartDate(actualStart);
                        updateSubscriberServices(ctx, sub.getId(), serviceId, actualStart);
                    }
                    
                    if (service.getChargeScheme()!=ServicePeriodEnum.ONE_TIME || 
                        service.getRecurrenceType()!=OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL)
                    {
                        continue;
                    }

                    proratedRefund4NonChargePeriod(ctx, sub, service, subSvc);
                }
            }
        }


        private void updateSubscriberServices(Context ctx, String subId, long serviceId, Date actualStart)
        {
            XDB xdb = (XDB) ctx.get(XDB.class);
            StringBuilder sb = new StringBuilder("update subscriberservices set startdate=");
            sb.append(actualStart.getTime());
            sb.append(" where subscriberid='");
            sb.append(subId);
            sb.append("' and serviceid=");
            sb.append(serviceId);
            try
            {
                xdb.execute(ctx, sb.toString());
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Failed to update the start date. Sub ID "+subId);
            }
        }


        @SuppressWarnings("serial")
        private void proratedRefund4NonChargePeriod(Context ctx, Subscriber sub, Service service, SubscriberServices subSvc) throws HomeException
        {
            FixedIntervalTypeEnum intervalType = service.getFixedInterval();
            int interval = service.getValidity();
            
            final Date endDate = subSvc.getStartDate();
            Calendar start = Calendar.getInstance();
            start.setTime(endDate);
            if (intervalType==FixedIntervalTypeEnum.DAYS)
            {
                start.add(Calendar.DAY_OF_YEAR, -interval);
            }
            else if (intervalType==FixedIntervalTypeEnum.MONTHS)
            {
                start.add(Calendar.MONTH, -interval);
            }
            else
            {
                throw new UnsupportedOperationException("Interval type can only be DAYS or MONTHS.");
            }
            final Date startDate = start.getTime();
            
            XDB xdb = (XDB) ctx.get(XDB.class);
            StringBuilder sqlBld = new StringBuilder("select created, note from subscribernote where ididentifier='");
            sqlBld.append(sub.getId());
            sqlBld.append("' and created between ");
            sqlBld.append(startDate.getTime());
            sqlBld.append(" and ");
            sqlBld.append(endDate.getTime());
            sqlBld.append(" order by created");
            
            final NonChargeStateHolder stateHolder = new NonChargeStateHolder();
            
            Visitor nonChargePeriodCalculator = new Visitor()
            {
                private SubscriberStateChangeType parseNote(String note)
                {
                    SubscriberStateChangeType ret = SubscriberStateChangeType.Other;
                    Matcher m = STATE_CHANGE_PATTERN.matcher(note);
                    if (m.find())
                    {
                        String from = m.group(1);
                        String to = m.group(2);
                        ret = determineType(from, to);
                    }
                    
                    return ret;
                }
                
                private SubscriberStateChangeType determineType(String from, String to)
                {
                    SubscriberStateChangeType ret = SubscriberStateChangeType.Other;
                    
                    if ((from.equals(SubscriberStateEnum.IN_ARREARS.getDescription())||
                        from.equals(SubscriberStateEnum.IN_COLLECTION.getDescription()))&&
                        (to.equals(SubscriberStateEnum.ACTIVE.getDescription())||
                        to.equals(SubscriberStateEnum.PROMISE_TO_PAY.getDescription())))
                    {
                        ret = SubscriberStateChangeType.LeaveNonChargePeriod;
                    }
                    else if (to.equals(SubscriberStateEnum.IN_ARREARS.getDescription())||
                            to.equals(SubscriberStateEnum.IN_COLLECTION.getDescription())&&
                            !from.equals(SubscriberStateEnum.IN_ARREARS))
                    {
                        ret = SubscriberStateChangeType.EnterNonChargePeriod;
                    }
                    
                    return ret;
                }

                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                {
                    final XResultSet rs = (XResultSet) obj;
                    try
                    {
                        Date created = new Date(rs.getLong("CREATED"));
                        String note = rs.getString("NOTE");
                        
                        SubscriberStateChangeType type = parseNote(note);
                        if (type==SubscriberStateChangeType.Other)
                        {
                            return;
                        }
                        
                        if (type==SubscriberStateChangeType.LeaveNonChargePeriod)
                        {
                            if (stateHolder.isInNonChargeState)
                            {
                                if (stateHolder.nonChargeStart==null)
                                {
                                    LogSupport.minor(ctx, PricePlanServiceProvisioningVisitor.this, 
                                        "Subscriber leaving non-charge period while no start date available.");
                                    return;
                                }
                                
                                stateHolder.days += 
                                    CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(stateHolder.nonChargeStart, created);
                                stateHolder.isInNonChargeState = false;
                            }
                            else
                            {
                                stateHolder.days += 
                                    CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(startDate, created);
                            }
                        }
                        else if (type==SubscriberStateChangeType.EnterNonChargePeriod)
                        {
                            stateHolder.nonChargeStart = created;
                            stateHolder.isInNonChargeState = true;
                        }
                    }
                    catch(Exception e)
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, PricePlanServiceProvisioningVisitor.this,
                            "Failed to retrieve subscriber note from db.", e);
                        }
                    }
                }
                
            };
            
            try
            {
                xdb.forEach(ctx, nonChargePeriodCalculator, sqlBld.toString());
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, PricePlanServiceProvisioningVisitor.this, 
                    "Exception detected when traversing subscriber notes.",e);
            }

            if (sub.getState()==SubscriberStateEnum.IN_ARREARS ||
                sub.getState()==SubscriberStateEnum.IN_COLLECTION)
            {
                if (stateHolder.isInNonChargeState)
                {
                    stateHolder.days += 
                        CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(stateHolder.nonChargeStart, endDate);
                }
                else
                {
                    makeRefund(ctx, sub, service, 1.0f);
                }
            }

            if (stateHolder.days>0)
            {
                float svcPeriodInDays = CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(startDate, endDate);
                float rate = stateHolder.days/svcPeriodInDays;
                makeRefund(ctx, sub, service, rate);
            }
        }
        
        @SuppressWarnings("unchecked")
        private void makeRefund(Context ctx, Subscriber sub, Service service, float rate) throws HomeException
        {
            final Map pricePlanFees = sub.getPricePlan(ctx).getServiceFees(ctx);
            final ServiceFee2 fee = (ServiceFee2) pricePlanFees.get(service.getID());
            
            final long proratedRefund = -Math.round(rate*fee.getFee());
            final String desc = "Refund the Dunned period";
            
            try
            {
                TransactionSupport.createTransaction(ctx, sub, proratedRefund,
                    desc, service.getAdjustmentType(), service.getAdjustmentGLCode());
            }
            catch (HomeException e)
            {
                throw new HomeException("Failed to generate refund transaction for the non-charge period of sub ID "+sub.getId(), e);
            }
        }

        private static class NonChargeStateHolder
        {
            boolean isInNonChargeState = false;
            Date nonChargeStart = null;
            Date nonChargeEnd = null;
            int days = 0;
        }

        private static final Pattern STATE_CHANGE_PATTERN = Pattern.compile("\\bSubscriber State : (.+)->(.+)\\n?$");
        
        private static enum SubscriberStateChangeType{EnterNonChargePeriod, LeaveNonChargePeriod, Other}

        private Set<SubscriberServices> lookForServicesToRenew(Collection<SubscriberServices> unprovisionedServices)
        {
            Set<SubscriberServices> ret = null;
            for (SubscriberServices subSvc : unprovisionedServices)
            {
                if (needsToRenew(subSvc.getServiceId()))
                {
                    if (ret == null)
                    {
                        ret = new HashSet<SubscriberServices>();
                    }
                    ret.add(subSvc);
                }
            }
            return ret;
        }


        private boolean needsToRenew(long serviceId)
        {
            for (long renewId : serviceIdsToRenew)
            {
                if (renewId==serviceId)
                {
                    return true;
                }
            }
            return false;
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
        private Map<ServiceFee2ID, SubscriberServices> getUnProvSuccessServices(final Context ctx, final Subscriber sub,
            final Collection<SubscriberServices> services)
        {
            final Map<ServiceFee2ID, SubscriberServices> successful = new HashMap<ServiceFee2ID, SubscriberServices>();
            final Set<Long> set = sub.getProvisionedServices(ctx);
            for (final SubscriberServices subService : services)
            {
                final Long serviceID = Long.valueOf(subService.getServiceId());
                if (!set.contains(serviceID))
                {
                    // the service has got unprovisioned properly
                    successful.put(new ServiceFee2ID(subService.getServiceId(),subService.getPath()), subService);
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
        private Map<ServiceFee2ID, SubscriberServices> getProvSuccessServices(final Context ctx, final Subscriber sub,
            final Collection<SubscriberServices> services)
        {
            final Map<ServiceFee2ID, SubscriberServices> successful = new HashMap<ServiceFee2ID, SubscriberServices>();
            final Set<Long> set = sub.getProvisionedServices(ctx);
            for (final SubscriberServices subService : services)
            {
                final Long serviceID = Long.valueOf(subService.getServiceId());
                if (set.contains(serviceID))
                {
                    // the service has got provisioned properly
                    successful.put(new ServiceFee2ID(subService.getServiceId(),subService.getPath()), subService);
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
        private void handleCharging(final Context parentCtx, final Subscriber sub, 
                final Map<ServiceFee2ID, SubscriberServices> serviceToBeStarted)
        {
            Context ctx = parentCtx.createSubContext();
            ctx.put(Subscriber.class, sub);
            /*
             * This is very ugly, but this is what happens when we don't have clearly
             * defined APIs for things like charging or provisioning.
             */

            //final SubscriberProvisionChargeHome chargeHome = new SubscriberProvisionChargeHome(ctx, null);

            try
            {
                /*
                 * TODO 2007-04-16 Improve performance: use getRawPricePlanVersion() and remove
                 * non subscribed services
                 */
                final Map<ServiceFee2ID, ServiceFee2> pricePlanFees = sub.getPricePlan(ctx).getServiceFees(ctx);

                // Sort the Services Fees to process into ascending execution order (TT 8120100019)
                final Collection<ServiceFee2> serviceFeesToCharge = 
                    PricePlanSupport.getServiceByExecutionOrder(ctx, serviceToBeStarted.keySet(), pricePlanFees);
                
                final int billCycleDate = SubscriberSupport.getBillCycleDay(ctx, sub);

                for (final ServiceFee2 fee : serviceFeesToCharge)
                {
                    final Long id = Long.valueOf(fee.getServiceId());
                    final SubscriberServices subService = serviceToBeStarted.get(id);
                    final Service service = subService.getService(ctx);

                    if (fee == null)
                    {
                        throw new Exception("Service Fee doesn't exist with service id: "+id + ". Abort charging.");
                    }
                    long lfee = fee.getFee();

                    double rate = 1.0;
                    int billingCycleDay = SubscriberSupport.getBillCycleDay(ctx, sub);

                    ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(service.getChargeScheme());
                    rate = handler.calculateRate(ctx, new Date(), billingCycleDay, sub.getSpid(), sub.getId(), fee);

                    final long proratedFee = Math.round(rate * lfee);

                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Rate based on billcycle ratio " + rate);
                    }

                    /*
                     * TT#5081722827 User Group Adjustment Limits should not restrict
                     * priceplan changes.
                     */

                    // chargeHome.doAdjustmentLimitCheck(proratedFee, adjustLimit);
                    final String adjMsg = "Price Plan Change: Charge for the service " + id;

                    final boolean duplicateTrans = CoreTransactionSupportHelper.get(ctx).isDuplicateTransaction(ctx, sub.getBAN(), sub
                        .getMSISDN(), service.getAdjustmentType(), proratedFee, new Date());

                    if (!duplicateTrans)
                    {
                        LogSupport.debug(ctx, this, "Creating transaction : " + adjMsg + " for the amount of "
                            + proratedFee);
                       // chargeHome.handleServiceTransaction(ctx, service, sub, proratedFee, adjMsg, suspendOnFailure, fee, false);
                    }
                    else
                    {
                        // done for debugging
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "DUPLICATE Charge TRANSACTION BAN : " + sub.getBAN()
                                    + "MSISDN : " + sub.getId()
                                    + " SERVICE ID : " + service.getID()
                                    + " AMOUNT : " + proratedFee);
                        }
                    }
                }
            }
            catch (final Exception e)
            {
                LogSupport.minor(ctx, this, "Couldn't charge services for subscriber " + sub.getId(), e);
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
        private void handleRefunds(final Context parentCtx, final Subscriber sub, 
                final Map<ServiceFee2ID, SubscriberServices> servicesToBeStopped)
        {
            Context ctx = parentCtx.createSubContext();
            ctx.put(Subscriber.class, sub);
            
            //final SubscriberProvisionChargeHome chargeHome = new SubscriberProvisionChargeHome(ctx, null);
            try
            {
                /*
                 * TODO 2007-04-16 Improve performance: use getRawPricePlanVersion() and remove
                 * non subscribed services
                 */
                final Map<ServiceFee2ID, ServiceFee2> pricePlanFees = sub.getPricePlan(ctx).getServiceFees(ctx);

                // Sort the Services Fees to process into ascending execution order (TT 8120100019)
                final Collection<ServiceFee2> serviceFeesToRefund = 
                    PricePlanSupport.getServiceByExecutionOrder(ctx, servicesToBeStopped.keySet(), pricePlanFees);

                final int billCycleDate = SubscriberSupport.getBillCycleDay(ctx, sub);

                for (final ServiceFee2 fee : serviceFeesToRefund)
                {
                    final Long id = Long.valueOf(fee.getServiceId());
                    final SubscriberServices subService = servicesToBeStopped.get(id);
                    final Service service = subService.getService(ctx);

                    long lfee = 0;
                    if (fee != null)
                    {
                        lfee = fee.getFee();
                    }

                    double rate = 1.0;
                    Date endDate;

                    if (subService.getEndDate().before(this.startDate_))
                    {
                        endDate = subService.getEndDate();
                    }
                    else
                    {
                        endDate = new Date();
                    }

                    int billingCycleDay = SubscriberSupport.getBillCycleDay(ctx, sub);

                    ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(service.getChargeScheme());
                    rate = handler.calculateRefundRate(ctx, endDate, billingCycleDay, sub.getSpid(), sub.getId(), fee);



                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "[REFUND] Rate based on billcycle ratio " + rate);   
                    }

                    final long proratedRefund = Math.round(rate * lfee);

                    // chargeHome.doAdjustmentLimitCheck(proratedRefund, adjustLimit);
                    final String adjMsg = "Price Plan Change: Refund for the service " + id;

                    final boolean duplicateTrans = CoreTransactionSupportHelper.get(ctx).isDuplicateTransaction(ctx, sub.getBAN(), sub
                        .getMSISDN(), service.getAdjustmentType(), proratedRefund, new Date());

                    if (!duplicateTrans && fee != null && fee.getServicePeriod() != ServicePeriodEnum.ONE_TIME)
                    {
                        LogSupport.debug(ctx, this, "Creating transaction : " + adjMsg + " for the amount of "
                            + proratedRefund);
                        //chargeHome.handleServiceTransaction(ctx, service, sub, proratedRefund, adjMsg, false, fee, true);
                    }
                    else
                    {
                        // done for debugging
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "DUPLICATE Refund TRANSACTION BAN : " + sub.getBAN()
                                    + "MSISDN : " + sub.getId()
                                    + " SERVICE ID : " + service.getID()
                                    + " AMOUNT : " + proratedRefund);
                        }
                    }
                }
            }
            catch (final Exception e)
            {
                LogSupport.minor(ctx, this, "Couldn't charge services for subscriber " + sub.getId(), e);
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
        private Set<SubscriberServices> getServicesStartingTodayOrBefore(final Context ctx, final String subId)
        {
            Set<SubscriberServices> result = new HashSet<SubscriberServices>();
            final Home h = (Home) ctx.get(SubscriberServicesHome.class);
            try
            {
                final And filter = new And();
                filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId));
                filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PENDING));
                filter.add(new LT(SubscriberServicesXInfo.START_DATE, this.endDate_));
                
                result = (Set<SubscriberServices>) h.where(ctx, filter).forEach(ctx, 
                        new SetBuildingVisitor(new SubscriberServiceComparatorWithExecutionOrder(ctx)));
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
        private Set<SubscriberServices> getServicesEndingTodayOrBefore(final Context ctx, final String subId)
        {
            Set<SubscriberServices> result = new HashSet<SubscriberServices>();

            final Home h = (Home) ctx.get(SubscriberServicesHome.class);
            try
            {
                final And filter = new And();
                filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId));
                filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PROVISIONED));
                filter.add(new LT(SubscriberServicesXInfo.END_DATE, this.endDate_));
                filter.add(new EQ(SubscriberServicesXInfo.MANDATORY, Boolean.FALSE));
                
                result = (Set<SubscriberServices>) h.where(ctx, filter).forEach(ctx, 
                        new SetBuildingVisitor(new SubscriberServiceComparatorWithExecutionOrder(ctx)));  
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
     * {@inheritDoc}
     */
    public void execute(final Context ctx)
    {
        startTime_ = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
        endTime_ = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).findDatesAfter(1));

        final Context subContext = ctx.createSubContext();
        initializeServiceIdListToRenew(subContext);
        processSubscribersWithServiceChanges(subContext);
    }

    private void initializeServiceIdListToRenew(Context ctx)
    {
        serviceIdsToRenew.clear();
        
        String fileName = CoreSupport.getFile(ctx, SERVICE_ID_FILE_NAME);
        File svcIdFile = new File(fileName);
        
        if (!svcIdFile.exists())
        {
            LogSupport.major(ctx, this, "The configuration file "+SERVICE_ID_FILE_NAME + " doesn't exist.");
            return;
        }
        
        try
        {
            BufferedReader bufRd = new BufferedReader(new FileReader(svcIdFile));
            String line = null;
            
            try
            {
                while ((line=bufRd.readLine())!=null)
                {
                    if (line.startsWith(COMMENT_SIGN))
                    {
                        continue;
                    }
                    
                    StringTokenizer st = new StringTokenizer(line, DEFAULT_DELIM);
                    while (st.hasMoreTokens())
                    {
                        String nextToken = st.nextToken().trim();
                        if (nextToken.length()==0)
                        {
                            continue;
                        }
                        
                        try
                        {
                            serviceIdsToRenew.add(Long.valueOf(nextToken));
                        }
                        catch (NumberFormatException e)
                        {
                            LogSupport.minor(ctx, this, "The value " + nextToken + " in " 
                                    + SERVICE_ID_FILE_NAME + " cannot be recognized as number.", e);
                        }
                    }
                }
            }
            catch(IOException e)
            {
                LogSupport.minor(ctx, this, "Exception detected while reading from " + SERVICE_ID_FILE_NAME, e);
            }
            finally
            {
                bufRd.close();
            }
        }
        catch (FileNotFoundException e)
        {
            LogSupport.major(ctx, this, "The configuration file "+SERVICE_ID_FILE_NAME + " doesn't exist.", e);
        }
        catch (IOException e)
        {
            LogSupport.minor(ctx, this, "Exception detected while operating the config file " + SERVICE_ID_FILE_NAME, e);
        }
        
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

        LogSupport.info(ctx, this, " Price Plan Service Monitoring Agent: BEGINS");

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
        
        SimpleXStatement subscriberPredicate = new SimpleXStatement("id IN (SELECT a.subscriberId FROM subscriberservices a WHERE a.mandatory='n' " +
            " AND (" +
            " (a.startDate <" + this.endTime_.getTime() + " AND a.provisionedState =" + ServiceStateEnum.PENDING_INDEX + " )" +
            " OR " +
            " (a.endDate <" + this.endTime_.getTime() + " AND a.provisionedState =" + ServiceStateEnum.PROVISIONED_INDEX + " )" +
            " )" +
            ")" +
        " AND state <>" + SubscriberStateEnum.INACTIVE_INDEX);
        
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
