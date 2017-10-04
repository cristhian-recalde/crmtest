/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;


/**
 * This home puts a restriction on the Auxiliary services that if any subscriber has
 * subscribed to this auxiliary service then that auxiliary service can not be deleted
 *
 * @author prasanna.kulkarni@redknee.com
 * @time Oct 7, 2005
 */
public class AuxiliaryServiceCheckAssociationsHome extends HomeProxy
{

    /**
     * Creates a new AuxiliaryServiceCheckAssociationsHome proxy.
     *
     * @param delegate
     *            The Home to which we delegate.
     */
    public AuxiliaryServiceCheckAssociationsHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        AuxiliaryService service = (AuxiliaryService) obj;
        if (service.hasSubscriberAssociations(ctx))
        {
            throw new HomeException("Auxiliary Service " + service.getName() + " is in use by one or more subscriptions.  Unable to delete.");
        }
        super.remove(ctx, obj);
    }
}
