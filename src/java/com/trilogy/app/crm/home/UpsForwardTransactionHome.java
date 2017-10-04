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
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.exception.codemapping.S2100ReturnCodeMsgMapping;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/**
 * If the transaction type is standard payment or deposit payment, it will update
 * subscriber on UPS through provisioning interface.
 *
 * @author larry.xia@redknee.com
 */
public class UpsForwardTransactionHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -5094819050716814362L;


    /**
     * Creates a new UpsForwardTransactionHome.
     *
     * @param delegate
     *            The delegate of this decorator.
     */
    public UpsForwardTransactionHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * Updates the subscriber on UPS through provisioning interface if the transaction is
     * standard payment or deposit payment.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            The transaction being created.
     * @throws HomeException
     *             Thrown by home.
     * @return Transaction created.
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Transaction transaction = (Transaction) obj;
        // add transaction to datastore
        ContextAgent start = null;
        Subscriber subs = (Subscriber) ctx.get(Subscriber.class);
        Object result = null;

        if (subs == null)
        {                            
            subs = SubscriberSupport.lookupSubscriberForMSISDN(ctx,transaction.getMSISDN(),transaction.getTransDate());

        }
        else if (!subs.getMSISDN().equals(transaction.getMSISDN()))
        {
            subs = SubscriberSupport.lookupSubscriberForMSISDN(ctx,transaction.getMSISDN(),transaction.getTransDate());
        }

        /*if (CoreTransactionSupportHelper.get(ctx).isDeposit(ctx, transaction))
        {
            if (CoreTransactionSupportHelper.get(ctx).isDepositRelease(ctx, transaction))
            {
                TransactionSupport.createInterestPayment(ctx, subs, transaction, subs.getDeposit(getContext()));
            }
            start = (ContextAgent) ctx.get(TPSPipeConstant.PIPELINE_SUBSCRIBER_DEPOSIT_KEY);
            result = update(ctx, start, transaction);
        }
        else*/
        if (CoreTransactionSupportHelper.get(ctx).isStandardPayment(ctx, transaction))
        {
            start = (ContextAgent) ctx.get(TPSPipeConstant.PIPELINE_SUBSCRIBER_BILL_KEY);
            result = update(ctx, start, transaction);
        }
        else
        {
            // [CW] UPS does not care about any other types of transaction
            result = super.create(ctx, obj);
        }
        return result;
    }


    /**
     * Apply account adjustment.
     *
     * @param ctx
     *            The operating context.
     * @param start
     *            The agent to execute the adjustment.
     * @param transaction
     *            The transaction being created.
     * @return The actual transaction created.
     * @throws HomeException
     *             Thrown by home.
     */
    public Object update(final Context ctx, final ContextAgent start, final Transaction transaction)
        throws HomeException
    {
        Object obj = null;

        final Home home = (Home) ctx.get(AccountHome.class);
        final Account acct = (Account) home.find(ctx, transaction.getAcctNum());
        try
        {

            if (acct == null)
            {
                throw new HomeException("Fail to find the account " + transaction.getAcctNum());
            }

            // get the account from context
            if (!ctx.has(Account.class))
            {
                ctx.put(Account.class, acct);
            }

            transaction.setAmount(transaction.getAmount() * -1);
            ctx.put(Transaction.class, transaction);

            if (!ctx.has(AdjustmentType.class))
            {
                ctx.put(AdjustmentType.class, AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx, transaction
                    .getAdjustmentType()));
            }
            Context subcontext = ctx.createSubContext();
            subcontext.put(TransactionHome.class, this.getDelegate()); // It will start from in between transaction pipeline.
            /*
             * Original Transaction Home put for SubscriberDepositSettingAgent to calling from head of transaction pipeline for deposit release call
             */
            subcontext.put("ORG_TRANSACTION_HOME", ctx.get(TransactionHome.class)); 
            
            subcontext.put(com.redknee.app.crm.bean.Transaction.class, transaction);
            // Ensure that a calculation session is available for payment processing
            String sessionKey = CalculationServiceSupport.createNewSession(subcontext);
            try
            {
                start.execute(subcontext);
            }
            finally
            {
                CalculationServiceSupport.endSession(subcontext, sessionKey);
            }

            if (subcontext.has(Exception.class))
            {
                throw (Exception) subcontext.get(Exception.class);
            }
            else if (subcontext.has(Transaction.class))
            {
                obj = subcontext.get(Transaction.class);
            }

            if (subcontext.has(UpsFailException.class))
            {
                throw (HomeException) subcontext.get(UpsFailException.class);
            }

            if (subcontext.getInt(TPSPipeConstant.TPS_PIPE_RESULT_CODE) != TPSPipeConstant.RESULT_CODE_SUCCESS)
            {
                String errorMsg = getErrorMessage(subcontext);
                throw new HomeException(errorMsg);
            }

            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_SUCCESS).log(ctx);
        }
        catch (final HomeException e)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_FAIL).log(ctx);
            throw e;
        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "Error when applying account adjustment ", e).log(ctx);

            HomeException he = null;
            if (e.getMessage() == null)
            {
                he = new HomeException("Error applying account adjustment. Please see logs for details!");
            }
            else
            {
                he = new HomeException("Error applying account adjustment: " + e.getMessage());
            }
            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_FAIL).log(ctx);
            throw he;
        }
        return obj;
    }
    
    private String getErrorMessage(Context ctx)
    {
        StringBuilder result = new StringBuilder("Unable to process the payment");
        int errorCode = ctx.getInt(TPSPipeConstant.TPS_PIPE_RESULT_CODE);
        switch (errorCode)
        {
        case TPSPipeConstant.RESULT_CODE_UPS_FAILS:
                int upsErrorCode = ctx.getInt(TPSPipeConstant.TPS_UPS_RESULT_CODE);
                result.append(" due to an error on UPS: ");
                result.append(S2100ReturnCodeMsgMapping.getMessage(upsErrorCode));
            break;
        case TPSPipeConstant.FAIL_TO_FIND_MSISDN_PREFIX:
            result.append(": Unable to find MSISDN prefix");
            break;
        case TPSPipeConstant.INVALID_MSISDN_LENGTH:
            result.append(": MSISDN has invalid length.");
            break;
        case TPSPipeConstant.FAIL_TO_FIND_ADJUST_TYPE:
            result.append(": Unable to find adjustment type.");
            break;
        case TPSPipeConstant.FAIL_TO_FILL_IN_TRASACTION_RECORD:
            result.append(": Unable to fill in transaction record.");
            break;
        case TPSPipeConstant.FAIL_TO_FIND_SUB:
            result.append(": Unable to find subscription.");
            break;
        case TPSPipeConstant.FAIL_TO_FIND_ACCOUNT:
            result.append(": Unable to find account.");
            break;
        case TPSPipeConstant.FAIL_TO_FIND_TAX_RATE:
            result.append(": Unable to find tax rate.");
            break;
        case TPSPipeConstant.FAIL_TO_FIND_SERVICE_PROVIDER:
            result.append(": Unable to find service provider.");
            break;
        case TPSPipeConstant.FAIL_TO_CREATE_TRANSACTION:
            result.append(": Unable to create transaction.");
            break;
        case TPSPipeConstant.FAIL_TO_QUERY_TRANSACTION_TABLE:
            result.append(": Unable to query the transaction table.");
            break;
        case TPSPipeConstant.FAIL_TO_CAL_TOTAL_CHARGES:
            result.append(": Unable to calculate total charges.");
            break;
        case TPSPipeConstant.FAIL_TO_CAL_TOTAL_USAGE:
            result.append(": Unable to calculate total usage.");
            break;
        case TPSPipeConstant.FAIL_TO_RESET_SUB:
            result.append(": Unable to reset subscription.");
            break;
        case TPSPipeConstant.PREPAID_SUB_REJECTED:
            result.append(": Prepaid subscriber rejected.");
            break;
        case TPSPipeConstant.RESULT_CODE_DATABASE_NA:
            result.append(": Database not available.");
            break;
        case TPSPipeConstant.RESULT_CODE_MOBILE_NA:
            result.append(": Mobile not available.");
            break;
        case TPSPipeConstant.RESULT_CODE_GENERAL:
            result.append(": General error.");
        break;
        default:
            result.append(": Unknown error (");
            result.append(errorCode);
            result.append(").");
            break;
        }
        
        return result.toString();
    }
}
