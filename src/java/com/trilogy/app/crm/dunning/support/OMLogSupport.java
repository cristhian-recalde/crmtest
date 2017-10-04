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

/**
 * Proivides support for generating Dunning Action OM log messages.
 *
 * @author gary.anderson@redknee.com
 */
public interface OMLogSupport
{
    /**
     * Generates an "attempt" log message.
     *
     * @param ctx The operating context.
     */
    void attempt(final Context ctx);

    /**
     * Generates a "fail" log message.
     *
     * @param ctx The operating context.
     */
    void fail(final Context ctx);

    /**
     * Generates a "success" message.
     *
     * @param ctx The operating context.
     */
    void success(final Context ctx);

} // interface
