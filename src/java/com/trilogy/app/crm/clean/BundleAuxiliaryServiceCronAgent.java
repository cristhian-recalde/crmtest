/*
 *  BundleAuxiliaryServiceCronAgent.java
 *
 *  Author : danny.ng@redknee.com
 *  Date   : Dec 16, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.clean;

import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * This agent will be run by scheduler as a nightly batch process
 * to invoke BundleAuxiliaryServiceAgent.
 * 
 * @author danny.ng@redknee.com
 */
public class BundleAuxiliaryServiceCronAgent implements ContextAgent
{
    private TaskEntry task = null;
    
    public BundleAuxiliaryServiceCronAgent()
    {
    }
    
    public BundleAuxiliaryServiceCronAgent(TaskEntry task)
    {
        this.task = task;
    }

    public void execute(Context ctx) throws AgentException
    {
        new InfoLogMsg(this, "BundleAuxiliaryServiceCronAgent started!", null).log(ctx);
        
        try
        {
             if ( task == null )
             {
                 new BundleAuxiliaryServiceAgent().execute(ctx);
             }
             else
             {
                 new BundleAuxiliaryServiceAgent(task).execute(ctx);
             }

             new InfoLogMsg(this, "BundleAuxiliaryServiceCronAgent finished!", null).log(ctx);
        }
        catch(Exception e)
        {
             new MinorLogMsg(this, "BundleAuxiliaryServiceCronAgent Cron Error", e).log(ctx);
        }
    }
}
