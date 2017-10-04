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
package com.trilogy.app.crm.client.bm;

/**
 * Indicates that an exception occurred at some level for the
 * SubscriberProfileProvision interface.
 *
 * @author gary.anderson@redknee.com
 */
public class SubscriberProfileProvisionException
    extends Exception
{
    /**
     * Used to indicate that an explicit error code was not provided.
     */
    public static final short UNKNOWN_ERROR_CODE = (short)-1;


    /**
     * Creates a new SubscriberProfileProvisionException.
     */
    public SubscriberProfileProvisionException()
    {
        this(UNKNOWN_ERROR_CODE);
    }


    /**
     * Creates a new SubscriberProfileProvisionException.
     *
     * @param errorCode The error code returned by BM, if available.
     */
    public SubscriberProfileProvisionException(final Short errorCode)
    {
        this(errorCode, "");
    }


    /**
     * Creates a new SubscriberProfileProvisionException.
     *
     * @param errorCode The error code returned by BM, if available.
     * @param message The exception message.
     */
    public SubscriberProfileProvisionException(final Short errorCode, final String message)
    {
        this(errorCode, message, null);
    }


    /**
     * Creates a new SubscriberProfileProvisionException.
     *
     * @param errorCode The error code returned by BM, if available.
     * @param cause The linked cause.
     */
    public SubscriberProfileProvisionException(final Short errorCode, final Throwable cause)
    {
        this(errorCode, "", cause);
    }


    /**
     * Creates a new SubscriberProfileProvisionException.
     *
     * @param errorCode The error code returned by BM, if available.
     * @param message The exception message.
     * @param cause The linked cause.
     */
    public SubscriberProfileProvisionException(final Short errorCode, final String message, final Throwable cause)
    {
        super(" [" + errorCode + ": "+ URCSReturnCodeMsgMapping.getMessage(errorCode) + "] " + message, cause);
        errorCode_ = errorCode;
    }


    /**
     * Gets the error code returned by BM, if available.
     *
     * @return The error code returned by BM, if available; -1 otherwise.
     */
    public short getErrorCode()
    {
        return errorCode_;
    }


    /**
     * The error code returned by BM, if available.
     */
    private final short errorCode_;

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
}
