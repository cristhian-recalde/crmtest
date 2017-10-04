/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.subscriber.elang;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.clean.CronConstants;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * This XStatement can be used to get ALL expired (or pre-expired) subscribers from the database.
 * 
 * OID 36176 Note: 
 *    XStatement can't check the SPID.PrepaidPooledExpiry and SPID.PrepaidPooledNonExpiry flags.
 *    XStatement can't check the pooled state of the subscriber since that data is dependent on account information.
 * 
 * Therefore, the XStatement should be used as follows:
 *    Home whereHome = subscriberHome.where(ctx, new SubscriberExpiredXStatement(adjExpiryDays));
 *    whereHome.select(ctx, new SubscriberExpiredPredicate(adjExpiryDays));  
 * 
 * Although this is a 2 step approach, it is more efficient than using the predicate alone.
 * 
 * @author Aaron Gourley
 * @since 7.5
 *
 */
public class SubscriberExpiredXStatement implements XStatement
{
    /**
     * Creates a new predicate.
     */ 
    public SubscriberExpiredXStatement()
    {
        this(0);
    }
    
    public SubscriberExpiredXStatement(int adjExpiryDays)
    {
        adjExpiryDays_ = adjExpiryDays;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.xdb.XStatement#createStatement(com.redknee.framework.xhome.context.Context)
     */
    public String createStatement(Context ctx)
    {
        if( xs_ == null )
        {
            xs_ = createXStatement(ctx);
        }
        return xs_.createStatement(ctx);
    }


    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.xdb.XStatement#set(com.redknee.framework.xhome.context.Context, com.redknee.framework.xhome.xdb.XPreparedStatement)
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
        xs_.set(ctx, ps);
    }
    
    private XStatement createXStatement(Context ctx)
    {
        Date currentDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        final Date adjustedTodayDate =
            CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(getAdjustedDate(currentDate));
                
        return new And()
        .add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID))
        .add(new LTE(SubscriberXInfo.EXPIRY_DATE, adjustedTodayDate.getTime()))
        .add(new In(SubscriberXInfo.STATE, new HashSet())
                .add(SubscriberStateEnum.ACTIVE)
                .add(SubscriberStateEnum.SUSPENDED));
    }

    /**
     * returns a new adjusted date (Used for pre-expiry stuff)
     * @param expiryDate
     * @return
     */
    public Date getAdjustedDate(Date expiryDate)
    {
        Date dat = expiryDate;
        if (adjExpiryDays_ != 0)
        {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(expiryDate);
            calendar.add(Calendar.DATE, Math.abs(adjExpiryDays_));
            
            dat = calendar.getTime(); 
        }
        return dat;
    }

    transient XStatement xs_;
    private int adjExpiryDays_ = 0;
}
