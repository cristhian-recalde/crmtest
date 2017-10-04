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
 * A Predicate that returns true when context for the displayed bean is in TABLE mode.
 * I.E. the toWeb() was called for a TableWebControl.
 *
 * @author victor.stratan@redknee.com
 */
public final class IsTableModePredicate implements Predicate
{
    /**
     * For serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * {@inheritDoc}
     */
    public boolean f(final Context ctx, final Object object)
    {
        return ctx.getBoolean("TABLE_MODE", false);
    }
}