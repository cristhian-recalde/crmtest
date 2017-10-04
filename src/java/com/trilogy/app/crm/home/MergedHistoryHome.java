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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.NullHome;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHistory;
import com.trilogy.app.crm.bean.AccountHistoryID;
import com.trilogy.app.crm.bean.AccountHistoryTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.support.AccountHistorySupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.web.control.WebControllerWebControl57;


/**
 * @author larry.xia@redknee.com
 */
public class MergedHistoryHome extends HomeProxy
{
    public MergedHistoryHome()
    {
        super(NullHome.instance());
    }


    @Override
    public Object find(Context ctx, Object arg0) throws HomeException, HomeInternalException
    {
        AccountHistoryID key = (AccountHistoryID) arg0;

        if (key.getType().equals(AccountHistoryTypeEnum.TRANSACTION))
        {
            return AccountHistorySupport.convertTransactionToHistory(ctx,
                    CoreTransactionSupportHelper.get(ctx).getTransaction(ctx, key.getId()),
                    AccountHistoryTypeEnum.TRANSACTION);
        }

        if (key.getType().equals(AccountHistoryTypeEnum.ACCOUNT_TRANSACTION))
        {
            Context subCtx = ctx.createSubContext();
            subCtx.put(TransactionHome.class, ctx.get(Common.ACCOUNT_TRANSACTION_HOME));
            return AccountHistorySupport.convertTransactionToHistory(ctx,
                    CoreTransactionSupportHelper.get(ctx).getTransaction(subCtx, key.getId()),
                    AccountHistoryTypeEnum.ACCOUNT_TRANSACTION);
        }

        CalculationService service = (CalculationService) ctx.get(CalculationService.class);
        Invoice invoice = null;
        try
        {
            invoice = service.getInvoiceForAccount(ctx, key.getBAN(), key.getKeyDate());
        }
        catch (CalculationServiceException e)
        {
        }

        return AccountHistorySupport.convertInvoiceToHistory(ctx, invoice);
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#select(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Collection select(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        // this is how you get the predicate
        //Predicate oldPredicate = (Predicate) XBeans.getInstanceOf(ctx,obj,Predicate.class);
        Collection sorted = new ArrayList();

        Account parentAcct = (Account) ctx.get(WebControllerWebControl57.PARENT_CPROPERTY);
        //Home acctHistHome = AccountHistorySupport.mergeHomesByBan(ctx, oldPredicate.getSQLClause());
        if (parentAcct != null)
        {
            Home acctHistHome = AccountHistorySupport.mergeHomesByBan(ctx, parentAcct.getBAN(),
                    new Date(0), new Date());
            Collection acctHistCollection = acctHistHome.select(ctx, obj);

            // Sort the list by date descending
            ArrayList acctHistList = new ArrayList(acctHistCollection);
            Collections.sort(acctHistList, accountHistoryDateComparator_);
            sorted = acctHistList;
        }

        return sorted;
    }

    /**
     * A comparator that can be used to sort a collection of invoices from
     * latest invoice date to earliest invoice date.
     */
    private static final Comparator<AccountHistory> accountHistoryDateComparator_ = new Comparator<AccountHistory>()
    {
        public int compare(final AccountHistory acctHist1, final AccountHistory acctHist2)
        {
            final Date acctHistDate1 = acctHist1.getKeyDate();
            final Date acctHistDate2 = acctHist2.getKeyDate();

            return (acctHistDate2.compareTo(acctHistDate1));
        }
    };
}
