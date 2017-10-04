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
package com.trilogy.app.crm.bulkloader.generic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.LinkedBlockingQueue;

import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.lifecycle.LifecycleSupport;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.context.ContextFactoryProxy;
import com.trilogy.framework.xhome.context.ThreadLocalContextFactory;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.log.PPMLogMsg;
import com.trilogy.framework.xlog.logger.ChannelFileAdapterLogger;
import com.trilogy.framework.xlog.logger.ChannelLogger;
import com.trilogy.framework.xlog.logger.FileLogger;
import com.trilogy.framework.xlog.logger.FileLoggerConfig;
import com.trilogy.framework.xlog.logger.FileLoggerConfigHome;
import com.trilogy.framework.xlog.logger.FileLoggerConfigTransientHome;
import com.trilogy.framework.xlog.logger.LoggerException;
import com.trilogy.framework.xlog.logger.LoggerSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * This class provides a point of entry for the Generic Bean Bulk Load Process.
 * Given a request it will execute the relevant load operation.
 * @author angie.li@redknee.com
 * @since 8.2
 */
public class GenericBeanBulkloadManager 
{
    
    /**
     * Validate the bulkload file and error file.
     * Set up the Loggers
     * @param ctx
     * @param request
     */
    public void validate(Context ctx, GenericBeanBulkLoadInput form)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Validating the Generic Bean Bulkloading Request. " + form.toString(), null).log(ctx);
        }
        
        File inputFile = new File(form.getFilePath().trim()) ;
        if (!inputFile.exists())
        {
            throw new IllegalPropertyArgumentException(GenericBeanBulkloaderRequestXInfo.FILE_PATH, 
                    "The given Bulkload input file doesn't exist.  Please select a valid Bulkload file.  The file was expected at: " 
                    + inputFile.getAbsolutePath());
        }
        String inputFileName = inputFile.getName();

        //If the log files exist for this file, then abort the bulkloading
        String errorDirectory = form.getReportFilePath().trim();
        validateFileLogger(ctx, 
                errorDirectory, 
                inputFileName, 
                PROGRESS_LOG_FILE_SUFFIX, 
                BulkloadConstants.GENERIC_BULKLOADER_LOG_FILE_LOGGER);
        validateFileLogger(ctx, 
                errorDirectory, 
                inputFileName, 
                FAILED_RECORDS_FILE_SUFFIX, 
                BulkloadConstants.GENERIC_BULKLOADER_ERROR_FILE_LOGGER);
        
        //TODO: Validate the Bulkloader configuration?
        /* Here we would like to see validation on the first records (lines) read in from the input file.
         */

    }
    
    protected PPMLogMsg createProgressMonitor(Context ctx)
    {
        // TODO Auto-generated method stub
        return new PPMLogMsg(ctx, BulkloadConstants.GENERIC_BULKLOADER_MODULE, bulkloaderIdentifier_);
    }
    
    /**
     * Validate that the log file doesn't already exist.
     * @param ctx
     * @param errorDirectory
     * @param inputFileName
     * @param suffix
     * @param fileLoggerConfigKey
     */
    private void validateFileLogger(Context ctx, final String errorDirectory, final String inputFileName, final String suffix,
            final String fileLoggerConfigKey) 
    {
        try
        {
            String configuredFileName = inputFileName + suffix;
            //Create Log file Logger
            Context subCtx = ctx.createSubContext();
            FileLoggerConfig cfg = updateFileLoggerConfig(subCtx, fileLoggerConfigKey, configuredFileName, errorDirectory);
            File errorFile = new File (LoggerSupport.getPath(subCtx, cfg), configuredFileName); 
            if (errorFile.exists())
            {
                throw new IllegalPropertyArgumentException(GenericBeanBulkloaderRequestXInfo.REPORT_FILE_PATH, 
                        "A Bulkload Error report already exists in the Report Directory.  Please remove the file " +
                        errorFile.getAbsolutePath() +  " or select another valid directory.");
            }
        }
        catch (LoggerException e)
        {
            throw new IllegalPropertyArgumentException(GenericBeanBulkloaderRequestXInfo.REPORT_FILE_PATH, 
                    "Failed to initialize the Bulkloader Logger due to LoggerSupport error: " + e.getMessage());
        }
        catch (HomeException e)
        {
            throw new IllegalPropertyArgumentException(GenericBeanBulkloaderRequestXInfo.REPORT_FILE_PATH, 
                    "Failed to update File Logger Config Configuration. Configure the File Logger identified by key=" + fileLoggerConfigKey);
        } 
    }

    private void initialize(Context ctx, GenericBeanBulkLoadInput form)
    {
        File inputFile = new File(form.getFilePath().trim()) ;
        String inputFileName = inputFile.getName();

        String errorDirectory = form.getReportFilePath().trim();
        
        //Create the Progress log writer 
        logFileLogger_ = configureFileLogger(ctx, 
                errorDirectory, 
                inputFileName, 
                PROGRESS_LOG_FILE_SUFFIX, 
                BulkloadConstants.GENERIC_BULKLOADER_LOG_FILE_LOGGER, 
                /* Because of multi-threading, wrap the BulkloaderLogMsgFormat by ThreadLocalContextFactory so that it's local variables aren't 
                 * overwritten by competing threads.  Frameworks formatters (XXLogMsgFormat) are ThreadLocal so nothing needs to be synchronized. */ 
                new ThreadLocalContextFactory(ctx, new ContextFactory() {
                    public Object create(Context ctx1) {
                        return new BulkloaderLogMsgFormat();
                    }
                }));
        //Create the Error log writer
        errorFileLogger_ = configureFileLogger(ctx, 
                errorDirectory, 
                inputFileName, 
                FAILED_RECORDS_FILE_SUFFIX, 
                BulkloadConstants.GENERIC_BULKLOADER_ERROR_FILE_LOGGER,
                /* Because of multi-threading, wrap the LogMsgFormat by ThreadLocalContextFactory so that it's local variables aren't 
                 * overwritten by competing threads. */ 
                new ThreadLocalContextFactory(ctx, new ContextFactory() {
                    public Object create(Context ctx1) {
                        return new BulkloaderErrorLogMsgFormat();
                    }
                }));

        //Assign Bulk Loader to this manager
        try
        {
            GenericBeanBulkloader bulkloader = HomeSupportHelper.get(ctx).findBean(
                    ctx, 
                    GenericBeanBulkloader.class, 
                    form.getBulkloader());
            bulkloader_ = new CSVParser(ctx, bulkloader);
        }
        catch (Exception e)
        {
            String msg = "Error occurred while creating Generic CSVParser.";
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new IllegalStateException("Failure occured when initializing the Generic Bean Bulkload Parser.", e);
        }
        
        //Create input file reader
        try
        {
        	 inputFile_ = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF8"));
        }
        catch(UnsupportedEncodingException e)
        {
        	throw new IllegalStateException("Failure occurred when initializing the input file " + inputFile.getAbsolutePath(), e);
        }
        catch(IOException e)
        {
            //Report any errors to the application log.  All the error prevention should have covered the cases though.
            throw new IllegalStateException("Failure occurred when initializing the input file " + inputFile.getAbsolutePath(), e);
        }
        
        try
        {
            //Initialize the progress measurement
            LineNumberReader lineCounter = new LineNumberReader(new InputStreamReader(new FileInputStream(inputFile),"UTF8"));
            String line = lineCounter.readLine();
            while (line != null)
            {
                //Read Lines until the end of file.
            	// TT#12072704024 skip empty lines from the total count, (updates count, does not change cur position)
            	if(line.trim().length() == 0)
            	{
            		lineCounter.setLineNumber(lineCounter.getLineNumber() - 1);
            	}
            	line = lineCounter.readLine();
            }
            totalNumberOfCSVCommands_ = lineCounter.getLineNumber();  // No need to offset by 1
            lineCounter.close();
            bulkloaderIdentifier_ = "[" + bulkloader_.getBulkloaderConfig().getIdentifier() + " " 
                    +  bulkloader_.getBulkloaderConfig().getName() + "] "+  inputFileName  + ": ";
            progress_ = createProgressMonitor(ctx);
        }
        catch(UnsupportedEncodingException e)
        {
        	throw new IllegalStateException("Failure occurred when initializing the input file " + inputFile.getAbsolutePath(), e);
        }
        catch(IOException e)
        {
            //Report any errors to the application log.  All the error prevention should have covered the cases though.
            throw new IllegalStateException("Failure occured when initializing the Progress Counter.", e);
        }
        
        //Initialize threadpool
        GenericBeanBulkloaderThreadPoolConfig config = (GenericBeanBulkloaderThreadPoolConfig) ctx.get(GenericBeanBulkloaderThreadPoolConfig.class);
        pool_ = new BulkloadProducerAgent(ctx, 
                new GenericBeanBulkloadAgent(this), 
                BulkloadConstants.GENERIC_BULKLOADER_MODULE, 
                config.getThreadCount(), 
                config.getQueueSize());
    }
    
    /**
     * Validate that the file doesn't already exist.  If not, create and register the Logger that will write the log file.
     * @param ctx
     * @param errorDirectory
     * @param inputFileName
     * @param suffix
     * @param fileLoggerConfigKey
     * @param logger
     * @param format
     */
    private ChannelLogger configureFileLogger(Context ctx, final String errorDirectory, final String inputFileName, final String suffix,
            final String fileLoggerConfigKey, final ContextFactoryProxy format) 
    {
        try
        {
            String configuredFileName = inputFileName + suffix;
            //Create Log file Logger
            Context subCtx = ctx.createSubContext();
            //Update the File loggers with the Bulk load request paths
            FileLoggerConfig cfg = updateFileLoggerConfig(subCtx, fileLoggerConfigKey, configuredFileName, errorDirectory);
            
            FileLogger fileLogger = new FileLogger(subCtx);
            ChannelLogger logger = new ChannelLogger(
                    subCtx,
                    new ChannelFileAdapterLogger(subCtx, fileLogger),
                    new LinkedBlockingQueue(cfg.getQueueSize()),
                    fileLoggerConfigKey);
            //Execute Logger Lifecycle.
            ((ContextAgent) logger).execute(subCtx);
            subCtx.put(BulkloaderLogMsgFormat.class, format);
            return logger;
        }
        catch (AgentException e)
        {
            IllegalPropertyArgumentException newException = new IllegalPropertyArgumentException(GenericBeanBulkloaderRequestXInfo.REPORT_FILE_PATH, 
                    "Failed to initialize the Bulkloader Logger due to error: " + e.getMessage());
            throw newException;
        }
        catch (HomeException e)
        {
            IllegalPropertyArgumentException newException = new IllegalPropertyArgumentException(GenericBeanBulkloaderRequestXInfo.REPORT_FILE_PATH, 
                    "Failed to update File Logger Config Configuration. Configure the File Logger identified by key=" + fileLoggerConfigKey);
            throw newException;
        }
    }

    /**
     * Save the configuration of the File Logger to the System, and set the File Logger into the context
     */ 
    private FileLoggerConfig updateFileLoggerConfig(Context ctx, 
            final String key, final String fileName, final String filePath) 
    throws HomeException
    {
        final Home home;
        {
            Home oringinalFileLoggerCfgHome = (Home) ctx.get(FileLoggerConfigHome.class);
            Home transientFileLoggerCfgHome = new FileLoggerConfigTransientHome(ctx);
            Homes.copy(oringinalFileLoggerCfgHome, transientFileLoggerCfgHome);
            home = transientFileLoggerCfgHome;
        }
        ctx.put(FileLoggerConfigHome.class, home);
        FileLoggerConfig oldCfg = (FileLoggerConfig) home.find(ctx, key);
        FileLoggerConfig cfg = null;
        if (oldCfg != null)
        {
            cfg = oldCfg;
            cfg.setFileName(fileName);
            cfg.setPath(filePath);
            home.store(ctx,cfg);
        }
        else
        {
            cfg = new FileLoggerConfig();
            cfg.setKey(key);
            cfg.setFileName(fileName);
            cfg.setPath(filePath);
            cfg.setAppend(true);
            home.create(cfg);
        }
        
        ctx.put(FileLoggerConfig.class, cfg);
        
        return cfg;
        
    }

    /**
     * TODO: We could make this implementation Multi-threaded by creating a Bulkloading 
     * Service Servicer to service the bulkloading requests. 
     * The decision to leave the processor as a single thread came about due to time constraints.  
     * March 30, 2009.
     * 
     * @param ctx
     * @param form
     */
    public void bulkload(Context ctx, GenericBeanBulkLoadInput form)
    {
        try
        {
            initialize(ctx, form);

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Begin processing Bulkloading Request. File=" + form.getFilePath(), null).log(ctx);
            }

            final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);

            writeBulkloadLogHeader(ctx, form);
            
            int count = 0 ;

            //Iterate through all the CSV commands in the input file.
            for (String csvCommand = getCSVCommand(ctx); csvCommand != null; csvCommand = getCSVCommand(ctx))
            {
                if (null != cfg)
                {
                    if (count < cfg.getLinesSkippedInBulkLoad())
                    {
                        new DebugLogMsg(this, "Skipping the header section " + csvCommand, null).log(ctx);
                        count++;
                        continue;
                    }
                }
                Context subCtx = ctx.createSubContext();
                subCtx.put(BulkloadConstants.GENERIC_BEAN_BULKLOAD_CSV_COMMAND, csvCommand);
                subCtx.put(BulkloadConstants.GENERIC_BEAN_BULKLOAD_SESSION_ID, getSessionId());
                try
                {
                    pool_.execute(subCtx);
                }
                catch (Throwable e)
                {
                    new MinorLogMsg(this, "Error occurred while invoking pool. " + e.getMessage(), e);
                    logOMLogMsg(ctx, BulkloadConstants.OM_BULKLOAD_FAILURE);
                    logToErrorRecords(ctx, new String(csvCommand));
                    logBulkloadMessage(ctx, e.getMessage());
                }
            }
        }
        finally
        {
            closeFiles(ctx);
            shutdownBulkloader(ctx, form);
        }
    }
    

    /**
     * Close all input and log files.
     * @param ctx
     */
    public void closeFiles(Context ctx)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Closing the Bulkloading files.", null).log(ctx);
        }
        try
        {
            if (inputFile_ != null)
            {
                inputFile_.close();
            }
        }
        catch (IOException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Error occurred while closing the Bulkloading input file.", e).log(ctx);
            }
        }
    }
    
    
    /**
     * Return the next line from the Generic Bean Bulkloader's input file. 
     * Return Null if the end of the file has been reached, and a blank String if there were
     * errors reading in from the file.
     * @param ctx
     * @return
     */
    public String getCSVCommand(Context ctx)
    {
        try
        {
            return inputFile_.readLine();
        }
        catch (IOException e)
        {
            new MinorLogMsg(this, bulkloaderIdentifier_ + "Unable to read from the Generic Bulkload's Error input file.", e).log(ctx);
        }
        return "";
    }
    
    /**
     * Write an information header to the log file.
     * @param ctx
     * @param form
     */
    private void writeBulkloadLogHeader(Context ctx, GenericBeanBulkLoadInput form) 
    {
        logBulkloadMessage(ctx, "Using GenericBeanBulkloader config: " + getBulkloader().getBulkloaderConfig().toString());
        logBulkloadMessage(ctx, "Starting Bulkloading from file: " + form.getFilePath() + ". " + Calendar.getInstance().getTime().toString());
    }

    /**
     * Updates the progress counter with one more additional completion.
     * @param ctx
     */
    protected void updateProgress(Context ctx)
    {
        /* TODO: in Framework 6, the PPM.progress() method will return a boolean to indicate if the 
         * the Agent should continue with the task.  When we upgrade to Framework 6, we will have to 
         * create the shutdown hook for this task. */
        progress_.progress(ctx, incNumberOfCommandsExecuted(), totalNumberOfCSVCommands_);
    }
    
    /**
     * Teardown all multi-threaded bulkloader components
     * @param ctx
     * @param form
     */
    public void shutdownBulkloader(Context ctx, GenericBeanBulkLoadInput form) 
    {
        PMLogMsg pm = logPMLogMsg(ctx, BulkloadConstants.PM_BULKLOAD_PRODUCER_AGENT_SHUTDOWN);
        if (pool_ != null)
        {
            pool_.shutdownPoolAndWaitForTermination();
        }
        pm.log(ctx);
        
        String msg = "Completed reading from bulkload file: " + form.getFilePath() + ". " + Calendar.getInstance().getTime().toString();
        logBulkloadMessage(ctx, msg);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        
        shutdownLogger(ctx, BulkloadConstants.GENERIC_BULKLOADER_LOG_FILE_LOGGER);
        shutdownLogger(ctx, BulkloadConstants.GENERIC_BULKLOADER_ERROR_FILE_LOGGER);
        
        markTaskCompletion(ctx);
    }

    /**
     * Release the logger LifecycleAgentControl and remove them from LifecycleAgentControl.  This should flush the remaining messages in the logger
     * and mark the threads for garbage collection.
     * @param ctx
     */
    private void shutdownLogger(Context ctx, final String agentId)
    {
        PMLogMsg wPM = logPMLogMsg(ctx, BulkloadConstants.PM_LOGGER_RELEASE + " RELEASED " + agentId);
    
        try
        {
            PMLogMsg qPM = logPMLogMsg(ctx, BulkloadConstants.PM_LOGGER_RELEASE + " QUEUED FOR RELEASE " + agentId);
            LifecycleSupport.queue(ctx, agentId, LifecycleStateEnum.RELEASE);
            qPM.log(ctx);
            
            //This will poll every 3 seconds to see if the RELEASE has occurred.  Timeout is set to Long.MAX_VALUE.
            LifecycleSupport.wait(ctx, agentId, LifecycleStateEnum.RELEASE);  
        }
        catch (InterruptedException e)
        {
            new MinorLogMsg(this, "InterruptedException occurred while waiting for Bulkloader " + agentId + " Logger agent to RELEASE. ", e).log(ctx);
        }
        catch (LifecycleException e)
        {
            new MinorLogMsg(this, "LifecycleException occurred while waiting for Bulkloader " + agentId + " Logger agent to RELEASE.", e).log(ctx);
        }
        finally
        {
            wPM.log(ctx);
            PMLogMsg rPM = logPMLogMsg(ctx, BulkloadConstants.PM_LOGGER_RELEASE + " REMOVED " + agentId);

            try
            {
                LifecycleSupport.remove(ctx, agentId);
            }
            catch (LifecycleException e)
            {
                new MajorLogMsg(this, "Failed to remove the " + agentId + " Logger LifecycleAgentControl and during shutdown.", e).log(ctx);
            }
            finally
            {
                rPM.log(ctx);
            }
        }
    }

    /**
     * Mark task as complete.
     * @param ctx
     */
    private void markTaskCompletion(Context ctx)
    {
        if (progress_ != null)
        {
            progress_.log(ctx);
        }
    }
    
    protected void logToErrorRecords(Context ctx, String errorRecord)
    {
        try
        {
            BulkloaderLogMsg logMsg = new BulkloaderLogMsg(this, errorRecord, null);
            errorFileLogger_.log(logMsg);
        }
        catch (Throwable t)
        {
            //Can't write to log file
            new MinorLogMsg(this, bulkloaderIdentifier_ + "Unable to write the following record to Generic Bulkload's Error file: " + errorRecord,
                    t).log(ctx);
        }
    }

    protected void logBulkloadMessage(Context ctx, String msg) 
    {
        logBulkloadMessage(ctx, msg, null);
    }
    
    protected void logBulkloadMessage(Context ctx, String msg, BulkloadException e) 
    {
        try
        {
            BulkloaderLogMsg logMsg = new BulkloaderLogMsg(this, msg, e);
            logFileLogger_.log(logMsg);
        }
        catch (Throwable tt)
        {
            //Can't write to log file
            new MinorLogMsg(this, bulkloaderIdentifier_ + "Unable to write the following record to the Generic Bulkloader's log file: " + e.getMessage(),
                    tt).log(ctx);
        }
    }
    
    protected void logOMLogMsg(Context ctx, String om)
    {
        new OMLogMsg(BulkloadConstants.GENERIC_BULKLOADER_MODULE, bulkloaderIdentifier_ + om).log(ctx);
    }
    
    protected PMLogMsg logPMLogMsg(Context ctx, String pm)
    {
        return new PMLogMsg(BulkloadConstants.GENERIC_BULKLOADER_MODULE, bulkloaderIdentifier_ + pm);
    }

    /**
     * Return the current count of commands executed by the bulkloader
     * @return
     */
    protected synchronized int incNumberOfCommandsExecuted()
    {
        return ++numberOfCSVCommandsExecuted;
    }
    
    protected CSVParser getBulkloader()
    {
        return bulkloader_;
    }
    
    /**
     * Return a unique Identifier for the Bulkload command being processed.  
     * @return
     */
    private int getSessionId()
    {
        return sessionId_.addAndGet(1);
    }
    
    public static final String FAILED_RECORDS_FILE_SUFFIX = ".err";
    public static final String PROGRESS_LOG_FILE_SUFFIX = ".log";
    
    /**
     * Input Stream for file
     */
    private BufferedReader inputFile_;
    
    /**
     * The Bulkloader parser used by this Manager
     */
    private CSVParser bulkloader_;
    
    /**
     * Session identifier for every CSV Command processed
     */
    private AtomicInteger sessionId_ = new AtomicInteger(0);
    
    /**
     * Progress Monitor for the Bulkloader (per file)
     */
    private PPMLogMsg progress_;
    private int totalNumberOfCSVCommands_ = 0;
    private int numberOfCSVCommandsExecuted = 0;
    private String bulkloaderIdentifier_;
    
    /**
     * ThreadPool.  The CRM Design team wants to create and use one single CRM
     * ThreadPool, and to use Sub-Pooling.  The implementation will take too long
     * and cause a massive Regression testing.
     * So for now we continue to create individual pools and mark the refactoring 
     * as a TODO 
     */
    //ThreadPool threadPool_;
    private BulkloadProducerAgent pool_; 
    private ChannelLogger logFileLogger_;
    private ChannelLogger errorFileLogger_;
}