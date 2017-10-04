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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.home.pipelineFactory.ui;

import java.io.IOException;

import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.driver.BMDriver;
import com.trilogy.app.crm.bundle.profile.AdjustmentTypeCreationHome;
import com.trilogy.app.crm.bundle.profile.CategoryAssociationHome;
import com.trilogy.app.crm.bundle.profile.CategoryValidator;
import com.trilogy.app.crm.bundle.profile.EffectivePeriodValidator;
import com.trilogy.app.crm.bundle.profile.FlexBundleValidator;
import com.trilogy.app.crm.bundle.profile.GroupBundleProfileValidator;
import com.trilogy.app.crm.bundle.profile.RatePlanAssociationRemoveHome;
import com.trilogy.app.crm.bundle.profile.RePurchasableBundleValidator;
import com.trilogy.app.crm.bundle.profile.RecurrenceSchemeValidator;
import com.trilogy.app.crm.bundle.profile.SegmentCategoryValidator;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationCreationHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.secondarybalance.validator.SecondaryBalanceValidatingHome;
import com.trilogy.app.crm.xhome.adapter.BeanAdapter;
import com.trilogy.app.crm.xhome.home.ContextRedirectingHome;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class BundleProfileUIHomePipelineFactory implements PipelineFactory
{

	/**
	 * {@inheritDoc}
	 */
	@Override
    public Home createPipeline(final Context ctx, final Context serverCtx)
	    throws HomeException, IOException, AgentException
	{
		LogSupport.info(ctx, this, "Installing the bundle profile UI home ");

		Home bundleProfileHome =
		    new ContextRedirectingHome(ctx,
		        com.redknee.app.crm.bundle.BundleProfileHome.class);

		bundleProfileHome = new SortingHome(ctx,
                new AdjustmentTypeCreationHome(ctx,
                    new CategoryAssociationHome(ctx,
                    new RatePlanAssociationCreationHome(ctx,
                            new RatePlanAssociationRemoveHome(ctx,
                            new ContextRedirectingHome(ctx, BMDriver.BUNDLE_PROFILE_DRIVER_KEY))))));
    
		
        CompoundValidator validator = new CompoundValidator();
        validator.add(new CategoryValidator());
        validator.add(new RecurrenceSchemeValidator());
        validator.add(new EffectivePeriodValidator());
        validator.add(new GroupBundleProfileValidator());
        validator.add(new SegmentCategoryValidator());
        validator.add(new FlexBundleValidator());
        validator.add(new RePurchasableBundleValidator());
        bundleProfileHome = new SecondaryBalanceValidatingHome(new ValidatingHome(bundleProfileHome, validator));
        
		// Add adapter to switch between GUI bean and core beans.
		bundleProfileHome =
		    new AdapterHome(
		        ctx,
		        bundleProfileHome,
		        new BeanAdapter<com.redknee.app.crm.bean.core.BundleProfile, com.redknee.app.crm.bean.ui.BundleProfile>(
		            com.redknee.app.crm.bean.core.BundleProfile.class,
		            com.redknee.app.crm.bean.ui.BundleProfile.class));
		
		

		return bundleProfileHome;
	}
}
