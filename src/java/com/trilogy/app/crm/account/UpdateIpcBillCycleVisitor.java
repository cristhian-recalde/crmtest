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
package com.trilogy.app.crm.account;

import java.util.Set;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.ipcg.IpcgClient;
import com.trilogy.app.crm.client.ipcg.IpcgClientFactory;
import com.trilogy.app.crm.client.ipcg.IpcgClientProxy;

/**
 * @author rattapattu
 */
public class UpdateIpcBillCycleVisitor implements Visitor
{
    private short billCycleDate;

    public UpdateIpcBillCycleVisitor(short billCycleDate)
    {
        this.billCycleDate = billCycleDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public void visit(Context ctx, Object obj) throws AgentException,
            AbortVisitException
    {
        Subscriber sub = (Subscriber) obj;
        Set svcSet = sub.getServices(ctx);
        if (IpcgClientProxy.hasDataService(ctx, svcSet))
        {
            IpcgClient ipcgClient = IpcgClientFactory.locateClient(ctx, sub.getTechnology());
            try
            {
                ipcgClient.addChangeSubBillCycleDate(ctx, sub, billCycleDate);
                new InfoLogMsg(this, "Updated IPCG's bill cycle date for subscription " + sub.getId() + " to " + billCycleDate, null).log(ctx);
            }
            catch (Exception e)
            {
                // we are not going to abort the process.
                LogSupport.major(ctx, this, "Failed to update bill cycle day for subscriber " + sub.getId());
            }
        }
    }
    
   
}
