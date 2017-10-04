/*
 *  Pipeline.java
 *
 *  Author : Larry Xia
 *  Date   : Oct 24, 2003
 *
 *  Copyright (c) Redknee, 2003
 *    - all rights reserved
 */
 
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.tps.IBISTPSProcessor;
import com.trilogy.app.crm.bas.tps.TPSProcessor;
import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;


/** Static Pipeline support methods. **/
public class Pipeline 
{
  

  
   /** Enters a TDR into the start of the Context Pipeline. **/
   public static void pump(Context ctx, TPSRecord tps)
   {
   	  Context subCtx = ctx.createSubContext();
      try
      {
         subCtx.put(TPSRecord.class, tps);
		 ContextAgent start = null; 
		 
      	 subCtx.put(TPSPipeConstant.TPS_PIPE_RESULT_CODE, Integer.valueOf(0));
		
 		start = (ContextAgent) subCtx.get(TPSPipeConstant.PIPELINE_TPS_KEY);
         
         if ( start != null )
         {
           	start.execute(subCtx);
           	Integer result = (Integer) subCtx.get(TPSPipeConstant.TPS_PIPE_RESULT_CODE);
           	if (result.intValue() != TPSPipeConstant.RESULT_CODE_SUCCESS)
           	{
				if (LogSupport.isDebugEnabled(ctx))
				{
	           		new DebugLogMsg(Pipeline.class,
						"Writing error to the tps error file because of error " + result, null).log(subCtx);
				}
           		TPSProcessor processor = (TPSProcessor) subCtx.get(TPSProcessor.class);
           		processor.writeErrFile(subCtx);
				new OMLogMsg(Common.OM_MODULE, 
					Common.OM_PAYMENT_FAIL, 1).log(ctx);
 
           	} else 
           	{
				new OMLogMsg(Common.OM_MODULE, 
					Common.OM_PAYMENT_SUCCESS, 1).log(ctx);
         		
           	}
         }
		 else 
		 {
		 	throw new Exception( "can not find TPS pipe in the context"); 
		 }
      }
      catch (Throwable t)
      {
		 if (LogSupport.isDebugEnabled(ctx))
		 {
         	new DebugLogMsg(Pipeline.class.getName(), "Pipeline Exception", t).log(subCtx);
		 }
		 new OMLogMsg(Common.OM_MODULE, 
			Common.OM_PAYMENT_FAIL, 1).log(ctx);
		 IBISTPSProcessor processor = (IBISTPSProcessor) subCtx.get(IBISTPSProcessor.class);
         
         // Put the error message into the TPS record so that the message
         // can be generated in the error file.
         if (tps != null)
         {
             tps.setLastError(t.getMessage());
         }

		 processor.writeErrFile(subCtx);
      }
   }
   
}


