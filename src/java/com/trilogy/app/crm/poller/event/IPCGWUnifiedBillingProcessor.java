/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of REDKNEE.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with REDKNEE.
 *
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */
package com.trilogy.app.crm.poller.event;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.poller.agent.IPCGWUnifiedBillingAgent;


/**
 * This class is called to process every one of the ER501 parsed from the IPCW ER files.
 *
 * @author larry.xia@redknee.com
 */
public class IPCGWUnifiedBillingProcessor extends IPCGWProcessor
{

    /**
     * Identifier for IPCG unified billing ER.
     */
    public static final int IPCG_UNIFIED_BILLING_ER_IDENTIFIER = 501;


    /**
     * Create a new instance of <code>IPCGWUnifiedBillingProcessor</code>.
     *
     * @param ctx
     *            The operating context.
     * @param queueSize
     *            Queue size.
     * @param threads
     *            Number of threads.
     */
    public IPCGWUnifiedBillingProcessor(final Context ctx, final int queueSize, final int threads)
    {
        super();
        init(ctx, "IPCGWUnifiedBilling", "IPCGWUnifiedBillingERErrFile", queueSize, threads,
            new IPCGWUnifiedBillingAgent(this));
    }
}
