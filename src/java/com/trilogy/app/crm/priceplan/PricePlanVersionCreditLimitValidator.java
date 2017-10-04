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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;

/**
 * Validates credit limit value.
 *
 * @author victor.stratan@redknee.com
 */
public class PricePlanVersionCreditLimitValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        final PricePlanVersion ppv = (PricePlanVersion) obj;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();

        try
        {
            final Home home = (Home) ctx.get(PricePlanHome.class);
            final PricePlan pricePlan = (PricePlan) home.find(ctx, Long.valueOf(ppv.getId()));

            if (pricePlan == null)
            {
                el.thrown(new IllegalStateException("Cannot obtain Price Plan bean."));
            }
            else if (pricePlan.getPricePlanType() == SubscriberTypeEnum.POSTPAID
                    && ppv.getCreditLimit() <= 0)
            {
                // ABM treats subscribers with Credit Limit equal to ZERO as PREPAID subscribers.
                // so, if the Subscriber is POSTPAID it should have at least some credit.
                el.thrown(new IllegalPropertyArgumentException(
                        PricePlanVersionXInfo.CREDIT_LIMIT,
                        "POSTPAID Price Plans cannot have ZERO credit limit!"));
            }
        }
        catch (HomeException e)
        {
            final String msg = "Cannot access " + PricePlanHome.class.getName() + " home!";
            el.thrown(new IllegalStateException(msg));
            new MajorLogMsg(this, msg, e).log(ctx);
        }
        finally
        {
            el.throwAll();
        }
    }
}
