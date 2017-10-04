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
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;

import com.trilogy.app.crm.CommonFramework;

/**
 * A Predicate that returns true when context for the displayed bean is in EDIT mode.
 *
 * @author victor.stratan@redknee.com
 */
public final class IsCreateModePredicate implements Predicate
{
    /**
     *
     * {@inheritDoc}
     */
    public boolean f(final Context ctx, final Object object)
    {
        return OutputWebControl.CREATE_MODE == ctx.getInt(CommonFramework.MODE, OutputWebControl.DISPLAY_MODE);
    }
}