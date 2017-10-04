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
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;


/**
 * Adapts a SubscriberProcessor to make it work like a Predicate.
 *
 * @author gary.anderson@redknee.com
 */
final
class SubscriberProcessorToPredicateAdapter
    implements Predicate
{
    /**
     * Creates a new adapter for the given SubscriberProcessor.
     *
     * @param context The operating context.
     * @param processor The SubscriberProcessor to adapt.
     */
    public SubscriberProcessorToPredicateAdapter(
        final Context context,
        final SubscriberProcessor processor)
    {
        context_ = context;
        processor_ = processor;
        interruption_ = null;
    }


    /**
     * {@inheritDoc}
     */
    public boolean f(Context ctx,final Object object)
    {
        final Subscriber subscriber = (Subscriber)object;

        try
        {
            processor_.process(context_, subscriber);
            return true;
        }
        catch (final SubscriberProcessingInterruptionException exception)
        {
            new MinorLogMsg(
                this,
                "Subscriber Processing aborted for subscriber "
                + exception.getSubscriber().getId(),
                exception).log(context_);

            interruption_ = exception;
            return false;
        }
    }


    /**
     * Clears the interruption if one was set.
     */
    public void clearInterruption()
    {
        interruption_ = null;
    }


    /**
     * Gets the most recently caught SubscriberProcessingInterruptionException if one
     * was raised; null otherwise.
     *
     * @return The most recently caught SubscriberProcessingInterruptionException if one
     * was raised; null otherwise.
     */
    public SubscriberProcessingInterruptionException getInterruption()
    {
        return interruption_;
    }


    /**
     * Indicates whether or not this processor was interrupted during one of the
     * previous calls to f().
     *
     * @return True if this processor was interrupted during one of the previous
     * calls to f(); false otherwise.
     */
    public boolean isInterrupted()
    {
        return interruption_ != null;
    }


    /**
     * The operating context.
     */
    private final Context context_;

    /**
     * The SubscriberProcessor being adapted.
     */
    private final SubscriberProcessor processor_;

    /**
     * The most recently caught SubscriberProcessingInterruptionException if one
     * was raised; null otherwise.
     */
    private SubscriberProcessingInterruptionException interruption_;

} // class
