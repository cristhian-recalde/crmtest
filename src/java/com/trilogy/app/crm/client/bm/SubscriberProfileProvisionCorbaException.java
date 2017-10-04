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
 * Indicates that an exception occurred at the CORBA level for the
 * SubscriberProfileProvision interface.
 *
 * @author gary.anderson@redknee.com
 */
public class SubscriberProfileProvisionCorbaException
    extends SubscriberProfileProvisionException
{
    /**
     * Creates a new SubscriberProfileProvisionCorbaException.
     */
    public SubscriberProfileProvisionCorbaException()
    {
        // EMPTY
    }


    /**
     * Creates a new SubscriberProfileProvisionCorbaException.
     *
     * @param message The exception message.
     * @param cause The linked cause.
     */
    public SubscriberProfileProvisionCorbaException(final String message, final Throwable cause)
    {
        super(UNKNOWN_ERROR_CODE, message, cause);
    }


    /**
     * Creates a new SubscriberProfileProvisionCorbaException.
     *
     * @param message The exception message.
     */
    public SubscriberProfileProvisionCorbaException(final String message)
    {
        super(UNKNOWN_ERROR_CODE, message);
    }


    /**
     * Creates a new SubscriberProfileProvisionCorbaException.
     *
     * @param cause The linked cause.
     */
    public SubscriberProfileProvisionCorbaException(final Throwable cause)
    {
        super(UNKNOWN_ERROR_CODE, cause);
    }


    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 1L;
}
