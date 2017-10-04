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

package com.trilogy.app.crm.transfer.web.action;

import java.io.PrintWriter;

import com.trilogy.app.crm.home.transfer.TransferDisputeCmd;
import com.trilogy.app.crm.transfer.TransferDisputeHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.LogSupport;

public class TransferDisputeCancelWebAction
    extends SimpleWebAction
{
    public TransferDisputeCancelWebAction()
    {
        super("cancelDispute", "Cancel Dispute");
    }

    public void execute(Context ctx)
        throws AgentException
    {
        String key = WebAgents.getParameter(ctx, ".disputeHistorykey");
        if (key == null || key.equals(""))
        {
            key = WebAgents.getParameter(ctx, "key");
        }
        PrintWriter out = WebAgents.getWriter(ctx);
        try
        {
            TransferDisputeCmd cmd = new TransferDisputeCmd(Long.valueOf(key), TransferDisputeCmd.CANCEL_CMD);
            Home h = (Home)ctx.get(TransferDisputeHome.class);

            h.cmd(ctx, cmd);
            printMessage(out, "Successfully cancelled dispute.");     
        }
        catch(Exception e)
        {
            LogSupport.major(ctx, this, "Unable to cancel dispute [" + key + "]", e);   
            
            printError(out, "Unable to cancel dispute [key=" + key + "] due to Exception:<br>" + e.getMessage());            
        }

        ContextAgents.doReturn(ctx);
    }
    
    private void printMessage(PrintWriter out, String msg)
    {
        out.println("<font color=\"green\">" + msg + "</font><br/><br/>");
        
    }
    
    private void printError(PrintWriter out, String error)
    {
        out.println("<font color=\"red\">" + error + "</font><br/><br/>");      
    }
}