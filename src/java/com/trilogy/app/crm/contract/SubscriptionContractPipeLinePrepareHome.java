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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.contract;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * A few transient variables need to be prepared
 */
public class SubscriptionContractPipeLinePrepareHome extends HomeProxy
{

    /**
     * @param delegate
     */
    public SubscriptionContractPipeLinePrepareHome(Home delegate)
    {
        super(delegate);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        Context subCtx = prepareProvisionHome(ctx, obj, false);
        
        Object ret = super.create(subCtx, obj);

        return ret;
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        final Context subCtx = prepareProvisionHome(ctx, obj, true);
        super.remove(subCtx, obj);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Context subCtx = prepareProvisionHome(ctx, obj, true);
        Object ret = super.store(subCtx, obj);
        return ret;
    }

    /**
     * Prepare context for subscription contract pipeline starting. All new key will be dropped after pipeline finished
     *
     * @param parentCtx
     * @param obj
     * @param isStoreOrRemove
     * @return
     * @throws HomeException
     */
    Context prepareProvisionHome(final Context parentCtx, final Object obj, final boolean isStoreOrRemove)
        throws HomeException
    {
        final Context ctx = parentCtx.createSubContext();
        SubscriptionContract contract  = (SubscriptionContract) obj;

        SubscriptionContractSupport.getSubscriptionContractTerm(ctx, contract);
      
        return ctx;
    }

}
