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
package com.trilogy.app.crm.checking;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.support.CollectionSupportHelper;


/**
 * Allows a Collection of MessageHandlers to be written to as if they were a
 * single MessageHandler.
 *
 * @author gary.anderson@redknee.com
 */
public final
class MessageHandlerCollectionProxy
	extends ContextAwareSupport
    implements MessageHandler
{
    /**
     * Creates a new, empty MessageHandlerCollectionProxy.
     */
    public MessageHandlerCollectionProxy(Context ctx)
    {
    	setContext(ctx);
        handlers_ = new ArrayList();
    }


    /**
     * Adds another MessageHandler to the collection to which we delegate.
     *
     * @param handler Another MessageHandler to which we delegate.
     */
    public void addHandler(final MessageHandler handler)
    {
        if (!handlers_.contains(handler))
        {
            handlers_.add(handler);
        }
    }


    /**
     * {@inheritDoc}
     */
    public void print(final String message)
    {
        // Dispatcher is an inner class defined below.
        CollectionSupportHelper.get(getContext()).forEach(getContext(),handlers_, new Dispatcher(message));
    }


    /**
     * The list of MessageHandlers to which we delegate.
     */
    private final List handlers_;


    /**
     * Creates a dispatcher for a given message.
     */
    private static final class Dispatcher
        implements Predicate
    {
        /**
         * Creates the dispacter with the given message.
         *
         * @param message The message to dispatch.
         */
        public Dispatcher(final String message)
        {
            message_ = message;
        }


        /**
         * {@inheritDoc}
         */
        public boolean f(Context ctx,final Object object)
        {
            final MessageHandler handler = (MessageHandler)object;
            handler.print(message_);
            return true;
        }


        /**
         * The message to send.
         */
        private final String message_;
    }

} // class
