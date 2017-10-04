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
package com.trilogy.app.crm.bean.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.service.ExternalAppMapping;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.extension.service.ServiceExtension;
import com.trilogy.app.crm.extension.service.ServiceExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppMappingSupportHelper;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Concrete service class.
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class Service extends AbstractService
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public long getIdentifier()
    {
        return getID();
    }

    /**
     * {@inheritDoc}
     */
    public void setIdentifier(long id)
    {
        setID(id);
    }

    /**
     * {@inheritDoc}
     */
    public SubscriptionType getSubscriptionType(Context ctx)
    {
        if (this.getSubscriptionType() != cachedSubTypeID_)
        {
            synchronized(CACHED_SUBSCRIPTION_TYPE_LOCK)
            {
                try
                {
                    cachedSubTypeObj_ = SubscriptionType.getSubscriptionTypeWithException(ctx, this.getSubscriptionType());
                    cachedSubTypeID_ = this.getSubscriptionType();
                }
                catch (HomeException e)
                {
                    String msg = "Unable to retreive SubscriptionType ID=" + getSubscriptionType();
                    new DebugLogMsg(this, msg, e).log(ctx);
                    new MinorLogMsg(this, msg + ": " + e.getMessage(), null).log(ctx);
                }
            }
        }

        return cachedSubTypeObj_;
    }
    
    public List<ExtensionHolder> getExtensionHolders()
    {
        return serviceExtensions_;
    }

    /**
     * Lazy loading extensions.
     * {@inheritDoc}
     */
    public List getServiceExtensions()
    {
        synchronized (this)
        {
            if (getExtensionHolders() == null)
            {
                final Context ctx = getContext();
                try
                {
                    // To avoid deadlock, use a service "with extensions loaded" along with extension loading adapter.
                    Service serviceCopy = (Service) this.clone();
                    serviceCopy.setServiceExtensions(new ArrayList());
                    
                    serviceCopy = (Service) new ExtensionLoadingAdapter<ServiceExtension>(ServiceExtension.class, ServiceExtensionXInfo.SERVICE_ID).adapt(ctx, serviceCopy);
                    serviceCopy = (Service) new ExtensionSpidAdapter().adapt(ctx, serviceCopy);
                    
                    this.setServiceExtensions(serviceCopy.getServiceExtensions());
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.");
                    LogSupport.debug(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.", e);
                }
            }
            else
            {
                for (ExtensionHolder holder : getExtensionHolders())
                {
                    if (holder.getExtension() instanceof SpidAware && this.getSpid()>0)
                    {
                        ((SpidAware) holder.getExtension()).setSpid(this.getSpid());
                    }
                    
                }
            }
        }
        
        return getExtensionHolders();
    }
    
    /**
     * Handler is a transient field dependent on the Service Type.
     * Default to "Generic".
     *
     * @return Type of service.
     */
    @Override
    public String getHandler()
    {
        //we need a lookup in the context here.
        ExternalAppMapping record = null;
        try
        {
            record = ExternalAppMappingSupportHelper.get(getContext()).getExternalAppMapping(getContext(), getType());
        }
        catch (Throwable e)
        {
            LogSupport.major(getContext(), this, "ExternalAppMapping record does not exist for Service Type="
                        + getType().getIndex() + ". Add this configuration.", e);
        }
        return (record != null ? record.getHandler() : "Generic");
    }
    
    
    public PropertyInfo getExtensionHolderProperty()
    {
        return ServiceXInfo.SERVICE_EXTENSIONS;
    }
    
    
    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>)getExtensionHolderProperty().get(this);
        return ExtensionSupportHelper.get(getContext()).unwrapExtensions(holders);
    }
    
    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        Set<Class<ServiceExtension>> extClasses = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(ctx,
                ServiceExtension.class);
        
        Collection<Class> desiredClass = new ArrayList<Class>();
        for (Class<ServiceExtension> ext : extClasses)
        {
            desiredClass.add(ext);
        }
        return desiredClass;

    }
    
    /**
     * Adding cloning functionality to clone added fields.
     *
     * @return the clone object
     * @throws CloneNotSupportedException should not be thrown
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Service clone = (Service) super.clone();
        return cloneServiceExtensionList(clone);
    }

    private Service cloneServiceExtensionList(final Service clone) throws CloneNotSupportedException
    {
        List serviceExtensions = super.getServiceExtensions();
        if (serviceExtensions != null)
        {
            final List extentionList = new ArrayList(serviceExtensions.size());
            clone.setServiceExtensions(extentionList);
            for (final Iterator it = serviceExtensions.iterator(); it.hasNext();)
            {
                extentionList.add(safeClone((XCloneable) it.next()));
            }
        }
        return clone;
    }
    
    /**
     * Check if the HTTP request is a result of a spid selection preview
     * page reload
     * 
     * @return true if screen refresh is due to a price plan related change in
     *         GUI.
     */
    public static boolean isFromWebNewOrPreviewOnSpid(final Context ctx)
    {
        if (ctx != null)
        {
            final HttpServletRequest req = (HttpServletRequest) ctx.get(HttpServletRequest.class);
            // If a preview is occurring as a result of a price plan selection
            // set the deposit and credit limit to that of the price plan
            if (req != null
                    && (WebController.isCmd("New", req) || WebController.isCmd("Preview", req)
                            && req.getParameter("PreviewButtonSrc") != null
                            && (req.getParameter("PreviewButtonSrc").indexOf(".spid") != -1)))
            {
                return true;
            }
        }
        return false;
    }
    

    @Override
    public int getTypeEnumIndex()
    {
        if (getType()!=null)
        {
            return getType().getIndex();
        }
        else
        {
            return -1;
        }
    }

    @Override
    public AbstractEnum getEnumType()
    {
        return getType();
    }

    public Context getContext()
    {
        return ctx_;
    }

    public void setContext(final Context context)
    {
        ctx_ = context;
    }

    private transient Context ctx_;

    private long cachedSubTypeID_;
    private SubscriptionType cachedSubTypeObj_;
    private static final Object CACHED_SUBSCRIPTION_TYPE_LOCK = new Object();
}
