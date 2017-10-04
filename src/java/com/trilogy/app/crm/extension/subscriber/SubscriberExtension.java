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
package com.trilogy.app.crm.extension.subscriber;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class SubscriberExtension extends AbstractSubscriberExtension
{

    /**
     * {@inheritDoc}
     */
    public String getSummary(Context ctx)
    {
        return "Subscriber ID=" + this.getSubId();
    }

    /**
     * @param ctx The operating context
     * @return This extension's subscription
     */
    public Subscriber getSubscriber(Context ctx)
    {
        Subscriber subscriber = BeanLoaderSupportHelper.get(ctx).getBean(ctx, Subscriber.class);
        if( subscriber != null 
                && (AbstractSubscriberExtension.DEFAULT_SUBID.equals(this.getSubId())
                        || SafetyUtil.safeEquals(subscriber.getId(), this.getSubId())) )
        {
            return subscriber;
        }
        
        if( AbstractSubscriberExtension.DEFAULT_SUBID.equals(this.getSubId()) )
        {
            return null;
        }
        
        try
        {
            subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, this.getSubId());
        }
        catch (HomeException e)
        {
        } 
        
        if( subscriber != null && SafetyUtil.safeEquals(subscriber.getId(), this.getSubId()) )
        {
            return subscriber;
        }
        
        return null;
    }
}
