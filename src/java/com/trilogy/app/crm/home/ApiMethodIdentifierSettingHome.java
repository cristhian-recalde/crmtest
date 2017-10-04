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

import com.trilogy.app.crm.api.queryexecutor.ApiInterface;
import com.trilogy.app.crm.api.queryexecutor.ApiMethod;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class ApiMethodIdentifierSettingHome extends HomeProxy implements ContextAware 
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public ApiMethodIdentifierSettingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

 
    /* (non-Javadoc)
     * Auto create account object if ban is not set
     * @see com.redknee.framework.xhome.home.Home#create(java.lang.Object)
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException 
    {
        ApiMethod method = (ApiMethod) obj;
        method.setFullName(method.getApiInterface() + "." + method.getName());
        method.setApiInterfaceFullName(HomeSupportHelper.get(ctx).findBean(ctx, ApiInterface.class, method.getApiInterface()).getFullName());
        method.setCustom(true);
        return super.create(ctx,method);
    }
    
 }
