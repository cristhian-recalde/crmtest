package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.*;

/**
 * This agent will be run by scheduler as a nightly batch process
 * to invoke ALL the cleanup cron agents at one time. Has been replaced
 * by seperate CronAgents in Phase3 for easy-to-test purpose. Could be put
 * back when all the cases works properly.
 * 
 * @author lzou
 * @date   Nov 10, 2003
 */
public class CleanupCronAgent 
    implements ContextAgent 
{
	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
    
	public void execute(Context ctx) throws AgentException 
	{
       new InfoLogMsg(this, "CleanupCronAgent started !", null).log(ctx);
       
       try
       {
            new SubscriberCleanUpAgent().execute(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  Subscriber CleanUp Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "Subscriber CleanUp Cron Error", e).log(ctx);
       }
       
       try
       {
            new AccountCleanUpAgent().execute(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  Account CleanUp Finsished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e )
       {
            new MinorLogMsg(this, "Account CleanUp Cron Error", e).log(ctx);
       }

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

       try
       {
            new InvoiceCleanUpAgent().execute(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  Invoice CleanUp Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "Invoice CleanUp Cron Error", e).log(ctx);        
       }

       try
       {
            new MSISDNStateModifyAgent().execute(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  MISIDNState Modify Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "MISISD State Modify Cron Error", e).log(ctx);
       }

       try
       {
            new PackageStateModifyAgent().execute(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  PackageState Modify Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "Package State Modify Cron Error", e).log(ctx);
       }
       
       try
       {
            new SubEndDateCheckAgent().execute(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  Subscriber End Date Check Finished  ]]]] ", null).log(ctx);
            }

       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "Subscriber End Date Check Cron Error", e).log(ctx);
       }
      
       new InfoLogMsg(this, "CleanupCronAgent finished !", null).log(ctx);
    }
}
