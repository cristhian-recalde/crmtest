package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.*;


/**
 * This agent will be run by scheduler as a nightly batch process
 * to invoke SubEndDateCheckAgent.
 * 
 * @author lzou
 * @date   Nov 10, 2003
 */
public class SubEndDateCheckCronAgent 
    implements ContextAgent 
{
	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
    
	public void execute(Context ctx) throws AgentException 
	{
       new InfoLogMsg(this, "SubEndDateCheckCronAgent started !", null).log(ctx);
       
       try
       {
            new SubEndDateCheckAgent().execute(ctx);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  Subscriber END Date Check Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "Subscriber End Date Check Cron Error", e).log(ctx);
       }
       
    }
}
