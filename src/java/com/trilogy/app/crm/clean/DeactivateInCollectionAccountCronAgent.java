package com.trilogy.app.crm.clean;

import java.util.Date;

import com.trilogy.app.crm.account.state.timer.DeactivateInCollectionAccount;
import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.*;

public class DeactivateInCollectionAccountCronAgent
   extends ContextAwareSupport
   implements CronContextAgent
{
	public DeactivateInCollectionAccountCronAgent()
	{
		
	}
   // INHERIT
   public void execute(final Context ctx)
      throws AgentException
   {
      if (LogSupport.isDebugEnabled(ctx))
      {
         new DebugLogMsg(this, "Deactivate IN_COLLECTION Accounts cron task initiated.",null).log(ctx);
      }

      try
      {
         new DeactivateInCollectionAccount().execute(ctx);
      }
      catch (Throwable t)
      {
         final String message = 
            "Deactivate IN_COLLECTION Accounts cron task encounterred exception.";

         new MajorLogMsg(this, "Deactivate IN_COLLECTION Accounts cron task encounterred exception.", t).log(ctx);
         throw new CronContextAgentException(message, t);
      }

      if (LogSupport.isDebugEnabled(ctx))
      {
         new DebugLogMsg(this, "Deactivate IN_COLLECTION Account cron task complete", null).log(ctx);
      }
   }

   // INHERIT
   public void stop()
   {
   }
}
