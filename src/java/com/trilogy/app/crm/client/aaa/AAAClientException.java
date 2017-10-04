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
package com.trilogy.app.crm.client.aaa;


/**
 * Provides a common base class from which to derive exceptions specific to the
 * AAAClient.
 *
 * @author gary.anderson@redknee.com
 */
public
class AAAClientException
    extends Exception
{
    /**
     * Creates a new AAAClientException for the given message.
     *
     * @param message The detail message to include in the exception.
     */
    public AAAClientException(final String message)
    {
        super(message);
    }


    /**
     * Creates a new AAAClientException for the given cause.
     *
     * @param throwable The underlying exception that caused this
     * AAAClientException to be thrown.
     */
    public AAAClientException(final Throwable throwable)
    {
        super(throwable);
    }


    /**
     * Creates a new AAAClientException for the given cause.
     *
     * @param message The detail message to include in the exception.
     * @param throwable The underlying exception that caused this
     * AAAClientException to be thrown.
     */
    public AAAClientException(final String message, final Throwable throwable)
    {
        super(message, throwable);
    }

} // class
