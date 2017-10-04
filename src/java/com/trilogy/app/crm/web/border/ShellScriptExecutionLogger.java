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
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Provides the logging feature to capture the information about shell
 * script execution.
 *
 * @author jimmy.ng@redknee.com
 */
public class ShellScriptExecutionLogger
{
    /**
     * Log the given script information in a separate log file.
     * 
     * @param ctx The operating context.
     * @param name The name of the script.
     * @param type The type of the script.
     * @param script The content of the script.
     * @param begin The date/time when the script execution begins.
     * @param end The date/time whent the script execution ends.
     */
    public void log(
        final Context ctx,
        final String name,
        final String type,
        final String content,
        final Date begin,
        final Date end)
    {
        // Create the log directory (if not already exist).
        final String log_dir = getLogDirectory(ctx);
        new File(log_dir).mkdirs();

        // Create the log file in the log directory.
        // TODO 2010-10-01 DateFormat access needs synchronization
        final String filename
            = log_dir
            + File.separator
            + type.toLowerCase()
            + NAME_DATE_FORMAT.format(begin)
            + ".log";
        PrintStream out = null;
        try
        {
            out = new PrintStream(new FileOutputStream(new File(filename)));
        }
        catch (IOException e)
        {
            new MinorLogMsg(
                this,
                "IO error occured when creating log file \"" + filename + "\"",
                e).log(ctx);
            return;
        }

        // Create content in the log file.
        // TODO 2010-10-01 DateFormat access needs synchronization
        out.println("[Begin Date/Time : " + LOG_DATE_FORMAT.format(begin) + "]");
        out.println("[End Date/Time   : " + LOG_DATE_FORMAT.format(end) + "]");
        if (name != null)
        {
            out.println("[Script Name     : " + name + "]");
        }
        if (type != null)
        {
            out.println("[Script Type     : " + type + "]");
        }
        out.println();
        out.println(content);
    }


    /**
     * Return the name of the directory that stores the log files.
     * 
     * @param ctx The operating context.
     * 
     * @return String The directory name.
     */
    protected String getLogDirectory(final Context ctx)
    {
        if (logDirectory_ == null)
        {
            logDirectory_ = CoreSupport.getProjectHome(ctx);
            if(!logDirectory_.endsWith("\\") && !logDirectory_.endsWith("/"))
            {
                logDirectory_ += File.separator;
                logDirectory_ += "log";
                logDirectory_ += File.separator;
                logDirectory_ += "scripts_executed";
            }
        }
        return logDirectory_;
    }
    
    
    // Directory that stores the log files.
    protected String logDirectory_ = null;
    
    // Formatter for date/time to be stored in the log file.
    protected static final SimpleDateFormat LOG_DATE_FORMAT
        = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
    
    // Formatter for date/time to be used as part of the log file name.
    protected static final SimpleDateFormat NAME_DATE_FORMAT
        = new SimpleDateFormat("-yyyyMMdd-HHmmss");

} // class
