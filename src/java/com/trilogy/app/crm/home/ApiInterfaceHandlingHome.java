package com.trilogy.app.crm.home;

import com.trilogy.app.crm.api.queryexecutor.ApiInterface;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodHome;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodXInfo;
import com.trilogy.app.crm.home.pipelineFactory.ApiMethodHomePipelineFactory;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class ApiInterfaceHandlingHome extends HomeProxy
{
    public ApiInterfaceHandlingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    @Override
    public Object create(Context ctx, Object obj) throws HomeException 
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object store(Context ctx, Object obj) throws HomeException 
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Context ctx, Object obj) throws HomeException 
    {
        throw new UnsupportedOperationException();
    }

}
