package com.trilogy.app.crm.bulkprovisioning.loader;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import java.util.concurrent.LinkedBlockingQueue;

import com.trilogy.app.crm.bulkloader.generic.BulkloadConstants;
import com.trilogy.app.crm.bulkloader.generic.BulkloaderErrorLogMsgFormat;
import com.trilogy.app.crm.bulkloader.generic.BulkloaderLogMsg;
import com.trilogy.app.crm.bulkloader.generic.BulkloaderLogMsgFormat;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkLoadInput;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkloaderRequestXInfo;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.DefaultCalendarSupport;
import com.trilogy.app.urcs.client.test.ProrateValueValidator;
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
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.logger.ChannelFileAdapterLogger;
import com.trilogy.framework.xlog.logger.ChannelLogger;
import com.trilogy.framework.xlog.logger.FileLogger;
import com.trilogy.framework.xlog.logger.FileLoggerConfig;
import com.trilogy.framework.xlog.logger.FileLoggerConfigHome;
import com.trilogy.framework.xlog.logger.FileLoggerConfigTransientHome;



public class BulkErrorLogWriter
{
    
    public void initialize(Context ctx, String errorDirectory, String fileName, String logKey)
    {
        
        Date curDate = new Date();
        
        String inputFileName = fileName + "_" + curDate.getYear() + curDate.getMonth() + curDate.getDay();
        
        
        // Create the Progress log writer
        errorLogWriter_ = configureFileLogger(ctx, errorDirectory, inputFileName, FAILED_RECORDS_FILE_SUFFIX,
                logKey,
                /*
                 * Because of multi-threading, wrap the BulkloaderLogMsgFormat by
                 * ThreadLocalContextFactory so that it's local variables aren't
                 * overwritten by competing threads. Frameworks formatters
                 * (XXLogMsgFormat) are ThreadLocal so nothing needs to be synchronized.
                 */
                new ThreadLocalContextFactory(ctx, new ContextFactory()
                {

                    public Object create(Context ctx1)
                    {
                        return new PRBTProvisioningLogMsgFormat();
                    }
                }));
        
        

    }
    
    /**
     * Release the logger LifecycleAgentControl and remove them from LifecycleAgentControl.  This should flush the remaining messages in the logger
     * and mark the threads for garbage collection.
     * @param ctx
     */
    public void shutdownLogger(Context ctx, final String agentId)
    {
    
        try
        {
            LifecycleSupport.queue(ctx, agentId, LifecycleStateEnum.RELEASE);
            
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

            try
            {
                LifecycleSupport.remove(ctx, agentId);
            }
            catch (LifecycleException e)
            {
                new MajorLogMsg(this, "Failed to remove the " + agentId + " Logger LifecycleAgentControl and during shutdown.", e).log(ctx);
            }
        }
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


    public void logError(Context ctx, String errorRecord)
    {
        try
        {
            BulkloaderLogMsg logMsg = new BulkloaderLogMsg(this, errorRecord, null);
            errorLogWriter_.log(logMsg);
        }
        catch (Throwable t)
        {
            // Can't write to log file
            new MinorLogMsg(this,  "Unable to write the following record to Network  Error file: " + errorRecord, t)
                    .log(ctx);
        }
    }
        

    
    private ChannelLogger errorLogWriter_;
    
    /**
     * PROVISIONING server Log File Logger Configuration key
     */
    public static final String PROVISIONING_SERVER_ERROR_LOG_FILE_LOGGER = "PRBTProvisioningServerErrorLogFileLogger";
    
    /**
     * Network element Error File Logger Configuration key
     */
    public static final String NETWORK_ELEMENT_ERROR_FILE_LOGGER = "PRBTNetworkElementErrorLogFileLogger";
    
    /**
     * TCB Error File Logger Configuration key
     */
    public static final String TCB_ERROR_FILE_LOGGER = "PRBT_RK_CRM_ErrorLogFileLogger";
    
    public static final String FAILED_RECORDS_FILE_SUFFIX = ".err";
    
    public final String PROVISIONING_SERVER_FILE_NAME = "ProvisioningServer";
    
    public final String NETWORK_ELEMENT_FILE_NAME = "NetworkElement";
    
}
