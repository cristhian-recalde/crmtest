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
package com.trilogy.app.crm.support;

import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.util.crmapi.wsdl.CRMExceptionFactory;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ShutdownException;


/**
 * 
 * @author Aaron Gourley
 * @since 
 *
 */
public class GracefulShutdownSupport
{
    public static Object API_SHUTDOWN_CTX_KEY = "CrmApiShutdown";
    public static Object CRM_SHUTDOWN_CTX_KEY = "CrmShutdown";

    public static synchronized <T extends Exception> int enter(Context ctx, Class<T> exceptionType) throws T
    {
        activeCallCount++;
        if (isShutdown(ctx))
        {
            new InfoLogMsg(GracefulShutdownSupport.class, "Rejecting incoming request because CRM or CRM API is shutting down.", null).log(ctx);
            FacetMgr fMgr = (FacetMgr) ctx.get(FacetMgr.class);
            CRMExceptionFactory faultFactory = (CRMExceptionFactory) fMgr.getInstanceOf(ctx, exceptionType, CRMExceptionFactory.class);
            if (faultFactory != null)
            {
                throw (T) faultFactory.createFault(new ShutdownException());
            }
            else
            {
                throw new RuntimeException("Rejected incoming request because CRM or CRM API is shutting down.");
            }
        }
        
        return activeCallCount;
    }

    
    /**
     * Performs post-processing logic for all API methods.  Used for graceful shutdown.
     * 
     * @param ctx The operating context
     * @return The number of requests being serviced by the API (excluding the current one)
     */
    public static synchronized int exit(Context ctx)
    {
        activeCallCount = Math.min(activeCallCount-1, 0);
        if( activeCallCount == 0 && isShutdown(ctx) )
        {
            synchronized( SHUTDOWN_LOCK )
            {
                new InfoLogMsg(GracefulShutdownSupport.class, "Notifying shutdown thread that it is now safe to continue with shutdown.  0 outstanding requests.", null).log(ctx);
                SHUTDOWN_LOCK.notifyAll();   
            }
        }
        return activeCallCount;
    }

    /**
     * Determine whether AppCrm or UtilCrmapi is shutting down.
     *
     * @param context
     *            The operating context.
     * @return Whether AppCrm or UtilCrmapi is shutting down.
     */
    public static boolean isShutdown(final Context context)
    {
        synchronized( SHUTDOWN_LOCK )
        {
            return context.getBoolean(CRM_SHUTDOWN_CTX_KEY, false)
                || context.getBoolean(API_SHUTDOWN_CTX_KEY, false);
        }
    }
    
    public static String getWhichShutdown(final Context context)
    {
        synchronized( SHUTDOWN_LOCK )
        {
            if (context.getBoolean(CRM_SHUTDOWN_CTX_KEY, false))
            {
            	return CRM_SHUTDOWN_CTX_KEY.toString();
            }
            return API_SHUTDOWN_CTX_KEY.toString();
        }
    }

    public static void setApiShutdown(final Context context, boolean isShuttingDown)
    {
        synchronized( SHUTDOWN_LOCK )
        {
            context.put(API_SHUTDOWN_CTX_KEY, isShuttingDown);
            if( isShuttingDown )
            {
                new InfoLogMsg(GracefulShutdownSupport.class, "CRM API shutdown flag set.  Future API requests will be rejected until CRM is notified that the API is back up.", null).log(context);   
            }
            else
            {
                new InfoLogMsg(GracefulShutdownSupport.class, "CRM API shutdown flag cleared.  API requests will be accepted.", null).log(context);
            }
        }
    }

    public static void setCrmShutdown(final Context context)
    {
        synchronized( SHUTDOWN_LOCK )
        {
            context.put(CRM_SHUTDOWN_CTX_KEY, true);
            new InfoLogMsg(GracefulShutdownSupport.class, "CRM shutdown flag set.  Future requests will be rejected.", null).log(context);
        }
    }
    
    public static void doGracefulShutdown(Context ctx)
    {
        setCrmShutdown(ctx);
        synchronized( SHUTDOWN_LOCK )
        {
            try
            {
                while( activeCallCount > 0 )
                {
                    new InfoLogMsg(GracefulShutdownSupport.class, "Graceful shutdown in progress.  Waiting for " + activeCallCount + " operations to complete.", null).log(ctx);
                    SHUTDOWN_LOCK.wait();
                }
            }
            catch (InterruptedException e)
            {
                new MinorLogMsg(GracefulShutdownSupport.class, "Interrupted while waiting for graceful termination of supported method calls.  Graceful shutdown can not be guaranteed at this point.", e).log(ctx);
            }
        }
    }
    
    private static final Object SHUTDOWN_LOCK = new Object();
    private static int activeCallCount = 0;

}
