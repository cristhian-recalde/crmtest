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

package com.trilogy.app.crm.home.pipelineFactory;

import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.app.crm.bean.CatalogEntityEnum;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationValidator;
import com.trilogy.app.crm.bundle.rateplan.ServiceRatePlanValidator;
import com.trilogy.app.crm.home.CatalogEntityHistoryAdapterHome;
import com.trilogy.app.crm.home.PricePlanServicePeriodValidator;
import com.trilogy.app.crm.home.PricePlanVersionBundlesValidator;
import com.trilogy.app.crm.home.PricePlanVersionHomeProxy;
import com.trilogy.app.crm.home.PricePlanVersionModificationValidator;
import com.trilogy.app.crm.home.PricePlanVersionPrimaryServiceValidator;
import com.trilogy.app.crm.home.PricePlanVersionValidator;
import com.trilogy.app.crm.home.core.CorePricePlanVersionHomePipelineFactory;
import com.trilogy.app.crm.priceplan.PricePlanVersionAdapterHome;
import com.trilogy.app.crm.priceplan.PricePlanVersionCreditLimitValidator;
import com.trilogy.app.crm.priceplan.PricePlanVersionDatesValidator;
import com.trilogy.app.crm.priceplan.PricePlanVersionServiceSubscriptionTypeValidator;
import com.trilogy.app.crm.priceplan.PricePlanVersionUniqueBundleValidator;
import com.trilogy.app.crm.priceplan.PricePlanVersionUniqueServiceValidator;
import com.trilogy.app.crm.priceplan.validator.PricePlanGroupValidator;
import com.trilogy.app.crm.secondarybalance.validator.SingleSecondaryBalanceBundleValidator;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.home.ServicePackageVersionValidator;

/**
 * Creates the home pipeline for {@link PricePlanVersion}.
 *
 * @author cindy.wong@redknee.com
 */
public class PricePlanVersionHomePipelineFactory extends CorePricePlanVersionHomePipelineFactory
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(final Context context, final Context serverContext)
    {
        // Base home should already be installed by AppCrmCore
        Home home = (Home) context.get(PricePlanVersionHome.class);
        home = new PricePlanVersionHomeProxy(context, home);

        final CompoundValidator validators = new CompoundValidator();

        validators.add(SingleSecondaryBalanceBundleValidator.instance());
        validators.add(new PricePlanVersionPrimaryServiceValidator());
        validators.add(new PricePlanGroupValidator());
        validators.add(new PricePlanVersionValidator());
        validators.add(new PricePlanServicePeriodValidator());
        validators.add(new ServiceRatePlanValidator());
        validators.add(new PricePlanVersionUniqueBundleValidator());
        validators.add(new PricePlanVersionUniqueServiceValidator());
        validators.add(new PricePlanVersionServiceSubscriptionTypeValidator());
        validators.add(new RatePlanAssociationValidator());
        validators.add(PricePlanVersionDatesValidator.instance());
        validators.add(new PricePlanVersionBundlesValidator());
        validators.add(new ServicePackageVersionValidator());
        

        home = new ValidatingHome(validators, home);

		home =
		    ConfigChangeRequestSupportHelper.get(context)
		        .registerHomeForConfigSharing(context, home,
		            PricePlanVersion.class);

		home = new PricePlanVersionAdapterHome(context, home);
		// Entry for CatalogEvent History
		home= new CatalogEntityHistoryAdapterHome(context,home,CatalogEntityEnum.PricePlanVersion);
		
        return home;
    }
}
