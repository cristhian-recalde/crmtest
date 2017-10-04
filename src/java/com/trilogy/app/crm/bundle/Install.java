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
package com.trilogy.app.crm.bundle;

import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.RMIHomeServer;
import com.trilogy.framework.xhome.home.ReadOnlyHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.TestSerializabilityHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.CritLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bundle.category.NoBundleRemoveHome;
import com.trilogy.app.crm.bundle.category.RemoveRatePlanAssociationHome;
import com.trilogy.app.crm.bundle.driver.BMDriver;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationHome;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationXDBHome;
import com.trilogy.app.crm.bundle.validator.BalanceTypeValidator;
import com.trilogy.app.crm.home.BundleAdjustmentBulkAdapterAgent;
import com.trilogy.app.crm.home.BundleAdjustmentValidatorAgent;
import com.trilogy.app.crm.home.RatePlanAssociationIDSettingHome;
import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;
import com.trilogy.app.crm.xhome.home.ContextRedirectingHome;

/**
 * Installs all the homes, beans and services for the Bundle Manager feature.
 *
 * @author paul.sperneac@redknee.com
 */
public class Install implements ContextAgent
{
    public void execute(final Context ctx) throws AgentException
    {
        try
        {
            // Add extra Home decoration where required
            ctx.put(BalanceApplicationHome.class,
                    new ValidatingHome(
                            new BalanceTypeValidator(),
                            (Home) ctx.get(BalanceApplicationHome.class)));

            //[amedina] required by selfcare only for BAS
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                try
                {
                    new RMIHomeServer(ctx,
                            new ReadOnlyHome(
                                    new PMHome(ctx,
                                            BalanceApplicationHome.class.getName(),
                                            (Home) ctx.get(BalanceApplicationHome.class))),
                            BalanceApplicationHome.class.getName()).register();
                }
                catch (java.rmi.RemoteException e)
                {
                    new MajorLogMsg(this,
                            "failed to register BalanceApplicationHome: " + e.getMessage(), e).log(ctx);
                }
            }



            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                try
                {
                    new RMIHomeServer(ctx,
                            new ReadOnlyHome(
                                    new PMHome(ctx,
                                            BundleProfileHome.class.getName(),
                                            (Home) ctx.get(BundleProfileHome.class))),
                            BundleProfileHome.class.getName()).register();
                }
                catch (java.rmi.RemoteException e)
                {
                    new MajorLogMsg(this, "failed to register BundleProfileHome: " + e.getMessage(), e).log(ctx);
                }
            }

            ctx.put(BMBundleCategoryAssociationHome.class,
                            new BMBundleCategoryAssociationXDBHome(ctx, "BundleCategoryAssociation"));

            
            if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isSingle(ctx))
            {
                try
                {
                    new RMIHomeServer(ctx,
                            new ReadOnlyHome(
                                    new PMHome(ctx,
                                            BMBundleCategoryAssociationHome.class.getName(),
                                            (Home) ctx.get(BMBundleCategoryAssociationHome.class))),
                            BMBundleCategoryAssociationHome.class.getName()).register();
                }
                catch (java.rmi.RemoteException e)
                {
                    new MajorLogMsg(this, "failed to register BMBundleCategoryAssociationHome: " + e.getMessage(), e).log(ctx);
                }
            }

            ctx.put(BundleCategoryHome.class,
                            new SortingHome(ctx,
                                    new RemoveRatePlanAssociationHome(ctx,
                                            new NoBundleRemoveHome(ctx,
                                                    new ContextRedirectingHome(ctx, BMDriver.BUNDLE_CATEGORY_DRIVER_KEY)))));

            ctx.put(SubscriberBucketHome.class,
                        new ContextRedirectingHome(ctx, BMDriver.SUBSCRIBER_BUCKET_DRIVER_KEY));
            ctx.put(BundleAdjustment.class, BundleAdjustmentFactory.instance());

            ctx.put(BundleAdjustmentAgent.class,
                    new BundlePrepareDataAgent(
                            new BundleCallAgent(
                                    new BundleSubscriberNoteCreateAgent(null))));
            
            ctx.put(BundleBulkAdjustmentAgent.class, 
            		new BundleAdjustmentValidatorAgent(
						new BundleAdjustmentBulkAdapterAgent(
								(ContextAgent)ctx.get(BundleAdjustmentAgent.class))));

            ctx.put(RatePlanAssociationHome.class,
                    new RatePlanAssociationIDSettingHome(ctx, new SpidAwareHome(ctx,
                            new TestSerializabilityHome(ctx,
                                    new RatePlanAssociationXDBHome(ctx)))));

        }
        catch (Throwable t)
        {
            new CritLogMsg(this, "fail to install BundleMgr", t).log(ctx);
        }
    }

}
