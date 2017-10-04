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
package com.trilogy.app.crm.home.validator;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;



import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.BillCycleHistoryXInfo;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class BillCycleHistoryEventDateValidator implements Validator
{

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        HomeOperationEnum op = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);
        if (obj instanceof BillCycleHistory)
        {
            BillCycleHistory hist = (BillCycleHistory) obj;
            if (HomeOperationEnum.CREATE.equals(op)
                    && BillCycleChangeStatusEnum.PENDING.equals(hist.getStatus()))
            {
                try
                {
                    BillCycle newBillCycle = BillCycleSupport.getBillCycle(ctx, hist.getNewBillCycleID());
                    Account account = AccountSupport.getAccount(ctx, hist.getBAN());
                    if (account.getBillCycleDay(ctx) != newBillCycle.getDayOfMonth())
                    {
                        validateMinimumWaitPeriod(ctx, cise, hist, account);
                        validateInvoiceGenerationState(ctx, cise, account);
                    }
                }
                catch (HomeException e)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            BillCycleHistoryXInfo.BAN, 
                            "Unable to retrieve account " + hist.getBAN()));
                }
            }
        }
        
        cise.throwAll();
    }

    protected void validateMinimumWaitPeriod(Context ctx, CompoundIllegalStateException cise, BillCycleHistory history, Account account)
    {
        Date nextAllowedRequestDate = BillCycleHistorySupport.getNextAllowedRequestDate(ctx, account);

        Date startOfNextBillCycle = history.getBillCycleChangeDate();
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.MONTHLY);
        if (handler != null)
        {
            CalendarSupport calendarSupport = CalendarSupportHelper.get(ctx);
            Date nextMonthDate = calendarSupport.findDateMonthsAfter(1, startOfNextBillCycle);
            startOfNextBillCycle = handler.calculateCycleStartDate(ctx, nextMonthDate, history.getOldBillCycleDay(), account.getSpid());
        }
        
        if (nextAllowedRequestDate.after(startOfNextBillCycle))
        {            
            SimpleDateFormat dateFormatter = DateWebControl.instance().getFormatter(ctx);
            String formattedDate = null;
            if (dateFormatter != null)
            {
                formattedDate = dateFormatter.format(nextAllowedRequestDate.getTime());
            }
            else
            {
                formattedDate = String.valueOf(nextAllowedRequestDate);
            }
            cise.thrown(new IllegalPropertyArgumentException(
                    BillCycleHistoryXInfo.BILL_CYCLE_CHANGE_DATE, 
                    "Account " + account.getBAN()
                    + " is not eligable for bill cycle change until " + formattedDate));
        }
    }

    protected void validateInvoiceGenerationState(Context ctx, CompoundIllegalStateException cise, Account account)
    {
        CalculationService calc = (CalculationService) ctx.get(CalculationService.class);
        String sessionKey = CalculationServiceSupport.createNewSession(ctx);
        try
        {
            if (!account.isPrepaid() && account.isResponsible())
            {
                Invoice invoice = calc.getMostRecentInvoice(ctx, account.getBAN());
                if (invoice == null)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            BillCycleHistoryXInfo.BAN, 
                            "No invoice has been generated for account " + account.getBAN() + ".  At least one invoice is required before account is eligible for bill cycle date change."));
                }
                else if (account.getBillCycleID() != invoice.getBillCycleID())
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            BillCycleHistoryXInfo.BAN, 
                            "No invoice has been generated since last bill cycle change for account " + account.getBAN() + ".  At least one invoice is required between bill cycle date changes."));
                }
                else
                {
                    try
                    {
                        And filter = new And();
                        filter.add(new EQ(AccountXInfo.RESPONSIBLE, true));
                        filter.add(new EQ(AccountXInfo.PARENT_BAN, account.getBAN()));
                        filter.add(new EQ(AccountXInfo.STATE, AccountStateEnum.ACTIVE));
                        filter.add(new NEQ(AccountXInfo.SYSTEM_TYPE, SubscriberTypeEnum.PREPAID));
                        
                        Collection responsibleSubAccounts = HomeSupportHelper.get(ctx).getBeans(ctx, Account.class, filter);
                        if (responsibleSubAccounts != null)
                        {
                            for (Object obj : responsibleSubAccounts)
                            {
                                if (obj instanceof Account)
                                {
                                    validateInvoiceGenerationState(ctx, cise, (Account) obj);
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                BillCycleHistoryXInfo.BAN, 
                                "Error retrieving responsible sub-accounts for account " + account.getBAN()));
                    }
                }
            }
        }
        catch (CalculationServiceException e)
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    BillCycleHistoryXInfo.BAN, 
                    "Error retrieving most recent invoice for account " + account.getBAN()));
        }
        finally
        {
            CalculationServiceSupport.endSession(ctx, sessionKey);
        }
    }

}
