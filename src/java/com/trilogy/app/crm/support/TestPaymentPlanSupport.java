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

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.payment.PaymentPlanActionEnum;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistory;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistoryHome;
import com.trilogy.app.crm.unit_test.LicensedTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.app.crm.unit_test.TestSetupPaymentPlan;

/**
 * Test some functions in the PaymentPlanSupport class. 
 * @author ali
 *
 */
public class TestPaymentPlanSupport extends LicensedTestCase 
{
    public TestPaymentPlanSupport(String name)
    {
        super(name, new String[]{PaymentPlanSupport.PAYMENT_PLAN_LICENSE_KEY});
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }
    
    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by the Redknee Xtest code, which provides the application's operating context.
     * 
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestPaymentPlanSupport.class);
        return suite;
    }
    
    @Override
    public void setUp()
    {
        super.setUp();
        
        orderedEnrollments = new TreeMap<Date, PaymentPlanHistory>(new Comparator()
        {
            public int compare(Object obj1, Object obj2) 
            {
                // Sort in descending order
                Date date1 = (Date) obj1;
                Date date2 = (Date) obj2;
                if (date1.after(date2))
                {
                    return -1;
                }
                else if (date2.after(date1))
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        });
        
        setupHomes();
        
        setupHistoryRecords();
    }
    
    private void setupHomes()
    {
         TestSetupPaymentPlan.setup(getContext());
    }
    
    private void setupHistoryRecords()
    {
        // Purposely insert records in an scrambled order so that we know if the Sorting Home works.
        Home home = (Home) getContext().get(PaymentPlanHistoryHome.class);
        
        try
        {
            // Most recent record
            Date date = lastEnrollment_;
            PaymentPlanHistory record = createHistoryRecord(date, TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.ENROLL);
            home.create(record);
            record = createHistoryRecord(CalendarSupportHelper.get(getContext()).getDayAfter(date), TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.EXIT);
            home.create(record);
            record = createHistoryRecord(CalendarSupportHelper.get(getContext()).getDayBefore(date), "11002", DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.ENROLL);
            home.create(record);
            record = createHistoryRecord(date, "11002", DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.EXIT);
            home.create(record);
            
            // Record before the interval
            date = CalendarSupportHelper.get(getContext()).findDateDaysBefore(10, startOfInterval_);
            record = createHistoryRecord(date, TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.EXIT);
            home.create(record);
            record = createHistoryRecord(CalendarSupportHelper.get(getContext()).getDayBefore(date), TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.ENROLL);
            home.create(record);
            
            // Records within the interval
            date = startOfInterval_;
            record = createHistoryRecord(CalendarSupportHelper.get(getContext()).findDateDaysAfter(5,date), TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.EXIT);
            home.create(record);
            record = createHistoryRecord(date, TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.ENROLL);
            home.create(record);
            
            date = CalendarSupportHelper.get(getContext()).findDateDaysAfter(30, startOfInterval_);
            record = createHistoryRecord(CalendarSupportHelper.get(getContext()).findDateDaysAfter(10,date), TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.EXIT);
            home.create(record);
            record = createHistoryRecord(date, TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.ENROLL);
            home.create(record);
            
            date = endOfInterval_;
            record = createHistoryRecord(date, TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.ENROLL);
            home.create(record);
            record = createHistoryRecord(CalendarSupportHelper.get(getContext()).findDateDaysAfter(CommonTime.YEARS_IN_FUTURE,date), TEST_ACCOUNT, DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.EXIT);
            home.create(record);
            
        }
        catch (Exception e)
        {
            fail("Failure during setting up History Records. " + e.getMessage());
        }
    }
    
    private PaymentPlanHistory createHistoryRecord(final Date date, final String accountId, long paymentPlanId, PaymentPlanActionEnum action)
    {
        PaymentPlanHistory record = new PaymentPlanHistory();
        record.setRecordDate(date);
        record.setAccountId(accountId);
        record.setPaymentPlanId(paymentPlanId);
        record.setAction(action);
        
        //Add to the tracking map
        if (accountId.equals(TEST_ACCOUNT) && action.equals(PaymentPlanActionEnum.ENROLL))
        {
            orderedEnrollments.put(date, record);
        }
        
        return record;
    }
    
    private void setupTestAccount()
    {
        try
        {
            TestSetupAccountHierarchy.setup(getContext(), false);
            TestSetupAccountHierarchy.setupAccount(getContext(), TEST_ACCOUNT, "", true);
            // Enroll Account in Payment Plan
            Account account = AccountSupport.getAccount(getContext(), TEST_ACCOUNT);
            account.setPaymentPlan(DEFAULT_PAYMENT_PLAN);
            account.setPaymentPlanAmount(15000); //$150
            account.setPaymentPlanMonthlyAmount(5000);
            account.setPaymentPlanStartDate(dateInAccountProfile_);

            Home home  = (Home) getContext().get(AccountHome.class);
            home.store(account);
            
            TestSetupAccountHierarchy.setupSubscriber(getContext(), TEST_ACCOUNT, TEST_ACCOUNT+"-1", 
                    SubscriberTypeEnum.POSTPAID, "1234445555", SubscriberStateEnum.ACTIVE, startOfInterval_);
            
        }
        catch (Exception e)
        {
            fail("Failed to setup Test Account, error=" + e.getMessage());
        }
    }
    
    //  INHERIT
    @Override
    public void tearDown()  
    {
        //tear down here
        
        super.tearDown();
    }
    
    public void testSetup()
    {
        try
        {
            //Test that all the records were saved in the Home
            Home home = (Home) getContext().get(PaymentPlanHistoryHome.class);
            Collection col = home.selectAll();
            assertEquals(12, col.size());
        }
        catch (Exception e)
        {
            fail("Error: " + e.getMessage());
        }
    }
    
    public void testIsEnabled()
    {
        assertTrue("Payment Plan Feature is disabled.  Install the '" + PaymentPlanSupport.PAYMENT_PLAN_LICENSE_KEY +
                "' License before continuing test." ,PaymentPlanSupportHelper.get(getContext()).isEnabled(getContext()));
    }
    
    public void testIsHistoryDisabled()
    {
        assertTrue("Payment Plan History Feature is disabled.  Remove '" + PaymentPlanSupport.PAYMENT_PLAN_HISTORY_DISABLE +
                "' License before continuing test." ,PaymentPlanSupportHelper.get(getContext()).isHistoryEnabled(getContext()));
    }
    
    public void testCountPaymentPlanEnrollments()
    {
        try
        {
            long count = PaymentPlanSupportHelper.get(getContext()).countPaymentPlanEnrollments(getContext(), "123", startOfInterval_, endOfInterval_);
            assertEquals(0L, count);
            
            count = PaymentPlanSupportHelper.get(getContext()).countPaymentPlanEnrollments(getContext(), TEST_ACCOUNT, new Date(0), startOfInterval_);
            assertEquals(2L, count);
            
            count = PaymentPlanSupportHelper.get(getContext()).countPaymentPlanEnrollments(getContext(), TEST_ACCOUNT, new Date(0), CalendarSupportHelper.get(getContext()).getDateWithLastSecondofDay(
                    CalendarSupportHelper.get(getContext()).findDateDaysBefore(1, startOfInterval_) ) );
            assertEquals(1L, count);
            
            count = PaymentPlanSupportHelper.get(getContext()).countPaymentPlanEnrollments(getContext(), TEST_ACCOUNT, startOfInterval_, endOfInterval_);
            assertEquals(3L, count);
            
            count = PaymentPlanSupportHelper.get(getContext()).countPaymentPlanEnrollments(getContext(), TEST_ACCOUNT, endOfInterval_, new Date());
            assertEquals(2L, count);
            
            count = PaymentPlanSupportHelper.get(getContext()).countPaymentPlanEnrollments(getContext(), TEST_ACCOUNT, CalendarSupportHelper.get(getContext()).getDateWithLastSecondofDay(endOfInterval_), new Date() );
            assertEquals(1L, count);
        }
        catch (Exception e)
        {
            fail("Error: " + e.getMessage());
        }
    }
    
    public void testGetLastEnrollments()
    {
        try
        {
            Collection<PaymentPlanHistory> enrollments = PaymentPlanSupportHelper.get(getContext()).getLastEnrollments(getContext(), TEST_ACCOUNT, 0, new Date());
            assertEquals(0, enrollments.size());
            
            // Most recent enrollment
            enrollments = PaymentPlanSupportHelper.get(getContext()).getLastEnrollments(getContext(), TEST_ACCOUNT, 1, new Date());
            assertEquals(1, enrollments.size());
            PaymentPlanHistory history = enrollments.iterator().next();
            assertEquals(TEST_ACCOUNT, history.getAccountId());
            assertEquals(DEFAULT_PAYMENT_PLAN, history.getPaymentPlanId());
            assertEquals(PaymentPlanActionEnum.ENROLL, history.getAction());
            assertEquals(lastEnrollment_, history.getRecordDate());
            PaymentPlanHistory latestEntry = orderedEnrollments.get(orderedEnrollments.firstKey());
            assertTrue("Entry does not match Expected result", SafetyUtil.safeEquals(latestEntry, history));
            
            //Last 3 enrollments
            enrollments = PaymentPlanSupportHelper.get(getContext()).getLastEnrollments(getContext(), TEST_ACCOUNT, 3, new Date());
            assertEquals(3, enrollments.size());
            Iterator iter = enrollments.iterator();
            Iterator<Date> compareIter = orderedEnrollments.keySet().iterator();
            Date date = new Date();
            while (iter.hasNext())
            {
                history = (PaymentPlanHistory) iter.next();
                if (date.before(history.getRecordDate()))
                {
                    fail("Payment Plan History Records are not ordered by Date, in descending order.");
                }
                date = history.getRecordDate();
                assertEquals(TEST_ACCOUNT, history.getAccountId());
                assertEquals(DEFAULT_PAYMENT_PLAN, history.getPaymentPlanId());
                assertEquals(PaymentPlanActionEnum.ENROLL, history.getAction());
                PaymentPlanHistory entry = orderedEnrollments.get(compareIter.next());
                assertTrue("Entry does not match Expected result", SafetyUtil.safeEquals(entry, history));
            }
            
            // Test retrieving more enrollments than are recorded; 10 enrollments, when there are only 5 in the system.
            enrollments = PaymentPlanSupportHelper.get(getContext()).getLastEnrollments(getContext(), TEST_ACCOUNT, 10, new Date());
            assertEquals(5, enrollments.size());
            iter = enrollments.iterator();
            compareIter = orderedEnrollments.keySet().iterator();
            date = new Date();
            while (iter.hasNext())
            {
                history = (PaymentPlanHistory) iter.next();
                if (date.before(history.getRecordDate()))
                {
                    fail("Payment Plan History Records are not ordered by Date, in descending order.");
                }
                date = history.getRecordDate();
                assertEquals(TEST_ACCOUNT, history.getAccountId());
                assertEquals(DEFAULT_PAYMENT_PLAN, history.getPaymentPlanId());
                assertEquals(PaymentPlanActionEnum.ENROLL, history.getAction());
                PaymentPlanHistory entry = orderedEnrollments.get(compareIter.next());
                assertTrue("Entry does not match Expected result", SafetyUtil.safeEquals(entry, history));
            }
            
            
            // Get Last Enrollments as of a day in the Past
            enrollments = PaymentPlanSupportHelper.get(getContext()).getLastEnrollments(getContext(), TEST_ACCOUNT, 1, endOfInterval_);
            assertEquals(1, enrollments.size());
            history = enrollments.iterator().next();
            assertEquals(TEST_ACCOUNT, history.getAccountId());
            assertEquals(DEFAULT_PAYMENT_PLAN, history.getPaymentPlanId());
            assertEquals(PaymentPlanActionEnum.ENROLL, history.getAction());
            assertEquals(endOfInterval_, history.getRecordDate());
            
            /* Test retrieving more enrollments than are recorded; 10 enrollments, 
             * when there are only 4 in the system, prior to Date:endOfInterval_ */
            enrollments = PaymentPlanSupportHelper.get(getContext()).getLastEnrollments(getContext(), TEST_ACCOUNT, 10, endOfInterval_);
            assertEquals(4, enrollments.size());
            iter = enrollments.iterator();
            date = new Date();
            while (iter.hasNext())
            {
                history = (PaymentPlanHistory) iter.next();
                if (date.before(history.getRecordDate()))
                {
                    fail("Payment Plan History Records are not ordered by Date, in descending order.");
                }
                date = history.getRecordDate();
                assertEquals(TEST_ACCOUNT, history.getAccountId());
                assertEquals(DEFAULT_PAYMENT_PLAN, history.getPaymentPlanId());
                assertEquals(PaymentPlanActionEnum.ENROLL, history.getAction());
            }
        }
        catch (Exception e)
        {
            fail("Error: " + e.getMessage());
        }
    }
    
    public void testGetPaymentPlanStartDate()
    {
        setupTestAccount();
        
        Account account = null;
        try
        {
            account = AccountSupport.getAccount(getContext(), TEST_ACCOUNT);
        }
        catch (Exception e)
        {
            fail("Failed to lookup Account. Error=" + e.getMessage());
        }
        
        // Most recent enrollment
        Date paymentPlanStartDate = account.getPaymentPlanStartDate(getContext(), new Date());
        assertEquals(lastEnrollment_, paymentPlanStartDate);
        
        // Get Last Enrollments as of a day in the Past
        paymentPlanStartDate = account.getPaymentPlanStartDate(getContext(), endOfInterval_);
        assertEquals(endOfInterval_, paymentPlanStartDate);
        
        paymentPlanStartDate = account.getPaymentPlanStartDate(getContext(), CalendarSupportHelper.get(getContext()).findDateDaysAfter(5, startOfInterval_));
        assertEquals(startOfInterval_, paymentPlanStartDate);
        
    }
    
    final Date startOfInterval_ = new Date(1193889600000L); //Nov 01 00:00:00 EDT 2007
    final Date endOfInterval_ = new Date(1201842000000L); //Feb 01 00:00:00 EST 2008
    final Date dateInAccountProfile_ = new Date(1201842001000L); //Feb 01 00:00:01 EST 2008
    final Date lastEnrollment_ = new Date(1208750400000L); //Apr 21 00:00:00 EDT 2008
    final long DEFAULT_PAYMENT_PLAN = 2;
    TreeMap<Date, PaymentPlanHistory> orderedEnrollments;    // In Descending order by date.
    final String TEST_ACCOUNT = "11001";
}
