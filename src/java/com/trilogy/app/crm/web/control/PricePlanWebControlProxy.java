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

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.ServicePackageVersionXInfo;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.WebControlSupport;
import com.trilogy.app.crm.support.WebControlSupportHelper;

/**
 * Web Control Proxy controls the visibility of fields depending on
 * bean attributes.
 *
 * Fields exposed/hidden here are:
 *   Price Plan Version's Credit Limit, depending on the Price Plan's Type.
 *   Voice, SMS, Date rate plans and overusage rates based on Price Plan's Subscription Type 
 *
 * @author angie.li@redknee.com
 * @author victor.stratan@redknee.com
 *
 */
public class PricePlanWebControlProxy extends ProxyWebControl
{
    /**
     * @param delegate
     */
    public PricePlanWebControlProxy(final WebControl delegate)
    {
        super(delegate);
    }

    @Override
    public void toWeb(final Context ctx, final PrintWriter p1, final String p2, final Object p3)
    {
        final PricePlan priceplan = (PricePlan) p3;

        final Context subCtx = HomeSupportHelper.get(ctx).getWhereContext(
                ctx, 
                Service.class, 
                new EQ(ServiceXInfo.SUBSCRIPTION_TYPE, priceplan.getSubscriptionType()));
        
        customizePricePlanVersionView(subCtx, priceplan);
        
        subCtx.put(PricePlan.class, priceplan);

        //Delegate to the super web control
        super.toWeb(subCtx, p1, p2, p3);
    }

    /**
     * Set the visibility of the Credit Limit field depending on the Price Plan Type.
     * Hide for Prepaid Price Plan Types, set visible and editable otherwise.
     * @param ctx
     * @param plan - price plan can be NULL if there was an error retrieving it
     */
    private void customizePricePlanVersionView(final Context ctx, final PricePlan plan)
    {
        boolean isServiceSubscriptionType = true;
        WebControlSupport wcSupport = WebControlSupportHelper.get(ctx);
        if (plan != null)
        {
            final SubscriptionType subType = plan.getSubscriptionType(ctx);
            isServiceSubscriptionType = subType == null || subType.isService();

            if (SubscriberTypeEnum.PREPAID.equals(plan.getPricePlanType()))
            {
                // Hide the Credit Limit field.
                
                /* 
                 * The reason that we are setting the visibility of the Price Plan VERSION's Credit Limit
                 * attribute here instead of in a Price Plan Version Web Control Proxy is that:
                 * 1) Although Price plans must exist before we can create versions for them, the first default
                 * version for a price plan with no versions defined has default ID of 0 (zero).  A Lookup on
                 * Price Plan ID 0 will return NULL for a price plan always and won't allow us to check the real
                 * price plan's Type.  Now we do the check for the Price Plan Type here and set the visibility
                 * accordingly.
                 * 2) Added benefit that no additional lookup is needed. (looking up the price plan from the
                 * price plan version).
                 */
                
                wcSupport.hideProperty(ctx, PricePlanVersionXInfo.CREDIT_LIMIT);
                WebControlSupportHelper.get(ctx).hideProperty(ctx, PricePlanVersionXInfo.DEPOSIT);                
            }
        }
        if (!isServiceSubscriptionType)
        {
            wcSupport.hideProperty(ctx, PricePlanXInfo.VOICE_RATE_PLAN);
            wcSupport.hideProperty(ctx, PricePlanXInfo.SMSRATE_PLAN);
            wcSupport.hideProperty(ctx, PricePlanXInfo.DATA_RATE_PLAN);
            wcSupport.hideProperty(ctx, PricePlanVersionXInfo.DEFAULT_PER_MINUTE_AIR_RATE);
            wcSupport.hideProperty(ctx, PricePlanVersionXInfo.OVERUSAGE_VOICE_RATE);
            wcSupport.hideProperty(ctx, PricePlanVersionXInfo.OVERUSAGE_SMS_RATE);
            wcSupport.hideProperty(ctx, PricePlanVersionXInfo.OVERUSAGE_DATA_RATE);

            wcSupport.hideProperty(ctx, ServicePackageVersionXInfo.PACKAGE_FEES);
        }
    }
}
