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
package com.trilogy.app.crm.bean;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.MLogMsg;

/**
 * 
 * @author simar.singh@redknee.com
 *
 * @param <BEAN> - It is home-bean (having a primary key attribute)
 */
public class IdentifiableBackgroundTask<BEAN extends AbstractBean & Identifiable> extends BackgroundTask
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public IdentifiableBackgroundTask(BEAN taskOwner)
    {
        initilaizeOwner(taskOwner);
    }
    
    public IdentifiableBackgroundTask(BEAN taskOwner, ContextAgent executor)
    {
        initilaizeOwner(taskOwner);
        initilaizeExecutor(executor);
    }
    
    private final void initilaizeOwner(BEAN taskOwner)
    {
        setTaskOwner(taskOwner);
        setOwnerId(String.valueOf(taskOwner.ID()));
    }
    
    private final void initilaizeExecutor(ContextAgent taskExecutor)
    {
        setTaskExecutor(taskExecutor);
    }
    
    @Override
    public String getKey()
    {
        BEAN taskOwner = (BEAN) getTaskOwner();
        return String.valueOf(taskOwner.ID());
    }

    @Override
    public void execute(Context ctx) throws AgentException
    {
        initilaizeOwner((BEAN) getTaskOwner());
        super.execute(ctx);
    }

//    @Override
//    public ContextAgent getTaskExecutor()
//    {
//        // TODO Auto-generated method stub
//        // TODO Auto-generated method stub
//        return new ContextAgent()
//        {
//
//            @Override
//            public void execute(Context ctx) throws AgentException
//            {
//                final PPMLogMsg ppmLogMsg = (PPMLogMsg) ctx.get(PPMLogMsg.class);
//                int counter = 0;
//                while (counter < 100)
//                {
//                    try
//                    {
//                        Thread.sleep(5000);
//                    }
//                    catch (InterruptedException e)
//                    {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                    ppmLogMsg.progress(ctx, (counter += 10), 100);
//                }
//            }
//        };
//    }
}
