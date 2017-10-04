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
package com.trilogy.app.crm.filter;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.WeekDayEnum;

/**
 * Helper class.
 */
public class ServiceProviderHomeHelper
{
    public static CRMSpid selectByImsiPrefix(final Context ctx, final String imsiPrefix)
        throws HomeException
    {
        final Home spHome = (Home) ctx.get(CRMSpidHome.class);

        final EQ condition = new EQ(CRMSpidXInfo.IMSI_PREFIX, imsiPrefix);
        final CRMSpid ret = (CRMSpid) spHome.find(ctx, condition);

        return ret;
    }

    public static CRMSpid selectById(Context ctx, final int spID)
            throws HomeException, HomeException
    {
        Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        if (spidHome == null)
        {
            throw new HomeException("Failed to locate CRMSpidHome in the context.");
        }

        CRMSpid ret = (CRMSpid) spidHome.find(ctx, Integer.valueOf(spID));

        return ret;
    }

    public static Collection selectByRecurDayOfWeek(Context ctx, final WeekDayEnum nameOfDay)
            throws HomeException
    {

        Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        if (spidHome == null)
        {
            throw new HomeException("Failed to locate CRMSpidHome in the context.");
        }

        EQ condition = new EQ(CRMSpidXInfo.WEEKLY_RECUR_CHARGING_DAY, nameOfDay);
        Collection spidColl = spidHome.select(ctx, condition);

        return spidColl;
    }
}
