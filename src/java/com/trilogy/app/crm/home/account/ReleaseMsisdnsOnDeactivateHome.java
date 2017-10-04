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

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;

/**
 * This Home decorator releases the IN_USE MSISDNs when the account is deactivated.
 *
 * @author victor.stratan@redknee.com
 */
public class ReleaseMsisdnsOnDeactivateHome extends HomeProxy
{
    /**
     * Creates a new BM provisioning decorator.
     *
     * @param ctx The operating context.
     * @param delegate The Home to which this proxy delegates.
     */
    public ReleaseMsisdnsOnDeactivateHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        final Account resultAccount = (Account) super.store(ctx, obj);

        if (oldAccount.getState() != AccountStateEnum.INACTIVE
                && resultAccount.getState() == AccountStateEnum.INACTIVE)
        {
            ensureAccountMsisdnsReleased(ctx, oldAccount);
        }

        return resultAccount;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        ensureAccountMsisdnsReleased(ctx, (Account)obj);
        super.remove(ctx, obj);
    }


    /**
     * Ensures that any aquired MSISDNs are released.
     *
     * @param ctx The operating context.
     * @param account The account for which to ensure there is no profile.
     */
    private void ensureAccountMsisdnsReleased(final Context ctx, final Account account)
    {
        final Home msisdnHome = (Home) ctx.get(MsisdnHome.class);
        final And condition = new And();
        condition.add(new EQ(MsisdnXInfo.BAN, account.getBAN()));
        condition.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.IN_USE));
        final Home whereHome = msisdnHome.where(ctx, condition);

        try
        {
            whereHome.forEach(ctx, new ReleaseMsisdnVisitor());
        }
        catch (final HomeException exception)
        {
            notifyExceptionListeners(ctx, exception);
        }
    }


    /**
     * Notifies any exception listener in the context that an exception has
     * occurred.
     *
     * @param ctx The operating context.
     * @param throwable The throwable to pass to the exception listener.
     */
    private void notifyExceptionListeners(final Context ctx, final Throwable throwable)
    {
        final ExceptionListener listener = (ExceptionListener)ctx.get(ExceptionListener.class);
        if (listener != null)
        {
            listener.thrown(throwable);
        }
    }
}

class ReleaseMsisdnVisitor implements Visitor
{
    public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    {
        final Msisdn ownership = (Msisdn) obj;
        try
        {
            MsisdnManagement.releaseMsisdn(ctx, ownership.getMsisdn(), ownership.getBAN(), "");
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Error while releasing MSISDN [" + ownership.getMsisdn()
                    + "] aqured by BAN [" + ownership.getBAN() + "]", e);
        }
    }
}