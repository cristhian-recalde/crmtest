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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Subscriber;


/**
 * Forces a preload of certain lazily-loaded values in the Subscriber profile
 * when find() is called.
 *
 * This class was created to solve a problem in WSC.  Since WSC is often unable
 * to load these values itself, CRM+ must populate them in the profile before
 * sending the profile to WSC.
 *
 * @author gary.anderson@redknee.com
 */
public
class PreloadLazyValuesHome
    extends HomeProxy
{
    /**
     * Creates a new PreloadLazyValuesHome for the given delegate.
     *
     *
     * @param context The operating context.
     * @param delegate The Subscriber home to which this decorator delegates.
     */
    public PreloadLazyValuesHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }


    /**
     * Forces a preload of the laziliy loaded property PersonalListPlanEntries.
     *
     * {@inheritDoc}
     */
    public Object find(final Context context, final Object key)
        throws HomeException
    {
        final Object result = super.find(context, key);

        if (result != null && result instanceof Subscriber)
        {
            final Subscriber subscriber = (Subscriber)result;
            subscriber.getPersonalListPlanEntries();
        }

        return result;
    }

} // class
