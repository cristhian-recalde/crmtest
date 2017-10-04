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
package com.trilogy.app.crm.priceplan;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanFunctionEnum;

/**
 * Set or overwrite the PricePlanFunction field in the Price Plan.
 *
 * @author victor.stratan@redknee.com
 */
public class SetVPNPricePlanFunctionWebControl extends ProxyWebControl
{
    /**
     * @param delegate home delegate
     */
    public SetVPNPricePlanFunctionWebControl(final WebControl delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    public void fromWeb(final Context ctx, final Object bean, final ServletRequest request, final String name)
    {
        super.fromWeb(ctx, bean, request, name);
        final PricePlan pp = (PricePlan) bean;
        pp.setPricePlanFunction(PricePlanFunctionEnum.VPN);
    }

    /**
     * {@inheritDoc}
     */
    public Object fromWeb(final Context ctx, final ServletRequest request, final String name)
    {
        final PricePlan pp = (PricePlan) super.fromWeb(ctx, request, name);
        pp.setPricePlanFunction(PricePlanFunctionEnum.VPN);
        return pp;
    }
}
