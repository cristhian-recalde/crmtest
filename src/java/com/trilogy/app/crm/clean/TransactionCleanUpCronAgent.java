package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.*;


/**
 * This agent will be run by cron scheduler as a nightly batch process
 * to invoke TransactionCleanUpAgent which will remove all out-dated
 * transaction history entries from database.
 * 
 * @author lzou
 * @date   Nov 10, 2003
 */
public class TransactionCleanUpCronAgent 
    implements ContextAgent 
{
	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
    
	public void execute(Context ctx) throws AgentException 
	{
       new InfoLogMsg(this, "TransactionCleanUpCronAgent started !", null).log(ctx);
       
       try
       {
            new TransactionCleanUpAgent().execute(ctx);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  Transaction CleanUp Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "Transaction CleanUp Cron Error", e).log(ctx);
       }
       
    }
}
