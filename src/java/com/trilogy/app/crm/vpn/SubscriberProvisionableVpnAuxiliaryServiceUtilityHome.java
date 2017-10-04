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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriber;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberHome;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberXInfo;
import com.trilogy.app.crm.elang.OracleIn;
import com.trilogy.app.crm.filter.AccountOfTypeWithMemory;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


public class SubscriberProvisionableVpnAuxiliaryServiceUtilityHome extends HomeProxy
{

    /**
     * Creates a new SubscriberAuxiliaryServiceIdentiferHome proxy.
     *
     * @param ctx
     *            The operating context
     * @param delegate
     *            The Home to which we delegate.
     */
    public SubscriberProvisionableVpnAuxiliaryServiceUtilityHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final SubscriberAuxiliaryService subAux = (SubscriberAuxiliaryService) obj;

        subAux.setContext(ctx);

        if (subAux.getType(ctx) != null && subAux.getType(ctx).equals(AuxiliaryServiceTypeEnum.Vpn))
        {
            final Subscriber sub = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAux);
            Account account = sub.getAccount(ctx);
            final Account rootAccount = account.getRootAccount(ctx);

            if (rootAccount.getVpnMSISDN().equals(sub.getMSISDN()))
            {
                // provision Business Group ID to Price Plan mapping 
                final AuxiliaryService auxSrv = subAux.getAuxiliaryService(ctx);
                SubscriberAuxiliaryVpnServiceSupport.provisionBusinessGroupPricePlan(ctx, rootAccount, auxSrv);

                // subscribe other subscribers
                subscribeAllSubscriptions(ctx, subAux, sub, rootAccount);
            }
        }
        return super.create(ctx, subAux);
    }


    /**
     * @param ctx
     * @param subAux
     * @param sub
     * @param rootAccount
     * @throws HomeException
     */
    private void subscribeAllSubscriptions(final Context ctx, final SubscriberAuxiliaryService subAux,
            final Subscriber sub, final Account rootAccount) throws HomeException
    {
        final String vpnSubscriberId = sub.getId();

        final Collection subscriberAccounts = AccountSupport.getTopologyEx(ctx, rootAccount, null,
                new AccountOfTypeWithMemory(GroupTypeEnum.SUBSCRIBER), true, false, null, false);
        final Set<String> subscriberBANs = new HashSet<String>();
        CollectionSupportHelper.get(ctx).process(ctx, subscriberAccounts, AccountXInfo.BAN, subscriberBANs);

        // Skip whole query if there are no BANs (OracleIn would replace itself with False.instance() anyways)
        if (subscriberBANs != null && subscriberBANs.size() > 0)
        {
            final And condition = new And();
            condition.add(new OracleIn(SubscriberXInfo.BAN, subscriberBANs));
            condition.add(new EQ(SubscriberXInfo.SUBSCRIPTION_TYPE, sub.getSubscriptionType()));
            condition.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));

            // the normal home is obstructed with the BAN = "crap" condition
            final Home subHome = (Home) ctx.get(SubscriberXDBHome.class);
            final Visitor visitor = new SubscribeVPNAuxiliaryServiceVisitor(subAux, vpnSubscriberId);
            subHome.forEach(ctx, visitor, condition);
        }
    }


    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        // if the service is VPN, then update the entries in VpnSubscriberHome too.
        final SubscriberAuxiliaryService subAuxSvc = (SubscriberAuxiliaryService) obj;
        subAuxSvc.setContext(ctx);
        final AuxiliaryService aux = subAuxSvc.getAuxiliaryService(ctx);
        if (!aux.getType().equals(AuxiliaryServiceTypeEnum.Vpn))
        {
            return super.store(ctx, obj);
        }
        final Home home = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);
        final Subscriber sub = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAuxSvc);
        final String msisdn = sub.getMSISDN();
        final VpnAuxiliarySubscriber vpnSub = (VpnAuxiliarySubscriber) home.find(ctx,
                Long.valueOf(subAuxSvc.getIdentifier()));
        final Account account = sub.getAccount(ctx);
        final Account rootAcct = account.getRootAccount(ctx);
        if (!SafetyUtil.safeEquals(msisdn, vpnSub.getSubscriberMsisdn()))
        {
            vpnSub.setAccount(rootAcct.getBAN());
        }

        return super.store(ctx, obj);
    }


    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        final SubscriberAuxiliaryService subAux = (SubscriberAuxiliaryService) obj;
        subAux.setContext(ctx);
        super.remove(ctx, obj);
        AuxiliaryService auxSvc = null;
        try
        {
            auxSvc = subAux.getAuxiliaryService(ctx);

            if (auxSvc.getType().equals(AuxiliaryServiceTypeEnum.Vpn))
            {
                final Subscriber sub = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAux);
                final Account acct = sub.getRootAccount(ctx);

                if (acct.getVpnMSISDN().equals(sub.getMSISDN()))
                {
                    // provision Business Group ID to Price Plan mapping 
                    final AuxiliaryService auxSrv = subAux.getAuxiliaryService(ctx);
                    SubscriberAuxiliaryVpnServiceSupport.unprovisionBusinessGroupPricePlan(ctx, acct);

                    final Home vpnSubHome = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);
                    final And and = new And();
                    and.add(new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, acct.getBAN()));
                    and.add(new EQ(VpnAuxiliarySubscriberXInfo.AUXILIARY_SERVICE_ID,
                            Long.valueOf(subAux.getAuxiliaryServiceIdentifier())));

                    vpnSubHome.where(ctx, and).forEach(ctx, new UnprovisionVPNVisitor(acct));
                }
            }

        }
        catch (final Exception e)
        {
            new InfoLogMsg(this,
                "Exception when trying to remove auxiliary services of main vpn subscriber and other subscribers", e)
                .log(ctx);
        }
    }

}
