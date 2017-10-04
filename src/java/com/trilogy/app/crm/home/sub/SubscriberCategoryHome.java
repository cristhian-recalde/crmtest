/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCategory;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.Lookup;


public class SubscriberCategoryHome extends HomeProxy
{

    // private Home accountHome;
    // private Home subsriberCategoryHome;
    // private Home subscriberHome;
    public SubscriberCategoryHome(Context ctx, Home home)
    {
        super(ctx, home);
        // subscriberHome = (Home) ctx.get(SubscriberHome.class);
    }


    /**
     * Save the association between subscriber to its bundles to the
     * BundleAuxiliaryServiceHome
     */
    public Object create(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	final Home accountHome = (Home) ctx.get(AccountHome.class);
        Subscriber sub = (Subscriber) obj;
        Account act = (Account) ctx.get(Lookup.ACCOUNT);
        if (act == null)
        {
            act = sub.getAccount(ctx);
        }
        try
        {
            if (getCategoryRank(ctx, act.getCategory()) < getCategoryRank(ctx, sub.getSubscriberCategory()))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Category rank is upgraded with new subscriber", null).log(ctx);
                }
                long exisitingActCat = act.getCategory();
                act.setCategory(sub.getSubscriberCategory());
                accountHome.store(ctx,act);
                propagateAccountCategory(ctx, act, exisitingActCat);
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error while creating category", e).log(ctx);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }
        return super.create(ctx, obj);
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    private long getCategoryRank(Context ctx, long category)
    {
        final Home subsriberCategoryHome = (Home) ctx.get(com.redknee.app.crm.bean.SubscriberCategoryHome.class);
        try
        {
            SubscriberCategory subCat = (SubscriberCategory) subsriberCategoryHome.find(ctx, Long.valueOf(category));
            if (subCat == null)
            {
                return 0;
            }
            return subCat.getRank();
        }
        catch (HomeException e)
        {
            new DebugLogMsg(this, "Encountered Exception while getting Category Rank", e).log(ctx);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }
        // simar - I feel bad that we end up returning 0 for an exception case also
        // it is there from before, I am here to just fix an unrleated issue (TT
        // 9071400109). I have just enabled the logging and notficiation to GUI
        // ExceptionListner
        // but should anyone get time to work on this, let the exception propagate
        // The caller should be made able handle exception
        return 0;
    }


    public Object store(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final Home accountHome = (Home) ctx.get(AccountHome.class);
        Subscriber sub = (Subscriber) obj;
        Subscriber existingSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        Account act = (Account) ctx.get(Lookup.ACCOUNT);
        long exisitingActCat = act.getCategory();
        if (existingSub == null)
        {
            new DebugLogMsg(this, "No old subscriber found, so no need to handle the change of Subscriber Category",
                    null).log(ctx);
            return super.store(ctx, obj);
        }
        if (existingSub.getSubscriberCategory() == sub.getSubscriberCategory())
        {
            new DebugLogMsg(this, "No change to the Subscriber Category detected", null).log(ctx);
            return super.store(ctx, obj);
        }
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Subscriber Category Change Detected [oldSubscriberCategory="
                    + existingSub.getSubscriberCategory() + ",newSubscriberCategory=" + sub.getSubscriberCategory()
                    + "].", null).log(ctx);
        }
        try
        {
            if (getCategoryRank(ctx, act.getCategory()) < getCategoryRank(ctx, sub.getSubscriberCategory()))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "If rank is upgraded", null).log(ctx);
                }
                act.setCategory(sub.getSubscriberCategory());
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Modifying Account Category - Upgrade", null).log(ctx);
                }
                accountHome.store(ctx, act);
            }
            else if (existingSub != null && (act.getCategory() == existingSub.getSubscriberCategory()))
            {
                if (LogSupport.isDebugEnabled(ctx))
                    new DebugLogMsg(this, "Rank is downgraded", null).log(ctx);
                long maxRank = 0, maxRankCategory = 0;
                Collection<Subscriber> allSubs = act.getSubscribers(ctx);
                Iterator<Subscriber> allSubsItr = allSubs.iterator();
                while (allSubsItr.hasNext())
                {
                    Subscriber checkSub = (Subscriber) allSubsItr.next();
                    // skiping the existing sub
                    if (checkSub.getId().equals(existingSub.getId()))
                    {
                        checkSub = sub;
                    }
                    long checkCategory = checkSub.getSubscriberCategory();
                    long checkRank = getCategoryRank(ctx, checkCategory);
                    if (maxRank < checkRank)
                    {
                        maxRank = checkRank;
                        maxRankCategory = checkCategory;
                    }
                }
                act.setCategory(maxRankCategory);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Modifying Account Category - Downgrade", null).log(ctx);
                }
                accountHome.store(ctx, act);
            }
            // propagate category change to parent accounts
            propagateAccountCategory(ctx, act, exisitingActCat);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Home Exception: Could not update Subscriber Category", e).log(ctx);
            }
            new MinorLogMsg(this, "Error while creating category", e).log(ctx);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }
        return super.store(ctx, obj);
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public void remove(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	super.remove(ctx, obj);
    }


    private void propagateAccountCategory(Context ctx, Account act, long exisitingActCat) throws HomeException
    {
        final Home accountHome = (Home) ctx.get(AccountHome.class);
        while (act.isResponsible() == false)
        {
            Account parentAct = act.getParentAccount(ctx);
            if (getCategoryRank(ctx, parentAct.getCategory()) < getCategoryRank(ctx, act.getCategory()))
            {
                parentAct.setCategory(act.getCategory());
            }
            else if (parentAct.getCategory() == exisitingActCat)
            {
                Collection<Subscriber> allSubs = parentAct.getSubscribers(ctx);
                Iterator<Subscriber> allSubsItr = allSubs.iterator();
                while (allSubsItr.hasNext())
                {
                    Subscriber checkSub = allSubsItr.next();
                    if (checkSub.getSubscriberCategory() >= act.getCategory())
                    {
                        return;
                    }
                }
                parentAct.setCategory(act.getCategory());
            }
            else
            {
                break;
            }
            new DebugLogMsg(this, "Propagating Change to parent account", null).log(ctx);
            accountHome.store(ctx,parentAct);
            act = parentAct;
        }
    }
    /*
     * Commented out since it is not used and it does not complie private boolean
     * rollBackAccounts(Context ctx,Vector accounts) { try{ Iterator accountItr =
     * accounts.iterator(); while(accountItr.hasNext()) { Account act = (Account)
     * accountItr.next(); accountHome.store(act); } } catch (Exception e) { new
     * CritLogMsg(
     * this,"Could not rollback all accounts on Subscriber Category Updation fail"
     * ,e).log(ctx); }
     * 
     * 
     * }
     */
}
