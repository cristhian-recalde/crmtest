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
package com.trilogy.app.crm.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;

import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AccountHistorySupport;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;


/**
 * 
 *
 * @author victor.stratan@redknee.com
 * @since 
 */
public class AccountHistory extends AbstractAccountHistory
{
    private static final long serialVersionUID = 1L;

    public List<AccountHistory> getSplitTransactions(Context ctx)
    {
        List<AccountHistory> result = new ArrayList<AccountHistory>();

        CoreTransactionSupport support = CoreTransactionSupportHelper.get(ctx);
        CalendarSupport calSupport = CalendarSupportHelper.get(ctx);

        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getTransDate());
        calSupport.clearTimeOfDay(cal);
        Date startDate = cal.getTime();

        calSupport.getDateWithLastSecondofDay(cal);
        Date endDate = cal.getTime();

        final And where = new And();
        where.add(new EQ(TransactionXInfo.RESPONSIBLE_BAN, this.getBAN()));
        where.add(new EQ(TransactionXInfo.ACCOUNT_RECEIPT_NUM, Long.parseLong(this.getId())));

        Collection<Transaction> transactions = support.getTransactions(ctx,
                where, startDate, endDate);

        for (Transaction transaction : transactions)
        {
            AccountHistory hist = AccountHistorySupport.convertTransactionToHistory(ctx, transaction,
                    AccountHistoryTypeEnum.TRANSACTION);
            result.add(hist);
        }

        return result;
    }
}
