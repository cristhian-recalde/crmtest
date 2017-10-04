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

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriber;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberHome;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberXInfo;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;

/**
 * @author yassir.pakran@redknee.com
 */
public class SubscriberVpnHome extends HomeProxy
{

    public SubscriberVpnHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    public SubscriberVpnHome(final Home delegate)
    {
        super(delegate);
    }

    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	// TODO victor 2009-11-04 review if this auxiliary service magic is really needed.
        // First de-select all the auxiliary services selected, then provision the sub.
        // Then update the sub with the wanted auxiliary services
        Subscriber sub = (Subscriber) obj;
        final List servicesWanted = sub.getAuxiliaryServices(ctx);
        final List noservices = new ArrayList();

        sub.setAuxiliaryServices(noservices);

        sub = (Subscriber) super.create(ctx, sub);

        sub.setAuxiliaryServices(servicesWanted);

        return sub;
    }

    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final Subscriber newSubscriber = (Subscriber) super.store(ctx, obj);
        Account account = newSubscriber.getAccount(ctx);
        final Account rootAccount = account.getRootAccount(ctx);

        // 2006-02-24: Changed to isMom (includes VPN and ICM), since both VPN
        // and ICM share the same functionality
        if (rootAccount != null && rootAccount.isMom(ctx))
        {
            final Home vpnSubHome = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);

            final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            final String oldSubscriberMsisdn = oldSubscriber.getMSISDN();
            final String newSubMsisdn = newSubscriber.getMSISDN();

            final boolean msisdnChange = !SafetyUtil.safeEquals(oldSubscriberMsisdn, newSubMsisdn);

            if (msisdnChange && rootAccount.getVpnMSISDN().equals(oldSubscriberMsisdn))
            {
                // The VPN lead subscriber is undergoing a MSISDN change
                // VpnMsisdn is an account feature so delegating to account store.
                rootAccount.setVpnMSISDN(newSubMsisdn);
                final Home actHome = (Home) ctx.get(ctx, AccountHome.class);
                actHome.store(ctx, rootAccount);
            }

            if (EnumStateSupportHelper.get(ctx).isEnteringState(oldSubscriber, newSubscriber, SubscriberStateEnum.INACTIVE))
            {
                try
                {
                    final EQ condition = new EQ(VpnAuxiliarySubscriberXInfo.SUBSCRIBER_ID, newSubscriber.getId());
                    final VpnAuxiliarySubscriber vpnSub = (VpnAuxiliarySubscriber) vpnSubHome.find(condition);
                    if (vpnSub == null)
                    {
                        new MinorLogMsg(this, "Vpn Subscriber " + newSubscriber.getId()
                                + "is not properly populated in the VPN Auxiliary Subscriber table", null).log(ctx);
                    }
                    else
                    {
                        final SubscriberAuxiliaryService subAuxSvc = SubscriberAuxiliaryServiceSupport
                                .getSubscriberAuxiliaryService(ctx, vpnSub.getSubcriberAuxiliaryId());
                        SubscriberAuxiliaryVpnServiceSupport.unProvisionVpnSubscriber(ctx, oldSubscriber, subAuxSvc);
                        // Update VPN service grade to SMSB
                        SubscriberAuxiliaryServiceSupport.updateSmsbSvcGradeforVPNService(ctx, newSubscriber, false);
                    }
                }
                catch (Exception e)
                {
                    new MinorLogMsg(this,
                            "Caught an exception while trying to unprovision the vpnauxiliaryservice of the subscriber "
                                    + oldSubscriber.getId() + " deactivation of " + oldSubscriberMsisdn, e).log(ctx);
                }
            }
            // Manda - if the subscriber is moving from Active to Suspend state
            // for a VPN Subscriber,
            // then check if there is VPN AuxiliaryService provisioned for the
            // Subscriber, and if exists
            // then disable(note: don't delete the vpnAuxService) the VPN
            // AuxiliaryService, and change the
            // service grade of the SMSB to default prepaid or postpaid.
            else if (!msisdnChange
                    && EnumStateSupportHelper.get(ctx).isTransition(oldSubscriber, newSubscriber, SubscriberStateEnum.ACTIVE,
                            SubscriberStateEnum.SUSPENDED))
            {
                try
                {
                    final EQ condition = new EQ(VpnAuxiliarySubscriberXInfo.SUBSCRIBER_ID, newSubscriber.getId());
                    final VpnAuxiliarySubscriber vpnSub = (VpnAuxiliarySubscriber) vpnSubHome.find(condition);
                    if (vpnSub == null)
                    {
                        new MinorLogMsg(this, "Vpn Subscriber " + newSubscriber.getId()
                                + "is not properly populated in the VPN Auxiliary Subscriber table", null).log(ctx);
                    }
                    else
                    {
                        final SubscriberAuxiliaryService subAuxSvc = SubscriberAuxiliaryServiceSupport
                                .getSubscriberAuxiliaryService(ctx, vpnSub.getSubcriberAuxiliaryId());
                        SubscriberAuxiliaryVpnServiceSupport.disableVpnSubscriber(ctx, newSubscriber, subAuxSvc);
                        // update the vpn service grade on SMSB
                        SubscriberAuxiliaryServiceSupport.updateSmsbSvcGradeforVPNService(ctx, newSubscriber, false);
                    }
                }
                catch (final Exception e)
                {
                    new MinorLogMsg(this,
                            "Caught an exception while trying to disable the vpnauxiliaryservice of the subscriber "
                                    + newSubscriber.getId() + " suspending of " + newSubMsisdn, e).log(ctx);
                }
            }
            else if (!msisdnChange
                    && EnumStateSupportHelper.get(ctx).isTransition(oldSubscriber, newSubscriber, SubscriberStateEnum.SUSPENDED,
                            SubscriberStateEnum.ACTIVE))
            {
                //TODO 2009-11-09 refactor this condition to the one above
                try
                {
                    final EQ condition = new EQ(VpnAuxiliarySubscriberXInfo.SUBSCRIBER_ID, newSubscriber.getId());
                    final VpnAuxiliarySubscriber vpnSub = (VpnAuxiliarySubscriber) vpnSubHome.find(condition);
                    if (vpnSub == null)
                    {
                        new MinorLogMsg(this, "Vpn Subscriber " + newSubscriber.getId()
                                + "is not properly populated in the VPN Auxiliary Subscriber table", null).log(ctx);
                    }
                    else
                    {
                        final SubscriberAuxiliaryService subAuxSvc = SubscriberAuxiliaryServiceSupport
                                .getSubscriberAuxiliaryService(ctx, vpnSub.getSubcriberAuxiliaryId());
                        SubscriberAuxiliaryVpnServiceSupport.enableVpnSubscriber(ctx, newSubscriber, subAuxSvc);
                        // update the VPN service grade on SMSB
                        SubscriberAuxiliaryServiceSupport.updateSmsbSvcGradeforVPNService(ctx, newSubscriber, true);
                    }
                }
                catch (final Exception e)
                {
                    new MinorLogMsg(this,
                            "Caught an exception while trying to disable the vpnauxiliaryservice of the subscriber "
                                    + newSubscriber.getId() + " suspending of " + newSubMsisdn, e).log(ctx);
                }

            }
            // if the MSISDN changes for a subscriber if the subscriber is the
            // main VPN subscriber then we update the profiles of all VPN
            // subscribers of the account
            // else we just update the profile in AppVpn

            // TODO 2008-08-22 name no longer part of Subscriber, move to the subscriber account watch dog 
            else if (msisdnChange)
            // || !SafetyUtil.safeEquals(oldSubscriber.getFirstName(), newSubscriber.getFirstName())
            // || !SafetyUtil.safeEquals(oldSubscriber.getLastName(), newSubscriber.getLastName()))
            {
                try
                {
                    final EQ condition = new EQ(VpnAuxiliarySubscriberXInfo.SUBSCRIBER_ID, newSubscriber.getId());
                    final VpnAuxiliarySubscriber vpnSub = (VpnAuxiliarySubscriber) vpnSubHome.find(condition);
                    if (vpnSub == null)
                    {
                        new MinorLogMsg(this, "Vpn Subscriber " + newSubscriber.getId()
                                + "is not properly populated in the VPN Auxiliary Subscriber table", null).log(ctx);
                    }
                    else
                    {
                        SubscriberAuxiliaryVpnServiceSupport.reProvisionVpnSubscriber(ctx, newSubscriber,
                              vpnSub.getVpnEntityId());
                    }
                }
                catch (final Exception e)
                {
                    new MinorLogMsg(this, "Caught an exception while trying to re-provision the vpn aux svc for sub "
                            + oldSubscriber.getId() + " for the subscriber's attribute change ", e).log(ctx);
                }
            }
        }

        return newSubscriber;
    }

}
