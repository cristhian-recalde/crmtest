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
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;


/**
 * Verifies if the Msisdn can be removed.
 *
 * @author victor.stratan@redknee.com
 */
public class MsisdnRemovalValidatorHome
    extends HomeProxy
{
    /**
     * Creates a new MsisdnRemovalValidatorHome.
     *
     * @param delegate The Home to which we delegate.
     */
    public MsisdnRemovalValidatorHome(
		final Home delegate)
        throws HomeException
    {
        super(delegate);
    }

    //INHERIT
    public void remove(Context ctx, Object obj) throws HomeException
    {
        final Msisdn msisdn = (Msisdn)obj;

        // Throws HomeException.
        if (msisdn.getState().equals(MsisdnStateEnum.IN_USE))
        {
            throw new HomeException("Cannot delete Mobile Number while it is IN USE.");
        }

        getDelegate(ctx).remove(ctx, obj); 
    }

} // class
