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
import com.trilogy.framework.xlog.log.EntryLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

/**
 * look up currency for the transaction, the currency is needed when generating receipt.
 *
 * @author larry.xia@redknee.com
 */
public class CurrencyLookupAgent extends PipelineAgent
{

    public CurrencyLookupAgent(final ContextAgent delegate)
    {
        super(delegate);
    }

    /**
     * @param ctx A context
     * @throws AgentException thrown if one of the services fails to initialize
     */

    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final Account acct = (Account) ctx.get(Account.class);

        final Transaction trans = (Transaction) ctx.get(Transaction.class);

        try
        {
            final CRMSpid crmspid = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, acct.getSpid());
            if (crmspid != null)
            {
                // TODO 2007-01-22 Do not navigate context
                final Context upperctx = (Context) ctx.get("..");
                upperctx.put(CRMSpid.class, crmspid);
                pass(ctx, this, "Service Provider found " + acct.getSpid());
            }
            else
            {
                throw new Exception("Service Provider not found" + acct.getSpid());
            }

        }
        catch (Exception e)
        {

            ERLogger.genAccountAdjustmentER(ctx,
                    trans,
                    TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                    TPSPipeConstant.FAIL_TO_FIND_SERVICE_PROVIDER);

            // send out alarm
            new EntryLogMsg(10534, this, "", "", new String[]{"Can not find service provider " + acct.getSpid()},
                    e).log(ctx);
            fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_FIND_SERVICE_PROVIDER);
        }
    }
}
