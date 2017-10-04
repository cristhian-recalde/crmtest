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

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.account.state.AccountStateTransitionSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;


/**
 * This webcontrol displays permissible state changes for accounts.
 *
 * @author lily.zou@redknee.com
 * @date Dec 14, 2003
 */

public class AccountStateChangeLimitWebControl extends PrimitiveWebControl

{

    /**
     * The extent of Enum values to choose from.
     */
    protected EnumCollection enum_;

    /**
     * Flag indicating if webpage gets submitted as "Preview" mode whenever user changes
     * select's option value
     */
    protected final boolean autoPreview_;


    /**
     * Create a new instance of <code>AccountStateChangeLimitWebControl</code>.
     *
     * @param stateEnum
     *            Available states.
     * @param autoPreview
     *            Auto preview.
     */
    public AccountStateChangeLimitWebControl(final EnumCollection stateEnum, final boolean autoPreview)
    {
        enum_ = stateEnum;
        autoPreview_ = autoPreview;
    }


    /**
     * {@inheritDoc}
     */
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final Enum enumeration = (Enum) obj;

        Account realAccount = null;

        /*
         * this is the enum which changes based on the account real state ( retrieved from
         * db )
         */
        EnumCollection newEnum = null;

        final Account account = (Account) ctx.get(AbstractWebControl.BEAN);

        // not creating a new bean
        if (account.getBAN() != null && account.getBAN().length() != 0)
        {
            try
            {
                realAccount = (Account) ((Home) ctx.get(AccountHome.class)).find(ctx, account.getBAN());
            }
            catch (final Exception e)
            {
                new MinorLogMsg(this, "No Account [BAN=" + account.getBAN() + "] found in Database", e).log(ctx);
            }
        }

        if (!account.isResponsible() && account.getState() == AccountStateEnum.PROMISE_TO_PAY)
        {
            out.print(account.getState());
        }
        else
        {
            newEnum = AccountStateTransitionSupport.instance().getPossibleManualStateEnumCollection(ctx, realAccount);
            new EnumWebControl(newEnum, true).toWeb(ctx, out, name, enumeration);
        }
    }


    /**
     * {@inheritDoc}
     */
    public Object fromWeb(final Context ctx, final ServletRequest req, final String name) throws NullPointerException
    {
        return new EnumWebControl(enum_, true).fromWeb(ctx, req, name);
    }
}
