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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionBANAdapter;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Subscriber advanced features object, used to show the subscriber extensions
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class SubscriberAdvancedFeatures extends AbstractSubscriberAdvancedFeatures
{
    protected transient Context context_ = null;
    
    public Context getContext()
    {
        return context_;
    }
    
    public void setContext(Context context)
    {
        context_ = context;
    }
    
    public PropertyInfo getExtensionHolderProperty()
    {
        return SubscriberAdvancedFeaturesXInfo.SUBSCRIBER_EXTENSIONS;
    }
    
    
    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>)getExtensionHolderProperty().get(this);
        return ExtensionSupportHelper.get(getContext()).unwrapExtensions(holders);
    }

    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        Set<Class<SubscriberExtension>> extClasses = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(ctx,
                SubscriberExtension.class);
        
        Collection<Class> desiredClass = new ArrayList<Class>();
        for (Class<SubscriberExtension> ext : extClasses)
        {
            desiredClass.add(ext);
        }
        return desiredClass;
    }
    
    @Override
    public void setSubscriberExtensions(List subscriberExtensions)
    throws IllegalArgumentException
    {
        setSubscriberExtensions(getContext(), subscriberExtensions);
    }

    public void setSubscriberExtensions(Context ctx, List subscriberExtensions)
    throws IllegalArgumentException
    {
        getParentBean(ctx).setSubExtensions(subscriberExtensions);
    }

    @Override
    public List getSubscriberExtensions()
    {
        return getSubscriberExtensions(getContext());
    }
    
    
    public List getSubscriberExtensions(Context ctx)
    {
        return (List) getParentBean(ctx).getSubExtensions(ctx);
/*        synchronized (this)
        {
            if (super.getSubscriberExtensions() == null)
            {
                try
                {
                    // To avoid deadlock, use a subscription "with extensions loaded" along with extension loading adapter.
                    SubscriberAdvancedFeatures subCopy = (SubscriberAdvancedFeatures) this.clone();
                    subCopy.setSubscriberExtensions(new ArrayList());
                    
                    subCopy = (SubscriberAdvancedFeatures) new ExtensionLoadingAdapter<SubscriberExtension>(SubscriberExtension.class, SubscriberExtensionXInfo.SUB_ID).adapt(ctx, subCopy);
                    subCopy = (SubscriberAdvancedFeatures) new ExtensionBANAdapter().adapt(ctx, subCopy);
                    subCopy = (SubscriberAdvancedFeatures) new ExtensionSpidAdapter().adapt(ctx, subCopy);
                    
                    this.setSubscriberExtensions(subCopy.getSubscriberExtensions(ctx));
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.");
                    LogSupport.debug(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.", e);
                }
            }
        }
        
        return super.getSubscriberExtensions(); */
    }    
    
    public Subscriber getParentBean()
    {
        return getParentBean(getContext());
    }

    public Subscriber getParentBean(Context ctx)
    {
        return (Subscriber) ctx.get(Subscriber.class);
    }
    
}
