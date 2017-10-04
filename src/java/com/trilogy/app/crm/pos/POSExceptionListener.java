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

import java.io.PrintWriter;

import com.trilogy.framework.xhome.beans.ExceptionListener;

/**
 * This Exception Listener logs exceptions to the respective process' log and error files.
 * 
 * @author Angie Li
 */
public class POSExceptionListener implements ExceptionListener 
{

    public POSExceptionListener(PrintWriter errWriter,PrintWriter logWriter)
    {
        setErrWriter(errWriter);
        setLogWriter(logWriter);
    }
    
    /**
     * Log the exception in the log and the error file if they exist.
     * Increase the count of errors by 1.
     */
    public void thrown(Throwable t) 
    {
        if(getErrWriter() != null)
        {
            if(t instanceof POSProcessorException)
            {
                getErrWriter().println(((POSProcessorException)t).getProcessor() + ": " + t.getMessage());
                getErrWriter().flush();
            }
            else
            {
                t.printStackTrace(getErrWriter());
            }
        }
        
        if(getLogWriter() != null)
        {
            getLogWriter().println(t.getMessage());
            t.printStackTrace(getLogWriter());
            getLogWriter().flush();
        }
        
        numberOfErrors++;
    }
    
    /**
     * @param writer The errWriter to set.
     */
    public void setErrWriter(PrintWriter writer)
    {
        this.errWriter=writer;
    }
    
    /**
     * @return Returns the errWriter.
     */
    public PrintWriter getErrWriter()
    {
        return errWriter;
    }
    
    /**
     * @return Returns the logWriter.
     */
    public PrintWriter getLogWriter()
    {
        return logWriter;
    }
    
    /**
     * @param logWriter The logWriter to set.
     */
    public void setLogWriter(PrintWriter logWriter)
    {
        this.logWriter = logWriter;
    }

    /**
     * @return Returns the numberOfErrors.
     */
    public int getNumberOfErrors()
    {
        return numberOfErrors;
    }

    /**
     * @param numberOfErrors The numberOfErrors to set.
     */
    public void setNumberOfErrors(int numberOfErrors)
    {
        this.numberOfErrors = numberOfErrors;
    }

    protected PrintWriter errWriter;
    protected PrintWriter logWriter;
    
    protected int numberOfErrors=0;
}
