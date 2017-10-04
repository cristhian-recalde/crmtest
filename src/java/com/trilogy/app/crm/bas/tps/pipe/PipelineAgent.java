/*
 *  PipelineAgent.java
 *
 *  Author : Kevin Greer
 *  Date   : Mar 24, 2003
 *
 *  Copyright (c) Redknee, 2003
 *    - all rights reserved
 */
 
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/** PipelineAgent base class. **/
public class PipelineAgent
   extends ContextAgentProxy
{

   public PipelineAgent(ContextAgent delegate)
   {
      super(delegate);
   }
   
 
   public Object require(Context ctx, Object that, Object key)
   {
      Object ret = ctx.get(key);
      
      if ( ret == null )
      {
		 if (LogSupport.isDebugEnabled(ctx))
		 {
         	new DebugLogMsg(that, "Invalid Context: Missing " + key, null).log(ctx);
		 }
      }
      
      return ret;
   }
   
   
   public void fail(Context ctx, Object that, String msg, Throwable t, int errorCode)
   {
       fail(ctx, that, msg, t, errorCode, 0);
   }

   public void fail(Context ctx, Object that, String msg, Throwable t, int errorCode, int ocgErrorCode)
   {
   	  ctx.put(Exception.class, t); 
   	  ctx.put(TPSPipeConstant.TPS_PIPE_RESULT_CODE, Integer.valueOf(errorCode));
   	 
   	  if (ocgErrorCode!=0)
   	  {
   	      ctx.put(TPSPipeConstant.TPS_UPS_RESULT_CODE, Integer.valueOf(ocgErrorCode));
   	  }
   	  
	  if (LogSupport.isDebugEnabled(ctx))
	  {
      	new DebugLogMsg(that, msg, t).log(ctx);
	  }
      
        // Put the error message into the TPS record so that the message
        // can be generated in the error file.
        final TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class);
        if (tps != null)
        {
            tps.setLastError(msg);
        }
   }
 
   public void pass(Context ctx, Object that, String msg)
      throws AgentException
   {
	  if (LogSupport.isDebugEnabled(ctx))
	  {
      	new DebugLogMsg(that, msg, null).log(ctx);
	  }
	  if (getDelegate() != null )
      	getDelegate().execute(ctx);
   }


}


