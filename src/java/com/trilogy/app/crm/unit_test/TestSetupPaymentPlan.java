package com.trilogy.app.crm.unit_test;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.payment.PaymentPlan;
import com.trilogy.app.crm.bean.payment.PaymentPlanActionEnum;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistory;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistoryHome;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistoryTransientHome;
import com.trilogy.app.crm.bean.payment.PaymentPlanHome;
import com.trilogy.app.crm.bean.payment.PaymentPlanTransientHome;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.PaymentPlanSupport;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * Setup components used to unit test Payment Plan Feature.
 * 
 * This class is used to setup the payment plan scenario for Accounts and subscribers created
 * in com.redknee.app.crm.unit_test.TestSetupAccountHierarchy.
 * 
 * @author angie.li
 *
 */
public class TestSetupPaymentPlan extends ContextAwareTestCase 
{
    public static long DEFAULT_PAYMENT_PLAN = 1L;
    public static long DEFAULT_PAYMENT_PLAN_AMOUNT = 100000;
    public static int DEFAULT_NUM_INSTALLMENTS = 4;

    public TestSetupPaymentPlan(String name)
    {
        super(name);
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
        final TestSuite suite = new TestSuite(TestSetupPaymentPlan.class);
        return suite;
    }
    
    @Override
    public void setUp()
    {
        super.setUp();
        setup(getContext());
    }
    
    //  INHERIT
    @Override
    public void tearDown()  
    {
        //tear down here
        completelyTearDown(getContext());
        
        super.tearDown();
    }
    
    /**
     * Sets up all new homes having to do with Payment Plan Feature, 
     * if has not been already executed.
     * @param ctx
     */
    public static void setup(Context ctx)
    {
        if (ctx.getBoolean(TestSetupPaymentPlan.class, true))
        {
            ctx.put(PaymentPlanHome.class, new TransientFieldResettingHome(ctx, new PaymentPlanTransientHome(ctx)));

            // Setup Payment Plan History Pipeline
            Home historyHome = new SortingHome(new TransientFieldResettingHome(ctx, new PaymentPlanHistoryTransientHome(ctx)));
            ctx.put(PaymentPlanHistoryHome.class, historyHome);

            //Install TestSetupPaymentPlan key to indicate not to setup again.
            ctx.put(TestSetupPaymentPlan.class, false);
            
            //Create a default Payment Plan 
            Home home = (Home) ctx.get(PaymentPlanHome.class);
            PaymentPlan pp = new PaymentPlan();
            pp.setId(DEFAULT_PAYMENT_PLAN);
            pp.setName("Unit Test Payment Plan");
            pp.setDesc("Unit Test Payment Plan");
            pp.setNumOfPayments(DEFAULT_NUM_INSTALLMENTS);
            pp.setCreditLimitDecrease(10);
            
            try
            {
                home.create(pp);
            }
            catch (HomeException e)
            {
                fail("Failed Payment Plan Setup. " + e.getMessage());
            }
        }
        else
        {
            LogSupport.debug(ctx, TestSetupPaymentPlan.class.getName(),
                    "Skipping TestSetupPaymentPlan setup again.");
        }
     }

    
    /**
     * Tear down the environment that was set up for this test.
     * @param ctx
     */
    public static void completelyTearDown(Context ctx)
    {
        try
        {
            Home home = (Home) ctx.get(PaymentPlanHome.class);
            home.removeAll();
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, TestSetupPaymentPlan.class.getName(),
                    "Failed to clean up Payment Plans. " + e.getMessage(), e);
        }
        
        try
        {
            Home home = (Home) ctx.get(PaymentPlanHistoryHome.class);
            home.removeAll();
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, TestSetupPaymentPlan.class.getName(),
                    "Failed to clean up Payment Plan History. " + e.getMessage(), e);
        }
    }
    
    
    /**
     * Store the relevant Payment Plan information into the given account.
     * @param context
     * @param ban  Account enrolling into the Payment Plan Program (identifier)
     * @param amount  initial Payment Plan Amount
     * @param startDate  Start Date of Payment Plan program 
     */
    public static void enrollIntoPaymentPlan(Context context, final String ban, 
            final long amount, final Date startDate, final int currentCount, 
            final int installmentsCharged)
    {
        try
        {
            Account account = AccountSupport.getAccount(context, ban);
            account.setPaymentPlan(TestSetupPaymentPlan.DEFAULT_PAYMENT_PLAN);
            account.setPaymentPlanAmount(amount);
            account.setPaymentPlanCurrCount(currentCount);
            account.setPaymentPlanInstallmentsCharged(installmentsCharged);
            account.setPaymentPlanMonthlyAmount(amount/TestSetupPaymentPlan.DEFAULT_NUM_INSTALLMENTS);
            account.setPaymentPlanStartDate(startDate);
            
            Home home = (Home) context.get(AccountHome.class);
            home.store(account);
            
            createHistoryRecord(context, startDate, ban, TestSetupPaymentPlan.DEFAULT_PAYMENT_PLAN, PaymentPlanActionEnum.ENROLL);
        }
        catch (HomeException e)
        {
            fail("Failed to enroll account " + ban + " in payment plan. " + e.getMessage());
        }
    }
    
    /**
     * Create and store the Payment Plan History Records in the system.
     * @param ctx
     * @param date
     * @param accountId
     * @param paymentPlanId
     * @param action
     */
    public static void createHistoryRecord(Context ctx, final Date date, 
            final String accountId, long paymentPlanId, PaymentPlanActionEnum action)
    {
        LicenseMgr mgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        if (!mgr.isLicensed(ctx, PaymentPlanSupport.PAYMENT_PLAN_HISTORY_DISABLE))
        {
            try
            {
                Home home = (Home) ctx.get(PaymentPlanHistoryHome.class);

                PaymentPlanHistory record = new PaymentPlanHistory();
                record.setRecordDate(date);
                record.setAccountId(accountId);
                record.setPaymentPlanId(paymentPlanId);
                record.setAction(action);

                home.create(record);
            }
            catch (HomeException e)
            {
                fail("Failed to create Payment Plan History records. " + e.getMessage());
            }
        }
        else
        {
            LogSupport.debug(ctx, TestSetupPaymentPlan.class.getName(), 
                "Skipped creating and storing Payment Plan History due to disabled license. ");
        }
    }
    
    
    public void testSetup()
    {
        testSetup(getContext());
    }
    
    /**
     * Test the Transient Homes were installed properly
     * @param ctx
     */
    public static void testSetup(Context ctx)
    {
        Home home = (Home) ctx.get(PaymentPlanHome.class);
        assertNotNull("PaymentPlanHome is null.  Setup Failed.", home);
        try
        {
            assertTrue(home.selectAll().size() == 1);
        }
        catch(HomeException e)
        {
            fail("Failed due to exception " + e.getMessage());
        }
        
        home = (Home) ctx.get(PaymentPlanHistoryHome.class);
        assertNotNull("PaymentPlanHistoryHome is null.  Setup Failed.", home);
    }
}
