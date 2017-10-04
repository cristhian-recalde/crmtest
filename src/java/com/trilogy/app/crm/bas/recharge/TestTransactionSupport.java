/*
 * TestTransactionSupport.java
 *
 * Author : victor.stratan@redknee.com
 * Date: Aug 24, 2006
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bas.recharge;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleTransientHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidTransientHome;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionTransientHome;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryTransientHome;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.CRMServicePeriodSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePeriodSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class TestTransactionSupport extends ContextAwareTestCase
{
    private static final int ADJUSTMENT_ID = 12345;

    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestTransactionSupport(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestTransactionSupport.class);
        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();

        ServicePeriodSupportHelper.register(getContext(), ServicePeriodSupport.class, CRMServicePeriodSupport.instance());

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, 2005);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 26);
        querydate = calendar.getTime();

        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(getContext()).getHandler(ChargingCycleEnum.MONTHLY);
        billdateNormal =  handler.calculateCycleStartDate(getContext(), querydate, 15, 11);


        calendar.setTime(billdateNormal);
        calendar.roll(Calendar.DATE, -1);
        billdatePrebilled = calendar.getTime();

        sub = new Subscriber();

        sub.setBAN("105");
        sub.setId("105-3");
        sub.setMSISDN("4160001115");
        sub.setSpid(11);
        sub.setSubscriberType(SubscriberTypeEnum.POSTPAID);

        BillCycle bc = new BillCycle();
        bc.setBillCycleID(12);
        bc.setDayOfMonth(15);

        Account acc = new Account();
        acc.setBAN("105");
        acc.setBillCycleID(12);
        acc.setSpid(11);

        spid = new CRMSpid();
        spid.setId(11);

        Home accountsHome = new AccountTransientHome(getContext());
        getContext().put(AccountHome.class, accountsHome);

        spidsHome = new CRMSpidTransientHome(getContext());
        getContext().put(CRMSpidHome.class, spidsHome);

        BillCycleHome billCyclesHome = new BillCycleTransientHome(getContext());
        getContext().put(BillCycleHome.class, billCyclesHome);

        transactionsHome = new TransactionTransientHome(getContext());
        getContext().put(TransactionHome.class, transactionsHome);

        historyHome = new SubscriberSubscriptionHistoryTransientHome(getContext());
        getContext().put(SubscriberSubscriptionHistoryHome.class, historyHome);
         try
        {
            accountsHome.create(getContext(), acc);
            billCyclesHome.create(getContext(), bc);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
    }

    private ServiceFee2 createBillingTransaction(long serviceID, boolean isSubscriptionCharge, Date receiveDate, int adjustment, long fee)
    {
        Transaction trans = new Transaction();
        trans.setBAN("105");
        trans.setSpid(11);
        trans.setMSISDN("4160001115");
        trans.setAgent("no_agent");
        trans.setAmount(fee);
        trans.setBalance(202000);
        trans.setSubscriberID("105-3");
        trans.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        trans.setGLCode("GL1");
        trans.setSubscriptionCharge(isSubscriptionCharge);
        trans.setTransDate(receiveDate);
        trans.setReceiveDate(receiveDate);
        try
        {
            trans.setReceiptNum(23000 + CREATED_TRANSACTIONS++);
            trans.setAdjustmentType(adjustment);

            transactionsHome.create(getContext(), trans);
            ServiceFee2 service = new ServiceFee2();
            service.setServiceId(serviceID);
            service.setFee(fee);
            Subscriber sub = new Subscriber();
            sub.setId("105-3");

            SubscriberSubscriptionHistorySupport.addChargingHistory(getContext(), service, sub, fee>=0?HistoryEventTypeEnum.CHARGE:HistoryEventTypeEnum.REFUND,
                    ChargedItemTypeEnum.SERVICE, fee, fee, trans, receiveDate);
            
            return service;

        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
        
        return null;
    }

    private void createSpid(boolean billInAdvance)
    {
        spid.setPrebilledRecurringChargeEnabled(billInAdvance);
        try
        {
            spidsHome.create(getContext(), spid);
        }
        catch (HomeException e)
        {
            fail(e.getMessage());
        }
    }

    public void testBillingCycleChargedAndNotRefunded_noTransactions() throws Exception
    {
        createSpid(true);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, new ServiceFee2(), ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertFalse("BC cannot be charged if there are no transactions.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_noTransactionsInBC_normal() throws Exception
    {
        createSpid(false);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, -5);
        createBillingTransaction(1, true, calendar.getTime(), ADJUSTMENT_ID, 1000);
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(getContext()).getHandler(ChargingCycleEnum.MONTHLY);

        Date d = CalendarSupportHelper.get(getContext()).findDateMonthsAfter(-1, handler.calculateCycleStartDate(getContext(), querydate, 15, spid.getId()));
        createBillingTransaction(2, false, d, ADJUSTMENT_ID, 1000);

        d = CalendarSupportHelper.get(getContext()).findDateMonthsAfter(1, handler.calculateCycleStartDate(getContext(), querydate, 15, spid.getId()));
        createBillingTransaction(3, false, d, ADJUSTMENT_ID, 1000);

        calendar.setTime(d);
        calendar.add(Calendar.DATE, 5);
        createBillingTransaction(4, true, calendar.getTime(), ADJUSTMENT_ID, 1000);

        ServiceFee2 service = new ServiceFee2();
        service.setServiceId(5);
        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertFalse("BC cannot be charged if there are no transactions in BC.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_subscribtionFromPreviousBConBCday_preBilled() throws Exception
    {
        createSpid(true);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdatePrebilled);
        calendar.add(Calendar.HOUR_OF_DAY, 8);
        ServiceFee2 service = createBillingTransaction(6, true, calendar.getTime(), ADJUSTMENT_ID, 1000);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertFalse("Transaction from next BC should not be taken into account.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_transactionsInBCfromNextBC_preBilled() throws Exception
    {
        createSpid(true);

        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(getContext()).getHandler(ChargingCycleEnum.MONTHLY);
        Date d = CalendarSupportHelper.get(getContext()).findDateMonthsAfter(1, handler.calculateCycleStartDate(getContext(), querydate, 15, spid.getId()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DATE, -1);
        ServiceFee2 service = createBillingTransaction(7, false, calendar.getTime(), ADJUSTMENT_ID, 1000);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertFalse("Transaction from next BC should not be taken into account.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_billTransactionPresent_normal() throws Exception
    {
        createSpid(false);

        ServiceFee2 service = createBillingTransaction(8, false, billdateNormal, ADJUSTMENT_ID, 1000);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC should be considered as charged.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_billTransactionPresent_preBilled() throws Exception
    {
        createSpid(true);

        ServiceFee2 service = createBillingTransaction(9, false, billdatePrebilled, ADJUSTMENT_ID, 1000);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC should be considered as charged.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_subscribtionTransactionPresent_normal() throws Exception
    {
        createSpid(false);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 5);
        ServiceFee2 service = createBillingTransaction(10, true, calendar.getTime(), ADJUSTMENT_ID, 500);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC should be considered as charged.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_subscribtionTransactionPresent_preBilled() throws Exception
    {
        createSpid(true);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 5);
        ServiceFee2 service = createBillingTransaction(11, true, calendar.getTime(), ADJUSTMENT_ID, 500);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC should be considered as charged.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_subscribtionOnBillCycleDay_normal() throws Exception
    {
        createSpid(false);

        ServiceFee2 service = createBillingTransaction(12, true, billdateNormal, ADJUSTMENT_ID, 500);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC should be considered as charged.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_subscribtionOnBillCycleDay_preBilled() throws Exception
    {
        createSpid(true);

        ServiceFee2 service = createBillingTransaction(13, true, billdateNormal, ADJUSTMENT_ID, 500);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC should be considered as charged.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_billTransactionPresentButRefunded_normal() throws Exception
    {
        createSpid(false);

        ServiceFee2 service = createBillingTransaction(14, false, billdateNormal, ADJUSTMENT_ID, 1000);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 5);
        createBillingTransaction(14, true, calendar.getTime(), ADJUSTMENT_ID, -500);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertFalse("BC cannot be charged if the charge was refunded.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_billTransactionPresentButRefunded_preBilled() throws Exception
    {
        createSpid(true);

        ServiceFee2 service = createBillingTransaction(15, false, billdatePrebilled, ADJUSTMENT_ID, 1000);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdatePrebilled);
        calendar.add(Calendar.DATE, 5);
        createBillingTransaction(15, true, calendar.getTime(), ADJUSTMENT_ID, -500);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertFalse("BC cannot be charged if the charge was refunded.", rez);
    }

    /**
     * This is a questionable test case. excluding from the standard set.
     *
     * @throws Exception
     */
    public void _testBillingCycleChargedAndNotRefunded_billTransactionPresentWithRefundOnPreviousBillCycle_preBilled()
        throws Exception
    {
        createSpid(true);

        ServiceFee2 service = createBillingTransaction(16, false, billdatePrebilled, ADJUSTMENT_ID, 1000);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdatePrebilled);
        calendar.add(Calendar.HOUR_OF_DAY, 8);
        ServiceFee2 service2 = createBillingTransaction(16, true, calendar.getTime(), ADJUSTMENT_ID, -500);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub,service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC is charged if the refund belongs to prefious BC.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_proratedTransactionPresentButRefunded_normal() throws Exception
    {
        createSpid(false);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 3);
        ServiceFee2 service = createBillingTransaction(17, true, calendar.getTime(), ADJUSTMENT_ID, 800);

        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 7);
        ServiceFee2 service2 = createBillingTransaction(17, true, calendar.getTime(), ADJUSTMENT_ID, -500);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertFalse("BC cannot be charged if the charge was refunded.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_proratedTransactionPresentButRefunded_preBilled() throws Exception
    {
        createSpid(true);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 3);
        ServiceFee2 service = createBillingTransaction(18, true, calendar.getTime(), ADJUSTMENT_ID, 800);

        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 7);
        ServiceFee2 service2 = createBillingTransaction(18, true, calendar.getTime(), ADJUSTMENT_ID, -500);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertFalse("BC cannot be charged if the charge was refunded.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_billPresentButRefundedBilledAgain_normal() throws Exception
    {
        createSpid(false);

        ServiceFee2 service = createBillingTransaction(19, false, billdateNormal, ADJUSTMENT_ID, 1000);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 5);
        ServiceFee2 service2 = createBillingTransaction(19, true, calendar.getTime(), ADJUSTMENT_ID, -500);

        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 9);
        ServiceFee2 service3 = createBillingTransaction(19, true, calendar.getTime(), ADJUSTMENT_ID, 300);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);
        assertTrue("BC should be considered as charged.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_billPresentButRefundedBilledAgain_preBilled() throws Exception
    {
        createSpid(true);

        ServiceFee2 service = createBillingTransaction(20, false, billdatePrebilled, ADJUSTMENT_ID, 1000);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdatePrebilled);
        calendar.add(Calendar.DATE, 5);
        ServiceFee2 service2 = createBillingTransaction(20, true, calendar.getTime(), ADJUSTMENT_ID, -500);

        calendar.setTime(billdatePrebilled);
        calendar.add(Calendar.DATE, 9);
        ServiceFee2 service3 = createBillingTransaction(20, true, calendar.getTime(), ADJUSTMENT_ID, 300);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC should be considered as charged.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_proratedPresentButRefundedBilledAgain_normal() throws Exception
    {
        createSpid(false);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 3);
        ServiceFee2 service = createBillingTransaction(21, true, calendar.getTime(), ADJUSTMENT_ID, 800);

        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 7);
        ServiceFee2 service2 = createBillingTransaction(21, true, calendar.getTime(), ADJUSTMENT_ID, -500);

        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 9);
        ServiceFee2 service3 = createBillingTransaction(21, true, calendar.getTime(), ADJUSTMENT_ID, 300);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC should be considered as charged.", rez);
    }

    public void testBillingCycleChargedAndNotRefunded_proratedPresentButRefundedBilledAgain_preBilled() throws Exception
    {
        createSpid(true);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 3);
        ServiceFee2 service = createBillingTransaction(22, true, calendar.getTime(), ADJUSTMENT_ID, 800);

        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 7);
        ServiceFee2 service2 = createBillingTransaction(22, true, calendar.getTime(), ADJUSTMENT_ID, -500);

        calendar.setTime(billdateNormal);
        calendar.add(Calendar.DATE, 9);
        ServiceFee2 service3 = createBillingTransaction(22, true, calendar.getTime(), ADJUSTMENT_ID, 300);

        boolean rez;
        rez = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(getContext(), sub, service, ChargedItemTypeEnum.SERVICE, ServicePeriodEnum.MONTHLY, ADJUSTMENT_ID, 500, querydate);

        assertTrue("BC should be considered as charged.", rez);
    }

    int CREATED_TRANSACTIONS = 0;

    CRMSpidHome spidsHome;
    TransactionHome transactionsHome;
    SubscriberSubscriptionHistoryHome historyHome;

    CRMSpid spid;
    Subscriber sub;

    Date querydate, billdateNormal, billdatePrebilled;
}
