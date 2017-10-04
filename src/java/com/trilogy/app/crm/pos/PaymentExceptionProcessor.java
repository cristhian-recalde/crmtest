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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.tps.HummingbirdTPSInputStream;
import com.trilogy.app.crm.bas.tps.InvalidTPSRecordException;
import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * File writer for POS Payment Exception record.
 * Rename the files processed so that they won't be processed again.
 * 
 * Not delimited, fixed length reports are generated using:
 * 1) CSV Home with blank delimiter
 * 2) Customized GET BODY tags in the bean model file
 * 
 * @author Angie Li
 * 
 */
public class PaymentExceptionProcessor extends POSFileWriteUtils implements PointOfSaleFileWriter 
{
    /**
     * Initializes the processor with a log writer,
     * sets the start of the extraction date range.  
     * @param ctx
     */
    public PaymentExceptionProcessor(Context ctx, final Date currentDate, LifecycleAgentScheduledTask agent)
    {
        logWriter_ = new POSLogWriter(ctx, "PaymentExceptionProcessor", this);
        startOfDateRange_ = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).getDayBefore(currentDate));
        agent_ = agent;
    }

    public PaymentExceptionProcessor(Context ctx, final Date currentDate)
    {
        this(ctx, currentDate, null);
    }
    /** 
     * Finds all TPS Error files with suffix tpsFileExtension_,
     * process each file to extract Payment Exception records,
     * rename these files to a name with suffix processedFileExtension_,
     * write the POS Payment Exception report.
     */
    public void writeFile(Context ctx) throws HomeException 
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PROCESSOR_NAME, "writeFile");
        
        POSPaymentExceptionGzipCSVHome csvHome = getPOSPaymentExceptionCSVHome(ctx);
        
        try
        {
            ArrayList files = getTPSErrorFiles(ctx);
            if (files.size() == 0)
            {
                getLogger().writeToLog("No TPS Error files available to be processed.");
            }
            Iterator iter = files.iterator();

            while (iter.hasNext())
            {
            	if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()) && !LifecycleStateEnum.RUN.equals(agent_.getState()))
                {
                    String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running. Remaining accounts will be processed next time.";
                    new InfoLogMsg(this, msg, null).log(ctx);
                    throw new AbortVisitException(msg);
                }

                File tpsErrorFile = (File) iter.next();
                
                final PMLogMsg pmVisitLogMsg = new PMLogMsg(PROCESSOR_NAME, "visit");
                
                processTPSErrorFile(ctx, tpsErrorFile, csvHome);
                
                pmVisitLogMsg.log(ctx); 
                
                renameProcessedFile(tpsErrorFile);
            }
            try
            {
                // Write the contents of the entire home to the CSV file only once.
                csvHome.writeFile(ctx);
            }
            catch (HomeException e)
            {
                POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, e);
                getLogger().thrown(pe);
            }
        }
        catch(AgentException ae)
        {
            throw new HomeException("Could not retrieve TPS Error Files.",ae);
        }
        finally
        {
            pmLogMsg.log(ctx);
            getLogger().closeLogFileWriter();
        }
    }

    /**
     * Returns File to which we write the Exceptions Extraction.
     */
    public String getFileName(Context ctx) 
    {
        String filename = PointOfSale.POS_PAYMENT_EXCEPTION_FILE;
        PointOfSaleConfiguration config = (PointOfSaleConfiguration) ctx.get(PointOfSaleConfiguration.class);
        if (config != null 
                && config.getPaymentExceptionFileName().trim().length() > 0)
        {
            filename = config.getPaymentExceptionFileName().trim();
        }
        return filename;
    }
    
    /**
     * Formats the Date to MM/dd/yy hh:mm:ss
     * @param date
     * @return
     */
    public String formatDate(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss");
        return dateFormat.format(date);
    }
    
    /**
     * Initializes the POS Payment Exception file and creates a CSVHome that writes to it.
     * Returns the POSPaymentExceptionCSVHome.
     * 
     * @param ctx
     * @return
     * @throws HomeException
     */
    protected POSPaymentExceptionGzipCSVHome getPOSPaymentExceptionCSVHome(Context ctx) throws HomeException
    {
        String filePath = initFileWriter(ctx, this);
        /* Write to a GZIP compressed file.  Use POSPaymentExceptionCSVHome to save 
         * it as an uncompressed file. */
        /* Not delimited, fixed length reports are achieved by a customized CSVSupport used by the GzipHome */
        return new POSPaymentExceptionGzipCSVHome(ctx, filePath);
    }
    
    /**
     * Return a list of TPS Error Files to process. 
     * @param ctx
     * @return
     * @throws AgentException
     */
    private ArrayList getTPSErrorFiles(Context ctx) throws AgentException
    {
        ArrayList errorFiles = new ArrayList();
        TPSConfig config = (TPSConfig) ctx.get(TPSConfig.class);
        if (config == null)
        {
            throw new AgentException("Cannot find TPS configuration bean in context.");
        }
        File  tpsDirectory  = new File(config.getErrorDirectory());
        if (!tpsDirectory.exists() || !tpsDirectory.isDirectory())
        {
            // should send out alarm
            POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                    "Invalid TPS repository directory.", 
                    null);
            getLogger().thrown(pe);
            return errorFiles;
        }    
        File[]  tpsFiles  = tpsDirectory.listFiles();
        if (tpsFiles.length > 0)
        {
            for (int i = 0; i < tpsFiles.length; ++i)
            {
                /* Process only the files with suffix tpsFileExtension_ */
                if ( tpsFiles[i].isFile()
                        && validFile(tpsFiles[i], tpsFileExtension_)
                        && isLastModifiedDateInDateRange(tpsFiles[i]))
                {
                    errorFiles.add(tpsFiles[i]);
                }
            }
        } 
        else 
        {
            getLogger().writeToLog("No TPS files exist in the TPS error directory.");
        }
        return errorFiles;
    }

    /**
     * Reads the each line in the TPS Error File (excluding comments) and 
     * processes each line to get the TPS record that failed. We then create
     * a POSPaymentException record from this information.
     * @param ctx
     * @param tpsErrorFile
     * @param csvHome
     */
    private void processTPSErrorFile(Context ctx, File tpsErrorFile, Home csvHome)
    {
        HummingbirdTPSInputStream in = null; 
        int numberOfTPSRecords = 0;
        int numberOfFailedTPSRecords = 0;
        try
        {
            in = new HummingbirdTPSInputStream(ctx, new FileInputStream(tpsErrorFile));
            getLogger().writeToLog("TPS Error file=" + tpsErrorFile.getName() + ":");
            
            while (true)
            {
                try
                {
                    boolean created = false;
                    TPSRecord  tps  = in.readTps(ctx);
                    
                    //Log OMs
                    new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_PAYMENTEXCEPTION_RECORD_ATTEMPT).log(ctx);
                    numberOfTPSRecords++;
                    
                    POSPaymentException exception = (POSPaymentException) csvHome.find(ctx, tps.getTransactionNum());
                    if (exception == null)
                    {
                        exception = new POSPaymentException();
                        created = true;
                    }
                    exception = updatePaymentException(ctx, exception, tps);
                    
                    // Write record to file
                    try
                    {
                        if (created)
                        {
                            csvHome.create(ctx, exception);
                        }
                        else
                        {
                            csvHome.store(ctx, exception);
                        }
                        
                        //Log OMs
                        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_PAYMENTEXCEPTION_RECORD_SUCCESS).log(ctx);
                    }
                    catch (HomeException e)
                    {
                        POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                                "Failed to write payment exception record for TPS trans# " + tps.getTransactionNum(), 
                                e);
                        getLogger().thrown(pe);
                        numberOfFailedTPSRecords++;
                        
                        //Log OMs
                        new OMLogMsg(Common.OM_MODULE, PointOfSale.OM_POS_PAYMENTEXCEPTION_RECORD_FAILURE).log(ctx);
                    }
                    
                }
                catch (InvalidTPSRecordException te)
                {
                    POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                            "TPSRecord was malformed.", 
                            te);
                    getLogger().thrown(pe);
                    numberOfFailedTPSRecords++;
                }
            }
        }
        catch (EOFException eofe)
        {
            //do nothing at the end of the file. Continue processing other files.
        }
        catch (IOException ie)
        {
            POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                    "Error occured while processing file=" + tpsErrorFile.getName(), 
                    ie);
            getLogger().thrown(pe);
        }
        catch (Exception e)
        {
            POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                    "Error occured while reading file=" + tpsErrorFile.getName(), 
                    e);
            getLogger().thrown(pe);
        }
        finally 
        {
            if (in != null)
            {
            	try 
            	{
            		in.close();
            	} catch (Exception e)
            	{
            		new MinorLogMsg(this, "fail to close TPS error file", e).log(ctx); 
            	}
            }
            writeSummary(numberOfTPSRecords, numberOfFailedTPSRecords);
        }
    }
    
    /**
     * Updates the POS Payment Exception with the information from the TPS record
     * @param ctx
     * @param exception
     * @param tps
     * @return
     */
    private POSPaymentException updatePaymentException(Context ctx, POSPaymentException exception, TPSRecord tps)
    {
        exception.setPayNum(PointOfSale.trimLength(String.valueOf(tps.getTransactionNum()), PAY_NUM_MAX_LENGTH, true));
        exception.setDate(formatDate(tps.getPaymentDate()));
        exception.setAmount(POSReportSupport.formatAmount(ctx, tps.getAmount(), formatBAN(tps.getAccountNum()), getLogger(), AMOUNT_MAX_LENGTH));
        return exception;
    }
    
    /**
    * Returns TRUE if the file's Last Modified Date after the beginning of the date range 
    * passed in by the Cron Agent. 
    * @param file
    * @return
    */
    private boolean isLastModifiedDateInDateRange(File file)
    {
        return startOfDateRange_.getTime() <= file.lastModified();
    }
    
    /**
    * Returns TRUE if the file's extension matches EXTENSION
    * @param file
    * @param extension
    * @return
    */
    private boolean validFile(File file, String extension)
    {
        return file.getName().toLowerCase().endsWith(extension.toLowerCase()); 
    }
    
    /**
     * Rename the given filename to a name using the suffix processedFileExtension_. 
     * @param tpsErrorFile - file to be renamed
     */
    private void renameProcessedFile(File tpsErrorFile)
    {
        String oldName = tpsErrorFile.getAbsolutePath();
        String newName = oldName.trim() + processedFileExtension_;
        File renamedFile = new File(newName);
        boolean success = tpsErrorFile.renameTo(renamedFile);
        if (!success)
        {
            POSProcessorException pe = new POSProcessorException(PROCESSOR_NAME, 
                    "Failed to rename processed file=" + tpsErrorFile.getName() + " to " + renamedFile.getName());
            getLogger().thrown(pe);
        }
        else
        {
            getLogger().writeToLog("Renamed " + tpsErrorFile.getName() + " to " + renamedFile.getName() + ".");
        }
    }
    
    /**
     * Writes to the processor's logs the summary of processing this file.
     * @param numberOfTPSRecords - number of records processed
     * @param numberOfFailedTPSRecords - number of records successfully converted to POS Payment Exception
     */
    private void writeSummary(int numberOfTPSRecords, int numberOfFailedTPSRecords)
    {
        // Summary
        getLogger().writeToLog("  # of TPS error records processed=" + numberOfTPSRecords + ".");
        getLogger().writeToLog("  # of failed payment exception records=" + numberOfFailedTPSRecords + ".");
    }
    
    /**
     * Removes leading zero from ban.
     * @param ban
     * @return
     */
    private String formatBAN(String ban)
    {
        while (ban.charAt(0)=='0')
        {
            ban = ban.substring(1);
        }
        return ban;
    }
    
    private final LifecycleAgentScheduledTask agent_;
    private static final String PROCESSOR_NAME = PaymentExceptionProcessor.class.getName();
    /** The start of extraction date range passed in from the Cron task**/
    private Date startOfDateRange_;
    /** The Files with this extension will be processed for extraction of Payment Exception Records */
    private final String tpsFileExtension_ = ".err";
    /** The Files that are processed will be renamed to a file with this file extension */
    private final String processedFileExtension_ = ".posdone";
    private static int AMOUNT_MAX_LENGTH = 15; 
    private static final int PAY_NUM_MAX_LENGTH = 13;
}
