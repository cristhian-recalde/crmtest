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
package com.trilogy.app.crm.bulkloader.generic;

import com.trilogy.app.crm.invoice.process.ProducerAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
/**
 * Implement Multi-threaded Generic Bean Bulkloader.
 *
 * The CRM Design team wants to create and use one single CRM
 * ThreadPool, and to use Sub-Pooling.  The implementation will take too long 
 * and cause a massive Regression testing.
 * So for now we continue to create individual pools and mark the refactoring.
 * In this case we're even reusing the Invoice Processing Multi-threading agents.  Waste not, want not!
 * 
 * @author angie.li@redknee.com
 * @since 8.2
 */
public class BulkloadProducerAgent extends ProducerAgent 
{
    public BulkloadProducerAgent(Context ctx,
            ContextAgent agent,
            String threadName,
            int threadSize,
            int queueSize)
    {
        super (ctx, agent, threadName, threadSize, queueSize);
    }
}
