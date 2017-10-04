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
package com.trilogy.app.crm.ff;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.BlacklistWhitelistTemplate;
import com.trilogy.app.crm.bean.BlacklistWhitelistTypeEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.extension.service.ServiceExtension;
import com.trilogy.app.crm.home.BlackListWhitelistServiceHome;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiConstants;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrCugListHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugListHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrPeerMsisdn;
import com.trilogy.app.ff.ecare.rmi.TrPlp;
import com.trilogy.app.ff.ecare.rmi.TrPlpList;
import com.trilogy.app.ff.ecare.rmi.TrPlpListHolder;
import com.trilogy.app.ff.ecare.rmi.TrPlpListHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberPlp;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfile2;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfile2Holder;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfile2HolderImpl;
import com.trilogy.model.app.ff.param.ParameterID;
import com.trilogy.model.app.ff.param.ParameterSetHolderImpl;

/**
 * @author chandrachud.ingale
 * @since 9.6
 */
public class BlacklistWhitelistPLPSupport
{

    /**
     * Gets the list of Blacklist/Whitelist MSISDNs associated with the given subscriber.
     * 
     * @param context
     *            The operating context.
     * @param oldSubscriber
     *            The subscriber for which to get the PLP MSISDNs.
     */
    public static Map<BlacklistWhitelistTemplate, Set<String>> getBlacklistWhitelistPlpMsisdnList(final Context context, final String msisdn)
            throws FFEcareException
    {
        FFECareRmiService service = FFClosedUserGroupSupport.getFFRmiService(context,
                BlacklistWhitelistPLPSupport.class);
        if (service == null)
        {
            throw new FFEcareException("Service connection is down, failed to get Blacklist/Whitelist PLP list.",
                    ExternalAppSupport.NO_CONNECTION);
        }

        try
        {
            final TrSubscriberProfile2Holder profileHolder = new TrSubscriberProfile2HolderImpl();
            final TrPlpListHolder trPlpListHolder = new TrPlpListHolderImpl();
            TrCugListHolder trCugHolder = new TrCugListHolderImpl();

            final int result = service.getSubWithDetails(msisdn, profileHolder, trPlpListHolder, trCugHolder);

            if (result == FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                Map<BlacklistWhitelistTemplate, Set<String>> blwlMsisdn = new HashMap<BlacklistWhitelistTemplate, Set<String>>();
                final TrSubscriberProfile2 profile = profileHolder.getValue();

                if (profile != null)
                {
                    List<BlacklistWhitelistTemplate> blwlTemplatesSubscribed = getBlacklistWhitelistPLPId(trPlpListHolder.getValue(), context);
                    for (int n = 0; n < profile.subPlpList.subPlp.length; ++n)
                    {
                        for (BlacklistWhitelistTemplate blwlTemplate : blwlTemplatesSubscribed)
                        {
                            if (blwlTemplate.getIdentifier() == profile.subPlpList.subPlp[n].plpId)
                            {
                                final TrSubscriberPlp plpList = profile.subPlpList.subPlp[n];
                                blwlMsisdn.put(blwlTemplate, getPlpMsisdn(plpList));
                            }
                        }
                    }
                }
                return blwlMsisdn;
            }
            else if (result != FFECareRmiConstants.FF_ECARE_PROFILE_NOT_FOUND)
            {
                throw new FFEcareException(
                        "Failed to look up subscriber's blacklist whitelist PLP information. (msisdn=" + msisdn + ")"
                                + " Service returned "
                                + CallingGroupSupport.getDisplayMessageForReturnCode(context, result), result);
            }
        }
        catch (final RemoteException exception)
        {
            throw new FFEcareException("Failed to look up subscriber's blacklist whitelist PLP information." + msisdn,
                    ExternalAppSupport.REMOTE_EXCEPTION, exception);
        }
        return null;

    }


    /**
     * @param value
     * @return
     */
    private static List<BlacklistWhitelistTemplate> getBlacklistWhitelistPLPId(TrPlpList value, Context context)
    {
        List<BlacklistWhitelistTemplate> blwlPlpList = new ArrayList<BlacklistWhitelistTemplate>();
        for (TrPlp trPlp : value.trPlpList)
        {
            if (trPlp.plpType == BlacklistWhitelistTypeEnum.BLACKLIST_INDEX
                    || trPlp.plpType == BlacklistWhitelistTypeEnum.WHITELIST_INDEX)
            {
                BlacklistWhitelistTemplate blwlTemplate = BlackListWhitelistServiceHome.populateBlWl(trPlp, context);
                blwlPlpList.add(blwlTemplate);
            }
        }
        return blwlPlpList;
    }


    /**
     * @param trSubscriberPlp
     * @return
     */
    public static Set<String> getPlpMsisdn(final TrSubscriberPlp trSubscriberPlp)
    {
        final Set<String> ret = new HashSet<String>();
        for (int n = 0; n < trSubscriberPlp.otherMsisdns.length; ++n)
        {
            ret.add(trSubscriberPlp.otherMsisdns[n].msisdn);
        }
        return ret;
    }


    /**
     * Gets the Blacklist/Whitelist template Ids from subscriber services.
     * 
     * @param context
     *            The operating context.
     * @param subscriberServices
     *            A Collection of SubscriberServices.
     */
    public static List<Long> getBlacklistWhitelistIdFromSubscriberServices(final Context context,
            final Collection<SubscriberServices> subscriberServices) throws HomeException
    {
        final List<Long> ret = new ArrayList<Long>();
        final Collection<Service> services = SubscriberServicesSupport.getServiceFromSubscriberServices(context,
                subscriberServices);

        for (final Iterator<Service> it = services.iterator(); it.hasNext();)
        {
            final Service service = (Service) it.next();
            if (service.getType() == ServiceTypeEnum.GENERIC)
            {
                ServiceExtension serviceExtension = ExtensionSupportHelper.get(context).getExtension(context, service,
                        ServiceExtension.class);

                if (serviceExtension != null
                        && (serviceExtension instanceof BlacklistWhitelistTemplateServiceExtension))
                {
                    BlacklistWhitelistTemplateServiceExtension blwlExtension = (BlacklistWhitelistTemplateServiceExtension) serviceExtension;
                    ret.add(Long.valueOf(blwlExtension.getCallingGroupId()));
                }
            }
        }

        return ret;
    }


    /**
     * @param ctx
     * @param service
     * @return
     */
    private static BlacklistWhitelistTemplateServiceExtension getBlWlTemplateSrvExtension(Context ctx, Service service)
    {
        return ExtensionSupportHelper.get(ctx).getExtension(ctx, service,
                BlacklistWhitelistTemplateServiceExtension.class);
    }


    public static void addPLP(final Context ctx, final SubscriberServices subscriberServices, final Service service)
            throws HomeException, FFEcareException
    {
        BlacklistWhitelistTemplateServiceExtension blwlTemplateSrvExtension = getBlWlTemplateSrvExtension(ctx, service);
        addPLP(ctx, subscriberServices, blwlTemplateSrvExtension.getCallingGroupId(), blwlTemplateSrvExtension.getGlCode());
    }


    public static void addPLP(final Context ctx, final SubscriberServices subscriberServices, final long blwlTemplateId, final String glCode)
            throws HomeException, FFEcareException
    {

        final FFECareRmiService client = FFClosedUserGroupSupport.getFFRmiService(ctx,
                BlacklistWhitelistPLPSupport.class);
        if (client == null)
        {
            throw new FFEcareException(
                    "F&F connection is down, failed to add Blacklist Whitelist PLP for subscriber : "
                            + subscriberServices.getSubscriberId() + ", template id : " + blwlTemplateId,
                    ExternalAppSupport.NO_CONNECTION);
        }

        final Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx,
                subscriberServices.getSubscriberId());

        final TrPeerMsisdn[] peerMsisdn = new TrPeerMsisdn[0];

        try
        {
            FFParamSetBuilder holder = new FFParamSetBuilder(ctx);
            holder.add(ParameterID.PARAM_NUM_FORCE_PLP_MSISDN_ADD, -1);
            holder.add(ParameterID.PARAM_ALLOW_INITIAL_PLP_MEMBER_REMOVAL, true);
            holder.add(ParameterID.PARAM_DEACTIVATE_PLP, false);
             holder.add(ParameterID.CRM_GLCODE, glCode);

            int result = client.updatePLPForSub(subscriber.getMSISDN(), blwlTemplateId, peerMsisdn,
                    holder.getParameters(), new ParameterSetHolderImpl());
            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                String msg = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
                throw new FFEcareException("Failed to add blacklist whitelist PLP " + blwlTemplateId
                        + " to subscriber " + subscriber.getId() + ": " + msg, result);
            }
        }
        catch (final RemoteException throwable)
        {
            throw new FFEcareException("Failed to add blacklist whitelist PLP " + blwlTemplateId + " for subscriber "
                    + subscriber.getId(), ExternalAppSupport.REMOTE_EXCEPTION, throwable);
        }
    }


    public static void deletePLP(final Context ctx, final SubscriberServices subscriberServices, final Service service)
            throws FFEcareException, HomeException
    {
        BlacklistWhitelistTemplateServiceExtension blwlTemplateSrvExtension = getBlWlTemplateSrvExtension(ctx, service);
        deletePLP(ctx, subscriberServices, blwlTemplateSrvExtension.getCallingGroupId());
    }


    public static void deletePLP(final Context ctx, final SubscriberServices subscriberServices,
            final long blwlTemplateId) throws FFEcareException, HomeException
    {
        final FFECareRmiService client = FFClosedUserGroupSupport.getFFRmiService(ctx,
                BlacklistWhitelistPLPSupport.class);
        if (client == null)
        {
            throw new FFEcareException(
                    "F&F connection is down, failed to delete Blacklist Whitelist PLP for subscriber : "
                            + subscriberServices.getSubscriberId() + ", template id : " + blwlTemplateId,
                    ExternalAppSupport.NO_CONNECTION);
        }

        final Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx,
                subscriberServices.getSubscriberId());

        final TrPeerMsisdn[] peerMsisdn = new TrPeerMsisdn[1];
        peerMsisdn[0] = new TrPeerMsisdn(subscriber.getMSISDN());

        try
        {
            int result = client.deletePLPForSub(subscriber.getMSISDN(), blwlTemplateId);
            if (result != FFECareRmiConstants.FF_ECARE_PLP_NOT_FOUND && result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                throw new FFEcareException("Unable to delete blacklist whitelist PLP " + blwlTemplateId
                        + " for subscriber " + subscriber.getId(), result);
            }
        }
        catch (final RemoteException throwable)
        {
            throw new FFEcareException("Unable to delete blacklist whitelist PLP " + blwlTemplateId
                    + " for subscriber " + subscriber.getId(), ExternalAppSupport.REMOTE_EXCEPTION, throwable);
        }
    }
    
    
    public static void suspendOrResume(Context ctx, Subscriber subscriber, com.redknee.app.crm.bean.Service service, boolean suspend) throws FFEcareException, HomeException
    {
        
        BlacklistWhitelistTemplateServiceExtension blwlServiceExtension = ExtensionSupportHelper.get(ctx).getExtension(
                ctx, (ExtensionAware) service, BlacklistWhitelistTemplateServiceExtension.class);
        if (blwlServiceExtension == null)
        {
            return;
        }
        
        final FFECareRmiService client = FFClosedUserGroupSupport.getFFRmiService(ctx, BlacklistWhitelistPLPSupport.class);
        if (client == null)
        {
            throw new FFEcareException(
                    "F&F connection is down, failed to suspend/resume Blacklist Whitelist PLP for subscriber : "
                            + subscriber.getId() + ", template id : " + blwlServiceExtension.getCallingGroupId(),
                    ExternalAppSupport.NO_CONNECTION);
        }

        final TrPeerMsisdn[] peerMsisdn = new TrPeerMsisdn[0];
        try
        {
            FFParamSetBuilder holder = new FFParamSetBuilder(ctx);
            holder.add(ParameterID.PARAM_DEACTIVATE_PLP, suspend);
            
            int result = client.updatePLPForSub(subscriber.getMSISDN(), blwlServiceExtension.getCallingGroupId(), peerMsisdn,
                    holder.getParameters(), new ParameterSetHolderImpl());
            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                String msg = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
                throw new FFEcareException("Failed to resume/suspend Blacklist Whitelist PLP" + blwlServiceExtension.getCallingGroupId()
                        + " for subscriber " + subscriber.getId() + ": " + msg, result);
            }
        }
        catch (final RemoteException throwable)
        {
            throw new FFEcareException("Failed to resume/suspend Blacklist Whitelist PLP" + blwlServiceExtension.getCallingGroupId()
                    + " for subscriber "
                    + subscriber.getId(), ExternalAppSupport.REMOTE_EXCEPTION, throwable);
        }
    }
}
