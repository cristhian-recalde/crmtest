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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.FreezeFunction;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.util.Link;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.LazyLoadBean;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.support.AccountSupport;


/**
 * Provides an implementation of MoveRequest that contains sufficient
 * information required to move an account.
 * 
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountMoveRequest extends AbstractAccountMoveRequest
{
    public static final String MOVE_SUCCESS_MSG_KEY = AccountMoveRequest.class.getSimpleName() + ".success";
    public static final String MOVE_NEW_BAN_SUCCESS_MSG_KEY = AccountMoveRequest.class.getSimpleName() + ".newBanSuccess";
    public static final String MOVE_NO_PARENT_SUCCESS_MSG_KEY = AccountMoveRequest.class.getSimpleName() + ".noParentSuccess";
    public static final String MOVE_NEW_BAN_NO_PARENT_SUCCESS_MSG_KEY = AccountMoveRequest.class.getSimpleName() + ".newBanNoParentSuccess";
    
    private class AccountMoveRequestPropertyChangeListener implements PropertyChangeListener
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
                    if (AccountMoveRequestXInfo.EXISTING_BAN.getName().equals(evt.getPropertyName()))
                    {
                        setOriginalAccount(null);
                        setOldAccount(null);
                    }
                    else if (AccountMoveRequestXInfo.NEW_BAN.getName().equals(evt.getPropertyName()))
                    {
                        setNewAccount(null);
                    }
                    else if (AccountMoveRequestXInfo.NEW_PARENT_BAN.getName().equals(evt.getPropertyName()))
                    {
                        setNewParentAccount(null);
                    }
                }
            }
        }
    }
    
    public AccountMoveRequest()
    {
    
        super();
        this.addPropertyChangeListener(new AccountMoveRequestPropertyChangeListener());
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
                    originalAccount = this.oldAccount_;
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
    
    
    public void setNewParentBAN(Account newParentAccount) throws IllegalArgumentException
    {
        if (newParentAccount != null)
        {
            setNewParentBAN(newParentAccount.getBAN());
            setNewParentAccount(newParentAccount);
        }
    }

    
    public Account getNewParentAccount(Context ctx)
    {
        Account newParentAccount = this.newParentAccount_;
        if (newParentAccount == null)
        {
            if (getNewParentBAN() != null && getNewParentBAN().length() > 0)
            {
                try
                {
                    newParentAccount = AccountSupport.getAccount(ctx, getNewParentBAN());
                    if (newParentAccount != null)
                    {
                        newParentAccount.setContext(ctx);
                    }
                    setNewParentAccount(newParentAccount);
                }
                catch (HomeException e)
                {
                    newParentAccount = null;
                }
            }
        }
        return newParentAccount;
    }

    
    protected void setNewParentAccount(Account newParentAccount)
    {
        if (newParentAccount != null)
        {
            try
            {
                Account frozenAccount = (Account) FreezeFunction.instance().f(
                        ContextLocator.locate(), 
                        newParentAccount.deepClone());
                this.newParentAccount_ = frozenAccount;
            }
            catch (CloneNotSupportedException e)
            {
                this.newParentAccount_ = null;
            }
        }
        else
        {
            this.newParentAccount_ = null;   
        }
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
        
        if (this.getNewParentBAN() != null && this.getNewParentBAN().trim().length() > 0)
        {
            final Link link = new Link(ctx);
            link.remove("cmd");
            link.add("cmd","SubMenuAccountEdit");
            link.remove("key");
            link.add("key",this.getNewParentBAN());
            
            if (this.getNewBAN()!=null && !this.getNewBAN().isEmpty() && !this.getExistingBAN().equals(this.getNewBAN()))
            {
                final Link newBanLink = new Link(ctx);
                newBanLink.remove("cmd");
                newBanLink.add("cmd","SubMenuAccountEdit");
                newBanLink.remove("key");
                newBanLink.add("key",this.getNewBAN());
                msg = mmgr.get(MOVE_NEW_BAN_SUCCESS_MSG_KEY, 
                        "Account successfully moved to account <a href=\"{0}\">{1}</a> with new BAN <a href=\"{2}\">{3}</a>. Old account with BAN {4} has been deactivated.", 
                        new String[] {
                                link.write(),
                                this.getNewParentBAN(),
                                newBanLink.write(),
                                this.getNewBAN(),
                                this.getExistingBAN()
                            });
            }
            else
            {
                msg = mmgr.get(MOVE_SUCCESS_MSG_KEY, 
                        "Account {0} successfully moved to account <a href=\"{1}\">{2}</a>.", 
                        new String[] {
                                this.getExistingBAN(),
                                link.write(),
                                this.getNewParentBAN()
                            });
            }
        }
        else
        {
            if (this.getNewBAN()!=null && !this.getNewBAN().isEmpty() && !this.getExistingBAN().equals(this.getNewBAN()))
            {
                final Link newBanLink = new Link(ctx);
                newBanLink.remove("cmd");
                newBanLink.add("cmd","SubMenuAccountEdit");
                newBanLink.remove("key");
                newBanLink.add("key",this.getNewBAN());
                msg = mmgr.get(MOVE_NEW_BAN_NO_PARENT_SUCCESS_MSG_KEY, 
                        "Account successfully moved out of existing account hierarchy with new BAN <a href=\"{0}\">{1}</a>. Old account with BAN {2} has been deactivated.", 
                        new String[] {
                                newBanLink.write(),
                                this.getNewBAN(),
                                this.getExistingBAN()
                            });
            }
            else
            {
                msg = mmgr.get(MOVE_NO_PARENT_SUCCESS_MSG_KEY, 
                    "Account {0} successfully moved out of existing account hierarchy.", 
                    new String[] {
                            this.getExistingBAN()
                        });
            }
        }
                
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
    
    public List<String> getStatusMessages(Context ctx)
    {
    	return Collections.unmodifiableList(statusMessages_);
    }

    public void clearStatusMessage(Context ctx)
    {
      statusMessages_ = new ArrayList<String>();
    }

    public void reportStatusMessages(Context ctx, String message)
    {
        assertBeanNotFrozen();
        
        statusMessages_.add(message);
    }
    
    
    
    protected Set<Throwable> errors_ = new HashSet<Throwable>();
    protected Set<MoveWarningException> warnings_ = new HashSet<MoveWarningException>();
    protected List<String> statusMessages_ = new ArrayList<String>();
    
    protected Account originalAccount_ = null;
    protected Account oldAccount_ = null;
    protected Account newAccount_ = null;
    protected Account newParentAccount_ = null;
}
