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
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

import com.trilogy.app.crm.bean.Account;

/**
 * A Predicate that returns true when Parent is set for the edited Account.
 *
 * @author victor.stratan@redknee.com
 */
public final class IsParrentSetPredicate implements Predicate
{
    /**
     *
     * {@inheritDoc}
     */
    public boolean f(final Context ctx, final Object object)
    {
        final Account bean = (Account) ctx.get(AbstractWebControl.BEAN, null);
        if (bean != null)
        {
            final String parent = bean.getParentBAN();
            if (parent != null && parent.length() > 0)
            {
                return true;
            }
        }

        return false;
    }
}