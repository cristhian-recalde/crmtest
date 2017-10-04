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

import com.trilogy.app.crm.Common;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * @author ali
 *
 * Generates OMs for Payment Plan Home operations.
 */
public class PaymentPlanOMCreationHome extends HomeProxy {
	
	public PaymentPlanOMCreationHome(Context ctx, final Home delegate)
	{
		super(ctx, delegate);
	}

    // INHERIT
    public Object create(Context ctx,final Object obj) throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_PLAN_CREATION_ATTEMPT).log(ctx);

        final Object result;
        try
        {
            result = super.create(ctx,obj);
            new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_PLAN_CREATION_SUCCESS).log(ctx);
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_PLAN_CREATION_FAILURE).log(ctx);
            throw exception;
        }

        return result;
    }
}
