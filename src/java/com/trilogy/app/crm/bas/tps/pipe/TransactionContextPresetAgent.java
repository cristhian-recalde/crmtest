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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;


/**
 *
 * someone put account/subscriber in upper level of context, we use this agent to make
 * sure that the account and subscriber in context is the one we need. I hate to this, but
 * it will take time to find the root cause.
 *
 * arturo.medina@redknee.com: From now on (HLD 4.12) payments will be searched and based on the
 * subscriber Id and not the MSISDN
 *
 * @author larry.xia@redknee.com
 *
 */
public class TransactionContextPresetAgent extends PipelineAgent
{

    /**
     * Constructor that receives a delegate to continue the pipeline.
     * @param delegate the next pipe in the pipeline to delegate
     */
    public TransactionContextPresetAgent(final ContextAgent delegate)
    {
        super(delegate);
    }

    /**
     * executes the logic for that partucular agent.
     * @param ctx
     *           A context
     * @exception AgentException
     *               thrown if one of the services fails to initialize
     */

    public void execute(final Context ctx)
        throws AgentException
    {
        final Transaction trans = (Transaction) ctx.get(Transaction.class);
        Subscriber sub = (Subscriber)ctx.get(Subscriber.class);
        Account acct = (Account) ctx.get(Account.class);

        try
        {
            if (trans.getSubscriberID() == null || trans.getSubscriberID().trim().length() <= 0)
            {
                throw new AgentException("The payemnt for MSISDN "
                        + trans.getMSISDN()
                        + " doesn't have a subscriber ID specified");
            }

            if (sub == null || !sub.getId().equals(trans.getSubscriberID()))
            {
                sub = SubscriberSupport.lookupSubscriberForSubId(ctx, trans.getSubscriberID());
                ctx.put(Subscriber.class, sub);
            }
            if (acct == null || !acct.getBAN().equals(trans.getAcctNum()))
            {
                acct = sub.getAccount(ctx);
                ctx.put(Account.class, acct);
            }
        }
        catch ( Exception e)
        {
            ERLogger.genAccountAdjustmentER(ctx,
                    trans,
                    TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                    TPSPipeConstant.FAIL_TO_FIND_SERVICE_PROVIDER);
            fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_FIND_SERVICE_PROVIDER);
        }
        pass(ctx, this, "Context reset");
    }

    /**
     * the serial version UID
     */
    private static final long serialVersionUID = -1995399106226677964L;

}
