/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.calldetail;

import java.util.Collection;

import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * @author  
 *
 */
public class CallDetailPrimaryKeyFindHome extends HomeProxy implements Constants
{
    /**
     * 
     */
    private static final long serialVersionUID = 944629720141220038L;

    /**
     * @param delegate
     */
    public CallDetailPrimaryKeyFindHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * find entity based on primary key(s)
    * @param ctx
     * @param obj
     * @return Object
     * @exception HomeException
     * @exception HomeInternalException
     */
    public Object find(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        if (String.class.isAssignableFrom(obj.getClass()) || Long.class.isAssignableFrom(obj.getClass()))
        {        
            Collection col = getDelegate(ctx).select(ctx, new EQ(CallDetailXInfo.ID, obj));
            if (! col.isEmpty())
            {   
                return col.iterator().next();
            }
            return null;
        }
        else
        {
            return getDelegate(ctx).find(ctx, obj);
        }
    }
 
}
