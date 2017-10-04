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
package com.trilogy.app.crm.bulkloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.txn.DefaultTransaction;
import com.trilogy.framework.xhome.txn.Transaction;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.app.crm.agent.BeanInstall;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AcctSubBulkLoad;
import com.trilogy.app.crm.bean.AcctSubBulkLoadWebControl;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.hlr.CrmHlrServiceImpl;
import com.trilogy.app.crm.home.BypassCreatePipelineHome;
import com.trilogy.app.crm.home.BypassValidationHome;


/**
 * This Request Servicer is used to bulk load accounts and
 * subscribers.  The tool will upload two csv files.  The first one will
 * be for accounts and the second will be for subscribers.
 *
 * The data in the CSV files needs to be valid and follow all validation rules.
 *
 * The data has to be in the Hummingbird approved format. Because of this, the
 * data is loaded in BulkLoadAccount and BulkLoadSubscriber formats, and then
 * copied into Account and Subscriber objects.
 *
 * @author jason.silva@redknee.com
 * @author gary.anderson@redknee.com
 * 
 * @author ameya.bhurke@redknee.com
 * @version TCB 9.7.2. - HB enhancement.
 *
 * ali:  Because more error logging support was needed, customized BulkLoadAccountCSVHome
 * and BulkLoadSubscriberCSVHome were used.
 *
 */
public class AcctSubBulkLoadRequestServicer implements RequestServicer, ContextAgent
{

    private static final String BUTTON_RUN = "Run";
    private static final Object lock = new Object();

    /**
     * {@inheritDoc}
     */
    public void service(
        final Context ctx,
        final HttpServletRequest req,
        final HttpServletResponse res)
        throws ServletException, IOException
    {
        final PrintWriter out = res.getWriter();
        final AcctSubBulkLoad form = new AcctSubBulkLoad();
        final Context subCtx = ctx.createSubContext();

        subCtx.put("MODE", OutputWebControl.EDIT_MODE);

        final ButtonRenderer buttonRenderer = (ButtonRenderer) ctx.get(ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        if (form.getDelimiter() == null || form.getDelimiter().trim().length() == 0)
        {
            out.println("<font color=\"red\">You have to have a delimiter</font><br/>");
        }
        else if (buttonRenderer.isButton(ctx, BUTTON_RUN))
        {
            FORM_WEBCONTROL.fromWeb(subCtx, form, req, "");
            
            if(form.isAsync())
            {
            	subCtx.put(PrintWriter.class, out);
            	subCtx.put(AcctSubBulkLoad.class, form);
            	subCtx.put(HttpServletRequest.class, req);
            	subCtx.put(HttpServletResponse.class, res);
            
            	try
            	{
            		new ThreadPool("AcctSubBulkLoad", 1, 1, this).execute(subCtx);
            	}
            	catch(AgentException t)
            	{
            		if(t.getCause() instanceof IOException)
            		{
            			throw (IOException) t.getCause();
            		}
            		else if( t.getCause() instanceof ServletException )
            		{
            			 throw (ServletException) t.getCause();
            		}
            		else
            		{
            			out.print(t.getMessage());
            		}
            	}
            }
            else
            {
            	loadData(subCtx, out, form, req, res);
            }
        }

        out.print("<form action=\"");
        out.print(req.getRequestURI());
        out.print("\" method=\"post\">");
        out.print("<input type=\"hidden\" name=\"cmd\" value=\"");
        out.print(req.getParameter("cmd"));
        out.print("\"/><table><tr><td>");

        FORM_WEBCONTROL.toWeb(subCtx, out, "", form);

        out.println("</td></tr><tr><th align=\"right\">");

        buttonRenderer.inputButton(out, ctx, this.getClass(), BUTTON_RUN, false);
        BeanWebController.outputHelpLink(ctx, null, null, out, buttonRenderer);

        out.println("</th></tr></table>");
        out.println("</form>");
    }

    
    /**
     *
     */
    private void loadData(
        Context ctx,
        final PrintWriter out,
        final AcctSubBulkLoad form,
        final HttpServletRequest req,
        final HttpServletResponse res)
        throws IOException
    {
        //File associations
        final char delimiter = form.getDelimiter().charAt(0);

        final File fLog = new File(form.getLogFilePath());
        if (!fLog.exists() || !fLog.isDirectory())
        {
            out.println(
                "<fond color=\"red\">Log path ["
                + fLog
                + "] doesn't exist or is not a directory. Cannot continue.</font><br/>");

            return;
        }

        ctx = createLocalContext(ctx, form.isDisableHLR());
        ctx.put(AcctSubBulkLoadRequestServicer.BULKLOAD_RUNNING, Boolean.TRUE);

        PrintWriter acctLogWriter = null;
        PrintWriter subLogWriter = null;
        PrintWriter acctErrWriter = null;
        PrintWriter subErrWriter = null;

        try
        {
            //Try to create these log files
            final File acctFile = new File(fLog, "accountUpload.log");
            acctFile.createNewFile();
            acctLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(acctFile, true)));
            acctErrWriter = new PrintWriter(new BufferedWriter(new FileWriter(form.getAccountFile() + ERROR_FILE_EXTENSION)));

            final File subFile = new File(fLog, "subUpload.log");
            subFile.createNewFile();
            subLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(subFile, true)));
            subErrWriter = new PrintWriter(new BufferedWriter(new FileWriter(form.getSubFile() + ERROR_FILE_EXTENSION)));

            acctLogWriter.print("\nAccount Load: " + new Date() + "\n\n");
            subLogWriter.print("\nSubscriber Load: " + new Date() + "\n\n");
            subLogWriter.print("\nHLR Disabled: " + form.isDisableHLR() + "\n\n");
        }
        catch (final IOException ioe)
        {
            //Error creating log files
            new InfoLogMsg(this, "IOException creating log files.", ioe).log(ctx);
        }
        
        FileChannel accountChannel = null;
        FileChannel subscriberChannel = null;
        FileLock accountLock = null;
        FileLock subscriberLock = null;
        int numAcctsProcess = 0;
        int numAcctsSuccess = 0;
        int numAcctsPartialSuccess = 0;
        int numSubsProcess = 0;
        int numSubsSuccess = 0;
        int numSubsPartialSuccess = 0;
        
        try 
        {
            synchronized (lock)
            {
                if (form.getAccountFile()!=null && form.getAccountFile().trim().length()>0)
                {
                    accountChannel = createFileChannel(form.getAccountFile());
                    accountLock = accountChannel.lock();
                }
                if (form.getSubFile()!=null && form.getSubFile().trim().length()>0)
                {
                    subscriberChannel = createFileChannel(form.getSubFile());
                    subscriberLock = subscriberChannel.lock();
                }
            }

            {
                final PMLogMsg pm = new PMLogMsg(PM_MODULE, "Create all accounts", "");
                final ProcessingFeedback feedback =
                    createAllAccounts(
                        ctx,
                        acctLogWriter,
                        acctErrWriter,
                        form.getAccountFile(),
                        accountChannel,
                        delimiter, form);
        
                numAcctsProcess = feedback.getProcessingCount();
                numAcctsSuccess = feedback.getSuccessCount();
                numAcctsPartialSuccess = feedback.getPartialSuccessCount();
                pm.log(ctx);
            }
    
            {
                final PMLogMsg pm = new PMLogMsg(PM_MODULE, "Create all subscribers", "");
                ctx.put(BypassValidationHome.FLAG, !form.isValidate());
                ctx.put(BypassCreatePipelineHome.BYPASS_SUBSCRIBER_PIPELINE, !form.isUpload());
                final ProcessingFeedback feedback =
                    createAllSubscribers(
                        ctx,
                        subLogWriter,
                        subErrWriter,
                        form.getSubFile(),
                        subscriberChannel,
                        delimiter, form);
    
                numSubsProcess = feedback.getProcessingCount();
                numSubsSuccess = feedback.getSuccessCount();
                numSubsPartialSuccess = feedback.getPartialSuccessCount();
                pm.log(ctx);
            }
        } 
        catch (OverlappingFileLockException e) 
        {
            try
            {
                acctLogWriter.println("Either the account or the subscriber bulk load file is already in use. Aborting bulk loading.\n");
            }
            catch (Throwable throwable)
            {
                //Error writing results to file
                new InfoLogMsg(this, "Unable to write to transaction count to log file.", throwable).log(ctx);
            }
            final PrintWriter responseOut = res.getWriter();
            responseOut.print("<H1>Either the account or the subscriber bulk load file is already in use. Aborting bulk loading.</H1>\n");
            responseOut.print("<BR><P>");
        } 
        finally
        {
            releaseLocks(accountLock, subscriberLock, accountChannel, subscriberChannel);
        }

        //Output the final stats to the log files and close the files used
        //Account
        try
        {
            acctLogWriter.println("*********************RESULTS***************************");
            acctLogWriter.println("Number of Accounts Processed: " + numAcctsProcess);
            acctLogWriter.println("Successful Transactions: " + numAcctsSuccess);
            acctLogWriter.println("Partial Successful Transactions: " + numAcctsPartialSuccess);
            acctLogWriter.println("Failed Transactions: " + (numAcctsProcess - numAcctsSuccess - numAcctsPartialSuccess));
            acctLogWriter.println("*********************RESULTS***************************");

            //Subscriber
            subLogWriter.println("*********************RESULTS***************************");
            subLogWriter.println("Number of Subscribers Processed: " + numSubsProcess);
            subLogWriter.println("Successful Transactions: " + numSubsSuccess);
            subLogWriter.println("Partial Successful Transactions: " + numSubsPartialSuccess);
            subLogWriter.println("Failed Transactions: " + (numSubsProcess - numSubsSuccess - numSubsPartialSuccess));
            subLogWriter.println("*********************RESULTS***************************");
        }
        catch (Throwable throwable)
        {
            //Error writing results to file
            new InfoLogMsg(this, "Unable to write to transaction count to log file.", throwable).log(ctx);
        }

        acctLogWriter.flush();
        acctLogWriter.close();
        subLogWriter.flush();
        subLogWriter.close();

        acctErrWriter.close();
        subErrWriter.close();

        //Output the results to the screen
        outputResults(numAcctsProcess, numAcctsSuccess, numAcctsPartialSuccess, numSubsProcess, numSubsSuccess, numSubsPartialSuccess, req, res);
    }
    
    private FileChannel createFileChannel(final String fileName) throws IOException, FileNotFoundException
    {
        File file = new File(fileName);
        file.createNewFile();
        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
        return fileChannel;
        
    }

    private void releaseLocks(FileLock accountLock, FileLock subscriberLock, FileChannel accountChannel, FileChannel subscriberChannel)
    {
        synchronized (lock)
        {
            if (accountLock!=null)
            {
                try
                {
                    accountLock.release();
                }
                catch (Exception ignored)
                {
                    
                }
            }
            if (subscriberLock!=null)
            {
                try
                {
                    subscriberLock.release();
                }
                catch (Exception ignored)
                {
                    
                }
            }
            if (accountChannel!=null)
            {
                try
                {
                    accountChannel.close();
                }
                catch (Exception ignored)
                {
                    
                }
            }
            if (subscriberChannel!=null)
            {
                try
                {
                    subscriberChannel.close();
                }
                catch (Exception ignored)
                {
                    
                }
            }
        }        
    }

    /**
     * @param form TODO
     *
     */
    private ProcessingFeedback createAllAccounts(
        final Context ctx,
        final PrintWriter acctLogWriter,
        final PrintWriter acctErrWriter,
        final String acctFileStr,
        final FileChannel accountChannel,
        final char delimiter, AcctSubBulkLoad form)
    {
        final ProcessingFeedback feedback = new ProcessingFeedback();

        final PMLogMsg createAllAccountsPM = new PMLogMsg(PM_MODULE, "Create All Accounts");
        final CSVErrorFileExceptionListener listener = new CSVErrorFileExceptionListener(acctErrWriter, acctLogWriter);
        try
        {
            ctx.put(ExceptionListener.class, listener);

            //Create the Account CSV Home
            final PMLogMsg loadAccountsCSVPM = new PMLogMsg(PM_MODULE, "Load All Accounts from CSV");
            /* com.redknee.app.crm.customer.hummingbird.web.service.BulkLoadAccountCSVHome is
             * a customized GenericCSVHome that allows more flexible error logging for the
             * parsing process. */
            Home acsv = new CustomizedBulkLoadAccountCSVHome(ctx, acctFileStr, accountChannel, delimiter);
            acsv = new ContextualizingHome(ctx, acsv);

            loadAccountsCSVPM.log(ctx);

            ThreadPoolVisitor v = null;
            try
            {
                final Account template = (Account) ctx.get(BeanInstall.BULK_ACCOUNT_TEMPLATE);
                final AccountCreateAgent agent =
                    new AccountCreateAgent(ctx,
                        (Home)ctx.get(AccountHome.class),
                        template,
                        acctLogWriter,
                        acctErrWriter);
                
                //CommittingAgent committer = new CommittingAgent(agent);
                
                //ThreadPoolVisitor v = new ThreadPoolVisitor(form.getThreads(), form.getQueue(), agent);
                v = new ThreadPoolVisitor(form.getThreads(), form.getQueue(), agent);


                acsv.forEach(ctx, v);
                try
                {
                	v.awaitCompletion();
                }
                catch(InterruptedException iex)
                {
                	new MinorLogMsg(this, "Exception when while trying to wait for the createAllAccounts task to complete", iex).log(ctx);
                }
                /*
                finally
                {
                	v.shutdown();
                	LogSupport.debug(ctx, this, "MIGRATION >>> Shutting down threadpool for account bulkload!");
                }
                */
                feedback.setProcessingCount(agent.getNumberOfProcessedAccounts());
                feedback.setSuccessCount(agent.getNumberOfSuccessfullyProcessedAccounts());
                feedback.setPartialSuccessCount(0);
            }
            catch (final Exception e)
            {
                //An error occured during the selectAll of the CSVHome
                final String msg = "Failure reading the AccountCSVHome: " + e.getMessage() + "\n";
                try
                {
                    acctLogWriter.print(msg);
                    e.printStackTrace(acctLogWriter);
                    acctLogWriter.println();
                }
                catch (Throwable tt)
                {
                    //Can't write to log file
                    new InfoLogMsg(this, "Unable to write to Account log file: " + msg, tt).log(ctx);
                }
            }
            finally
            {
            	// Ensure the visitor has been initialized
            	if(v != null)
            	{
	            	LogSupport.debug(ctx, this, "MIGRATION >>> Shutting down threadpool ["+v.getActiveThreadCount()+"] for account bulkload...");
	            	v.shutdown();
	            	LogSupport.debug(ctx, this, "MIGRATION >>> Shut down threadpool ["+v.getActiveThreadCount()+"] for account bulkload!");
	            	v = null;
            	}
            }              
        }
        catch (Exception e)
        {
            //An error occured during the creation of the AccountCSVHome
            final String msg = "Failure creating the AccountCSVHome: " + e.getMessage() + "\n";
            try
            {
                acctLogWriter.print(msg);
                e.printStackTrace(acctLogWriter);
                acctLogWriter.println();
            }
            catch (Throwable tt)
            {
                //Can't write to log file
                new InfoLogMsg(this, "Unable to write to Account log file: " + msg, tt).log(ctx);
            }
        }
        finally
        {
            createAllAccountsPM.log(ctx);
        }

        return feedback;
    }


    /**
     * @param form TODO
     *
     */
    private ProcessingFeedback createAllSubscribers(
        final Context ctx,
        final PrintWriter subLogWriter,
        final PrintWriter subErrWriter,
        final String subFileStr,
        final FileChannel subscriberChannel,
        final char delimiter, AcctSubBulkLoad form)
    {
        final ProcessingFeedback feedback = new ProcessingFeedback();

        final PMLogMsg createAllSubscribersPM = new PMLogMsg(PM_MODULE, "Create All Subscribers");
        final CSVErrorFileExceptionListener listener = new CSVErrorFileExceptionListener(subErrWriter, subLogWriter);

        try
        {
            ctx.put(ExceptionListener.class, listener);

            final PMLogMsg loadSubscribersCSVPM =
                new PMLogMsg(PM_MODULE, "Load All Subscribers from CSV");

            /* com.redknee.app.crm.customer.hummingbird.web.service.BulkLoadSubscriberCSVHome is
             * a customized GenericCSVHome that allows more flexible error logging for the
             * parsing process. */
            Home scsv = new CustomizedBulkLoadSubscriberCSVHome(ctx, subFileStr, subscriberChannel, delimiter);
            scsv = new ContextualizingHome(ctx, scsv);

            loadSubscribersCSVPM.log(ctx);

            Context subCtx = ctx.createSubContext();
            
            //CommittingAgent committer = null;
            ThreadPoolVisitor threadPoolVisitor = null;
            
            try
            {
                final SubscriberCreateAgent agent =
                    new SubscriberCreateAgent(subCtx,
                        (Home)ctx.get(SubscriberHome.class),
                        (Subscriber)ctx.get(BeanInstall.BULK_SUBSCRIBER_TEMPLATE),
                        subLogWriter,
                        subErrWriter);
                
                //committer = new CommittingAgent(ctx,agent,form.getThreads(), 200);
                
                threadPoolVisitor = new ThreadPoolVisitor(form.getThreads(), form.getQueue(), agent);
                
                BulkLoadCachingSupport.autoCacheHomes(subCtx);
                

                scsv.forEach(subCtx, threadPoolVisitor);
                try
                {
                	threadPoolVisitor.awaitCompletion();
                }
                catch(InterruptedException iex)
                {
                	new MinorLogMsg(this, "Exception when while trying to wait for createAllSubscribers task to complete", iex).log(ctx);
                }
                /*
                finally
                {
                	threadPoolVisitor.shutdown();
                	LogSupport.debug(ctx, this, "MIGRATION >>> Shutting down threadpool for subscriber bulkload!");
                }
                */

                feedback.setProcessingCount(agent.getNumberOfProcessedSubscribers());
                feedback.setSuccessCount(agent.getNumberOfSuccessfullyProcessedSubscribers());
                feedback.setPartialSuccessCount(agent.getNumberOfPartialSuccessfullyProcessedSubscribers());
            }
            catch (final Exception e)
            {
                //An error occured during the selectAll of the SubscriberCSVHome
                final String msg = "Failure reading the SubCSVHome-" + e.getMessage() + "\n";
                try
                {
                    subLogWriter.print(msg);
                    e.printStackTrace(subLogWriter);
                    subLogWriter.println();
                }
                catch (Throwable tt)
                {
                    //Can't write to log file
                    new InfoLogMsg(this, "Unable to write to Subscriber log file: " + msg, tt).log(ctx);
                }
            }
            finally
            {
            	// Ensure the visitor has been initialized
            	if(threadPoolVisitor != null)
            	{
	            	LogSupport.debug(ctx, this, "MIGRATION >>> Shutting down threadpool ["+threadPoolVisitor.getActiveThreadCount()+"] for subscriber bulkload...");
	            	threadPoolVisitor.shutdown();
	            	LogSupport.debug(ctx, this, "MIGRATION >>> Shut down threadpool ["+threadPoolVisitor.getActiveThreadCount()+"] for subscriber bulkload!");
	            	threadPoolVisitor = null;
            	}
            }
            /*
            
            ###############################################################
            The following code is commented on purpose as this is not to
            be delivered yet and is not fully tested. The author chooses to
            preserve a copy of this code in the SVN.
            ###############################################################
            
            finally
            {
            	if(threadPoolVisitor != null)
            	{
            		threadPoolVisitor.shutdown();
                	if(committer != null)
                	{
                		// active thread count = 0 ensure that all thread have finished processing.
                		while (threadPoolVisitor.getActiveThreadCount() != 0)
                		{
                			Thread.sleep(1000);
                		}
                		
                		committer.flush();
                	}
            	}
            }
            */
        }
        catch (final Exception e)
        {
            //An error occured during the creation of the SubscriberCSVHome
            final String msg = "Failure creating the SubCSVHome- " + e.getMessage() + "\n";
            try
            {
                subLogWriter.print(msg);
                e.printStackTrace(subLogWriter);
                subLogWriter.println();
            }
            catch (Throwable tt)
            {
                //Can't write to log file
                new InfoLogMsg(this, "Unable to write to Subscriber log file: " + msg, tt).log(ctx);
            }
        }
        finally
        {
            createAllSubscribersPM.log(ctx);
        }

        return feedback;
    }

    
    private Transaction setAutoCommitOff(Context ctx) 
    {
    	Transaction txn = new DefaultTransaction(ctx);
    	ctx.put(Transaction.class, txn);
    	
    	return txn;
    }
    

    /**
     *
     */
    private ProcessingFeedback updateAllAccounts(
        final Context ctx,
        final Map stateMap,
        final PrintWriter acctLogWriter)
    {
        final ProcessingFeedback feedback = new ProcessingFeedback();

        final PMLogMsg updateAllAccountsPM = new PMLogMsg(PM_MODULE, "Update All Accounts");
        try
        {
            final Home ah = (Home) ctx.get(AccountHome.class);

            for (final Iterator i = stateMap.values().iterator(); i.hasNext();)
            {
                final PMLogMsg updateAccountPM = new PMLogMsg(PM_MODULE, "Update Account");
                final Account a = (Account) i.next();
                try
                {
                    //Get the existing account
                    final Account existingA = (Account) ah.find(ctx, a);

                    if (existingA.getState() != a.getState())
                    {
                        //update its state
                        existingA.setState(a.getState());
                        //Store the updated existing account
                        //in case any changes were made to the
                        //account when subs were added
                        ah.store(ctx, existingA);
                    }
                }
                catch (final Exception e)
                {
                    //Error updating the Account State to the old value
                    final String msg =
                        a.getBAN() + "-Failure updating Account State-" + e.getMessage() + "\n";

                    try
                    {
                        acctLogWriter.print(msg);
                        e.printStackTrace(acctLogWriter);
                    }
                    catch (final Throwable tt)
                    {
                        //Can't write to log file
                        new InfoLogMsg(this, "Unable to write to Account log file: " + msg, tt).log(ctx);
                    }
                }
                finally
                {
                    updateAccountPM.log(ctx);
                }
            }
        }
        finally
        {
            updateAllAccountsPM.log(ctx);
        }

        return feedback;
    }


    /**
     * Adds in the context fake HLR services to disable allHLR calls
     *
     * @param ctx
     * @return
     */
    private Context disableHLR(Context ctx)
    {
        ctx = ctx.createSubContext();
        ctx.setName("nullHLRClient");

        ctx.put(CrmHlrServiceImpl.HLR_SKIPPED, CrmHlrServiceImpl.HLR_SKIPPED);
        
        return ctx;
    }

    /**
     * Creates a local context for the processing of an individual ER.  In this
     * context, the Subscriber and Account home are being cached to improve
     * efficiency in the repeated look-up of the same Subscriber and Account.
     * The disabling of HLR is performed, if necessary.  The Account reservation
     * (for loading individual Accounts) map is initialized.
     *
     * @param parentContext The original operating environment.
     * @param disableHLR True if HLR should be disabled during loading; false
     * otherwise;
     *
     * @return An operating environment.
     */
    private Context createLocalContext(
        final Context parentContext,
        final boolean disableHLR)
    {
        final Context context;
        
        if (disableHLR)
        {
            context = disableHLR(parentContext);
        }
        else
        {
            context = parentContext.createSubContext();
        }

        // The Account home.
        {
            Home home = (Home) context.get(AccountHome.class);
            // removed the LRUCachingHome because it's next to impossible to make it work with AccountExtensions (pool)
            home = new ContextualizingHome(context, home);
            home = new PMHome(home, context, "AcctSubBulkLoadRequestServicer.AccountHome");

            context.put(AccountHome.class, home);
        }

        // The Subscriber home.
        {
            Home home = (Home) context.get(SubscriberHome.class);
            home = new ContextualizingHome(context, home);
            home = new PMHome(home, context, "AcctSubBulkLoadRequestServicer.SubscriberHome");

            context.put(SubscriberHome.class, home);
        }


        return context;
    }

    /**
     * This method is used to output a result summary to the web
     */
    private void outputResults(final int numAcctsProcess, final int numAcctsSuccess, final int numAcctsPartialSuccess, final int numSubsProcess,
            final int numSubsSuccess, final int numSubsPartialSuccess, final HttpServletRequest req, final HttpServletResponse res) throws IOException
    {

        final PrintWriter out = res.getWriter();
        out.print("<H1>Account and Subscriber Bulk Load</H1>\n");
        out.print("<BR><P>");
        out.print("ACCOUNT RESULTS\n");
        out.print("<BR>");
        out.print("Number of Accounts Processed: " + numAcctsProcess + "\n");
        out.print("<BR>");
        out.print("Successful Transactions: " + numAcctsSuccess + "\n");
        out.print("<BR>");
        out.print("Partial Successful Transactions: " + numAcctsPartialSuccess + "\n");
        out.print("<BR>");
        out.print("Failed Transactions: " + (numAcctsProcess - numAcctsSuccess - numAcctsPartialSuccess) + "\n");
        out.print("<BR>");

        //Subscriber
        out.print("<BR>");
        out.print("SUBSCRIBER RESULTS\n");
        out.print("<BR>");
        out.print("Number of Subscribers Processed: " + numSubsProcess + "\n");
        out.print("<BR>");
        out.print("Successful Transactions: " + numSubsSuccess + "\n");
        out.print("<BR>");
        out.print("Partial Successful Transactions: " + numSubsPartialSuccess + "\n");
        out.print("<BR>");
        out.print("Failed Transactions: " + (numSubsProcess - numSubsSuccess - numSubsPartialSuccess) + "\n");
        out.print("<BR>");
        out.print("Please see the log output for details\n");
        out.print("<BR>");
        out.print("</P>");
    }

    public static final String BULKLOAD_RUNNING = "AccountSubscriberBulkloadRunning";

    private static final WebControl FORM_WEBCONTROL = new AcctSubBulkLoadWebControl();

    private static final String PM_MODULE   = AcctSubBulkLoadRequestServicer.class.getName();

    /**
     * Extension used for error files.
     */
    private static final String ERROR_FILE_EXTENSION = ".err";

    /**
     * The size of cache to use for the subscriber and account homes.
     */
    private static final int CACHE_SIZE = 3;


    /**
     * Provides a feedback mechanism for the Account and Subscriber visitors.
     */
    private static final
    class ProcessingFeedback
    {
        /**
         * Gets the number of items processed.
         *
         * @return The number of items processed.
         */
        public int getProcessingCount()
        {
            return processingCount_;
        }

        /**
         * Sets the number of items processed.
         *
         * @param processingCount The number of items processed.
         */
        public void setProcessingCount(final int processingCount)
        {
            processingCount_ = processingCount;
        }

        /**
         * Gets the number of successful processings.
         *
         * @return The number of successful processings.
         */
        public int getSuccessCount()
        {
            return successCount_;
        }

        /**
         * Sets the number of partial successful processings.
         *
         * @param partialSuccessCount The number of successful processings.
         */
        public void setPartialSuccessCount(final int partialSuccessCount)
        {
            partialSuccessCount_ = partialSuccessCount;
        }

        /**
         * Gets the number of partial successful processings.
         *
         * @return The number of partial successful processings.
         */
        public int getPartialSuccessCount()
        {
            return partialSuccessCount_;
        }

        /**
         * Sets the number of successful processings.
         *
         * @param successCount The number of successful processings.
         */
        public void setSuccessCount(final int successCount)
        {
            successCount_ = successCount;
        }

        /**
         * The number of items processed.
         */
        private int processingCount_;

        /**
         * The number of successful processings.
         */
        private int successCount_;

        /**
         * The number of successful processings.
         */
        private int partialSuccessCount_;


		public static final String PM_MODULE = AcctSubBulkLoadRequestServicer.class.getName();
}


	@Override
	public void execute(Context ctx) throws AgentException 
	{
		PrintWriter pw = (PrintWriter)ctx.get(PrintWriter.class);
		AcctSubBulkLoad form = (AcctSubBulkLoad)ctx.get(AcctSubBulkLoad.class);
		HttpServletRequest req = (HttpServletRequest)ctx.get(HttpServletRequest.class);
		HttpServletResponse res = (HttpServletResponse)ctx.get(HttpServletResponse.class);
		try
		{
			loadData(ctx, pw, form, req, res);
		}
		catch(Exception e)
		{
			throw new AgentException(e);
		}
	}
}