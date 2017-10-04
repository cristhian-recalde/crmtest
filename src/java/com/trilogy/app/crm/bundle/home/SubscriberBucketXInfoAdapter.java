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
package com.trilogy.app.crm.bundle.home;

import java.io.IOException;

import com.trilogy.app.crm.bundle.RecurrenceOptions;
import com.trilogy.app.crm.bundle.RecurrenceOptionsXInfo;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.beans.xi.XInfoAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.product.bundle.manager.api.v21.SubscriberBucketApi;

/**
 * Adapts the CRM bucket to the RMI Bundle manager beans
 * All enums and beans the XInfo doesn't understand will be mapped here
 * @author arturo.medina@redknee.com
 *
 */
public class SubscriberBucketXInfoAdapter extends XInfoAdapter
{
    /**
     * Default constructor
     * @param source
     * @param destination
     */
    public SubscriberBucketXInfoAdapter(XInfo source, XInfo destination)
    {
        super(source, destination);
    }

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object source) throws HomeException
    {
        SubscriberBucket dest = (SubscriberBucket)super.adapt(ctx, source);
        
        SubscriberBucketApi profile = (SubscriberBucketApi) source;
        dest.setOptions((adaptOptions(ctx, profile.getOptions())));

        return dest;
    }

    /**
     * Adapts the Recurrence options from BM to CRM
     * @param options
     * @return
     */
    private RecurrenceOptions adaptOptions(Context ctx, 
            com.redknee.product.bundle.manager.api.v21.RecurrenceOptions options)
    {
        RecurrenceOptions crmOptions = null;
        try
        {
            crmOptions = (RecurrenceOptions) XBeans.copy(ctx, 
                    com.redknee.product.bundle.manager.api.v21.RecurrenceOptionsXInfo.instance(), 
                    options, 
                    RecurrenceOptionsXInfo.instance());
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, 
                    "InstantiationException when trying to get all the attributes on the RecurrenceOptions", e);
        }

        
        return crmOptions;
    }

    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object destination) throws HomeException
    {
        SubscriberBucketApi dest = (SubscriberBucketApi)super.unAdapt(ctx, destination);
        
        SubscriberBucket profile = (SubscriberBucket) destination;
        dest.setOptions((unAdaptOptions(ctx, profile.getOptions())));
        return dest;
    }
    
    private com.redknee.product.bundle.manager.api.v21.RecurrenceOptions unAdaptOptions(
            Context ctx, RecurrenceOptions options)
    {
        com.redknee.product.bundle.manager.api.v21.RecurrenceOptions bmOptions = null;
        try
        {
            bmOptions = (com.redknee.product.bundle.manager.api.v21.RecurrenceOptions)
                    XBeans.copy(ctx, RecurrenceOptionsXInfo.instance(),options, 
                    com.redknee.product.bundle.manager.api.v21.RecurrenceOptionsXInfo.instance());
        }
        catch (Exception e)
        {     
            LogSupport.major(ctx, this, "InstantiationException when trying to get all the attributes on the RecurrenceOptions", e);
        }
        return bmOptions;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6381752242787260475L;

}
