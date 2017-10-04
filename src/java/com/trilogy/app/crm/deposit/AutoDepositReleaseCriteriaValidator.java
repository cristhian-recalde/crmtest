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

package com.trilogy.app.crm.deposit;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteriaXInfo;
import com.trilogy.app.crm.bean.ReleaseScheduleConfigurationEnum;

/**
 * Ensures the release schedule is within the range specified in HLD.
 *
 * @author cindy.wong@redknee.com
 */
public class AutoDepositReleaseCriteriaValidator implements Validator
{

    /**
     * Validates a criteria's release schedule. When <code>ApplyToBillCycle</code> is <code>TRUE</code> (i.e.,
     * release schedule represents day of month), release schedule must be in the range of 1-28 inclusive. When
     * <code>ApplyToBillCycle</code> is <code>FALSE</code> (i.e., release schedule represents the number of days
     * before bill cycle day), release schedule must be in the range of 0-28 inclusive.
     *
     * @param context
     *            The operating context.
     * @param object
     *            The criteria being validated.
     * @see com.redknee.framework.xhome.beans.Validator#validate
     */
    public final void validate(final Context context, final Object object)
    {
        final AutoDepositReleaseCriteria criteria = (AutoDepositReleaseCriteria) object;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        if (criteria.getReleaseScheduleConfiguration().equals(ReleaseScheduleConfigurationEnum.DAY_OF_MONTH)
            && criteria.getReleaseSchedule() == 0)
        {
            el.thrown(new IllegalPropertyArgumentException(AutoDepositReleaseCriteriaXInfo.RELEASE_SCHEDULE,
                "Release Schedule must be in range 1-28 when representing Day of Month"));
        }
        el.throwAll();
    }

}
