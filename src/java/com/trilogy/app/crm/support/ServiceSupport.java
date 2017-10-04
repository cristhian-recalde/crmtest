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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import  com.redknee.app.crm.util.SubscriberServicesUtil;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtensionXInfo;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.service.filter.ServiceHandlerPredicate;


/**
 * Class contains support methods for services handling.
 *
 * @author joe.chen@redknee.com
 */
public final class ServiceSupport
{
    /**
     * Name of the handler for voice services.
     */
    public static final String VOICE = "Voice";

    /**
     * Name of the handler for SMS services.
     */
    public static final String SMS = "Sms";

    /**
     * Creates a new <code>ServiceSupport</code> instance. This method is made private
     * to prevent instantiation of utility class.
     */
    private ServiceSupport()
    {
        // empty
    }


    /**
     * Determines if suspension of service needs to be refunded. Based only on
     * service.isSmartSuspension().
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            The service to be determined.
     * @return Returns <code>true</code> if service is not configured with
     *         SmartSuspension, <code>false</code> otherwise.
     */
    public static boolean isSuspensionRefunded(final Context ctx, final ServiceBase service)
    {
        return !service.isSmartSuspension();
    }


    /**
     * Determines if unsuspension of the service needs to be charged. First checks if the
     * service is not configured with SmartSuspension. Then checks if there is a charge
     * transaction in the BillCycle.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being examined.
     * @param service
     *            The service being examined.
     * @param billingDate
     *            The date to treat as billing date.
     * @return Returns <code>true</code> is service is not in SmartSuspention mode OR if
     *         the billCycle does not have a charge transaction, <code>false</code>
     *         otherwise.
     * @throws HomeException
     *             Thrown Thrown if there are problems looking up any related beans.
     */
    public static boolean isUnsuspensionCharged(final Context ctx, final Subscriber sub, final ServiceFee2 service, final ChargedItemTypeEnum itemType,
            final long amount, final Date billingDate) throws HomeException
    {
        if (!service.getService(ctx).getSmartSuspension())
        {
            return true;
        }
        return !RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(ctx, sub, service, itemType, service.getServicePeriod(), service.getService(ctx).getAdjustmentType(), amount, billingDate);
    }


    /**
     * <p>
     * Determine if an un-suspention is going on Weekly charges safe.
     * </p>
     * <p>
     * TODO 2007-02-16 rewrite to be based on subscriber state not making an unnecesary DB
     * call
     * </p>
     *
     * @param ctx
     *            the operation context
     * @param sub
     *            subscriber to check
     * @param service
     *            service to check
     * @return true if the passed in service and subscriber look like an un-suspention is
     *         happening
     */
    public static boolean isUnsuspend(final Context ctx, final Subscriber sub, final ServiceBase service)
    {
        final Collection trans = CoreTransactionSupportHelper.get(ctx).getTransactionsForSubAdjustment(ctx, sub.getId(),
                service .getAdjustmentType(), new Limit(1));
        return trans != null && trans.size() > 0;
    }




    /**
     * Converts a collection of services or ServiceFee2 object into ServiceId collection.
     *
     * @param serviceObjs
     *            Collection of {@link Service} or {@link ServiceFee2} objects.
     * @return a new collection with service id Number object.
     */
    public static Collection transformServiceObjectToIds(final Collection serviceObjs)
    {
        Collection list = null;
        if (serviceObjs != null)
        {
            list = new ArrayList(serviceObjs.size());
            for (final Iterator it = serviceObjs.iterator(); it.hasNext();)
            {
                Object obj = it.next();
                if (obj instanceof Service)
                {
                    obj = ((Service) obj).ID();
                }
                else if (obj instanceof Number)
                {
                    // I assume that it is the service id!!
                }
                else if (obj instanceof ServiceFee2)
                {
                    obj = ((ServiceFee2) obj).ID();
                }
                else if (obj instanceof ServiceFee2ID)
                {
                    obj = ((ServiceFee2ID) obj).getServiceId();
                }
                else if (obj instanceof SubscriberAuxiliaryService)
                {
                    obj = Long.valueOf(((SubscriberAuxiliaryService) obj).getAuxiliaryServiceIdentifier());
                }
                else if (obj instanceof BundleProfile)
                {
                    obj = ((BundleProfile)obj).ID();
                }
                else if (obj instanceof BundleFee)
                {
                    obj = ((BundleFee) obj).ID();
                }
                else if (obj instanceof ServicePackageFee)
                {
                    obj = Long.valueOf(((ServicePackageFee) obj).getPackageId());
                }
                else if (obj instanceof ServicePackage)
                {
                    obj = ((ServicePackage) obj).ID();
                }
                else if (obj instanceof SubscriberServices)
                {
                    obj = ((SubscriberServices) obj).getServiceId();
                }
                else
                {
                    throw new IllegalArgumentException("Invalid Service object, " + obj);
                }
                if (obj != null)
                {
                    list.add(obj);
                }
            }
        }
        return list;
    }


    /**
     * Converts the collect of service id into Services objects, it also filter out all
     * invalid service id.
     *
     * @param ctx
     *            The operating context.
     * @param serviceIds
     *            The collection of service IDs.
     * @return a new collection of {@link Service} objects.
     * @throws HomeException
     *             Thrown if there are problems looking up any of the services.
     */
    public static List<Service> transformServiceIdToObjects(final Context ctx, final Collection serviceIds)
        throws HomeException
    {
        List<Service> list = null;
        if (serviceIds != null)
        {
            list = new ArrayList<Service>(serviceIds.size());

            final Home serviceHome = (Home) ctx.get(ServiceHome.class);
            for (Object obj : serviceIds)
            {
                Service service = null;
                if (obj instanceof Number)
                {
                    service = (Service) serviceHome.find(ctx, obj);
                }
                else if (obj instanceof ServiceFee2ID)
                {
                    service = (Service) serviceHome.find(ctx, ((ServiceFee2ID) obj).getServiceId());
                }
                else if (obj instanceof Service)
                {
                    service = (Service) obj;
                }
                else if (obj instanceof SubscriberServices)
                {
                    service = ((SubscriberServices) obj).getService(ctx);
                }
                else
                {
                    throw new IllegalArgumentException("Invalid Service ID object, " + obj);
                }

                if (service != null)
                {
                    list.add(service);
                }
            }
        }
        return list;
    }


    /**
     * Retains all services that are available in ServiceHome.
     *
     * @param ctx
     *            the operating context
     * @param servicesToProvision
     *            either {@link Service} or service IDs
     * @return a new collection with service id which are available in service home.
     * @throws HomeException
     *             Thrown if there are problems converting the IDs to {@link Service} or
     *             vice versa.
     */
    public static Collection filterValidService(final Context ctx, final Collection servicesToProvision)
        throws HomeException
    {
        final Set retServices = new HashSet(servicesToProvision.size());
        retServices.addAll(transformServiceObjectToIds(servicesToProvision));
        retServices.retainAll(getAllServiceIds(ctx));

        return retServices;
    }


    /**
     * Get the list of all valid service ids.
     *
     * @param ctx
     *            The operating context.
     * @return The collection of all valid service IDs.
     * @throws UnsupportedOperationException
     *             Thrown if {@link Home#selectAll(Context)} is not supported.
     * @throws HomeException
     *             Thrown if there are problems looking up the services.
     */
    public static Collection getAllServiceIds(final Context ctx) throws HomeException
    {
        return transformServiceObjectToIds(HomeSupportHelper.get(ctx).getBeans(ctx, Service.class, True.instance()));
    }


    /**
     * Get the service object for a service id.
     *
     * @param ctx
     *            The operating cotnext.
     * @param serviceID
     *            The identifier of the service to look up.
     * @return The service with the requested ID.
     * @throws HomeException
     *             Thrown if there are problems looking up the service.
     */
    public static Service getService(final Context ctx, final long serviceID) throws HomeException
    {
        final Service service = HomeSupportHelper.get(ctx).findBean(
                ctx,
                Service.class,
                Long.valueOf(serviceID));
        return service;
    }

    
    /**
     * Get the service object instance
     *
     * @param ctx
     *            The operating cotnext.
     */
    public static Service getServiceInstance(final Context ctx) throws Exception
    {
            return (Service) XBeans.instantiate(Service.class, ctx);
    }
    
    public static Collection<Service> filterServicesByType(final Context ctx, final Collection<Long> serviceList,
            final ServiceTypeEnum type) throws HomeException
    {
        if (type == null || serviceList == null || serviceList.size() == 0)
        {
            return new ArrayList<Service>();
        }

        final And condition = new And();
        condition.add(new EQ(ServiceXInfo.TYPE, type));
        if (serviceList.size() == 1)
        {
            condition.add(new EQ(ServiceXInfo.ID, serviceList.iterator().next()));
        }
        else if (serviceList.size() > 1)
        {
            if (serviceList instanceof Set)
            {
                condition.add(new In(ServiceXInfo.ID, (Set) serviceList));
            }
            else
            {
                condition.add(new In(ServiceXInfo.ID, new HashSet(serviceList)));
            }
        }

        return HomeSupportHelper.get(ctx).getBeans(ctx, Service.class, condition);
    }

    public static boolean hasServiceOfType(final Context ctx, final Collection<Long> serviceList,
            final ServiceTypeEnum type) throws HomeException
    {
        if (serviceList == null || serviceList.size() == 0)
        {
            return false;
        }

        final And condition = new And();
        condition.add(new EQ(ServiceXInfo.TYPE, type));
        if (serviceList.size() == 1)
        {
            condition.add(new EQ(ServiceXInfo.ID, serviceList.iterator().next()));
        }
        else if (serviceList.size() > 1)
        {
            if (serviceList instanceof Set)
            {
                condition.add(new In(ServiceXInfo.ID, (Set) serviceList));
            }
            else
            {
                condition.add(new In(ServiceXInfo.ID, new HashSet(serviceList)));
            }
        }

        return HomeSupportHelper.get(ctx).hasBeans(ctx, Service.class, condition);
    }


    /**
     * Get Service object from adjustment type.
     *
     * @param ctx
     *            The operating context.
     * @param adjustmentId
     *            The adjustment type ID.
     * @return The service with the provided adjustment type.
     * @throws HomeException
     *             Thrown if there are problems looking up.
     */
    public static Service getServiceByAdjustment(final Context ctx, final int adjustmentId) throws HomeException
    {
        final Service service = HomeSupportHelper.get(ctx).findBean(
                ctx,
                Service.class,
                 new EQ(ServiceXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustmentId)));
        return service;
    }


    /**
     * Get Auxiliary Service by Adjustment Id.
     *
     * @param ctx
     *            The operating context.
     * @param adjustmentId
     *            The adjustment type ID.
     * @return The auxiliary service with the provided adjustment type.
     * @throws HomeException
     *             Thrown if there are problems looking up.
     */

    public static AuxiliaryService getAuxServiceByAdjustment(final Context ctx, final int adjustmentId)
        throws HomeException
    {
        final AuxiliaryService service = HomeSupportHelper.get(ctx).findBean(
                ctx,
                AuxiliaryService.class,
                new EQ(AuxiliaryServiceXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustmentId)));
        return service;
    }


    /**
     * Converts the service Object or id collection into a id string.
     *
     * @param services
     *            The collection of {@link Service} objects or service IDs.
     * @return a comma-delimited string of all service IDs.
     */
    public static String getServiceIdString(final Collection services)
    {
        final StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (final Iterator it = services.iterator(); it.hasNext();)
        {
            final Object obj = it.next();
            if (!firstTime)
            {
                sb.append(",");
            }
            else
            {
                firstTime = false;
            }

            if (obj instanceof Number)
            {
                sb.append(((Number) obj).intValue());
            }
            else if (obj instanceof ServiceBase)
            {
                sb.append(((Service) obj).getIdentifier());
            }
            else if (obj instanceof ServiceFee2)
            {
                sb.append(((ServiceFee2) obj).getServiceId());
            }
            else if (obj instanceof ServiceFee2ID)
            {
                sb.append(((ServiceFee2ID) obj).getServiceId());
            }
            else if (obj instanceof SubscriberAuxiliaryService)
            {
                sb.append(((SubscriberAuxiliaryService) obj).getAuxiliaryServiceIdentifier());
            }
            else if (obj instanceof BundleProfile)
            {
                sb.append(((BundleProfile)obj).ID());
            }
            else if (obj instanceof BundleFee)
            {
                sb.append(((BundleFee) obj).ID());
            }
            else if (obj instanceof ServicePackageFee)
            {
                sb.append(((ServicePackageFee) obj).ID());
            }
            else if (obj instanceof ServicePackage)
            {
                sb.append(((ServicePackage) obj).ID());
            }
            else if (obj instanceof SubscriberServices)
            {
                sb.append(((SubscriberServices) obj).getServiceId());
            }
            else
            {
                throw new IllegalArgumentException("Invalid Service ID object, " + obj);
            }
        }
        return sb.toString();
    }


    /**
     * Creates and returns a new AdjustmentType for the given Service in the given
     * context.
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            The Service for which to create a new AdjustmentType.
     * @return A new AdjustmentType.
     * @throws HomeException
     *             Thrown if there are problems creating the adjustment type.
     */
    public static AdjustmentType createAdjustmentType(final Context ctx, final Service service) throws HomeException
    {
        return createAdjustmentTypeFor(ctx, service, "Service " + service.getID() + "- " + service.getName(),
                service.getAdjustmentGLCode());
    }


    /**
     * Creates an adjustment type for a service.
     *
     * @param ctx
     *            The operating context.
     * @param service
     *            The service to create adjustment type for.
     * @param name
     *            The name of the adjustment type to create.
     * @param glCode
     *            GL code to use for this adjustment type.
     * @return The new adjustment type with the provided name.
     * @throws HomeException
     *             Thrown if there are problems creating the adjustment type.
     */
    private static AdjustmentType createAdjustmentTypeFor(final Context ctx, final Service service, final String name,
            final String glCode) throws HomeException
    {
        AdjustmentType type = null;
        try
        {
            type = (AdjustmentType) XBeans.instantiate(AdjustmentType.class, ctx);
        }
        catch (final Exception e)
        {
            throw new HomeException("Failed to instantiate AdjustmentType", e);
        }
        type.setParentCode(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                AdjustmentTypeEnum.Services));
        type.setName(name);
        type.setDesc(service.getAdjustmentTypeDesc());

        type.setAction(AdjustmentTypeActionEnum.EITHER);
        type.setCategory(false);
        type.setLoyaltyEligible(true);
        
        final Map spidInformation = type.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(service.getSpid());
        AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

        if (information == null)
        {
            information = new AdjustmentInfo();
            spidInformation.put(key, information);
        }

        information.setSpid(service.getSpid());
        String serviceGlCode = glCode.trim();
        information.setGLCode(serviceGlCode);
        information.setInvoiceDesc(service.getAdjustmentInvoiceDesc());

        int taxAuthority = service.getTaxAuthority();
        if (taxAuthority == -1)
        {
            taxAuthority = SpidSupport.getDefaultTaxAuthority(ctx, service.getSpid());
        }
        information.setTaxAuthority(taxAuthority);

        type.setAdjustmentSpidInfo(spidInformation);

        final Home home = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        type = (AdjustmentType) home.create(ctx, type);

        return type;
    }


    /**
     * Find the first service type in subscriber selected services with the provided
     * service handler.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being examined.
     * @param serviceHandler
     *            The service handler to look for.
     * @return The first service in the subscriber's selected services which has the
     *         provided service handler.
     * @throws HomeException
     *             Thrown if there are problems looking up the services.
     */
    public static Service findSubscriberPricePlanServiceType(final Context ctx, final Subscriber sub,
            final String serviceHandler) throws HomeException
    {
        if (sub == null)
        {
            return null;
        }

        final Collection svcIds = sub.getServices();

        final Collection svcObjs = transformServiceIdToObjects(ctx, svcIds);
        final Object obj = CollectionSupportHelper.get(ctx).findFirst(ctx, svcObjs, new ServiceHandlerPredicate(serviceHandler));
        return (Service) obj;
    }


    /**
     * Find the first service type in subscriber provisioned services with the provided
     * service handler.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being examined.
     * @param serviceHandler
     *            The service handler to look for.
     * @return The first service in the subscriber's provisioned services which has the
     *         provided service handler.
     * @throws HomeException
     *             Thrown if there are problems looking up the services.
     */
    public static Service findSubscriberProvisionedServiceType(final Context ctx, final Subscriber sub,
            final String serviceHandler) throws HomeException
    {
        if (sub == null)
        {
            return null;
        }
        final Collection svcIds = sub.getProvisionedServices(ctx);
        final Collection svcObjs = transformServiceIdToObjects(ctx, svcIds);
        final Object obj = CollectionSupportHelper.get(ctx).findFirst(ctx, svcObjs, new ServiceHandlerPredicate(serviceHandler));
        return (Service) obj;
    }


    /**
     * Find the first service type in subscriber's newly provisioned services.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being examined.
     * @param serviceHandler
     *            The service handler to look for.
     * @return The first service in the subscriber's newly provisioned services which has
     *         the provided service handler.
     * @throws HomeException
     *             Thrown if there are problems looking up the services.
     */
    public static Service findSubscriberNewlyProvisionedServiceType(final Context ctx, final Subscriber sub,
            final String serviceHandler) throws HomeException
    {
        if (sub == null)
        {
            return null;
        }

        final Collection svcIds = sub.getTransientProvisionedServices();
        final Collection svcObjs = transformServiceIdToObjects(ctx, svcIds);
        final Object obj = CollectionSupportHelper.get(ctx).findFirst(ctx, svcObjs, new ServiceHandlerPredicate(serviceHandler));
        return (Service) obj;
    }


    /**
     * Find the service or the package that contains the service type.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being examined.
     * @param serviceHandler
     *            The service handler to look for.
     * @return The first service or service package in the subscriber's selected services
     *         which has the provided service handler.
     * @throws HomeException
     *             Thrown if there are problems looking up the services.
     */
    public static Object findObjectContainingServiceType(final Context ctx, final Subscriber sub,
        final String serviceHandler) throws HomeException
    {
        if (sub == null)
        {
            return null;
        }

        Object obj = null;
        Collection<ServicePackageFee> newPackages;

        // TODO 2007-05-10 maybe use subscriber.getRawPricePlanVersion()
        final PricePlanVersion version = PricePlanSupport.getVersion(ctx,
                sub.getPricePlan(), sub.getPricePlanVersion());

        if (version.getServicePackageVersion() == null)
        {
            newPackages = new ArrayList<ServicePackageFee>();
        }
        else
        {
            newPackages = version.getServicePackageVersion().getPackageFees().values();
        }
        for (ServicePackageFee fee : newPackages)
        {
            final ServicePackage pack = ServicePackageSupportHelper.get(ctx).getServicePackage(ctx, fee.getPackageId());
            final Map serviceFees = pack.getCurrentVersion(ctx).getServiceFees(ctx);
            final Collection svcObjs = transformServiceIdToObjects(ctx, SubscriberServicesUtil.getServiceIds(serviceFees.keySet()));
            obj = CollectionSupportHelper.get(ctx).findFirst(ctx, svcObjs, new ServiceHandlerPredicate(serviceHandler));
            if (obj != null)
            {
                obj = pack;
                break;
            }
        }

        if (obj == null)
        {
            final Map serviceFees = version.getServicePackageVersion().getServiceFees();
            final Collection svcObjs = transformServiceIdToObjects(ctx, SubscriberServicesUtil.getServiceIds(serviceFees.keySet()));
            obj = CollectionSupportHelper.get(ctx).findFirst(ctx, svcObjs, new ServiceHandlerPredicate(serviceHandler));
        }

        return obj;
    }


    /**
     * Returns the subset of services with smart suspension.
     *
     * @param ctx
     *            The operating context.
     * @param provisionedServices
     *            The set of service IDs being examined.
     * @return a set of IDs of services contained in <code>provisionedServices</code>
     *         that have Smart Suspension.
     */
    public static Set<Long> getSmartSuspensionServices(final Context ctx, final Set<Long> provisionedServices)
    {
        final Set<Long> serviceToSuspend = new HashSet<Long>(provisionedServices.size());
        for (Long serviceId : provisionedServices)
        {
            try
            {
                final Service service = getService(ctx, serviceId);
                if (!service.isSmartSuspension())
                {
                    serviceToSuspend.add(serviceId);
                }
            }
            catch (final Exception e)
            {
                new DebugLogMsg(ServiceSupport.class, "Exception caught", e).log(ctx);
            }
        }

        return serviceToSuspend;
    }


    /**
     * Checks to see if the given subscriber has the given service type enabled in their
     * PricePlan.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to examine services.
     * @param serviceType
     *            The type of service to search for.
     * @return True if the subscriber has an enabled service of the given type in their
     *         PricePlan.
     * @throws HomeException
     *             Thrown if the there are any problems accessing Home data in the
     *             context.
     */
    public static boolean isServiceSelected(final Context context, final Subscriber subscriber,
            final ServiceTypeEnum serviceType) throws HomeException
    {
        final PricePlanVersion plan = subscriber.getPricePlan(context);
        final Map serviceFees = plan.getServiceFees(context);
        final Set serviceIdentifiers = serviceFees.keySet();

        final And seachCriteria = new And();
        seachCriteria.add(new In(ServiceXInfo.ID, (Set<Long>)SubscriberServicesUtil.getServiceIds(serviceIdentifiers)));
        seachCriteria.add(new EQ(ServiceXInfo.TYPE, serviceType));

        return HomeSupportHelper.get(context).hasBeans(context, Service.class, seachCriteria);
    }


    /**
     * Disables the service for a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber whose service is being disabled.
     * @param svc
     *            The service to disable for the subscriber.
     * @return Result code of the underlying client.
     * @throws HomeException
     *             Thrown if there are problems disabling the service.
     */
    public static int disableService(final Context ctx, final Subscriber sub, final Service svc) throws HomeException
    {
        int result = 0;

        if (svc.getHandler().equals(VOICE))
        {
            final AppEcpClient appEcpClient = (AppEcpClient) ctx.get(AppEcpClient.class);
            if (appEcpClient == null)
            {
                new DebugLogMsg(ServiceSupport.class, "AppEcpClient not found in context", null).log(ctx);
            }
            else
            {
                result = appEcpClient.updateSubscriberState(sub.getMSISDN(), AppEcpClient.SUSPENDED);
            }
        }
        else if (svc.getHandler().equals(SMS))
        {
            final AppSmsbClient appSmsbClient = (AppSmsbClient) ctx.get(AppSmsbClient.class);
            if (appSmsbClient == null)
            {
                new DebugLogMsg(ServiceSupport.class, "AppSmsbClient not found in context", null).log(ctx);
            }
            else
            {
                result = appSmsbClient.enableSubscriber(sub.getMSISDN(), false);
            }
        }
        return result;
    }


    /**
     * Enables the service for a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber whose service is being enabled.
     * @param svc
     *            The service to enabled for the subscriber.
     * @return Result code of the underlying client.
     * @throws HomeException
     *             Thrown if there are problems enabling the service.
     */
    public static int enableService(final Context ctx, final Subscriber sub, final Service svc) throws HomeException
    {
        int result = 0;

        if (svc.getHandler().equals(VOICE))
        {
            final AppEcpClient appEcpClient = (AppEcpClient) ctx.get(AppEcpClient.class);
            if (appEcpClient == null)
            {
                new DebugLogMsg(ServiceSupport.class, "AppEcpClient not found in context", null).log(ctx);
            }
            else
            {
                result = appEcpClient.updateSubscriberState(sub.getMSISDN(), AppEcpClient.ACTIVE);
            }
        }
        else if (svc.getHandler().equals(SMS))
        {
            final AppSmsbClient appSmsbClient = (AppSmsbClient) ctx.get(AppSmsbClient.class);
            if (appSmsbClient == null)
            {
                new DebugLogMsg(ServiceSupport.class, "AppSmsbClient not found in context", null).log(ctx);
            }
            else
            {
                // result=appSmsbClient.enableSubscriber(sub.getMSISDN(), true);

                /*
                 * Manda - Calling a new method in SMSB Client to enable the Subscriber
                 * and also calculate the prorated SMS
                 */
                result = appSmsbClient.enableSubscriberAndProrateSms(sub.getMSISDN(), true, sub);
            }
        }
        return result;
    }

    /**
     * verifies if the one time charge has been charged already.
     * @param ctx the operating context
     * @param newSub the current subscriber state
     * @param oldSub the previous subscriber state
     * @param service the service to be charged
     * @return true if already charged
     */
    public static boolean oneTimeChargeExist(final Context ctx,
            final Subscriber newSub,
            final Subscriber oldSub,
            final Service service)
    {
        boolean isDuplicate = false;

        if (service.getChargeScheme() == ServicePeriodEnum.ONE_TIME)
        {
            final SubscriberServices subSvc = SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                    oldSub.getId(),
                    service.getIdentifier(), 
					SubscriberServicesUtil.DEFAULT_PATH);

            /*
             * the service is not on the subscriber yet so its safe to say no duplicate transaction occurred
             */
            if (subSvc != null)
            {
                final And filter = new And();
                filter.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, oldSub.getId()));
                filter.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, service.getAdjustmentType()));
                filter.add(new GTE(TransactionXInfo.TRANS_DATE, subSvc.getStartDate()));
                filter.add(new LTE(TransactionXInfo.TRANS_DATE, subSvc.getEndDate()));

                try
                {
                    isDuplicate = HomeSupportHelper.get(ctx).hasBeans(ctx, Transaction.class, filter);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(ServiceSupport.class,
                            "Error determining number of " + service.getAdjustmentType()
                            + " transactions for subscription " + oldSub.getId(), e).log(ctx);
                }

                LogSupport.debug(ctx,
                        ServiceSupport.class,
                        "isDuplicate for service "
                        + service.getIdentifier()
                        + " = "
                        + isDuplicate);
            }
        }
        return isDuplicate;
    }

    /**
     * Retrieves the provision configs of the service based on the subscriber's type.
     *
     * @param subscriber
     *            The subscriber to retrieve provision configs for.
     * @param service
     *            Service to retrieve provision configs from.
     * @return HLR provision configs of the service corresponding to the subscriber's
     *         type.
     */
    public static String getServiceProvisionConfigs(final Subscriber subscriber, final com.redknee.app.crm.bean.Service service)
    {
        String hlrCmds = null;
        if (service != null)
        {
            if (subscriber.isPostpaid())
            {
                hlrCmds = service.getProvisionConfigs();
            }
            else if (subscriber.isPrepaid())
            {
                hlrCmds = service.getPrepaidProvisionConfigs();
            }
        }
        return hlrCmds;
    }

    /**
     * Retrieves the resume configs of the service based on the subscriber's type.
     *
     * @param subscriber
     *            The subscriber to retrieve resume configs for.
     * @param service
     *            Service to retrieve resume configs from.
     * @return HLR resume configs of the service corresponding to the subscriber's
     *         type.
     */
    public static String getServiceResumeConfigs(final Subscriber subscriber, final com.redknee.app.crm.bean.Service service)
    {
        String hlrCmds = null;
        if (service != null)
        {
            if (subscriber.isPostpaid())
            {
                hlrCmds = service.getResumeConfigs();
            }
            else if (subscriber.isPrepaid())
            {
                hlrCmds = service.getPrepaidResumeConfigs();
            }
        }
        return hlrCmds;
    }

    /**
     * Retrieves the suspend configs of the service based on the subscriber's type.
     *
     * @param subscriber
     *            The subscriber to retrieve suspend configs for.
     * @param service
     *            Service to retrieve suspend configs from.
     * @return HLR suspend configs of the service corresponding to the subscriber's
     *         type.
     */
    public static String getServiceSuspendConfigs(final Subscriber subscriber, final com.redknee.app.crm.bean.Service service)
    {
        String hlrCmds = null;
        if (service != null)
        {
            if (subscriber.isPostpaid())
            {
                hlrCmds = service.getSuspendConfigs();
            }
            else if (subscriber.isPrepaid())
            {
                hlrCmds = service.getPrepaidSuspendConfigs();
            }
        }
        return hlrCmds;
    }
    
    /**
     * Retrieves the provision configs of the service based on the subscriber's type.
     *
     * @param subscriber
     *            The subscriber to retrieve provision configs for.
     * @param service
     *            Service to retrieve provision configs from.
     * @return HLR provision configs of the service corresponding to the subscriber's
     *         type.
     */
    public static String getServiceUnProvisionConfigs(final Subscriber subscriber, final com.redknee.app.crm.bean.Service service)
    {
        String hlrCmds = null;
        if (service != null)
        {
            if (subscriber.isPostpaid())
            {
                hlrCmds = service.getUnprovisionConfigs();
            }
            else if (subscriber.isPrepaid())
            {
                hlrCmds = service.getPrepaidUnprovisionConfigs();
            }
        }
        return hlrCmds;
    }
    

    
    public static BlacklistWhitelistTemplateServiceExtension getBlacklistWhitelistTemplateExtension(final Context ctx, final long extensionId,
            final CallingGroupTypeEnum callingGroupType) throws HomeException
    {
        And filter = new And();
        filter.add(new EQ(BlacklistWhitelistTemplateServiceExtensionXInfo.CALLING_GROUP_TYPE, callingGroupType));
        filter.add(new EQ(BlacklistWhitelistTemplateServiceExtensionXInfo.CALLING_GROUP_ID, Long.valueOf(extensionId)));

        return  HomeSupportHelper.get(ctx).findBean(ctx,
                BlacklistWhitelistTemplateServiceExtension.class, filter);
    }
    

    public static Service getServiceForBlacklistWhitelistTemplate(final Context ctx, final long extensionId,
            final CallingGroupTypeEnum callingGroupType) throws HomeException
    {
        return getBlacklistWhitelistTemplateExtension(ctx, extensionId, callingGroupType).getService(ctx);
    }
    
}
