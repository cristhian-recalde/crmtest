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
package com.trilogy.app.crm.client.aaa;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.service.aaa.RMIAAAService;


/**
 * Provides a RequestServicer that forces a reset.
 * @author deepak.mishra@redknee.com
 */
public class AAAResetRequestServicer implements RequestServicer
{

    public AAAResetRequestServicer()
    {
        // EMPTY
    }


    public void service(Context context, final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException
    {
        context = context.createSubContext();
        context.setName(this.getClass().getName());
        final PrintWriter out = (PrintWriter) context.get(PrintWriter.class);
        try
        {
            AAAGatewayRMIClient aAAGatewayRMIClient = (AAAGatewayRMIClient) context.get(AAAGatewayRMIClient.class);
            aAAGatewayRMIClient.reset(context);
            if (context.get(RMIAAAService.class) != null)
            {
                out.print("Reset complete.");
            }
            else
            {
                out.print("Reset complete but no RMI communication established.<br>");
                out.print("Check configuration for correctness.  Check logs for failure details.");
            }
        }
        catch (Throwable t)
        {
            out.print("Attempt to reset caused a problem: " + t.getMessage());
        }
    }
} // class
