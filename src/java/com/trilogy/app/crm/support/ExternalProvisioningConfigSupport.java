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

import com.trilogy.app.crm.bean.provision.ExternalProvisionStrategyEnum;
import com.trilogy.app.crm.bean.provision.ExternalProvisioningConfig;
import com.trilogy.framework.xhome.context.Context;

/**
 * Support Utilities for External Provisioning Configuration
 * @author Angie Li
 */
public class ExternalProvisioningConfigSupport 
{
    /**
     * Return the strategy chosen for External Application Provisioning when Creating Subscribers
     * @param context
     * @return
     */
    public static ExternalProvisionStrategyEnum getCreateStrategy(Context context)
    {
        ExternalProvisioningConfig config = (ExternalProvisioningConfig) context.get(ExternalProvisioningConfig.class);
        return config.getCreateStrategy();
    }
    
    /**
     * Return the strategy chosen for External Application Provisioning when Deleting Subscribers
     * @param context
     * @return
     */
    public static ExternalProvisionStrategyEnum getRemoveStrategy(Context context)
    {
        ExternalProvisioningConfig config = (ExternalProvisioningConfig) context.get(ExternalProvisioningConfig.class);
        return config.getRemoveStrategy();
    }
    
    /**
     * Return the strategy chosen for External Application Provisioning when Storing Subscribers
     * @param context
     * @return
     */
    public static ExternalProvisionStrategyEnum getStoreStrategy(Context context)
    {
        ExternalProvisioningConfig config = (ExternalProvisioningConfig) context.get(ExternalProvisioningConfig.class);
        return config.getStoreStrategy();
    }
}
