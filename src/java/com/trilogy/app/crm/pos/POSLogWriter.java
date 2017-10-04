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
package com.trilogy.app.crm.pos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.web.XlogExceptionListener;

/**
 * Point of Sale Log Writer
 * 
 * At this point no Error log is supported.  
 * If it were implemented it would require more work to initLogFileWriter(Context, String)
 * and to be passed into the exception listener.
 * 
 * @author Angie Li
 */
public class POSLogWriter implements ContextAware
{

    /**
     * Creates a POS Log Writer with for the given processor
     * @param ctx
     * @param name - the processor name
     * @param processor - the POS extract processor
     */
    public POSLogWriter(Context ctx, String name, PointOfSaleFileWriter processor)
    {
        processorName_ = name;
        initLogFileWriter(ctx, processor.getFileName(ctx));
    }
    
    /**
     * Creates a POS Log Writer with the given filename and processor name 
     * @param ctx
     * @param name - the processor name will be used as the log file name as well.
     */
    public POSLogWriter(Context ctx, String name)
    {
        processorName_ = name;
        initLogFileWriter(ctx, name);
    }
    
    /**
     * Initializes this processor's log file.
     * If Logging is disabled, then log all errors in the AppCRM log as a MajorLogMessage, 
     * and all writeToLog messages are to be written as Debug Messages.
     * If Logging is enabled:
     * If the file exists, it is deleted and newly created. 
     * There is no separate error file for the processor, so the exception listener only writes to the log.
     * @param ctx
     * @param fileName - name of file to which this processor is writing. This method add the Log file extension to the filename for the log.
     * @return
     * @throws IOException
     */
    private void initLogFileWriter(Context ctx, String fileName) 
    {
        setContext(ctx);
        PrintWriter writer = null;
        PointOfSaleConfiguration config = (PointOfSaleConfiguration) ctx.get(PointOfSaleConfiguration.class);
        
        if (config != null && config.getDisablePOSLog())
        {
            logWriter_ = null;
            listener_ = new XlogExceptionListener(ctx);
        }
        else
        {
            String path = ".";
            if (config != null && config.getLogDirectory().trim().length() > 0 )
            {
                path = config.getLogDirectory().trim();
            }
            
            File dir = new File(path);
            
            if (!dir.exists())
            {
                dir.mkdir();
            }
            //Append the ".log" extension on to the end of the filename
            String filePath = path + File.separator + fileName + POS_LOG_FILE_EXTENSION;
            File logFile = new File(filePath);
            if (logFile.exists())
            {
                logFile.delete();
            }
            try
            {
                logFile.createNewFile();
                
                logWriter_ = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
                /* At this time there is no explicit Error Log although the POSExceptionListener supports it. 
                 * The error file can be added later here if it is needed.*/
                listener_ = new POSExceptionListener(null, logWriter_);
            }
            catch (IOException ioe)
            {
                new MajorLogMsg(this, "Failed to create this Log File: " + filePath, ioe).log(ctx);
                listener_ = new XlogExceptionListener(ctx);
            }
        }
        
        writeToLog("***Processor Start-up at " + (new Date()).toString() + "***");
    }
    
    /**
     * Flushes and closes the log file print writer for this processor. 
     */
    public void closeLogFileWriter()
    {
        if (getLogWriter() != null)
        {
            writeToLog("***Processor Completed at " + (new Date()).toString() + "***");
            
            getLogWriter().flush();
            getLogWriter().close();
        }
    }
    
    /**
     * Writes the given message into the POS log file for this processor.
     * @param msg
     */
    public void writeToLog(String msg)
    {
        if (getLogWriter() != null)
        {
            getLogWriter().println(msg);
            getLogWriter().flush();
        }
        else
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, msg, null).log(getContext());
            }
        }
    }
    
    /**
     * Invokes the Exception listener's thrown
     * @param t
     */
    public void thrown(Throwable t) 
    {
        getExceptionListener().thrown(t);
    }
    
    /** 
     * Returns the PrintWriter for the POS Log file for this processor.
     * @return
     */
    public PrintWriter getLogWriter()
    {
        return logWriter_;
    }
    
    /**
     * Returns the POSExceptionListener for this processor.
     * @return
     */
    public ExceptionListener getExceptionListener()
    {
        return listener_;
    }

    /**
     * @return Returns the context.
     */
    public Context getContext()
    {
        return context_;
    }
    
    /**
     * @param context The context to set.
     */
    public void setContext(Context context)
    {
        context_ = context;
    }

    /** File Suffix for Log Files */
    private static final String POS_LOG_FILE_EXTENSION = ".log";
    /** Context */
    private Context context_;
    /** The processor for which this logger is writing */
    private String processorName_;
    /** Log Writer */
    private PrintWriter logWriter_;
    /** Exception Listener */
    private ExceptionListener listener_;
}
