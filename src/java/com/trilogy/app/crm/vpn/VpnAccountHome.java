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

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.util.snippet.log.Logger;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriber;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberHome;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberXInfo;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * Although this class is called VpnAccountHome, it performs most of the MOM provisioning actions in the CRM. MOM is not
 * just VPN, and includes ICM as well.
 *
 * @author yassir.pakran@redknee.com
 */
public class VpnAccountHome extends HomeProxy
{

    public VpnAccountHome(final Context ctx, final Home home)
    {
        super(ctx, home);
    }

    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Account account = (Account) super.create(ctx, obj);

        return account;
    }

    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Account account = (Account) obj;
        Account acct = null;

        final Home acctHome = (Home) ctx.get(AccountHome.class);
        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);

        if (oldAccount == null)
        {
            Logger.major(ctx, this, "Old Account missing from context !!!");
            return getDelegate(ctx).store(ctx, obj);
        }

        // If this account wasn't a VPN at all ignore the code below
        // 2006-02-24: Changed to isMom (includes VPN and ICM), since both VPN and ICM
        // share the same functionality
        if (!account.isMom(ctx) && !oldAccount.isMom(ctx))
        {
            return getDelegate(ctx).store(ctx, obj);
        }

        try
        {
            final Home subAuxSvcHome = (Home) ctx.get(SubscriberAuxiliaryServiceHome.class);
            final Home vpnSubHome = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);

            if (account.getState().equals(AccountStateEnum.SUSPENDED)
                    && oldAccount.getState() != account.getState())
            {
                // If the account is suspended (TT6041233183), we need to deprovision the VPN
                // auxiliary service for the responsible sub accounts from the HLR

                // Suspend all the VPN services of all the non responsible subscribers
                final Or condition = new Or();
                condition.add(new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, account.getBAN()));
                condition.add(new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, account.getRootAccount(ctx).getBAN()));

                vpnSubHome.where(ctx, condition).forEach(new ProvisionVPNServiceVisitor(account, false));
            }
            else if (oldAccount.getState().equals(AccountStateEnum.SUSPENDED)
                    && (account.getState().equals(AccountStateEnum.ACTIVE)
                            || account.getState().equals(AccountStateEnum.PROMISE_TO_PAY)))
            {
                // If the account changes from suspended (TT6041233183), we need to re-provision
                // the VPN auxiliary service to the HLR for all responsible subscribers
                final EQ condition = new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, oldAccount.getBAN());
                vpnSubHome.where(ctx, condition).forEach(new ProvisionVPNServiceVisitor(account, true));
            }
            else if (!SafetyUtil.safeEquals(oldAccount.getVpnMSISDN(), account.getVpnMSISDN()))
            {
                // the VPN account should be the root and be of VPN Type So safely call
                // all the subscribers with this account as the VPN account
                acct = (Account) super.store(ctx, account);
                if (account.isRootAccount())
                {
                    if (account.getVpnMSISDN() == null || account.getVpnMSISDN().trim().length() == 0)
                    {
                        final And whereClause = new And();
                        whereClause.add(new Limit(1));
                        whereClause.add(new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, account.getBAN()));
                        final Collection vpnSubs = vpnSubHome.select(ctx, whereClause);
                        if (vpnSubs == null || vpnSubs.size() > 0)
                        {
                            throw new HomeException("Cannot change vpnMsisdn to \"\" when there are subscribers");
                        }
                    }
                    final VpnMsisdnVisitor vpnMsisdnVisitor = new VpnMsisdnVisitor(subAuxSvcHome);
                    vpnSubHome.where(ctx, new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, account.getBAN())).forEach(ctx,
                            vpnMsisdnVisitor);
                }
            }

            // If the new account is VPN, we need to create a new relation for all the subscribers underneath
            // this includes all the sub accounts
            // 2006-02-24: Changed to isMom (includes VPN and ICM), since both VPN and ICM
            // share the same functionality
            if (!oldAccount.isMom(ctx) && account.isMom(ctx))
            {
                // TODO 2009-01-06 nothing to do, VPN subscription is done on VPN auxiliary service subscription 
            }
            else if (oldAccount.isMom(ctx) && !account.isMom(ctx))
            {
                // If the account changed its type deprovision the VPN services
                final Or or = new Or();
                or.add(new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, account.getBAN()));
                or.add(new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, account.getRootAccount(ctx).getBAN()));

                vpnSubHome.where(ctx, or).forEach(new UnprovisionVPNVisitor(account));
                account.setVpnMSISDN("");
            }
        }
        catch (final Exception e)
        {
            if (e instanceof HomeException || e instanceof HomeInternalException)
            {
                throw (HomeException) e;
            }
            LogSupport.minor(ctx, this, "Exception while handling VPN account", e);
        }

        // if there is no home exception continue
        if (acct != null)
        {
            return acct;
        }
        else
        {
            return super.store(ctx, obj);
        }

    }
}

class VpnMsisdnVisitor implements Visitor
{
    /**
     * Prevent java version serialization problems.
     */
    private static final long serialVersionUID = 1L;

    public VpnMsisdnVisitor(final Home subAuxSvcHome)
    {
        home_ = subAuxSvcHome;
    }

    public void visit(final Context ctx, final Object obj)
    {
        try
        {
            final VpnAuxiliarySubscriber vpnSub = (VpnAuxiliarySubscriber) obj;
            final EQ where = new EQ(SubscriberAuxiliaryServiceXInfo.IDENTIFIER,
                    Long.valueOf(vpnSub.getSubcriberAuxiliaryId()));
            final SubscriberAuxiliaryService subAux = (SubscriberAuxiliaryService) home_.find(ctx, where);
            subAux.setContext(ctx);
            final Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(ctx, subAux.getSubscriberIdentifier());
            SubscriberAuxiliaryVpnServiceSupport.reProvisionVpnSubscriber(ctx, sub,
                    subAux.getVpnAuxiliarySubscriber(ctx).getVpnEntityId());

            // Manda - Handling update SMSB with VPN Service Grade
            SubscriberAuxiliaryServiceSupport.updateSmsbSvcGradeforVPNService(ctx, sub, true);

        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "Exception caught in Visitor. ", null).log(ctx);
        }
    }

    private final transient Home home_;
}

