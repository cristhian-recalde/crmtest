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
package com.trilogy.app.crm.lifecycle;

import com.trilogy.app.crm.support.GracefulShutdownSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.Context;


/**
 * 
 * @author Aaron Gourley
 * @since 1.1
 */
public class GracefulShutdownLifecycleAgent extends LifecycleAgentSupport
{
    public GracefulShutdownLifecycleAgent(Context ctx)
    {
        super(ctx);
    }

    @Override
    public synchronized Object doCmd(Context ctx, Object cmd) throws LifecycleException
    {
        if (cmd instanceof LifecycleStateEnum)
        {
            LifecycleStateEnum state = (LifecycleStateEnum) cmd;
            if(LifecycleStateEnum.RELEASE.equals(state))
            {
                GracefulShutdownSupport.doGracefulShutdown(ctx);
            }
        }
        
        return super.doCmd(ctx, cmd);
    }

    @Override
    public synchronized void doRelease(Context ctx) throws LifecycleException
    {
        GracefulShutdownSupport.doGracefulShutdown(ctx);

        super.doRelease(ctx);
    }
    
}
