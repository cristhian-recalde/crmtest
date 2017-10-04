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

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This adapter is installed in the SearchableSubscriberAuxiliaryService home pipeline.
 * The purpose of that pipeline is to 
 *   1) find the Subscriber Identifier, or 
 *   2) find the CRM Subscriber's provisioned Auxiliary Services 
 * using the  * SearchableSubscriberAuxiliaryService criteria
 * If no such Auxiliary Service is provisioned then return the SubscriberAuxiliaryService 
 * with the appropriate Subscriber identifier filled in.
 * 
 * Only the search home methods are supported.  All other operations are not supported.
 * 
 * @author angie.li@redknee.com
 *
 */
public class FindSubscriberAuxiliaryServiceAdapterHome extends HomeProxy 
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    FindSubscriberAuxiliaryServiceAdapterHome(Context ctx)
    {
        super(ctx);
    }
    
    public Object find(Context ctx, Object obj)
        throws HomeException
    {
        //Subscriber record holder
        Subscriber sub = null;
        //Return value
        SubscriberAuxiliaryService  subAuxService = null;
        if (obj instanceof Predicate)
        {
            throw new UnsupportedOperationException("SearchableSubscriberAuxiliaryService pipeline doesn't allow the Home.find(Predicate) operation.  Use Home.find(Bean)");
        }

        if (obj instanceof SearchableSubscriberAuxiliaryService)
        {
            SearchableSubscriberAuxiliaryService criteria = (SearchableSubscriberAuxiliaryService) obj;

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, 
                        "Looking up Subscriber using the SearchableSubscriberAuxiliaryService pipeline. Query=" + criteria.toString());
            }
            
            try
            {
                sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, criteria.getSearchMsisdn(),
                        criteria.getSubscriptionType(), CalendarSupportHelper.get(ctx).getEndOfDay(criteria.getSearchDate()));
            }
            catch(HomeException e)
            {
                throw new HomeException("Failed to find the Subscriber using the SearchableSubscriberAuxiliaryService pipeline. " + e.getMessage(), e);
            }
            
            /* If subscriber is not found, then return null.  We cannot create such a SubscriberAuxiliaryService. */
            if (sub != null)
            {
                /* If criteria includes Auxiliary Service Id, then search for the provisioned Subscriber Auxiliary Service
                 * Otherwise, return a new Subscriber Auxiliary Service record with the Subscriber information filled in.
                 */
                if (isValidAuxliaryService(ctx, criteria.getAuxiliaryServiceId()))
                {
                    subAuxService = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesBySubIdAndSvcId(ctx, sub.getId(), criteria.getAuxiliaryServiceId());
                    
                    /* In the case that the Auxiliary Service ID is valid, but the Subscriber still doesn't have have this
                     * Auxiliary Service provisioned, the value returned is null.
                     */
                }
                else
                {
                    subAuxService = new SubscriberAuxiliaryService();
                    subAuxService.setSubscriberIdentifier(sub.getId());
                }
            }
        }
        return subAuxService;
    }


    /**
     * Return TRUE if the identified Auxiliary Service exists in the System.
     * Otherwise, returns FALSE.
     * @param ctx
     * @param auxiliaryServiceId
     * @return
     * @throws HomeException
     */
    private boolean isValidAuxliaryService(Context ctx, long auxiliaryServiceId) 
        throws HomeException
    {
        AuxiliaryService auxService = AuxiliaryServiceSupport.getAuxiliaryService(ctx, auxiliaryServiceId);
        return auxService != null;
    }

    public Object create(Context ctx, Object obj)
    throws HomeException
    {
        throw new UnsupportedOperationException("SearchableSubscriberAuxiliaryService pipeline doesn't allow the Home.create operation.");
    }

    public Object store(Context ctx, Object obj)
    throws HomeException
    {
        throw new UnsupportedOperationException("SearchableSubscriberAuxiliaryService pipeline doesn't allow the Home.store operation.");
    }

    public void remove(Context ctx, Object obj)
    throws HomeException
    {
        throw new UnsupportedOperationException("SearchableSubscriberAuxiliaryService pipeline doesn't allow the Home.remove operation.");

    }

    public Collection select(Context ctx, Object obj)
    throws HomeException
    {
        throw new UnsupportedOperationException("SearchableSubscriberAuxiliaryService pipeline doesn't allow the Home.select operation.  Use the Home.find operartion.");

    }
}
