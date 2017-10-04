/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author amedina
 *
 * This agent will be run by scheduler as a nightly batch process
 * to invoke ActivePendingSubAgent
 */
public class AvailablePendingSubCronAgent implements ContextAgent {

	public AvailablePendingSubCronAgent()
	{
		
	}
	
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context ctx) throws AgentException 
	{
	       new InfoLogMsg(this, "AvailablePendingSubCronAgent started !", null).log(ctx);
	       
	       try
	       {
	            new AvailablePendingSubAgent().execute(ctx);

	            if (LogSupport.isDebugEnabled(ctx))
	            {
	                new DebugLogMsg(this, "[[[[  AvailablePendingSubCronAgent Finished  ]]]] ", null).log(ctx);
	            }
	       }
	       catch(Exception e)
	       {
	            new MinorLogMsg(this, "AvailablePendingSubCronAgent Cron Error", e).log(ctx);
	       }

	      
	       new InfoLogMsg(this, "MSISDNStateCronAgent finished !", null).log(ctx);
	}

}
