/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.app.crm.voicemail.VoiceMailManageInterface;
import com.trilogy.driver.voicemail.mpathix.web.control.ConnectDisconnectWebControl;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgent;


/**
 * @author Prasanna.Kulkarni
 * @time Oct 22, 2005
 */
public class VMConnectDisconnectWebAgent extends ServletBridge implements WebAgent
{ 
    public void execute(Context ctx) throws AgentException
    {
        PrintWriter out = getWriter(ctx);
        HttpServletRequest req = getRequest(ctx);
        String action = req.getParameter(ConnectDisconnectWebControl.ACTION);
        if (action == null)
        {
            return;
        }
        
        VoiceMailManageInterface client = (VoiceMailManageInterface)ctx.get(VoiceMailManageInterface.class); 

        if (action.equalsIgnoreCase(ConnectDisconnectWebControl.CONNECTCMD))
        {
             client.connect();
            return;
        }
        if (action.equalsIgnoreCase(ConnectDisconnectWebControl.DISCONNECTCMD))
        {
            client.disconnect();
             return;
        }
        if (action.equalsIgnoreCase(ConnectDisconnectWebControl.RECONNECTCMD))
        {
            client.reconnect();
            return;
        }
    }


    private void printMessage(PrintWriter out, String message)
    {
        out.println("<br/><h3>");
        out.println(message);
        out.println("</h3><br/>");
    }

    private static final long serialVersionUID = 1L;
}