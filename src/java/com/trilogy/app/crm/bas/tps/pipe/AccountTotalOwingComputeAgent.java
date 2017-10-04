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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.EntryLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/**
 * Computes the total amount owing for the account.
 *
 * @author magarita.alp@redknee.com
 */
public class AccountTotalOwingComputeAgent extends PipelineAgent
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>AccountTotalOwingComputeAgent</code>.
     *
     * @param delegate
     *            Delegate of this agent.
     */
    public AccountTotalOwingComputeAgent(final ContextAgent delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final Transaction trans = (Transaction) ctx.get(Transaction.class);
        long totalOwing = 0;
        long taxOwing = 0;
        String msg = null;

        try
        {
            // Invoice invoice = ( Invoice) ctx.get(Invoice.class);
            final Date invoiceDate = (Date) ctx.get(TPSPipeConstant.ACCOUNT_INVOICE_DATE);
            Home home = (Home) ctx.get(TransactionHome.class);

            /*
             * MAALP : RFF Group Pooled Account. For the group accounts calculate owing
             * for the account rather then for an individual subscriber
             */
            final Account account = AccountSupport.getAccount(ctx, trans.getBAN());

            if (account != null && account.isPooled(ctx))
            {
                final And and = new And();

                and.add(new EQ(TransactionXInfo.BAN, trans.getAcctNum()));
                and.add(new In(TransactionXInfo.ADJUSTMENT_TYPE,
                    AdjustmentTypeSupportHelper.get(ctx).getPaymentsButDepositCodes(ctx)));

                if (invoiceDate != null)
                {
                    totalOwing = ((Long) ctx.get(TPSPipeConstant.TPS_PIPE_ACCOUNT_TOTAL_OWING)).longValue();
                    taxOwing = ((Long) ctx.get(TPSPipeConstant.TPS_PIPE_ACCOUNT_TAX_OWING)).longValue();

                    // get all credits (not including the deposits) since the invoice date
                    and.add(new GTE(TransactionXInfo.RECEIVE_DATE, invoiceDate));
                }

                home = home.where(ctx, and);

                final Collection transSet = home.selectAll(ctx);

                if (transSet == null || transSet.size() < 1)
                {
                    msg = "Transaction not found. Total owing:" + totalOwing + " , taxOwing:" + taxOwing;

                }
                else
                {
                    // the payment is always negative
                    totalOwing += totalPaid(transSet);
                    taxOwing += taxPaid(transSet);
                    msg = "Transaction found. Total owing:" + totalOwing + " , taxOwing:" + taxOwing;
                }
                ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TOTAL_OWING, Long.valueOf(totalOwing));
                ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TAX_OWING, Long.valueOf(taxOwing));
                pass(ctx, this, msg);

            }
            else
            {
                pass(ctx, this, "The account " + account.getBAN() + " is not pooled skipping this agent");
            }

        }
        catch (final Exception e)
        {

            ERLogger.genAccountAdjustmentER(ctx, trans, TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                TPSPipeConstant.FAIL_TO_QUERY_TRANSACTION_TABLE);

            // send out alarm
            new EntryLogMsg(10534, this, "", "", new String[]
            {
                "Adjustment table searching fails",
            }, e).log(ctx);
            fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_QUERY_TRANSACTION_TABLE);
        }

    }


    /**
     * calculate the total payment.
     *
     * @param transactions
     *            A collection of payment transaction
     * @return Total payment.
     */

    public long totalPaid(final Collection transactions)
    {
        /* TODO [Cindy] 2008-01-31: Sum in Oracle? */
        long ret = 0;
        final Iterator it = transactions.iterator();
        while (it.hasNext())
        {
            final Transaction trans = (Transaction) it.next();
            ret += trans.getAmount();
        }

        return ret;
    }


    /**
     * calculate the total tax paid.
     *
     * @param transactions
     *            A collection of payment transaction
     * @return Total tax paid.
     */

    public long taxPaid(final Collection transactions)
    {
        /* TODO [Cindy] 2008-01-31: Sum in Oracle? */
        long ret = 0;
        final Iterator it = transactions.iterator();
        while (it.hasNext())
        {
            final Transaction trans = (Transaction) it.next();
            ret += trans.getTaxPaid();
        }

        return ret;
    }

}
