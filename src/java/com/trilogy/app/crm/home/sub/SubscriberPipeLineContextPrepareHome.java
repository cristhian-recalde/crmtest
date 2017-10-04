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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.provision.gateway.SPGServiceProvisionCollector;
import com.trilogy.app.crm.subscriber.provision.ProvisionResultCode;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.Lookup;

/**
 * A few transient variables need to be prepared, stored, cached in the context, for passing value crossing homes,
 * performance issue.
 *
 * @author joe.chen@redknee.com
 */
public class SubscriberPipeLineContextPrepareHome extends HomeProxy
{

    /**
     * @param delegate
     */
    public SubscriberPipeLineContextPrepareHome(Home delegate)
    {
        super(delegate);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	Context subCtx = prepareProvisionHome(ctx, obj, false);
        /**
         * Adding for TT fix TT#13070248026
         * As per BSS Sgr id : SgR.CRM.1391 The MSL for pooled subscriptions would be set to infinite in CPS.
         * Therefore, in BSS, while subscriber creation under a group pooled account(dummy subscription)
         * The MSL value should be set as default value 
         */
        Subscriber sub = (Subscriber) obj;
        if(sub.getAccount(subCtx).getGroupType().getIndex() == GroupTypeEnum.GROUP_POOLED_INDEX)
        {
            sub.setMonthlySpendLimit(Subscriber.DEFAULT_MONTHLYSPENDLIMIT);
        }
        Object ret = super.create(subCtx, sub);

        return ret;
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	final Context subCtx = prepareProvisionHome(ctx, obj, true);
        super.remove(subCtx, obj);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final Context subCtx = prepareProvisionHome(ctx, obj, true);
        Object ret = super.store(subCtx, obj);

        /*
         * Cindy Wong: TT 6121242579 Add this exception to context to make sure the transaction fails.
         */
        final Object exception = subCtx.get(Exception.class);
        if (exception != null)
        {
            ctx.put(Exception.class, exception);
        }
        return ret;
    }

    /**
     * Prepare context for subscriber pipeline starting. All new key will be dropped after pipeline finished
     *
     * @param parentCtx
     * @param obj
     * @param isStoreOrRemove
     * @return
     * @throws HomeException
     */
    Context prepareProvisionHome(final Context parentCtx, final Object obj, final boolean isStoreOrRemove)
        throws HomeException
    {
        final Context ctx = parentCtx.createSubContext();
        Subscriber sub = (Subscriber) obj;

        prepareProvisionResultCode(ctx, sub);
        prepareExceptionListener(ctx, sub);
        preparePricePlanVersion(ctx, sub);

        // the account
        final Account acct = AccountSupport.getAccount(ctx, sub.getBAN());
        ctx.put(Lookup.ACCOUNT, acct);
        ctx.put(Account.class, acct);

        ctx.put(Subscriber.class, sub);
        
        MSP.setBeanSpid(ctx, sub.getSpid()); 

        // prepare oldSub, and account object, so we make sure object are cached in this
        // context, dropped after pipeline.

        Subscriber oldSub = null;
        if (isStoreOrRemove && sub.getId() != null && sub.getId().length() > 0)
        {
            // the old subscriber
            oldSub = (Subscriber) find(ctx, sub.getId());
            if (oldSub.getContextInternal()==null)
            {
                oldSub.setContext(ctx);
            }
            
    		//fix me, it is 7.3 staff. 
    		try {
                oldSub.getSuspendedBundles(ctx);
                oldSub.getSuspendedPackages(ctx);
                oldSub.getSuspendedAuxServices(ctx);
                oldSub.getSuspendedServices(ctx);
                oldSub.getCLTCServices(ctx);
                
    		    Subscriber oldSub2 = (Subscriber)oldSub.deepClone();
    		    
    		    oldSub2.getAuxiliaryServices(ctx); 
    		    oldSub2.resetProvisionedAuxServiceIdsBackup();
    		    oldSub2.resetProvisionedAuxServiceBackup();
     	        oldSub2.getProvisionedAuxServiceIdsBackup(ctx);
     	        oldSub2.getProvisionedAuxServiceBackup(ctx);
     	        oldSub2.getSubExtensions(ctx);
    		    oldSub2.freeze();
                ctx.put(Lookup.OLD_FROZEN_SUBSCRIBER,oldSub2);
    		    
    		} catch ( CloneNotSupportedException e)
    		{
    		    // should not happen. 
    		}
        }
        ctx.put(Lookup.OLDSUBSCRIBER, oldSub);

        prepareSPGServiceProvisionCollector(ctx, sub, oldSub);

        // prepare deactivatedSub, useful for subscriber conversions from Postpaid
        // to Prepaid subscribers (and vice versa). Only saved on CREATE
        if (!isStoreOrRemove && sub.getId() != null && sub.getId().length() > 0)
        {
            // the old deactivated subscriber
            Subscriber deactivatedSub = (Subscriber) find(ctx, sub.getId());
            // on conversion
            if (deactivatedSub != null && !sub.getSubscriberType().equals(deactivatedSub.getSubscriberType()))
            {
                ctx.put(Lookup.TEMPLATESUBSCRIBER, deactivatedSub);
            }
        }
        
      


      
        return ctx;
    }

    void prepareExceptionListener(final Context ctx, final Subscriber sub)
    {
        HTMLExceptionListener el = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
        if (el == null)
        {
            if (!ctx.has(ExceptionListener.class) || !(ctx.get(ExceptionListener.class) instanceof HTMLExceptionListener))
            {
                el = new HTMLExceptionListener(new MessageMgr(ctx, this));
            }
            else
            {
                el = (HTMLExceptionListener) ctx.get(ExceptionListener.class);
            }
        }
        ctx.put(HTMLExceptionListener.class, el);
        sub.setExceptionListener(el);
        
    }

    void prepareProvisionResultCode(final Context ctx, final Subscriber newSub)
    {
        ProvisionResultCode el = new ProvisionResultCode();
        ctx.put(ProvisionResultCode.class, el);
        // newSub.setProvisionResultCode(el);
    }

    void preparePricePlanVersion(final Context ctx, final Subscriber sub) throws HomeException
    {
        PricePlanVersion pricePlan = null;
        try
        {
            pricePlan = sub.getPricePlan(ctx);
        }
        catch (Exception e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Error while retrieving price plan " + sub.getPricePlan(), e).log(ctx);
            }

            throw new HomeException("Configuration Error: failed to retrieve selected price plan ["
                + e.getMessage() + "]", e);
        }

        // NOTE there is no pricePlan == null check because there is a validator on the pipeline that
        // will stop the update if the Price Plan of PricePlan Version is not valid

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Price Plan is: " + pricePlan, null).log(ctx);
        }

        ctx.put(PricePlanVersion.class, pricePlan);
    }

    void prepareSPGServiceProvisionCollector(final Context ctx, final Subscriber newSub, final Subscriber oldSub)
    {
        final SPGServiceProvisionCollector collector = new SPGServiceProvisionCollector();
        collector.newSub = newSub;
        collector.oldSub = oldSub;

        ctx.put(SPGServiceProvisionCollector.class, collector);
    }
}
