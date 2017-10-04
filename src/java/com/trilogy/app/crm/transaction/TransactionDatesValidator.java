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

package com.trilogy.app.crm.transaction;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.home.DateValidator;

/**
 * Validates the dates of a Transaction.
 *
 * @author cindy.wong@redknee.com
 */
public class TransactionDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>TransactionDatesValidator</code>.
     */
    protected TransactionDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>TransactionDatesValidator</code>.
     *
     * @return An instance of <code>TransactionDatesValidator</code>.
     */
    public static TransactionDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new TransactionDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final Transaction transaction = (Transaction) object;

        // transactions cannot be old, so there is no need to grab the old bean

        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig not found in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        try
        {
			validatePrior(context, null, transaction,
			    TransactionXInfo.TRANS_DATE, config);
			validateAfter(context, null, transaction,
			    TransactionXInfo.TRANS_DATE, config);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Singleton instance.
     */
    private static TransactionDatesValidator instance;
}
