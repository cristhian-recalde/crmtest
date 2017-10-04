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
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.home.sub.UserAdjustmentLimitValidator;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/**
 * Calculate and reset credit limit of a subscriber.
 *
 * @author larry.xia@redknee.com
 */
public class SubscriberCreditLimitUpdateAgent extends PipelineAgent
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 3000361168780459402L;

    /**
     * Use this as a key in the context for a boolean value to explicitly enable or
     * disable this processor. True if processing should occur; false otherwise.
     */
    public static final String ENABLE_PROCESSING = "SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING";


    /**
     * Creates a new instance of <code>SubscriberCreditLimitUpdateAgent</code>.
     *
     * @param delegate
     *            The delegate of this agent.
     */
    public SubscriberCreditLimitUpdateAgent(final ContextAgent delegate)
    {
        super(delegate);
    }


    /**
     * Sets the credit limit of the subscriber.
     *
     * @param ctx
     *            A context
     * @exception AgentException
     *                thrown if one of the services fails to initialize
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "UPS subscriber credit limit updating", null).log(ctx);
        }

        if (!ctx.getBoolean(ENABLE_PROCESSING, true))
        {
            pass(ctx, this, "Processing explicitly disabled in context.");
            return;
        }

        final Account acct = (Account) ctx.get(Account.class);
        final Subscriber subs = (Subscriber) ctx.get(Subscriber.class);
        final Transaction trans = (Transaction) ctx.get(Transaction.class);

        try
        {
            long amount = trans.getAmount();

            /*
             * Check if its a subscriber move, if it is, we should not use the entire
             * transaction amount because in a move, the deposit made transaction is for
             * the entire deposit. We perform a deposit made for the deposit entered into
             * the subscriber move form because we want the transaction to show up on the
             * subscriber's transaction screen for tracking purposes. So to avoid
             * increasing the credit limit unnessecary, we fetch the delta of the deposit
             * on move from the context
             */
            if (ctx.get(Common.MOVE_SUBSCRIBER) != null)
            {
                amount = ((Long) ctx.get(Common.SUBSCRIBER_MOVE_DEPOSIT_DELTA)).longValue();

                /*
                 * Dont do credit limit adjustment on deposit lowering in subscriber move
                 * HLD Object ID 11882
                 */
                if (amount < 0)
                {
                    amount = 0;
                }
            }

            final CreditCategory creditCategory = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class, Integer.valueOf(acct.getCreditCategory()));

            /*
             * [Cindy] 2007-11-12: Credit limit should not be updated on deposit release
             * if the option is not set in SPID.
             */
            if (amount > 0 || SpidSupport.isCreditLimitUpdatedOnDepositRelease(ctx, subs.getSpid()))
            {
                subs.setCreditLimit((long) (subs.getCreditLimit(ctx) + amount * creditCategory.getFactor()));

                /*
                 * [Cindy] 2007-11-27 TT7102900043: Skip credit limit check when the
                 * change is caused by deposit made.
                 */
                ctx.put(UserAdjustmentLimitValidator.SKIP_USER_ADJUSTMENT_LIMIT_VALIDATION, true);
            }

            pass(ctx, this, "credit limit changed to " + subs.getCreditLimit());
        }
        catch (final Exception e)
        {
            ERLogger.genAccountAdjustmentER(ctx, trans, TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                TPSPipeConstant.FAIL_UPDATE_CREDIT_LIMIT);

            new EntryLogMsg(10535, this, "", "", null, e).log(ctx);
            fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_UPDATE_SUSCRIBER_TABLE);
        }

    }

}
