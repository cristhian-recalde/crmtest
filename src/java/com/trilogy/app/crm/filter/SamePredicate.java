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
 * A Predicate that returns true when the given object is the same as the
 * configured object.  That is, if (match == object).
 *
 * @author gary.anderson@redknee.com
 */
public final
class SamePredicate
    implements Predicate
{
    /**
     * Creates a new SamePredicate for the given object.
     *
     * @param match The object to which all others are compared.
     */
    public SamePredicate(final Object match)
    {
        if (match == null)
        {
            throw new IllegalArgumentException("The match parameter is null.");
        }

        match_ = match;
    }


    // INHERIT
    public boolean f(Context _ctx,final Object object)
    {
        return match_ == object;
    }

    
    /**
     * The object to which all others are compared.
     */
    private final Object match_;

}// class
