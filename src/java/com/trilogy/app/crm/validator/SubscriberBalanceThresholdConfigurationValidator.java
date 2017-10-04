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

package com.trilogy.app.crm.validator;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.SubscriberSupport;

/***
 * @author chandrachud.ingale
 * @since 9.9
 */
public class SubscriberBalanceThresholdConfigurationValidator implements Validator
{

    private static Validator instance;


    private SubscriberBalanceThresholdConfigurationValidator()
    {}


    public static Validator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberBalanceThresholdConfigurationValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object object)
    {
        final Subscriber newSub = (Subscriber) object;

        if (newSub.getAtuBalanceThreshold() != newSub.DEFAULT_ATUBALANCETHRESHOLD
                && !SubscriberSupport.isBalanceThresholdAllowed(ctx, newSub))
        {
            throw new IllegalPropertyArgumentException(SubscriberXInfo.ATU_BALANCE_THRESHOLD,
                    "Balance threshold configuration not allowed by Service Provider");
        }

    }

}
