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
package com.trilogy.app.crm.currency;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.CurrencyExchangeRateHistory;
import com.trilogy.app.crm.bean.CurrencyExchangeRateHistoryHome;
import com.trilogy.app.crm.bean.CurrencyExchangeRateHistoryXDBHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CurrencyExchangeRateHistorySupport;
import com.trilogy.app.crm.support.CurrencyExchangeRateHistorySupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Test Currency Exchange Rate History for some of the edge and normal cases
 * You can only run these test cases on a running application because this requires XDB connection
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class TestCurrencyHandler extends ContextAwareTestCase
{

    private Date fewDaysAgo;
    private Date fewHoursAgo;
    private Date fewMinsAgo;
    private Date todayDate;
    
    public TestCurrencyHandler(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by standard JUnit tools (i.e.,
     * those that do not provide a context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by the Redknee Xtest code,
     * which provides the application's operating context.
     * 
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestCurrencyHandler.class);

        return suite;
    }


    // INHERIT
    @Override
    public void setUp()
    {
        super.setUp();
        Context ctx = getContext();

//        Home serviceHome = new ServiceTransientHome(ctx);
 //       ctx.put(ServiceHome.class, serviceHome);
        Home currencyHome = new CurrencyExchangeRateHistoryXDBHome(ctx);
        ctx.put(CurrencyExchangeRateHistoryHome.class, currencyHome);

        todayDate = new Date();
        java.util.Calendar todayDateCalendar = new java.util.GregorianCalendar();
        todayDateCalendar.add(Calendar.HOUR_OF_DAY, -4);
        
        fewHoursAgo = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(todayDateCalendar.getTime());
        
        todayDateCalendar.add(Calendar.MINUTE, 46); 
        
        fewMinsAgo = todayDateCalendar.getTime();
        
        todayDateCalendar.add(Calendar.DAY_OF_MONTH,5);

        fewDaysAgo = todayDateCalendar.getTime();
        
        CurrencyExchangeRateHistory c1 = new CurrencyExchangeRateHistory();
        c1.setCurrencyId("USD");
        c1.setCurrencyRatio(1.5);
        c1.setStartDate(todayDate);
        c1.setUserName("kumaran");

        CurrencyExchangeRateHistory c2 = new CurrencyExchangeRateHistory();
        c2.setCurrencyId("USD");
        c2.setCurrencyRatio(2.0);
        c2.setStartDate(fewDaysAgo);
        c2.setUserName("kumaran");

        CurrencyExchangeRateHistory c3 = new CurrencyExchangeRateHistory();
        c3.setCurrencyId("USD");
        c3.setCurrencyRatio(3.0);
        c3.setStartDate(fewHoursAgo);
        c3.setUserName("kumaran");

        CurrencyExchangeRateHistory c4 = new CurrencyExchangeRateHistory();
        c4.setCurrencyId("USD");
        c4.setCurrencyRatio(4);
        c4.setStartDate(fewMinsAgo);
        c4.setUserName("kumaran");


        try
        {
            currencyHome.create(ctx, c1);
            currencyHome.create(ctx, c2);
            currencyHome.create(ctx, c3);
            currencyHome.create(ctx, c4);
        }
        catch (HomeException e)
        {
            throw new IllegalStateException("Cannot continue with tests.", e);
        }
    }


    public void testCurrentDateExchangeRate() throws HomeException
    {
        final Context ctx = getContext();

        CurrencyExchangeRateHistorySupport currencyHistory = CurrencyExchangeRateHistorySupportHelper.get(ctx);

        Date todayDate = new Date();
        
        CurrencyExchangeRateHistory exchangeRate = 
            currencyHistory.getCurrencyExchangeRateForCurrencyTypeAndInterval(ctx, todayDate, "USD");
        
        assertEquals(1.5, exchangeRate.getCurrencyRatio());
    }

    public void testExchangeRateWithOneEntryForExchangeRate() throws HomeException
    {
        final Context ctx = getContext();

        CurrencyExchangeRateHistorySupport currencyHistory = CurrencyExchangeRateHistorySupportHelper.get(ctx);
        
        CurrencyExchangeRateHistory exchangeRate = 
            currencyHistory.getCurrencyExchangeRateForCurrencyTypeAndInterval(ctx, fewDaysAgo, "USD");
        
        assertEquals(2.0, exchangeRate.getCurrencyRatio());
    }
    
    public void testExchangeRateForMultipleEntriesForOneDay() throws HomeException
    {
        final Context ctx = getContext();

        CurrencyExchangeRateHistorySupport currencyHistory = CurrencyExchangeRateHistorySupportHelper.get(ctx);
        
        CurrencyExchangeRateHistory exchangeRate = 
            currencyHistory.getCurrencyExchangeRateForCurrencyTypeAndInterval(ctx, fewHoursAgo, "USD");
        
        assertEquals(4, exchangeRate.getCurrencyRatio());
    }
    
    public void testExchangeRateForNoEntriesOnThatDate() throws HomeException
    {
        //ExchangeRates in the database only come after requesting exchangeDate
        final Context ctx = getContext();

        CurrencyExchangeRateHistorySupport currencyHistory = CurrencyExchangeRateHistorySupportHelper.get(ctx);
        
        Calendar fewDaysAgoCalendar = new java.util.GregorianCalendar();
        fewDaysAgoCalendar.setTime(fewDaysAgo);
        fewDaysAgoCalendar.add(Calendar.DAY_OF_MONTH, -10);
        
        CurrencyExchangeRateHistory exchangeRate = 
            currencyHistory.getCurrencyExchangeRateForCurrencyTypeAndInterval(ctx, fewDaysAgoCalendar.getTime(), "USD");
        
        assertEquals(2.0, exchangeRate.getCurrencyRatio());
    }
    
    public void testExchangeRateForEntriesForTheRequestedDate() throws HomeException
    {
        //ExchangeRates in the database before and after requesting exchangeDate
        final Context ctx = getContext();

        CurrencyExchangeRateHistorySupport currencyHistory = CurrencyExchangeRateHistorySupportHelper.get(ctx);
        
        Calendar fewDaysAgoCalendar = new java.util.GregorianCalendar();
        fewDaysAgoCalendar.setTime(fewDaysAgo);
        fewDaysAgoCalendar.add(Calendar.DAY_OF_MONTH, 1);
        
        CurrencyExchangeRateHistory exchangeRate = 
            currencyHistory.getCurrencyExchangeRateForCurrencyTypeAndInterval(ctx, fewDaysAgoCalendar.getTime(), "USD");
        
        assertEquals(2.0, exchangeRate.getCurrencyRatio());
    }
}