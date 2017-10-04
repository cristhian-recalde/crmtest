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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.PersonalListPlanHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.poller.event.FnFSelfCareProcessor;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiConstants;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrPeerMsisdn;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberPlp;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfile2;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfile2Holder;
import com.trilogy.app.ff.ecare.rmi.TrSubscriberProfile2HolderImpl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.model.app.ff.param.ParameterID;
import com.trilogy.model.app.ff.param.ParameterSetHolderImpl;

public abstract class PersonalListPlanSupport
{
    /**
     * Updates the set of MSISDNs in the subscriber's PersonalListPlan in the Friends and Family application.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber to update.
     * @exception HomeException
     *                Thrown if there are problems communicating with the services stored in the context.
     */
    public static void updatePersonalListPlanMSISDNs(final Context ctx, final Subscriber subscriber)
        throws FFEcareException
    {
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final Map newPlps = subscriber.getPersonalListPlanEntries();
        String message = "";
        int lastCode = -1;

        if (oldSub != null)
        {
            if (isMsisdnChanged(oldSub, subscriber)) // remove everything and add PLPs associated with new MSISDN
            {
                removePersonalListPlan(ctx, oldSub);

                if (newPlps != null && newPlps.size() > 0)
                {
                    for (final Iterator it = newPlps.keySet().iterator(); it.hasNext();)
                    {
                        final Long plpId = (Long) it.next();
                        updatePersonalListPlanMSISDNs(ctx, subscriber, plpId.longValue(), (Set) newPlps.get(plpId));
                    }
                }
            }

            final Map oldPlps = oldSub.getPersonalListPlanEntries();

            for (final Iterator i = oldPlps.keySet().iterator(); i.hasNext();)
            {
                final Long plpId = (Long) i.next();
                try
                {
                    updatePersonalListPlanMSISDNs(ctx, subscriber, plpId.longValue(), (Set) oldPlps.get(plpId),
                            (Set) newPlps.get(plpId));
                }
                catch (FFEcareException e)
                {
                    message.concat(e.getMessage());
                    lastCode = e.getResultCode();
                }
            }
        }

        if (!message.equals(""))
        {
            throw new FFEcareException(message, lastCode);
        }
    }

    private static boolean isMsisdnChanged(final Subscriber oldSub, final Subscriber newSub)
    {
        if (oldSub == null || newSub == null)
        {
            throw new IllegalArgumentException("Subscriber cannot be null.");
        }

        return !oldSub.getMSISDN().equals(newSub.getMSISDN());
    }

    /**
     * Removes the PLP associated with the subscriber within the Friends and family service.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to remove a PLP.
     * @exception HomeException
     *                Thrown if there are problems communicating with the services stored in the context.
     */
    public static void removePersonalListPlan(final Context ctx, final Subscriber subscriber) throws FFEcareException
    {
        if (subscriber == null)
        {
            throw new IllegalArgumentException("Subscriber cannot be null.");
        }

        final Map plpEntries = subscriber.getPersonalListPlanEntries();
        if (plpEntries == null || plpEntries.size() == 0)
        {
            return;
        }

        final FFECareRmiService service = FFClosedUserGroupSupport.getFFRmiService(ctx, PersonalListPlanSupport.class);

        if (service == null)
        {
            LogSupport.major(ctx, PersonalListPlanSupport.class, "FFECareRmiService is not available.");
            throw new FFEcareException("No F&F Service in the context", ExternalAppSupport.NO_CONNECTION);
        }

        try
        {
            if (ctx.has(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER))
            {
                // In case of FnFSelfCare Poller, the subscriber's FnF profile would already be removed from
                // CUG/PLP,hence no need to make a call to ECP. [amit]
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, PersonalListPlanSupport.class,
                            "The context under use has the key 'FnFPoller',call to ECP is bypassed");
                }
                return;
            }

            final int result = service.deletePLPForSub(subscriber.getMSISDN());

            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                throw new FFEcareException("Friends and Family service returned "
                        + CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result)
                        + " while attempting to delete personal list plan.", result);
            }
        }
        catch (final RemoteException exception)
        {
            throw new FFEcareException("Failed to remove personal list plans from subscriber "
                    + subscriber.getId(), ExternalAppSupport.REMOTE_EXCEPTION, exception);
        }
    }

    public static void validateMaxPlpNum(final Context ctx, final Subscriber subscriber) throws FFEcareException
    {
        final Map plpEntries = subscriber.getPersonalListPlanEntries(ctx);

        if (plpEntries != null && plpEntries.size() > 0)
        {
            int maxPlpNum = 0;

            final Home spHome = (Home) ctx.get(CRMSpidHome.class);
            try
            {
                final CRMSpid sp = (CRMSpid) spHome.find(ctx, Integer.valueOf(subscriber.getSpid()));
                maxPlpNum = sp.getMaxPlpPerSubscriber();
            }
            catch (Exception e)
            {
                throw new FFEcareException("Failed to get the Max PLP Number Per Subscriber."
                        + " PLP Number validation cannot be performed.", ExternalAppSupport.UNKNOWN, e);
            }

            if (plpEntries.size() > maxPlpNum)
            {
                throw new FFEcareException("Subscriber cannot have more than " + maxPlpNum + " Personal Lists.", FFECareRmiConstants.FF_ECARE_PLP_MEMBER_ADD_EXCEEDED);
            }
        }
    }

    /**
     * exclude some invalid updating case.
     *
     * @param ctx
     * @param subscriber
     * @param plpId
     * @param oldMsisdns
     * @param msisdns
     * @throws FFEcareException
     */
    public static void updatePersonalListPlanMSISDNs(final Context ctx,
            final Subscriber subscriber,
            final long plpId,
            final Set oldMsisdns,
            final Set msisdns)
        throws FFEcareException
    {
        final Collection ownedPLP = subscriber.getPersonalListPlan(ctx);
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        if (ownedPLP.contains(Long.valueOf(plpId)))
        {
            // not update unprovisioned, GUI design issue, should not happen at same time.
            if (!oldMsisdns.equals(msisdns))
            {
            	if(!oldSub.getMsisdn().equals(subscriber.getMsisdn()))//In case of api's change msisdn request new plp msisdn list is empty. Adding old msisdn's plp list. 
            		msisdns.addAll(oldMsisdns);
            		
            	updatePersonalListPlanMSISDNs(ctx, subscriber, plpId, msisdns);
            
            }
        }
    }

    /**
     * Updates the set of MSISDNs in the subscriber's PersonalListPlan in the Friends and Family application.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber to update.
     * @param msisdns
     *            The set of MSISDNs in the subscriber's PLP.
     * @exception HomeException
     *                Thrown if there are problems communicating with the services stored in the context.
     */
    public static void updatePersonalListPlanMSISDNs(final Context ctx,
            final Subscriber subscriber,
            final long plpId,
            final Set msisdns) throws FFEcareException
    {
        FFECareRmiService service = null;
        service = FFClosedUserGroupSupport.getFFRmiService(ctx, PersonalListPlanSupport.class);

        if (service == null)
        {
            throw new FFEcareException(" F&F connection is down, fail to up update PLP to F&F for subscriber"
                    + subscriber.getId() + " plp " + plpId, ExternalAppSupport.NO_CONNECTION);
        }

        final TrPeerMsisdn[] peerMsisdn = new TrPeerMsisdn[msisdns.size()];
        final Iterator msisdnIterator = msisdns.iterator();
        for (int n = 0; n < peerMsisdn.length; ++n)
        {
            peerMsisdn[n] = new TrPeerMsisdn((String) msisdnIterator.next());
        }

        try
        {
			FFParamSetBuilder holder = new FFParamSetBuilder(ctx);
			holder.add(ParameterID.PARAM_NUM_FORCE_PLP_MSISDN_ADD, -1);
			holder.add(ParameterID.PARAM_ALLOW_INITIAL_PLP_MEMBER_REMOVAL, true);
			
            final int result = service.updatePLPForSub(subscriber.getMSISDN(), plpId, peerMsisdn,
                    holder.getParameters(), new ParameterSetHolderImpl());

            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                String msg = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
                throw new FFEcareException("Unable to update PLP for subscriber " + subscriber.getId()
                        + ": " + msg, result);
            }
        }
        catch (final RemoteException throwable)
        {
            throw new FFEcareException("fail to up update PLP to F&F for subscriber" + subscriber.getId()
                    + " plp " + plpId + " reason " + throwable.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, throwable);
        }
    }

    /**
     * Gets the list of personal plan MSISDNs associated with the given subscriber.
     *
     * @param context
     *            The operating context.
     * @param oldSubscriber
     *            The subscriber for which to get the PLP MSISDNs.
     */
    public static Map getPersonalListPlanMSISDNs(
            final Context context,
            final String msisdn)
        throws FFEcareException
    {
        final Map ret = new HashMap();

        FFECareRmiService service = null;
        service = FFClosedUserGroupSupport.getFFRmiService(context, PersonalListPlanSupport.class);

        if (service == null)
        {
            throw new FFEcareException(" F&F connection is down, fail to get PLP list", ExternalAppSupport.NO_CONNECTION);
        }

        try
        {
            final TrSubscriberProfile2Holder profileHolder = new TrSubscriberProfile2HolderImpl();

            final int result = service.getSub(msisdn, profileHolder);

            if (result == FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                final TrSubscriberProfile2 profile = profileHolder.getValue();

                if (profile != null)
                {
                    for (int n = 0; n < profile.subPlpList.subPlp.length; ++n)
                    {
                        final TrSubscriberPlp plpList = profile.subPlpList.subPlp[n];
                        ret.put(Long.valueOf(plpList.plpId), getPlpMsisdn(plpList));
                    }
                }
            }
            else if (result != FFECareRmiConstants.FF_ECARE_PROFILE_NOT_FOUND)
            {
                throw new FFEcareException("Failed to look up subscriber's personal list plan information. (msisdn="
                        + msisdn + ")" + " Service returned "
                        + CallingGroupSupport.getDisplayMessageForReturnCode(context, result), result);
            }
        }
        catch (final RemoteException exception)
        {
            throw new FFEcareException(
                    "Failed to look up subscriber's personal list plan information." + msisdn, ExternalAppSupport.REMOTE_EXCEPTION,
                    exception);
        }

        return ret;
    }

    /**
     * convert return from F&F to CRM data type
     *
     * @param list
     * @return
     */
    public static Set getPlpMsisdn(final TrSubscriberPlp list)
    {
        final Set ret = new TreeSet();
        for (int n = 0; n < list.otherMsisdns.length; ++n)
        {
            ret.add(list.otherMsisdns[n].msisdn);
        }
        return ret;
    }

    /**
     * Gets the PersonalListPlan selected in the given Collection of SubscriberAuxiliaryServices. If more than one PLP
     * is selected, then the PLP with the greatest maximum subscriber count is returned. If more than one such PLP
     * exists, then the PLP returned is non-deterministic.
     *
     * @param context
     *            The operating context.
     * @param associations
     *            A Collection of SubscriberAuxiliaryServices.
     */
    public static Collection getPersonalListPlan(
            final Context context,
            final Collection associations)
        throws HomeException
    {
        final Collection ret = new ArrayList();

        final Collection services = SubscriberAuxiliaryServiceSupport.getAuxiliaryServiceCollection(context,
                associations, null);

        for (final Iterator it = services.iterator(); it.hasNext();)
        {
            final AuxiliaryService service = (AuxiliaryService) it.next();

            if (service.getType() == AuxiliaryServiceTypeEnum.CallingGroup)
            {
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
                    LogSupport.minor(context, PersonalListPlanSupport.class,
                            "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + service.getIdentifier());
                }
                
                if (callingGroupType.equals(CallingGroupTypeEnum.PLP))
                {
                    ret.add(Long.valueOf(callingGroupIdentifier));
                }
            }
        }

        return ret;
    }

    /**
     * get PLP
     *
     * @param ctx the operating context
     * @param id
     * @return
     * @throws HomeException
     */
    public static PersonalListPlan getPLPByID(final Context ctx, final long id) throws HomeException
    {
        final Home home = (Home) ctx.get(PersonalListPlanHome.class);

        return (PersonalListPlan) home.find(ctx, Long.valueOf(id));
    }

    /**
     * add PLP to F&F
     *
     * @param ctx the operating context
     * @param subscriberAuxiliaryService
     * @param service
     * @throws HomeException
     * @throws FFEcareException
     */
    public static void addPLP(final Context ctx,
            final SubscriberAuxiliaryService subscriberAuxiliaryService,
            final AuxiliaryService service)
        throws HomeException, FFEcareException
    {
        long callingGroup = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUP;
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            callingGroup = callingGroupAuxSvcExtension.getCallingGroup();
        }
        else 
        {
            LogSupport.minor(ctx, PersonalListPlanSupport.class,
                    "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + service.getIdentifier());
        }
        addPLP(ctx, subscriberAuxiliaryService, callingGroup);
    }
    
    public static void addPLP(final Context ctx,
            final SubscriberAuxiliaryService subscriberAuxiliaryService,
            final long callingGroup)
        throws HomeException, FFEcareException
    {
        final FFECareRmiService client = FFClosedUserGroupSupport.getFFRmiService(ctx, PersonalListPlanSupport.class);
        
        if (client == null)
        {
            throw new FFEcareException(" F&F connection is down, fail to up add PLP to F&F for subscriber"
                    + subscriberAuxiliaryService.getSubscriberIdentifier()
                    + " plp " + callingGroup, ExternalAppSupport.NO_CONNECTION);
        }

        final Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx,
                subscriberAuxiliaryService.getSubscriberIdentifier());

        final TrPeerMsisdn[] peerMsisdn = new TrPeerMsisdn[0];
        // peerMsisdn[0] = new TrPeerMsisdn(subscriber.getMSISDN());

        try
        {
			FFParamSetBuilder holder = new FFParamSetBuilder(ctx);
			holder.add(ParameterID.PARAM_NUM_FORCE_PLP_MSISDN_ADD, -1);
			holder.add(ParameterID.PARAM_ALLOW_INITIAL_PLP_MEMBER_REMOVAL, true);
			holder.add(ParameterID.PARAM_DEACTIVATE_PLP, false);
			
			int result = client.updatePLPForSub(subscriber.getMSISDN(), callingGroup, peerMsisdn,
                    holder.getParameters(), new ParameterSetHolderImpl());
            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                String msg = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
                throw new FFEcareException("Failed to add personal list plan" + callingGroup
                        + " to subscriber " + subscriber.getId() + ": " + msg, result);
            }
        }
        catch (final RemoteException throwable)
        {
            throw new FFEcareException("Failed to add personal list plan" + callingGroup
                    + " to subscriber "
                    + subscriber.getId(), ExternalAppSupport.REMOTE_EXCEPTION, throwable);
        }
    }

    /**
     * Delete PLP from F&F
     *
     * @param ctx the operating context
     * @param subscriberAuxiliaryService
     * @param service
     * @throws FFEcareException
     * @throws HomeException
     */
    public static void deletePLP(final Context ctx, final SubscriberAuxiliaryService subscriberAuxiliaryService,
            final AuxiliaryService service) throws FFEcareException, HomeException
    {
        long callingGroup = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUP;
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            callingGroup = callingGroupAuxSvcExtension.getCallingGroup();
        }
        else 
        {
            LogSupport.minor(ctx, PersonalListPlanSupport.class,
                    "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + service.getIdentifier());
        }
        deletePLP(ctx, subscriberAuxiliaryService, callingGroup);
    }

    public static void deletePLP(final Context ctx, final SubscriberAuxiliaryService subscriberAuxiliaryService,
            final long plpId) throws FFEcareException, HomeException
    {
        final FFECareRmiService client = FFClosedUserGroupSupport.getFFRmiService(ctx, PersonalListPlanSupport.class);
        if (client == null)
        {
            throw new FFEcareException(" F&F connection is down, fail to up delete PLP to F&F for subscriber"
                    + subscriberAuxiliaryService.getSubscriberIdentifier() + " plp " + plpId, ExternalAppSupport.NO_CONNECTION);
        }

        final Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx,
                subscriberAuxiliaryService.getSubscriberIdentifier());

        final TrPeerMsisdn[] peerMsisdn = new TrPeerMsisdn[1];
        peerMsisdn[0] = new TrPeerMsisdn(subscriber.getMSISDN());

        try
        {
            int result = client.deletePLPForSub(subscriber.getMSISDN(), plpId);
            if (result!=FFECareRmiConstants.FF_ECARE_PLP_NOT_FOUND && result!=FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                throw new FFEcareException("Unable to remove PLP for Sub", result);
            }
        }
        catch (final RemoteException throwable)
        {
            throw new FFEcareException("Failed to delete personal list plan" + plpId
                    + " to subscriber "
                    + subscriber.getId(), ExternalAppSupport.REMOTE_EXCEPTION, throwable);
        }
    }

    public static void suspendOrResumePLP(final Context ctx,
            final SubscriberAuxiliaryService subscriberAuxiliaryService,
            final long callingGroup, final boolean suspend)
        throws HomeException, FFEcareException
    {
        final FFECareRmiService client = FFClosedUserGroupSupport.getFFRmiService(ctx, PersonalListPlanSupport.class);
        
        if (client == null)
        {
            throw new FFEcareException(" F&F connection is down, fail to suspend PLP to F&F for subscriber"
                    + subscriberAuxiliaryService.getSubscriberIdentifier()
                    + " plp " + callingGroup, ExternalAppSupport.NO_CONNECTION);
        }

        final Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx,
                subscriberAuxiliaryService.getSubscriberIdentifier());

        final TrPeerMsisdn[] peerMsisdn = new TrPeerMsisdn[0];
        // peerMsisdn[0] = new TrPeerMsisdn(subscriber.getMSISDN());

        try
        {
			FFParamSetBuilder holder = new FFParamSetBuilder(ctx);
			holder.add(ParameterID.PARAM_DEACTIVATE_PLP, suspend);
			
            int result = client.updatePLPForSub(subscriber.getMSISDN(), callingGroup, peerMsisdn,
                    holder.getParameters(), new ParameterSetHolderImpl());
            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                String msg = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
                throw new FFEcareException("Failed to suspend personal list plan" + callingGroup
                        + " for subscriber " + subscriber.getId() + ": " + msg, result);
            }
        }
        catch (final RemoteException throwable)
        {
            throw new FFEcareException("Failed to suspend personal list plan" + callingGroup
                    + " for subscriber "
                    + subscriber.getId(), ExternalAppSupport.REMOTE_EXCEPTION, throwable);
        }
    }
    
    public static String[] getPLPListEntriesArray(final Set members)
    {
        final String[] ret = new String[members.size()];
        int i = 0;
        for (final Iterator it = members.iterator(); it.hasNext();)
        {
            ret[i] = (String) it.next();
            ++i;
        }
        return ret;
    }
}
