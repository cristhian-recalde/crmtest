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

import com.trilogy.app.crm.state.FinalStateAware;
import com.trilogy.framework.xhome.beans.XDeepCloneable;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;


/**
 * This predicate returns true if the given state or state-aware
 * object is in a 'final' state.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class FinalStatePredicate extends SimpleDeepClone implements Predicate,XDeepCloneable
{

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        boolean result = false;

        if (obj instanceof FinalStateAware)
        {
            result = ((FinalStateAware)obj).isInFinalState();
        }

        return result;
    }



}
