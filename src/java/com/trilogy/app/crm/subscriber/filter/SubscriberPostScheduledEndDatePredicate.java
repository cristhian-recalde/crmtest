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
package com.trilogy.app.crm.subscriber.filter;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * @author jchen
 *
 * Predicate to filter subscriber to be activated, 
 * TO refactor out state related predicate
 * 
 * @deprecated Use ELang!
 */
@Deprecated
public class SubscriberPostScheduledEndDatePredicate implements Predicate,XStatement
{
    public SubscriberPostScheduledEndDatePredicate(Date checkTime)
    {
        checkTime_ = checkTime;
    }

    public boolean f(Context _ctx, Object obj)
    {
        Subscriber sub = (Subscriber) obj;
        if (getScheduleDeactivationDate(sub).getTime() >  checkTime_.getTime()
                && !EnumStateSupportHelper.get(_ctx).isOneOfStates(sub, VALID_STATES))
        {
            return true;
        }

        return false;
    }


    /**
     * @param sub
     * @return
     */
    private Date getScheduleDeactivationDate(Subscriber sub)
    {
        return sub.getEndDate();
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.filter.SQLClause#getSQLClause()
     */
    public String createStatement(Context ctx)
    {
        // return "scheduleDeactivationDate > "
        return "endDate > " + checkTime_.getTime() + " AND state NOT in " + getValidateStates();
    }


    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }

    private Date checkTime_;

    String getValidateStates()
    {
        StringBuilder sb = new StringBuilder(" (");
        for (SubscriberStateEnum state : VALID_STATES)
        {
            sb.append(state.getIndex());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(")");
        return sb.toString();
    }

    private static final Collection<SubscriberStateEnum> VALID_STATES = 
        Collections.unmodifiableCollection(Arrays.asList(
                SubscriberStateEnum.PENDING, 
                SubscriberStateEnum.INACTIVE,
                SubscriberStateEnum.MOVED));
}
