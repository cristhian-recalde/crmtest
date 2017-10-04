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

import com.trilogy.app.crm.poller.agent.TFAFailedTransferAgent;
import com.trilogy.framework.xhome.context.Context;


/**
 * This class is called to process ER 293 from the TFA ER files
 * 
 * @author ltang
 *
 */
public class TFAFailedTransferProcessor extends TFAProcessor
{
    public static final int TFA_FAILED_TRANSFER_ER_IDENTIFIER = 293;
    
    public TFAFailedTransferProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, "TFAFailedTransfer", "TFAFailedTransferERErrFile", queueSize, threads, new TFAFailedTransferAgent(this));
    }
}
