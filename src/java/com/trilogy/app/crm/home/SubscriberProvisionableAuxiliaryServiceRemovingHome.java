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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.vpn.SubscriberAuxiliaryVpnServiceSupport;


/**
 * Run the unprovision command for all subscribers subscribed to given provisionable
 * auxiliary service.
 *
 * @author lily.zou@redknee.com
 */
public class SubscriberProvisionableAuxiliaryServiceRemovingHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new SubscriberAuxiliaryServiceIdentiferHome proxy.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            The Home to which we delegate.
     */
    public SubscriberProvisionableAuxiliaryServiceRemovingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
        final AuxiliaryService auxService = association.getAuxiliaryService(ctx);
        final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
        final SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) ctx
            .get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);

        if (shouldUnprovision(ctx, oldAssociation, subscriber, auxService))
        {
            unprovision(ctx, association, auxService, subscriber);
        }
        super.remove(ctx, obj);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
        final AuxiliaryService auxService = association.getAuxiliaryService(ctx);
        final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);

        final SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) ctx.get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);
        
        if (isAdditionalMsisdn(ctx, auxService))
        {
            
            final Msisdn newAMsisdn = (Msisdn) ctx.get(Lookup.NEW_AMSISDN);
            final Msisdn oldAMsisdn = (Msisdn) ctx.get(Lookup.OLD_AMSISDN);
            if (newAMsisdn != null
                    && oldAMsisdn != null
                    && !SafetyUtil.safeEquals(newAMsisdn.getMsisdn(), oldAMsisdn.getMsisdn()))
            {
                // AMSISDN updated, so unprovision the auxiliary service
                unprovision(ctx, association, auxService, subscriber);                    
            }
            else if( !StateChangeAuxiliaryServiceSupport.isActive(ctx, association) )
            {
                // If required, unprovision the inactive auxiliary service
                if( shouldUnprovision(ctx, oldAssociation, subscriber, auxService) )
                {
                    unprovision(ctx, association, auxService, subscriber);
                }
            }
        }
        else if (isMultiSIM(ctx, auxService) 
                && association.getSecondaryIdentifier() != SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
        {
            final boolean isMsisdnUpdated = !SafetyUtil.safeEquals(association.getMultiSimMsisdn(), oldAssociation.getMultiSimMsisdn());
            if (isMsisdnUpdated)
            {
                Context sCtx = ctx.createSubContext();
                
                // Unprovision the old dummy MSISDN
                String key = HLRConstants.PRV_CMD_TYPE_MULTISIM_REMOVE_MSISDN;
                sCtx.put(SubscriberAuxiliaryService.class, oldAssociation);
                try
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Attempting to send HLR command '" + key
                                + "' for auxiliary service [ID=" + association.getAuxiliaryServiceIdentifier()
                                + ",SecondaryID=" + association.getSecondaryIdentifier()
                                + "] for subscription [" + association.getSubscriberIdentifier() + "]...", null).log(ctx);
                    }
                    
                    HlrSupport.updateHlr(sCtx, subscriber, key);
                }
                catch (ProvisionAgentException e)
                {
                    new MinorLogMsg(this, "Error removing dummy MSISDN " + oldAssociation.getMultiSimMsisdn()
                            + " from HLR during primary MSISDN change for Multi-SIM auxiliary service " + oldAssociation.getAuxiliaryServiceIdentifier()
                            + " (IMSI=" + oldAssociation.getMultiSimImsi() + ")", e).log(ctx);
                }
            }
        }
        
        return super.store(ctx, obj);
    }


    /**
     * Unprovisions the auxiliary service from HLR and VPN.  Note that the HLR is only unprovisioned
     * for subscribers that are not in the INACTIVE state
     */
    private void unprovision(final Context ctx, final SubscriberAuxiliaryService association,
            final AuxiliaryService auxService, final Subscriber subscriber) throws HomeException
    {
        try
        {
            // HLD OID 13650, TT8010300013
            // Don't send HLR deprovisioning commands for inactive subscribers.  They should have been
            // deprovisioned from the HLR when the 'inactive' command was sent in the subscriber pipeline.
            if( !EnumStateSupportHelper.get(ctx).stateEquals(subscriber, SubscriberStateEnum.INACTIVE) )
            {
                unProvisionHLR(ctx, association, auxService, subscriber);   
            }

            /*
             * Cindy: Eclipse complains subscriber and auxService may be null, but
             * the null check has already been done (through the value of
             * unprovision).
             */
            if (auxService != null
                    && auxService.getType().equals(AuxiliaryServiceTypeEnum.Vpn))
            {
                SubscriberAuxiliaryVpnServiceSupport.unProvisionVpnSubscriber(ctx, subscriber, association);

            }
        }
        catch (final Exception e)
        {
            /*
             * something went wrong when unprovisioning this aux.svc from hlr.
             * don't want to remove entry in SubscriberAuxiliaryService home
             * because that will cause inconsistence between hlr and ecare. throw
             * HomeException out to stop process
             */
            throw new HomeException(e.getMessage());
        }
    }


    /**
     * Updates the HLR by sending the given command.
     *
     * @param ctx
     *            The operating context.
     * @param auxService
     *            Auxiliary service being unprovisioned.
     * @param subscriber
     *            The subscriber being unprovisioned.
     * @throws HomeException
     *             Thown if unprovisioning failed.
     */
    private void unProvisionHLR(final Context ctx, final SubscriberAuxiliaryService association, final AuxiliaryService auxService, final Subscriber subscriber)
        throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Attempting to send HLR unprovisioning command for auxiliary service [ID=" + auxService.getIdentifier()
                    + "] for subscription [" + subscriber.getId() + "]...", null).log(ctx);
        }
        
        StateChangeAuxiliaryServiceSupport.unProvisionHlr(ctx, association, auxService, subscriber, this);
    }


    /**
     * Whether this association should be unprovisioned.
     *
     * @param ctx
     *            The operating context.
     * @param association
     *            The association to be unprovisioned.
     * @param subscriber
     *            The subscriber to be unprovisioned.
     * @param auxService
     *            The auxiliary service to be unprovisioned.
     * @return Whether this association should be unprovisioned to HLR or not.
     */
    private boolean shouldUnprovision(final Context ctx, final SubscriberAuxiliaryService association,
        final Subscriber subscriber, final AuxiliaryService auxService)
    {
        if (auxService == null
                || subscriber == null
                || !association.isProvisioned()
                || !auxService.isHLRProvisionable()
                || StateChangeAuxiliaryServiceSupport.isProvisionOnSuspendDisable(ctx, subscriber, auxService))
        {
            return false;
        }
        else if (AuxiliaryServiceTypeEnum.MultiSIM.equals(auxService.getType())
                    && association.getSecondaryIdentifier() == SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
        {
            // Provisioning commands only get sent for per-SIM services
            return false;
        }
        return true;
    }


    /**
     * Whether this auxiliary service is an additional MSISDN type and additional MSISDN
     * feature is enabled.
     *
     * @param context
     *            The operating context.
     * @param auxService
     *            The auxiliary service being determined.
     * @return Returns <code>true</code> if additional MSISDN feature is enabled and the
     *         auxiliary service is additional MSISDN type.
     */
    private boolean isAdditionalMsisdn(final Context context, final AuxiliaryService auxService)
    {
        return SafetyUtil.safeEquals(auxService.getType(), AuxiliaryServiceTypeEnum.AdditionalMsisdn)
            && AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context);
    }


    /**
     * Whether this auxiliary service is an Multi-SIM type and Multi-SIM
     * feature is enabled.
     *
     * @param ctx
     *            The operating context.
     * @param auxService
     *            The auxiliary service being determined.
     * @return Returns <code>true</code> if Multi-SIM feature is enabled and the
     *         auxiliary service is Multi-SIM type.
     */
    private boolean isMultiSIM(final Context ctx, final AuxiliaryService auxService)
    {
        return auxService != null 
                && auxService.getType() == AuxiliaryServiceTypeEnum.MultiSIM
                && LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.MULTI_SIM_LICENSE);
    }
}
