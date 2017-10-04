/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.transaction;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.support.AdjustmentTypeLimitSupport;
import com.trilogy.app.crm.support.AdjustmentTypePermissionSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author cindy.wong@redknee.com
 * 
 */
public class TransactionAdjustmentTypePermissionValidator implements Validator
{

    protected TransactionAdjustmentTypePermissionValidator()
    {
        // empty
    }

    public static TransactionAdjustmentTypePermissionValidator instance()
    {
        return instance;
    }

    /**
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        // skip validation if it has been done before in this context.
        if (ctx.get(getClass().getName()) != null)
        {
            return;
        }

        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        if (obj == null || !(obj instanceof Transaction))
        {
            el
                    .thrown(new IllegalStateException(
                            "System Error: transaction is must be supplied to this validator"));
        }
        else
        {
            final Transaction transaction = (Transaction) obj;
            try
            {
                AdjustmentType adjType = AdjustmentTypeSupportHelper
                        .get(ctx)
                        .getAdjustmentType(ctx, transaction.getAdjustmentType());
                if (adjType == null)
                {
                    el.thrown(new IllegalPropertyArgumentException(
                            TransactionXInfo.ADJUSTMENT_TYPE,
                            "The adjustment type provided does not exist"));
                }
                else if (adjType.isCategory())
                {
                    el.thrown(new IllegalPropertyArgumentException(
                            TransactionXInfo.ADJUSTMENT_TYPE,
                            "Adjustment type " + adjType.getCode()
                                    + " is a category"));
                }
                else if (!AdjustmentTypePermissionSupport
                        .isTransactionPermitted(ctx, transaction))
                {
                    el
                            .thrown(new IllegalPropertyArgumentException(
                                    TransactionXInfo.ADJUSTMENT_TYPE,
                                    "The logged in user does not have permission to create adjustment in the provided adjustment type"));
                }
            }
            catch (HomeException exception)
            {
                el.thrown(exception);
            }
        }

        // put a flag in context
        ctx.put(getClass().getName(), Boolean.TRUE);

        el.throwAll();
    }

    private static TransactionAdjustmentTypePermissionValidator instance = new TransactionAdjustmentTypePermissionValidator();
}
