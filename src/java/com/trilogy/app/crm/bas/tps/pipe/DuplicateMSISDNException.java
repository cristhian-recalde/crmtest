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

package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * Exception thrown if there are more subscribers with the same MSISDN and some
 * are inactive and have outstanding balance owing.
 * @author arturo.medina@redknee.com
 *
 */
public class DuplicateMSISDNException extends HomeException
{

    /**
     * The serialVersionUID
     */
    private static final long serialVersionUID = 7950155317382269105L;

    /**
     * Constructor that accepts only one String.
     * @param arg0 the message to display.
     */
    public DuplicateMSISDNException(final String arg0)
    {
        super(arg0);
        subscribers_ = "";
    }

    /**
     * Constructor that accepts only one String and the array of duplicate subscribers.
     * @param arg0 the message to display.
     * @param subscribers token of the subscribers.
     */
    public DuplicateMSISDNException(final String arg0, final String subscribers)
    {
        super(arg0);
        subscribers_ = subscribers;
    }

    /**
     * Constructor that accepts another exception on the stack.
     * @param arg0 the inner Exception
     */
    public DuplicateMSISDNException(final Throwable arg0)
    {
        super(arg0);
        subscribers_ = "";
    }

    /**
     * Constructor that receives the message and the inner exception.
     * @param arg0 the message to display.
     * @param arg1 the inner Exception
     */
    public DuplicateMSISDNException(final String arg0, final Throwable arg1)
    {
        super(arg0, arg1);
        subscribers_ = "";
    }

    /**
     * Returns the list of tokenized subscribers.
     * @return a token with subscriber IDs
     */
    public String getSubscribersList()
    {
        return subscribers_;
    }

    /**
     * The token of subcribers in the list
     */
    private final String subscribers_;
}

