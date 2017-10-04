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
package com.trilogy.app.crm.move.processor.subscription;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;


/**
 * This processor is responsible for pegging any relevent OM's for subscription move
 * business logic.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class OMSubscriptionMoveProcessor<SMR extends SubscriptionMoveRequest> extends MoveProcessorProxy<SMR>
{

    public OMSubscriptionMoveProcessor(MoveProcessor<SMR> delegate)
    {
        this(delegate, 
                Common.OM_SUBSCRIBER_MOVE_ATTEMPT, 
                Common.OM_SUBSCRIBER_MOVE_SUCCESS, 
                Common.OM_SUBSCRIBER_MOVE_FAIL);
    }
    
    public OMSubscriptionMoveProcessor(MoveProcessor<SMR> delegate, 
            String attemptOMName, 
            String successOMName, 
            String failureOMName)
    {
        super(delegate);
        attemptOM_ = attemptOMName;
        successOM_ = successOMName;
        failureOM_ = failureOMName;
        
        if (attemptOM_ == null || successOM_ == null || failureOM_ == null)
        {
            throw new NullPointerException("OM name cannot be null.");
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        new OMLogMsg(Common.OM_MODULE, attemptOM_).log(ctx);   
        try
        {
            super.move(ctx);
            new OMLogMsg(Common.OM_MODULE, successOM_).log(ctx);  
        }
        catch (Exception e)
        {
            new OMLogMsg(Common.OM_MODULE, failureOM_).log(ctx);
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
            
            if (e instanceof MoveException)
            {
                throw (MoveException)e;
            }
            else
            {
                throw new MoveException(this.getRequest(), "Unexpected error performing subscription move.  See logs for details.", e);   
            }
        }
    }

    protected final String attemptOM_;
    protected final String successOM_;
    protected final String failureOM_;
}
