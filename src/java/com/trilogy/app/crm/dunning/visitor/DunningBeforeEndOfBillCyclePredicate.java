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
package com.trilogy.app.crm.dunning.visitor;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class DunningBeforeEndOfBillCyclePredicate implements Predicate
{
    /**
     * {@inheritDoc}
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if (obj instanceof Account)
        {
            Account account = (Account) obj;
            Date endOfBillCycle = getEndOfBillCycle(ctx, account);
            try
            {
                AbstractDunningAccountProcessor processor = new ConcreteDunningAccountProcessor(endOfBillCycle);
                DunningReportRecord r = processor.process(ctx, account);

//                if (r != null
//                        && (r.getForecastedState().equals(AccountStateEnum.NON_PAYMENT_WARN)
//                                || r.getForecastedState().equals(AccountStateEnum.NON_PAYMENT_SUSPENDED)
//                                || r.getForecastedState().equals(AccountStateEnum.IN_ARREARS)))
//                {
//                    return true;
//                }
            }
            catch (Exception e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Error determining whether or not dunning will occur before end of account " + account.getBAN() + " bill cycle (" + endOfBillCycle + ")", e).log(ctx);
                }
            }
        }
        return false;
    }

    private Date getEndOfBillCycle(Context ctx, Account account)
    {
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.MONTHLY);
        if (handler != null)
        {
            CalendarSupport calendarSupport = CalendarSupportHelper.get(ctx);
            int billCycleDay;
            try
            {
                billCycleDay = account.getBillCycleDay(ctx);
            }
            catch (HomeException e)
            {
                throw new IllegalStateException(e);
            }
            Date runningDate = calendarSupport.getRunningDate(ctx);
            return handler.calculateCycleEndDate(ctx, runningDate, billCycleDay, account.getSpid());
        }

        throw new IllegalStateException("Unable to calculate end of bill cycle for account " + account.getBAN());
    }

    private static final class ConcreteDunningAccountProcessor extends AbstractDunningAccountProcessor
    {
        private ConcreteDunningAccountProcessor(Date runningDate)
        {
            super(runningDate);
        }


        @Override
        public String getProcessName()
        {
            return DunningBeforeEndOfBillCyclePredicate.class.getSimpleName();
        }
    }
}
