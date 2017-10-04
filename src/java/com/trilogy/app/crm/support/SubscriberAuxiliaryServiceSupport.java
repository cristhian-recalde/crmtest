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
package com.trilogy.app.crm.support;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bas.recharge.SuspensionSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.BirthdayPlan;
import com.trilogy.app.crm.bean.BirthdayPlanHome;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.HomezoneCount;
import com.trilogy.app.crm.bean.HomezoneCountHome;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.PersonalListPlanHome;
import com.trilogy.app.crm.bean.SctAuxiliaryService;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXDBHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.client.AppEcpClientSupport;
import com.trilogy.app.crm.client.smsb.AppSmsbClientSupport;
import com.trilogy.app.crm.config.AppHomezoneClientConfig;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.ff.PersonalListPlanSupport;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiConstants;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrPeerMsisdn;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfile;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfileHolder;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfileHolderImpl;
import com.trilogy.framework.xhome.beans.ComparableComparator;
import com.trilogy.framework.xhome.beans.ReverseComparator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Provides utility methods for use with SubscriberAuxiliaryService associations.
 *
 * @author gary.anderson@redknee.com
 */
public final class SubscriberAuxiliaryServiceSupport
{
    /**
     * Creates a new <code>SubscriberAuxiliaryServiceSupport</code> instance. This
     * method is made private to prevent instantiation of utility class.
     */
    private SubscriberAuxiliaryServiceSupport()
    {
        // empty
    }

    
    /**
     * Creates an association between the given Set of MSISDNs and the given
     * AuxiliaryService.
     *
     * @param context
     *            The operating context.
     * @param service
     *            The AuxiliaryService to associate with the subscriber.
     * @param msisdns
     *            The Collection of subscriber's MSISDN (String). Home information in the
     *            context.
     */
    public static void createAssociations(final Context context, final AuxiliaryService service,
            final String... msisdns)
    {
        createAssociations(context, service, Arrays.asList(msisdns));
    }


    /**
     * Creates an association between the given Set of MSISDNs and the given
     * AuxiliaryService.
     *
     * @param context
     *            The operating context.
     * @param service
     *            The AuxiliaryService to associate with the subscriber.
     * @param msisdns
     *            The Collection of subscriber's MSISDN (String). Home information in the
     *            context.
     */
    public static void createAssociations(final Context context, final AuxiliaryService service,
            final Collection<String> msisdns)
    {
        for (final String msisdn : msisdns)
        {
            try
            {
                createAssociation(context, msisdn, service.getIdentifier(), SECONDARY_ID_NOT_USED);
            }
            catch (final HomeException exception)
            {
                new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class.getName(),
                    "Problem encounterred while trying to create an association between MSISDN " + msisdn
                        + " and AuxiliaryService " + service.getName() + " (" + service.getIdentifier() + ").",
                    exception).log(context);
            }
        }
    }


    /**
     * Creates an association between the given MSISDN and the given AuxiliaryService.
     *
     * @param context
     *            The operating context.
     * @param msisdn
     *            The subscriber's MSISDN.
     * @param serviceIdentifier
     *            The identifier of the AuxiliaryService to associate with the subscriber.
     * @param secondaryIdentifier The unique instance id of the Aux service
     * @return An association between the given MSISDN and the given auxiliary service.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     * Home information in the context. This exception will be thrown if the 
     * msisdn cannot be found.
     */
    public static SubscriberAuxiliaryService createAssociation(final Context context, final String msisdn,
            final long serviceIdentifier, final long secondaryIdentifier) throws HomeException
    {
        final Subscriber subscriber = SubscriberSupport.lookupSubscriberForMSISDN(context, msisdn);

        if (subscriber == null)
        {
            throw new HomeException("Failed to locate subscriber for voice number " + msisdn);
        }

        return createAssociationForSubscriber(context, subscriber, serviceIdentifier, secondaryIdentifier, null);
    }


    /**
     * Creates an association between the given MSISDN and the given AuxiliaryService.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber.
     * @param serviceIdentifier
     *            The identifier of the AuxiliaryService to associate with the subscriber.
     * @param secondaryIdentifier The unique instance id of the Aux service
     * @return An association between the given MSISDN and the given auxiliary service.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     */
    public static SubscriberAuxiliaryService createAssociationForSubscriber(final Context context,
            final Subscriber subscriber, final long serviceIdentifier, final long secondaryIdentifier,
            final AuxiliaryServiceTypeEnum type) throws HomeException
    {
        final SubscriberAuxiliaryService association = populateAssociationForSubscriber(context, subscriber,
                serviceIdentifier, secondaryIdentifier, type);
        return createSubscriberAuxiliaryService(context, association);
    }


    public static SubscriberAuxiliaryService createSubscriberAuxiliaryService(final Context context,
            final SubscriberAuxiliaryService subAuxService) throws HomeException
    {
        final Home home = (Home) context.get(SubscriberAuxiliaryServiceHome.class);
        if (subAuxService != null)
        {
            final SubscriberAuxiliaryService created = (SubscriberAuxiliaryService) home.create(context, subAuxService);
            if (created != null)
            {
                created.setContext(context);
            }
            return created;
        }
        return null;
    }
    
    public static SubscriberAuxiliaryService populateAssociationForSubscriber(final Context context,
            final Subscriber subscriber, final long serviceIdentifier, final long secondaryIdentifier,
            final AuxiliaryServiceTypeEnum type) throws HomeException
    {
        if (subscriber == null)
        {
            throw new HomeException("Cannot create subscriber auxiliary association -- subscriber is null");
        }
        final SubscriberAuxiliaryService association = new SubscriberAuxiliaryService();
        association.setSubscriberIdentifier(subscriber.getId());
        association.setAuxiliaryServiceIdentifier(serviceIdentifier);
        association.setSecondaryIdentifier(secondaryIdentifier);
        association.setProvisioned(true);
        association.setContext(context);
        if (type != null)
        {
            association.setType(type);
        }

        return association;
    }
    /**
     * Creates an association between the given MSISDN and the given AuxiliaryService.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber.
     * @param serviceIdentifier
     *            The identifier of the AuxiliaryService to associate with the subscriber.
     * @param startDate
     *            The start date of the AuxSvc
     * @param endDate
     *            The end date of the AuxSvc
     * @param paymentNumber
     *            The number of payments.
     * @return An association between the given MSISDN and the given auxiliary service.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     */
    public static SubscriberAuxiliaryService createAssociation(final Context context, final Subscriber subscriber,
            final long serviceIdentifier, final Date startDate, final Date endDate, final int paymentNumber)
        throws HomeException
    {
        final SubscriberAuxiliaryService association = new SubscriberAuxiliaryService();
        association.setSubscriberIdentifier(subscriber.getId());
        association.setAuxiliaryServiceIdentifier(serviceIdentifier);
        association.setProvisioned(true);
        association.setStartDate(startDate);
        association.setEndDate(endDate);
        association.setPaymentNum(paymentNumber);
        association.setContext(context);

        final FFECareRmiService service;
        
        try
        {
            service = FFClosedUserGroupSupport.getFFRmiService(context, SubscriberAuxiliaryServiceSupport.class);
        }
        catch (FFEcareException e)
        {
            throw new HomeException(e);
        }

        if (service == null)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(SubscriberAuxiliaryServiceSupport.class.getName(),
                    "Failed to establish a connection with Friends and Family service.", new Exception()).log(context);
            }
            // exit here to prevent NPE
            return null;
        }

        final Home home = (Home) context.get(SubscriberAuxiliaryServiceHome.class);
        SubscriberAuxiliaryService subService = null;
        try
        {
            subService = (SubscriberAuxiliaryService) home.create(context, association);
            if (subService != null)
            {
                subService.setContext(context);
            }
        }
        catch (final HomeException e)
        {
            try
            {
                AuxiliaryService auxService = SubscriberAuxiliaryServiceSupport.getAuxiliaryService(context,
                        association); 
                
                long plpId = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPIDENTIFIER;
                CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(context).getExtension(context, auxService, CallingGroupAuxSvcExtension.class);
                if (callingGroupAuxSvcExtension!=null)
                {
                    plpId = callingGroupAuxSvcExtension.getCallingGroupIdentifier();
                }
                else
                {
                    LogSupport.minor(context, ClosedUserGroupSupport73.class,
                            "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + auxService.getIdentifier());
                }
                
                service.deletePLPForSub(subscriber.getMSISDN(), plpId);
                removeAssociation(context, subscriber.getMSISDN(), serviceIdentifier,
                        SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED);
            }
            catch (final Exception e1)
            {
                throw new HomeException("Failed to update subscriber's personal list plan.", e);
            }
            throw new HomeException("Failed to update subscriber's personal list plan.", e);
        }
                
        final TrPeerMsisdn[] peerMsisdn = new TrPeerMsisdn[1];                
        peerMsisdn[0] = new TrPeerMsisdn(subscriber.getMSISDN());

        final Collection<SubscriberAuxiliaryService> auxiliaryServices = new ArrayList<SubscriberAuxiliaryService>();
        if (subService != null)
        {
            auxiliaryServices.add(subService);
        }
        else
        {
            auxiliaryServices.add(association);
        }

        return subService;
    }


    /**
     * Gets the AuxiliaryService referred to by the given SubscriberAuxiliaryService.
     *
     * @param context
     *            The operating context.
     * @param association
     *            The SubscriberAuxiliaryService association.
     * @return The auxiliary service referred to by the given subscriber-auxiliary service
     *         association.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     * @deprecated Use {@link com.redknee.app.crm.bean.SubscriberAuxiliaryService.getAuxiliaryService(Context)}
     */
    @Deprecated
    public static AuxiliaryService getAuxiliaryService(final Context context,
            final SubscriberAuxiliaryService association) throws HomeException
    {
        return association.getAuxiliaryService(context);
    }


    /**
     * Creates a Collection of AuxiliaryServices for a Collection of
     * SubscriberAuxiliaryServices.
     *
     * @param context
     *            The operating context.
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     * @param chargingModeType
     *            Charging mode type of the auxiliary services to look for. Use
     *            <code>null</code> if looking up for all services.
     * @return A Collection of AuxiliaryServices.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     */
    public static Collection<AuxiliaryService> getAuxiliaryServiceCollection(final Context context,
            final Collection<SubscriberAuxiliaryService> associations, final ServicePeriodEnum chargingModeType)
        throws HomeException
    {
        if (associations != null)
        {
            final Set<Long> idSet = new HashSet<Long>();

            final Iterator<SubscriberAuxiliaryService> associationIterator = associations.iterator();
            while (associationIterator.hasNext())
            {
                final SubscriberAuxiliaryService association = associationIterator.next();
                idSet.add(Long.valueOf(association.getAuxiliaryServiceIdentifier()));
            }

            final Home home = (Home) context.get(AuxiliaryServiceHome.class);
            final And and = new And();
            and.add(new In(AuxiliaryServiceXInfo.IDENTIFIER, idSet));
            if (chargingModeType != null)
            {
                and.add(new EQ(AuxiliaryServiceXInfo.CHARGING_MODE_TYPE, chargingModeType));
            }
            return home.select(context, and);
        }
        throw new HomeException("fail to retrieve auxiliary service collection from invalid input");
    }

    /**
     * Returns all auxiliary services charged weekly.
     *
     * @param context
     *            The operating context.
     * @param associations
     *            The set of subscriber-auxiliary service associations.
     * @return The set of auxiliary services which the subscriber has subscribed and are
     *         charged weekly.
     * @throws HomeException
     *             Thrown if there are problems retrieving the weekly associations.
     */
    public static Collection<AuxiliaryService> getWeeklyAuxiliaryServiceCollection(final Context context,
            final Collection<SubscriberAuxiliaryService> associations) throws HomeException
    {
        final Set<Long> serviceIds = getSelectionSet(context, associations);

        final And filter = new And();
        filter.add(new In(AuxiliaryServiceXInfo.IDENTIFIER, serviceIds));
        filter.add(new EQ(AuxiliaryServiceXInfo.CHARGING_MODE_TYPE, ServicePeriodEnum.WEEKLY));
        
        Collection<AuxiliaryService> services = HomeSupportHelper.get(context).getBeans(context, AuxiliaryService.class, filter);

        if (services == null)
        {
            services = new ArrayList<AuxiliaryService>();
        }
        return services;
    }


    /**
     * Creates a map of AuxiliaryService identifier to AuxiliaryService for a Collection
     * of SubscriberAuxiliaryServices.
     *
     * @param context
     *            The operating context.
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     * @return A map of AuxiliaryService identifier (Long) to AuxiliaryService.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     */
    public static Map<Long, AuxiliaryService> getAuxiliaryServiceMap(final Context context,
            final Collection<SubscriberAuxiliaryService> associations) throws HomeException
    {
        final Map<Long, AuxiliaryService> serviceMap = new HashMap<Long, AuxiliaryService>();
        final Set<Long> serviceIds = getSelectionSet(context, associations);
        
        final Collection<AuxiliaryService> services = HomeSupportHelper.get(context).getBeans(
                context, 
                AuxiliaryService.class, 
                new In(AuxiliaryServiceXInfo.IDENTIFIER, serviceIds));

        for (final AuxiliaryService service : services)
        {
            serviceMap.put(Long.valueOf(service.getIdentifier()), service);
        }

        return serviceMap;
    }


    /**
     * Gets the Birthday Plan selected in the given Collection of
     * SubscriberAuxiliaryServices. Assumes that only one Birthday Plan can be selected.
     * Returns null if no Birthday Plan is subscribed
     *
     * @param context
     *            The operating context.
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     * @return The Birthday plan selected.
     * @throws HomeException
     *             Thrown if there are problems retrieving the Birthday Plan.
     */
    public static BirthdayPlan getBirthdayPlan(final Context context,
            final Collection<SubscriberAuxiliaryService> associations) throws HomeException
    {
        final Collection<AuxiliaryService> services = getAuxiliaryServiceCollection(context, associations, null);

        BirthdayPlan plan = null;
        final Home home = (Home) context.get(BirthdayPlanHome.class);

        for (final AuxiliaryService service : services)
        {
            if (service.getType() != AuxiliaryServiceTypeEnum.CallingGroup && !service.isBirthdayPlan(context))
            {
                continue;
            }

            long callingGroupIdentifier = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPIDENTIFIER;
            CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(context).getExtension(context, service, CallingGroupAuxSvcExtension.class);
            if (callingGroupAuxSvcExtension!=null)
            {
                callingGroupIdentifier = callingGroupAuxSvcExtension.getCallingGroupIdentifier();
            }
            else
            {
                LogSupport.minor(context, ClosedUserGroupSupport73.class,
                        "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + service.getIdentifier());
            }

            plan = (BirthdayPlan) home.find(context, Long.valueOf(callingGroupIdentifier));
        }

        return plan;
    }


    /**
     * Gets the PersonalListPlan selected in the given Collection of
     * SubscriberAuxiliaryServices. If more than one PLP is selected, then the PLP with
     * the greatest maximum subscriber count is returned. If more than one such PLP
     * exists, then the PLP returned is non-deterministic.
     *
     * @param context
     *            The operating context.
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     * @return The personal list plan selected.
     * @throws HomeException
     *             Thrown if there are problems retrieving the PLP.
     */
    public static PersonalListPlan getPersonalListPlan(final Context context,
            final Collection<SubscriberAuxiliaryService> associations) throws HomeException
    {
        final Collection<AuxiliaryService> services = getAuxiliaryServiceCollection(context, associations, null);

        PersonalListPlan plan = null;
        final Home home = (Home) context.get(PersonalListPlanHome.class);

        for (final Object element : services)
        {
            final AuxiliaryService service = (AuxiliaryService) element;
            if (service.getType() != AuxiliaryServiceTypeEnum.CallingGroup
                || !service.isPLP(context))
            {
                continue;
            }

            long callingGroupIdentifier = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPIDENTIFIER;
            CallingGroupTypeEnum callingGroupType = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPTYPE;
            CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(context).getExtension(context, service, CallingGroupAuxSvcExtension.class);
            if (callingGroupAuxSvcExtension!=null)
            {
                callingGroupIdentifier = callingGroupAuxSvcExtension.getCallingGroupIdentifier();
                callingGroupType = callingGroupAuxSvcExtension.getCallingGroupType();
            }
            else
            {
                LogSupport.minor(context, ClosedUserGroupSupport73.class,
                        "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + service.getIdentifier());
            }

            if (!callingGroupType.equals(CallingGroupTypeEnum.PLP))
            {
                continue;
            }
            
            final PersonalListPlan potentialPlan = (PersonalListPlan) home.find(context,
                    Long.valueOf(callingGroupIdentifier));

            // TODO 2007-04-10 why is the while cycle not interrupted? There should be only one PLP
            if (plan == null || plan.getMaxSubscriberCount() < potentialPlan.getMaxSubscriberCount())
            {
                plan = potentialPlan;
            }
        }

        return plan;
    }


    /**
     * Gets the list of personal plan MSISDNs associated with the given subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to get the PLP MSISDNs.
     * @return The set of PLP MSISDNs associated with the subscriber.
     */
    public static SortedSet<String> getPersonalListPlanMSISDNs(final Context context, final Subscriber subscriber)
    {
        final String subscriberMsisdn = subscriber.getMSISDN();

        FFECareRmiService service = null;
        try
        {
            service = FFClosedUserGroupSupport.getFFRmiService(context, SubscriberAuxiliaryServiceSupport.class);
        }
        catch (FFEcareException e)
        {
            new MinorLogMsg(PersonalListPlanSupport.class, "Error retrieving F&F RMI Client: " + e.getMessage(), e).log(context);
        }

        if (service == null)
        {
            return null;
        }

        final SortedSet<String> plpMsisdns = new TreeSet<String>();

        try
        {
            final TrSubscriberProfileHolder profileHolder = new TrSubscriberProfileHolderImpl();

            final int result = service.getSub(subscriberMsisdn, profileHolder);

            if (result == FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                final TrSubscriberProfile profile = profileHolder.getValue();

                if (profile != null)
                {
                    for (int n = 0; n < profile.otherMsisdns.length; ++n)
                    {
                        plpMsisdns.add(profile.otherMsisdns[n].msisdn);
                    }
                }
            }
            else if (result != FFECareRmiConstants.FF_ECARE_PROFILE_NOT_FOUND)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    new DebugLogMsg(SubscriberAuxiliaryServiceSupport.class.getName(),
                        "Failed to look up subscriber's personal list plan information. (msisdn=" + subscriberMsisdn
                            + ")" + " Service returned "
                            + CallingGroupSupport.getDisplayMessageForReturnCode(context, result), new Exception())
                        .log(context);
                }
            }
        }
        catch (final RemoteException exception)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Failed to look up subscriber's personal list plan information.", exception).log(context);
        }

        return plpMsisdns;
    }


    /**
     * Provides a unique, sorted set of AuxiliaryService identifiers (Long) for the given
     * Collection of SubscriberAuxiliaryServices. This is useful determining if a given
     * AuxiliaryService is represented in the given set.
     *
     * @param context
     *            The operating context.
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     * @return A unique, sorted set of AuxiliaryService identifiers (Long).
     */
    public static Set<Long> getSelectionSet(final Context context,
            final Collection<SubscriberAuxiliaryService> associations)
    {
        return getSelectionSet(context, null, associations);
    }
    
    public static Set<Long> getSelectionSet(final Context context, final AuxiliaryServiceTypeEnum type,
            final Collection<SubscriberAuxiliaryService> associations)
    {
        final Set<Long> selections = new TreeSet<Long>(new ReverseComparator(ComparableComparator.instance()));

        if (associations != null)
        {
            for (final SubscriberAuxiliaryService association : associations)
            {
                if (type == null || type.equals(association.getType(context)))
                {
                    selections.add(Long.valueOf(association.getAuxiliaryServiceIdentifier()));
                }
            }
        }
        return selections;
    }


    /**
     * Provides a unique, sorted map of AuxiliaryService identifiers (Long) to SubscriberAuxiliaryServices.
     * This is useful determining if a given AuxiliaryService is represented in the given set.
     *
     * @param context
     *            The operating context.
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     * @return A unique, sorted map of AuxiliaryService identifiers (Long) to SubscriberAuxiliaryServices.
     */
    public static Map<Long, Map<Long,SubscriberAuxiliaryService>> getSelectionMap(final Context context,
            final Collection<SubscriberAuxiliaryService> associations)
    {
        return getSelectionMap(context, null, associations);
    }

    public static Map<Long, Map<Long,SubscriberAuxiliaryService>> getSelectionMap(final Context context,
            final AuxiliaryServiceTypeEnum type, final Collection<SubscriberAuxiliaryService> associations)
    {
        final Comparator<Long> comparator = new ReverseComparator(ComparableComparator.instance());

        final Map<Long, Map<Long, SubscriberAuxiliaryService>> selections;
        selections = new TreeMap<Long, Map<Long, SubscriberAuxiliaryService>>(comparator);

        if (associations != null)
        {
            for (final SubscriberAuxiliaryService association : associations)
            {
                if (type == null || type.equals(association.getType(context)))
                {
                    Long serviceId = Long.valueOf(association.getAuxiliaryServiceIdentifier());
                    Map<Long,SubscriberAuxiliaryService> secondaryAssociation = selections.get(serviceId);
                    if (secondaryAssociation == null)
                    {
                        secondaryAssociation = new TreeMap<Long, SubscriberAuxiliaryService>(comparator);
                        selections.put(serviceId, secondaryAssociation);
                    }

                    secondaryAssociation.put(association.getSecondaryIdentifier(), association);
                }
            }
        }
        return selections;
    }


    /**
     * Gets the SubscriberAuxiliaryServices associated with the given subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to look up SubscriberAuxiliaryServices.
     * @return The SubscriberAuxiliaryServices associated with the given subscriber.
     * 
     * @deprecated Use {@link com.redknee.app.crm.bean.Subscriber.getAuxiliaryServices(Context)}
     */
    @Deprecated
    public static List<SubscriberAuxiliaryService> getSubscriberAuxiliaryServices(final Context context,
            final Subscriber subscriber)
    {
        return subscriber.getAuxiliaryServices(context);
    }

    /**
     * Gets the collection of SubscriberAuxiliaryServices associated with the given
     * subscriber and auxiliary service. Replaces
     * {@link #getSubscriberAuxiliaryServicesBySubIdAndSvcId(Context, String, long)}.
     *
     * @param context
     *            The operating context.
     * @param subscriberId
     *            The identifier of the subscriber for which to look up
     *            SubscriberAuxiliaryServices.
     * @param auxiliaryServiceId
     *            The identifier of the auxiliary service.
     * @return The SubscriberAuxiliaryServices associated with the given subscriber.
     */
    public static Collection<SubscriberAuxiliaryService> getSubscriberAuxiliaryServices(final Context context,
            final String subscriberId, final long auxiliaryServiceId)
    {
        Collection<SubscriberAuxiliaryService> auxiliaryServiceAssociations = null;

        try
        {
            final And and = new And();
            and.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subscriberId));
            and.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, Long
                .valueOf(auxiliaryServiceId)));
            auxiliaryServiceAssociations = selectSubscriberAuxiliaryServices(context, and);
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Failed to look-up AuxiliaryService associations for subscriber " + subscriberId, t).log(context);

            auxiliaryServiceAssociations = new ArrayList<SubscriberAuxiliaryService>();
        }

        return auxiliaryServiceAssociations;
    }

    /**
     * Gets the SubscriberAuxiliaryServices associated with the given secondary ID and aux
     * service ID. For now, this method should only be used for CUG
     *
     * @param context
     *            The operating context.
     * @param auxSvcIdentifier
     *            The identifier of the auxiliary service for which to look up
     *            SubscriberAuxiliaryServices.
     * @param secondaryIdentifier
     *            The secondary id for the aux service
     * @return The SubscriberAuxiliaryServices associated with the given aux service ID
     *         and secondary ID.
     * @throws HomeException
     *             Thrown by home.
     */
    public static Collection<SubscriberAuxiliaryService> getSubscriberAuxiliaryServices(final Context context,
            final long auxSvcIdentifier, final long secondaryIdentifier) throws HomeException
    {
        Collection<SubscriberAuxiliaryService> auxiliaryServiceCollection = null;

        try
        {
            final And andELang = new And();
            andELang.add(new EQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER,
                    Long.valueOf(secondaryIdentifier)));
            andELang.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER,
                    Long.valueOf(auxSvcIdentifier)));

            auxiliaryServiceCollection = selectSubscriberAuxiliaryServices(context, andELang);
        }
        catch (final HomeException t)
        {
            new MinorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Failed to look-up AuxiliaryService associations for secondaryID " + secondaryIdentifier
                    + " and auxiliary service Id " + auxSvcIdentifier, t).log(context);

            throw t;
        }

        return auxiliaryServiceCollection;
    }

    /**
     * Gets a specific SubscriberAuxiliaryService associated with the given subscriber.
     *
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     * @param serviceIdentifier
     *            The AuxiliaryService id for which to look up
     * @return The specific SubscriberAuxiliaryService associated with the given Aux.Svc
     *         identifier.
     */
    public static SubscriberAuxiliaryService getSelectedSubscriberAuxiliaryService(
            final Collection<SubscriberAuxiliaryService> associations,
            final long serviceIdentifier, final long secondaryIdentifier)
    {
        SubscriberAuxiliaryService entry = null;
        for (final SubscriberAuxiliaryService association : associations)
        {
            if (association.getAuxiliaryServiceIdentifier() == serviceIdentifier
                    && association.getSecondaryIdentifier() == secondaryIdentifier)
            {
                entry = association;
                break;
            }
        }

        return entry;
    }

    /**
     * Provides a unique, sorted set of AuxiliaryService identifiers (Long) for the given
     * Collection of SubscriberAuxiliaryServices. This is useful determining if a given
     * AuxiliaryService is represented in the given set.
     *
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     * @return A unique, sorted set of AuxiliaryService identifiers (Long).
     */
    public static SortedSet<Long> getSelectionSet(final Collection<SubscriberAuxiliaryService> associations)
    {
        final SortedSet<Long> selections = new TreeSet<Long>();

        final Iterator<SubscriberAuxiliaryService> associationIterator = associations.iterator();
        while (associationIterator.hasNext())
        {
            final SubscriberAuxiliaryService association = associationIterator.next();

            selections.add(Long.valueOf(association.getAuxiliaryServiceIdentifier()));
        }

        return selections;
    }

    /**
     * Manda - This method returns the set of Auxiliary service identifiers for the given
     * collection of SctAuxiliaryServices Collection.
     *
     * @param existingSet
     *            SortedSet object representing the collection which already have some
     *            AuxiliaryService identifiers.
     * @param sctAuxCollection
     *            Collection object representing the SctAuxiliaryService objects.
     * @return A unique, sorted set of AuxiliaryService identifiers (Long).
     */
    public static SortedSet getSelectionSetFromSat(final SortedSet existingSet, final Collection sctAuxCollection)
    {
        SortedSet existingSet_ = null;
        if (existingSet != null)
        {
            existingSet_ = existingSet;
        }
        else
        {
            existingSet_ = new TreeSet();
        }

        if (sctAuxCollection == null)
        {
            return existingSet_;
        }

        final Iterator servIter = sctAuxCollection.iterator();
        while (servIter.hasNext())
        {
            final SctAuxiliaryService association = (SctAuxiliaryService) servIter.next();

            existingSet_.add(Long.valueOf(association.getAuxiliaryServiceIdentifier()));
        }
        return existingSet_;

    }

    /**
     * Gets SubscriberAuxiliaryServices associated with the given subscriber.
     *
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     * @param serviceIdentifier
     *            The AuxiliaryService id for which to look up
     * @return The specific SubscriberAuxiliaryService associated with the given Aux.Svc
     *         identifier.
     */
    public static Collection<SubscriberAuxiliaryService> getSelectedSubscriberAuxiliaryServices(
            final Collection<SubscriberAuxiliaryService> associations, final long serviceIdentifier)
    {
        Collection<SubscriberAuxiliaryService> entries = new ArrayList<SubscriberAuxiliaryService>();
        for (SubscriberAuxiliaryService association : associations)
        {
            if (association.getAuxiliaryServiceIdentifier() == serviceIdentifier)
            {
                entries.add(association);
            }
        }
        return entries;
    }

    /**
     * Gets the SubscriberAuxiliaryServices associated with the given subscriber.
     *
     * @param context
     *            The operating context.
     * @param identifier
     *            The identifier of the subscriber for which to look up
     *            SubscriberAuxiliaryServices.
     * @return The SubscriberAuxiliaryServices associated with the given subscriber.
     */
    public static Collection<SubscriberAuxiliaryService> getSubscriberAuxiliaryServices(final Context context,
            final String identifier)
    {
        Collection<SubscriberAuxiliaryService> associations = null;

        try
        {
            associations = selectSubscriberAuxiliaryServices(context,
                    new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, identifier));
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Failed to look-up AuxiliaryService associations for subscriber " + identifier, exception).log(context);
        }
        
        if (associations == null)
        {
            associations = Collections.emptyList();
        }

        return associations;
    }
    
    
    
    public static void updateSubscriberPrivateCugAuxiliaryServices(final Context context,
            final String identifier, String newIdentifier, long secondaryIdentifier, long newSecondaryIdentifier)
    {
        Collection<SubscriberAuxiliaryService> associations = null;
        try
        {
            associations = selectSubscriberAuxiliaryServices(
                    context,
                    new And().add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, identifier)).add(
                            new EQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, secondaryIdentifier)));
            final Home home = (Home) context.get(SubscriberAuxiliaryServiceXDBHome.class);
            if (associations != null)
            {
                for (SubscriberAuxiliaryService auxService : associations)
                {
                    if (auxService.getAuxiliaryService(context).isPrivateCUG(context))
                    {
                        auxService.setSubscriberIdentifier(newIdentifier);
                        auxService.setSecondaryIdentifier(newSecondaryIdentifier);
                        home.store(auxService);
                        break;
                    }
                }
            }
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                    "Failed to update AuxiliaryService associations for subscriber " + identifier, exception)
                    .log(context);
        }
    }
    
    public static SubscriberAuxiliaryService removeSubscriberPrivateCugAuxiliaryServices(final Context context,
            final String identifier, long secondaryIdentifier)
    {
        Collection<SubscriberAuxiliaryService> associations = null;
        try
        {
            associations = selectSubscriberAuxiliaryServices(
                    context,
                    new And().add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, identifier)).add(
                            new EQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, secondaryIdentifier)));
            final Home home = (Home) context.get(SubscriberAuxiliaryServiceXDBHome.class);
            if (associations != null)
            {
                for (SubscriberAuxiliaryService auxService : associations)
                {
                    if (auxService.getAuxiliaryService(context).isPrivateCUG(context))
                    {
                        home.remove(auxService);
                        return auxService;
                    }
                }
            }
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                    "Failed to rmove AuxiliaryService associations for subscriber " + identifier, exception)
                    .log(context);
        }
        return null;
    }


    public static Collection<SubscriberAuxiliaryService> getProvisionedSubscriberAuxiliaryServices(final Context context,
            final String identifier)
    {
        Collection<SubscriberAuxiliaryService> associations = null;

        try
        {
        	And and = new And(); 
        	and.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, identifier));
        	and.add(new EQ(SubscriberAuxiliaryServiceXInfo.PROVISIONED, Boolean.TRUE)); 
        	
            associations = selectSubscriberAuxiliaryServices(context, and);
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Failed to look-up AuxiliaryService associations for subscriber " + identifier, exception).log(context);
        }
        
        if (associations == null)
        {
            associations = Collections.emptyList();
        }

        return associations;
    }
    
    
    /**
	 * Gets the SubscriberAuxiliaryServices associated with the given subscriber id and
	 * given auxiliaryservice id.
	 *
	 * @param context
	 *            The operating context.
	 * @param subIdentifier
	 *            The identifier of the subscriber for which to look up
	 * @param auxSvcIdentifier
	 *            The identifier of the auxiliary service for which to look up
	 *            SubscriberAuxiliaryServices.
	 * @return The SubscriberAuxiliaryServices associated with the given subscriber and
	 *         given id.
	 */
	public static SubscriberAuxiliaryService getSubAuxServBySubIdAuxIdAndSecondaryId(final Context context,
			final String subIdentifier, final long auxSvcIdentifier, long secondaryId)
	{
		SubscriberAuxiliaryService auxiliaryService = null;

		try
		{
			final And filter = new And();
			filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subIdentifier));
			filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, auxSvcIdentifier));
			filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, secondaryId));

			final Collection<SubscriberAuxiliaryService> associations = selectSubscriberAuxiliaryServices(context,
					filter);
			if (associations != null && !associations.isEmpty())
			{
				auxiliaryService = associations.iterator().next();
			}
		}
		catch (final HomeException exception)
		{
			new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
					"Failed to look-up AuxiliaryService associations for subscriber " + subIdentifier
					+ " and auxiliary service Id " + auxSvcIdentifier, exception).log(context);
		}

		return auxiliaryService;
	}
    
    
    
    /**
     * Gets the SubscriberAuxiliaryServices associated with the given subscriber id and
     * given auxiliaryservice id.
     *
     * @param context
     *            The operating context.
     * @param subIdentifier
     *            The identifier of the subscriber for which to look up
     * @param auxSvcIdentifier
     *            The identifier of the auxiliary service for which to look up
     *            SubscriberAuxiliaryServices.
     * @return The SubscriberAuxiliaryServices associated with the given subscriber and
     *         given id.
     */
    public static SubscriberAuxiliaryService getSubscriberAuxiliaryServicesBySubIdAndSvcId(final Context context,
            final String subIdentifier, final long auxSvcIdentifier)
    {
        SubscriberAuxiliaryService auxiliaryService = null;

        try
        {
            final And filter = new And();
            filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subIdentifier));
            filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, auxSvcIdentifier));

            final Collection<SubscriberAuxiliaryService> associations = selectSubscriberAuxiliaryServices(context,
                    filter);
            if (associations != null && !associations.isEmpty())
            {
                auxiliaryService = associations.iterator().next();
            }
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Failed to look-up AuxiliaryService associations for subscriber " + subIdentifier
                    + " and auxiliary service Id " + auxSvcIdentifier, exception).log(context);
        }

        return auxiliaryService;
    }
/**
 * 
 * @param context
 * @param subIdentifier
 * @param auxSvcIdentifier
 * @param secondoryIdentifier
 * @return
 */
    public static SubscriberAuxiliaryService getSubscriberAuxiliaryServicesWithIdentifiers(final Context context,
            final String subIdentifier, final long auxSvcIdentifier, final long secondoryIdentifier)
    {
        SubscriberAuxiliaryService auxiliaryService = null;

        try
        {
            final And filter = new And();
            filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subIdentifier));
            filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, auxSvcIdentifier));
            
            if(secondoryIdentifier != 0)
            {
            	filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, secondoryIdentifier));	
            }
            final Collection<SubscriberAuxiliaryService> associations = selectSubscriberAuxiliaryServices(context,
                    filter);
            if (associations != null && !associations.isEmpty())	
            {
                auxiliaryService = associations.iterator().next();
            }
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Failed to look-up AuxiliaryService associations for subscriber " + subIdentifier
                    + " and auxiliary service Id " + auxSvcIdentifier, exception).log(context);
        }

        return auxiliaryService;
    }

    /**
     * Gets the existing associations for the given service.
     *
     * @param context
     *            The operating context.
     * @param service
     *            The AuxiliaryService for which to look-up existing associations.
     * @return The set of all subscriber-auxiliary services associated with the provided
     *         auxiliary service.
     * @exception HomeException
     *                Thrown if there are problems accessing Home information in the
     *                context.
     * @deprecated Use {@link com.redknee.app.crm.bean.AuxiliaryService.getSubscriberAssociations()}
     */
    @Deprecated
    public static Collection getSubscriberAuxiliaryServices(final Context context, final AuxiliaryService service)
        throws HomeException
    {
        return selectSubscriberAuxiliaryServices(context, new EQ(
                SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, Long.valueOf(service.getIdentifier())));
    }


    /**
     * Gets the set of auxiliary services provisioned for a subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber in question.
     * @return The set of subscriber-auxiliary service associations of the subscriber
     *         which are provisioned.
     * @throws HomeException
     *             Thrown if there are problems looking up the associations.
     */
    public static Collection<SubscriberAuxiliaryService> getSubscriberProvisionedAuxiliaryServices(
            final Context context, final Subscriber subscriber) throws HomeException
    {
        final And filter = new And();
        filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subscriber.getId()));
        filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.PROVISIONED, Boolean.TRUE));

        return selectSubscriberAuxiliaryServices(context, filter);
    }


    /**
     * Gets the set of auxiliary services provisioned for a subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber in question.
     * @return The set of subscriber-auxiliary service associations of the subscriber
     *         which are provisioned.
     * @throws HomeException
     *             Thrown if there are problems looking up the associations.
     */
    public static AuxiliaryService findSubscriberProvisionedAuxiliaryServicesByType(final Context context,
            final Subscriber subscriber, final AuxiliaryServiceTypeEnum type) throws HomeException
    {
        final Collection<SubscriberAuxiliaryService> subscriberAuxiliaryServices;
        subscriberAuxiliaryServices = getSubscriberProvisionedAuxiliaryServices(context, subscriber);
        final Collection<AuxiliaryService> auxiliaryServices;
        auxiliaryServices = getAuxiliaryServiceCollection(context, subscriberAuxiliaryServices, null);
        return CollectionSupportHelper.get(context).findFirst(
                context, 
                auxiliaryServices, 
                new EQ(AuxiliaryServiceXInfo.TYPE, type));
    }


    /**
     * Gets the set of the IDs of the auxiliary services provisioned for a subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber in question.
     * @return The set of auxiliary service IDs which the subscriber has provisioned.
     * @throws HomeException
     *             Thrown if there are problems looking up the associations.
     */
    public static Collection<SubscriberAuxiliaryService> getSubscriberProvisionedAuxiliaryServiceIDs(
            final Context context, final Subscriber subscriber) throws HomeException
    {
        final Collection<SubscriberAuxiliaryService> provisionedAuxiliaryServices;
        provisionedAuxiliaryServices = subscriber.getAuxiliaryServices(context);
        final ListBuildingVisitor result = new ListBuildingVisitor();
        try
        {
            Visitors.forEach(context, provisionedAuxiliaryServices,
                    new FunctionVisitor(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, result));
        }
        catch (final AgentException e)
        {
            final HomeException he = new HomeException(e);
            throw he;
        }
        return result;
    }


    /**
     * Gets the currently active SubscriberAuxiliaryServices associated with the given
     * subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to look up SubscriberAuxiliaryServices.
     * @param runningDate
     *            Date to act upon.
     * @return The active SubscriberAuxiliaryServices associated with the given
     *         subscriber.
     * @throws HomeException
     *             Thrown if the auxiliary services cannot be retrieved.
     */
    public static Collection<SubscriberAuxiliaryService> getActiveSubscriberAuxiliaryServices(final Context context,
            final Subscriber subscriber, final Date runningDate) throws HomeException
    {
    	return getActiveSubscriberAuxiliaryServices(context, subscriber, runningDate, null);
    }

    /**
     * Gets the currently active SubscriberAuxiliaryServices associated with the given
     * subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to look up SubscriberAuxiliaryServices.
     * @param runningDate
     *            Date to act upon.
     * @return The active SubscriberAuxiliaryServices associated with the given
     *         subscriber.
     * @throws HomeException
     *             Thrown if the auxiliary services cannot be retrieved.
     */
    public static Collection<SubscriberAuxiliaryService> getActiveSubscriberAuxiliaryServices(final Context context,
            final Subscriber subscriber, final Date runningDate, Date nextRecurringChargeDate) throws HomeException
    {
        final Date endOfDay = CalendarSupportHelper.get(context).getEndOfDay(runningDate);
        boolean notificationOnly = context.getBoolean(RecurringRechargeSupport.NOTIFICATION_ONLY);

        final And filter = new And();
        filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subscriber.getId()));
        filter.add(new LTE(SubscriberAuxiliaryServiceXInfo.START_DATE, endOfDay));
        filter.add(new GT(SubscriberAuxiliaryServiceXInfo.END_DATE, endOfDay));
        filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.PROVISIONED, Boolean.TRUE));
        if(!notificationOnly && nextRecurringChargeDate != null)
        {
        	filter.add(new LTE(SubscriberAuxiliaryServiceXInfo.NEXT_RECURRING_CHARGE_DATE,nextRecurringChargeDate));
        }
        else if(notificationOnly && nextRecurringChargeDate != null)
        {
        	filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.NEXT_RECURRING_CHARGE_DATE,nextRecurringChargeDate));
        }
        

        Collection<SubscriberAuxiliaryService> allActiveAuxServicesForSubscriber = null;

        try
        {
            allActiveAuxServicesForSubscriber = HomeSupportHelper.get(context).getBeans(context, SubscriberAuxiliaryService.class, filter);
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Failed to look-up active AuxiliaryService associations for subscriber " + subscriber.getId(),
                exception).log(context);
        }
        if (allActiveAuxServicesForSubscriber == null)
        {
            allActiveAuxServicesForSubscriber = new ArrayList<SubscriberAuxiliaryService>();
        }
        return allActiveAuxServicesForSubscriber;
    }
    

    /**
     * Gets the existing future associations for the given service.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The AuxiliaryService for which to look-up existing future associations.
     * @param runningDate
     *            The date to act upon.
     * @return The set of existing future associations of the subscriber.
     * @exception HomeException
     *                Thrown if there are problems accessing Home information in the
     *                context.
     * @deprecated Use {@link com.redknee.app.crm.bean.Subscriber.getFutureAuxiliaryServices(Context, Date}
     */
    @Deprecated
    public static Collection<SubscriberAuxiliaryService> getSubscriberFutureProvisionAuxSvcs(final Context context,
            final Subscriber subscriber, final Date runningDate) throws HomeException
    {
        return subscriber.getFutureAuxiliaryServices(context, runningDate);
    }


    /**
     * Updates the Subscriber's AuxiliaryService associations when moved from one account
     * to another.
     *
     * @param context
     *            The operating context.
     * @param oldSubscriberIdentifier
     *            The old identifier of the Subscriber.
     * @param newSubscriberIdentifier
     *            The new identifier of the Subscriber.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     */
    public static void moveSubscriber(final Context context, final String oldSubscriberIdentifier,
            final String newSubscriberIdentifier) throws HomeException
    {
        
        try
        {
            Subscriber oldSub = SubscriberSupport.getSubscriber(context, oldSubscriberIdentifier);
            Subscriber newSub = SubscriberSupport.getSubscriber(context, newSubscriberIdentifier);
            Account oldParentAccount = null;
            Account newParentAccount = null;
            if (oldSub != null && newSub != null)
            {
                oldParentAccount = oldSub.getAccount(context).getParentAccount(context);
                newParentAccount = newSub.getAccount(context).getParentAccount(context);
            }
            final Collection<SubscriberAuxiliaryService> associations = getSubscriberAuxiliaryServices(context,
                    oldSubscriberIdentifier);
            boolean isCUGAuxService = false;
            if (associations == null || associations.isEmpty())
            {
                return;
            }
            // We need to bypass the normal pipeline because we don't want any of
            // the side-effects. We only need to update the SubscriberIdentifier.
            final Home home = (Home) context.get(SubscriberAuxiliaryServiceXDBHome.class);
            for (SubscriberAuxiliaryService association : associations)
            {
                if (oldParentAccount != null && newParentAccount != null
                        && oldParentAccount.getBAN().equals(newParentAccount.getBAN())
                        && association.getType(context).equals(AuxiliaryServiceTypeEnum.CallingGroup))
                {
                    isCUGAuxService = true;
                    home.remove(context, association);
                }
                else
                {
                    association.setSubscriberIdentifier(newSubscriberIdentifier);
                    home.store(context, association);
                }
            }
            // TODO: Right now, when moving a subscriber, refunds are created for the old
            // subscriber and charges for the
            // new subscriber. If changed here, behavior would be different for auxiliary
            // services when compared
            // to bundles, services and packages. Modify
            // ChargingCopyMoveStrategy.adjustServiceAndBundleCharges()
            // to also move subscription history events for every item and also modify
            // adjustAuxServiceCharges()
            // not to refund/charge those events for auxiliary services.
            //
            // SubscriberSubscriptionHistorySupport.moveSubscriber(context,
            // oldSubscriberIdentifier, newSubscriberIdentifier,
            // ChargedItemTypeEnum.AUXSERVICE);
            // we need to transfer old subscriber's homezone count to new one
            // this is safe because any time this method is called both the subscribers
            // are present in the DB
            HomezoneCount hzCntBean = HomeSupportHelper.get(context).findBean(context, HomezoneCount.class,
                    oldSubscriberIdentifier);
            if (hzCntBean == null)
            {
                new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                        "No homzone count bean found with subscriber-id:" + oldSubscriberIdentifier, null).log(context);
                // throw new HomeException("No HomeZone count bean found with
                // subscriber-id:"+oldSubscriberIdentifier);
                hzCntBean = new HomezoneCount();
                hzCntBean.setSubscriberIdentifier(oldSubscriberIdentifier);
                hzCntBean.setHzcount(0);
            }
            final int oldHzCount = hzCntBean.getHzcount();
            updateSubscriberHzCountById(context, newSubscriberIdentifier, oldHzCount);
            enableHZIfRequired(context, SubscriberSupport.lookupSubscriberForSubId(context, newSubscriberIdentifier));
            AdditionalMsisdnAuxiliaryServiceSupport.updateAMsisdnSubscriberId(context, oldSubscriberIdentifier,
                    newSubscriberIdentifier);
            updateSubscriberHzCountById(context, oldSubscriberIdentifier, 0);
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Unable to move the Auxiliary service" , t).log(context);
        }
    }


    /**
     * Remove an association between the given MSISDN and the given AuxiliaryService.
     *
     * @param context
     *            The operating context.
     * @param msisdn
     *            The subscriber's MSISDN.
     * @param serviceIdentifier
     *            The identifier of the AuxiliaryService to remove from the subscriber.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     */
    public static void removeAssociation(final Context context, final String msisdn, final long serviceIdentifier,
            final long secondaryIdentifier)
        throws HomeException
    {
        final Subscriber subscriber = SubscriberSupport.lookupSubscriberForMSISDN(context, msisdn);

        if (subscriber == null)
        {
            throw new HomeException("Failed to locate subscriber for voice number " + msisdn);
        }

        removeAssociationForSubscriber(context, subscriber, serviceIdentifier, secondaryIdentifier);
    }


    /**
     * Determines if the SubscriberAuxliaryService is weekly or monthly.
     *
     * @param context
     *            The operating context.
     * @param association
     *            The Subscriber Auxiliary Service
     * @return True if the SubscriberAuxliaryService is Weekly, False if the
     *         SubscriberAuxliaryService is Monthly
     * @throws HomeException
     *             Thrown if there are problems looking up the charging mode.
     */
    public static boolean isWeeklyAuxiliaryService(final Context context, final SubscriberAuxiliaryService association)
        throws HomeException
    {
        AuxiliaryService service = association.getAuxiliaryService(context);
        return ServicePeriodEnum.WEEKLY.equals(service.getChargingModeType());
    }


    /**
     * Remove an association between the given Subscriber and the given AuxiliaryService.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber.
     * @param serviceIdentifier
     *            The identifier of the AuxiliaryService to remove from the subscriber.
     * @param secondaryIdentifier The unique instance ID of the Aux service
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     */
    public static SubscriberAuxiliaryService removeAssociationForSubscriber(final Context context, final Subscriber subscriber,
            final long serviceIdentifier, final long secondaryIdentifier) throws HomeException
    {
        final SubscriberAuxiliaryService association = getSubscriberAuxiliaryService(context, subscriber.getId(),
                serviceIdentifier, secondaryIdentifier);

        return removeSubscriberAuxiliaryService(context, association);
    }
    
    /**
     * Remove an subscriberauxiliaryservice 
     *
     * @param context
     *            The operating context.
     * @param subAuxService
     *            The subscriber auxiliary service that should removed.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     */
    public static SubscriberAuxiliaryService removeSubscriberAuxiliaryService(final Context context,
            SubscriberAuxiliaryService subAuxService) throws HomeException
    {
        if (subAuxService != null)
        {
            HomeSupport homeSupport = HomeSupportHelper.get(context);
            if (homeSupport.hasBeans(context, SubscriberAuxiliaryService.class, subAuxService.ID()))
            {
                homeSupport.removeBean(context, subAuxService);
            }
        }
        return subAuxService;
    }
    
    

    /**
     * Gets the collection of SubscriberAuxiliaryServices associated with the given
     * subscriber and auxiliary service. Replaces
     * {@link #getSubscriberAuxiliaryServicesBySubIdAndSvcId(Context, String, long)}.
     *
     * @param context
     *            The operating context.
     * @param subscriberId
     *            The identifier of the subscriber for which to look up
     *            SubscriberAuxiliaryServices.
     * @param auxiliaryServiceId
     *            The identifier of the auxiliary service.
     * @param secondaryId
     *            Secondary identifier.
     * @return The SubscriberAuxiliaryServices associated with the given subscriber.
     */
    public static SubscriberAuxiliaryService getSubscriberAuxiliaryService(final Context context,
            final String subscriberId, final long auxiliaryServiceId, final long secondaryId)
    {
        SubscriberAuxiliaryService result = null;
        try
        {
            final And and = new And();
            and.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subscriberId));
            and.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER,
                    Long.valueOf(auxiliaryServiceId)));
            if (secondaryId >= 0)
            {
                and.add(new EQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, Long.valueOf(secondaryId)));
            }
            final Collection<SubscriberAuxiliaryService> associations = selectSubscriberAuxiliaryServices(context, and);
            if (associations != null && !associations.isEmpty())
            {
                result = associations.iterator().next();
            }
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Failed to look-up AuxiliaryService associations for subscriber " + subscriberId, t).log(context);
        }

        return result;
    }

    /**
     * Selects a collection of subscriber auxiliary services based on the provided
     * predicate.
     *
     * @param context
     *            The operating context.
     * @param predicate
     *            Predicate to select the subscriber auxiliary services upon.
     * @return The collection of subscriber auxiliary services selected based on the
     *         provided predicate. Auxiliary service type of these associations are
     *         already set by
     *         {@link #setTypeForSubscriberAuxiliaryServiceCollection(Context, Collection)}
     *         .
     * @throws HomeException
     *             Thrown by home.
     */
    public static Collection<SubscriberAuxiliaryService> selectSubscriberAuxiliaryServices(final Context context,
            final Predicate predicate) throws HomeException
    {
        final Home home = (Home) context.get(SubscriberAuxiliaryServiceHome.class);

        Collection<SubscriberAuxiliaryService> collection = home.select(context, predicate);
        if (collection != null)
        {
            collection = setTypeForSubscriberAuxiliaryServiceCollection(context, collection);
        }
        return collection;
    }

    /**
     * Sets the auxiliary service type of all the SubscriberAuxiliaryService in a
     * collection.
     *
     * @param context
     *            The operating context.
     * @param collection
     *            Collection to be set.
     * @return The (same) collection with auxiliary service type set.
     */
    public static Collection<SubscriberAuxiliaryService> setTypeForSubscriberAuxiliaryServiceCollection(
            final Context context, final Collection<SubscriberAuxiliaryService> collection)
    {
        for (final SubscriberAuxiliaryService association : collection)
        {
            AuxiliaryService service = null;
            try
            {
                service = AuxiliaryServiceSupport.getAuxiliaryServicById(context,
                        association.getAuxiliaryServiceIdentifier());
            }

            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append("SubscriberAuxiliaryServiceSupport.setTypeForSubscriberAuxiliaryServiceCollection(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, SubscriberAuxiliaryServiceSupport.class, sb.toString(), exception);
                }
                continue;
            }

            if (service == null)
            {
                LogSupport.info(context, SubscriberAuxiliaryServiceSupport.class, "Cannot find auxiliary service "
                    + association.getAuxiliaryServiceIdentifier() + " referred by SubscriberAuxiliaryService "
                    + association.getIdentifier() + ", skipping");
                continue;
            }
            association.setType(service.getType());
        }
        return collection;
    }

    /**
     * Updates subscriber homezone count.
     *
     * @param context
     *            The operating context.
     * @param subscriberId
     *            Subscriber ID.
     * @param count
     *            Home zone count.
     * @throws HomeException
     *             Thrown if the update cannot be carried out.
     */
    public static void updateSubscriberHzCountById(final Context context, final String subscriberId, final int count)
        throws HomeException
    {
        final HomezoneCount homezoneCount = new HomezoneCount();
        homezoneCount.setSubscriberIdentifier(subscriberId);
        homezoneCount.setHzcount(count);

        Home hzCountHome = (Home) context.get(HomezoneCountHome.class);
        try
        {
            hzCountHome.store(context, homezoneCount);
        }
        catch (final HomeException he)
        {
            // TODO: check if entry not found home exception
            if (count == 1)
            {
                hzCountHome.create(context, homezoneCount);
            }
        }
    }


    /**
     * Updates subscriber homezone count.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber in question.
     * @param count
     *            The new homezone count.
     * @throws HomeException
     *             Thrown if the update cannot be carried out.
     */
    public static void updateSubscriberHzCount(final Context context, final Subscriber subscriber, final int count)
        throws HomeException
    {
        updateSubscriberHzCountById(context, subscriber.getId(), count);
    }


    /**
     * Updates subscriber homezone count.
     *
     * @param context
     *            The operating context.
     * @param homezoneCount
     *            The homezone count bean.
     * @throws HomeException
     *             Thrown if the update cannot be carried out.
     */
    public static void updateSubscriberHzCountWithBean(final Context context, final HomezoneCount homezoneCount)
        throws HomeException
    {
        Home hzCountHome = (Home) context.get(HomezoneCountHome.class);
        try
        {
            hzCountHome.store(context, homezoneCount);
        }
        catch (final HomeException he)
        {
            // TODO: check if entry not found HomeException
            if (homezoneCount.getHzcount() == 1)
            {
                hzCountHome.create(context, homezoneCount);
            }
        }
    }


    /**
     * Enables homezone for a subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being updated.
     */
    public static void enableHZIfRequired(final Context context, final Subscriber subscriber)
    {
        final boolean updateHomeZone = ((AppHomezoneClientConfig) context.get(AppHomezoneClientConfig.class))
            .getContactHomezone();

        if (!updateHomeZone)
        {
            return;
        }

        HomezoneCount hzCntBean = null;
        try
        {
            hzCntBean = HomeSupportHelper.get(context).findBean(context, HomezoneCount.class, subscriber.getId());
            if (hzCntBean == null)
            {
                new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                    "No homezone count bean found with subscriber-id:" + subscriber.getId(), null).log(context);
                hzCntBean = new HomezoneCount();
            }
            final int hzCount = hzCntBean.getHzcount();

            if (hzCount > 0)
            {
                // flag to true
                AppEcpClientSupport.enableHomeZoneInECP(context, subscriber);
            }
        }
        catch (final Exception e)
        {
            // ignore
            new MajorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Could not update the homezone flag for subscriber profile in the ECP for the subscriber:"
                    + subscriber.getId(), e).log(context);
        }
    }


    /**
     * Manda - New support method added to update the SMSB Service grade whenever the VPN
     * Service is added/removed.
     *
     * @param ctx
     *            Context Object
     * @param sub
     *            Subscriber Object
     * @return int result of the update to SMSB.
     * @throws HomeException
     *             Thrown if there are rpbolems updating SMSB.
     */
    public static int updateSmsbSvcGradeforVPNService(final Context ctx, final Subscriber sub) throws HomeException
    {
        int result = DEFAULT_VPN_ERROR_CODE;
        try
        {
            result = AppSmsbClientSupport.updateSubscriberProfileForSvcGrade(ctx, sub);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Unexpected error in SMSB Update for Service Grade", e).log(ctx);
            throw new HomeException("provisioing result 9999: failed to update SMSB subscriber for Service Grade ["
                + e.getMessage() + "]", e);
        }
        return result;
    }


    /**
     * Manda - New support method added to update the SMSB Service grade whenever the VPN
     * Service is provisioned/unprovisioned.
     *
     * @param ctx
     *            Context Object
     * @param sub
     *            Subscriber Object
     * @param provision
     *            boolean which tells whether to provision or unprovision the vpn service
     *            grade
     * @return int result of the update to SMSB.
     * @throws HomeException
     *             Thrown if there are problems updating subscriber in SMSB.
     */
    public static int updateSmsbSvcGradeforVPNService(final Context ctx, final Subscriber sub, final boolean provision)
        throws HomeException
    {
        int result = DEFAULT_VPN_ERROR_CODE;
        try
        {
            if (provision)
            {
                result = AppSmsbClientSupport.updateSubscriberProfileForSvcGrade(ctx, sub);
            }
            else
            {
                result = AppSmsbClientSupport.updateSubscriberProfileForSvcGradeForUnprovision(ctx, sub);
            }
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(SubscriberAuxiliaryServiceSupport.class,
                "Unexpected error in SMSB Update for Service Grade", e).log(ctx);
            throw new HomeException("provisioing result 9999: failed to update SMSB subscriber for Service Grade ["
                + e.getMessage() + "]", e);
        }
        return result;
    }


    /**
     * This method returns the Charging Mode type(Weekly/Monthly) index for the given
     * AuxiliaryService id.
     *
     * @param ctx
     *            context object
     * @param auxServiceId
     *            AuxiliaryService Id
     * @return short chargingmode type Id
     */
    public static short getChargingModeType(final Context ctx, final long auxServiceId)
    {
        short chargingMode = ServicePeriodEnum.MONTHLY_INDEX;
        AuxiliaryService auxService = null;
        try
        {
            auxService = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, auxServiceId);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(SubscriberAuxiliaryServiceSupport.class.getName(),
                "Unable to find the Auxiliary Service with the given Aux Service Id = " + auxServiceId, e).log(ctx);
        }

        if (auxService != null)
        {
            chargingMode = auxService.getChargingModeType().getIndex();
        }

        return chargingMode;
    }


    /**
     * Manda - This method returns the set of Auxiliaryservice identifiers for the given
     * collection of SctAuxiliaryServices Collection.
     *
     * @param existingSet
     *            SortedSet object representing the collection which already have some
     *            AuxiliaryService identifiers.
     * @param sctAuxCollection
     *            Collection object representing the SctAuxiliaryService objects.
     * @return A unique, sorted set of AuxiliaryService identifiers (Long).
     */
    public static SortedSet<Long> getSelectionSetFromSat(final Collection<SctAuxiliaryService> sctAuxCollection)
    {
        final SortedSet<Long> result = new TreeSet<Long>();
        if (sctAuxCollection == null)
        {
            return result;
        }

        for (final SctAuxiliaryService association : sctAuxCollection)
        {
            result.add(Long.valueOf(association.getAuxiliaryServiceIdentifier()));
        }

        return result;
    }

    /**
     * Default VPN error code.
     */
    private static final int DEFAULT_VPN_ERROR_CODE = -2;

    /**
     * The default value of secondary ID when it is not used.
     */
    public static final int SECONDARY_ID_NOT_USED = -1;

    /**
     * Returns the subscriber-auxiliary association with the provided identifier.
     *
     * @param context
     *            The operating context.
     * @param identifier
     *            The identifier of the association to look up.
     * @return The subscriber-auxiliary service association with the provided ID, or
     *         <code>null</code> if none is found.
     * @throws HomeException
     *             Thrown if there are problems looking up the association.
     */
    public static SubscriberAuxiliaryService getSubscriberAuxiliaryService(final Context context, final long identifier)
        throws HomeException
    {
        final SubscriberAuxiliaryService association = HomeSupportHelper.get(context).findBean(
                context, 
                SubscriberAuxiliaryService.class, 
                identifier);
        return association;
    }


    /**
     * Builds a map of (subscriber ID, association) from a collection of
     * subscriber-auxiliary service associations.
     *
     * @param context
     *            The operating context.
     * @param associations
     *            Collection of subscriber-auxiliary service associations.
     * @return A map of (subscriber ID, association).
     */
    public static Map<String, List<SubscriberAuxiliaryService>> buildSubIdAssociationMap(final Context context,
            final Collection<SubscriberAuxiliaryService> associations)
    {
        final Map<String, List<SubscriberAuxiliaryService>> serviceBySubIdMap;
        serviceBySubIdMap = new HashMap<String, List<SubscriberAuxiliaryService>>();

        for (SubscriberAuxiliaryService association : associations)
        {
            List<SubscriberAuxiliaryService> serivceBySubIdList;
            serivceBySubIdList = serviceBySubIdMap.get(association.getSubscriberIdentifier());
            if (serivceBySubIdList == null)
            {
                serivceBySubIdList = new ArrayList<SubscriberAuxiliaryService>();
            }

            serivceBySubIdList.add(association);
            serviceBySubIdMap.put(association.getSubscriberIdentifier(), serivceBySubIdList);
        }
        return serviceBySubIdMap;
    }
    
    
    
    public static void updateGatewayTypeServiceState(Context ctx, Subscriber sub , long serviceId, long secondaryId, boolean state)
    {
        SubscriberAuxiliaryService subAuxSvc = getSelectedSubscriberAuxiliaryService(sub.getAuxiliaryServices(ctx), serviceId, secondaryId); 
        
        if (subAuxSvc != null)
        {
            subAuxSvc.setProvisionActionState(state);
            subAuxSvc = updateSubscriberAuxiliaryServiceOnXDBHomeDirectly(ctx, subAuxSvc); 
            // the cached subscriber auxiliary service in the subscriber is not update? do we need or not? 
        }
    }
    

    public static SubscriberAuxiliaryService updateSubscriberAuxiliaryService(Context ctx, SubscriberAuxiliaryService subAuxSvc)
    {
        try 
        {
            Home home =(Home) ctx.get(SubscriberAuxiliaryServiceHome.class);
            if (home != null)
            {
                return (SubscriberAuxiliaryService)home.store(ctx, subAuxSvc);
            }
            else 
            {
                throw new Exception("can not fine subscriber auxiliary service xdb home"); 
            }
            
        } catch (Exception e)
        {
            new MinorLogMsg(SuspensionSupport.class,"fail to update suscirber auxiliary service for " + subAuxSvc.getSubscriberIdentifier() + 
                    " " +  subAuxSvc.getIdentifier(), e).log(ctx);          
        }
        
        return null; 
    }
    
    
    public static SubscriberAuxiliaryService updateSubscriberAuxiliaryServiceOnXDBHomeDirectly(Context ctx, SubscriberAuxiliaryService subAuxSvc)
    {
        try 
        {
            Home home =(Home) ctx.get(SubscriberAuxiliaryServiceXDBHome.class);
            if (home != null)
            {
                return (SubscriberAuxiliaryService)home.store(ctx, subAuxSvc);
            }
            else 
            {
                throw new Exception("can not fine subscriber auxiliary service xdb home"); 
            }
            
        } catch (Exception e)
        {
            new MinorLogMsg(SuspensionSupport.class,"fail to update suscirber auxiliary service for " + subAuxSvc.getSubscriberIdentifier() + 
                    " " +  subAuxSvc.getIdentifier(), e).log(ctx);          
        }
        
        return null; 
    }
    
    
   public static com.redknee.app.crm.bean.service.ServiceStateEnum getState(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum action, boolean status)
   {
       if(status)
       {
           switch (action.getIndex() )
           {
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.CREATE_INDEX:
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.PROVISION_INDEX:
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.RESUME_INDEX:
               return com.redknee.app.crm.bean.service.ServiceStateEnum.PROVISIONED;
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.SUSPEND_INDEX:
               return com.redknee.app.crm.bean.service.ServiceStateEnum.SUSPENDED;
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.UNPROVISION_INDEX:
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.REMOVE_INDEX:
               return com.redknee.app.crm.bean.service.ServiceStateEnum.UNPROVISIONED;
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.PENDING_INDEX:
               return com.redknee.app.crm.bean.service.ServiceStateEnum.PENDING; 
           }
           
           return com.redknee.app.crm.bean.service.ServiceStateEnum.PROVISIONED; 
               
       } else
       {
           switch (action.getIndex() )
           {
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.CREATE_INDEX:
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.PROVISION_INDEX:
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.RESUME_INDEX:
               return com.redknee.app.crm.bean.service.ServiceStateEnum.PROVISIONEDWITHERRORS;
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.SUSPEND_INDEX:
               return com.redknee.app.crm.bean.service.ServiceStateEnum.SUSPENDEDWITHERRORS;
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.UNPROVISION_INDEX:
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.REMOVE_INDEX:
               return com.redknee.app.crm.bean.service.ServiceStateEnum.UNPROVISIONEDWITHERRORS;
           case com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.PENDING_INDEX:
               return com.redknee.app.crm.bean.service.ServiceStateEnum.PENDING;
           }
           
           return com.redknee.app.crm.bean.service.ServiceStateEnum.UNPROVISIONEDWITHERRORS; 
       }
       
   }
}
