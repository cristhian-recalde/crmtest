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
 * Exception to be thrown when the VPN group leader is invalid.
 *
 * @author larry.xia@redknee.com
 */
public class InvalidVPNGroupLeaderException extends Exception
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>InvalidVPNGroupLeaderException</code>.
     *
     * @param msg
     *            Detailed message.
     */
    public InvalidVPNGroupLeaderException(final String msg)
    {
        super(msg);
    }


    /**
     * Create a new instance of <code>InvalidVPNGroupLeaderException</code>.
     *
     * @param message
     *            Detailed message.
     * @param cause
     *            Cause of this exception.
     */
    public InvalidVPNGroupLeaderException(final String message, final Throwable cause)
    {
        super(message, cause);
    }


    /**
     * Create a new instance of <code>InvalidVPNGroupLeaderException</code>.
     *
     * @param cause
     *            Cause of this exception.
     */
    public InvalidVPNGroupLeaderException(final Throwable cause)
    {
        super(cause);
    }
}
