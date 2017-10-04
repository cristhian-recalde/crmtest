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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.PersonalListPlanXInfo;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtensionXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.URCSPromotionAuxSvcExtensionXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.VPNAuxSvcExtension;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.vpn.SubscriberAuxiliaryVpnServiceSupport;
import com.trilogy.app.urcs.promotion.Promotion;
import com.trilogy.app.urcs.promotion.PromotionStatus;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.util.snippet.log.Logger;


/**
 * Provides Utility methods for Auxiliary Services.
 *
 * @author shailesh.kushwaha@redknee.com
 */

public final class AuxiliaryServiceSupport
{

    /**
     * Creates a new <code>AuxiliaryServiceSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private AuxiliaryServiceSupport()
    {
        // empty
    }


    /**
     * Returns the auxiliary service object with the provided identifier.
     *
     * @param ctx
     *            The operating context.
     * @param id
     *            Auxiliary service identifier.
     * @return The identified auxiliary service.
     * @throws HomeException
     *             Thrown if there are problems looking up the auxiliary service.
     */
    public static AuxiliaryService getAuxiliaryService(final Context ctx, final long id) throws HomeException
    {
        return HomeSupportHelper.get(ctx).findBean(
                ctx, 
                AuxiliaryService.class, 
                Long.valueOf(id));
    }

    /**
     * return Auxiliary Service object
     * @param exsisting auxiliary service ID Home information in the context.
     * 
     * @deprecated Use {@link #getAuxiliaryService(Context, long)} instead.
     */
    @Deprecated
    public static AuxiliaryService getAuxiliaryServicById(final Context ctx, final long auxSvcId)
        throws HomeException
    {
        return getAuxiliaryService(ctx, auxSvcId);
    }

    
    /**
     * Returns the auxiliary service object with the provided identifier.
     *
     * @param context
     *            The operating context.
     * @param home
     *            Auxiliary service home.
     * @param id
     *            Auxiliary service identifier.
     * @return The identified auxiliary service.
     * @throws HomeException
     *             Thrown if there are problems looking up the auxiliary service.
     */
    public static AuxiliaryService getAuxiliaryService(final Context context, final Home home, final long id)
        throws HomeException
    {
        Context sCtx = context.createSubContext().put(AuxiliaryServiceHome.class, home);
        return getAuxiliaryService(sCtx, id);
    }

    public static Map<Long, AuxiliaryService> getPromotionAuxiliaryServicesMap(final Context ctx, final int spid,
            final PromotionStatus[] promotionsStatus)
    {
        final Map<Long, AuxiliaryService> result = new HashMap<Long, AuxiliaryService>();
        final Set<Long> options = new HashSet<Long>();
        for (int i = 0; i < promotionsStatus.length; i++)
        {
            final Promotion promotion = promotionsStatus[i].promotion;
            options.add(Long.valueOf(promotion.optionTag));
        }
        And condition = new And();
        condition.add(new In(URCSPromotionAuxSvcExtensionXInfo.SERVICE_OPTION, options));
        
        try
        {
            Collection<URCSPromotionAuxSvcExtension> extensions = HomeSupportHelper.get(ctx).getBeans(ctx, URCSPromotionAuxSvcExtension.class, condition);
            
            if (extensions!=null && extensions.size()>0)
            {
                Set<Long> identifiers = new HashSet<Long>();
                for (URCSPromotionAuxSvcExtension extension : extensions)
                {
                    identifiers.add(extension.getAuxiliaryServiceId());
                }
                
                condition = new And();
                condition.add(new EQ(AuxiliaryServiceXInfo.SPID, Integer.valueOf(spid)));
                condition.add(new EQ(AuxiliaryServiceXInfo.TYPE, AuxiliaryServiceTypeEnum.URCS_Promotion));
                condition.add(new In(AuxiliaryServiceXInfo.IDENTIFIER, identifiers));
    
                final Collection<AuxiliaryService> auxSrvs = HomeSupportHelper.get(ctx).getBeans(ctx, AuxiliaryService.class, condition);
                for (AuxiliaryService service : auxSrvs)
                {
                    long serviceOption = URCSPromotionAuxSvcExtension.DEFAULT_SERVICEOPTION;
                    URCSPromotionAuxSvcExtension urcsPromotionAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, URCSPromotionAuxSvcExtension.class);
                    if (urcsPromotionAuxSvcExtension!=null)
                    {
                        serviceOption = urcsPromotionAuxSvcExtension.getServiceOption();
                    }
                    else
                    {
                        LogSupport.minor(ctx, AuxiliaryServiceSupport.class,
                                "Unable to find required extension of type '" + URCSPromotionAuxSvcExtension.class.getSimpleName()
                                        + "' for auxiliary service " + service.getIdentifier());
                    }
                    result.put(Long.valueOf(serviceOption), service);
                }
            }
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, AuxiliaryServiceSupport.class, "Unable to retreive Auxiliary Services for Service Options", e);
        }

        return result;
    }

    /**
     * Returns a collection of all auxiliary services of service provider.
     *
     * @param context
     *            the operating context.
     * @param spid
     *            Service provider identifier.
     * @return A collection of all auxiliary services of the provided service provider.
     * @throws HomeException
     *             Thrown if there are problems looking up the auxiliary services.
     */
    public static Collection<AuxiliaryService> getAuxiliaryServicesBySpid(final Context context, final int spid) throws HomeException
    {
        return HomeSupportHelper.get(context).getBeans(
                context, 
                AuxiliaryService.class, 
                new EQ(AuxiliaryServiceXInfo.SPID, spid));
    }

    
    /**
     * Gets all the available AuxiliaryServices for the given spid. An
     * AuxiliaryService is available if it has the same SPID as the subscriber.
     *
     * @param context
     *            he operating context.
     * @param spid
     *            integer value representing SPID of the subscriber
     * @return java.util.Collection object holding All the available AuxiliaryServices for
     *         the given subscriber.
     */
    public static Collection<AuxiliaryService> getAllAvailableAuxiliaryServices(final Context context, final int spid)
    {
        try
        {
            final And filter = new And();
            filter.add(new EQ(AuxiliaryServiceXInfo.SPID, Integer.valueOf(spid)));
            filter.add(new NEQ(AuxiliaryServiceXInfo.STATE, AuxiliaryServiceStateEnum.CLOSED));
            if (!AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context))
            {
                filter.add(new NEQ(AuxiliaryServiceXInfo.TYPE, AuxiliaryServiceTypeEnum.AdditionalMsisdn));
            }
            final Collection<AuxiliaryService> services = 
                HomeSupportHelper.get(context).getBeans(
                        context, AuxiliaryService.class, 
                        filter,
                        true, AuxiliaryServiceXInfo.TYPE, AuxiliaryServiceXInfo.NAME, AuxiliaryServiceXInfo.IDENTIFIER);

            return services;
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(AuxiliaryServiceSupport.class, "Failed to look-up available auxiliary services.", t).log(context);
            return new ArrayList<AuxiliaryService>();
        }
    }


    /**
     * Returns the personal list plan with the provided identifier and service provider
     * identifier.
     *
     * @param ctx
     *            The operating Context
     * @param plpId
     *            The PLP ID for which the corresponding PLP is required
     * @param spid
     *            the SPID to which the PLP must belong
     * @return PersonalListPlan object of the the submitted plpId
     */
    public static PersonalListPlan getPlpFromPlpIdBySpid(final Context ctx, final long plpId, final int spid)
    {
        PersonalListPlan plp = null;
        try
        {
            And filter = new And();
            filter.add(new EQ(PersonalListPlanXInfo.ID, plpId));
            filter.add(new EQ(PersonalListPlanXInfo.SPID, spid));
            filter.add(new Limit(1));
            
            Collection<PersonalListPlan> plps = HomeSupportHelper.get(ctx).getBeans(ctx, PersonalListPlan.class, filter);
            if (plps.size() > 0)
            {
                plp = plps.iterator().next();
            }
        }
        catch (final HomeException he)
        {
            new MajorLogMsg(AuxiliaryServiceSupport.class, "Problem encountered while trying to fetch PLP with ID"
                + plpId + ".", he).log(ctx);
        }
        return plp;
    }


    /**
     * Returns the auxiliary service associated with the provided calling group.
     *
     * @param ctx
     *            The operating context.
     * @param callingGroupIdentifier
     *            Calling group identifier.
     * @param cgt
     *            Type of calling group.
     * @param spid
     *            Service provider identifier.
     * @return The auxiliary service associated with the provided calling group.
     */
    public static AuxiliaryService getAuxServiceByCallingGroupIdentifier(final Context ctx,
        final long callingGroupIdentifier, final CallingGroupTypeEnum cgt, final int spid)
    {
        AuxiliaryService auxSvc = null;
        try
        {
            And filter = new And();
            filter.add(new EQ(CallingGroupAuxSvcExtensionXInfo.CALLING_GROUP_IDENTIFIER, callingGroupIdentifier));
            filter.add(new EQ(CallingGroupAuxSvcExtensionXInfo.CALLING_GROUP_TYPE, cgt));
            
            Collection<CallingGroupAuxSvcExtension> extensions = HomeSupportHelper.get(ctx).getBeans(ctx, CallingGroupAuxSvcExtension.class, filter);
            
            if (extensions!=null && extensions.size()>0)
            {
                Set<Long> identifiers = new HashSet<Long>();
                for (CallingGroupAuxSvcExtension extension : extensions)
                {
                    identifiers.add(extension.getAuxiliaryServiceId());
                }
                
                And condition = new And();
                condition.add(new EQ(AuxiliaryServiceXInfo.SPID, Integer.valueOf(spid)));
                condition.add(new In(AuxiliaryServiceXInfo.IDENTIFIER, identifiers));
    
                return HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, condition);
            }
            
            return null;
        }
        catch (final HomeException he)
        {
            new MajorLogMsg(AuxiliaryServiceSupport.class,
                    "Problem encountered while AuxiliaryService with CallingGroupIdentifier " + callingGroupIdentifier
                    + ".", he).log(ctx);
        }
        return auxSvc;
    }


    /**
     * Returns the personal list plan with the provided identifier.
     *
     * @param ctx
     *            The operating Context
     * @param plpId
     *            The PLP ID for which the corresponding PLP is required
     * @return PersonalListPlan object of the the submitted plpId
     */
    public static PersonalListPlan getPlpFromPlpId(final Context ctx, final long plpId)
    {
        PersonalListPlan plp = null;
        try
        {
            plp = HomeSupportHelper.get(ctx).findBean(ctx, PersonalListPlan.class, plpId);
        }
        catch (final HomeException he)
        {
            new MajorLogMsg(AuxiliaryServiceSupport.class, "Problem encountered while trying to fetch PLP with ID"
                    + plpId + ".", he).log(ctx);
        }
        return plp;
    }
    
    /**
     * verifies if the one time charge has been charged already.
     *
     * @param ctx
     *            the operating context
     * @param subscriber
     *            the previous subscriber state
     * @param service
     *            the service to be charged
     * @return true if already charged
     */
    public static boolean oneTimeChargeExists(final Context ctx,  final Subscriber subscriber,
        final SubscriberAuxiliaryService association, final AuxiliaryService service) throws HomeException
    {
        boolean isDuplicate = false;

        if (association!=null && service.getChargingModeType() == ServicePeriodEnum.ONE_TIME)
        {
            isDuplicate = SubscriberSubscriptionHistorySupport.isLastChargedBetween(ctx, subscriber.getId(), ChargedItemTypeEnum.AUXSERVICE, association, association.getStartDate(), association.getEndDate());
            
        }
        return isDuplicate;
    }
    

    public static AuxiliaryService getAuxiliaryServiceByAdjustmentType(Context ctx, int adjustmentType) throws HomeException
    {
        Home home = (Home) ctx.get(AuxiliaryServiceHome.class);
        if (home==null)
        {
            throw new HomeException("Startup error: aux service home not found in context");
        }
        
        return (AuxiliaryService) home.find(ctx, new EQ(AuxiliaryServiceXInfo.ADJUSTMENT_TYPE, Integer.valueOf(adjustmentType)));
    }
    
    public static boolean supportsPreDating(Context ctx, com.redknee.app.crm.bean.AuxiliaryService service)
    {
        if (service == null || service.getType().equals(AuxiliaryServiceTypeEnum.Vpn) ||
                service.getType().equals(AuxiliaryServiceTypeEnum.HomeZone) ||
                service.getType().equals(AuxiliaryServiceTypeEnum.AdditionalMsisdn) ||
                service.getType().equals(AuxiliaryServiceTypeEnum.PRBT) ||
                service.getType().equals(AuxiliaryServiceTypeEnum.Discount) ||
                service.getType().equals(AuxiliaryServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY) 
                )
        {
            return false;
        }
        else
        {
            return true;
        }
    }


}
