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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.LazyLoadBean;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.beans.FreezeFunction;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountExtensionMoveRequest extends AbstractAccountExtensionMoveRequest
{
    private class AccountExtensionMoveRequestPropertyChangeListener implements PropertyChangeListener
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
                    if (AccountExtensionMoveRequestXInfo.EXISTING_BAN.getName().equals(evt.getPropertyName()))
                    {
                        setOriginalAccount(null);
                        setOldAccount(null);
                    }
                    else if (AccountExtensionMoveRequestXInfo.NEW_BAN.getName().equals(evt.getPropertyName()))
                    {
                        setNewAccount(null);
                    }
                }
            }
        }
    }
    
    public AccountExtensionMoveRequest()
    {
        super();
        this.addPropertyChangeListener(new AccountExtensionMoveRequestPropertyChangeListener());
    }
    
    
    public void setExistingBAN(Account account) throws IllegalArgumentException
    {
        if (account != null)
        {
            setExistingBAN(account.getBAN());
            setOldAccount(account);
        }
    }


    public Account getOriginalAccount(Context ctx)
    {
        Account originalAccount = this.originalAccount_;
        if (originalAccount == null)
        {
            try
            {
                originalAccount = AccountSupport.getAccount(ctx, getExistingBAN());
                if (originalAccount != null)
                {
                    originalAccount.setContext(ctx);
                    if (this.oldAccount_ == null)
                    {
                        setOldAccount(originalAccount);
                    }
                }
                else if (this.oldAccount_ != null)
                {
                    originalAccount = oldAccount_;
                }
                setOriginalAccount(originalAccount);
            }
            catch (HomeException e)
            {
                originalAccount = null;
            }
        }
        return originalAccount;
    }

    
    protected void setOriginalAccount(Account originalAccount)
    {
        if (originalAccount != null)
        {
            Context ctx = ContextLocator.locate();
            
            try
            {
                Object clone = originalAccount.deepClone();
                if (clone instanceof LazyLoadBean)
                {
                    ((LazyLoadBean)clone).lazyLoadAllProperties(ctx);
                }
                Account frozenAccount = (Account) FreezeFunction.instance().f(
                        ctx, 
                        clone);
                this.originalAccount_ = frozenAccount;
            }
            catch (CloneNotSupportedException e)
            {
                this.originalAccount_ = null;
            }
        }
        else
        {
            this.originalAccount_ = null;
        }
    }

    
    public Account getOldAccount(Context ctx)
    {
        Account oldAccount = this.oldAccount_;
        if (oldAccount == null)
        {
            try
            {
                oldAccount = AccountSupport.getAccount(ctx, getExistingBAN());
                if (oldAccount != null)
                {
                    oldAccount.setContext(ctx);   
                }
                if (this.originalAccount_ == null)
                {
                    setOriginalAccount(oldAccount);
                }
                setOldAccount(oldAccount);
            }
            catch (HomeException e)
            {
                oldAccount = null;
            }
        }
        return oldAccount;
    }

    
    protected void setOldAccount(Account oldAccount)
    {
        this.oldAccount_ = oldAccount;
    }
    
    
    public void setNewBAN(Account newAccount) throws IllegalArgumentException
    {
        if (newAccount != null)
        {
            setNewBAN(newAccount.getBAN());
            setNewAccount(newAccount);
        }
    }

    
    public Account getNewAccount(Context ctx)
    {
        Account newAccount = this.newAccount_;
        if (newAccount == null)
        {
            if (getNewBAN() != null && getNewBAN().length() > 0)
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
        }
        return newAccount;
    }

    
    protected void setNewAccount(Account newAccount)
    {
        if (newAccount != null)
        {
            try
            {
                Account frozenAccount = (Account) FreezeFunction.instance().f(
                        ContextLocator.locate(), 
                        newAccount.deepClone());
                this.newAccount_ = frozenAccount;
            }
            catch (CloneNotSupportedException e)
            {
                this.newAccount_ = null;
            }
        }
        else
        {
            this.newAccount_ = null;   
        }
    }
    
    
    public PropertyInfo getExtensionHolderProperty()
    {
        return AccountExtensionMoveRequestXInfo.ACCOUNT_EXTENSIONS;
    }
    
    
    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>)getExtensionHolderProperty().get(this);
        return ExtensionSupportHelper.get().unwrapExtensions(holders);
    }

    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        return AccountSupport.getExtensionTypes(ctx, getNewAccount(ctx));
    }

    /**
     * @{inheritDoc}
     */
    public String getSuccessMessage(Context ctx)
    {
        MessageMgr mmgr = new MessageMgr(ctx, this);
        return mmgr.get(AccountExtensionMoveRequest.class.getSimpleName() + ".success", 
                "{0} account extensions successfully moved from account {1} to account {2}.", 
                new String[] {
                        String.valueOf(this.getExtensions().size()),
                        this.getExistingBAN(),
                        this.getNewBAN()
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

    protected Account originalAccount_ = null;
    protected Account oldAccount_ = null;
    protected Account newAccount_ = null;
}
