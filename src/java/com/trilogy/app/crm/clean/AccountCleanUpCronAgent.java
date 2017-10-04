package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

import com.trilogy.framework.xlog.log.*;


/**
 * This agent will be run by scheduler as a nightly batch process
 * to invoke AccountCleanUpAgent.
 * 
 * @author lzou
 * @date   Nov 10, 2003
 */
public class AccountCleanUpCronAgent 
    implements ContextAgent 
{
	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
    
	public void execute(Context ctx) throws AgentException 
	{
       new InfoLogMsg(this, "AccountCleanUpCronAgent started !", null).log(ctx);
       
       try
       {
            new AccountCleanUpAgent().execute(ctx);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  Account CleanUp Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "Account CleanUp Cron Error", e).log(ctx);
       }

       new InfoLogMsg(this, "Account CleanUp Cron finished !", null).log(ctx);
    }
}
