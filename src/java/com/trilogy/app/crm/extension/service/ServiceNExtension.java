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
package com.trilogy.app.crm.extension.service;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.ui.Product;

/**
 * 
 *
 * @author 
 * @since 
 */
public class ServiceNExtension extends AbstractServiceNExtension
{
    /**
     * {@inheritDoc}
     */
    public String getSummary(Context ctx)
    {
        return "Service ID=" + this.getServiceId();
    }

    /**
     * @param ctx
     *            The operating context
     * @return This extension's service bean
     */
    public Product getService(Context ctx)
    {
    	Product service = null;
        try
        {
            Home home = (Home) ctx.get(Product.class);
            service = (Product) home.find(ctx, this.getServiceId());
        }
        catch (HomeException e)
        {
        }

        if (service != null
                && service.getProductId() == this.getServiceId())
        {
            return service;
        }

        return null;
    }
}
