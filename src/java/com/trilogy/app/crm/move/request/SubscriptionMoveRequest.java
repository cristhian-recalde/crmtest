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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.beans.FreezeFunction;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.LazyLoadBean;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Provides an implementation of MoveRequest that contains sufficient
 * information required to move a subscription.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SubscriptionMoveRequest extends AbstractSubscriptionMoveRequest
{
    private class SubscriptionMoveRequestPropertyChangeListener implements PropertyChangeListener
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
                    if (SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID.getName().equals(evt.getPropertyName()))
                    {
                        setOriginalSubscription(null);
                        setOldSubscription(null);
                        setOldAccount(null);
                        setSubscriptionType(null);
                    }
                    else if (SubscriptionMoveRequestXInfo.NEW_SUBSCRIPTION_ID.getName().equals(evt.getPropertyName()))
                    {
                        setNewSubscription(null);
                    }
                    else if (SubscriptionMoveRequestXInfo.NEW_BAN.getName().equals(evt.getPropertyName()))
                    {
                        setNewAccount(null);
                    }
                }
            }
        }
    }
    
    public SubscriptionMoveRequest()
    {
        super();
        this.addPropertyChangeListener(new SubscriptionMoveRequestPropertyChangeListener());
    }
    
    public SubscriptionType getSubscriptionType(Context ctx)
    {
        SubscriptionType subscriptionType = null;
        
        Subscriber subscription = getOriginalSubscription(ctx);
        if (subscription != null)
        {
            long typeId = subscription.getSubscriptionType();
            if (subscriptionType == null
                    || this.subscriptionType_ == null
                    || this.subscriptionType_.getId() != typeId)
            {
                try
                {
                    setSubscriptionType(subscription.getSubscriptionType(ctx));
                }
                catch (Exception e)
                {
                    new MinorLogMsg(this, "Error retrieving subscription type (Type ID=" + typeId + ") for subscription " + subscription.getId(), e).log(ctx);
                }
            }
        }
    
        return this.subscriptionType_;
    }

    
    protected void setSubscriptionType(SubscriptionType subscriptionType)
    {
        this.subscriptionType_ = subscriptionType;
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
        setSubscriptionType(null);
    }
    
    
    public void setOldSubscriptionId(Subscriber subscription) throws IllegalArgumentException
    {
        if (subscription != null)
        {
            setOldSubscriptionId(subscription.getId());
            setOldSubscription(subscription);
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
        setSubscriptionType(null);
    }
    
    
    public void setNewSubscriptionId(Subscriber subscription) throws IllegalArgumentException
    {
        if (subscription != null)
        {
            setNewSubscriptionId(subscription.getId());
            setNewSubscription(subscription);
        }
    }

    
    public Subscriber getNewSubscription(Context ctx)
    {
        Subscriber newSubscription = this.newSubscription_;
        if (newSubscription == null)
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
        return newSubscription;
    }

    
    protected void setNewSubscription(Subscriber newSubscription)
    {
        this.newSubscription_ = newSubscription;
    }

    
    public Account getOldAccount(Context ctx)
    {
        Account oldAccount = this.oldAccount_;
        Subscriber originalSubscription = getOriginalSubscription(ctx);
        if (originalSubscription != null)
        {
            String oldBAN = originalSubscription.getBAN();
            if (oldAccount == null)
            {
                try
                {
                    oldAccount = originalSubscription.getAccount(ctx);
                    if (oldAccount != null)
                    {
                        oldAccount.setContext(ctx);   
                    }
                    setOldAccount(oldAccount);
                }
                catch (HomeException e)
                {
                    oldAccount = null;
                }
            }   
        }
        return oldAccount;
    }

    
    protected void setOldAccount(Account oldAccount)
    {
        this.oldAccount_ = oldAccount;
    }
    
    
    public void setNewBAN(Account account) throws IllegalArgumentException
    {
        if (account != null)
        {
            setNewBAN(account.getBAN());
            setNewAccount(account);
        }
    }

    
    public Account getNewAccount(Context ctx)
    {
        Account newAccount = this.newAccount_;
        if (newAccount == null)
        {
            try
            {
                newAccount = AccountSupport.getAccount(ctx, getNewBAN());
                if (newAccount != null)
                {
                    newAccount.setContext(ctx);   
                }
                setNewAccount(newAccount);
            }
            catch (HomeException e)
            {
                newAccount = null;
            }
        }
        return newAccount;
    }

    
    protected void setNewAccount(Account newAccount)
    {
        this.newAccount_ = newAccount;
    }

    /**
     * @{inheritDoc}
     */
    public String getSuccessMessage(Context ctx)
    {
        String msg = null;
        
        MessageMgr mmgr = new MessageMgr(ctx, this);

        final Link link = new Link(ctx);
        link.remove("cmd");
        link.add("cmd","SubMenuAccountEdit");
        link.remove("key");
        link.add("key",this.getNewBAN());
        
        msg = mmgr.get(SubscriptionMoveRequest.class.getSimpleName() + ".success", 
                "Subscription {0} successfully moved to account <a href=\"{1}\">{2}</a>.", 
                new String[] {
                        this.getOldSubscriptionId(),
                        link.write(),
                        this.getNewBAN()
                    });
        
        return msg;
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

    protected SubscriptionType subscriptionType_ = null;
    protected Subscriber originalSubscription_ = null;
    protected Subscriber oldSubscription_ = null;
    protected Subscriber newSubscription_ = null;
    protected Account oldAccount_ = null;
    protected Account newAccount_ = null;
}
