/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.bean.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.BillingMessage;
import com.trilogy.app.crm.bean.CreditCategoryBillingMessage;
import com.trilogy.app.crm.bean.CreditCategoryBillingMessageID;
import com.trilogy.app.crm.billing.message.BillingMessageAdapter;
import com.trilogy.app.crm.billing.message.BillingMessageHomePipelineFactory;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.messages.MessageConfigurationSupport;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author cindy.wong@redknee.com
 * @since 8.4
 */
public class CreditCategory extends AbstractCreditCategory
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    // the IdentifierAware interface
    public long getIdentifier()
    {
        return (long) getCode();
    }

    public void setIdentifier(long ID) throws IllegalArgumentException
    {
        setCode((int) ID);
    }

    // from the BillingMessageAware interface
    public MessageConfigurationSupport getConfigurationSupport(Context ctx)
    {
        MessageConfigurationSupport<CreditCategoryBillingMessage, CreditCategoryBillingMessageID> support = (MessageConfigurationSupport<CreditCategoryBillingMessage, CreditCategoryBillingMessageID>) ctx
        .get(BillingMessageHomePipelineFactory
                .getBillingMessageConfigurationKey(CreditCategoryBillingMessage.class));
        return support;
    }

    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return context_;
    }

    /**
     * {@inheritDoc}
     */
    public void setContext(final Context context)
    {
        context_ = context;
    }

    public PropertyInfo getExtensionHolderProperty()
    {
        return CreditCategoryXInfo.CREDIT_CATEGORY_EXTENSIONS;
    }

    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>) getExtensionHolderProperty()
                .get(this);
        return ExtensionSupportHelper.get(getContext()).unwrapExtensions(holders);
    }
    
    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        Set<Class<CreditCategoryExtension>> extClasses = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(ctx,
                CreditCategoryExtension.class);
        Collection<Class> desiredClass = new ArrayList<Class>();
        for (Class<CreditCategoryExtension> ext : extClasses)
        {
            desiredClass.add(ext);
        }
        return desiredClass;
    }

    /**
     * Lazy loading extensions. {@inheritDoc}
     */
    @Override
    public List getCreditCategoryExtensions()
    {
        synchronized (this)
        {
            if (super.getCreditCategoryExtensions() == null)
            {
                final Context ctx = getContext();
                try
                {
                    /*
                     * To avoid deadlock, use a credit category
                     * "with extensions loaded" along with extension loading
                     * adapter.
                     */
                    CreditCategory categoryCopy = (CreditCategory) this.clone();
                    categoryCopy.setCreditCategoryExtensions(new ArrayList());

                    categoryCopy = (CreditCategory) new ExtensionLoadingAdapter<CreditCategoryExtension>(
                            CreditCategoryExtension.class,
                            CreditCategoryExtensionXInfo.CREDIT_CATEGORY)
                            .adapt(ctx, categoryCopy);

                    this.setCreditCategoryExtensions(categoryCopy
                            .getCreditCategoryExtensions());
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, this,
                            "Exception occurred loading extensions. Extensions NOT loaded.");
                    LogSupport.debug(ctx, this,
                            "Exception occurred loading extensions. Extensions NOT loaded.", e);
                }
            }
        }

        return super.getCreditCategoryExtensions();
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
                    LogSupport.minor(getContext(), this, "Unable to load Billing Message for Credit Category ["
                            + this.getCode() + "]. Error: " + e.getMessage(), e);
                }
            }
        }
        Collection<BillingMessage> existingRecords =  super.getBillingMessages();

        ArrayList<CreditCategoryBillingMessage> l = new ArrayList<CreditCategoryBillingMessage>(existingRecords.size());
        for (BillingMessage record : existingRecords)
        {
        	CreditCategoryBillingMessage msg = new CreditCategoryBillingMessage();
        	msg.setActive(record.getActive());
        	msg.setIdentifier(record.getIdentifier());
        	msg.setLanguage(record.getLanguage());
        	msg.setMessage(record.getMessage());
        	msg.setSpid(record.getSpid());
            l.add(msg);
        }
        return l; 
    }

    public void saveBillingMessages(final Context ctx)
    {
        synchronized (this)
        {
            LogSupport.debug(ctx, this, "FIX THIS: Wrong call flow. Should not be called for UI bean.",
                    new HomeException(""));
            if (billingMessages_ != null)
            {
                try
                {
                    BillingMessageAdapter.instance().unAdapt(ctx, this);
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to save Billing Message for Credit Category ["
                            + this.getCode() + "]. Error: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Adding cloning functionality to clone added fields.
     * 
     * @return the clone object
     * @throws CloneNotSupportedException
     *             should not be thrown
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        CreditCategory clone = (CreditCategory) super.clone();
        return cloneCreditCategoryExtensionList(clone);
    }

    private CreditCategory cloneCreditCategoryExtensionList(
            final CreditCategory clone) throws CloneNotSupportedException
    {
        List categoryExtensions = super.getCreditCategoryExtensions();
        if (categoryExtensions != null)
        {
            final List extentionList = new ArrayList(categoryExtensions.size());
            clone.setCreditCategoryExtensions(extentionList);
            for (final Iterator it = categoryExtensions.iterator(); it.hasNext();)
            {
                extentionList.add(safeClone((XCloneable) it.next()));
            }
        }
        return clone;
    }

    /**
     * The operating context.
     */
    protected transient Context context_;
}
