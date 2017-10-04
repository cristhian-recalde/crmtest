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

import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.support.AdjustmentTypeLimitSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Validator for adjustment type limit.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class TransactionAdjustmentTypeLimitValidator implements Validator
{
    /*
     * Key to set if transaction should be validated.
     */
    public static final String VALIDATE_KEY = "TransactionAdjustmentTypeLimitValidator.validate";

    protected TransactionAdjustmentTypeLimitValidator()
    {
        // empty
    }

    public static TransactionAdjustmentTypeLimitValidator instance()
    {
        if (instance == null)
        {
            instance = new TransactionAdjustmentTypeLimitValidator();
        }
        return instance;
    }

    /**
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        /*
         * [Cindy Wong] 2010-02-21: skip validation if it has been done before
         * in this context. Also skip validation if it's not required.
         */
        if (!ctx.getBoolean(VALIDATE_KEY, false))
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
                if (!AdjustmentTypeLimitSupport.isUnderLimit(ctx, transaction))
                {
                    el
                            .thrown(new IllegalPropertyArgumentException(
                                    TransactionXInfo.AMOUNT,
                                    "The amount exceeds the limit allowed for this particular adjustment type"));
                }
            }
            catch (HomeException exception)
            {
                el.thrown(exception);
            }
        }

        // put a flag in context
        ctx.put(VALIDATE_KEY, false);

        el.throwAll();
    }

    /**
     * Singleton instance.
     */
    private static TransactionAdjustmentTypeLimitValidator instance;
}
