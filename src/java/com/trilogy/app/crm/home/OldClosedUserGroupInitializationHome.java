/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This class provides functionality for creating/removing sub auxiliary services
 * associated with the corresponding Closed User Group.
 * 
 * @author ltse
 */
public class OldClosedUserGroupInitializationHome extends HomeProxy
{

    /**
     * Creates a new ClosedUserGroupAuxiliaryServiceCreationHome.
     * 
     * @param ctx
     *            The operating context.
     * @param delegate
     *            The home to delegate to.
     */
    public OldClosedUserGroupInitializationHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * INHERIT
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        final ClosedUserGroup cug = (ClosedUserGroup) obj;
        // get old CUG and put it into the context
        ClosedUserGroup oldCug = ClosedUserGroupSupport.getCUG(ctx, cug.getID(), cug.getSpid());
        Context subCtx = ctx.createSubContext();
        subCtx.put(ClosedUserGroupServiceHome.OLD_CUG, oldCug);
        obj = super.store(subCtx, obj);
        return obj;
    }
    
}
