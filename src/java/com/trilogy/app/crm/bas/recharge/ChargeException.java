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
package com.trilogy.app.crm.bas.recharge;

/**
 * Exception trhown when a service transaction charge occurs.
 * @author arturo.medina@redknee.com
 *
 */
public class ChargeException extends Exception
{

    /**
     * the serial version uid
     */
    private static final long serialVersionUID = 5044769989627532793L;

    /**
     * default constructor
     *
     */
    public ChargeException()
    {
        super();
    }

    /**
     * accepts a message andthe cause of the wrapped exception
     * @param message the message to be wrapped
     * @param cause the previous cause.
     */
    public ChargeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Accepts the a message to show to the final user.
     * @param message the error messae to show.
     */
    public ChargeException(String message)
    {
        super(message);
    }

    /**
     * Accepts the main cause for this exception to be constructed.
     * @param cause the previous error to be wrapped.
     */
    public ChargeException(Throwable cause)
    {
        super(cause);
    }
}
