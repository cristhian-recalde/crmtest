package com.trilogy.app.crm.bean.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.service.ExternalAppMapping;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.extension.service.ServiceExtension;
import com.trilogy.app.crm.extension.service.ServiceExtensionXInfo;
import com.trilogy.app.crm.extension.service.ServiceNExtension;
import com.trilogy.app.crm.extension.service.ServiceNExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppMappingSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xlog.log.LogSupport;

public class ServiceProduct extends AbstractServiceProduct {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int getSpid() {

		Context ctx = getContext();
		Product service = null;
		try{
			And filter = new And();
			filter.add(new EQ(ProductXInfo.PRODUCT_ID,getIdentifier()));
			service = HomeSupportHelper.get(ctx).findBean(ctx, Product.class, filter);
		
		} catch (Exception e) {
			LogSupport.minor(ctx, this,"Can not find Product " + e.getMessage());
		}
		return service.getSpid();
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
                    ServiceProduct serviceCopy = (ServiceProduct) this.clone();
                    serviceCopy.setServiceExtensions(new ArrayList());
                    
                    serviceCopy = (ServiceProduct) new ExtensionLoadingAdapter<ServiceNExtension>(ServiceNExtension.class, ServiceNExtensionXInfo.SERVICE_ID).adapt(ctx, serviceCopy);
                    serviceCopy = (ServiceProduct) new ExtensionSpidAdapter().adapt(ctx, serviceCopy);
                    
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
    
	/**
	 * {@inheritDoc}
	 */
	public long getIdentifier() {
		return getProductId();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIdentifier(long id) {
		setProductId(id);
	}

	@Override
	public PropertyInfo getExtensionHolderProperty() {
		return ServiceProductXInfo.SERVICE_EXTENSIONS;
	}

	@Override
	public Collection<Class> getExtensionTypes() {
		final Context ctx = ContextLocator.locate();
		Set<Class<ServiceNExtension>> extClasses = ExtensionSupportHelper.get(
				ctx).getRegisteredExtensions(ctx, ServiceNExtension.class);

		Collection<Class> desiredClass = new ArrayList<Class>();
		for (Class<ServiceNExtension> ext : extClasses) {
			desiredClass.add(ext);
		}
		return desiredClass;

	}

	@Override
	public Collection<Extension> getExtensions() {
		Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>) getExtensionHolderProperty()
				.get(this);
		return ExtensionSupportHelper.get(getContext()).unwrapExtensions(
				holders);
	}

	public Context getContext() {
		if(ctx_==null)
			ctx_ = ContextLocator.locate();
		return ctx_;
	}

	public void setContext(final Context context) {
		ctx_ = context;
	}
	
	@Override
	public String getRootPermission() {
		return  ROOT_PERMISSION + "." + getSpid() + "." + getType().getIndex() + "." + getIdentifier();
	}

	private transient Context ctx_;
	public static String ROOT_PERMISSION = "app.crm.service";

}
