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
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xhome.web.search.SearchBorder;

import com.trilogy.app.crm.bean.BalanceHistory;
import com.trilogy.app.crm.bean.BalanceHistoryID;
import com.trilogy.app.crm.bean.BalanceHistorySearch;
import com.trilogy.app.crm.bean.BalanceHistoryTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.BalanceHistorySupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;


/**
 * Home for the Merged Transaction and Call Details view at the subscriber level.
 * Only 'View'-ing actions will be supported for this (select and find).
 * 
 * @author victor.stratan@redknee.com
 */
public class MergedBalanceHistoryHome extends HomeProxy
{
    public MergedBalanceHistoryHome()
    {
        super(NullHome.instance());
    }


    @Override
    public Object find(final Context ctx, final Object obj) throws HomeException
    {
        final BalanceHistoryID key = (BalanceHistoryID) obj;

        if (key.getType().equals(BalanceHistoryTypeEnum.TRANSACTION))
        {
            return BalanceHistorySupport.convertTransactionToHistory(ctx,
                    CoreTransactionSupportHelper.get(ctx).getTransaction(ctx, key.getId()));
        }

        return BalanceHistorySupport.convertCallDetailToHistory(ctx,
                CallDetailSupportHelper.get(ctx).getCallDetail(ctx, key.getId()));
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#select(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Collection select(Context ctx, Object obj) throws HomeException
    {
        final Subscriber sub = (Subscriber) ctx.get(Subscriber.class);

        Date startDate = null;
        Date endDate = null;
        int limit = 100;

        final BalanceHistorySearch search = (BalanceHistorySearch) SearchBorder.getCriteria(ctx);
        if (search != null)
        {
            if (search.getStartDate() != null)
            {
                startDate = search.getStartDate();
            }
            else
            {
                startDate = new Date(0);
            }

            if (search.getEndDate() != null)
            {
                endDate = CalendarSupportHelper.get(ctx).getEndOfDay(search.getEndDate());
            }
            else
            {
                endDate = new Date();
            }
            limit = search.getLimit();
        }

        final Collection<BalanceHistory> sorted;
        if (sub != null)
        {
            sorted = BalanceHistorySupport.mergeSelectBySubscriberID(ctx, sub.getId(), startDate, endDate, limit);
        }
        else
        {
            sorted = new ArrayList<BalanceHistory>();
        }

        return sorted;
    }

    /**
     * A comparator that can be used to sort a collection of invoices from
     * latest transaction date to earliest transaction date. The Balance History
     * model has an attribute SORTDATE to facilitate this sorting. See notes in
     * BalanceHistorySupport for more details on how it is set.
     */
    private static final Comparator<BalanceHistory> balanceHistoryDateComparator_ = new Comparator<BalanceHistory>()
    {
        public int compare(final BalanceHistory balHist1, final BalanceHistory balHist2)
        {
            final Date balHistDate1 = balHist1.getSortDate();
            final Date balHistDate2 = balHist2.getSortDate();

            int compareResult = balHistDate1.compareTo(balHistDate2);
            if (compareResult == 0)
            {
                /* When dates equal, compare by Transaction/Call Detail IDs.  It is highly 
                 * unlikely that in a real world scenario, a call and a transaction will 
                 * happen in the exact same time. However, transactions processed in 
                 * succession might be time stamped the same. */
                compareResult = balHist1.getId().compareTo(balHist2.getId());
            }
            return compareResult;
        }
    };
}
