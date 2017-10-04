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

import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.home.UniqueIdGeneratorHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * This is a home which maintains a single id sequence for all call detail range homes.
 *
 * @author paul.sperneac@redknee.com
 */
public class CallDetailIdGeneratorHome extends UniqueIdGeneratorHome
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;


    public CallDetailIdGeneratorHome(final Context ctx, final Home delegate, final String sequenceName)
    {
        super(ctx, delegate, sequenceName);
    }

  

    /**
     * {@inheritDoc}
     */
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final CallDetail cd = (CallDetail) obj;
        if (cd.getId() == 0)
        {
            cd.setId(getSequence().nextValue(ctx));
        }

        return super.create(ctx, cd);
    }

}
