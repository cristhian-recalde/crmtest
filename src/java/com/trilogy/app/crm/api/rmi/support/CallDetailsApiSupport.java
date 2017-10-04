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
package com.trilogy.app.crm.api.rmi.support;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.elang.PagingXStatement;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * Provides utility functions for use with CallDetail
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class CallDetailsApiSupport
{    
    public static Collection<CallDetail> getCallDetailsUsingGivenParameters(final Context ctx, 
            final Subscriber subscriber, final Calendar start, final Calendar end, Long pageKey,
            final int limit, final Boolean isAscending) throws Exception
    {
        boolean ascending = RmiApiSupport.isSortAscending(isAscending);

        final And condition = new And();
        condition.add(new EQ(CallDetailXInfo.SUBSCRIBER_ID, subscriber.getId()));
        if (start != null)
        {
            condition.add(new GTE(CallDetailXInfo.TRAN_DATE, CalendarSupportHelper.get(ctx).calendarToDate(start)));
            condition.add(new GTE(CallDetailXInfo.POSTED_DATE, CalendarSupportHelper.get(ctx).calendarToDate(start)));
        }
        if (end != null)
        {
            condition.add(new LTE(CallDetailXInfo.TRAN_DATE, CalendarSupportHelper.get(ctx).calendarToDate(end)));
        }
        if (pageKey != null)
        {
            condition.add(new PagingXStatement(CallDetailXInfo.ID, pageKey, ascending));
        }
        
        Collection<CallDetail> collection = HomeSupportHelper.get(ctx).getBeans(
                ctx,
                CallDetail.class,
                condition,
                limit,
                ascending, CallDetailXInfo.ID, CallDetailXInfo.POSTED_DATE);

        return collection;
    }
    
    public static Collection<CallDetail> getCallDetailsUsingGivenParametersOrderByDateTime(final Context ctx, 
            final Subscriber subscriber, final Calendar start, final Calendar end, Long pageKey,
            final int limit, final Boolean isAscending) throws Exception
    {
        boolean ascending = RmiApiSupport.isSortAscending(isAscending);

        final And condition = new And();
        condition.add(new EQ(CallDetailXInfo.SUBSCRIBER_ID, subscriber.getId()));
        if (start != null)
        {
            condition.add(new GTE(CallDetailXInfo.TRAN_DATE, CalendarSupportHelper.get(ctx).calendarToDate(start)));
            condition.add(new GTE(CallDetailXInfo.POSTED_DATE, CalendarSupportHelper.get(ctx).calendarToDate(start)));
        }
        if (end != null)
        {
            condition.add(new LTE(CallDetailXInfo.TRAN_DATE, CalendarSupportHelper.get(ctx).calendarToDate(end)));
        }
        if (pageKey != null)
        {
            condition.add(new PagingXStatement(CallDetailXInfo.ID, pageKey, ascending));
        }
        
        Collection<CallDetail> collection = HomeSupportHelper.get(ctx).getBeans(
                ctx,
                CallDetail.class,
                condition,
                limit,
                ascending, CallDetailXInfo.TRAN_DATE);

        return collection;
    }
    
    public static Collection<CallDetail> filterCallDetailsByPageKey(final Context ctx,
            Collection<CallDetail> collection, Long pageKey, final Boolean isAscending, int limit) throws Exception
    {
        if (pageKey != null)
        {
            Collection<CallDetail> filteredCollection = new ArrayList<CallDetail>();
            boolean asc = isAscending == null ? false : isAscending;
            for (com.redknee.app.crm.bean.calldetail.CallDetail obj : collection)
            {
                if (limit <= 0)
                {
                    break;
                }
                if (asc)
                {
                    if (obj.getId() > pageKey)
                    {
                        filteredCollection.add(obj);
                        limit--;
                    }
                }
                else
                {
                    if (obj.getId() < pageKey)
                    {
                        filteredCollection.add(obj);
                        limit--;
                    }
                }
            }
            return filteredCollection;
        }
        else
        {
            Collection<CallDetail> filteredCollection = new ArrayList<CallDetail>();
            for (com.redknee.app.crm.bean.calldetail.CallDetail obj : collection)
            {
                if (limit <= 0)
                {
                    break;
                }
                filteredCollection.add(obj);
                limit--;
            }
            return filteredCollection;
        }
    }
}