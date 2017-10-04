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
package com.trilogy.app.crm.support;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeCmdEnum;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Support class that enables the increment on homes.
 * This class is implemented as support class for any home
 * factory that might need it since it won't be using the method
 * "enableAutoIncrement" for homes.
 * @author arturo.medina@redknee.com
 *
 */
public final class AutoIncrementSupport
{
    /**
     * private constructor
     * This is a Utility class and shouln't have default constructor
     *
     */
    private AutoIncrementSupport()
    {

    }


    /**
     * Enables autoincrementing in transient homes.
     *
     * @param ctx registry
     * @param home the home to enable
     */
    public static void enableAutoIncrement(final Context ctx,
            final Home home)
    {
        try
        {
            home.cmd(ctx, HomeCmdEnum.AUTOINC_ENABLE);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(AutoIncrementSupport.class, e.getMessage(), e).log(ctx);
            }
        }
    }

}
