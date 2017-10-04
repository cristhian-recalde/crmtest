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
package com.trilogy.app.crm.contract;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.web.control.KeyWebControlProxy;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;


/**
 * Custom web control to display all the available contracts
 * 
 */
public class CustomPricePlanKeyWebControl extends KeyWebControlProxy
{

    public static final KeyWebControlOptionalValue DEFAULT = new KeyWebControlOptionalValue("--", "");


    public CustomPricePlanKeyWebControl(final AbstractKeyWebControl keyWebControl)
    {
        super(keyWebControl);
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object value)
    {
        Context subCtx = ctx.createSubContext();
        final Object obj = subCtx.get(AbstractWebControl.BEAN);
        if (obj instanceof com.redknee.app.crm.bean.ui.SubscriptionContractTerm)
        {
            com.redknee.app.crm.bean.ui.SubscriptionContractTerm term = (com.redknee.app.crm.bean.ui.SubscriptionContractTerm) obj;
            Home home = (Home) subCtx.get(PricePlanHome.class);
            And and = new And();
            and.add(new EQ(PricePlanXInfo.SUBSCRIPTION_TYPE, term.getSubscriptionType()));
            and.add(new EQ(PricePlanXInfo.PRICE_PLAN_TYPE, term.getSubscriberType()));
            home = home.where(subCtx, and);
            subCtx.put(PricePlanHome.class, home);
        }
        super.toWeb(subCtx, out, name, value);
    }
}
