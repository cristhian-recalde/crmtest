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
package com.trilogy.app.crm.home.account;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.license.DefaultLicenseMgr;
import com.trilogy.framework.license.License;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryTransientHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleTransientHome;
import com.trilogy.app.crm.bean.BlackList;
import com.trilogy.app.crm.bean.BlackListConfigHome;
import com.trilogy.app.crm.bean.BlackListConfigTransientHome;
import com.trilogy.app.crm.bean.BlackListHome;
import com.trilogy.app.crm.bean.BlackListTransientHome;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for AccountValidator.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestAccountValidator
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestAccountValidator(final String name)
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

        final TestSuite suite = new TestSuite(TestAccountValidator.class);

        return suite;
    }


    // INHERIT
    @Override
    public void setUp()
    {
        super.setUp();
        
        // Set up a BillCycle home for testing.
        billCycleHome_ = new BillCycleTransientHome(getContext());
        final BillCycle billCycle = new BillCycle();
        billCycle.setBillCycleID(123);
        billCycle.setSpid(123);
        try
        {
            billCycleHome_.create(getContext(),billCycle);
        }
        catch (HomeException e)
        {
            fail("Failed to create a BillCycle for testing");
        }
        getContext().put(BillCycleHome.class, billCycleHome_);
        
        // Set up a BlackList home for testing.
        blackListHome_ = new BlackListTransientHome(getContext());
        final BlackList blackList = new BlackList();
        blackList.setBlackListID(123);
        blackList.setIdType(123);
        blackList.setIdNumber("dummy");
        blackList.setBlackType(BlackTypeEnum.BLACK);
        try
        {
            blackListHome_.create(getContext(),blackList);
        }
        catch (HomeException e)
        {
            fail("Failed to create a BlackList for testing");
        }
        getContext().put(BlackListHome.class, blackListHome_);
        
        // Set up a AccountType home for testing.
        accountTypeHome_ = new AccountCategoryTransientHome(getContext());
        final AccountCategory accountType = new AccountCategory();
        accountType.setIdentifier(123);
        accountType.setCustomerType(CustomerTypeEnum.CORPORATE);
        try
        {
            accountTypeHome_.create(getContext(),accountType);
        }
        catch (HomeException e)
        {
            fail("Failed to create an AccountType for testing");
        }
        getContext().put(AccountCategoryHome.class, accountTypeHome_);
        
        
        Home licenseHome = new com.redknee.framework.license.LicenseTransientHome(getContext());
        createLicense(licenseHome, LicenseConstants.PREPAID_LICENSE_KEY);
        createLicense(licenseHome, LicenseConstants.POSTPAID_LICENSE_KEY);
        createLicense(licenseHome, LicenseConstants.HYBRID_LICENSE_KEY);
        /**
         * Put my license home into the context
         */
        getContext().put(com.redknee.framework.license.LicenseHome.class, licenseHome);
        
        /**
         * Put my license manager into the context, this is used by the 
         * app crm LicenseHome
         */
        getContext().put(LicenseMgr.class,
                new DefaultLicenseMgr(getContext()));
        
        
        
        // Set up a VALID Account for testing.
        account_ = new Account();
        account_.setContext(getContext());
        account_.setType(123);  // Business
        account_.setContactName("dummy");
        account_.setContactTel("123567789");
        account_.setEmployer("dummy");
        account_.setEmployerAddress("dummy");
        account_.setCompanyName("dummy");
        account_.setCompanyAddress1("dummy");
        account_.setCompanyCity("dummy");
        account_.setCompanyCountry("dummy");
        account_.setCompanyFax("123456789");
        account_.setCompanyTel("123456789");
        account_.setTradingName("dummy");
        account_.setRegistrationNumber("123");
        account_.setFirstName("dummy");
        account_.setLastName("dummy");

        List accountIdList = new ArrayList();
        account_.setIdentificationGroupList(accountIdList);
        AccountIdentificationGroup aig = new AccountIdentificationGroup();
        AccountIdentification ai = new AccountIdentification();
        ai.setIdType(456);
        ai.setIdNumber("dummy");
        accountIdList.add(aig);
        aig.getIdentificationList().add(ai);
        ai = new AccountIdentification();
        ai.setIdType(789);
        ai.setIdType(789);
        ai.setIdNumber("dummy");
        aig.getIdentificationList().add(ai);

        account_.setSpid(123);

        try
        {
            Home home = new AccountTransientHome(getContext());
            getContext().put(AccountHome.class, home);
            home.create(account_);
        }
        catch (final HomeException exception)
        {
            fail("Failed to create a transient AccountHome and place the test account within it.");
        }
        
        getContext().put(BlackListConfigHome.class, new BlackListConfigTransientHome(getContext()));
        
        getContext().put(SysFeatureCfg.class, new SysFeatureCfg());

        accountValidator_ = new AccountValidator();
    }


    // INHERIT
    @Override
    public void tearDown()
    {
        accountValidator_ = null;
        account_ = null;
        accountTypeHome_ = null;
        blackListHome_ = null;
        billCycleHome_ = null;
        
        super.tearDown();
    }
    
    
    public void createLicense(Home home, String key){
        License myLicense = new License();
        myLicense.setName(key);
        myLicense.setKey(key);
        myLicense.setEnabled(true);
        try
        {
            home.create(getContext(), myLicense);
        }
        catch (Exception e)
        {
            fail("Unexpected exception installing license into license manager " + e.getMessage());
        }
            	
    	
    }
    
    
    /**
     * Tests that the validate() method works according to the intent.
     */
    public void testValidate()
    {
        // TODO 2009-02-25 rewrite in separate test methods
        // Test case for required fields.
        try
        {
            account_.setEmployer("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because Contact Name " +
                 "is mandatory for Business account");
            
            tearDown();
            setUp();
            account_.setEmployerAddress("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because Contact Telephone Number " +
                 "is mandatory for Business account");
            
            tearDown();
            setUp();
            account_.setCompanyName("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because Company Name " +
                 "is mandatory for Business account");
            
            tearDown();
            setUp();
            account_.setCompanyAddress1("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because Company Address " +
                 "is mandatory for Business account");
            
            tearDown();
            setUp();
            account_.setCompanyCity("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because Company City " +
                 "is mandatory for Business account");
            
            tearDown();
            setUp();
            account_.setCompanyCountry("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because Company Country " +
                 "is mandatory for Business account");
            
            tearDown();
            setUp();
            account_.setTradingName("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because Trading Name " +
                 "is mandatory for Business account");
            
            tearDown();
            setUp();
            account_.setRegistrationNumber("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because Registration Number " +
                 "is mandatory for Business account");
            
            tearDown();
            setUp();
            account_.setCompanyTel("123");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because length of " +
                 "Company Telephone Number must be at least 7");
            
            tearDown();
            setUp();
            account_.setCompanyFax("123");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because length of " +
                 "Company Fax Number must be at least 7");
            
            tearDown();
            setUp();
            account_.setType(456);  // non-Business
            account_.setFirstName("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because First Name " +
                 "is mandatory for non-Business account");
            
            tearDown();
            setUp();
            account_.setType(456);  // non-Business
            account_.setLastName("");
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because Last Name " +
                 "is mandatory for non-Business account");
        }
        catch (IllegalStateException e)
        {
            // Do nothing
        }
        
        // Test case for blacklist's black type.
        try
        {
            tearDown();
            setUp();
            List accountIdList = new ArrayList();
            account_.setIdentificationGroupList(accountIdList);
            AccountIdentificationGroup aig = new AccountIdentificationGroup();
            AccountIdentification ai = new AccountIdentification();
            ai.setIdType(123);
            ai.setIdNumber("dummy");
            aig.getIdentificationList().add(ai);
            accountIdList.add(aig);
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because account " +
                 "is in black list");
        }
        catch (IllegalStateException e)
        {
            // Do nothing
        }
        
        // Test case for no BillCycle found.
        try
        {
            tearDown();
            setUp();
            account_.setSpid(456);
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because no Bill Cycle " +
                 "can be found for the selected SP");
        }
        catch (IllegalStateException e)
        {
            // Do nothing
        }
        
        // Test case for invalid Promise-To-Pay Date.
        try
        {
            tearDown();
            setUp();
            account_.setState(AccountStateEnum.PROMISE_TO_PAY);
            account_.setPromiseToPayDate(
                CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(Calendar.getInstance().getTime()));
            accountValidator_.validate(getContext(),account_);
            fail("IllegalStateException should have thrown because PTP Date " +
                 "is not after today");
        }
        catch (IllegalStateException e)
        {
            // Do nothing
        }
    }
    
    
    /**
     * Some reusable objects.
     */
    private BillCycleHome billCycleHome_ = null;
    private BlackListHome blackListHome_ = null;
    private AccountCategoryHome accountTypeHome_ = null;
    private Account account_ = null;
    private AccountValidator accountValidator_ = null;
    
    
    /**
     * Provides a convenient test wrapper class for ExceptionListener.
     */
    private class TestWrapperExceptionListener implements ExceptionListener
    {
        public void thrown(final Throwable t)
        {
            thrownInvoked_ = true;
        }

        public boolean isThrownInvoked()
        {
            return thrownInvoked_;
        }
        
        private boolean thrownInvoked_ = false;
    } // inner-class

    /**
     * replaces the original AccountValidator
     */
    private class AccountValidator implements Validator
    {
       public CompoundValidator delegate_;

       public AccountValidator()
       {
          delegate_ = new CompoundValidator()
             .add(new AccountSystemTypeValidator())
             .add(new BusinessAccountValidator())
             .add(new AccountBlacklistValidator())
             .add(new AccountBillCycleValidator())
             .add(new AccountPromiseToPayValidator());
       }

    public void validate(Context ctx, Object obj)
          throws IllegalStateException
       {
          delegate_.validate(ctx, obj);
       }
    }
}
