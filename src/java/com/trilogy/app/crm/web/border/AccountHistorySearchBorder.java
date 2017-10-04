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
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.support.AccountHistorySupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import java.util.Date;


/**
 * This border decorates Account History (merges Transaction and Invoice)
 * and filters according to the BAN and a date range.
 * 
 * This is old code and there exists another way to achieve this same
 * effect using a MergeHome.  The idea is to add a merge home into the
 * StorageInstall, like so:
 * 
 *   Home[] cluster = {
 * 		new AdapterHome((Home) ctx.get(InvoiceHome.class), new InvoiceToHistoryAdapter())
 *   };
 * 
 *   ctx.put(
 *   AccountHistoryHome.class, 
 *		new MergeHome(
 *			new AdapterHome((Home) ctx.get(TransactionHome.class), new TransactionToHistoryAdapter()),
 *			cluster,
 *			null ))
 * 
 * Then to create TransactionToHistoryAdapter and InvoiceToHistoryAdapter 
 * classes (which implement Adapter) used to convert a Transaction/Invoice 
 * into an Account History.
 * 
 * Then to write this Search Border to use SelectSearchAgent's for BAN, StartDate 
 * and EndDate. 
 * 
 * Issues occurred when I tried to implement the above solution because Transaction
 * and Invoice Date searches are difficult.  The fields are named differently in the DB. 
 * Ideally, we could search using the KeyDate in Account History, but SelectSearchAgents
 * are done before the Merge.
 * 
 * -Angie Li
 *
 * @author Larry Xia
 */
public class AccountHistorySearchBorder extends SearchBorder
{
    public AccountHistorySearchBorder(final Context context)
    {
        super(context, AccountHistory.class, new AccountHistorySearchWebControl());

        // BAN
        addAgent(new ContextAgentProxy()
        {
            public void execute(final Context ctx) throws AgentException
            {
                Account account = (Account) ctx.get(Account.class);

                final AccountHistorySearch criteria = (AccountHistorySearch) getCriteria(ctx);

                Home txnHome = (Home) ctx.get(TransactionHome.class);
                ctx.put(TransactionHome.class, txnHome.where(ctx, new Limit(criteria.getLimit())));

                if (!account.getBAN().equals(""))
                {
                    Date startDate = criteria.getStartDate();
                    Date endDate = criteria.getEndDate();

                    if (startDate == null)
                    {
                        startDate = new Date(0);
                    }
                    else
                    {
                        startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(startDate);
                    }

                    if (endDate == null)
                    {
                        endDate = new Date();
                    }
                    else
                    {
                        endDate = CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(endDate);
                    }

                    Home historyHome = AccountHistorySupport.mergeHomesByBan(ctx, account.getBAN(), startDate, endDate);
                    ctx.put(AccountHistoryHome.class, new SortingHome(historyHome).where(ctx,
                            new Limit(criteria.getLimit())));
                }

                delegate(ctx);
            }
        });
    }
}
