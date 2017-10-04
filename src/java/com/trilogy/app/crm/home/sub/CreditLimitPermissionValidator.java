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

import java.security.Principal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CreditLimitAdjustment;
import com.trilogy.app.crm.bean.CreditLimitAdjustmentHome;
import com.trilogy.app.crm.bean.CreditLimitAdjustmentXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.support.UserGroupSupport;

/**
 * Prevents credit limit changes when the agent is over the limit.
 *
 * @author cindy.wong@redknee.com
 */
public class CreditLimitPermissionValidator implements Validator
{
    /**
     * Singleton instance of validator.
     */
    private static final CreditLimitPermissionValidator INSTANCE = new CreditLimitPermissionValidator();

    /**
     * Creates a new <code>CreditLimitPermissionValidator</code>.
     */
    protected CreditLimitPermissionValidator()
    {
        // empty constructor
    }

    /**
     * Returns an instance of this class.
     *
     * @return An instance of this class.
     */
    public static CreditLimitPermissionValidator getInstance()
    {
        return INSTANCE;
    }

    /**
     * If the current change involves a change of credit limit, determine if the current user is allowed to make such
     * change base on the criteria set at the group level.
     *
     * @param context
     *            The operating context.
     * @param object
     *            The subscriber to be validated.
     */
    public final void validate(final Context context, final Object object)
    {
        final Subscriber subscriber = (Subscriber) object;
        final CompoundIllegalStateException compoundException = new CompoundIllegalStateException();
        Subscriber oldSubscriber;
        try
        {
            oldSubscriber = (Subscriber) ((Home) context.get(SubscriberHome.class)).find(context, subscriber.getId());
        }
        catch (HomeException exception)
        {
            throw new IllegalStateException(exception);
        }

        // determine if the credit limit has changed
        if (UserGroupSupport.isCreditLimitCheckEnabled(context, oldSubscriber, subscriber))
        {
            // determine if credit limit change is allowed now
            try
            {
                final Date currentDate = new Date();
                final Date allowedDate = nextCreditLimitAdjustmentDate(context, currentDate);
                if (!currentDate.equals(allowedDate))
                {
                    compoundException.thrown(new IllegalPropertyArgumentException("CreditLimit",
                        "You are not allowed to make more credit limit adjustments until " + allowedDate.toString()));
                }
            }
            catch (HomeException exception)
            {
                compoundException.thrown(new IllegalStateException(
                    "Programming Error: Cannot determine user's eligibility", exception));
            }
        }
        compoundException.throwAll();
    }

    /**
     * Return the earliest date when the current user is allowed to make another credit limit adjustment.
     *
     * @param context
     *            The operating context.
     * @param activeDate
     *            The active date to operate on.
     * @return The earliest date the current user is allowed to make Credit Limit Adjustment.
     * @throws HomeException
     *             Thrown if there are problems with home operations.
     */
    private Date nextCreditLimitAdjustmentDate(final Context context, final Date activeDate) throws HomeException
    {
        Date date = (Date) activeDate.clone();
        final User user = (User) context.get(Principal.class);
        final CRMGroup group = (CRMGroup) context.get(Group.class);

        if (UserGroupSupport.isCreditLimitPermissionEnabled(context, group))
        {
            // get the credit limit adjustments made by this user
            final Home home = (Home) context.get(CreditLimitAdjustmentHome.class);
            final And and = new And();
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, -group.getDurationOfCreditLimitAdjustments());
            final EQ eq = new EQ(CreditLimitAdjustmentXInfo.AGENT, user.getId());
            final GTE gte = new GTE(CreditLimitAdjustmentXInfo.ADJUSTMENT_DATE, calendar.getTime());
            and.add(eq);
            and.add(gte);

            // compare the numbers
            final Home subHome = home.where(context, and);
            final Collection adjustments = subHome.selectAll(context);
            if (adjustments.size() >= group.getNumberOfCreditLimitAdjustments())
            {
                // find the earliest day
                Date earliestDate = new Date();
                for (Object object : adjustments)
                {
                    final CreditLimitAdjustment adjustment = (CreditLimitAdjustment) object;
                    if (adjustment.getAdjustmentDate().before(earliestDate))
                    {
                        earliestDate = adjustment.getAdjustmentDate();
                    }
                }
                calendar.setTime(earliestDate);
                calendar.add(Calendar.DAY_OF_MONTH, group.getDurationOfCreditLimitAdjustments());
                date = calendar.getTime();
            }
        }
        return date;
    }
}
