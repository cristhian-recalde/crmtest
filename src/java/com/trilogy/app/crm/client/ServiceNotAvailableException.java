/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client;


/**
 * Indicates that a requested service is unavailable.
 *
 * @author gary.anderson@redknee.com
 */
public
class ServiceNotAvailableException
    extends Exception
{
    /**
     * Creates a new ServiceNotAvailableException.
     */
    public ServiceNotAvailableException()
    {
        // EMPTY
    }


    /**
     * Creates a new ServiceNotAvailableException.
     *
     * @param message The message to accompany the exception.
     */
    public ServiceNotAvailableException(final String message)
    {
        super(message);
    }

    /**
     * Creates a new ServiceNotAvailableException.
     *
     * @param cause The root cause of this exception.
     */
    public ServiceNotAvailableException(final Throwable cause)
    {
        super(cause);
    }


    /**
     * Creates a new ServiceNotAvailableException.
     *
     * @param message The message to accompany the exception.
     * @param cause The root cause of this exception.
     */
    public ServiceNotAvailableException(final String message, final Throwable cause)
    {
        super(message, cause);
    }


} // class
