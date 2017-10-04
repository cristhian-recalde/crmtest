/*
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
package com.trilogy.app.crm.home;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for AccountPromiseToPayDateClearingHome.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestAccountPromiseToPayDateClearingHome
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestAccountPromiseToPayDateClearingHome(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestAccountPromiseToPayDateClearingHome.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
        
        // Set up a simple Account with just the BAN for testing. 
        account_ = new Account();
        account_.setBAN("dummy");
        
        home_ =
            new AccountPromiseToPayDateClearingHome(
                getContext(),
                new AccountTransientHome(getContext()));
    }


    // INHERIT
    public void tearDown()
    {
        home_ = null;
        account_ = null;
        
        super.tearDown();
    }
    
    
    /**
     * Tests that the create() method works according to the intent.
     */
    public void testCreate()
        throws HomeException
    {
        final Date todayDate =
            CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(Calendar.getInstance().getTime());
        
        // Test case for PTP account.
        {
            account_.setState(AccountStateEnum.PROMISE_TO_PAY);
            account_.setPromiseToPayDate(todayDate);
            home_.create(getContext(),account_);
            
            final Account createdAccount = (Account) home_.find(getContext(),"dummy");
            assertTrue(
                "For PTP state, the PTP Date should be the same as the one we set",
                todayDate.equals(createdAccount.getPromiseToPayDate()));
        }
        
        // Test case for non-PTP account.
        {
            tearDown();
            setUp();
            
            account_.setState(AccountStateEnum.ACTIVE);
            account_.setPromiseToPayDate(todayDate);
            home_.create(getContext(),account_);
            
            final Account createdAccount = (Account) home_.find(getContext(),"dummy");
            assertTrue(
                "For non-PTP state, the PTP Date should have been cleared",
                createdAccount.getPromiseToPayDate() == null);
        }
    }
    
    
    /**
     * Tests that the store() method works according to the intent.
     */
    public void testStore()
        throws HomeException
    {
        final Date todayDate =
            CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(Calendar.getInstance().getTime());
        
        // Test case for PTP account.
        {
            home_.create(getContext(),account_);  // Create the account first
            
            account_.setState(AccountStateEnum.PROMISE_TO_PAY);
            account_.setPromiseToPayDate(todayDate);
            home_.store(getContext(),account_);
            
            final Account storedAccount = (Account) home_.find(getContext(),"dummy");
            assertTrue(
                "For PTP state, the PTP Date should be the same as the one we set",
                todayDate.equals(storedAccount.getPromiseToPayDate()));
        }
        
        // Test case for non-PTP account.
        {
            tearDown();
            setUp();
            
            home_.create(getContext(),account_);  // Create the account first
            
            account_.setState(AccountStateEnum.ACTIVE);
            account_.setPromiseToPayDate(todayDate);
            home_.store(getContext(),account_);
            
            final Account storedAccount = (Account) home_.find(getContext(),"dummy");
            assertTrue(
                "For non-PTP state, the PTP Date should have been cleared",
                storedAccount.getPromiseToPayDate() == null);
        }
    }
    
    
    /**
     * Some reusable objects.
     */
    private Account account_ = null;
    private Home home_ = null;
}
