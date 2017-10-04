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

import com.trilogy.app.crm.poller.agent.TFABillingAgent;
import com.trilogy.framework.xhome.context.Context;


/**
 * This class is called to process CDR 375 from TFA CDR files
 * 
 * @author ltang
 *
 */
public class TFABillingProcessor extends TFAProcessor
{
    public static final int TFA_CDR_IDENTIFIER = 375;

    public static final String TRANSACTION_FROM_TFA_CDR_POLLER    = "Transaction from TFA CDR poller";
 
    public TFABillingProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, "TFABillingProcessor", "TFABillingProcessorERErrFile", queueSize, threads, new TFABillingAgent(this));
    }
}
