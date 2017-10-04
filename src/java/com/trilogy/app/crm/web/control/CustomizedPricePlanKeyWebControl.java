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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.PricePlanFunctionEnum;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTechnologyConversion;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.audi.AudiUpdateSubscriber;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.filter.ContractDurationPricePlanPredicate;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.support.BeanLoaderSupport;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyAware;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.snippet.log.Logger;

/**
 * Filter out disabled price plan.
 *
 * @author victor.stratan@redknee.com
 * @author aaron.gourley@redknee.com
 */
public class CustomizedPricePlanKeyWebControl extends KeyWebControlProxy
{
    private static final String PRICE_PLAN_LIST_CONTAINS_RESTRICTED = "PricePlan.ListContainsRestricted";

    public CustomizedPricePlanKeyWebControl(final AbstractKeyWebControl keyWebControl)
    {
        this(keyWebControl, false);
    }
    
    public CustomizedPricePlanKeyWebControl(final AbstractKeyWebControl keyWebControl, boolean hideRestrictedPricePlans)
    {
        super(keyWebControl);
        hideOverridablePricePlans_ = hideRestrictedPricePlans;
    }


    @Override
    public void toWeb(Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final Context subCtx = ctx.createSubContext();
        
        final Object sub = subCtx.get(AbstractWebControl.BEAN);
        Long ppid = (Long) obj;

        BeanLoaderSupport beanSupport = BeanLoaderSupportHelper.get(ctx);
        Map<Class, Collection<PropertyInfo>> beanLoaderMap = null;
        if (sub instanceof AudiUpdateSubscriber)
        {
            AudiUpdateSubscriber audiUpdateRequest = (AudiUpdateSubscriber) sub;
            String msisdn = audiUpdateRequest.getMSISDN();
            if (msisdn != null)
            {
                Subscriber subscriber = null;
                try
                {
                    subscriber = SubscriberSupport.lookupSubscriberForMSISDN(subCtx, msisdn);
                }
                catch (HomeException e)
                {
                    if (LogSupport.isDebugEnabled(subCtx))
                    {
                        new DebugLogMsg(this, "Error looking up subscriber for MSISDN " + msisdn, e).log(ctx);
                    }
                }
                if (subscriber != null)
                {
                    subCtx.put(Subscriber.class, subscriber);
                    beanLoaderMap = beanSupport.getBeanLoaderMap(ctx, Subscriber.class);
                }
            }
        }
        if (beanLoaderMap != null)
        {
            beanSupport.setBeanLoaderMap(subCtx, beanLoaderMap);
        }
        
        if (sub instanceof Subscriber)
        {
            final Subscriber s = (Subscriber) sub;
            boolean forceDisplayMode = s.getState() == SubscriberStateEnum.SUSPENDED;
            if (!forceDisplayMode 
                    && SubscriberXInfo.SECONDARY_PRICE_PLAN != (PropertyInfo) subCtx.get(AbstractWebControl.PROPERTY) 
                    && !s.isPooledGroupLeader(subCtx))
            {
                Account parentAccount = null;
                final Account account = SubscriberSupport.lookupAccount(subCtx, s);
                try
                {
                    if (account != null)
                    {
                        parentAccount = account.getParentAccount(subCtx);
                    }
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to find parent account " + e.getMessage(), e);
                }
                if (parentAccount != null)
                {
                    // This will optimize potential future BeanLoader lookups
                    subCtx.put(Account.class, account);
                    
                    final GroupPricePlanExtension extension = parentAccount.getGroupPricePlanExtension();
                    if (extension != null)
                    {
                        /*
                         * [Cindy Wong] force update of the subscriber's price plan.
                         */
                        ppid = Long.valueOf(extension.getGroupPricePlan(s));
                        ((Subscriber) sub).setPricePlan(ppid);
                        forceDisplayMode = true;
                    }
                }
            }

            if (forceDisplayMode)
            {
                final int mode = subCtx.getInt("MODE", DISPLAY_MODE);
                // every mode becomes display mode
                if (mode != DISPLAY_MODE)
                {
                    subCtx.put("MODE", Integer.valueOf(DISPLAY_MODE));
                }
            }
        }
        
        if (sub instanceof ConvertAccountBillingTypeRequest)
        {
            ConvertAccountBillingTypeRequest convertAccountBillingTypeRequest = (ConvertAccountBillingTypeRequest) sub;
            Account parentAccount = null;
            final Account account = convertAccountBillingTypeRequest.getOldAccount(subCtx);
            if (account != null)
            {
                try
                {
                    parentAccount = account.getParentAccount(subCtx);
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to find parent account " + e.getMessage(), e);
                }
            }
            if (parentAccount != null)
            {
                // This will optimize potential future BeanLoader lookups
                subCtx.put(Account.class, account);
                
                final GroupPricePlanExtension extension = parentAccount.getGroupPricePlanExtension();
                if (extension != null)
                {
                    /*
                     * [Cindy Wong] force update of the subscriber's price plan.
                     */
                    ppid = Long.valueOf(extension.getGroupPricePlan(convertAccountBillingTypeRequest.getSystemType()));
                    ((ConvertAccountBillingTypeRequest) sub).setPricePlan(ppid);
                    subCtx.put("MODE", Integer.valueOf(DISPLAY_MODE));
                }
            }
        }
        

        out.print("<table><tr><td>");
        super.toWeb(subCtx, out, name, ppid);
        
        if (subCtx.getBoolean(PRICE_PLAN_LIST_CONTAINS_RESTRICTED, false))
        {
            out.print("</td></tr><tr><td>");
            MessageMgr mmgr = new MessageMgr(subCtx, this);
            String restrictionText = mmgr.get("PricePlan.RestrictedValueLabel", "Use is restricted by Price Plan");
            out.print("<font size=\"1\">*&nbsp;-&nbsp;" + restrictionText + "</font>");
        }
        out.print("</td></tr></table>");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDesc(final Context ctx, final Object bean)
    {
        StringBuilder desc = new StringBuilder(super.getDesc(ctx, bean));
        
        if (bean instanceof PricePlan)
        {
            PricePlan pricePlan = (PricePlan) bean;
            if (pricePlan.isRestrictionViolation(ctx, ctx.get(AbstractWebControl.BEAN)))
            {
                desc.append(" (*)");
                ctx.put(PRICE_PLAN_LIST_CONTAINS_RESTRICTED, true);
            }
        }
        
        return desc.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate getSelectFilter(Context ctx)
    {
        And selectFilter = new And();
        selectFilter.add(super.getSelectFilter(ctx));
        selectFilter.add(getRestrictionPredicate(ctx));
        if (hideOverridablePricePlans_)
        {
            selectFilter.add(getOverridableRestrictionPredicate(ctx));
        }
        return selectFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate getSelectFilter()
    {
        return getSelectFilter(ContextLocator.locate());
    }

    protected Predicate getRestrictionPredicate(Context ctx)
    {
        And filter = new And();
        filter.add(getSpidFilter(ctx));
        filter.add(getSubscriberTypeFilter(ctx));
        filter.add(getTechnologyFilter(ctx));
        filter.add(getDisabledPricePlanFilter(ctx));
        filter.add(getInvalidPricePlanFilter(ctx));
        filter.add(getSpecialPoolSubscriptionPPsFilter(ctx));
        return filter;
    }

    protected Predicate getOverridableRestrictionPredicate(Context ctx)
    {
        And filter = new And();
        filter.add(new ContractDurationPricePlanPredicate());
        return filter;
    }


    public Predicate getDisabledPricePlanFilter(final Context ctx)
    {
        Predicate filter = True.instance();
        
        // filter out disabled priceplan
        // add the current priceplan subscriber selected in case this priceplan is
        // disabled
        final Object sub = ctx.get(AbstractWebControl.BEAN);
        if (sub instanceof Subscriber)
        {
            final Subscriber subscriber = (Subscriber) sub;
            // changes done to make it similar as enabled price plan which is known as ACTIVE now.
            final Predicate filterDisabled = new EQ(PricePlanXInfo.STATE, PricePlanStateEnum.ACTIVE);

            final long curPp = subscriber.getPricePlan();
            if (curPp >= 0)
            {
                // we need to use backup price plan id, so that even subscriber changes
                // price plan, the disabled one still shows
                filter = new Or().add(filterDisabled)
                        .add(new EQ(PricePlanXInfo.ID, Long.valueOf(subscriber.getPricePlanBackup())));
            }
            else
            {
                filter = filterDisabled;
            }
        }
        
        return filter;
    }

    public Predicate getSpidFilter(final Context ctx)
    {
        Predicate condition = True.instance();
        
        final Object bean = ctx.get(AbstractWebControl.BEAN);
        if (bean == null)
        {
            // No need to filter.
            return condition;
        }

        int spid = 0;
        
        if (bean instanceof ConvertSubscriptionBillingTypeRequest)
        {
            final ConvertSubscriptionBillingTypeRequest billingTypeConversion = (ConvertSubscriptionBillingTypeRequest) bean;
            spid = billingTypeConversion.getSpid();
        }
        else
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "Unsupported bean type uses " + this.getClass().getSimpleName());
            }
            
            // No need to filter.
            return condition;
        }
        
        if (spid != 0)
        {
            condition = new EQ(PricePlanXInfo.SPID, spid);
        }
        
        return condition;
    }    

    public Predicate getSubscriberTypeFilter(final Context ctx)
    {
        Predicate condition = True.instance();
        
        final Object bean = ctx.get(AbstractWebControl.BEAN);
        if (bean == null
                || bean instanceof com.redknee.app.crm.bean.AuxiliaryService
                || bean instanceof com.redknee.app.crm.bean.ui.AuxiliaryService)
        {
            // No need to filter.
            return condition;
        }

        final SubscriberTypeEnum billingType; 
        if (bean instanceof Subscriber)
        {
            final Subscriber subscriber = (Subscriber) bean;
            billingType = subscriber.getSubscriberType();
        }
        else if (bean instanceof SubscriberTechnologyConversion)
        {
            final SubscriberTechnologyConversion subscriberTechConversion = (SubscriberTechnologyConversion) bean;
            billingType = subscriberTechConversion.getSubscriberType();
        }
        else if (bean instanceof ConvertAccountBillingTypeRequest)
        {
            final ConvertAccountBillingTypeRequest billingTypeConversion = (ConvertAccountBillingTypeRequest) bean;
            billingType = billingTypeConversion.getSystemType();
        }
        else if (bean instanceof ConvertSubscriptionBillingTypeRequest)
        {
            final ConvertSubscriptionBillingTypeRequest billingTypeConversion = (ConvertSubscriptionBillingTypeRequest) bean;
            billingType = billingTypeConversion.getSubscriberType();
        }
        else
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "Unsupported bean type uses " + this.getClass().getSimpleName());
            }
            
            // No need to filter.
            return condition;
        }
        
        if (billingType != null)
        {
            condition = new EQ(PricePlanXInfo.PRICE_PLAN_TYPE, billingType);
        }
        else
        {
            condition = False.instance();
        }
        
        return condition;
    }


    private Predicate getTechnologyFilter(final Context ctx)
    {
        Predicate filter = True.instance();
        
        final Object bean = ctx.get(AbstractWebControl.BEAN);
        if (bean instanceof TechnologyAware)
        {
            final TechnologyAware techAware = (TechnologyAware) bean;
            final TechnologyEnum technology = techAware.getTechnology();
            if (TechnologyEnum.ANY != technology)
            {
                filter = new EQ(PricePlanXInfo.TECHNOLOGY, technology);
            }
        }
        
        return filter;
    }

    public Predicate getInvalidPricePlanFilter(final Context ctx)
    {
        Predicate condition =  True.instance();
        
        if (PricePlanFunctionEnum.NORMAL == pricePlanFunction_)
        {
            condition = new NEQ(PricePlanXInfo.CURRENT_VERSION, Integer.valueOf(0));
        }
        
        return condition;
    }

    /**
     * Filter out special price plans created for Pool subscriptions based on the ID range.
     */
    public Predicate getSpecialPoolSubscriptionPPsFilter(final Context ctx)
    {
        final And condition = new And();
        
        // remove the ID filtering after migration of Pool price plans to Pool price plan function
        final LT idFilter = new LT(PricePlanXInfo.ID, PricePlanSupport.POOL_PP_ID_START);
        final EQ functionFilter = new EQ(PricePlanXInfo.PRICE_PLAN_FUNCTION, pricePlanFunction_);
        condition.add(idFilter);
        condition.add(functionFilter);
        
        return condition;
    }

    public CustomizedPricePlanKeyWebControl setPricePlanFunction(final PricePlanFunctionEnum pricePlanFunction)
    {
        pricePlanFunction_ = pricePlanFunction;
        return this;
    }
    
    // Sort home.
    public Home getHome(Context ctx)
    {
        Home home = (Home) ctx.get(getHomeKey());
        return new SortingHome(ctx, home, new Comparator(){

            @Override
            public int compare(Object arg0, Object arg1)
            {
                if (arg0 instanceof PricePlan)
                {
                    PricePlan pp0 = (PricePlan) arg0;
                    PricePlan pp1 = (PricePlan) arg1;
                    
                    if (arg0 == null && arg1 == null)
                    {
                        return 0;
                    }
                    else if (arg0 == null)
                    {
                        return -1;
                    }
                    else if (arg1 == null)
                    {
                        return 1;
                    }
                    else
                    {
                        int compareNames = pp0.getName().toLowerCase().compareTo(pp1.getName().toLowerCase());
                        if (compareNames!=0)
                        {
                            return compareNames;
                        }
                        else
                        {
                            return ((Comparable) XBeans.getIdentifier(arg0)).compareTo(XBeans.getIdentifier(arg1));
                        }
                    }
                }
                else
                {
                    return ((Comparable) XBeans.getIdentifier(arg0)).compareTo(XBeans.getIdentifier(arg1));
                }
            }});
    }
    
    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        String pricePlanId = req.getParameter(name);
        final Context subCtx = ctx.createSubContext();
        final Object sub = subCtx.get(AbstractWebControl.BEAN);
        if(pricePlanId == null)
        {
            pricePlanId = "-1";
        }
        long ppid = Long.valueOf(pricePlanId);
        if (sub instanceof ConvertAccountBillingTypeRequest)
        {
            ConvertAccountBillingTypeRequest convertAccountBillingTypeRequest = (ConvertAccountBillingTypeRequest) sub;
            Account parentAccount = null;
            final Account account = convertAccountBillingTypeRequest.getOldAccount(subCtx);
            if (account != null)
            {
                try
                {
                    parentAccount = account.getParentAccount(subCtx);
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to find parent account " + e.getMessage(), e);
                }
            }
            if (parentAccount != null)
            {
                // This will optimize potential future BeanLoader lookups
                subCtx.put(Account.class, account);
                final GroupPricePlanExtension extension = parentAccount.getGroupPricePlanExtension();
                if (extension != null)
                {
                    /*
                     * [Cindy Wong] force update of the subscriber's price plan.
                     */
                    ppid = Long.valueOf(extension.getGroupPricePlan(convertAccountBillingTypeRequest.getSystemType()));
                    return ppid;
                }
            }
        }
        return getDelegate().fromWeb(ctx, req, name);
    }

    protected PricePlanFunctionEnum pricePlanFunction_ = PricePlanFunctionEnum.NORMAL;

    protected boolean hideOverridablePricePlans_;
}
