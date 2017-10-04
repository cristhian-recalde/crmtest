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
package com.trilogy.app.crm.poller;

import java.text.ParseException;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.calldetail.CallDetail;


/**
 * Interface to a creator of call detail based on a particular ER.
 *
 * @author arturo.medina@redknee.com.
 */
public interface CallDetailCreator
{

    /**
     * Creates the call detail based on a list of parameters and the ER to generate.
     *
     * @param ctx
     *            The operating context.
     * @param info
     *            ER processor info.
     * @param params
     *            Parameters of the call detail.
     * @return The created call detail.
     * @throws ParseException
     *             Thrown if there are problems parsing the ER.
     * @throws HomeException
     *             Thrown if there are data storage-related problems (e.g. failed data
     *             lookup).
     * @throws AgentException
     *             Thrown if there are operational problems with the ER processing.
     */
    CallDetail createCallDetail(Context ctx, ProcessorInfo info, List<String> params) throws ParseException,
        HomeException, AgentException;


    /**
     * Creates multiple call details based on a list of parameters and a received ER. This
     * is necessary in addition to {{@link #createCallDetail(Context, ProcessorInfo, List)}}
     * because a single ER may generate multiple call details.
     *
     * @param ctx
     *            The operating context.
     * @param info
     *            ER processor info.
     * @param params
     *            Parameters of the call detail.
     * @return A collection of created call details.
     * @throws ParseException
     *             Thrown if there are problems parsing the ER.
     * @throws HomeException
     *             Thrown if there are data storage-related problems (e.g. failed data
     *             lookup).
     * @throws AgentException
     *             Thrown if there are operational problems with the ER processing.
     */
    List<CallDetail> createCallDetails(Context ctx, ProcessorInfo info, List<String> params) throws ParseException,
        HomeException, AgentException;
}
