package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.*;

/**
 * This agent invokes TransferExceptionCleanUpAgent 
 * 
 * @author ling.tang@redknee.com
 */
public class TransferExceptionCleanUpCronAgent 
    implements ContextAgent
{
   /**
    * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
    */

   public void execute(Context ctx) throws AgentException
   {
      new InfoLogMsg(this, "TransferExceptionCleanUpCronAgent started !", null).log(ctx);

      try
      {
         new TransferExceptionCleanUpAgent().execute(ctx);

         if (LogSupport.isDebugEnabled(ctx))
         {
            new DebugLogMsg(this, "[[[[  Transfer Exception CleanUp Finished  ]]]] ", null).log(ctx);
         }
      }
      catch (Exception e)
      {
         new MinorLogMsg(this, "Transfer Exception CleanUp Cron Error", e).log(ctx);
      }

   }
}
