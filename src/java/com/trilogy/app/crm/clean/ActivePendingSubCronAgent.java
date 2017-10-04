package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.*;


/**
 * This agent will be run by scheduler as a nightly batch process
 * to invoke ActivePendingSubAgent
 * @author lzou
 * @date   Nov 10, 2003
 * no longer in use, function moved to  SubscriberStateUpdateCronAgent.
 * 
 */
public class ActivePendingSubCronAgent 
    implements ContextAgent 
{
	public ActivePendingSubCronAgent()
	{
		
	}
	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
    
	public void execute(Context ctx) throws AgentException 
	{
       new InfoLogMsg(this, "ActivePendingSubCronAgent started !", null).log(ctx);
       
       try
       {
            new ActivePendingSubAgent().execute(ctx);

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  ActivePendingSubCronAgent Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "ActivePendingSubCronAgent Cron Error", e).log(ctx);
       }

      
       new InfoLogMsg(this, "ActivePendingSubCronAgent finished !", null).log(ctx);
    }
}
