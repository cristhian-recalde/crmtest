/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.sequenceId;

import java.util.Date;

import com.trilogy.app.crm.bean.OnDemandSequence;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * @author ksivasubrmaniam
 * 
 *         The purpose of this class is to reset the startign sequence if it has reached
 *         the max.
 */
public class OnDemandSequenceResettingHome extends HomeProxy
{

    public OnDemandSequenceResettingHome(Home deletgate)
    {
        super(deletgate);
    }


    /**
     * {@inheritDoc}
     * 
     */
    public Object store(Context ctx, final Object obj) throws HomeException
    {
        final OnDemandSequence sequence = (OnDemandSequence) obj;
        if (sequence.isYearlyReset())
        {
            Date date = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(sequence.getLastResetDate());
            Date yearFromLastUpdate = CalendarSupportHelper.get(ctx).findDateMonthsAfter(12, date);
            Date curDate = new Date();
            if (curDate.after(yearFromLastUpdate))
            {
                sequence.setNextNum(sequence.getStartNum());
                sequence.setLastResetDate(new Date());
                OnDemandSequenceManager.clearCache(ctx, sequence.getIdentifier());
            }
        }
        if (sequence.getEndNum() == sequence.getNextNum())
        {
            sequence.setNextNum(sequence.getStartNum());
            sequence.setLastResetDate(new Date());
            OnDemandSequenceManager.clearCache(ctx, sequence.getIdentifier());
        }
        return super.store(ctx, sequence);
    }
}
