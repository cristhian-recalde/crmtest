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
package com.trilogy.app.crm.home;


/**
 * Indicates that an attempt has been made to create an object with an
 * identifier in a protected range.
 *
 * @author gary.anderson@redknee.com
 */
public
class ProtectedIdentifierException
    extends Exception
{
    /**
     * Creates a new ProtectedIdentifierException.
     */
    public ProtectedIdentifierException()
    {
        // Empty
    }


    /**
     * Creates a new ProtectedIdentifierException.
     *
     * @param message A message describing the exception.
     */
    public ProtectedIdentifierException(final String message)
    {
        super(message);
    }


    /**
     * Creates a new ProtectedIdentifierException.
     *
     * @param message A message describing the exception.
     * @param cause The initial coause that prompted this exception.
     */
    public ProtectedIdentifierException(final String message, final Throwable cause)
    {
        super(message, cause);
    }


    /**
     * Creates a new ProtectedIdentifierException.
     *
     * @param message A message describing the exception.
     * @param cause The initial coause that prompted this exception.
     */
    public ProtectedIdentifierException(final Throwable cause)
    {
        super(cause);
    }


} // class
