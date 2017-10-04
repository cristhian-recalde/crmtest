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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * @author jke
 */
public class MsisdnPrefixValidationHome extends HomeProxy
{

    public MsisdnPrefixValidationHome(Context ctx, Home _delegate)
    {
        super(_delegate);
        setContext(ctx);
    }


    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        try
        {
            return super.create(ctx, obj);
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            if (e.getMessage().indexOf("XStatement Error for [INSERT INTO XMsisdnPrefix") != -1
                    && e.getMessage().indexOf("unique constraint") != -1)
            {
                throw new HomeException("Prefix already exists.", e);
            }
            else
                throw e;
        }
    }


    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        try
        {
            return super.store(ctx, obj);
        }
        catch (HomeException e)
        {
            if (e.getMessage().indexOf("XStatement Error for [UPDATE XMsisdnPrefix") != -1
                    && e.getMessage().indexOf("unique constraint") != -1)
                throw new HomeException("Prefix already exists.", e);
            else
                throw e;
        }
    }

}
