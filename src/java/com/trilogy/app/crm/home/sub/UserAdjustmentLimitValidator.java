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

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;


/**
 * Validates the credit limit of the subscriber against the adjustment limit of the group.
 *
 * @author cindy.wong@redknee.com
 * @since 26-Sep-07
 */
public class UserAdjustmentLimitValidator implements Validator
{

    /**
     * Do not verify credit limit against the user's adjustment limit if this context key
     * is set to true.
     */
    public static final String SKIP_USER_ADJUSTMENT_LIMIT_VALIDATION = "SkipUserAdjustmentLimitValidation";


    /**
     * Create a new instance of <code>UserAdjustmentLimitValidator</code>.
     */
    protected UserAdjustmentLimitValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>UserAdjustmentLimitValidator</code>.
     *
     * @return An instance of <code>UserAdjustmentLimitValidator</code>.
     */
    public static UserAdjustmentLimitValidator instance()
    {
        if (instance == null)
        {
            instance = new UserAdjustmentLimitValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final Subscriber subscriber = (Subscriber) object;
        final Subscriber oldSubscriber = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);
        final CompoundIllegalStateException el = new CompoundIllegalStateException();

        /*
         * [Cindy] 2007-11-27 TT7102900043: Skip credit limit check when the change is
         * caused by deposit made.
         */
        if (!context.getBoolean(SKIP_USER_ADJUSTMENT_LIMIT_VALIDATION, false) &&
                !EnumStateSupportHelper.get(context).isEnteringState(oldSubscriber, subscriber, SubscriberStateEnum.PROMISE_TO_PAY))
        {

            /*
             * If the subscriber's credit limit is not set, Subscriber.getCreditLimit(context)
             * returns the price plan's credit limit.
             */
            if (oldSubscriber == null || oldSubscriber.getCreditLimit(context) != subscriber.getCreditLimit(context))
            {
                try
                {
                    verifyCreditLimit(context, SubscriberXInfo.CREDIT_LIMIT, subscriber.getCreditLimit(context));
                }
                catch (final IllegalPropertyArgumentException exception)
                {
                    el.thrown(exception);
                }
            }
        }
        el.throwAll();
    }


    /**
     * Verifies the credit limit provided is below the user agent's group limit.
     *
     * @param context
     *            The operating context.
     * @param property
     *            The property this credit limit is set from.
     * @param creditLimit
     *            The credit limit being set to.
     */
    protected void verifyCreditLimit(final Context context, final PropertyInfo property, final long creditLimit)
    {
        final CRMGroup group = (CRMGroup) context.get(Group.class);

        /*
         * Don't validate when group is not in context -- credit limit modified by cron
         * task should not be validated.
         */
        if (group != null)
        {
            final long agentLimit = group.getCreditLimitAdjustmentLimit();
            if (creditLimit > agentLimit)
            {
                final Currency currency = (Currency) context.get(Currency.class);
                final StringBuilder sb = new StringBuilder();
                sb.append("New credit limit ");
                if (currency != null)
                {
                    sb.append(currency.formatValue(creditLimit));
                }
                else
                {
                    sb.append(creditLimit);
                }
                sb.append(" exceeds Credit Limit Adjustment Limit ");
                if (currency != null)
                {
                    sb.append(currency.formatValue(agentLimit));
                }
                else
                {
                    sb.append(agentLimit);
                }
                sb.append(" set in User Group");
                throw new IllegalPropertyArgumentException(property, sb.toString());
            }
        }
    }

    /**
     * Singleton instance.
     */
    private static UserAdjustmentLimitValidator instance;
}
