/*
	TPSProcessor

	@Author : Larry Xia
	Date    : Oct, 21 2003
*/
 
package com.trilogy.app.crm.bas.tps;

// java
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.tps.pipe.ConveragedAccountSubscriberLookupAgent;
import com.trilogy.app.crm.bas.tps.pipe.ConvertTpsToTransactionAgent;
import com.trilogy.app.crm.bas.tps.pipe.IBISPaymentTypeCheckingAgent;
import com.trilogy.app.crm.bas.tps.pipe.IBISPaymentTypeMappingAgent;
import com.trilogy.app.crm.bas.tps.pipe.IbisDuplicationCheckingAgent;
import com.trilogy.app.crm.bas.tps.pipe.Pipeline;
import com.trilogy.app.crm.bas.tps.pipe.VoidFieldCheckingAgent;
import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

// INSPECTED: 07/11/2003 ltse
/**
 * TPS file processor, process a TPS file
 * @author lxia
 *
 */


public class IBISTPSProcessor extends AbstractTPSProcessor
{
	
    public IBISTPSProcessor( )
    { 
     }
    
    public IBISTPSProcessor( Context context, File file)
    {
    	init(context, file); 
     }
 
    public void init(Context context, File file){
        setContext(context);
        tpsFile_ = file; 
 		errFilename = tpsFile_.getName(); 
		errFilename = errFilename.substring( 0, errFilename.lastIndexOf(TPSProvisioningAgent.getTpsAppendix(context))-1) + ".err";    	
    	
    }
    public boolean processFile()
    {
    	boolean ret = true; 
		IBISTPSInputStream in = null; 
		Context context_ = this.getContext().createSubContext();
		context_.put( TPSProcessor.class, this); 
		
		context_.put(TPSPipeConstant.PIPELINE_TPS_KEY, 
			       	 new IBISPaymentTypeMappingAgent(
			       	 new VoidFieldCheckingAgent(
   				     new ConveragedAccountSubscriberLookupAgent(
  	    		     new IbisDuplicationCheckingAgent(
				     new ConvertTpsToTransactionAgent(null))))));

  		try {	
			in = new IBISTPSInputStream(this.getContext(), new FileInputStream(tpsFile_)); 
			if (!in.verifyHeader() ){
				new MinorLogMsg(this, "Invalid TPS file header", null).log(this.getContext()); 
				ret = false; 
			} else { 
 				while (true){
					Context sub_context = context_.createSubContext(); 
		   			try 
		   			{
		   				TPSRecord  tps  = in.readTps( sub_context);
		   				tps.setTpsFileName(this.tpsFile_.getName()); 
						new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_ATTEMPT, 1).log(getContext());
		   				Pipeline.pump(sub_context, tps);
		   				
		   			} 
		   			catch (InvalidTPSRecordException te)
		   			{
						new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_FAIL, 1).log(getContext());						
						ERLogger.genInvalidEntryER(sub_context); 
                        
                        // Put the error message into the TPS record so that the message
                        // can be generated in the error file.
                        final TPSRecord tps = (TPSRecord) sub_context.get(TPSRecord.class);
                        if (tps != null)
                        {
                            tps.setLastError(te.getMessage());
                        }
                        
						writeErrFile( sub_context); 
 						new EntryLogMsg(10532, this, "","", null, te).log(this.getContext());
 					}
					
					
				}
			}	
  		}catch (EOFException ee)
		{
			
			  // do nothing
		}
		catch (IOException ie)
		{
 			new MinorLogMsg(this, "IO error occured when processing TDR file " + 
 					this.tpsFile_.getName(),ie).log(getContext());
		 	ret = false; 
		 }
		 catch (Exception e)
		 {
 			 new MinorLogMsg(this, "Exception occured when processing TPS file " + 
 			 		this.tpsFile_.getName(), e).log(getContext());
		 	 ret = false; 
		 } 
		 finally 
		 {
			
			   try
			   {
				   in.close();
				  
	 		   }
			   catch (Exception e)
			   {
		 		  new MinorLogMsg(this, "IO error occured when closing TPS  file " + 
						tpsFile_.getName(), e).log(getContext());
			   }
			   try
			   {
				   if (out != null){
				  	out.println(getCountString(errCounter) + "9999999999"); 
				  	out.close(); 
				  }
	 		   }
			   catch (Exception e)
			   {
		 		  new MinorLogMsg(this, "IO error occured when closing TPS Error file " + 
						errFilename, e).log(getContext());
			   }
		}
      	return ret; 
     }
     
	
 	public synchronized void writeErrFile(Context ctx){
		 try {  		
			 if (out == null){
				TPSConfig config=(TPSConfig) ctx.get(TPSConfig.class);
				out = new PrintStream( new FileOutputStream( 
					 new File( config.getErrorDirectory() + File.separator 
					 + errFilename)));   			
				out.println((String)ctx.get(TPSPipeConstant.TPS_PIPE_ERROR_FILE_HEADER)); 
				ctx.put(TPSPipeConstant.TPS_PIPE_ERROR_OUTPUT_STREAM, out); 
			 	errCounter = 2; 
			 }
			
			TPSRecord  tps  = (TPSRecord) ctx.get(TPSRecord.class);
			if (tps != null)
			{
 				out.println(tps.getRawline());
              out.println("#ERROR - " + tps.getLastError());
 				++errCounter;  
			}
		 
		 } catch ( Exception e ){
			new MinorLogMsg(this, "IO error occured when writing to TPS Error file " + 
					errFilename, e).log(ctx);
		 }
	}


	long errCounter = 2; // header and trailer line are included.
    
  }
