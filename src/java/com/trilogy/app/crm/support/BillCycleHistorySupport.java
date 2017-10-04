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
package com.trilogy.app.crm.support;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.BillCycleHistoryID;
import com.trilogy.app.crm.bean.BillCycleHistoryXInfo;
import com.trilogy.app.crm.bean.CRMSpid;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class BillCycleHistorySupport
{
    public static BillCycleHistory getLastEventInHierarchy(Context ctx, String ban)
    {
        return getLastEventInHierarchy(ctx, ban, null);
    }
    
    public static BillCycleHistory getLastEventInHierarchy(Context ctx, Account account)
    {
        return getLastEventInHierarchy(ctx, account, null);
    }
    
    public static BillCycleHistory getLastEventInHierarchy(Context ctx, String ban, BillCycleChangeStatusEnum status)
    {
        Account account;
        try
        {
            account = AccountSupport.getAccount(ctx, ban);
        }
        catch (HomeException e)
        {
            account = null;
        }
        return getLastEventInHierarchy(ctx, account, status);
    }
    
    public static BillCycleHistory getLastEventInHierarchy(Context ctx, Account account, BillCycleChangeStatusEnum status)
    {
        if (account == null)
        {
            return null;
        }
        
        BillCycleHistory result = null;
        
        try
        {
            Account rootAccount = account.getRootAccount(ctx);
            if (rootAccount != null)
            {
                result = getLastEvent(ctx, rootAccount.getBAN(), status);
                if (result != null)
                {
                    return result;
                }
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(BillCycleHistorySupport.class, "Error retrieving root account for " + account.getBAN(), e).log(ctx);
        }
        
        return null;
    }
    
    public static BillCycleHistory getLastEvent(Context ctx, String ban)
    {
        return getLastEvent(ctx, ban, null);
    }


    public static Date getLastEventDate(Context ctx, String ban)
    {
        return getLastEventDate(ctx, ban, null);
    }
    
	public static Date getLastEventDateBefore(Context ctx, String ban,
	    BillCycleChangeStatusEnum status, Date date)
	{
		HomeSupport homeSupport = HomeSupportHelper.get(ctx);
		try
		{
			And filter = new And();
			filter.add(new EQ(BillCycleHistoryXInfo.BAN, ban));
			if (status != null)
			{
				filter.add(new EQ(BillCycleHistoryXInfo.STATUS, status));
			}
			else
			{
				// By default, only return pending and complete events
				Or subFilter = new Or();
				subFilter.add(new EQ(BillCycleHistoryXInfo.STATUS,
				    BillCycleChangeStatusEnum.PENDING));
				subFilter.add(new EQ(BillCycleHistoryXInfo.STATUS,
				    BillCycleChangeStatusEnum.COMPLETE));
				filter.add(subFilter);
			}

			if (date != null)
			{
				filter.add(new LT(BillCycleHistoryXInfo.BILL_CYCLE_CHANGE_DATE,
				    date));
			}

			Object lastHistoryDate =
			    homeSupport.max(ctx,
			        BillCycleHistoryXInfo.BILL_CYCLE_CHANGE_DATE, filter);
			if (lastHistoryDate instanceof Number)
			{
				Date lastChangeDate =
				    new Date(((Number) lastHistoryDate).longValue());
				return lastChangeDate;
			}
		}
		catch (HomeException e)
		{
			new MinorLogMsg(BillCycleHistorySupport.class,
			    "Error looking up last bill cycle history date for criteria [BAN="
			        + ban + ",status=" + status + "]", e).log(ctx);
		}
		return null;
	}
    public static Date getLastEventDate(Context ctx, String ban, BillCycleChangeStatusEnum status)
    {
        HomeSupport homeSupport = HomeSupportHelper.get(ctx);
        try
        {
            And filter = new And();
            filter.add(new EQ(BillCycleHistoryXInfo.BAN, ban));
            if (status != null)
            {
                filter.add(new EQ(BillCycleHistoryXInfo.STATUS, status));
            }
            else
            {
                // By default, only return pending and complete events
                Or subFilter = new Or();
                subFilter.add(new EQ(BillCycleHistoryXInfo.STATUS, BillCycleChangeStatusEnum.PENDING));
                subFilter.add(new EQ(BillCycleHistoryXInfo.STATUS, BillCycleChangeStatusEnum.COMPLETE));
                filter.add(subFilter);
            }

            Object lastHistoryDate = homeSupport.max(ctx, BillCycleHistoryXInfo.BILL_CYCLE_CHANGE_DATE, filter);
            if (lastHistoryDate instanceof Number)
            {
                Date lastChangeDate = new Date(((Number)lastHistoryDate).longValue());
                return lastChangeDate;
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(BillCycleHistorySupport.class, "Error looking up last bill cycle history date for criteria [BAN=" + ban + ",status=" + status + "]", e).log(ctx);
        }
		return null;
	}

    
    public static BillCycleHistory getLastEventBefore(Context ctx, String ban,
	    BillCycleChangeStatusEnum status, Date date)
	{
		{
			HomeSupport homeSupport = HomeSupportHelper.get(ctx);
			try
			{
				Date lastChangeDate =
				    getLastEventDateBefore(ctx, ban, status, date);
				if (lastChangeDate != null)
				{
					return homeSupport.findBean(ctx, BillCycleHistory.class,
					    new BillCycleHistoryID(lastChangeDate, ban));
				}
			}
			catch (HomeException e)
			{
				new MinorLogMsg(BillCycleHistorySupport.class,
				    "Error looking up last bill cycle history event for criteria [BAN="
				        + ban + ",status=" + status + "]", e).log(ctx);
			}
			return null;
		}
	}
    public static BillCycleHistory getLastEvent(Context ctx, String ban, BillCycleChangeStatusEnum status)
    {
        HomeSupport homeSupport = HomeSupportHelper.get(ctx);
        try
        {
            Date lastChangeDate = getLastEventDate(ctx, ban, status);
            if (lastChangeDate != null)
            {
                return homeSupport.findBean(ctx, BillCycleHistory.class, new BillCycleHistoryID(lastChangeDate, ban));
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(BillCycleHistorySupport.class, "Error looking up last bill cycle history event for criteria [BAN=" + ban + ",status=" + status + "]", e).log(ctx);
        }
        return null;
    }

	public static Date getNextAllowedRequestDate(Context ctx, Account account)
	{
		Date lastDate =
		    getLastEventDate(ctx, account.getBAN(),
		        BillCycleChangeStatusEnum.COMPLETE);

		if (lastDate == null)
		{
			return CalendarSupportHelper.get(ctx).getRunningDate(ctx);
		}

		CRMSpid spid = null;
		try
		{
			spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
		}
		catch (HomeException e)
		{
			new MinorLogMsg(BillCycleHistorySupport.class,
			    "Error looking up the SPID for account " + account.getBAN(), e)
			    .log(ctx);
		}
		if (spid == null)
		{
			return null;
		}
		return CalendarSupportHelper.get(ctx).findDateDaysAfter(
		    spid.getMinBillCycleChangeWaitPeriod(), lastDate);
	}
}
