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
package com.trilogy.app.crm.move.request.factory;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.subscriber.MoveSubscriptionExtensionHolder;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequest;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SubscriptionExtensionMoveRequestFactory
{

    static MoveRequest getInstance(Context ctx, Subscriber subscription)
    {
        SubscriptionExtensionMoveRequest request = null;

        if (subscription != null
                && subscription.getSubExtensions(ctx) != null
                && subscription.getSubExtensions(ctx).size() > 0)
        {   
            request = new SubscriptionExtensionMoveRequest();
            
            request.setOldSubscriptionId(subscription);
            
            // Add a copy of each extension to the move request
            List<ExtensionHolder> subExtensions = request.getOriginalSubscription(ctx).getSubExtensions(ctx);
            for (ExtensionHolder extensionHolder : subExtensions)
            {
                if (extensionHolder != null && extensionHolder.getExtension() != null)
                {
                    addExtensionToRequest(request, extensionHolder.getExtension());
                }
            }
        }
        
        return request;
    }

    static MoveRequest getInstance(Context ctx, SubscriberExtension extension)
    {
        SubscriptionExtensionMoveRequest request = null;

        if (extension != null)
        {
            request = new SubscriptionExtensionMoveRequest();
            
            request.setOldSubscriptionId(extension.getSubId());
            addExtensionToRequest(request, extension);
        }
        
        return request;
    }

    private static void addExtensionToRequest(SubscriptionExtensionMoveRequest request, Extension extension)
    {
        ExtensionHolder newHolder = new MoveSubscriptionExtensionHolder();
        newHolder.setExtension(extension);
        
        List<ExtensionHolder> extensions = request.getSubscriptionExtensions();
        if (extensions == null)
        {
            extensions = new ArrayList<ExtensionHolder>();
        }
        else
        {
            extensions = new ArrayList<ExtensionHolder>(extensions);
        }
        
        extensions.add(newHolder);

        // Trigger property change
        request.setSubscriptionExtensions(extensions);
    }
}
