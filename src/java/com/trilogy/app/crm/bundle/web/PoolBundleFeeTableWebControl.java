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
package com.trilogy.app.crm.bundle.web;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bundle.BundleFee;
import com.trilogy.app.crm.bundle.BundleFeeTableWebControl;
import com.trilogy.app.crm.bundle.BundleFeeXInfo;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileTransientHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.filter.AuxiliaryBundleFeePredicate;
import com.trilogy.app.crm.filter.MandatoryBundleFeePredicate;
import com.trilogy.app.crm.filter.OneTimeBundleFromFeePredicate;
import com.trilogy.app.crm.filter.SwitchToContextBeanProxyPredicate;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.AndPredicate;
import com.trilogy.framework.xhome.filter.NotPredicate;
import com.trilogy.framework.xhome.filter.OrPredicate;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.HiddenWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.product.bundle.manager.provision.bundle.RecurrenceScheme;


/**
 * PoolBundleFeeTableWebControl That modifies the Behaviour of BundleFeeWebControl to This
 * ideally should be rewritten; but was coped from EnhacedBundleFeeTabWebControl
 * 
 * @author simar.singh@redknee.com
 */
public class PoolBundleFeeTableWebControl extends BundleFeeTableWebControl
{

    WebControl id_wc_ = new ProxyWebControl(new HiddenWebControl(new ReadOnlyWebControl(id_wc)))
    {

        /**
         * Make the ID field for all non-auxiliary bundles display only and include it as
         * a hidden field value.
         * 
         * @param ctx
         *            current context
         * @param out
         *            see interface definition
         * @param name
         *            see interface definition
         * @param obj
         *            see interface definition
         */
        @Override
        public void toWeb(Context ctx, final PrintWriter out, final String name, final Object obj)
        {
            ctx = ctx.createSubContext();
            ctx.put("MODE", DISPLAY_MODE);
            out.print("<input type=\"hidden\" name=\"");
            out.print(name);
            out.print("\" value=\"" + obj + "\" />");
            super.toWeb(ctx, out, name, obj);
        }
    };


    public PoolBundleFeeTableWebControl()
    {
    }


    @Override
    public WebControl getIdWebControl()
    {
        return id_wc_;
    }


    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final Context subCtx = setContextForTableViewProperties(ctx.createSubContext());
        Collection<com.redknee.app.crm.bean.core.BundleFee> bundlFeeCollection;
        Home bundleHome;
        try
        {
            final Object parentBean = subCtx.get(BEAN);
            if (null == parentBean || !(parentBean instanceof PoolExtension))
            {
                throw new IllegalStateException("Did not find Parent Bean of Pool-Extension in context. [" + parentBean
                        + "]");
            }
            PoolExtension poolExtension = ((PoolExtension) parentBean);
            bundleHome = (Home) subCtx.get(BundleProfileHome.class);
            if (null == bundleHome)
            {
                throw new IllegalStateException("Could not find Bundle-Profile Home in the context");
            }
            bundleHome = bundleHome.where(subCtx, getPoolBundleFilter(subCtx, poolExtension));
            bundlFeeCollection = PoolExtension.transformBundles(subCtx, bundleHome.selectAll(ctx));
        }
        catch (Throwable t)
        {
            bundleHome = new BundleProfileTransientHome(subCtx);
            bundlFeeCollection = new ArrayList<com.redknee.app.crm.bean.core.BundleFee>();
            handleException(subCtx, t);
        }
        // install filtered bundle-home
        subCtx.put(BundleProfileHome.class, bundleHome);
        setPoolBundles(subCtx, bundlFeeCollection);
        super.toWeb(subCtx, out, name, bundlFeeCollection);
    }


    @Override
    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
        super.fromWeb(setContextForTableViewProperties(ctx), obj, req, name);
    }


    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        return super.fromWeb(setContextForTableViewProperties(ctx), req, name);
    }


    @Override
    public void outputCheckBox(Context ctx, PrintWriter out, String name, Object bean, boolean checked)
    {
        // mark the bundles that aren't already provisioned to
        final BundleFee poolBundle = (com.redknee.app.crm.bundle.BundleFee) bean;
        if (poolBundle.getId() < 0)
        {
            return;
        }
        if (checked)
        {
            final PoolExtension poolExtension = (PoolExtension) ctx.get(BEAN);
            if (!poolExtension.getPoolBundles().containsKey(poolBundle.getId()))
            {
                checked = false;
            }
        }
        super.outputCheckBox(ctx, out, name, bean, checked);
    }


    private Predicate getPoolBundleFilter(Context ctx, PoolExtension poolExtension)
    {
        // get auxillary bundles of segment type
        final And condition;
        {
            BundleSegmentEnum segment = null;
            SubscriberTypeEnum subType = null;
            // Fix for TCBSUP-541
            if(poolExtension.getBAN()!= null && !poolExtension.getBAN().equals(""))
            {
            	subType = poolExtension.getSubscriberType(ctx);
            }
            
            if(subType == null)
            {
            	Object obj = com.redknee.app.crm.support.ExtensionSupportHelper.get(ctx).getParentBean(ctx);
            	if(obj instanceof AccountCreationTemplate)
            	{
            		AccountCreationTemplate acctTmp = (AccountCreationTemplate) obj;
            		if(acctTmp != null)
    	    		{
    	    			subType = acctTmp.getSystemType();
    	    		}
            	}
            	else if(obj instanceof Account) // Fix for TCBSUP-541 - When searched from Account create Account object will be passed.
            	{
            		Account acctTmp = (Account) obj;
            		if(acctTmp != null)
    	    		{
    	    			subType = acctTmp.getSystemType();
    	    		}
            	}
            }
            
            if (SubscriberTypeEnum.PREPAID == subType)
            {
                segment = BundleSegmentEnum.PREPAID;
            }
            else if (SubscriberTypeEnum.POSTPAID == subType)
            {
                segment = BundleSegmentEnum.POSTPAID;
            }
            // filter out expired one time bundles
            final Or or;
            {
                or = new Or();
                or.add(new EQ(BundleProfileXInfo.RECURRENCE_SCHEME, Integer
                        .valueOf(RecurrenceScheme._RECUR_CYCLE_FIXED_DATETIME)));
                or.add(new EQ(BundleProfileXInfo.RECURRENCE_SCHEME, Integer
                        .valueOf(RecurrenceScheme._RECUR_CYCLE_FIXED_INTERVAL)));
                or.add(new EQ(BundleProfileXInfo.RECURRENCE_SCHEME, Integer
                        .valueOf(RecurrenceScheme._ONE_OFF_FIXED_INTERVAL)));
                or.add(new GTE(BundleProfileXInfo.END_DATE, new Date()));
                or.add(new EQ(BundleProfileXInfo.END_DATE, new Date(0)));
            }
            condition = new And();
            condition.add(new EQ(BundleProfileXInfo.AUXILIARY, Boolean.TRUE));
            if(subType != null && !subType.equals(SubscriberTypeEnum.HYBRID))
            {
            	condition.add(new EQ(BundleProfileXInfo.SEGMENT, segment));
            }
            condition.add(new EQ(BundleProfileXInfo.GROUP_CHARGING_SCHEME, GroupChargingTypeEnum.GROUP_BUNDLE));
            condition.add(new EQ(BundleProfileXInfo.SPID, poolExtension.getSpid()));
            // condition.add(or);
        }
        return condition;
    }


    private void setPoolBundles(final Context ctx, final Collection<com.redknee.app.crm.bean.core.BundleFee> list)
    {
        /**
         * At present due to Design limitation, we con not charge pool bundles. In future
         * this fee negation may need to be removed
         */
        for (final Iterator i = list.iterator(); i.hasNext();)
        {
            final BundleFee fee = (BundleFee) i.next();
            fee.setFee(0);
            fee.setSource(BundleFee.AUXILIARY);
        }
    }


    private Context setContextForTableViewProperties(Context subCtx)
    {
        subCtx.put(com.redknee.framework.xhome.webcontrol.AbstractTableWebControl.ENABLE_ADDROW_BUTTON, false);
        subCtx.put(com.redknee.framework.xhome.webcontrol.AbstractTableWebControl.DISABLE_NEW, false);
        subCtx.put(com.redknee.framework.xhome.webcontrol.AbstractTableWebControl.HIDE_CHECKBOX, false);
        subCtx.put(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY, false);
        com.redknee.framework.xhome.web.action.ActionMgr.disableActions(subCtx);
        setMode(subCtx, BundleFeeXInfo.FEE, ViewModeEnum.NONE);
        setMode(subCtx, BundleFeeXInfo.SERVICE_PREFERENCE, ViewModeEnum.NONE);
        setMode(subCtx, BundleFeeXInfo.SERVICE_PERIOD, ViewModeEnum.READ_ONLY);
        setMode(subCtx, BundleFeeXInfo.PAYMENT_NUM, ViewModeEnum.NONE);
        setMode(subCtx, BundleFeeXInfo.END_DATE, ViewModeEnum.NONE);
        setMode(subCtx, BundleFeeXInfo.START_DATE, ViewModeEnum.NONE);
        setMode(subCtx, BundleFeeXInfo.PAID_BY_CONTRACT, ViewModeEnum.NONE);
        setMode(subCtx, BundleFeeXInfo.APPLY_WITHIN_MRC_GROUP, ViewModeEnum.NONE);
        subCtx.put(NUM_OF_BLANKS, 0);
        return subCtx;
    }


    private void handleException(Context ctx, Throwable t)
    {
        ExceptionListener excl = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (null != excl)
        {
            excl.thrown(t);
        }
    }

    private static final Predicate NonAuxiliaryPredicate_ = new NotPredicate(new AuxiliaryBundleFeePredicate());
    private static final Predicate OneTimePredicate_ = new OneTimeBundleFromFeePredicate();
    private static final Predicate NonAuxiliaryOrOneTimePredicate_ = new OrPredicate().add(NonAuxiliaryPredicate_).add(
            OneTimePredicate_);
    private static final Predicate NonAuxiliaryAndMandatoryPredicate_ = new AndPredicate().add(NonAuxiliaryPredicate_)
            .add(new MandatoryBundleFeePredicate());
    private static final Predicate NonAuxiliaryAndMandatoryAndNotOneTimePredicate_ = new AndPredicate().add(
            NonAuxiliaryAndMandatoryPredicate_).add(new NotPredicate(OneTimePredicate_));
    private static final Predicate OneTime_ = new SwitchToContextBeanProxyPredicate(OneTimePredicate_);
    private static final Predicate NonAuxiliaryAndMandatoryAndNotOneTime_ = new SwitchToContextBeanProxyPredicate(
            NonAuxiliaryAndMandatoryAndNotOneTimePredicate_);
    private static final Predicate NonAuxiliaryOrOneTime_ = new SwitchToContextBeanProxyPredicate(
            NonAuxiliaryOrOneTimePredicate_);
}
