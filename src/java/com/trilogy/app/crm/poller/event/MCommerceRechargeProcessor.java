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

package com.trilogy.app.crm.poller.event;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.poller.agent.MCommerceRechargeAgent;


/**
 * This class is called to process every one of the ER1251 parsed from the MCommerce ER
 * files.
 *
 * @author danny.ng@redknee.com
 * @since Mar 03, 2006
 */
public class MCommerceRechargeProcessor extends CRMProcessor
{

    /**
     * Create a new instance of <code>MCommerceRechargeProcessor</code>.
     *
     * @param ctx
     *            The operating context.
     * @param queueSize
     *            Queue size.
     * @param threads
     *            Thread pool size.
     */
    public MCommerceRechargeProcessor(final Context ctx, final int queueSize, final int threads)
    {
        super();
        init(ctx, "MCommerceRecharge", "MCommerceRechargeERErrFile", queueSize, threads, new MCommerceRechargeAgent(
            this));
    }
}
