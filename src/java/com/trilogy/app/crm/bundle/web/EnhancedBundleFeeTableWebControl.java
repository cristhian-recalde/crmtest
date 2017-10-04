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

package com.trilogy.app.crm.bundle.web;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.filter.AndPredicate;
import com.trilogy.framework.xhome.filter.NotPredicate;
import com.trilogy.framework.xhome.filter.OrPredicate;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.HashSetBuildingVisitor;
import com.trilogy.framework.xhome.visitor.MapVisitor;
import com.trilogy.framework.xhome.visitor.PredicateVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bundle.BundleFee;
import com.trilogy.app.crm.bundle.BundleFeeTableWebControl;
import com.trilogy.app.crm.bundle.BundleFeeXInfo;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.filter.AuxiliaryBundleFeePredicate;
import com.trilogy.app.crm.filter.MandatoryBundleFeePredicate;
import com.trilogy.app.crm.filter.OneTimeBundleFromFeePredicate;
import com.trilogy.app.crm.filter.SwitchToContextBeanProxyPredicate;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.web.control.BundleFeeFeeWebControl;
import com.trilogy.app.crm.web.control.HideOnConditionWebControl;
import com.trilogy.app.crm.web.control.PersistentReadOnlyOnPredicateWebControl;
import com.trilogy.app.crm.web.renderer.GroupByTableRenderer;

/**
 * BundleFeeTableWebControl which displays bundle sources and forces selection of mandatory bundles.
 *
 * @author kevin.greer@redknee.com
 */
public class EnhancedBundleFeeTableWebControl extends BundleFeeTableWebControl
{
    /**
     * Comparator for bundle source, then ID.
     *
     * @author cindy.wong@redknee.com
     * @since 2008-07-29
     */
    private static final class BundleSourceIdComparator implements Comparator
    {

        public int compare(final Object o1, final Object o2)
        {
            final BundleFee f1 = (BundleFee) o1;
            final BundleFee f2 = (BundleFee) o2;
   
            if (f1.getSource().equals(f2.getSource()))
            {
                return (int) (f1.getId() - f2.getId());
            }
   
            if (f1.isAuxiliarySource())
            {
                return 1;
            }
            if (f2.isAuxiliarySource())
            {
                return -1;
            }
   
            return f1.getSource().compareTo(f2.getSource());
        }
    }

    /**
     * Context key for the first auxiliary bundle available for selection. Used to keep
     * track of the fee.
     */
    public static final String FIRST_AUX_BUNDLE_KEY = "EnhandedBundleFeeTableWebControl.FirstAuxBundle";

    WebControl id_wc_ = new ProxyWebControl(id_wc)
    {
        /**
         * Make the ID field for all non-auxiliary bundles display only and include it as a hidden field value.
         * @param ctx current context
         * @param out see interface definition
         * @param name see interface definition
         * @param obj see interface definition
         */
        @Override
        public void toWeb(Context ctx, final PrintWriter out, final String name, final Object obj)
        {
            final BundleFee fee = (BundleFee) ctx.get(BEAN);

            if (!fee.isAuxiliarySource())
            {
                ctx = ctx.createSubContext();

                ctx.put("MODE", DISPLAY_MODE);

                out.print("<input type=\"hidden\" name=\"");
                out.print(name);
                out.print("\" value=\"" + obj + "\" />");
            }

            super.toWeb(ctx, out, name, obj);
        }
    };

    private static final Predicate NonAuxiliaryPredicate_ = new NotPredicate(new AuxiliaryBundleFeePredicate());
    private static final Predicate OneTimePredicate_ = new OneTimeBundleFromFeePredicate();
    private static final Predicate NonAuxiliaryOrOneTimePredicate_ = new OrPredicate()
            .add(NonAuxiliaryPredicate_).add(OneTimePredicate_);
    private static final Predicate NonAuxiliaryAndMandatoryPredicate_ = new AndPredicate()
            .add(NonAuxiliaryPredicate_).add(new MandatoryBundleFeePredicate());
    private static final Predicate NonAuxiliaryAndMandatoryAndNotOneTimePredicate_ = new AndPredicate()
            .add(NonAuxiliaryAndMandatoryPredicate_).add(new NotPredicate(OneTimePredicate_));

    private static final Predicate OneTime_ =
        new SwitchToContextBeanProxyPredicate(OneTimePredicate_);
    private static final Predicate NonAuxiliaryAndMandatoryAndNotOneTime_ =
            new SwitchToContextBeanProxyPredicate(NonAuxiliaryAndMandatoryAndNotOneTimePredicate_);
    private static final Predicate NonAuxiliaryOrOneTime_ =
            new SwitchToContextBeanProxyPredicate(NonAuxiliaryOrOneTimePredicate_);

    WebControl startDate_wc_ = new HideOnConditionWebControl(NonAuxiliaryAndMandatoryAndNotOneTime_, new PersistentReadOnlyOnPredicateWebControl(OneTime_, startDate_wc));

    WebControl endDate_wc_ = new HideOnConditionWebControl(NonAuxiliaryAndMandatoryAndNotOneTime_, new PersistentReadOnlyOnPredicateWebControl(OneTime_, endDate_wc)); 
    
    WebControl paymentNum_wc_ = new HideOnConditionWebControl(NonAuxiliaryOrOneTime_, paymentNum_wc);

    /**
     * Bundle fee web control.
     */
    WebControl fee_wc_ = new BundleFeeFeeWebControl(fee_wc);

    @Override
    public WebControl getIdWebControl()
    {
        return id_wc_;
    }

    @Override
    public WebControl getStartDateWebControl()
    {
        return startDate_wc_;
    }

    @Override
    public WebControl getEndDateWebControl()
    {
        return endDate_wc_;
    }

    @Override
    public WebControl getPaymentNumWebControl()
    {
        return paymentNum_wc_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getFeeWebControl()
    {
        return fee_wc_;
    }

	public void setBundleFees(final Context ctx, final List list) {

		for (final Iterator i = list.iterator(); i.hasNext();) {
			
			final BundleFee fee = (BundleFee) i.next();
			
			try {
				
				BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, fee.getId());
				fee.setServicePeriod(bundle.getChargingRecurrenceScheme());
				if (fee.isAuxiliarySource()) 
				{

					fee.setFee(bundle.getAuxiliaryServiceCharge());
				}
			} catch (final Throwable t) {
				new MinorLogMsg(this, "Invalid bundle id: " + fee.getId(), null)
						.log(ctx);

				i.remove();
			}
		}

	}


    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final Context subCtx = ctx.createSubContext();
        com.redknee.framework.xhome.web.action.ActionMgr.disableActions(subCtx);

        final Subscriber sub = (Subscriber) subCtx.get(BEAN);
        //final List beans = new ArrayList(SubscriberBundleSupport.getAllBundles(subCtx, sub).values());

        List tempBeans = (List) obj; 
    	
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
 
        if ( mode == EDIT_MODE || mode == CREATE_MODE )
        {
        	tempBeans = new ArrayList(SubscriberBundleSupport.getAllBundles(subCtx, sub).values());
        } else 
        {
        	tempBeans =  new ArrayList(SubscriberBundleSupport.getSubscribedBundles(subCtx, sub).values());
        }
         
        final List beans = tempBeans;
        
        /*
         * [Cindy Wong] TT 8062000027: Puts the first available bundle in the context, so
         * BundleFeeFeeWebControl knows what fee to display for this bundle. This is not a
         * very elegant hack, but it's the only way to figure out which bundle would be
         * automatically selected in the GUI drop-down when displaying a blank row,
         * without rewriting any framework-generated web controls.
         */
        final List auxBundles = new ArrayList(SubscriberBundleSupport.getAvailableAuxiliaryBundles(subCtx, sub)
            .values());
        if (auxBundles != null && auxBundles.size() > 0)
        {
            Collections.sort(auxBundles, new BundleSourceIdComparator());
            subCtx.put(FIRST_AUX_BUNDLE_KEY, auxBundles.iterator().next());
        }

        final Home bundleHome = (Home) subCtx.get(BundleProfileHome.class);

        subCtx.put(BundleProfileHome.class, new ContextFactory()
        {
            public Object create(final Context ctx)
            {
                try
                {
                    final Object bean = ctx.get(BEAN);
                    if (!(bean instanceof BundleFee))
                    {
                        return bundleHome;
                    }

                    final BundleFee bundle = (BundleFee) bean;
                    if (!bundle.isAuxiliarySource())
                    {
                        return bundleHome;
                    }

                    // TODO 2009-02-02 take this out of the ContextFactory as optimization
                    final HashSetBuildingVisitor set = new HashSetBuildingVisitor();

                    Visitors.forEach(ctx, beans, new PredicateVisitor(
                            new NEQ(BundleFeeXInfo.SOURCE, BundleFee.AUXILIARY),
                            new FunctionVisitor(BundleFeeXInfo.ID, set)));

                    final And condition = new And();
                    condition.add(new EQ(BundleProfileXInfo.AUXILIARY, Boolean.TRUE));
                    condition.add(new Not(new In(BundleProfileXInfo.BUNDLE_ID, set)));
                                        
                    return SubscriberBundleSupport.filterExpiredOneTimeBundles(ctx, bundleHome.where(ctx, condition));
                }
                catch (final Throwable t)
                {
                    new MinorLogMsg(this, "Internal Error", t).log(ctx);

                    return bundleHome;
                }
            }
        });

        // Compare by Source with "Auxiliary" coming last
        Collections.sort(beans, new BundleSourceIdComparator());

        setBundleFees(subCtx, beans);

        setMode(subCtx, BundleFeeXInfo.SERVICE_PREFERENCE, ViewModeEnum.NONE);
        setMode(subCtx, BundleFeeXInfo.FEE, ViewModeEnum.READ_ONLY);
        //setMode(subCtx, BundleFeeXInfo.SERVICE_PERIOD, ViewModeEnum.READ_ONLY);
        setMode(subCtx, BundleFeeXInfo.PAID_BY_CONTRACT, ViewModeEnum.NONE);
        setMode(subCtx, BundleFeeXInfo.APPLY_WITHIN_MRC_GROUP, ViewModeEnum.NONE);
        setMode(subCtx, BundleFeeXInfo.CHARGE_FAILURE_ACTION, ViewModeEnum.READ_ONLY);
        // find all possible bundles
        // sort by package
        // add rows
        // alter tablerenderer
        // add auxillary bundles

        super.toWeb(subCtx, out, name, beans);
    }


    @Override
    public TableRenderer tableRenderer(final Context ctx)
    {
        final TableRenderer renderer = super.tableRenderer(ctx);
        return new GroupByTableRenderer(ctx, BundleFeeXInfo.SOURCE, renderer);
    }


    @Override
    public void outputCheckBox(final Context ctx, final PrintWriter out, final String name, final Object bean,
            final boolean isChecked)
    {
        final BundleFee bundle = (BundleFee) bean;

        final int mode = ctx.getInt("MODE", DISPLAY_MODE);

        if (!isChecked)
        {
            // during this call isChecked only determines if this is an actual bean
            // or the spare lines that are displayed in order to be able to add new beans
            super.outputCheckBox(ctx, out, name, bean, isChecked);
        }
        else if (bundle.getServicePreference() == ServicePreferenceEnum.MANDATORY)
        {
            out.print("<td><input type=\"checkbox\" name=\"X");
            out.print(name);
            out.print("X\" disabled value=\"X\" checked=\"checked\" /> ");

            out.print("<input type=\"hidden\" name=\"");
            out.print(name);
            out.print(SEPERATOR);
            out.print("_enabled\" value=\"X\" /> </td>");
        }
        else if (mode == DISPLAY_MODE)
        {
            // it looks like this is never executed
            out.print("<td>&nbsp;</td>");
        }
        else
        {
            Subscriber sub = (Subscriber) ctx.get(BEAN);
            List bundleIds = null;
            try
            {
                bundleIds = new ArrayList((List) Visitors.forEach(ctx, sub.getBundles(), new MapVisitor(BundleFeeXInfo.ID)));
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "Cannot add subscribed bundles to bundle list", e).log(ctx);
                bundleIds = new ArrayList();
            }

            super.outputCheckBox(ctx, out, name, bean, bundleIds.contains(Long.valueOf(bundle.getId())));
        }
    }
}