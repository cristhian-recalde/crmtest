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
package com.trilogy.app.crm;

import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeHome;
import com.trilogy.app.crm.bean.account.SubscriptionTypeTransientHome;
import com.trilogy.app.crm.bean.service.ExternalAppMappingHome;
import com.trilogy.app.crm.bean.service.ExternalAppMappingTransientHome;
import com.trilogy.app.crm.support.ExternalAppMappingSupportHelper;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This is a support class for testing.
 *
 * @author victor.stratan@redknee.com
 */
public class TestSupport
{
    public static void setupTransientExternalAppMapping(final Context ctx)
    {
        final Home home = new ExternalAppMappingTransientHome(ctx);
        ctx.put(ExternalAppMappingHome.class, home);

        ExternalAppMappingSupportHelper.get(ctx).addExternalAppMappingBeans(ctx, home);
    }

    public static void setupTransientSubscriptionType(final Context ctx)
    {
        final Home home = new AdapterHome(
                ctx, 
                new SubscriptionTypeTransientHome(ctx), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.account.SubscriptionType, com.redknee.app.crm.bean.core.SubscriptionType>(
                        com.redknee.app.crm.bean.account.SubscriptionType.class, 
                        com.redknee.app.crm.bean.core.SubscriptionType.class));
        ctx.put(SubscriptionTypeHome.class, home);

        addSubscriptionTypeBeans(ctx, home);
    }

    static void addSubscriptionTypeBeans(final Context ctx, final Home home)
    {
        createSubscriptionType(ctx, home, 10001L,              "airtime", SubscriptionTypeEnum.AIRTIME_INDEX);
        createSubscriptionType(ctx, home, 10002L,               "wallet", SubscriptionTypeEnum.MOBILE_WALLET_INDEX);
        createSubscriptionType(ctx, home, 10003L,           "net_wallet", SubscriptionTypeEnum.NETWORK_WALLET_INDEX);
        createSubscriptionType(ctx, home, 10004L, "prepaid_calling_card", SubscriptionTypeEnum.PREPAID_CALLING_CARD_INDEX);
        createSubscriptionType(ctx, home, 10005L,            "wire_line", SubscriptionTypeEnum.WIRE_LINE_INDEX);
        createSubscriptionType(ctx, home, 10006L,            "broadband", SubscriptionTypeEnum.BROADBAND_INDEX);
    }

    static void createSubscriptionType(final Context ctx, final Home home,
            final long identifier, final String name, final int type)
    {
        final SubscriptionType record = new SubscriptionType();
        record.setId(identifier);
        record.setName(name);
        record.setDescription(name);
        record.setType(type);
        try
        {
            home.create(ctx, record);
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, TestSupport.class, "Unable to configure bean " + record, e);
        }
    }

}
