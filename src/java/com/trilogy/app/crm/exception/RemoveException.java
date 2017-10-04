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

package com.trilogy.app.crm.exception;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * This is an exception thrown when a bean, logically speaking, should not be removed from the home. For example, the
 * bean is still in use by other beans.
 *
 * @author cindy.wong@redknee.com
 */
public class RemoveException extends HomeException
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 6760563231452107059L;

    /**
     * Create a new <code>RemoveException</code>.
     *
     * @param errorMessage
     *            Error message of the exception.
     */
    public RemoveException(final String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * Create a new <code>RemoveException</code>.
     *
     * @param cause
     *            Cause of the exception.
     */
    public RemoveException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Create a new <code>RemoveException</code>.
     *
     * @param errorMessage
     *            Error message of the exception.
     * @param cause
     *            Cause of the exception.
     */
    public RemoveException(final String errorMessage, final Throwable cause)
    {
        super(errorMessage, cause);
    }

}
