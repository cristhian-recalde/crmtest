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
package com.trilogy.app.crm.home.account;

import java.util.List;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.extension.account.AbstractSubscriberLimitExtension;
import com.trilogy.app.crm.extension.account.AccountExtensionXInfo;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtension;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SubscriberLimitSupport;

/**
 * Validations done on the Account hierarchy.
 *
 * @author joe.chen@redknee.com
 */
public class AccountHierachyValidator implements Validator
{

    public static final String BYPASS_CHANGE_CHILD_ACCOUNT_STATE_VALIDATION =
            "bypass change child account state validation";

    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Account account = (Account) obj;

        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        try
        {
            validateParentValid(ctx, account, el);
            validateRootMustResponsible(ctx, account, el);
            validateChangeChildAccountState(ctx, account, el);
            validateAddAccountToParentAccount(ctx, account, el);
        }
        catch (HomeException e)
        {
            el.thrown(e);
        }

        el.throwAll();
    }

    void validateParentValid(final Context ctx, final Account account, final CompoundIllegalStateException el)
        throws HomeException
    {
        final String parentBAN = account.getParentBAN();
        if (parentBAN != null && parentBAN.length() > 0)
        {
            final Account parentAcc = AccountSupport.getAccount(ctx, parentBAN);
            if (parentAcc == null)
            {
                el.thrown(new IllegalPropertyArgumentException(
                        AccountXInfo.PARENT_BAN,
                        "Invalid parent BAN."));
            }
            // TODO return the retreived parent account and pass it as a parametter into validateChangeChildAccountState
        }
    }

    void validateRootMustResponsible(final Context ctx, final Account account, final CompoundIllegalStateException el)
    {
        if (account.isRootAccount() && !account.isResponsible())
        {
            el.thrown(new IllegalPropertyArgumentException(
                    AccountXInfo.RESPONSIBLE,
                    "Root account must be responsible."));
        }
    }

    /**
     * @param ctx the operating context
     * @param account Account to validate
     * @param el where the exceptions will be placed
     */
    void validateChangeChildAccountState(final Context ctx, final Account account,
            final CompoundIllegalStateException el)
    {
        if (ctx.getBoolean(BYPASS_CHANGE_CHILD_ACCOUNT_STATE_VALIDATION, false))
        {
            return;
        }
        if (!account.isRootAccount() && !account.getResponsible())
        {
            try
            {
                final Account parentAccount = account.getParentAccount(ctx);
                final int state = account.getState().getIndex();
                final int parentState = parentAccount.getState().getIndex();
                if (parentState == AccountStateEnum.SUSPENDED_INDEX
                        && state != AccountStateEnum.INACTIVE_INDEX
                        && state != AccountStateEnum.SUSPENDED_INDEX)
                {
                    el.thrown(new IllegalPropertyArgumentException(
                            AccountXInfo.STATE,
                            "Parent Account" + parentAccount.getBAN()
                                    + " State is suspeneded, Child Account " + account.getBAN()
                                    + " cannot be Active"));
                }
                if (parentState == AccountStateEnum.INACTIVE_INDEX
                        && state != AccountStateEnum.INACTIVE_INDEX)
                {
                    el.thrown(new IllegalPropertyArgumentException(
                            AccountXInfo.STATE,
                            "Parent Account " + parentAccount.getBAN()
                                    + " State is Inactive, Child Account " + account.getBAN()
                                    + " not in Inactive state"));
                }
            }
            catch (HomeException e)
            {
                final Exception propExpt = new IllegalPropertyArgumentException(
                        AccountXInfo.STATE,
                        "Unable to validate Account State.");
                propExpt.initCause(e);
                el.thrown(propExpt);
            }
        }
    }
    
    /**
     * Validates whether adding a subscriber to an account would violate the subscriber
     * limit.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            Account to be validated.
     * @throws HomeException 
     */
    public static void validateAddAccountToParentAccount(final Context context, final Account account, final CompoundIllegalStateException el) throws HomeException
    {
        List<SubscriberLimitExtension> extensions = null;
        
        final String parentBAN = account.getParentBAN();
        
        if(parentBAN != null)
        {
            extensions = ExtensionSupportHelper.get(context).getExtensions(context,
                    SubscriberLimitExtension.class, new EQ(AccountExtensionXInfo.BAN, parentBAN));
        }
          
        if (extensions != null)
        {
            for (final SubscriberLimitExtension extension : extensions)
            {
                if (extension == null)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        LogSupport.debug(context, AccountHierachyValidator.class, "No "
                            + ExtensionSupportHelper.get(context).getExtensionName(context, SubscriberLimitExtension.class)
                            + " exists for account " + account.getBAN());
                    }
                }
                else if (extension.getMaxSubscribers() == AbstractSubscriberLimitExtension.DEFAULT_MAXSUBSCRIBERS)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        LogSupport.debug(context, AccountHierachyValidator.class, extension.getName(context)
                            + " - limit not set properly for account " + account.getBAN() + ", skipping validation.");
                    }
                }
                else
                {
                    int numAccount = 1;
                    try
                    {
                            numAccount += AccountSupport.getImmediateChildrenActiveAccountCount(context, parentBAN);
                    }
                    catch (final HomeException exception)
                    {
                        if (LogSupport.isDebugEnabled(context))
                        {
                            final StringBuilder sb = new StringBuilder();
                            sb.append(exception.getClass().getSimpleName());
                            sb.append(" caught in ");
                            sb.append("AccountHierachyValidator.validateAddAccountToParentAccount(): ");
                            if (exception.getMessage() != null)
                            {
                                sb.append(exception.getMessage());
                            }
                            LogSupport.debug(context, AccountHierachyValidator.class, sb.toString(), exception);
                        }
    
                    }
                    if (numAccount > extension.getMaxSubscribers())
                    {
                        el.thrown(new IllegalStateException("Account creation failed: subscriber limit of account " + parentBAN
                            + " has been exceeded."));
                    }
                }
            }
        }
    }
}
