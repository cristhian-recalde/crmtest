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

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;

/**
 * Put some commonly looked up value in the context so that the AccountHome pipeline doesn't need to look them up
 * again and again.
 *
 * @author victor.stratan@redknee.com
 */
public class AccountPipeLineContextPrepareHome extends HomeProxy
{
    /**
     * Contructor takes the delegate reference.
     *
     * @param delegate the home decorator to delegate to
     */
    public AccountPipeLineContextPrepareHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Context preppedCtx = prepareContext(ctx, (Account) obj, true);
        final Object result = super.create(preppedCtx, obj);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object store(final Context ctx, final Object bean) throws HomeException
    {
        final Context preppedCtx = prepareContext(ctx, (Account) bean, false);
        final Object result = super.store(preppedCtx, bean);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        final Context preppedCtx = prepareContext(ctx, (Account) obj, false);
        super.remove(preppedCtx, obj);
        FrameworkSupportHelper.get(preppedCtx).printCapturedExceptions(preppedCtx);
    }

    /**
     * Populate context with looked up values and other utility objects.
     *
     * @param parentCtx  the current context
     * @param account    the current account bean
     * @param fromCreate true if the call is from create() call
     * @return the resulting prepared context
     * @throws HomeException thrown if necessary object is missing or if thrown by underlying calls
     */
    protected Context prepareContext(final Context parentCtx, final Account account, final boolean fromCreate)
            throws HomeException
    {
        final Context ctx = parentCtx.createSubContext();
        ctx.setName("Account Context Prepare");

        final MessageMgr mgr = new MessageMgr(ctx, this);

        final Object oldAccount;
        if (fromCreate)
        {
            // setting OLD_ACCOUNT to null. in case there is already an OLD_ACCOUNT referenced in the context.
            oldAccount = null;
        }
        else
        {
            // look up existing Account
            oldAccount = super.find(ctx, account.getBAN());
            if (oldAccount == null)
            {
                final String msg = mgr.get(NO_ACCOUNT_ERROR_KEY, NO_ACCOUNT_ERROR, new String[]{account.getBAN()});
                throw new HomeException(msg);
            }
        }
        ctx.put(AccountConstants.OLD_ACCOUNT, oldAccount);

        if (!ctx.has(HTMLExceptionListener.class))
        {
            if (!ctx.has(ExceptionListener.class) || !(ctx.get(ExceptionListener.class) instanceof HTMLExceptionListener))
            {
                final HTMLExceptionListener el = new HTMLExceptionListener(mgr);
                ctx.put(HTMLExceptionListener.class, el);
            }
            else
            {
                ctx.put(HTMLExceptionListener.class, ctx.get(ExceptionListener.class));
            }
        }
        
        Context appCtx = (Context) ctx.get("app");
        appCtx.put("newPricePlanChange", false);
        appCtx.put("groupPricePlanChange", false);

        return ctx;
    }

    /**
     * Message Manager key for missing account error message.
     */
    private static final String NO_ACCOUNT_ERROR_KEY = "NO_ACCOUNT_ERROR_KEY";

    /**
     * Default missing account error message.
     */
    private static final String NO_ACCOUNT_ERROR = "Ubable to find account \"{0}\" used in Home call.";
}
