/*
 *  PooledGroupAccountFalseSubscriberProvisioningHome.java
 *
 *  Author : Gary Anderson
 *  Date   : 2003-11-14
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberIdentitySupport;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.AccountCategory;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Updates group-pooled accounts so that they are aware of which subscriber is
 * the group-pooled, "false" subscriber.
 *
 * @author gary.anderson@redknee.com
 */
public class PooledGroupAccountFalseSubscriberProvisioningHome
    extends HomeProxy
{
    /**
     * Creates a new PooledGroupAccountFalseSubscriberProvisioningHome for the
     * given home.
     *
     * @param context The operating context.
     * @param delegate The Home to which this object delegates.
     */
    public PooledGroupAccountFalseSubscriberProvisioningHome(
        Context context,
        Home delegate)
    {
        super(context, delegate);
    }
    
    public Object create(Context ctx, Object obj)
            throws HomeException
        {
            Subscriber sub = (Subscriber)obj;
            boolean needUpdateAccount = false; 
            Context subCtx = ctx.createSubContext();
            
            Home accountHome = (Home)subCtx.get(AccountHome.class);
            Account account = null;
            Account parentAccount = null;
            if (accountHome == null)
            {
                throw new HomeException("Could not find AccountHome in context.");
            }
            else 
            {
                account = (Account)accountHome.find(sub.getBAN());  
            }
            if (account != null)
            { 
                parentAccount = account.getParentAccount(subCtx);
            }
            else
            {
                throw new HomeException("Could not find account in the database"); 
            }
            
            Home home = (Home) subCtx.get(AccountCategoryHome.class);
            
            if(parentAccount != null)
            {
                AccountCategory accountCategory = (AccountCategory) home.find(ctx_, new EQ(AccountCategoryXInfo.IDENTIFIER,
                        parentAccount.getType()));
                if (parentAccount.isPooled(subCtx) && sub.isPostpaid() && accountCategory != null
                        && accountCategory.getCustomerType().equals(CustomerTypeEnum.FAMILY)
                        && !hasLeader(subCtx, account, parentAccount))
                {
                    parentAccount.setOwnerMSISDN(sub.getMSISDN());
                    needUpdateAccount = true;
                }
            }
            
            sub = (Subscriber) super.create(subCtx, sub); 
            
            if (needUpdateAccount)
            {
                try
                {   
                    subCtx.put(Account.class, parentAccount);
                    subCtx.put(Lookup.ACCOUNT, parentAccount);
                    accountHome.store(subCtx, parentAccount); 
                } catch (HomeException e)
                {
                    new MinorLogMsg(this, "fail to update group msisdn in account " + sub.getBAN(), e).log(subCtx); 
                }
            }
            
            return sub;
        }

    
    // INHERIT    
    public Object store(Context ctx, Object obj)
        throws HomeException
    {
        final Home home = (Home)ctx.get(SubscriberHome.class);
        if (home == null)
        {
            throw new HomeException("Could not find SubscriberHome in context.");
        }

        final Object identifier = new SubscriberIdentitySupport().ID(obj);

        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER); 
        
        Object ret=super.store(ctx,obj);
        
        final Subscriber newSubscriber = (Subscriber)obj;

        ensureAccountGroupMSISDNSet(ctx,oldSubscriber, newSubscriber);
        
        return ret;
    }


    /**
     * Ensures that the groupMSISDN is set ion the subscriber's account if
     * necessary.
     *
     * @param oldSubscriber The subscriber for which to ensure its account has
     * its groupMSISDN set, in its previous state.
     * @param newSubscriber The subscriber for which to ensure its account has its
     * groupMSISDN set, in its new state.
     */
    private void ensureAccountGroupMSISDNSet(
      Context ctx,
      Subscriber oldSubscriber,
      Subscriber newSubscriber)
        throws HomeException
    {
        final Account account =
            SubscriberSupport.lookupAccount(ctx, newSubscriber);
        
        Account parentAccount = account.getParentAccount(ctx);

        if (parentAccount != null && parentAccount.isPooled(ctx))
        {
            final String ownerMSISDN = parentAccount.getOwnerMSISDN();

            final boolean firstSubscriber = 
                (ownerMSISDN == null || "".equals(ownerMSISDN.trim()))
                && newSubscriber.getState() == SubscriberStateEnum.ACTIVE;

            final boolean newOwnerMSISDN = 
                ownerMSISDN != null
                && ownerMSISDN.equals(oldSubscriber.getMSISDN())
                && !oldSubscriber.getMSISDN().equals(newSubscriber.getMSISDN());
                
            
            /*
             * TT #8120800032: Update owner MSISDN if a MSISDN swap occurs.
             */
            if (newOwnerMSISDN)
            {
                parentAccount.setOwnerMSISDN(newSubscriber.getMSISDN());
                final Home home = (Home)ctx.get(AccountHome.class);
                home.store(ctx,parentAccount);
            }
        }
    }
    
    
    public static boolean hasLeader(Context ctx, Account account, Account parentAccount)
    {
        if (parentAccount.getOwnerMSISDN() == null || parentAccount.getOwnerMSISDN().length()==0)
        {
            return false; 
            
        }
        
        return getSubscriberInAccountByMsisdn(ctx, parentAccount.getOwnerMSISDN()); 
        
     }
    
    
    public static boolean getSubscriberInAccountByMsisdn(Context ctx, String msisdn)
    {
        try 
        {
            Context appCtx = (Context) ctx.get("app");
            Home home = (Home) appCtx.get(appCtx, SubscriberHome.class);
            And and = new And(); 
            
            Account account = AccountSupport.getAccountByMsisdn(appCtx, msisdn);
            and.add(new EQ(SubscriberXInfo.BAN, account.getBAN())); 
            and.add(new EQ(SubscriberXInfo.MSISDN, msisdn)); 
            and.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE)); 
            
            Collection coll = home.select(appCtx, and);
                
            if (coll != null && coll.size() > 0)
            {
                return true; 
            }
            
            
        } catch (Exception e)
        {
            new MinorLogMsg(PooledGroupAccountFalseSubscriberProvisioningHome.class, 
                    "unexpected exception during searching subscriber for " + msisdn, e ).log(ctx); 
        }
        
        return false; 
        
    }
    
} // class
