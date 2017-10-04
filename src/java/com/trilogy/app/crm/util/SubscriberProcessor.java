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
package com.trilogy.app.crm.util;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;


/**
 * Processes a single subscriber.
 *
 * @author gary.anderson@redknee.com
 */
public
interface SubscriberProcessor
{
    /**
     * Processes the given subscriber.
     *
     * @param context The operating context.
     * @param subscriber The subscriber to process.
     *
     * @exception SubscriberProcessingInterruptionException Thrown if a problem
     * occured during processing that should gracefully halt processing of
     * further subscribers.
     */
    void process(Context context, Subscriber subscriber)
        throws SubscriberProcessingInterruptionException;


} // interface
