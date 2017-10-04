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

import java.util.Collection;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.account.AccountExtensionXInfo;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 7.4.16
 */
public class SubscriberGroupPricePlanHome extends HomeProxy
{

    public SubscriberGroupPricePlanHome(Home delegate)
    {
        super(delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        obj = super.create(ctx, obj);
        updateGroupPricePlanAccountExtension(ctx, obj);
        return obj;
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        obj = super.store(ctx, obj);
        updateGroupPricePlanAccountExtension(ctx, obj);
        return obj;
    }

    private void updateGroupPricePlanAccountExtension(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        if( obj instanceof Subscriber )
        {
            Subscriber sub = (Subscriber)obj;
            if( sub.isPooledGroupLeader(ctx) )
            {
                // Update the Group Price Plan account extension
                Collection<GroupPricePlanExtension> extensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, GroupPricePlanExtension.class, new EQ(AccountExtensionXInfo.BAN, sub.getBAN()));
                for( GroupPricePlanExtension extension : extensions )
                {
                    Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, extension);
                    if( extensionHome != null )
                    {
                        // Update the extension.  This will trigger installable extension update() logic.
                        if( sub.isPrepaid() )
                        {
                            if( extension.getPrepaidPricePlanID() != sub.getPricePlan() )
                            {
                                extension.setPrepaidPricePlanID(sub.getPricePlan());
                                extensionHome.store(ctx, extension);   
                            }
                        }
                        else
                        {
                            if( extension.getPostpaidPricePlanID() != sub.getPricePlan() )
                            {
                                extension.setPostpaidPricePlanID(sub.getPricePlan());
                                extensionHome.store(ctx, extension);   
                            }
                        }
                    }
                    else
                    {
                        ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
                        if (el != null)
                        {
                            el.thrown(new IllegalArgumentException("Extension not supported: " + (extension != null ? extension.getClass().getName() : null)));
                        }
                    }
                }
            }
        }
    }

}
