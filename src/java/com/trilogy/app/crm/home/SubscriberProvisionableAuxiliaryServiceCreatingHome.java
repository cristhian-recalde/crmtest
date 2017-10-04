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
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.client.EcpVpnClientException;
import com.trilogy.app.crm.client.VpnClientException;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.vpn.SubscriberAuxiliaryVpnServiceSupport;

/**
 * Run the provision hlr command for all subscribers subscribed to given provisionable
 * service.
 *
 * @author lily.zou@redknee.com
 */
public class SubscriberProvisionableAuxiliaryServiceCreatingHome extends SubscriberProvisionableAuxiliaryServiceHome
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
    public SubscriberProvisionableAuxiliaryServiceCreatingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;

        final AuxiliaryService auxService = association.getAuxiliaryService(ctx);
        final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);

        if (shouldProvision(ctx, association, subscriber, auxService))
        {
            provisionSubscriberAuxiliaryService(ctx, association, auxService, subscriber);
        }

        final Object createdObject = super.create(ctx, obj);

        return createdObject;
    }


    

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        /*
         * Cindy: Pass through first before re-provisioning HLR (if needed). In the case of
         * AMSISDN swap, this allows the old one to be unprovisioned from HLR first, so we
         * won't hit any limitation like hitting the maximum number of AMSISDN per
         * subscriber/at most one AMSISDN per bearer type.
         */
        final Object storedObject = super.store(ctx, obj);

        final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) storedObject;

        final AuxiliaryService auxService = association.getAuxiliaryService(ctx);

        SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) ctx.get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);
        
        if (isEnabledAdditionalMsisdn(ctx, auxService))
        {
            final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
            
            final boolean wasProvisioned = oldAssociation != null && oldAssociation.getProvisioned();
            final boolean isActive = shouldProvision(ctx, association, subscriber, auxService);
            final boolean enteringActive = isActive && !wasProvisioned;

            final Msisdn newMsisdn = (Msisdn) ctx.get(Lookup.NEW_AMSISDN);
            final Msisdn oldMsisdn = (Msisdn) ctx.get(Lookup.OLD_AMSISDN);
            boolean isMsisdnUpdated = true;
            if (oldMsisdn == null || newMsisdn == null)
            {
                isMsisdnUpdated = false;
            }
            else if (SafetyUtil.safeEquals(newMsisdn.getMsisdn(), oldMsisdn.getMsisdn()))
            {
                isMsisdnUpdated = false;
            }

            if (enteringActive || isMsisdnUpdated)
            {
                provisionSubscriberAuxiliaryService(ctx, association, auxService, subscriber);
            }
        }
        else if (isEnabledMultiSIM(ctx, auxService))
        {
            final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
            
            final boolean wasProvisioned = oldAssociation != null && oldAssociation.getProvisioned();
            final boolean isActive = shouldProvision(ctx, association, subscriber, auxService);
            
            final boolean enteringActive = isActive && !wasProvisioned;
            final boolean isMsisdnUpdated = !SafetyUtil.safeEquals(association.getMultiSimMsisdn(), oldAssociation.getMultiSimMsisdn());

            if (enteringActive || isMsisdnUpdated)
            {
                try
                {
                    // Re-provision the Multi-SIM service to map the dummy MSISDN to the master MSISDN
                    provisionSubscriberAuxiliaryService(ctx, association, auxService, subscriber);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Error notifying HLR of re-association of IMSI " + association.getMultiSimImsi()
                            + " with subscriber " + association.getSubscriberIdentifier()
                            + " for Multi-SIM auxiliary service " + association.getAuxiliaryServiceIdentifier()
                            + " (DummyMSISDN=" + association.getMultiSimMsisdn() + ")", e).log(ctx);
                }
            }            
        }

        return storedObject;
    }


   }
