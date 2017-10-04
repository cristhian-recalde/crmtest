package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This agent will be run by scheduler to invoke ClosedAuxiliaryServiceCleanUpAgent.
 * 
 * @author agourley
 * @since 8.2
 */
public class ClosedAuxiliaryServiceCleanUpCronAgent 
    implements ContextAgent 
{
	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
    
	public void execute(Context ctx) throws AgentException 
	{
       new InfoLogMsg(this, "ClosedAuxiliaryServiceCleanUpCronAgent started !", null).log(ctx);
       
       try
       {
            new ClosedAuxiliaryServiceCleanUpAgent().execute(ctx);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  Closed AuxiliaryService CleanUp Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "Closed AuxiliaryService CleanUp Cron Error", e).log(ctx);
       }

       new InfoLogMsg(this, "Closed AuxiliaryService CleanUp Cron finished !", null).log(ctx);
    }
}
