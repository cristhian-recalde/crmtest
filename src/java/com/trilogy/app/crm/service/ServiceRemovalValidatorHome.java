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
package com.trilogy.app.crm.service;

import java.util.Collection;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Validates whether it is safe to remove the Service from CRM.
 * 
 * @author angie.li@redknee.com
 *
 */
public class ServiceRemovalValidatorHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ServiceRemovalValidatorHome(Home delegate)
        throws HomeException
    {
        super(delegate);
    }
    
    /**
     * We only do the check for Service references during delete.  
     * Updates to services should still be available even if there are active
     * Service References.
     */
    public void remove(Context context, Object obj) throws HomeException
    {
        Service service = (Service) obj;
        
        long serviceId = service.getID();
        
        Home subServiceHome = (Home) context.get(SubscriberServicesHome.class);
        
        And filter = new And();
        filter.add(new EQ(SubscriberServicesXInfo.SERVICE_ID, new Long(serviceId)));
        filter.add(new Limit(1));
        /* No need to check for the states because if the SubscriberService record exists
         * that means the service can potentially be used.  Deactivated subscribers delete 
         * the SubscriberServices records */
        
        try
        {
            Collection serviceInUse =  subServiceHome.where(context, filter).selectAll();    
            
            if (serviceInUse.size() > 0)
            {
                throw new HomeException("Cannot Delete a Service that is currently in Use by Subscribers.");
            }
        }
        catch (HomeException e)
        {
            HomeException te = new HomeException(
                    "Abort Delete Service. " + e.getMessage());
            te.initCause(e);
            throw te;
        }
        
        getDelegate(context).remove(context, obj);
    }

}
