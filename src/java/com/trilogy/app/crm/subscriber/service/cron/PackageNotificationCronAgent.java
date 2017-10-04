/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.service.cron;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberServiceCharger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;


/**
 * 
 * 
 * @author alpesh.champeneri@redknee.com
 */
public class PackageNotificationCronAgent implements ContextAgent
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.
     * xhome.context.Context)
     */
    @SuppressWarnings("unchecked")
    public void execute(Context ctx) throws AgentException
    {
     
        final Context subContext = ctx.createSubContext();
        this.startTime_ = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
        this.endTime_ = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).findDatesAfter(1)); 
        processPackageNotificatoin(subContext);
        
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
        
        SimpleXStatement subscriberPredicate = new SimpleXStatement("id IN (SELECT a."
                + SubscriberServicesXInfo.SUBSCRIBER_ID.getSQLName() + " FROM subscriberservices a, subscriberauxiliaryservice b WHERE a."
                + SubscriberServicesXInfo.MANDATORY.getSQLName() + "='n' " + " AND" + " (" 
                
                + " (a."
                + SubscriberServicesXInfo.START_DATE.getSQLName() + " <" + this.endTime_.getTime() + " AND a."
                + SubscriberServicesXInfo.PROVISIONED_STATE.getSQLName() + "=" + ServiceStateEnum.PROVISIONED_INDEX + " )"
                
                + " OR " + " (b."
                + SubscriberAuxiliaryServiceXInfo.START_DATE.getSQLName() + " <" + this.endTime_.getTime() + " AND b."
                + SubscriberAuxiliaryServiceXInfo.PROVISIONED.getSQLName() + "='y' )"
                
                + " )" + ")" + " AND " + SubscriberXInfo.STATE.getSQLName()
                + " = " + SubscriberStateEnum.ACTIVE_INDEX);
             
        return subscriberPredicate;
    }
    
    /**
     * Find all the subscribers service and process each of them.
     *
     * @param ctx
     *            The operating context.
     */
    private void processPackageNotificatoin(final Context ctx)
    {
        final Home h = (Home) ctx.get(SubscriberHome.class);

       
        LogSupport.info(ctx, this, " Service Expiry/Recurrence Agent: BEGINS startDate:"+this.startTime_+" endDate:"+this.endTime_);
       

        LogSupport.info(ctx, this, "Predicate Query :"+ getPredicate(ctx));
        
        try
        {
            h.where(ctx, getPredicate(ctx)).forEach(
                new PackageNotificationVisitor(this.startTime_, this.endTime_));

        }
        catch (final Exception e)
        {
            LogSupport.minor(ctx, PackageNotificationCronAgent.class, "Error getting services for subscriber ", e);
        }
        LogSupport.info(ctx, this, " Service Expiry/Recurrence Monitoring Agent: ENDS");
    }
    public PackageNotificationCronAgent(final Date startTime, final Date endTime)
    {
        this.startTime_ = startTime;
        this.endTime_ = endTime;
    }
    
    public PackageNotificationCronAgent()
    {
    }
    static final class PackageNotificationVisitor implements Visitor
    {

        
        /**
         * Create a new instance of <code>PackageNotificationVisitor</code>.
         *
         * @param startDate
         *            Date to use as start date.
         * @param endDate
         *            Date to use as end date.
         */
        public PackageNotificationVisitor(final Date startDate, final Date endDate)
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
            handlePackageNotification(ctx, sub);
        }


        /**
         * send notification to all services which have provisioned with expire, pre-expire and pre-recurrence today.
         *
         * @param ctx
         *            The operating context.
         * @param sub
         *            The subscriber being provisioned.
         */
        private void handlePackageNotification(final Context ctx, final Subscriber sub)
        {
            try
            {
                String currency = Currency.DEFAULT.getName();
                try
                {
                    CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
                    
                    if (!spid.isEnablePackageNotification())
                    {
                        return;
                    }
                    currency = spid.getCurrency();
                }
                catch (Throwable t)
                {
                    LogSupport.minor(ctx,  this, "Unable to retrieve SPID", t);
                }
                
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this,
                            "Begin handlePackageNotification. Working on subscriber : " + sub.getId());
                }
                Date currentDate = new Date();
                if (currentDate.after(sub.getStartDate()) && (!sub.getState().equals(SubscriberStateEnum.PENDING)))
                {
                    final Collection<SubscriberServices> services = getServicesStartingTodayOrBefore(ctx, sub.getId());
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "services: " + services);
                    }
                    if(services != null)
                    {
                        sendServiceNotification(ctx, sub, services,currency);
                    }
                    
                    final Collection<SubscriberAuxiliaryService> auxServices = getAuxServicesStartingTodayOrBefore(ctx, sub.getId());
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Auxiliary services: " + auxServices);
                    }
                    if(auxServices != null)
                    {
                        sendAuxServiceNotification(ctx,sub, auxServices,currency);
                    }
                    
                }
            }
            catch (final Exception e)
            {
                LogSupport.minor(ctx, this, "Couldn't send notification to services for subscriber " + sub.getId(), e);
            }
        }


       
        /**
         * Returns the set of service IDs from a collection of subscriber services.
         *
         * @param services
         *            A collection of subscriber services.
         * @return A set of service IDs corresponding to the the services.
         */
        private Set<Long> getServiceIdList(final Collection<SubscriberServices> services)
        {
            final Set<Long> serviceIds = new HashSet<Long>();
            for (final SubscriberServices subscriberServices : services)
            {
                serviceIds.add(Long.valueOf(subscriberServices.getServiceId()));
            }
            return serviceIds;
        }

        /**
         * Returns the set of service IDs from a collection of subscriber services.
         *
         * @param services
         *            A collection of subscriber services.
         * @return A set of service IDs corresponding to the the services.
         */
        private Set<Long> getAuxServiceIdList(final Collection<SubscriberAuxiliaryService> services)
        {
            final Set<Long> serviceIds = new HashSet<Long>();
            for (final SubscriberAuxiliaryService subscriberAuxiliaryService : services)
            {
                serviceIds.add(Long.valueOf(subscriberAuxiliaryService.getAuxiliaryServiceIdentifier()));
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
                filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PROVISIONED));
                filter.add(new LT(SubscriberServicesXInfo.START_DATE, this.endDate_));
                
                result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, filter);
            }
            catch (final Exception exception)
            {
                LogSupport.minor(ctx, PackageNotificationCronAgent.class,
                    "Error getting services to be started for subscriber " + subId, exception);
            }
            return result;
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
        private Collection<SubscriberAuxiliaryService> getAuxServicesStartingTodayOrBefore(final Context ctx, final String subId)
        {
            Collection<SubscriberAuxiliaryService> result = new ArrayList<SubscriberAuxiliaryService>();
            try
            {
                final And filter = new And();
                filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subId));
                filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.PROVISIONED, Boolean.TRUE));
                filter.add(new LT(SubscriberAuxiliaryServiceXInfo.START_DATE, this.endDate_));
                
                result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberAuxiliaryService.class, filter);
            }
            catch (final Exception exception)
            {
                LogSupport.minor(ctx, PackageNotificationCronAgent.class,
                    "Error getting services to be started for subscriber " + subId, exception);
            }
            return result;
        }
        
        private Map getServices(final Context ctx, Collection<Service> service)
        {
            Map<Long,Service> map = new HashMap<Long,Service>();
            if( service != null)
            {
                for(Service s : service)
                {
                    map.put(s.getID(), s);
                }
            }
            return map;
        }
        private Map getAuxServices(final Context ctx, Collection<AuxiliaryService> service)
        {
            Map<Long,AuxiliaryService> map = new HashMap<Long,AuxiliaryService>();
            if( service != null)
            {
                for(AuxiliaryService s : service)
                {
                    map.put(s.getID(), s);
                }
            }
            return map;
        }
        
        /**
         * send service notification
         *
         * @param ctx
         *            The operating context.
         * @param subId
         *            Subscriber ID.
         * @return Set of services to be ended.
         */
        private void sendServiceNotification(final Context ctx, Subscriber subscriber, final Collection<SubscriberServices> services, String currency)
        {
            try
            {
                
                final And and = new And();
                and.add(new In(ServiceXInfo.ID, getServiceIdList(services)));
                
                Collection<Service> serviceResult = new ArrayList<Service>();
                serviceResult = HomeSupportHelper.get(ctx).getBeans(ctx, com.redknee.app.crm.bean.Service.class, and);
                Map<Long,Service> map = getServices(ctx, serviceResult);
               
                
                for(SubscriberServices subService : services)
                {
                    Service service = map.get(subService.getServiceId());
                    Date endDate = subService.getEndDate();
                    
                    int preExpiryOrRecurrence = service.getPreExpiryOrRecurrence();
                    Date nextRecurringDate = subService.getNextRecurringChargeDate();
                    
                    if(isSendNotification(ctx, this.startDate_, endDate, 0))
                    {
                        /*
                         * Send Notification
                         */
                        SubscriptionNotificationSupport.sendServiceExpiryNotification(ctx, subscriber, service, null, endDate);
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Expiry Service Notification sent successfully.");
                        }
                    }
                    else if(isSendNotification(ctx, this.startDate_, nextRecurringDate, preExpiryOrRecurrence))
                    {
                        /*
                         * Send Notification
                         */
                        Map<Long, ServiceFee2> serviceFees = SubscriberServicesSupport.getServiceFeesWithSource(ctx, subscriber);
                        ServiceFee2 servicefee = serviceFees.get(service.getID());
                        
                        String fee = CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,
                                currency, servicefee.getFee());;
                                
                        SubscriptionNotificationSupport.sendRecurrenceServiceNotification(ctx, subscriber, service, null, nextRecurringDate,fee);
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Pre-Recurrence Service Notification sent successfully.");
                        }
                    }
                    else if(isSendNotification(ctx, this.startDate_, endDate, preExpiryOrRecurrence))
                    {
                        /*
                         * Send Notification
                         */
                        SubscriptionNotificationSupport.sendServiceExpiryNotification(ctx, subscriber, service, null, endDate);
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Pre-Expiry Service Notification sent successfully.");
                        }
                    }
                }
                
            }
            catch (final Exception exception)
            {
                LogSupport.minor(ctx, PackageNotificationCronAgent.class,
                    "Error getting on sendServiceNotification.", exception);
            }
            
        }
        
        /**
         * send service notification
         *
         * @param ctx
         *            The operating context.
         * @param subId
         *            Subscriber ID.
         * @return Set of services to be ended.
         */
        private void sendAuxServiceNotification(final Context ctx, Subscriber subscriber, final Collection<SubscriberAuxiliaryService> auxServices, String currency)
        {
            try
            {
                
                final And and = new And();
                and.add(new In(AuxiliaryServiceXInfo.IDENTIFIER, getAuxServiceIdList(auxServices)));
                
                Collection<AuxiliaryService> serviceResult = new ArrayList<AuxiliaryService>();
                serviceResult = HomeSupportHelper.get(ctx).getBeans(ctx, com.redknee.app.crm.bean.AuxiliaryService.class, and);
                Map<Long,AuxiliaryService> map = getAuxServices(ctx, serviceResult);
               
                
                for(SubscriberAuxiliaryService subAuxService : auxServices)
                {
                    AuxiliaryService auxService = map.get(subAuxService.getAuxiliaryServiceIdentifier());
                    Date endDate = subAuxService.getEndDate();
                    
                    int preExpiryOrRecurrence = auxService.getPreExpiryOrRecurrence();
                    Date nextRecurringDate = subAuxService.getNextRecurringChargeDate();
                    
                    if(isSendNotification(ctx, this.startDate_, endDate, 0))
                    {
                        /*
                         * Send Expiry Notification
                         */
                        SubscriptionNotificationSupport.sendServiceExpiryNotification(ctx, subscriber, null, auxService, endDate);
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Expiry Service Notification sent successfully.");
                        }
                    }
                    else if(isSendNotification(ctx, this.startDate_, nextRecurringDate, preExpiryOrRecurrence))
                    {
                        /*
                         * Send Pre-Recurrence Notification
                         */
                        
                        String fee = CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,
                                currency, auxService.getCharge());
                                
                        SubscriptionNotificationSupport.sendRecurrenceServiceNotification(ctx, subscriber, null, auxService, endDate,fee);
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Pre-Recurrence Service Notification sent successfully.");
                        }
                    }
                    else if(isSendNotification(ctx, this.startDate_, endDate, preExpiryOrRecurrence))
                    {
                        /*
                         * Send Pre-Exipiry Notification
                         */
                        SubscriptionNotificationSupport.sendServiceExpiryNotification(ctx, subscriber, null, auxService, endDate);
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Pre-Expiry Service Notification sent successfully.");
                        }
                    }
                }
            }
            catch (final Exception exception)
            {
                LogSupport.minor(ctx, PackageNotificationCronAgent.class,
                    "Error getting sendAuxServiceNotification.", exception);
            }
            
        }
        
        private boolean isSendNotification(Context ctx, Date currentDate, Date endDate, int days)
        {
            if(currentDate != null && endDate != null)
            {
                return (currentDate.getTime() == CalendarSupportHelper.get(ctx).findDateDaysBefore(days, endDate).getTime());
            }
            return false;
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
     * Date to use as start time.
     */
    private Date startTime_;
    /**
     * Date to use as end time.
     */
    private Date endTime_;
}
