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

import com.trilogy.app.crm.bean.ui.AbstractTechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.extension.service.ServiceExtension;
import com.trilogy.app.crm.extension.service.TechnicalServiceTemplateServiceExtension;
import com.trilogy.app.crm.extension.service.TechnicalServiceTemplateServiceExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xlog.log.LogSupport;


public class TechnicalServiceTemplate extends AbstractTechnicalServiceTemplate
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
                final Context ctx = ContextLocator.locate();
                try
                {// Service Id is template Id
                    
                	TechnicalServiceTemplate technicalServiceCopy = (TechnicalServiceTemplate) this.clone();
                    technicalServiceCopy.setServiceExtensions(new ArrayList());
                    
                	technicalServiceCopy = (TechnicalServiceTemplate) new ExtensionLoadingAdapter<TechnicalServiceTemplateServiceExtension>(TechnicalServiceTemplateServiceExtension.class, TechnicalServiceTemplateServiceExtensionXInfo.ID).adapt(ctx, technicalServiceCopy);
                    technicalServiceCopy = (TechnicalServiceTemplate) new ExtensionSpidAdapter().adapt(ctx, technicalServiceCopy);
                    
                   this.setServiceExtensions(technicalServiceCopy.getServiceExtensions());
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, "TechnicalServiceTemplate", "Exception occurred loading extensions. Extensions NOT loaded.", e);
                    
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
    
    public PropertyInfo getExtensionHolderProperty()
    {
        return TechnicalServiceTemplateXInfo.SERVICE_EXTENSIONS;
    }
    
    public void setServiceExtensions(List serviceExtensions)
			throws IllegalArgumentException {
		assertBeanNotFrozen();

		assertServiceExtensions(serviceExtensions);

		List old = this.serviceExtensions_;

		this.serviceExtensions_ = serviceExtensions;

		firePropertyChange("serviceExtensions", old, serviceExtensions);
	}
    
    private Collection getExtensionCollection(Context ctx){
		return serviceExtensions_;
    	
    }

	public void assertServiceExtensions(List serviceExtensions)
			throws IllegalArgumentException {
		if (!(serviceExtensions instanceof ArrayList))
			return;
		ArrayList l__ = (ArrayList) serviceExtensions;

		l__.trimToSize();
	}
	
	 public Object clone() throws CloneNotSupportedException
	    {
	        TechnicalServiceTemplate clone = (TechnicalServiceTemplate) super.clone();
	        return cloneServiceExtensionList(clone);
	    }

	    private TechnicalServiceTemplate cloneServiceExtensionList(final TechnicalServiceTemplate clone) throws CloneNotSupportedException
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
    public Context getContext()
    {
        return ctx_;
    }

    public void setContext(final Context context)
    {
        ctx_ = context;
    }
    private transient Context ctx_;
    protected List serviceExtensions_ = null;
 }
