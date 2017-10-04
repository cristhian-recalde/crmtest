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

package com.trilogy.app.crm.vpn;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdditionalMsisdnBean;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriber;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberHome;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.client.EcpVpnClientException;
import com.trilogy.app.crm.client.VpnClient;
import com.trilogy.app.crm.client.VpnClientException;
import com.trilogy.app.crm.client.urcs.BGroupPricePlanClient;
import com.trilogy.app.crm.client.urcs.BGroupPricePlanException;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.extension.auxiliaryservice.core.VPNAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author yassir.pakran@redknee.com
 */
public class SubscriberAuxiliaryVpnServiceSupport
{
    private SubscriberAuxiliaryVpnServiceSupport()
    {
    }


    public static String provisionVpnSubscriber(final Context ctx, final Subscriber subscriber) throws VpnClientException,
            EcpVpnClientException, HomeException
    {
        final VpnClient vpnClient = (VpnClient) ctx.get(VpnClient.class);
        final String entityId = vpnClient.createVpnEntity(ctx, subscriber);
        if (entityId == null)
        {
            throw new HomeException("Failed to provision Vpn Subscriber " + subscriber.getBAN()
                    + " because Entity ID is null");
        }

        return entityId;
    }

    public static void provisionBusinessGroupPricePlan(final Context ctx, final Account vpnRoot,
            final AuxiliaryService auxSrv) throws HomeException
    {
        final BGroupPricePlanClient client;
        client = (BGroupPricePlanClient) ctx.get(UrcsClientInstall.BUSINESS_GROUP_PRICE_PLAN_CLIENT_KEY);

        try
        {
            VPNAuxSvcExtension vpnAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxSrv, VPNAuxSvcExtension.class);
            if (vpnAuxSvcExtension!=null)
            {
                client.setBusinessGroupPricePlan(ctx, vpnRoot.getSpid(), vpnRoot.getBAN(), vpnAuxSvcExtension.getVpnPricePlan());
            }
            else
            {
                LogSupport.minor(ctx, SubscriberAuxiliaryVpnServiceSupport.class,
                        "Unable to find required extension of type '" + VPNAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + auxSrv.getIdentifier());
            }
        }
        catch (BGroupPricePlanException e)
        {
            throw new HomeException("Unable to provision Business Group to Price Plan mapping.", e);
        }
    }

    public static void unprovisionBusinessGroupPricePlan(final Context ctx, final Account vpnRoot) throws HomeException
    {
        final BGroupPricePlanClient client;
        client = (BGroupPricePlanClient) ctx.get(UrcsClientInstall.BUSINESS_GROUP_PRICE_PLAN_CLIENT_KEY);

        try
        {
            client.setBusinessGroupPricePlan(ctx, vpnRoot.getSpid(), vpnRoot.getBAN(), -1);
        }
        catch (BGroupPricePlanException e)
        {
            throw new HomeException("Unable to provision Business Group to Price Plan mapping.", e);
        }
    }

    /**
     * @param ctx the operating context
     * @param subscriber
     * @param subAuxService
     * @throws VpnClientException thrown by VPN client or if client is missing
     * @throws EcpVpnClientException
     * @throws HomeException
     */
    public static void unProvisionVpnSubscriber(final Context ctx, final Subscriber subscriber,
            final SubscriberAuxiliaryService subAuxService) throws VpnClientException, EcpVpnClientException,
            HomeException
    {
        final VpnClient vpnClient = (VpnClient) ctx.get(VpnClient.class);
        if (vpnClient == null)
        {
            throw new VpnClientException("Vpn Client Not Found in Context");
        }
        final VpnAuxiliarySubscriber vpnSub = subAuxService.getVpnAuxiliarySubscriber(ctx);
        vpnClient.deleteVpnEntity(ctx, subscriber, vpnSub.getVpnEntityId());

        final Home home = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);
        home.remove(ctx, vpnSub);
    }

    /**
     * Support method to disable the VPN Entity for the Subscriber, this means disable the VPN Auxiliary Service for the
     * subscriber.
     *
     * @param ctx
     *            Context
     * @param subscriber
     *            Subscriber object
     * @param subAux
     *            SubscriberAuxiliaryService object
     * @throws VpnClientException
     *             exception
     * @throws EcpVpnClientException
     *             exception
     * @throws HomeException
     *             exception
     */
    public static void disableVpnSubscriber(final Context ctx, final Subscriber subscriber,
            final SubscriberAuxiliaryService subAux) throws VpnClientException, EcpVpnClientException, HomeException
    {
        final VpnClient vpnClient = (VpnClient) ctx.get(VpnClient.class);
        if (vpnClient == null)
        {
            throw new VpnClientException("Vpn Client Not Found in Context");
        }
        try
        {
            vpnClient.disableVpnEntity(ctx, subscriber, subAux.getVpnAuxiliarySubscriber(ctx).getVpnEntityId());
        }
        catch (final VpnClientException vce)
        {
            new MinorLogMsg(SubscriberAuxiliaryVpnServiceSupport.class.getName(),
                    "Error while disabling the Vpn Entity for " + " Subscriber = " + subscriber.getId()
                    + " when moving to " + subscriber.getState().getDescription(ctx) + " state", vce).log(ctx);
            throw vce;
        }
    }

    /**
     * Support method to enable the VPN Entity for the Subscriber, this means enabling the VPN Auxiliary Service for the
     * subscriber.
     * 
     * @param ctx
     *            the operating Context
     * @param subscriber
     *            Subscriber object
     * @param subAux
     *            SubscriberAuxiliaryService object
     * @throws VpnClientException
     *             exception
     * @throws EcpVpnClientException
     *             exception
     * @throws HomeException
     *             exception
     */
    public static void enableVpnSubscriber(final Context ctx, final Subscriber subscriber,
            final SubscriberAuxiliaryService subAux) throws VpnClientException, EcpVpnClientException, HomeException
    {
        final VpnClient vpnClient = (VpnClient) ctx.get(VpnClient.class);
        if (vpnClient == null)
        {
            throw new VpnClientException("Vpn Client Not Found in Context");
        }
        try
        {
            vpnClient.enableVpnEntity(ctx, subscriber, subAux.getVpnAuxiliarySubscriber(ctx).getVpnEntityId());
        }
        catch (final VpnClientException vce)
        {
            new MinorLogMsg(SubscriberAuxiliaryVpnServiceSupport.class.getName(),
                    "Error while enabling the Vpn Entity for Subscriber = " + subscriber.getId()
                    + " when moving to " + subscriber.getState().getDescription(ctx) + " state", vce).log(ctx);
            throw vce;
        }
    }

    public static void populateVpnSubandVpnService(Context ctx, final String rootAcctBan,
            final Subscriber subscriber, final SubscriberAuxiliaryService subAuxService,
            String vpnEntityId, final VpnAuxiliarySubscriber vpnSub) throws HomeException
    {
        vpnSub.setSubscriberId(subscriber.getId());
        vpnSub.setSubscriberMsisdn(subscriber.getMSISDN());
        vpnSub.setAuxiliaryServiceId(subAuxService.getAuxiliaryServiceIdentifier());
        vpnSub.setAccount(rootAcctBan);
        vpnSub.setSubcriberAuxiliaryId(subAuxService.getIdentifier());
        vpnSub.setVpnEntityId(vpnEntityId);
    }

    public static void reProvisionVpnSubscriber(final Context ctx, final Subscriber newSubscriber,
            final String vpnEntityId)
        throws VpnClientException, HomeException
    {
        final VpnClient vpnClient = (VpnClient) ctx.get(VpnClient.class);
        if (vpnClient == null)
        {
            throw new VpnClientException("VPN Client Not Found in Context");
        }
        vpnClient.updateVpnEntity(ctx, newSubscriber, vpnEntityId, false);

    }

    public static void provisionVpn(final Context ctx, final Subscriber subscriber,
            final SubscriberAuxiliaryService auxService) throws EcpVpnClientException, HomeException,
            VpnClientException
    {
        final String rootAcctBan = subscriber.getRootAccount(ctx).getBAN();

        final String vpnEntityId = provisionVpnSubscriber(ctx, subscriber);

        final Home home = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);
        final VpnAuxiliarySubscriber vpnSub = new VpnAuxiliarySubscriber();
        populateVpnSubandVpnService(ctx, rootAcctBan, subscriber, auxService, vpnEntityId, vpnSub);
        home.create(ctx, vpnSub);
    }
}
