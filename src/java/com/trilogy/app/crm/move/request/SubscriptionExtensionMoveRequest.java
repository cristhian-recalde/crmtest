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
package com.trilogy.app.crm.move.request;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.beans.FreezeFunction;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;

import com.trilogy.app.crm.bean.LazyLoadBean;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SubscriptionExtensionMoveRequest extends AbstractSubscriptionExtensionMoveRequest
{
    private class SubscriptionExtensionMoveRequestPropertyChangeListener implements PropertyChangeListener
    {
        /**
         * @{inheritDoc}
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt != null)
            {
                Object oldValue = evt.getOldValue();
                Object newValue = evt.getNewValue();
                if (!SafetyUtil.safeEquals(oldValue, newValue))
                {
                    if (SubscriptionExtensionMoveRequestXInfo.OLD_SUBSCRIPTION_ID.getName().equals(evt.getPropertyName()))
                    {
                        setOriginalSubscription(null);
                        setOldSubscription(null);
                    }
                    else if (SubscriptionExtensionMoveRequestXInfo.NEW_SUBSCRIPTION_ID.getName().equals(evt.getPropertyName()))
                    {
                        setNewSubscription(null);
                    }
                }
            }
        }
    }
    
    public SubscriptionExtensionMoveRequest()
    {
        super();
        this.addPropertyChangeListener(new SubscriptionExtensionMoveRequestPropertyChangeListener());
    }
    
    
    public void setOldSubscriptionId(Subscriber subscription) throws IllegalArgumentException
    {
        if (subscription != null)
        {
            setOldSubscriptionId(subscription.getId());
            setOldSubscription(subscription);
        }
    }


    public Subscriber getOriginalSubscription(Context ctx)
    {
        Subscriber originalSubscription = this.originalSubscription_;
        if (originalSubscription == null)
        {
            try
            {
                originalSubscription = SubscriberSupport.lookupSubscriberForSubId(ctx, getOldSubscriptionId());
                if (originalSubscription != null)
                {
                    originalSubscription.setContext(ctx);
                    if (this.oldSubscription_ == null)
                    {
                        setOldSubscription(originalSubscription);
                    }
                }
                else if (this.oldSubscription_ != null)
                {
                    originalSubscription = this.oldSubscription_;
                }
                setOriginalSubscription(originalSubscription);
            }
            catch (HomeException e)
            {
                originalSubscription = null;
            }
        }
        return originalSubscription;
    }

    
    protected void setOriginalSubscription(Subscriber originalSubscription)
    {
        if (originalSubscription != null)
        {
            Context ctx = ContextLocator.locate();
            try
            {
                Object clone = originalSubscription.deepClone();
                if (clone instanceof LazyLoadBean)
                {
                    ((LazyLoadBean)clone).lazyLoadAllProperties(ctx);
                }
                Subscriber frozenSubscription = (Subscriber) FreezeFunction.instance().f(
                        ctx, 
                        clone);
                this.originalSubscription_ = frozenSubscription;
            }
            catch (CloneNotSupportedException e)
            {
                this.originalSubscription_ = null;
            }
        }
        else
        {
            this.originalSubscription_ = null;
        }
    }

    
    public Subscriber getOldSubscription(Context ctx)
    {
        Subscriber oldSubscription = this.oldSubscription_;
        if (oldSubscription == null)
        {
            try
            {
                oldSubscription = SubscriberSupport.lookupSubscriberForSubId(ctx, getOldSubscriptionId());
                if (oldSubscription != null)
                {
                    oldSubscription.setContext(ctx);   
                }
                if (this.originalSubscription_ == null)
                {
                    setOriginalSubscription(oldSubscription);
                }
                setOldSubscription(oldSubscription);
            }
            catch (HomeException e)
            {
                oldSubscription = null;
            }
        }
        return oldSubscription;
    }

    
    protected void setOldSubscription(Subscriber oldSubscription)
    {
        this.oldSubscription_ = oldSubscription;
    }
    
    
    public void setNewSubscriptionId(Subscriber newSubscription) throws IllegalArgumentException
    {
        if (newSubscription != null)
        {
            setNewSubscriptionId(newSubscription.getId());
            setNewSubscription(newSubscription);
        }
    }

    
    public Subscriber getNewSubscription(Context ctx)
    {
        Subscriber newSubscription = this.newSubscription_;
        if (newSubscription == null)
        {
            if (getNewSubscriptionId() != null && getNewSubscriptionId().length() > 0)
            {
                try
                {
                    newSubscription = SubscriberSupport.lookupSubscriberForSubId(ctx, getNewSubscriptionId());
                    if (newSubscription != null)
                    {
                        newSubscription.setContext(ctx);
                    }
                    setNewSubscription(newSubscription);
                }
                catch (HomeException e)
                {
                    newSubscription = null;
                }
            }
        }
        return newSubscription;
    }

    
    protected void setNewSubscription(Subscriber newSubscription)
    {
        if (newSubscription != null)
        {
            try
            {
                Subscriber frozenSubscription = (Subscriber) FreezeFunction.instance().f(
                        ContextLocator.locate(), 
                        newSubscription.deepClone());
                this.newSubscription_ = frozenSubscription;
            }
            catch (CloneNotSupportedException e)
            {
                this.newSubscription_ = null;
            }
        }
        else
        {
            this.newSubscription_ = null;   
        }
    }
    
    
    public PropertyInfo getExtensionHolderProperty()
    {
        return SubscriptionExtensionMoveRequestXInfo.SUBSCRIPTION_EXTENSIONS;
    }
    
    
    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>)getExtensionHolderProperty().get(this);
        return ExtensionSupportHelper.get().unwrapExtensions(holders);
    }

    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        return SubscriberSupport.getExtensionTypes(ctx, this.getNewSubscription(ctx).getSubscriberType());
    }

    

    /**
     * @{inheritDoc}
     */
    public String getSuccessMessage(Context ctx)
    {
        MessageMgr mmgr = new MessageMgr(ctx, this);
        return mmgr.get(SubscriptionExtensionMoveRequest.class.getSimpleName() + ".success", 
                "{0} subscription extensions successfully moved from subscription {1} to subscription {2}.", 
                new String[] {
                        String.valueOf(this.getExtensions().size()),
                        this.getOldSubscriptionId(),
                        this.getNewSubscriptionId()
                    });
    }

    
    /**
     * @{inheritDoc}
     */
    public void reportError(Context ctx, Throwable error)
    {
        assertBeanNotFrozen();
        
        errors_.add(error);
    }

    
    /**
     * @{inheritDoc}
     */
    public void reportWarning(Context ctx, MoveWarningException warning)
    {
        assertBeanNotFrozen();
        
        warnings_.add(warning);
    }

    
    /**
     * @{inheritDoc}
     */
    public boolean hasErrors(Context ctx)
    {
        return errors_ != null && errors_.size() > 0;
    }

    
    /**
     * @{inheritDoc}
     */
    public boolean hasWarnings(Context ctx)
    {
        return warnings_ != null && warnings_.size() > 0;
    }

    
    /**
     * @{inheritDoc}
     */
    public Set<Throwable> getErrors(Context ctx)
    {
        return Collections.unmodifiableSet(errors_);
    }
    

    /**
     * @{inheritDoc}
     */
    public Set<MoveWarningException> getWarnings(Context ctx)
    {
        return Collections.unmodifiableSet(warnings_);
    }
    
    
    protected Set<Throwable> errors_ = new HashSet<Throwable>();
    protected Set<MoveWarningException> warnings_ = new HashSet<MoveWarningException>();

    protected Subscriber originalSubscription_ = null;
    protected Subscriber oldSubscription_ = null;
    protected Subscriber newSubscription_ = null;
}
