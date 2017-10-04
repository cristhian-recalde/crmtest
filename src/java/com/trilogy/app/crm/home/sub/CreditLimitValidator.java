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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;

/**
 * Validates Credit Limit value.
 *
 * @author victor.stratan@redknee.com
 */
public final class CreditLimitValidator implements Validator
{
    private CreditLimitValidator()
    {
    }

    public static CreditLimitValidator instance()
    {
        if (instance == null)
        {
            instance = new CreditLimitValidator();
        }

        return instance;
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        final Subscriber newSub = (Subscriber) obj;

        if(newSub.isPrepaid() || newSub.isPooled(ctx))
        {
            // nothing to validate as credit limit for a pre-paid or pooled is never used
            return;
        }
        // ABM treats subscribers with Credit Limit equal to ZERO as PREPAID subscribers.
        // so, if the Subscriber is POSTPAID it should have at least some credit.
        if (newSub.getSubscriberType() == SubscriberTypeEnum.POSTPAID
                && newSub.getCreditLimit(ctx) <= 0)
        {
            final CompoundIllegalStateException el = new CompoundIllegalStateException();
            el.thrown(new IllegalPropertyArgumentException(
                    SubscriberXInfo.CREDIT_LIMIT,
                    "POSTPAID Subscriber cannot have ZERO credit limit!"));
            el.throwAll();
        }
        else if (newSub.getCreditLimit(ctx) == Subscriber.DEFAULT_CREDITLIMIT)
        {
            // this check is to prevent implementaion errors, when Subscriber beans are created automatically
            final CompoundIllegalStateException el = new CompoundIllegalStateException();
            el.thrown(new IllegalPropertyArgumentException(
                    SubscriberXInfo.CREDIT_LIMIT,
                    "Credit Limit not initialized!"));
            el.throwAll();
        }
    }

    private static CreditLimitValidator instance;
}
