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

import java.util.Collection;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.AccountCategory;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Class for implementing family plan.
 * 
 * @author ankit.nagpal
 * @since 9_7_2
 */

public class AccountFamilyPlanHome extends HomeProxy
{

    /**
     * Create a new instance of <code>AccountProvisioningHome</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this home.
     */
    public AccountFamilyPlanHome(final Context ctx, final Home delegate)
    {
        super(delegate);
        setContext(ctx);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        if (!(obj instanceof Account))
        {
            throw new HomeException("System Error: cannot save a non-account to AccountHome");
        }

        Account account = (Account) obj;
        int numAcct = -1;
        
        if (account != null)
        {
            final Account parentAccount = account.getParentAccount(ctx);
            if(parentAccount != null && parentAccount.isPooled(ctx))
            {
                try
                {
                    numAcct = AccountSupport.getImmediateChildrenActiveAccountCount(ctx, parentAccount.getBAN());
                }
                catch (final HomeException exception)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, AccountFamilyPlanHome.class, "Not able to find sub accounts", exception);
                    }
                }
                Home home = (Home) ctx.get(AccountCategoryHome.class);
                
                AccountCategory accountCategory = (AccountCategory) home.find(ctx_, new EQ(
                        AccountCategoryXInfo.IDENTIFIER, parentAccount.getType()));
                if (numAcct == 0 && accountCategory != null && account.getSubscriberType().equals(SubscriberTypeEnum.PREPAID)
                        && accountCategory.getCustomerType().equals(CustomerTypeEnum.FAMILY))
                {
                    int numPostpaidSub = AccountSupport.getNumberOfActivePostpaidSubscribersInTopology(ctx,
                            parentAccount);
                    if (numPostpaidSub <= 1)
                    {
                        throw new HomeException("Cannot add new account till an active owner is added");
                    }
                }
            }
        }
        return super.create(ctx, account);
    }
}
