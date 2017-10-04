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

import java.util.Collection;
import java.util.HashSet;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Home responsible to run the dunning process on a dunned account when its credit category is
 * changed to a dunning exempt one.
 * 
 * @author Marcio Marques
 * @since 9.0
 * 
 */
public class AccountCreditCategoryModificationHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new AccountCreditCategoryModificationHome object.
     * @param ctx
     * @param delegate
     */
    public AccountCreditCategoryModificationHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx, Object obj) throws HomeException
    {
        Account account = (Account) obj;
        Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        
        if (account.getCreditCategory() != oldAccount.getCreditCategory())
        {
            CreditCategory oldCreditCategory = (CreditCategory) HomeSupportHelper.get(ctx).findBean(ctx,
                    CreditCategory.class,
                    new EQ(CreditCategoryXInfo.CODE, Integer.valueOf(oldAccount.getCreditCategory())));
            CreditCategory newCreditCategory = (CreditCategory) HomeSupportHelper.get(ctx).findBean(ctx,
                    CreditCategory.class,
                    new EQ(CreditCategoryXInfo.CODE, Integer.valueOf(account.getCreditCategory())));
            Collection<AccountStateEnum> dunningStates = new HashSet<AccountStateEnum>();
            dunningStates.add(AccountStateEnum.NON_PAYMENT_WARN);
            dunningStates.add(AccountStateEnum.NON_PAYMENT_SUSPENDED);
            dunningStates.add(AccountStateEnum.IN_ARREARS);
            dunningStates.add(AccountStateEnum.PROMISE_TO_PAY);
            if (newCreditCategory.isDunningExempt() && !oldCreditCategory.isDunningExempt() &&
                    EnumStateSupportHelper.get(ctx).isOneOfStates(account, dunningStates))
            {
                account.setState(AccountStateEnum.ACTIVE);
            }
        }
        return super.store(ctx, account);
    }
}