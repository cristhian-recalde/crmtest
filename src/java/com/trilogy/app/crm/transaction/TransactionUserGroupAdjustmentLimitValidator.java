/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.transaction;

import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.support.UserGroupSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * Validator for adjustment user group limit.
 * 
 * @author simar.singh@redknee.com
 * @since 8.5
 */
public class TransactionUserGroupAdjustmentLimitValidator implements Validator
{


    protected TransactionUserGroupAdjustmentLimitValidator()
    {
        // empty
    }


    public static TransactionUserGroupAdjustmentLimitValidator instance()
    {
        if (instance == null)
        {
            instance = new TransactionUserGroupAdjustmentLimitValidator();
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

        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        if (obj == null || !(obj instanceof Transaction))
        {
            el.thrown(new IllegalStateException("System Error: transaction is must be supplied to this validator"));
        }
        else
        {
            final Transaction transaction = (Transaction) obj;
            try
            {
                if (Math.abs(UserGroupSupport.getAdjustmentLimit(ctx)) < Math.abs(transaction.getAmount()))
                {
                    el.thrown(new IllegalPropertyArgumentException(TransactionXInfo.AMOUNT,
                            "The amount exceeds the Limit allowed for this User Group"));
                }
            }
            catch (HomeException exception)
            {
                el.thrown(exception);
            }
        }
        el.throwAll();
    }

    /**
     * Singleton instance.
     */
    private static TransactionUserGroupAdjustmentLimitValidator instance;
}
