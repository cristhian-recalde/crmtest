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
package com.trilogy.app.crm.bean;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.billing.message.BillingMessageAdapter;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.extension.spid.SpidExtension;
import com.trilogy.app.crm.extension.spid.SpidExtensionXInfo;
import com.trilogy.app.crm.home.CRMSpidPropertyListeners;
import com.trilogy.app.crm.home.MsisdnPortHandlingHome;
import com.trilogy.app.crm.home.account.AccountPropertyListeners;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.messages.MessageConfigurationSupport;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.MapVisitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Concrete class for model's custom java code
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class CRMSpid extends AbstractCRMSpid implements LazyLoadBean
{
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use contextualized version of method.
     */
    @Deprecated
    @Override
    public ChargingComponentsConfig getChargingComponentsConfig()
    {
        return getChargingComponentsConfig(getContext());
    }

    public com.redknee.app.crm.bean.core.ChargingComponentsConfig getChargingComponentsConfig(Context ctx)
    {
        ChargingComponentsConfig version = super.getChargingComponentsConfig();

        try
        {
            // Adapt between business logic bean and data bean
            return (com.redknee.app.crm.bean.core.ChargingComponentsConfig) new ExtendedBeanAdapter<com.redknee.app.crm.bean.ChargingComponentsConfig, com.redknee.app.crm.bean.core.ChargingComponentsConfig>(
                    com.redknee.app.crm.bean.ChargingComponentsConfig.class, 
                    com.redknee.app.crm.bean.core.ChargingComponentsConfig.class).adapt(ctx, version);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, e.getClass().getSimpleName() + " occurred in " + CRMSpid.class.getSimpleName() + ".getChargingComponentsConfig(): " + e.getMessage(), e).log(ctx);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getSpid()
    {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    public void setSpid(int spid)
    {
        setId(spid);
    }

    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        if (context_ == null)
        {
            return ContextLocator.locate();
        }
        return context_;
    }

    /**
     * {@inheritDoc}
     */
    public void setContext(final Context context)
    {
        context_ = context;
    }

    /**
     * {@inheritDoc}
     */
    public long getIdentifier()
    {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    public void setIdentifier(long ID) throws IllegalArgumentException
    {
        setId((int)ID );
    }

    /**
     * {@inheritDoc}
     */
    public MessageConfigurationSupport getConfigurationSupport(Context ctx)
    {
        return FrameworkSupportHelper.get(ctx).getConfigurationSupport(ctx);
    }
    
    
    public boolean isDormancyEnabled(Context ctx)
    {
            return (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.DORMANT_STATE_LICENSE_KEY)) && isEnableDormancy();
    }
    
    
    public PropertyInfo getExtensionHolderProperty()
    {
        return CRMSpidXInfo.SPID_EXTENSIONS;
    }
    
    
    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>)getExtensionHolderProperty().get(this);
        return ExtensionSupportHelper.get(getContext()).unwrapExtensions(holders);
    }
    
    
    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        Set<Class<SpidExtension>> extClasses = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(ctx,
                SpidExtension.class);
        Collection<Class> desiredClass = new ArrayList<Class>();
        for (Class<SpidExtension> ext : extClasses)
        {
            desiredClass.add(ext);
        }
        return desiredClass;
    }
    
    /**
     * Lazy loading extensions.
     * {@inheritDoc}
     */
    @Override
    public List getSpidExtensions()
    {
        synchronized (this)
        {
            if (super.getSpidExtensions() == null)
            {
                final Context ctx = getContext();
                try
                {
                    // To avoid deadlock, use a spid "with extensions loaded" along with extension loading adapter.
                    CRMSpid spidCopy = (CRMSpid) this.clone();
                    spidCopy.setSpidExtensions(new ArrayList());
                    
                    spidCopy = (CRMSpid) new ExtensionLoadingAdapter<SpidExtension>(SpidExtension.class, SpidExtensionXInfo.SPID).adapt(ctx, spidCopy);
                    spidCopy = (CRMSpid) new ExtensionSpidAdapter().adapt(ctx, spidCopy);
                    
                    this.setSpidExtensions(spidCopy.getSpidExtensions());
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.");
                    LogSupport.debug(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.", e);
                }
            }
        }
        
        return super.getSpidExtensions();
    }

    @Override
    public List getBillingMessages()
    {
        synchronized (this)
        {
            if (billingMessages_ == null)
            {
                try
                {
                    BillingMessageAdapter.instance().adapt(getContext(), this);
                }
                catch (HomeException e)
                {
                    LogSupport.minor(getContext(), this, "Unable to load Billing Message for Service Provider ["
                            + this.getId() + "]. Error: " + e.getMessage(), e);
                }
            }
        }
        Collection<BillingMessage> existingRecords =  super.getBillingMessages();

        ArrayList<SpidBillingMessage> l = new ArrayList<SpidBillingMessage>(existingRecords.size());
        for (BillingMessage record : existingRecords)
        {
        	SpidBillingMessage msg = new SpidBillingMessage();
        	msg.setActive(record.getActive());
        	msg.setIdentifier(record.getIdentifier());
        	msg.setLanguage(record.getLanguage());
        	msg.setMessage(record.getMessage());
        	msg.setSpid(record.getSpid());
            l.add(msg);
        }
        return l;
    }

    /**
     * MSISDNs may need to be created on fly for ported in cases
     */
    @Override
    public boolean getAutoCreateMSISDN()
    {
        Context ctx = ContextLocator.locate();
        if(null != ctx & ctx.has(MsisdnPortHandlingHome.MSISDN_PORT_KEY))
        {
            return true;
        }
        return super.getAutoCreateMSISDN();
    }
    
    public void saveBillingMessages(final Context ctx)
    {
        synchronized (this)
        {
            if (billingMessages_ != null)
            {
                try
                {
                    BillingMessageAdapter.instance().unAdapt(ctx, this);
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to save Billing Message for Service Provider ["
                            + this.getId() + "]. Error: " + e.getMessage(), e);
                }
            }
        }
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
        CRMSpid clone = (CRMSpid) super.clone();
        clone = (CRMSpid) cloneLazyLoadMetaData(clone);
        return cloneSpidExtensionList(clone);
    }

	/**
	 * @{inheritDoc
	 */
	@Override
	public Object deepClone() throws CloneNotSupportedException
	{
		final CRMSpid clone = (CRMSpid) super.deepClone();
		return cloneLazyLoadMetaData(clone);
	}

	
    private CRMSpid cloneSpidExtensionList(final CRMSpid clone) throws CloneNotSupportedException
    {
        List spidExtensions = super.getSpidExtensions();
        if (spidExtensions != null)
        {
            final List extentionList = new ArrayList(spidExtensions.size());
            clone.setSpidExtensions(extentionList);
            for (final Iterator it = spidExtensions.iterator(); it.hasNext();)
            {
                extentionList.add(safeClone((XCloneable) it.next()));
            }
        }
        return clone;
    }

    
    public void CreateCipherKey(Context ctx)
    throws HomeException
    {
    	Home home = (Home) ctx.get(CipherKeyHome.class); 
    	
    	CipherKey key = new CipherKey(); 
    	key.setSpid(this.getId());
    	key.setKeyLength(this.getCipherKeyLength());
    	key.setDescription(this.getCipherDescription()); 
    	key.setKeyType(this.getCipherKeyType()); 
    	
    	home.create(ctx, key);
     }
    
	@Override
	public String getCipherDescription() 
	{
		
		synchronized (this)
		{
			if (!this.CipherKeyLoaded_)
			{
				lazyLoadCipherKey();
			}
		}
		
		return super.getCipherDescription();
	}

	@Override
	public int getCipherKeyLength() {
		synchronized (this)
		{
			if (!this.CipherKeyLoaded_)
			{
				lazyLoadCipherKey();
			}
		}
		return super.getCipherKeyLength();
	}

	@Override
	public int getCipherKeyType() {
		synchronized (this)
		{
			if (!this.CipherKeyLoaded_)
			{
				lazyLoadCipherKey();
			}
		}
		return super.getCipherKeyType();
	}


	private void lazyLoadCipherKey()
	{
		Home home = (Home) this.getContext().get(CipherKeyHome.class);
		try
		{
			CipherKey key = (CipherKey) home.find(this.getContext(), new Integer(this.getId()));
			if (key != null)
			{
				this.setCipherDescription(key.getDescription());
				this.setCipherKeyLength(key.getKeyLength());
				this.setCipherKeyType(key.getKeyType()); 
				this.setCipherKeyLoaded(true); 
			}
		} catch (Throwable t)
		{
			new MajorLogMsg(this, "fail to load cipher key", t).log(this.getContext());
		}
	}
	
	
	
	@Override
	/**
	 * whence set it is final
	 */
	public void setCipherKeyLength(int cipherKeyLength)
			throws IllegalArgumentException 
	{
		
		if(!this.CipherKeyLoaded_)
		super.setCipherKeyLength(cipherKeyLength);
	}

	@Override
	/**
	 * when set it is final
	 */
	public void setCipherKeyType(int cipherKeyType)
			throws IllegalArgumentException 		
	{
		if(!this.CipherKeyLoaded_)
		super.setCipherKeyType(cipherKeyType);
	}

	/**
	 * Helper function that returns Msisdn Group map of SubscriptionType, MsisdnGroup.
	 * 
	 * 
	 * @param subscriptionType
	 * @return
	 *   null if mapping is not defined
	 */
    @SuppressWarnings("unchecked")
    public Integer getDefaultSubTypeMsisdnGroupId(long subscriptionType)
	{
        Map<Long, DefaultSubTypeMsisdnGroup> msisdnGroups = this.getDefaultSubTypeMsisdnGroups();
        DefaultSubTypeMsisdnGroup defaultMsisdnGroup = msisdnGroups.get(subscriptionType);
        if (defaultMsisdnGroup != null)
        {
            return defaultMsisdnGroup.getMsisdnGroup();
        }
	    return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean lazyLoad(Context ctx, PropertyInfo property)
	{
		if (property != null)
		{
			if (isFrozen())
			{
				new InfoLogMsg(this, "Unable to lazy-load "
				    + property.getBeanClass().getSimpleName() + "."
				    + property.getName() + " because CRMSpid " + this.getId()
				    + " is frozen.", null).log(ctx);
			}
			else if (property.getBeanClass().isAssignableFrom(this.getClass()))
			{
				PMLogMsg pm =
				    new PMLogMsg(LazyLoadBean.class.getName(), this.getClass()
				        .getSimpleName()
				        + ".lazyLoad("
				        + property.getName()
				        + ")");
				try
				{
					property.get(this);
					return true;
				}
				catch (Throwable t)
				{
					ExceptionListener el =
					    (ExceptionListener) ctx.get(ExceptionListener.class);
					if (el != null)
					{
						el.thrown(new IllegalPropertyArgumentException(
						    property, t.getMessage()));
					}
					new MinorLogMsg(this, "Error occured lazy-loading "
					    + property.getBeanClass().getSimpleName() + "."
					    + property.getName() + ": " + t.getMessage(), t)
					    .log(ctx);
				}
				finally
				{
					pm.log(ctx);
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean lazyLoadAllProperties(Context ctx)
	{
		PMLogMsg pm =
		    new PMLogMsg(LazyLoadBean.class.getName(),
		        Account.class.getSimpleName() + ".lazyLoadAllProperties()");

		Context sCtx = ctx.createSubContext();

		try
		{
			for (PropertyInfo property : getLazyLoadedProperties(sCtx))
			{
				lazyLoad(sCtx, property);
			}

			return true;
		}
		catch (Throwable t)
		{
			ExceptionListener el =
			    (ExceptionListener) sCtx.get(ExceptionListener.class);
			if (el != null)
			{
				el.thrown(t);
			}
			new MinorLogMsg(this,
			    "Error occured lazy-loading properties for spid "
			        + this.getId() + ": " + t.getMessage(), t).log(sCtx);
		}

		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public static Collection<PropertyInfo> getLazyLoadedProperties(Context ctx)
	{
		if (lazyLoadedProperties_ == null)
		{
			lazyLoadedProperties_ =
			    CRMSpidPropertyListeners.getLazyLoadedProperties();

			if (lazyLoadedProperties_ == null)
			{
				lazyLoadedProperties_ = new HashSet<PropertyInfo>();
			}
			lazyLoadedProperties_ =
			    Collections.unmodifiableSet(lazyLoadedProperties_);
		}
		return lazyLoadedProperties_;
	}

	/**
	 * A list of security questions and answers that can be asked to identify a
	 * subscriber.
	 **/
	@Override
	public Set getSubscriberLanguages()
	{
		synchronized (this)
		{
			if (!getSubscriberLanguagesLoaded())
			{
				lazyLoadSubscriberLanguages();
			}
		}
		return super.getSubscriberLanguages();
	}

	/**
	 * A list of security questions and answers that can be asked to identify a
	 * subscriber.
	 **/
	@Override
	public void setSubscriberLanguages(Set subscriberLanguages)
	{
		synchronized (this)
		{
			if (!getSubscriberLanguagesLoaded())
			{
				lazyLoadSubscriberLanguages();
			}
		}
		super.setSubscriberLanguages(subscriberLanguages);
	}
	
	protected synchronized void lazyLoadSubscriberLanguages()
	{
		final Context ctx = getContext();
        Home langHome = (Home) ctx.get(SpidLangHome.class);
        
		if (langHome == null)
		{
			// SecurityQuestionAndAnswer home is not yet available
			LogSupport
			    .minor(
			        ctx,
			        this,
			        "SubscriberLanguages Home is not available yet. SubscriberLanguages info NOT loaded.");
			setSubscriberLanguagesLoaded(true);
			return;
		}
		try
		{
	        final SortedSet selectedSet = new TreeSet();
	        Collection lang = (Collection) langHome.where(ctx, new EQ(SpidLangXInfo.SPID, Integer.valueOf(this.getId()))).forEach(ctx,
	                new MapVisitor(SpidLangXInfo.LANGUAGE));
	        selectedSet.addAll(lang);
	        this.subscriberLanguages_ = selectedSet;
			setSubscriberLanguagesLoaded(true);
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, this,
			    "Unable to load SubscriberLanguages Info", e);
		}
	}

	/**
	 * Adds PropertyChangeListener so the property changes will be watch for
	 * further
	 * changes.
	 */
	public void watchLazyLoadedProperitesChange()
	{
		if (spidPropertyWatch_ == null)
		{
			spidPropertyWatch_ = new CRMSpidPropertyListeners();
		}
		this.addPropertyChangeListener(spidPropertyWatch_);
	}

	/**
	 * Adds PropertyChangeListener so the property changes will be watch for
	 * further
	 * changes.
	 */
	public PropertyChangeListener getCRMSpidLazyLoadedPropertyListener()
	{
		if (spidPropertyWatch_ == null)
		{
			spidPropertyWatch_ = new CRMSpidPropertyListeners();
		}
		return spidPropertyWatch_;
	}

	/**
	 * Removes PropertyChangeListener so the property changes will not be
	 * watched for further changes.
	 */
	public void stopLazyLoadedProperitesChange()
	{
		this.removePropertyChangeListener(spidPropertyWatch_);
	}
	
	/**
	 * @param clone
	 * @return
	 */
	private Object cloneLazyLoadMetaData(final CRMSpid clone)
	{
		clone.spidPropertyWatch_ = new CRMSpidPropertyListeners();
		if (this.spidPropertyWatch_ != null)
		{
			((CRMSpidPropertyListeners) this.spidPropertyWatch_)
			    .cloneLazyLoadMetaData((CRMSpidPropertyListeners) clone.spidPropertyWatch_);
			clone.watchLazyLoadedProperitesChange();
		}
		return clone;
	}


	private static Set<PropertyInfo> lazyLoadedProperties_;


	private PropertyChangeListener spidPropertyWatch_ = null;

	/**
    * The operating context.
    */
    protected transient Context context_;
}
