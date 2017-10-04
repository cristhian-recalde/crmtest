/*
 * Created on 2004-11-9
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.cltc;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * @author jchen
 *
 *Cron Agent for SubscriberCltcCleanUpAgent
 */
public class SubscriberCltcCleanUpCronAgent 
implements ContextAgent 
{
	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
    
	public void execute(Context ctx) throws AgentException 
	{
       new InfoLogMsg(this, "SubscriberCltcCleanUpCronAgent started !", null).log(ctx);
       
       try
       {
            new SubscriberCltcCleanUpAgent().execute(ctx);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "[[[[  SubscriberCltc CleanUp Finished  ]]]] ", null).log(ctx);
            }
       }
       catch(Exception e)
       {
            new MinorLogMsg(this, "SubscriberCltc CleanUp Cron Error", e).log(ctx);
       }

       new InfoLogMsg(this, "SubscriberCltc CleanUp Cron finished !", null).log(ctx);
    }
}