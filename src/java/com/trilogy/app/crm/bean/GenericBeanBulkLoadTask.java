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
package com.trilogy.app.crm.bean;

import java.io.File;

import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkLoadInput;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkLoadTaskManager;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkloadManager;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Bulk Load Task that invokes Generic Bean Bulk Loader
 * 
 * @author simar.singh@redknee.com
 */
public class GenericBeanBulkLoadTask extends AbstractGenericBeanBulkLoadTask implements GenericBeanBulkLoadInput
{

    private static final long serialVersionUID = 1L;


    @Override
    public void execute(Context ctx) throws AgentException
    {
        Context sCtx = ctx.createSubContext();
        try
        {
            /*
             * Since Threading is still not using a sub pool of the FW Threadpool, we open
             * another door to run ourselves to crash (Too many Bulk loaders working at
             * once). For now, we will not allow more than one Bulk load execution at
             * once. This also helps us prevent competing race conditions in bulk loading.
             */
            GenericBeanBulkloadManager bulkloadMgr = new GenericBeanBulkLoadTaskManager();
            sCtx.put(GenericBeanBulkloadManager.class, bulkloadMgr);
            // Validate form information
            bulkloadMgr.validate(sCtx, this);
            // Perform bulkloading
            bulkloadMgr.bulkload(sCtx, this);
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(this, "Unexpected problem occured during Generic Bean Bulkloading.", t).log(sCtx);
            throw new AgentException(t);
        }
    }


    @Override
    public String getFilePath()
    {
        return getFileLocation();
    }


    @Override
    public String getReportFilePath()
    {
        return getLogDirectory();
    }


    @Override
    public int getBulkloader()
    {
        return getBulkLoader();
    }


    @Override
    public String getReprotFile()
    {
        File bulkFile = new File(getFileLocation());
        File errorFileDirectory = new File(getBaseDirectory(), getLogDirectory());
        return new File(errorFileDirectory, bulkFile.getName() + ".err").getAbsolutePath();
    }


    @Override
    public void setLogDirectory(String logDirectory)
    {
        try
        {
            if (null != logDirectory && logDirectory.length() > 0)
            {
                super.setLogDirectory(logDirectory);
                File directory = new File(getBaseDirectory(), logDirectory);
                if (directory.exists())
                {
                    if (!directory.isDirectory())
                    {
                        throw new IllegalArgumentException("The path [" + directory.getAbsolutePath()
                                + "] exists but is not a valid directory.");
                    }
                }
                else if (!directory.mkdirs())
                {
                    throw new IllegalArgumentException("Directories in the path [" + directory.getAbsolutePath()
                            + "] could not be created.");
                }
            }
            else
            {
                super.setLogDirectory(LOG_DIRECTORY);
            }
        }
        catch (Throwable t)
        {
            if (t instanceof IllegalArgumentException)
            {
                // should not handle, throw it again and fault the bean setting
                throw (IllegalArgumentException)t;
                
            }
            else
            {
                super.setLogDirectory(LOG_DIRECTORY);
                handleError(t, "Error in processing Log Directory [" + logDirectory + "]. Error [" + t.getMessage()
                        + "]. Default directory [" + LOG_DIRECTORY + "] will be used.");
            }
        }
    }


    @Override
    public String getLogDirectory()
    {
        if (null == this.logDirectory_ || this.logDirectory_.isEmpty())
        {
            return LOG_DIRECTORY;
        }
        else
        {
            return logDirectory_;
        }
    }


    private void handleError(Throwable t, String message)
    {
        final Context ctx = ContextLocator.locate();
        if (null != ctx)
        {
            new MinorLogMsg(this, message, t).log(ctx);
            ExceptionListener exceptionListener = (ExceptionListener) ctx.get(ExceptionListener.class);
            exceptionListener.thrown(new IllegalStateException(message,t));
        }
    }

    private final static String LOG_DIRECTORY = "log";
}
