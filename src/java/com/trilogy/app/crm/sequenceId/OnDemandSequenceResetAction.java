/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.sequenceId;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * 
 * @author ksivasubramaniam
 * Resets sequenceId to startDate and set lastreset date to current date.
 */
public class OnDemandSequenceResetAction extends SimpleWebAction
{

    private static final long serialVersionUID = -67197088052047576L;


    public OnDemandSequenceResetAction()
    {
    }


    public OnDemandSequenceResetAction(final String action, final String label)
    {
        super(action, label);
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.web.action.WebActionBean#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        String sequenceId = WebAgents.getParameter(ctx, "key");
        PrintWriter out = WebAgents.getWriter(ctx);
        String message = "";
        String errorMsg = "";
        short result = 0;
        LogSupport.info(ctx, this, "Reset sequence " + sequenceId + " was invoked ");

        try
        {
            OnDemandSequenceManager.reset(ctx, sequenceId);
        }
        catch (Exception e)
        {
            LogSupport.info(ctx, this, "Problem occured in sequenceID [" + sequenceId + " reset.", e);
            result = -1;
        }

        if (result == 0)
        {
            message = "Sequence reset  was succesful for key [" + sequenceId+ "]. ";
            message = "<table width=\"70%\"><tr><td><center><b style=\"color:green;\">" + message + "</b></center></td></tr></table>";
        }
        else
        {
            message = "Problem occured in sequence reset for sequenceID [" + sequenceId + "]. ";
            message = "<table width=\"70%\"><tr><td><center><b style=\"color:red;\">" + message + "</b></center></td></tr></table>";
        }
        out.println(message);
        if (!"".equals(errorMsg))
        {
            out.println("<table width=\"70%\"><tr><td><center><b style=\"color:red;\">" + errorMsg + "</b></center></td></tr></table>");
        }
        out.println("<table width=\"70%\"><tr><td><center>");
        ContextAgents.doReturn(ctx);
        out.println("</center></td></tr></table>");
    }


} 
