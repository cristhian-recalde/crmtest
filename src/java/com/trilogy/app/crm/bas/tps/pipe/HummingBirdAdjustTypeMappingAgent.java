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
import com.trilogy.framework.xlog.log.EntryLogMsg;

import com.trilogy.app.crm.bas.tps.TPSAdjMap;
import com.trilogy.app.crm.bas.tps.TPSAdjMapHome;
import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/**
 * Map TPS payment to CRM payment type.
 *
 * @author larry.xia@redknee.com
 */
public class HummingBirdAdjustTypeMappingAgent extends PipelineAgent
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>HummingBirdAdjustTypeMappingAgent</code>.
     *
     * @param delegate
     *            The delegate of this agent.
     */
    public HummingBirdAdjustTypeMappingAgent(final ContextAgent delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class);
        final Account acct = (Account) ctx.get(Account.class);
        try
        {
            AdjustmentType type = null;
            
            if (tps.getPaymentType() == null || tps.getPaymentType().trim().length() == 0)
            {
                type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx, AdjustmentTypeEnum.StandardPayments);

            }
            else
            {
                final Home mapHome = (Home) ctx.get(TPSAdjMapHome.class);
                final TPSAdjMap map = (TPSAdjMap) mapHome.find(ctx, tps.getPaymentType());
                if (map != null)
                {
                    int adjustmentType;
                    if (tps.getVoidFlag())
                    {
                        adjustmentType = map.getReverseAdjType();
                    }
                    else
                    {
                        adjustmentType = map.getAdjType();
                    }
                    type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx, adjustmentType);
                }
                else
                {
                    throw new Exception("Illegale Adjustment Type");
                }
            }

            if (type == null)
            {
                throw new Exception("The adjustment type for " + tps.getPaymentType() + " does noe exist");
            }

            ctx.put(AdjustmentType.class, type);
            tps.setAdjType(type); 
            pass(ctx, this, "Adjustment type mapped to " + type.getCode());
        }
        catch (final Exception e)
        {

            ERLogger.genAccountAdjustmentER(ctx, tps, TPSPipeConstant.FAIL_TO_FIND_ADJUST_TYPE);

            // send out alarm
            new EntryLogMsg(10534, this, "", "", new String[]
            {
                "Adjustment type table searching fails",
            }, e).log(ctx);
            fail(ctx, this, "Lookup adjustment type fails", e, TPSPipeConstant.FAIL_TO_FIND_ADJUST_TYPE);

        }
    }

}
