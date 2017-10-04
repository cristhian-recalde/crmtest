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

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriber;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberHome;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberXInfo;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.util.snippet.log.Logger;

/**
 * @author yassir.pakran@redknee.com
 */
public class VpnSubscriberValidator implements Validator
{
    private static final VpnSubscriberValidator INSTANCE = new VpnSubscriberValidator();

    private VpnSubscriberValidator()
    {
    }

    public static VpnSubscriberValidator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj)
    {
        final Subscriber sub = (Subscriber) obj;
        Account rootAccount = null;
        final Account subscriberAccount = (Account) ctx.get(Lookup.ACCOUNT);

        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
        if (subscriberAccount == null)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.BAN,
                    "Cannot find Account " + sub.getBAN()));
            exceptions.throwAllAsCompoundException();
        }

        if (subscriberAccount.isMom(ctx))
        {
            // subscriber account cannot be VPN account
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.BAN,
                    "Illegal Account type for BAN " + sub.getBAN() + ". Subscriber Account cannot be VPN Account."));
            exceptions.throwAllAsCompoundException();
        }

        if (subscriberAccount.isRootAccount())
        {
            // this subscriber account is root account so it cannot be part of a VPN account
            // TODO investigate
            return;
        }

        // this is the group account that contains the subscriber account
        Account account = null;
        try
        {
            account = subscriberAccount.getParentAccount(ctx);
        }
        catch (HomeException ex)
        {
            final String msg = "Cannot determine parent Account for subscriber Account " + subscriberAccount.getBAN();
            Logger.minor(ctx, this, msg, ex);

            final Exception propException = new IllegalPropertyArgumentException(SubscriberXInfo.BAN, msg);
            propException.initCause(ex);
            exceptions.thrown(propException);
            exceptions.throwAllAsCompoundException();
        }

        if (account == null)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.BAN,
                    "Cannot find Account " + sub.getBAN()));
            exceptions.throwAllAsCompoundException();
        }

        try
        {
            rootAccount = account.getRootAccount(ctx);
        }
        catch (HomeException e)
        {
            final String msg = "Cannot determine Root Account for Account " + account.getBAN();
            Logger.minor(ctx, this, msg, e);

            final Exception propException = new IllegalPropertyArgumentException(SubscriberXInfo.BAN, msg);
            propException.initCause(e);
            exceptions.thrown(propException);
            exceptions.throwAllAsCompoundException();
        }

        // 2006-02-24: Changed to isMom (includes VPN and ICM), since both VPN and ICM share the same functionality
        if (rootAccount.isMom(ctx))
        {
            final String vpnMsisdn = rootAccount.getVpnMSISDN();

            if (EnumStateSupportHelper.get(ctx).stateEquals(sub, SubscriberStateEnum.INACTIVE)
                    && SafetyUtil.safeEquals(vpnMsisdn, sub.getMSISDN()))
            {
                // Prevent deactivation of subscriber if it is the VPN lead subscriber in account with members
                final Home vpnSubHome = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);
                final And whereClause = new And();
                // TODO 2009-10-28 Limit does not work properly when it is used like this
                // whereClause.add(new Limit(2));
                whereClause.add(new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, rootAccount.getBAN()));
                Collection vpnSubs = null;
                try
                {
                    vpnSubs = vpnSubHome.select(ctx, whereClause);
                }
                catch (HomeException he)
                {
                    exceptions.thrown(new IllegalStateException(
                            "Error looking up VPN subscribers in account " + rootAccount.getBAN()
                            + ". Aborting deactivation because there may be other VPN subscribers in the account."));
                }
                if (vpnSubs != null && vpnSubs.size() > 0)
                {
                    final String vpnSubId = ((VpnAuxiliarySubscriber) vpnSubs.iterator().next()).getSubscriberId();
                    if (vpnSubs.size() > 1 || !SafetyUtil.safeEquals(sub.getId(), vpnSubId))
                    {
                        exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.STATE,
                                "Can't deactivate subscriber " + sub.getId()
                                + " because it is the lead VPN subscriber of account " + rootAccount.getBAN()
                                + ", which contains other subscribers. Change the VPN MSISDN"
                                + " in the account to a different subscriber before deactivating."));
                    }
                }
            }

            if (account.isRootAccount())
            {
                try
                {
                    if (vpnMsisdn != null && vpnMsisdn.length() != 0)
                    {
                        if (SubscriberSupport.lookupSubscriberForMSISDN(ctx, vpnMsisdn) == null)
                        {
                            if (!vpnMsisdn.equals(sub.getMSISDN()))
                            {
                                exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.BAN,
                                        "VPN leader Subscriber with MSISDN \"" + vpnMsisdn
                                        + "\" missing in VPN Root Account \"" + rootAccount.getBAN() + "\""));
                            }
                        }
                    }
                }
                catch (HomeException homeEx)
                {
                    final Exception propException = new IllegalPropertyArgumentException(SubscriberXInfo.BAN,
                            "Unable to validate presence of Subscriber with Msisdn \"" + vpnMsisdn + "\"");
                    propException.initCause(homeEx);
                    exceptions.thrown(propException);
                }
            }
        }

        exceptions.throwAllAsCompoundException();
    }
}
