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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.clean.visitor.ClosedAuxiliaryServiceCleanUpVisitor;


/**
 * When updating or removing closed auxiliary services, this class will
 * clean up anything that references that service.
 *
 * @author Aaron Gourley
 * @since 8.2
 */
public class ClosedAuxiliaryServiceCleanupHome extends HomeProxy
{
    public ClosedAuxiliaryServiceCleanupHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    
    /**
     * @{inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        AuxiliaryService service = (AuxiliaryService) super.store(ctx, obj);

        if (service.isInFinalState())
        {
            try
            {
                cleanup(ctx, service);
            }
            catch (AgentException e)
            {
                // Log this error, but don't rethrow it.  The Closed Auxiliary Service Cleanup Agent will retry.
                String msg = "Error occurred cleaning up after closed Auxiliary Service " + service.getIdentifier() + ": " + e.getMessage();
                new MinorLogMsg(this, msg, null).log(ctx);
                new DebugLogMsg(this, msg, e).log(ctx);
            }
        }
        
        return service;
    }

    
    /**
     * @{inheritDoc}
     */
    @Override
    public void remove(Context ctx, Object obj) throws HomeException
    {
        AuxiliaryService service = (AuxiliaryService) obj;
        if (service.isInFinalState())
        {
            try
            {
                cleanup(ctx, service);
            }
            catch (AgentException e)
            {
                // Log this error and rethrow it.  The Closed Auxiliary Service Cleanup Agent will retry, but we
                // must not proceed with the removal attempt.
                new MinorLogMsg(this, "Error occurred cleaning up after to closed Auxiliary Service " + service.getIdentifier() + ": " + e.getMessage(), e).log(ctx);
                throw new HomeException(e);
            }
        }
        
        super.remove(ctx, obj);
    }


    private void cleanup(Context ctx, AuxiliaryService service) throws AgentException
    {
        try
        {
            ClosedAuxiliaryServiceCleanUpVisitor.instance().visit(ctx, service);
        }
        catch (AbortVisitException e)
        {
            // Ignore
        }
    }
}
