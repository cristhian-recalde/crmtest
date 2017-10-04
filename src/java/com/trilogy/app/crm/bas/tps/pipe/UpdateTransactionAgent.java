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
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.home.UpsFailException;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;

/**
 * Save transaction to transaction home. The transaction home is the delegate from UpsForwaordTransactionHome. I do it
 * in this way, because we need print receipt after transaction is saved. Otherwise, if we print receipt back in
 * UpsForwaordTransactionHome, we need more code to handle when we should print receipt and when not.
 *
 * @author larry.xia@redknee.com
 */
public class UpdateTransactionAgent extends PipelineAgent
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -6146948115237785283L;

    /**
     * Creates a new <code>UpdateTransactionAgent</code>.
     *
     * @param delegate
     *            The delegating agent.
     */
    public UpdateTransactionAgent(final ContextAgent delegate)
    {
        super(delegate);
    }

    /**
     * Saves transaction to transaction home, delegate of UpsForwardTransactionHome.
     *
     * @param ctx
     *            A context.
     * @exception AgentException
     *                Thrown if one of the services fails to initialize.
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final Home home = (Home) ctx.get(TransactionHome.class);

        final Transaction transaction = (Transaction) ctx.get(Transaction.class);

        try
        {
            transaction.setAmount(-transaction.getAmount());
            transaction.setTaxPaid(-transaction.getTaxPaid());

            ctx.put(Transaction.class, home.create(ctx, transaction));

            // ERLogger.genAccountAdjustmentER(ctx, transaction,
            // TPSPipeConstant.RESULT_CODE_SUCCESS,
            // TPSPipeConstant.RESULT_CODE_SUCCESS);

            pass(ctx, this, "Successfully create transaction, adjustment type " + transaction.getAdjustmentType()
                + " account " + transaction.getAcctNum() + " msisdn " + transaction.getMSISDN());

        }
        catch (HomeException e)
        {
            int upsResult = TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY;
            if (e instanceof UpsFailException)
            {
                upsResult = ((UpsFailException) e).getErrorCode();
            }

            if (e instanceof OcgTransactionException)
            {
                upsResult = ((OcgTransactionException) e).getErrorCode();
            }

            ERLogger.genAccountAdjustmentER(ctx, transaction, upsResult, TPSPipeConstant.RESULT_CODE_DATABASE_NA);

            if (CoreTransactionSupportHelper.get(ctx).isDeposit(ctx, transaction))
            {
                new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_FAIL, 1).log(ctx);
            }

            // send out alarm
            new EntryLogMsg(10533, this, "", "", null, e).log(ctx);
            fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_CREATE_TRANSACTION);

        }

    }

}
