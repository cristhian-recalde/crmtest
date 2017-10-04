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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.payment.PaymentPlan;
import com.trilogy.app.crm.bean.payment.PaymentPlanHome;
import com.trilogy.app.crm.home.PaymentPlanERLogHome;
import com.trilogy.app.crm.home.PaymentPlanOMCreationHome;
import com.trilogy.app.crm.home.core.CorePaymentPlanHomePipelineFactory;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;

/**
 * Payment Plan Pipeline Creator.
 * 
 * @author ali
 *
 */
public class PaymentPlanHomePipelineFactory extends CorePaymentPlanHomePipelineFactory
{

    @Override
    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException, AgentException 
    {
        // Already installed by core
        Home home = (Home) ctx.get(PaymentPlanHome.class);

        home = new PaymentPlanOMCreationHome(ctx, home);
        
        home = new PaymentPlanERLogHome(home);

        home = new IdentifierSettingHome(
                ctx,
                home,
                IdentifierEnum.PAYMENTPLAN_ID, null);

        // To ensure that Payment Plan IDs are larger than 0.
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx, IdentifierEnum.PAYMENTPLAN_ID, 1, Long.MAX_VALUE);

		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home, PaymentPlan.class);
        return home;
    }

}
