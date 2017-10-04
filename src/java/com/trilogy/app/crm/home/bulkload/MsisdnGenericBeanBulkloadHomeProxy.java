package com.trilogy.app.crm.home.bulkload;

import java.util.Collection;

import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkloadHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * @author sbanerjee
 *
 */
public class MsisdnGenericBeanBulkloadHomeProxy extends HomeProxy implements
        GenericBeanBulkloadHome
{

    /**
     * @param ctx
     * @param delegate
     */
    public MsisdnGenericBeanBulkloadHomeProxy(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * @param ctx
     */
    public MsisdnGenericBeanBulkloadHomeProxy(Context ctx)
    {
        super(ctx);
    }
    
    @Override
    public Object create(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        return super.create(ctx, obj);
    }
    
    @Override
    public Collection select(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        return super.select(ctx, obj);
    }
    
    @Override
    public Object store(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        return super.store(ctx, obj);
    }
}