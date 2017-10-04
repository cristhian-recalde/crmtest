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
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;

/**
 * Proivides support for generating Dunning Active Action OM log messages.
 *
 * @author gary.anderson@redknee.com
 */
public class ActiveOMLogSupport implements OMLogSupport
{
    /**
     * {@inheritDoc}
     */
    public void attempt(final Context ctx)
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_DUNNING_ACTIVE_ACTION_ATTEMPT).log(ctx);
    }

    /**
     * {@inheritDoc}
     */
    public void fail(final Context ctx)
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_DUNNING_ACTIVE_ACTION_FAIL).log(ctx);
    }

    /**
     * {@inheritDoc}
     */
    public void success(final Context ctx)
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_DUNNING_ACTIVE_ACTION_SUCCESS).log(ctx);
    }

} // class
