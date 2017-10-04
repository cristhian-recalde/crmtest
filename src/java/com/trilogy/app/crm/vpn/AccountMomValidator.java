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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberHome;
import com.trilogy.app.crm.bean.VpnAuxiliarySubscriberXInfo;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * A Validator for MOM accounts (Includes both VPN and ICM).
 *
 * Replaces com.redknee.app.crm.home.account.VpnAccountValidator Moved it here so that the class name reflects the
 * function. No svn history was lost.
 *
 * @author yassir.pakran@redknee.com
 */
public class AccountMomValidator implements Validator
{
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Account account = (Account) obj;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        try
        {
            validateMomAccount(ctx, account, el);
        }
        catch (HomeException e)
        {
            el.thrown(e);
        }
        el.throwAll();
    }

    /**
     * Validates if the account type is of MOM, then: 1) At least one of VPN and ICM have to be set, 2) the VPN MSISDN
     * must be set.
     *
     * @param ctx the operating context
     * @param account account to validate
     * @param el exception listener to collect validation exceptions
     * @throws HomeException
     */
    private void validateMomAccount(final Context ctx, final Account account, final CompoundIllegalStateException el)
        throws HomeException
    {
        // 2006-02-24: Changed to isMom (includes VPN and ICM), since both VPN and ICM share the same functionality
        if (account.isMom(ctx))
        {
            validateServiceType(account, el);
            validateVpnMsisdn(ctx, account, el);
        }
    }

    /**
     * Throws error if the neither VPN or ICM are chosen.
     *
     * @param account account to validate
     * @param el exception listener to collect validation exceptions
     */
    private void validateServiceType(final Account account, final CompoundIllegalStateException el)
    {
        if (!account.isVpn() && !account.isIcm())
        {
            final String msg = "Account Type for account " + account.getBAN()
                    + " is MOM.  Specify a MOM type (either VPN or ICM or both).";
            el.thrown(new IllegalPropertyArgumentException("MOM Type", msg));
        }
    }

    private void validateVpnMsisdn(final Context ctx, final Account account, final CompoundIllegalStateException el)
        throws HomeException
    {
        final String ban = account.getBAN();
        if (!account.isResponsible() && !account.isRootAccount())
        {
            el.thrown(new IllegalPropertyArgumentException(AccountXInfo.TYPE,
                    "Only a root account can be a VPN account."));
        }

        // first validate MSISDN to be that of the a valid immediate subscriber
        final String vpnMsisdn = account.getVpnMSISDN();

        if (vpnMsisdn != null && vpnMsisdn.length() != 0)
        {
            final Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, vpnMsisdn);

            if (sub == null)
            {
                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.VPN_MSISDN,
                        " \"" + vpnMsisdn + "\" not assigned to a subscriber yet"));
            }
            else
            {
                final Account subscriberAccount = SubscriberSupport.lookupAccount(ctx, sub);
                if (subscriberAccount == null)
                {
                    el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.BAN,
                            "Cannot find Account " + sub.getBAN()));
                }
                else
                {
                    final Account rootAccount = subscriberAccount.getRootAccount(ctx);
                    if (!SafetyUtil.safeEquals(rootAccount.getBAN(), ban))
                    {
                        el.thrown(new IllegalPropertyArgumentException(AccountXInfo.VPN_MSISDN,
                                "Conflict between this account (" + ban + ") and " + vpnMsisdn
                                + "'s Root account (" + rootAccount.getBAN() + "). "
                                + AccountXInfo.VPN_MSISDN.getName()
                                + " should be under a subscriber sub-account of the VPN root account."));
                    }
                }
            }
        }
        else
        {
            // VPN MSISDN must be non-empty if the account has subscribers with VPN service
            final Home vpnSubHome = (Home) ctx.get(VpnAuxiliarySubscriberHome.class);
            final And whereClause = new And();
            whereClause.add(new Limit(1));
            whereClause.add(new EQ(VpnAuxiliarySubscriberXInfo.ACCOUNT, account.getBAN()));
            final Collection vpnSubs = vpnSubHome.select(ctx, whereClause);
            if (vpnSubs == null || vpnSubs.size() > 0)
            {
                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.VPN_MSISDN,
                        "Cannot be empty when there are subscribers with VPN service in the account."));
            }
        }
    }
}
