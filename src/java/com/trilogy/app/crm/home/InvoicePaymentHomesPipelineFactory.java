/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.paymentprocessing.InvoicePaymentRecord;
import com.trilogy.app.crm.paymentprocessing.InvoicePaymentRecordHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Invoice Payment Record home installer.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.4
 */
public class InvoicePaymentHomesPipelineFactory implements PipelineFactory
{

    /**
     * @see com.redknee.app.crm.home.PipelineCreator#createPipeline(com.redknee.framework.xhome.context.Context,
     *      com.redknee.framework.xhome.context.Context)
     */
    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException, AgentException
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, InvoicePaymentRecord.class,
                "InvoicePaymentRecord");

        home = new AuditJournalHome(ctx, home);
        ctx.put(InvoicePaymentRecordHome.class, home);

        return home;
    }
}
