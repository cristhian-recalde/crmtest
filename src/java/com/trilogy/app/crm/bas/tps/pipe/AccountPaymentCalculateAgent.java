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

import java.util.Date;
import java.util.Set;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/**
 * Calculates account payment.
 *
 * @author margarita.alp@redknee.com
 */
public class AccountPaymentCalculateAgent extends PipelineAgent
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>AccountPaymentCalculateAgent</code>.
     *
     * @param delegate
     *            Delegate of this agent.
     */
    public AccountPaymentCalculateAgent(final ContextAgent delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final Long totalOwing = (Long) ctx.get(TPSPipeConstant.TPS_PIPE_ACCOUNT_TOTAL_OWING);
        final Long taxOwing = (Long) ctx.get(TPSPipeConstant.TPS_PIPE_ACCOUNT_TAX_OWING);
        final com.redknee.app.crm.bean.core.Transaction trans = (com.redknee.app.crm.bean.core.Transaction) ctx.get(Transaction.class);
        final TPSConfig config = (TPSConfig) ctx.get(TPSConfig.class);

        long totalCharge = 0;
        long totalUsage = 0;
        Account acct = null;
        long balance = 0;

        boolean isPaymentReversal = false;
        try
        {
            isPaymentReversal = CoreTransactionSupportHelper.get(ctx).isPaymentReversal(ctx, trans);
        }
        catch (HomeException e1)
        {
            LogSupport.minor(ctx, this, "Exception occured while resolving adjustment type " +
                    "category for transaction with receipt number"+trans.getReceiptNum());
        }
        
        if (trans.getAmount() != 0 && config.getOCG() && !isPaymentReversal)
        {
            pass(ctx, this, "Payment amount " + trans.getAmount());

        }
        else
        {

            try
            {
                acct = AccountSupport.getAccount(ctx, trans.getBAN());
            }
            catch (final HomeException e1)
            {
                ERLogger.genAccountAdjustmentER(ctx, trans, TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                    TPSPipeConstant.FAIL_TO_CAL_TOTAL_CHARGES);

                // send out alarm
                new EntryLogMsg(10534, this, "", "", new String[]
                {
                    "When calculating totalCharges/totalUsage, Account search fails",
                }, e1).log(ctx);
                fail(ctx, this, e1.getMessage(), e1, TPSPipeConstant.FAIL_TO_CAL_TOTAL_CHARGES);
                return;
            }

            if (acct != null && acct.isPooled(ctx))
            {
                try
                {
                    totalCharge = getCharge(ctx);
                    ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TOTAL_CHARGE, Long.valueOf(totalCharge));

                }
                catch (final Exception e)
                {
                    ERLogger.genAccountAdjustmentER(ctx, trans, TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                        TPSPipeConstant.FAIL_TO_CAL_TOTAL_CHARGES);

                    /*
                     * TODO [Cindy] 2008-01-31: What is entry ID 10534?
                     */

                    // send out alarm
                    new EntryLogMsg(10534, this, "", "", new String[]
                    {
                        "When calculating totalCharges/totalUsage, Adjustment History table searching fails",
                    }, e).log(ctx);
                    fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_CAL_TOTAL_CHARGES);
                    return;
                }

                try
                {
                    totalUsage = getUsage(ctx);
                    ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_TOTAL_USAGE, Long.valueOf(totalUsage));

                    balance = (totalOwing.longValue() - taxOwing.longValue()
                        - (trans.getAmount() - trans.getTaxPaid()) + totalCharge + totalUsage);

                }
                catch (final Exception e)
                {
                    ERLogger.genAccountAdjustmentER(ctx, trans, TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                        TPSPipeConstant.FAIL_TO_CAL_TOTAL_USAGE);

                    // send out alarm
                    new EntryLogMsg(10534, this, "", "", new String[]
                    {
                        "call detail table searching fails",
                    }, e).log(ctx);
                    fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_CAL_TOTAL_USAGE);
                    return;
                }
            }

            ctx.put(TPSPipeConstant.TPS_PIPE_ACCOUNT_BALANCE, Long.valueOf(balance));

            pass(ctx, this, "Calculate total charges: " + totalCharge + ", totalUsage: " + totalUsage + ", balance: "
                + balance);

        }
    }


    /**
     * Get total charge.
     *
     * @param ctx
     *            A context
     * @return Total charge
     * @exception Exception
     *                thrown if one of the services fails to initialize
     */
    public long getCharge(final Context ctx) throws Exception
    {
        Date invoiceDate = (Date) ctx.get(TPSPipeConstant.INVOICE_INVOICE_DATE);

        if (invoiceDate == null)
        {
            invoiceDate = (Date) ctx.get(TPSPipeConstant.ACCOUNT_INVOICE_DATE);
        }

        final Transaction trans = (Transaction) ctx.get(Transaction.class);

        And filter = new And();
        filter.add(new EQ(TransactionXInfo.BAN, trans.getAcctNum()));
        
        Set adjTypeSet = AdjustmentTypeSupportHelper.get(ctx).getPaymentsCodes(ctx);
        adjTypeSet.addAll(AdjustmentTypeSupportHelper.get(ctx).getSelfAndDescendantCodes(ctx,
                AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, 
                        AdjustmentTypeEnum.DepositPayments)));
        filter.add(new Not(new In(TransactionXInfo.ADJUSTMENT_TYPE, adjTypeSet)));
        if (invoiceDate != null)
        {
            filter.add(new GTE(TransactionXInfo.RECEIVE_DATE, invoiceDate));
        }

        Number totalAmount = HomeSupportHelper.get(ctx).sum(ctx, TransactionXInfo.AMOUNT, filter);
        Number totalTaxPaid = HomeSupportHelper.get(ctx).sum(ctx, TransactionXInfo.TAX_PAID, filter);
        
        return totalAmount.longValue() - totalTaxPaid.longValue();
    }


    /**
     * Get recent total usage.
     *
     * @param ctx
     *            A context
     * @return Recent total usage
     */

    public long getUsage(final Context ctx)
    {
        Date invoiceDate = (Date) ctx.get(TPSPipeConstant.INVOICE_INVOICE_DATE);

        if (invoiceDate == null)
        {
            invoiceDate = (Date) ctx.get(TPSPipeConstant.ACCOUNT_INVOICE_DATE);
        }
        final Transaction trans = (Transaction) ctx.get(Transaction.class);

        XStatement filter = new EQ(CallDetailXInfo.BAN, trans.getAcctNum());

        if (invoiceDate != null)
        {
            And and = new And();
            and.add(filter);
            and.add(new GTE(CallDetailXInfo.POSTED_DATE, invoiceDate));
            filter = and;
        }
        
        final Date date;
        if (invoiceDate != null)
        {
            date = invoiceDate;
        }
        else
        {
            date = new Date(0);
        }
        
        try
        {
            Number chargeSum = HomeSupportHelper.get(ctx).sum(ctx, CallDetailXInfo.CHARGE, filter);
            return chargeSum.longValue();
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error retrieving call detail charge sum from database for query [" + filter + "]", e).log(ctx);
        }

        return 0;
    }

    /**
     * SQL result column name for account level transactions.
     */
    private static final String TRANSACTION_SUM = "ACCT_TRANS";

}
