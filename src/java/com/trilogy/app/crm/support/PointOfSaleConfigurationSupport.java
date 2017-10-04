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
package com.trilogy.app.crm.support;

import com.trilogy.app.crm.pos.PointOfSaleConfiguration;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.framework.core.bean.Application;
import com.trilogy.framework.xhome.context.Context;

/**
 * Support methods for POS Configuration
 * @author ali
 */
public class PointOfSaleConfigurationSupport 
{
    /**
     * Gets the PointOfSaleConfiguration from the context.
     *
     * @param ctx The operating context.
     *
     * @return The configuration parameters for invoice generation.
     */
    public static PointOfSaleConfiguration getPOSConfig(final Context ctx)
    {
        PointOfSaleConfiguration config = (PointOfSaleConfiguration) ctx.get(PointOfSaleConfiguration.class);
        if (config == null)
        {
            Application app = (Application) ctx.get(Application.class);
            
            config = new PointOfSaleConfiguration();
            config.setRepositoryDirectory("/tmp");
            config.setLogDirectory("/tmp");
            
            ReportUtilities.logMajor(
                ctx,
                PointOfSaleConfiguration.class.getName(),
                "Could not find PointOfSaleConfiguration in context.  Using \"{0}\" instead.",
                new Object[] { config },
                null);
        }

        return config;
    }
}
