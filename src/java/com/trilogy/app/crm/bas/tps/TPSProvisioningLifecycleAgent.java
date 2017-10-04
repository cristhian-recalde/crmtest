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
package com.trilogy.app.crm.bas.tps;

import java.io.File;

import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.RunnableLifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.log.ERLogger;


/**
 * @author jke
 */
public class TPSProvisioningLifecycleAgent extends RunnableLifecycleAgentSupport
{
    public TPSProvisioningLifecycleAgent(final Context context)
    {
        super(context);
        shouldContinueRunning_ = false;
    }
    
    @Override
    public void doRun(final Context context)
    	throws LifecycleException
	{
	    setShouldContinueRunning(true);

	    while (shouldContinueRunning())
	    {
	        try
	        {
	            process(context);
	            synchronized (runLock_)
	            {
	                runLock_.wait(getSleepTime(context));
	            }
	        }
	        catch (final Throwable exception)
	        {
	            new InfoLogMsg(this, "doRun()", exception).log(context);
	        }
	    }
	}
    
    @Override
    public void doStop(final Context context)
    	throws LifecycleException
	{
        synchronized (runLock_)
        {
            setShouldContinueRunning(false);
            runLock_.notifyAll();
        }
	}
    
    public void process(Context ctx) throws AgentException
    {
		ctx = ctx.createSubContext();
		
		// This is so that it doesn't take up the calling Principal's Permissions
		// when called via the "Run Now" Action in the GUI
		ctx.put(java.security.Principal.class, null);
          
        config=(TPSConfig) ctx.get(TPSConfig.class);
        if(config==null)
        {
            throw new CronContextAgentException("Cannot find TPS configuration bean in context.");
        }
  
       TPSProcessor processor = null;
		if (LogSupport.isDebugEnabled(ctx))
		{
 			new DebugLogMsg(this, "TPS batch processing begins", null).log(ctx);
		} 
		
		
		File  tpsDirectory  = new File(config.getTPSDirectory());
		if (!tpsDirectory.exists() || !tpsDirectory.isDirectory())
		{
			// should send out alarm
			new MinorLogMsg(this, "Invalid TPS repository directory ["
			        + tpsDirectory.getAbsolutePath() + "]", null).log(ctx);
			return; 
		}	
			
		File  doneDirectory  = new File(config.getDoneDirectory());
		if (!doneDirectory.exists())
		{
			new MinorLogMsg(this, "Invalid TPS archive file directory ["
			        + doneDirectory.getAbsolutePath() + "]", null).log(ctx);
			
			if (!doneDirectory.mkdir()) {
				new MinorLogMsg(this, "Failed to create a default TPS archive directory ["
				        + doneDirectory.getAbsolutePath() + "]", null).log(ctx); 
				return;
			}
			
			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this, "Created a default TPS archive directory ["
				        + doneDirectory.getAbsolutePath() + "]", null).log(ctx);
			}	
				
		}

		File  dumpDirectory  = new File(config.getErrorDirectory());
		if (!dumpDirectory.exists())
		{
			new MinorLogMsg(this, "Invalid TPS error file directory ["
			        + config.getErrorDirectory() + "]", null).log(ctx); 
			if (!dumpDirectory.mkdir()){
				new MinorLogMsg(this, "Failed to create a default TPS error directory ["
				        + dumpDirectory.getAbsolutePath() + "]", null).log(ctx);
				return;
			}
			
			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this, "Create a default TPS error directory [" + 
						dumpDirectory.getAbsolutePath() + "]", null).log(ctx);
			}
			ctx.put(TPS_ERROR_FILE_DIRECTORY, dumpDirectory.getAbsolutePath()); 
		}
 
 		File[]  tpsFiles  = tpsDirectory.listFiles();	

		if (tpsFiles.length > 0)
		{
			for (int i = 0; i < tpsFiles.length; ++i)
			  {
			  	if ( tpsFiles[i].isFile() ){ 				  	
					 if ( this.validFile( tpsFiles[i], config.getExtension())) {
						if (LogSupport.isDebugEnabled(ctx))
						{
							new DebugLogMsg(this, "Processing file : " + tpsFiles[i].getName(), null).log(ctx);
						}
						processor = getProcessor(ctx, config, tpsFiles[i]);
						
 						if (processor.processFile() ) {
  							tpsFiles[i].renameTo( new File( rename( tpsFiles[i].getAbsolutePath(), OPS_DONE)));   							
 						} else { // invalid header 
							ERLogger.genInvalidFileER( ctx, tpsFiles[i].getName() );
							new EntryLogMsg(10531, this, "","", new String[]{tpsFiles[i].getName()}, null).log(ctx);
							if(tpsFiles[i].exists())	{
								tpsFiles[i].renameTo( new File( rename( tpsFiles[i].getAbsolutePath(), OPS_ERROR)));   													
							}
	 					}
 					} else { // invalid file name
						ERLogger.genInvalidFileER( ctx, tpsFiles[i].getName() );
						new EntryLogMsg(10531, this, "","", new String[]{tpsFiles[i].getName()}, null).log(ctx);
				 		if(tpsFiles[i].exists())
                        {
                            tpsFiles[i].renameTo( new File( rename( tpsFiles[i].getAbsolutePath(), OPS_ERROR)));
                        }   							
				 	}
				} else {
					// if directory then ignore.
				}
             } 
                  	
		   } else {
				if (LogSupport.isDebugEnabled(ctx))
				{
					new DebugLogMsg(this, "No TPS file is received", null).log(ctx);
				} 
		   }
		   if (LogSupport.isDebugEnabled(ctx))
		   {
		   		new DebugLogMsg(this, "TPS files processing ends", null).log(ctx);
		   }
              
     }


    public TPSProcessor getProcessor( Context ctx, TPSConfig cfg, File file) throws AgentException{
    	if ( cfg.getTPSProcessor() == null)
        {
            throw new AgentException("no TPS processor is defined");
        } 
    	
    	TPSProcessor processor = null; 
    	
    	try {
    		 processor =(TPSProcessor) Class.forName( cfg.getTPSProcessor()).newInstance() ;
    		 processor.init( ctx, file); 
     	}catch ( Exception e){
    		throw new AgentException("Fail to load TPS processor, " + e.getMessage() );
    	}
    	
    	return processor; 
    }

	public boolean validFile(File file, String extension){
		
 		return file.getName().toLowerCase().endsWith(extension.toLowerCase()); 
		
	}

	public String rename(String old, int type)
	{
	 	switch ( type ) {
	 		case OPS_DONE: 
				return config.getDoneDirectory().concat(old.substring(old.lastIndexOf(File.separator)));
			case OPS_ERROR:
				return config.getErrorDirectory().concat(old.substring(old.lastIndexOf(File.separator)));
	 	}
		return old; 	 		

	}

    
    /**
     * Sets whether or not the processing thread should be running.
     *
     * @param running True if the processing thread should be running; false
     * otherwise.
     */
    private synchronized void setShouldContinueRunning(final boolean running)
    {
        shouldContinueRunning_ = running;
    }


    /**
     * Indicates whether or not the processing thread should be running.
     *
     * @return True if the processing thread should be running; false
     * otherwise.
     */
    private synchronized boolean shouldContinueRunning()
    {
        return shouldContinueRunning_;
    }

    
    /**
     * Gets the time to sleep in milliseconds.
     *
     * @return The time to sleep in milliseconds.
     */
    private long getSleepTime(Context ctx)
    {
        return ctx.getLong(TPS_SLEEP_TIME_CTX_KEY, DEFAULT_SLEEP_TIME);
    }
    
    
    private boolean shouldContinueRunning_;

    /**
     * Used to identify this class's PMs.
     */
    private static final String PM_MODULE = TPSProvisioningLifecycleAgent.class.getName();

    /**
     * The default time to sleep between cycles in milliseconds.
     */
    private static final String TPS_SLEEP_TIME_CTX_KEY = "TPS.SleepTime";
    private static final long DEFAULT_SLEEP_TIME = 15000;
    
    private final Object runLock_ = new Object();

	public final static String TPS_APPENDIX = "PMT"; 
	protected TPSConfig config; 
	protected static final int OPS_DONE=0;
	protected static final int OPS_ERROR=1; 
	public final static String TPS_ERROR_FILE_DIRECTORY= "TPS_ERROR_FILE_DIRECTORY";  


}
