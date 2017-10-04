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

package com.trilogy.app.crm.service;

import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.home.ServicePackageVersionHomeProxy;
import com.trilogy.app.crm.home.core.CoreServicePackageVersionHomePipelineFactory;
import com.trilogy.app.crm.priceplan.ServicePackageVersionUniqueServiceValidator;

/**
 * Creates a home pipeline for ServicePackageVersionHome.
 *
 * @author cindy.wong@redknee.com
 */
public class ServicePackageVersionHomePipelineFactory extends CoreServicePackageVersionHomePipelineFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(final Context context, final Context serverContext)
    {
        Home home = super.createPipeline(context, serverContext);

        home = new ServicePackageVersionHomeProxy(home);

        final CompoundValidator validators = new CompoundValidator();
        validators.add(new ServicePackageVersionUniqueServiceValidator());
        validators.add(ServicePackageVersionDatesValidator.instance());

        home = new ValidatingHome(validators, home);
        return home;
    }
}
