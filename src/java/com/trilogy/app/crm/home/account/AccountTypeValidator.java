/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.account;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;


/**
 * Can only be used in Account Home pipeline.
 * 
 * @author paul.sperneac@redknee.com
 */
public class AccountTypeValidator implements Validator
{

    private static AccountTypeValidator instance_ = null;


    public static AccountTypeValidator instance()
    {
        if (instance_ == null)
        {
            instance_ = new AccountTypeValidator();
        }
        return instance_;
    }


    @Override
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Account account = (Account) obj;
        final RethrowExceptionListener el = new RethrowExceptionListener();
        final LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
		if (account.isPooled(ctx))
        {
            // If the account is prepaid, check if prepaid pooled accounts are licensed
            if (SafetyUtil.safeEquals(account.getSystemType(), SubscriberTypeEnum.PREPAID)
                    && !lMgr.isLicensed(ctx, LicenseConstants.PREPAID_GROUP_POOLED_LICENSE_KEY))
            {
                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.TYPE,
                        "No license exists for combination of Account Type/Billing Type: pooled/prepaid"));
            }
        }
        // Check for valid account type transition by comparing the new account with the
        // old account
        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        if (oldAccount != null)
        {
            if (account.isIndividual(ctx) && !oldAccount.isIndividual(ctx))
            {
                // in the normal case individual or subscriber account should not have
                // any sub accounts
                final EQ condition = new EQ(AccountXInfo.PARENT_BAN, account.getBAN());
                boolean hasChildAccounts = false;
                try
                {
                    hasChildAccounts = HomeSupportHelper.get(ctx).hasBeans(ctx, Account.class, condition);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(ServiceSupport.class, "Error determining whether or not account "
                            + account.getBAN() + " has child accounts.", e).log(ctx);
                }
                if (hasChildAccounts)
                {
                    el.thrown(new IllegalPropertyArgumentException(AccountXInfo.TYPE,
                            "Account type cannot be set to individual sub accounts are created in the account."));
                }
            }
			if (!account.isIndividual(ctx) && oldAccount.isIndividual(ctx))
            {
                // in the normal case group account should not have any subscriptions
                final EQ condition = new EQ(SubscriberXInfo.BAN, account.getBAN());
                boolean hasSubscriber = false;
                try
                {
                    hasSubscriber = HomeSupportHelper.get(ctx).hasBeans(ctx, Subscriber.class, condition);
                }
                catch (HomeException e)
                {
                    el
                            .thrown(new IllegalPropertyArgumentException(
                                    AccountXInfo.TYPE,
                                    "Account type cannot be set to Group if subscriptions are created in the account.  Error occurred counting subscribers."));
                }
                if (hasSubscriber)
                {
                    el.thrown(new IllegalPropertyArgumentException(AccountXInfo.TYPE,
                            "Account type cannot be set to Group if subscriptions are created in the account."));
                }
            }
			if (!oldAccount.isPooled(ctx) && account.isPooled(ctx))
            {
                // OID 36163 - Account Type conversion will be restricted for Prepaid
                // Accounts. They can't be converted to or from pooled.
                // MM 2 change: account can be converted to Pool account, but Pooled
                // cannot be converted to non pooled
                try
                {
                    long subscribersCount = HomeSupportHelper.get(ctx).getBeanCount(ctx, Subscriber.class, new EQ(SubscriberXInfo.BAN,
                            account.getBAN()));
                    if (subscribersCount > 0)
                    {
                        el.thrown(new IllegalPropertyArgumentException(AccountXInfo.TYPE,
                                "Account type cannot be converted to/from pooled because " + subscribersCount
                                        + " subscriber(s) exist in account " + account.getBAN()));
                    }
                }
                catch (HomeException e)
                {
                    el
                            .thrown(new IllegalPropertyArgumentException(
                                    AccountXInfo.TYPE,
                                    "Error occurred counting subscribers. Account type cannot set to Pooled because Subscriber subscriber(s) msy exist in account "
                                            + account.getBAN()));
                }
                try
                {
                    long responsibleAccountCount = HomeSupportHelper.get(ctx).getBeanCount(ctx, AccountHome.class, new And().add(
                            new EQ(AccountXInfo.PARENT_BAN, account.getBAN())).add(
                            new EQ(AccountXInfo.RESPONSIBLE, Boolean.TRUE)));
                    if (responsibleAccountCount > 0)
                    {
                        el.thrown(new IllegalPropertyArgumentException(AccountXInfo.TYPE,
                                "Account type cannot be converted to Pooled because " + responsibleAccountCount
                                        + " reponsible account(s) exist in account " + account.getBAN()));
                    }
                }
                catch (HomeException e)
                {
                    el
                            .thrown(new IllegalPropertyArgumentException(
                                    AccountXInfo.TYPE,
                                    "Error occoured while counting individual subscriber child accounts. Account type cannot be converted to/from pooled because individual subscriber accounts(s) may exist in account "
                                            + account.getBAN()));
                }
            }
			else if (oldAccount.isPooled(ctx) && !account.isPooled(ctx))
            {
                //accounts can not be converted from pool
                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.TYPE,
                        "Account type cannot be converted from Pooled."));
            }
        }
        el.throwAllAsCompoundException();
    }
}
