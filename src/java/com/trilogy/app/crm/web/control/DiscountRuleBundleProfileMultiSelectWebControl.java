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
package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.ui.DiscountRule;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

/**
 * 
 * @author ishan.batra
 * @since  9.9
 *
 */
public class DiscountRuleBundleProfileMultiSelectWebControl extends BundleProfileMultiSelectWebControl
{ 
    public DiscountRuleBundleProfileMultiSelectWebControl(boolean pricePlanBundles, boolean auxiliaryBundles)
    {
        super(pricePlanBundles,auxiliaryBundles);
    }

    @Override
    public Context filterSubscriberType(final Context ctx)
    {
        final Home originalHome = (Home) ctx.get(BundleProfileHome.class);
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        BundleSegmentEnum subType = null;
        
        if (obj instanceof DiscountRule)
        {
        	DiscountRule discountRule = (DiscountRule) obj;
            
            if (SubscriberTypeEnum.POSTPAID_INDEX  == discountRule.getBillingType())
            {
                subType = BundleSegmentEnum.POSTPAID;
            }
            else if (SubscriberTypeEnum.PREPAID_INDEX == discountRule.getBillingType())
            {
                subType = BundleSegmentEnum.PREPAID;
            }
        }
           
        if (subType!=null)
        {
            final Predicate typePredicate = new EQ(BundleProfileXInfo.SEGMENT, subType);
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, typePredicate);
            final Context subCtx = ctx.createSubContext();
            subCtx.put(BundleProfileHome.class, newHome);
            return subCtx;
        }
        return ctx;
    }

}
