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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Update the subscriber expiry date based on value populated in FixedStopPricePlanSubExtension.
 * This is required since the expiry date needs to be updated before the DB operation
 * and the external date needs to update after it has been created.
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.2
 *
 */
public class FixedStopPricePlanBalanceExpiryHome extends HomeProxy
{


    public FixedStopPricePlanBalanceExpiryHome(Context context, Home home)
    {
        super(context, home);
    }
    
 
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Subscriber sub = (Subscriber)obj;
        
        
        FixedStopPricePlanSubExtension extension = ExtensionSupportHelper.get(ctx).getExtension(ctx, sub,  FixedStopPricePlanSubExtension.class);
        
        //pre-create
        setSubscriberExpiry(sub, extension);
        
        sub = (Subscriber) super.create(ctx, sub);
        
        //post-store
        updateExternalSubscriptionProfile(ctx, sub, extension);
        
        return sub;
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Subscriber sub = (Subscriber)obj;
        
        
        FixedStopPricePlanSubExtension extension = ExtensionSupportHelper.get(ctx).getExtension(ctx, sub,  FixedStopPricePlanSubExtension.class);
        
        //pre-store
        setSubscriberExpiry(sub, extension);
        
        sub = (Subscriber) super.store(ctx, sub);
        
        //post-post
        updateExternalSubscriptionProfile(ctx, sub, extension);
        
        return sub;
    }


    private void updateExternalSubscriptionProfile(Context ctx, Subscriber sub, FixedStopPricePlanSubExtension extension)
    {
        if(extension != null && extension.getBalanceExpiry() != null)
        {
            sub.setExpiryDate(extension.getBalanceExpiry());
            
            try
            {
                SubscriberSupport.updateExpiryDateSubscriptionProfile(ctx, sub);
            }
            catch (HomeException e)
            {
                final String msg = "Unable to set balance expiry subscriber [" + sub.getId() + "]. Cause: " + e.getMessage();
                new MinorLogMsg(this, msg, e).log(ctx);
            }
        }
    }


    private void setSubscriberExpiry(Subscriber sub, FixedStopPricePlanSubExtension extension)
    {
        if (extension != null && extension.getBalanceExpiry() != null)
        {
            sub.setExpiryDate(extension.getBalanceExpiry());
        }
    }

}
