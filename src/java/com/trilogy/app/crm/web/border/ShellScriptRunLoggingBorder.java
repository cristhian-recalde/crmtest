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
package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;


/**
 * Provides the logging feature to capture the script information when
 * the "Run" button is clicked.
 *
 * @author jimmy.ng@redknee.com
 */
public class ShellScriptRunLoggingBorder
    extends ShellScriptExecutionLogger
    implements Border
{
    // INHERIT
    public void service(
        final Context ctx,
        final HttpServletRequest req,
        final HttpServletResponse res,
        final RequestServicer delegate)
        throws ServletException, IOException
    {
        // Capture both begin and end date/time.
        final Date begin_datetime = Calendar.getInstance().getTime();
        delegate.service(ctx, req, res);
        final Date end_datetime = Calendar.getInstance().getTime();
        
        // Store the script execution information in a log file.
        final String script_action = req.getParameter("script_action");
        if ("Run".equals(script_action))
        {
//            final String script_name = req.getParameter("script_loadname");
            final String script_type = req.getParameter("script_type");
            final String script_content = req.getParameter("script_script");
            
            log(ctx,
                null,
                script_type,
                script_content,
                begin_datetime,
                end_datetime);
        }
    }
    
} // class
