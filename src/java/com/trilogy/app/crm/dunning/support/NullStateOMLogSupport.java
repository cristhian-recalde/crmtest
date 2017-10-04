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
package com.trilogy.app.crm.dunning.support;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AccountStateEnum;

/**
 * Proivides support for generating debug log messages rather than OMLogMessages
 * when an account has an unexpected or unknown state.
 *
 * @author gary.anderson@redknee.com
 */
public class NullStateOMLogSupport implements OMLogSupport
{
    /**
     * Creates a new NullStateOMLogSupport.
     *
     * @param state The account state that for which this null log support
     * object generates messages.
     */
    public NullStateOMLogSupport(final AccountStateEnum state)
    {
        state_ = state;
    }

    // INHERIT
    public void attempt(final Context ctx)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(
                    getClass().getName(),
                    "Attempt OM message should have been generated for " + state_,
                    null).log(ctx);
        }
    }

    // INHERIT
    public void fail(final Context ctx)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(
                    getClass().getName(),
                    "Fail OM message should have been generated for " + state_,
                    null).log(ctx);
        }
    }

    // INHERIT
    public void success(final Context ctx)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(
                    getClass().getName(),
                    "Success OM message should have been generated for " + state_,
                    null).log(ctx);
        }
    }

    /**
     * The account state that for which this null log support object generates
     * messages.
     */
    protected final AccountStateEnum state_;

} // class
