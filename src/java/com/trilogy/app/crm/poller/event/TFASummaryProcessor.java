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

import com.trilogy.app.crm.poller.agent.TFASummaryTransferAgent;
import com.trilogy.framework.xhome.context.Context;


/**
 * This class is called to process TFA ER 290 from TFA ER files
 * 
 * @author simar.singh@redknee.com
 *
 */
public class TFASummaryProcessor extends TFAProcessor
{
    public static final int TFA_SUMMARY_ER_IDENTIFIER = 290;
 
    public TFASummaryProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, "TFASummaryProcessor", "TFASummaryProcessorERErrFile", queueSize, threads, new TFASummaryTransferAgent(this));
    }
}
