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

import java.io.PrintWriter;
import java.io.StringWriter;

import com.trilogy.framework.xhome.beans.ExceptionListener;


/**
 * Adapts a MessageHandler so that it may be used as an ExceptionListener.
 *
 * @author gary.anderson@redknee.com
 */
public final
class MessageHandlerToExceptionListenerAdapter
    implements ExceptionListener
{
    /**
     * Creates a new adapter for the given handler.
     *
     * @param handler The handler to adapt.
     */
    public MessageHandlerToExceptionListenerAdapter(final MessageHandler handler)
    {
        if (handler == null)
        {
            throw new IllegalArgumentException("The handler is null.");
        }

        handler_ = handler;
    }

    /**
     * {@inheritDoc}
     */
    public void thrown(final Throwable throwable)
    {
        handler_.print("Exception caught: " + throwable.getMessage());

        final StringWriter buffer = new StringWriter();
        final PrintWriter writer = new PrintWriter(buffer);

        throwable.printStackTrace(writer);

        writer.flush();

        handler_.print(buffer.toString());
        writer.close();
    }


    /**
     * The handler to adapt.
     */
    private final MessageHandler handler_;

} // class
