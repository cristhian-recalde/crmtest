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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;


import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Update all Subscriber states based on their state timers.
 *
 * @author jimmy.ng@redknee.com
 */
public class SubscriberStateUpdateAgent
    implements ContextAgent
{
    
    /**
     * Create a new SubscriberStateUpdateAgent.
     *
     * @param context The operating context.
     */
    public SubscriberStateUpdateAgent(final Context context)
    {
    }
    
    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(final Context context)
        throws AgentException
    {
        final PMLogMsg pmLog = new PMLogMsg(PM_MODULE, "execute()");
        
        try
        {
            
        	
        }
        finally
        {
            pmLog.log(context);
        }
    }
    
    


    
    /**
     * Used to identify this class in the performance measurements.
     */
    private static final String PM_MODULE = SubscriberStateUpdateAgent.class.getName();
    
    

} // class
