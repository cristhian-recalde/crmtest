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
 * Exception thrown if there are problems with the recurring charge process.
 *
 * @author lanny.tse@redknee.com
 */
public class RecurRechargeProcessException extends Exception
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>RecurRechargeProcessException</code>.
     *
     * @param msg
     *            Detail message of the exception.
     */
    public RecurRechargeProcessException(final String msg)
	{
		super(msg);
	}


    /**
     * Create a new instance of <code>RecurRechargeProcessException</code>.
     *
     * @param message
     *            Detail message of the exception.
     * @param cause
     *            Cause of the exception.
     */
    public RecurRechargeProcessException(final String message, final Throwable cause)
    {
        super(message, cause);
}


    /**
     * Create a new instance of <code>RecurRechargeProcessException</code>.
     *
     * @param cause
     *            Cause of the exception.
     */
    public RecurRechargeProcessException(final Throwable cause)
    {
        super(cause);
    }

}
