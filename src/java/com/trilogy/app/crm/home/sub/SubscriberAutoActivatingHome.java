/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * 
 * @author Aaron Gourley
 * @since 
 *
 */
public class SubscriberAutoActivatingHome extends HomeProxy
{

    public SubscriberAutoActivatingHome()
    {
        super();
    }


    public SubscriberAutoActivatingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    public SubscriberAutoActivatingHome(Context ctx)
    {
        super(ctx);
    }


    public SubscriberAutoActivatingHome(Home delegate)
    {
        super(delegate);
    }


    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        // OID 36169 - Subscriber's in prepaid group pooled accounts are always created in 'Active' state.
        // This had to be done in 2 steps because ABM will only perform a first call activation if the subscriber is in the 'Available' state.
        // See com.redknee.app.crm.home.sub.SubscriberStateProfileChangeHome for FCA initiating logic
        return activateIfAvailable(ctx, (Subscriber)super.create(ctx, obj));
    }


    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        // TT7082400017 - Mike Bijelich internal note @ Tue Aug 28 11:10:49 EDT 2007
        // Let the user select 'Available', but when the pipeline is executed, it detects that 
        // the subscriber should be put into the 'Active' state eventually, so it actually 
        // goes from Pending -> Available -> Active (eventhough to the user they only 
        // selected Available).
        return activateIfAvailable(ctx, (Subscriber)super.store(ctx, obj));
    }


    private Object activateIfAvailable(Context ctx, Subscriber subscriber) throws HomeException, HomeInternalException
    {
        if (subscriber.isPrepaid() && subscriber.isPooled(ctx)
             && subscriber.getAccount(ctx).isPrepaid() )
        {
            // Reload subscriber just to be safe
            Subscriber subToActivate = SubscriberSupport.lookupSubscriberForSubId(ctx, subscriber.getId());
            if (SubscriberStateEnum.AVAILABLE.equals(subToActivate.getState()))
            {
                // Set state to ACTIVE and store the updated subscriber
                subToActivate.setState(SubscriberStateEnum.ACTIVE);
                super.store(ctx, subToActivate);
            }
        }
        return subscriber;
    }
}
