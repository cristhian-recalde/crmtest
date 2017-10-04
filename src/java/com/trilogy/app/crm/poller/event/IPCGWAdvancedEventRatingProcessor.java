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

import com.trilogy.app.crm.poller.agent.IPCGWAdvancedEventRatingAgent;


/**
 * This class is called to process the 511 ERs parsed from the IPCW ER files.
 *
 * @author jimmy.ng@redknee.com
 */
public class IPCGWAdvancedEventRatingProcessor extends IPCGWProcessor
{

    /**
     * ER number of the IPCG advanced event rating ER.
     */
    public static final int IPCG_ADVANCED_EVENT_RATING_ER_IDENTIFIER = 511;


    /**
     * Create a new instance of <code>IPCGWAdvancedEventRatingProcessor</code>.
     *
     * @param ctx
     *            The operating context.
     * @param queueSize
     *            Queue size.
     * @param threads
     *            Number of threads allocated for this processor.
     */
    public IPCGWAdvancedEventRatingProcessor(final Context ctx, final int queueSize, final int threads)
    {
        super();
        init(ctx, "IPCGWAdvancedEventRating", "IPCGWAdvancedEventRatingERErrFile", queueSize, threads,
            new IPCGWAdvancedEventRatingAgent(this));
    }
}
