/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bundle.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Home that forks write operations (create, store, remove) to a writeOnlyHome and read requests to a readOnlyHome
 *
 */
public class ReadWriteDelegatingHome extends HomeProxy
{
    /**
     * Constructor
     * @param ctx
     * @param readOnlyHome
     * @param writeOnlyHome
     */
    public ReadWriteDelegatingHome(Context ctx, Home readOnlyHome, Home writeOnlyHome)
    {
        super(ctx, readOnlyHome);
        writeOnlyHome_ = writeOnlyHome;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object create(Context ctx, Object bean)
        throws HomeException
    {
        return getWriteHome().create(ctx, bean);
    }
    
    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx, Object bean)
        throws HomeException
    {
        return getWriteHome().store(ctx, bean);
    }
    
    /**
     * {@inheritDoc}
     */
    public void remove(Context ctx, Object bean)
        throws HomeException
    {
        getWriteHome().remove(ctx, bean);
    }
    
    private Home getWriteHome()
    {
        return writeOnlyHome_;
    }

    /**
     * Write only home delegate
     */
    private Home writeOnlyHome_;
    
}
