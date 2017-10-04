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
package com.trilogy.app.crm.util;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;


/**
 * Provides support for generating operational measurements (OM log messages)
 * for attempts, sucesses, and failures.  For example, for a given OM identifier
 * of "Do_Function", an instance of this class can generate each of
 * "Do_Function_Attempt", "Do_Function_Success", and "Do_Function_Failure".
 *
 * @author gary.anderson@redknee.com
 */
public final
class OMHelper
{
    /**
     * Creates a new OMHelper for the given OM identifier.  Leading and trailing
     * space is removed from the identifier.
     *
     * @param identifier The unique identifier of the OM.
     *
     * @exception IllegalArgumentException Thrown if the given identifier is
     * null and if it is blank.
     */
    public OMHelper(final String identifier)
    {
        if (identifier == null)
        {
            throw new IllegalArgumentException("The identifier is null.");
        }

        final String trimmedIdentifier = identifier.trim();

        if (trimmedIdentifier.length() == 0)
        {
            throw new IllegalArgumentException("The identifier is blank.");
        }

        attempt_ = trimmedIdentifier + "_Attempt";
        success_ = trimmedIdentifier + "_Success";
        failure_ = trimmedIdentifier + "_Failure";
    }


    /**
     * Generates an "attempt" OM.
     *
     * @param context The operating context.
     */
    public void attempt(final Context context)
    {
        new OMLogMsg(Common.OM_MODULE, attempt_).log(context);
    }


    /**
     * Generates an "success" OM.
     *
     * @param context The operating context.
     */
    public void success(final Context context)
    {
        new OMLogMsg(Common.OM_MODULE, success_).log(context);
    }


    /**
     * Generates an "failure" OM.
     *
     * @param context The operating context.
     */
    public void failure(final Context context)
    {
        new OMLogMsg(Common.OM_MODULE, failure_).log(context);
    }


    /**
     * The attempt OM name.
     */
    private final String attempt_;

    /**
     * The success OM name.
     */
    private final String success_;

    /**
     * The failure OM name.
     */
    private final String failure_;

} // class
