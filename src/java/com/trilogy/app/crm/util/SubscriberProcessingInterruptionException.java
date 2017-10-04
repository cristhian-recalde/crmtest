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

import com.trilogy.app.crm.bean.Subscriber;


/**
 * Thrown to indicate that a problem has occurred in the processing of
 * subscribers that should cause the processing to abort.
 *
 * @author gary.anderson@redknee.com
 */
public
class SubscriberProcessingInterruptionException
    extends Exception
{
    /**
     * Creates a new exception to indicate that a problem has occurred in the
     * processing of subscribers.
     *
     * @param message The detailed exception message.
     * @param subscriber The subscriber that was being processed.
     */
    public SubscriberProcessingInterruptionException(
        final String message,
        final Subscriber subscriber)
    {
        super(message);
        subscriber_ = subscriber;
    }


    /**
     * Creates a new exception to indicate that a problem has occurred in the
     * processing of subscribers.
     *
     * @param message The detailed exception message.
     * @param subscriber The subscriber that was being processed.
     * @param cause The initiating cause of the interruption.
     */
    public SubscriberProcessingInterruptionException(
        final String message,
        final Subscriber subscriber,
        final Throwable cause)
    {
        super(message, cause);
        subscriber_ = subscriber;
    }


    /**
     * Gets the subscriber being processed at the time of the interruption.
     *
     * @return The subscriber being processed at the time of the interruption.
     */
    public Subscriber getSubscriber()
    {
        return subscriber_;
    }


    /**
     * The subscriber being processed at the time of the interruption.
     */
    private final Subscriber subscriber_;

} // class
