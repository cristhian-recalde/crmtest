/*
	TPSProvisioningAgent

	@Author : Larry Xia
	Date    : Oct, 21 2003
*/
 
package com.trilogy.app.crm.bas.tps;

import java.io.File;
import java.security.Principal;

import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * a Cron Agent which process TPS files
 * @author Larry Xia 
 */

// INSPECTED: 07/11/2003 ltse

public class TPSProvisioningAgent extends ContextAwareSupport
            implements CronContextAgent
{
	private final static String TPS_APPENDIX = "PMT"; 
	protected TPSConfig config; 
	protected static final int OPS_DONE=0;
	protected static final int OPS_ERROR=1; 
	public final static String TPS_ERROR_FILE_DIRECTORY= "TPS_ERROR_FILE_DIRECTORY";  
	
	
    public void stop()
    {
        // TODO Add stop code
    }

    public void execute(Context context) throws AgentException
    {
		Context ctx = context.createSubContext();
		ctx.put(Principal.class, null); 
         
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
			new MinorLogMsg(this,"Invalid TPS repository directory",null).log(ctx); 
			return; 
		}	
			
		File  doneDirectory  = new File(config.getDoneDirectory());
		if (!doneDirectory.exists())
		{
			new MinorLogMsg(this, "Invalid TPS archive file directory", null).log(ctx); 			
			
			if (!doneDirectory.mkdir()) {
				new MinorLogMsg(this, "Failed to create a default TPS archive directory", null).log(ctx); 
				return;
			}
			
			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this, "Create a default TPS archive directory " + 
					doneDirectory.getAbsolutePath(), null).log(ctx);
			}	
				
		}

		File  dumpDirectory  = new File(config.getErrorDirectory());
		if (!dumpDirectory.exists())
		{
			new MinorLogMsg(this, "Invalid TPS error file directory", null).log(ctx); 
			if (!dumpDirectory.mkdir()){
				new MinorLogMsg(this, "Failed to create a default TPS error directory" + 
							dumpDirectory.getAbsolutePath(), null).log(ctx);
				return;  	
			}
			
			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this, "Create a default TPS error directory " + 
						dumpDirectory.getAbsolutePath(), null).log(ctx);
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
							tpsFiles[i].renameTo( new File( rename( tpsFiles[i].getAbsolutePath(), OPS_ERROR)));   							
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
    		throw new AgentException("no TPS processor is defined"); 
    	
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

	public static String getTpsAppendix(Context ctx)
	{
		TPSConfig config=(TPSConfig) ctx.get(TPSConfig.class);
		if (config == null)
			return TPS_APPENDIX;
		else
			return config.getExtension();
		
	}
 }
