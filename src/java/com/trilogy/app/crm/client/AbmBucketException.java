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
package com.trilogy.app.crm.client;


/**
 * Indicates that an exception occurred while managing the FreeCallTime bucket
 * templates in ABM.
 *
 * @author gary.anderson@redknee.com
 */
public
class AbmBucketException
    extends Exception
{
    /**
     * Used to indicate that no result code was set in this exception.
     */
    public static final short NO_RESULT = -1;


    /**
     * Creates a new AbmBucketException.
     *
     * @param message The message to display.
     */
    public AbmBucketException(final String message)
    {
        super(message);
        resultCode_ = NO_RESULT;
    }


    /**
     * Creates a new AbmBucketException.
     *
     * @param message The message to display.
     * @param resultCode The result code returned by ABM that prompted this
     * exception.
     */
    public AbmBucketException(final String message, final short resultCode)
    {
        super(message + " Result code: " + AbmResultCode.toString(resultCode) + ".");
        resultCode_ = resultCode;
    }


    /**
     * Creates a new AbmBucketException.
     *
     * @param message The message to display.
     * @param throwable The throwable that was the initiating cause of this
     * exception.
     */
    public AbmBucketException(final String message, final Throwable throwable)
    {
        super(message, throwable);
        resultCode_ = NO_RESULT;
    }


    /**
     * Gets the result code of the ABM operation if one is available; NO_RESULT
     * otherwise.
     *
     * @return The result code.
     */
    public short getResultCode()
    {
        return resultCode_;
    }


    /**
     * The result code of the ABM operation if one is available; NO_RESULT otherwise.
     */
    private final short resultCode_;

} // class
