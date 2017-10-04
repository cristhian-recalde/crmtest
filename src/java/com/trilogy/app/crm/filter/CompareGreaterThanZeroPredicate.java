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
package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;


/**
 * A Predicate that returns true when the given configured object is greater
 * than the given object.  That is, if (match.compareTo(object) > 0).
 *
 * @author gary.anderson@redknee.com
 */
public final
class CompareGreaterThanZeroPredicate
    implements Predicate
{
    /**
     * Creates a new CompareGreaterThanZeroPredicate for the given object.
     *
     * @param match The object to which all others are compared.
     */
    public CompareGreaterThanZeroPredicate(final Comparable match)
    {
        if (match == null)
        {
            throw new IllegalArgumentException("The match parameter is null.");
        }

        match_ = match;
    }


    // INHERIT
    public boolean f(Context ctx,final Object object)
    {
        return match_.compareTo(object) > 0;
    }

    
    /**
     * The object to which all others are compared.
     */
    private final Comparable match_;

}// class
