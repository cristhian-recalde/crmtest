/*
 * InvoiceSearchBorder
 * 
 * Author : Danny Ng Date : 2004-11-03
 * 
 * Copyright (c) 2003, Redknee All rights reserved.
 */
package com.trilogy.app.crm.web.border;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.InvoiceSearch;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.PrivilegedAccessor;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.web.search.SearchBorder;

/**
 * A suite of unit tests for InvoiceSearchBorder
 * 
 * @author Danny Ng
 */
public class TestInvoiceSearchBorder
    extends ContextAwareTestCase
{

    public InvoiceSearch criteria_;
    
    /**
     * Constructs a test case with the given name.
     * 
     * @param name
     *            The name of the test.
     */
    public TestInvoiceSearchBorder(String name)
    {
        super(name);
    }
    
    /**
     * Creates a new suite of Tests for execution. This method is intended to be
     * invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be
     * invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     * 
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestInvoiceSearchBorder.class);

        return suite;
    }
    
    // INHERIT
    public void setUp()
    {
        criteria_ = new InvoiceSearch();
        super.setUp();
    }


    // INHERIT
    public void tearDown()
    {
        criteria_ = null;
        super.tearDown();
    }
    
    /**
     * Tests that it if there is no user input it does not
     * attempt to find the ban from the input.
     *
     */
    public void testInvoiceSearchForNoInput()
    {
        // No input
        criteria_.setBAN("");
        criteria_.setMsisdn("");
        SearchBorder.setCriteria(getContext(), criteria_);
        
        InvoiceSearchBorderTester isb = new InvoiceSearchBorderTester(getContext());
        
        assertFalse("Attempted to find the ban with no user input", isb.calledFindBan);
    }
    
    /**
     * Tests the find band method returns the correct BAN
     * in the two cases where only ban is inputed and only
     * msisdn is inputed.
     *
     */
    public void testFindBanWithValidInput()
    {
        String testBan = "0100";
        String testSubscriber = "0100-1";
        String testMsisdn = "1234567890";
        
        // BAN input with no MSISDN input
        criteria_.setBAN(testBan);
        criteria_.setMsisdn("");
        SearchBorder.setCriteria(getContext(), criteria_);
        
        // Create search border
        InvoiceSearchBorder isb = new InvoiceSearchBorder(getContext());
        
        String ban = null;
        ban = invokeFindBan(isb);
      
        assertTrue("Ban found did not match input", testBan.equals(ban));
        
        
        ///////////////////////////// Msisdn input with no BAN input /////////////////
        tearDown();
        setUp();
        ban = null;
        // MSISDN input with no BAN input
        criteria_.setBAN("");
        criteria_.setMsisdn(testMsisdn);
        SearchBorder.setCriteria(getContext(), criteria_);
        
        // Putting msisdn, subscriber and account into the context
        // So that when the InvoiceSearchBorder uses the SubscriberSupport
        // It can find the right account
        setupContext(testMsisdn, testSubscriber, testBan);
        ban = invokeFindBan(isb);
        try
        {
            Method findBan = PrivilegedAccessor.getMethod(
                        InvoiceSearchBorder.class, 
                        "findBan", 
                        new Class[]{Context.class, criteria_.getClass()}
                    );
            ban = (String) findBan.invoke(
                        isb, new Object[]{getContext(), criteria_}
                    );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        assertTrue("Ban found did not match input", testBan.equals(ban));
    }
    
    /**
     * Tests that the findBan method returns an empty string
     * for invalid input.
     * 
     * Tests the scenerio of both BAN and MSISDN are inputted
     * which should return empty string.
     * 
     * Tests the scenerio of an invalid or non existent MSISDN
     * entered which should return empty string.
     *
     */
    public void testFindBanWithInValidInput()
    {
        String testBan = "0100";
        String testSubscriber = "0100-1";
        String testMsisdn = "1234567890";
        String invalidMsisdn = "1111111111";
        
        String ban = null;
        InvoiceSearchBorder isb = new InvoiceSearchBorder(getContext());
        
        // BAN input along with MSISDN input
        criteria_.setBAN(testBan);
        criteria_.setMsisdn(testMsisdn);
        SearchBorder.setCriteria(getContext(), criteria_);
        ban = invokeFindBan(isb);
        
        assertTrue("Non empty BAN found with inputs for account and msisdn", ban.equals(""));
        
        // No Ban input with invalid MSISDN input
        setupContext(invalidMsisdn, testSubscriber, testBan);
        
        ban = invokeFindBan(isb);
        
        assertTrue("Non empty BAN found with non existent MSISDN", ban.equals(""));
        
    }
    
    private void setupContext(String msisdn, String subscriber, String account) {
        try
        {
            // Putting msisdn into context
            Home msisdnHome = new MsisdnHomeTester();
            Msisdn msisdnObject = new Msisdn();
            msisdnObject.setMsisdn(msisdn);
            // FIXME CANDY
            msisdnObject.setBAN(account);
            msisdnHome.store(getContext(),msisdnObject);
            getContext().put(MsisdnHome.class, msisdnHome);
            
            // Putting subscriber into context
            Home subscriberHome = new SubscriberHomeTester();
            Subscriber subscriberObject = new Subscriber();
            subscriberObject.setBAN(account);
            subscriberHome.store(getContext(),subscriberObject);
            getContext().put(SubscriberHome.class, subscriberHome);
            
            // Putting account into context
            Home accountHome = new AccountHomeTester();
            Account accountObject = new Account();
            accountObject.setBAN(account);
            accountHome.store(getContext(),accountObject);
            getContext().put(AccountHome.class, accountHome);
            
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }
    
    private String invokeFindBan(InvoiceSearchBorder isb) {
        String ban = null;
        try
        {
            Method findBan = PrivilegedAccessor.getMethod(
                        InvoiceSearchBorder.class, 
                        "findBan", 
                        new Class[]{Context.class, criteria_.getClass()}
                    );
            ban = (String) findBan.invoke(
                        isb, new Object[]{getContext(), criteria_}
                    );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ban;
    }
}

class InvoiceSearchBorderTester
    extends InvoiceSearchBorder
{
    public boolean calledFindBan = false;
    
    public InvoiceSearchBorderTester(final Context context)
    {
        super(context);
    }
    
    /*private String findBan(Context ctx, InvoiceSearch criteria)
    {
        calledFindBan = true;
        return "";
    }*/
}


class MsisdnHomeTester extends HomeProxy
    implements MsisdnHome
{
    public Msisdn msisdnObject_;

    public Object create(Context ctx, Object obj)
            throws HomeException
    {
        msisdnObject_ = (Msisdn) obj;
        return obj;
    }

    public Object store(Context ctx,Object obj)
            throws HomeException
    {
        msisdnObject_ = (Msisdn) obj;
        return obj;
    }

    public Object find(Context ctx, Object key)
            throws HomeException
    {
        return msisdnObject_;
    }
}

class SubscriberHomeTester extends HomeProxy
    implements SubscriberHome
{
    public Subscriber subscriberObject_;

    public Object create(Context ctx, Object obj)
            throws HomeException
    {
        subscriberObject_ = (Subscriber) obj;
        return obj;
    }

    public Object store(Context ctx,Object obj)
            throws HomeException
    {
        subscriberObject_ = (Subscriber) obj;
        return obj;
    }

    public Object find(Context ctx, Object key)
            throws HomeException
    {
        return subscriberObject_;
    }
}

class AccountHomeTester extends HomeProxy
    implements AccountHome
{
    public Account accountObject_;

    public Object create(Context ctx, Object obj)
            throws HomeException
    {
        accountObject_ = (Account) obj;
        return obj;
    }

    public Object store(Context ctx,Object obj)
            throws HomeException
    {
        accountObject_ = (Account) obj;
        return obj;
    }

    public Object find(Context ctx, Object key)
            throws HomeException
    {
        return accountObject_;
    }
}
