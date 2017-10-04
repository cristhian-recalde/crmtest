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
import com.trilogy.framework.xhome.visitor.AbortVisitException;

import com.trilogy.app.crm.bean.core.BundleFee;

/**
 * @author victor.stratan@redknee.com
 */
public class AuxiliaryBundleFeePredicate implements Predicate
{
    public boolean f(final Context ctx, final Object obj) throws AbortVisitException
    {
        if (obj instanceof BundleFee)
        {
            final BundleFee fee = (BundleFee) obj;

            return fee.isAuxiliarySource();
        }
        return false;
    }
}