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
package com.trilogy.app.crm.client;

import java.beans.PropertyChangeEvent;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.transfer.TransferAmountTypeEnum;


/**
 * Provides a Test Client for replacing the AppTFAClient.
 *
 * @author gary.anderson@redknee.com
 */
public class AppTFATestClient
    extends AppTFAClient
{

    /**
     * Creates a new Test Client for replacing the AppTFAClient.
     *
     * @param context The operating context.
     * @throws AgentException Thrown if there are problems initializing the
     * client.
     */
    public AppTFATestClient(final Context context)
        throws AgentException
    {
        super(context);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public TransferFundsResponse transferFunds(final Context context, final short action, final long amount,
        final short amountType, final String contributorChargingId, final String currency,
        final boolean expiryExtension, final long transferTypeId, final String recipientChargingId)
    {
        final StringBuilder builder = new StringBuilder("[");
        builder.append("action: ").append(action).append(", ");
        builder.append("amount: ").append(amount).append(", ");
        builder.append("amountType: ").append(amountType).append(", ");
        builder.append("contributorChargingId: ").append(contributorChargingId).append(", ");
        builder.append("currency: ").append(currency).append(", ");
        builder.append("expiryExtension: ").append(expiryExtension).append(", ");
        builder.append("transferTypeId: ").append(transferTypeId).append(", ");
        builder.append("recipientChargingId: ").append(recipientChargingId).append("]");

        new DebugLogMsg(this, builder.toString(), null).log(context);

        final TransferFundsResponse results = new TransferFundsResponse();

        if (action == 2 && amount == 12300L)
        {
            results.resultCode_ = 5;
        }
        else if (action == 1 && amount == 45600)
        {
            results.resultCode_ = 6;
        }
        else
        {
            results.resultCode_ = 0;
        }

        switch (amountType)
        {
            case TransferAmountTypeEnum.TRANSFER_REQUEST_AMOUNT_INDEX:
            {
                results.debitAmount_ = amount - 1;
                results.adjustmentAmount_ = amount;
                results.creditAmount_ = amount + 1;
                break;
            }
            case TransferAmountTypeEnum.CREDIT_AMOUNT_INDEX:
            {
                results.debitAmount_ = amount - 2;
                results.adjustmentAmount_ = amount - 1;
                results.creditAmount_ = amount;
                break;
            }
            case TransferAmountTypeEnum.DEBIT_AMOUNT_INDEX:
            {
                results.debitAmount_ = amount;
                results.adjustmentAmount_ = amount + 1;
                results.creditAmount_ = amount + 2;
                break;
            }
            default:
            {
                results.debitAmount_ = -1;
                results.adjustmentAmount_ = -1;
                results.creditAmount_ = -1;
            }
        }

        results.transactionId_ = "Charged by: " + getAgentId(context) + " action = " + action;

        return results;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt)
    {
        // Empty -- test client doesn't need to care.
    }
}
