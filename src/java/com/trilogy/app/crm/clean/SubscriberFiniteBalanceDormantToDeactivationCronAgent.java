/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.*;

/**
 * This agent will be run by scheduler as a nightly batch process
 * to invoke SubscriberFiniteBalanceDeactivationCleanupCronAgent 
 * 
 * @author simar.singh@redknee.com 
 * 
 */
public class SubscriberFiniteBalanceDormantToDeactivationCronAgent 
    implements ContextAgent
{
   /**
    * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
    */

   public void execute(Context ctx) throws AgentException
   {
      new InfoLogMsg(this, "SubscriberFiniteBalanceDeactivationCleanupCronAgent started !", null).log(ctx);

      try
      {
         new SubscriberFiniteBalanceDormantToDeactivateAgent().execute(ctx);

         if (LogSupport.isDebugEnabled(ctx))
         {
            new DebugLogMsg(this, "[[[[  Subscriber Deactivation Cleanup Finished  ]]]] ", null).log(ctx);
         }
      }
      catch (Exception e)
      {
         new MinorLogMsg(this, "Subscriber CleanUp Cron Error", e).log(ctx);
      }

   }
}
