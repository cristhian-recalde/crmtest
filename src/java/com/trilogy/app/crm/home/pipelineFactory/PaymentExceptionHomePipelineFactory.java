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

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.app.crm.bean.payment.PaymentExceptionHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;

/**
 * Pipeline factory for Payment Exception.
 * 
 * @author Angie Li
 *
 */
public class PaymentExceptionHomePipelineFactory implements PipelineFactory
{
    public PaymentExceptionHomePipelineFactory() {}

    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException, AgentException 
    {
        Home paymentExceptionHome = StorageSupportHelper.get(ctx).createHome(ctx, PaymentException.class, "PAYMENTEXCEPTION");
        paymentExceptionHome = new IdentifierSettingHome(
                       ctx,
                       paymentExceptionHome,
                       IdentifierEnum.PAYMENTEXCEPTION_ID, null);
        
        IdentifierSequenceSupportHelper.get(ctx).ensureNextIdIsLargeEnough(ctx, IdentifierEnum.PAYMENTEXCEPTION_ID, paymentExceptionHome);
              
        paymentExceptionHome = new SortingHome(paymentExceptionHome);
        
        ctx.put(PaymentExceptionHome.class, paymentExceptionHome);
        
        return paymentExceptionHome;
    }

}
