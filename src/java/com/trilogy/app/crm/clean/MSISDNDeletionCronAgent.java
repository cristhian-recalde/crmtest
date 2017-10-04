package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.*;


/**
 * This agent will be run by scheduler as a nightly batch process
 * to invoke MSISDNDeletionAgent.
 * 
 * @author sgaidhani
 * @date   11 Mar, 2013
 */
public class MSISDNDeletionCronAgent 
    implements ContextAgent 
{
	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
    
	public void execute(Context ctx) throws AgentException 
	{
       new InfoLogMsg(this, "MSISDNDeletionCronAgent started !", null).log(ctx);
       
       try
       {
            new MSISDNDeletionAgent().execute(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  MSISDN Deletion Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "MSISDN Deletion Cron Error", e).log(ctx);
       }

      
       new InfoLogMsg(this, "MSISDNDeletionCronAgent finished !", null).log(ctx);
    }
}
