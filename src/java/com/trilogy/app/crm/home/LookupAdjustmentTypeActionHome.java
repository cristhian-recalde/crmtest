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

package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;


/**
 * Sets the transaction action based on adjustment type action.
 *
 * @author paul.sperneac@redknee.com
 * @deprecated Not referenced in any code.
 */
@Deprecated
public class LookupAdjustmentTypeActionHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>LookupAdjustmentTypeActionHome</code>.
     *
     * @param delegate
     *            Delegate of this decorator.
     */
    public LookupAdjustmentTypeActionHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        final Transaction bean = (Transaction) obj;

        final AdjustmentType adjType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, bean.getAdjustmentType());

        if (adjType != null)
        {
            final AdjustmentTypeActionEnum action = adjType.getAction();

            if (action == AdjustmentTypeActionEnum.CREDIT || action == AdjustmentTypeActionEnum.DEBIT
                || action == AdjustmentTypeActionEnum.EITHER)
            {
                bean.setAction(action);
            }
        }

        return super.create(ctx, obj);
    }

}
