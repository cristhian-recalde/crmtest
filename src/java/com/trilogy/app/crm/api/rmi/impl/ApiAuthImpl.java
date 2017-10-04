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
package com.trilogy.app.crm.api.rmi.impl;

import java.rmi.Remote;

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.support.GracefulShutdownSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v2_1.api.ApiAuth;

/**
 * RMI service to support WS-Security in the CRM API.  In the WS-Security callflow
 * the SOAP request is authenticated BEFORE it is passed to CRM.  This class performs
 * the authentication on the API's behalf.
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class ApiAuthImpl implements Remote, ContextAware, ApiAuth
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    public ApiAuthImpl(final Context ctx)
    {
        setContext(ctx);
    }
    
    public Boolean authenticate(CRMRequestHeader header)
    {
        // login to a subcontext, so that the session isn't stored in the API's main context
        return authenticate(getContext().createSubContext(), header.getUsername(), header.getPassword());
    }
    
    private Boolean authenticate(Context ctx, String username, String password)
    {
        return ApiSupport.authenticateUser(ctx, username, password);
    }

    public Boolean notifyApiShutdown(CRMRequestHeader header)
    {
        Context ctx = getContext().createSubContext();
        if( authenticate(ctx, header.getUsername(), header.getPassword())
                && ApiSupport.authorizeUser(ctx, new SimplePermission("api.rmi.shutdown")) )
        {
            GracefulShutdownSupport.setApiShutdown(getContext(), true);
        }
        return getContext().getBoolean(GracefulShutdownSupport.API_SHUTDOWN_CTX_KEY, false);
    }
    
    public Boolean notifyApiStartup(CRMRequestHeader header)
    {
        Context ctx = getContext().createSubContext();
        if( authenticate(ctx, header.getUsername(), header.getPassword())
                && ApiSupport.authorizeUser(ctx, new SimplePermission("api.rmi.shutdown")) )
        {
            GracefulShutdownSupport.setApiShutdown(getContext(), false);
        }
        return getContext().getBoolean(GracefulShutdownSupport.API_SHUTDOWN_CTX_KEY, false);
    }
    
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }
    
    public Context getContext()
    {
        return ctx_;
    }
    
    private Context ctx_ = null;
}
