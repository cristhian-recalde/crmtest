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
package com.trilogy.app.crm.bulkloader.generic.bean;

import java.util.Collection;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This adapter is installed in the SearchableSubscriber home pipeline.
 * The purpose of that pipeline is to find the Subscriber Identifier using the 
 * SearchableSubscriber criteria.
 * 
 * Only the search home methods are supported.  All other operations are not supported.
 * @author angie.li@redknee.com
 *
 */
public class FindSubscriberAdapterHome extends HomeProxy 
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public FindSubscriberAdapterHome(Context ctx)
    {
        super(ctx);
    }
    
    /**
     * Find and return Subscriber based on given SearchableSubscriber parameters.
     * Returns Null if no such subscriber exists in the System.
     * @param ctx
     * @param obj
     * @return Object
     * @exception HomeException
     */
    public Object find(Context ctx, Object obj)
    throws HomeException
    {
        Subscriber sub = null;
        if (obj instanceof Predicate)
        {
            throw new UnsupportedOperationException("SearchableSubscriber pipeline doesn't allow the Home.find(Predicate) operation.  Use Home.find(Bean)");
        }

        if (obj instanceof SearchableSubscriber)
        {
            SearchableSubscriber querySub = (SearchableSubscriber) obj;

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, 
                        "Looking up Subscriber using the SearchableSubscriber pipeline. Query=" + querySub.toString());
            }
            
            try
            {
                sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, querySub.getSearchMsisdn(),
                        querySub.getSubscriptionType(), CalendarSupportHelper.get(ctx).getEndOfDay(querySub.getSearchDate()));
            }
            catch(HomeException e)
            {
                throw new HomeException("Failed to find the Subscriber using the Searchable Subscriber pipeline. " + e.getMessage(), e);
            }
        }
        return sub;
    }


    public Object create(Context ctx, Object obj)
    throws HomeException
    {
        throw new UnsupportedOperationException("SearchableSubscriber pipeline doesn't allow the Home.create operation.");
    }

    public Object store(Context ctx, Object obj)
    throws HomeException
    {
        throw new UnsupportedOperationException("SearchableSubscriber pipeline doesn't allow the Home.store operation.");
    }

    public void remove(Context ctx, Object obj)
    throws HomeException
    {
        throw new UnsupportedOperationException("SearchableSubscriber pipeline doesn't allow the Home.remove operation.");

    }

    public Collection select(Context ctx, Object obj)
    throws HomeException
    {
        throw new UnsupportedOperationException("SearchableSubscriber pipeline doesn't allow the Home.select operation.  Use the Home.find operartion.");

    }
}
