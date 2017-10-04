/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.poller;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.poller.event.*;

/**
 * @author simar.singh@redknee.com
 * @category Parses one Subscriber Bucket Provisioning Event Record
 *         - ER 1314 and acts on CRM Model
 */
public class BucketProvEventProcessor extends CRMProcessor implements Constants
{

    /**
     *
     *
     * @param date
     *            the date of the ER
     * @param erid
     *            the id of the ER
     * @param record
     *            the ER itself
     * @param startIndex
     *            the index of the first nonparsed char in the ER
     * @throws NumberFormatException
     * @throws IndexOutOfBoundsException
     * @see com.redknee.service.poller.nbio.event.EventProcessor#process(long,java.lang.String,
     *      char[], int)
     */
    public void process(long date, String erid, char[] record, int startIndex) throws NumberFormatException,
            IndexOutOfBoundsException
    {
        Context ctx = getContext().createSubContext();
        ctx.put(ProcessorInfo.class, new ProcessorInfo(date, erid, record, startIndex));
        try
        {
            threadPool_.execute(ctx);
        }
        catch (AgentException e)
        {
            new MinorLogMsg(this, "Failed to process ER 1314 because of Exception " + e.getMessage(), e)
                    .log(getContext());
            saveErrorRecord(ctx, record);
        }
    }


    /**
     * @param ctx
     * @param queueSize
     * @param threads
     */
    public BucketProvEventProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, NAME, ER_PREFIX, queueSize, threads, new BucketProvEventAgent(this));
    }

    public static final String NAME = "BucketProvEvent";
    private static final String ER_PREFIX = "BucketPRovEventERErrFile";
}
