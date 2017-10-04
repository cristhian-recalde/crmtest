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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.unit_test.xtest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.application.RemoteApplication;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.app.crm.unit_test.TestSetupMobileNumbers;
import com.trilogy.app.crm.unit_test.TestSetupPackage;
import com.trilogy.app.crm.unit_test.TestSetupPricePlanAndServices;
import com.trilogy.app.crm.unit_test.UnitTestSupport;

/**
 * This class has helper methods that will create Account Hierarchy for XTests.
 * 
 * Accounts and Subscribers are created by sending URL commands to CRM.  
 * Disclaimer:
 * This test harness will break if:
 *  + the menu used to create Accounts and Subscribers changes
 *  + the account or subscriber beans change (added required fields)
 *  + the account or subscriber pipelines add more validation for field that are not specified at this time.
 *  
 * Example of Usage:
 * com.redknee.app.crm.move.XTestMoveManager
 * 
 * @author angie.li@redknee.com
 *
 */
public class XTestSetupAccountHierarchy extends ContextAwareTestCase 
{
    public XTestSetupAccountHierarchy(String name)
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
        final TestSuite suite = new TestSuite(XTestSetupAccountHierarchy.class);
        return suite;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
     */
    @Override
    protected void setUp() 
    {
        super.setUp();

        setup(getContext());
    }

    public static void setup(Context context) 
    {
        if (getAllowSetupInstallation(context))
        {
            //Setup Homes
            //Install only if running on-line
            if (UnitTestSupport.isTestRunningInXTest(context))
            {
                //TestSetupPricePlanAndServices.setup(context);

                TestSetupAccountHierarchy.setup(context, false, false);

                //For Test MSISDN 
                try
                {
                    IdentifierSequenceSupportHelper.get(context).ensureSequenceExists(context, UNIT_TEST_MSISDN, 999991000L, 9999999999L);
                    IdentifierSequenceSupportHelper.get(context).ensureSequenceExists(context, UNIT_TEST_UNIQUE_ACCOUNT_ID, 1000L, Long.MAX_VALUE);

                }
                catch(Exception e)
                {
                    logDebugMsg(context, "Failed to create the IdentifierSequence for Unit test Msisdns. " + e.getMessage(), e);
                }

                //Setup Price Plan and Services
                TestSetupPricePlanAndServices.setup(context);
                
                //Setup the Pooled MSISDN Group 
                TestSetupMobileNumbers.setupUnitTestPoolMsisdn(context);
                
                //Prevent the setup from running repeatedly
                setAllowSetupInstallation(context, false);
            }
            else
            {
                LogSupport.debug(context, XTestSetupAccountHierarchy.class, "Skip XTestSetupAccountHierarchy.setup.  This test is only to be run online.");
            }
        }
    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#tearDown()
     */
    @Override
    protected void tearDown() 
    {
        super.tearDown();

        completelyTearDown(getContext());
    }

    public static void completelyTearDown(Context context) 
    {
        TestSetupAccountHierarchy.completelyTearDown(context);
        TestSetupPricePlanAndServices.completelyTearDown(context);
        
        //Allow live Unit test setup to run again. 
        setAllowSetupInstallation(context, true);
    }
    
    /**
     * Return TRUE if the installation of the configuration setup is permitted.
     * @param ctx
     * @return
     */
    public static boolean getAllowSetupInstallation(Context ctx) 
    {
        return ctx.getBoolean(XTestSetupAccountHierarchy.class, true);
    }
    
    /**
     * Sets the flag to allow configuration overwriting. 
     * @param ctx
     * @param b  If TRUE, allows configuration setup to run again (potentially overwriting old setup).
     */
    public static void setAllowSetupInstallation(Context ctx, boolean b) 
    {
        ctx.put(XTestSetupAccountHierarchy.class, b);
    }


    /**
     * Create an Individual Subscriber Account with accompanying Subscription (Airtime) and the 
     * given parameters
     * @param context
     * @param subscriberType
     * @param parentBAN
     * @param isResponsible
     * @return
     */
    public static String setupIndividualSubscriberAccount(Context context, 
            final SubscriberTypeEnum subscriberType, 
            final String parentBAN, 
            final boolean isResponsible,
            boolean isPooledMember) 
    {
        // Using the URL create Account
        String newAccount = "";
        try
        {
            newAccount = createAccount(context, parentBAN, isResponsible, 
                    TestSetupAccountHierarchy.ACCOUNT_TYPE_INDIVIDUAL, subscriberType);
            TestSetupAccountHierarchy.addTestAccountBan(newAccount);

            //Get Unique msisdn for the test
            long msisdn = prepareNonPoolMsisdnAndSIMPackage(context);

            //Add the Account to the Application context so that when we start the session to create the subscription for this Account it will pick up the correct BAN.
            Context applicationCtx = (Context) context.get("core");
            Account account = AccountSupport.getAccount(context, newAccount);
            applicationCtx.put(Account.class, account);

            //PackageId and msisdn will be the same.  No sense in creating another unique number.
            createActiveAirtimeSubscription(context, subscriberType, msisdn, msisdn);
            
            if (subscriberType.equals(SubscriberTypeEnum.PREPAID))
            {
                createActiveWalletSubscription(context, SubscriberTypeEnum.PREPAID, msisdn,
                        TestSetupAccountHierarchy.DEFAULT_WALLET_SUBSCRIPTION_CLASS,
                        TestSetupAccountHierarchy.getSubscriptionTypeId(SubscriptionTypeEnum.MOBILE_WALLET_INDEX),
                        isPooledMember);
            }
        }
        catch(IOException e)
        {
            e.getStackTrace();
            fail("Failed setup due to: " + e.getMessage());
        }
        catch(Exception e)
        {
            String msg = "Failed setup due to: " + e.getMessage();
            logDebugMsg(context, msg, e);
            fail(msg);
        }
        return newAccount;
    }

    /**
     * Create a Responsible Group Account and return the requested BAN in the created hierarchy.
     * @param context
     * @param banSelection
     * @return
     */
    public static String setupGroupAccount(Context context, final ReturnIdentifier banSelection) 
    {
        return setupGroupAccount(context, "", true, banSelection);
    }

    /**
     * Sets up a group account in the system with the given parameters.
     * Returns the account identifier depending on returnNonResponsibleBAN value.
     * @param context
     * @param parentBAN
     * @param isResponsible
     * @param returnNonResponsibleBAN if TRUE: returns the identifier of a individual subscriber within the newly 
     *                             created group account.  Otherwise, returns the root account identifier (root of group account).
     * @return
     */
    public static String setupGroupAccount(Context context,
            final String parentBAN,
            final boolean isResponsible,
            final ReturnIdentifier banSelection) 
    {
        // Using the URL create Account
        String newParentAccount = "";
        String newSubscriberAccount = ""; 
        try
        {
            newParentAccount = createAccount(context, parentBAN, isResponsible, 
                    TestSetupAccountHierarchy.ACCOUNT_TYPE_NON_INDIVIDUAL, SubscriberTypeEnum.HYBRID);
            TestSetupAccountHierarchy.addTestAccountBan(newParentAccount);

            newSubscriberAccount = setupIndividualSubscriberAccount(context, 
                    SubscriberTypeEnum.PREPAID, 
                    newParentAccount, 
                    false, //Subscriber Account is non responsible
                    false); //Subscriber Account is not a pool member
        }
        catch(IOException e)
        {
            String msg = "Failed setupGroupAccount due to: " + e.getMessage();
            logDebugMsg(context, msg, e);
            fail("Failed setup due to: " + e.getMessage());
        }
        String returnedBAN = getSelectedBAN(banSelection, newParentAccount, newSubscriberAccount, 
                newSubscriberAccount, newSubscriberAccount, newSubscriberAccount);
        return returnedBAN;
    }

    /**
     * Creates a hierarchy of group accounts (2 levels).
     * Returns the account identifier depending on returnNonResponsibleBAN value.
     * 
     * @param context
     * @param returnNonResponsibleBAN   if TRUE: returns the identifier of a group account immediately below 
     *                                         the root of the hierarchy.  Otherwise, returns the identifier of the
     *                                         root of the hierarchy.
     * @return
     */
    public static String setupAccountsWithHierarchy(Context context, final ReturnIdentifier banSelection)
    {
        // Using the URL create Account
        String parentAccount = "";
        String midGroupAccount = "";
        String leafGroupAccount = "";
        try
        {
            parentAccount = createAccount(context, "", true, 
                    TestSetupAccountHierarchy.ACCOUNT_TYPE_NON_INDIVIDUAL, SubscriberTypeEnum.HYBRID);
            TestSetupAccountHierarchy.addTestAccountBan(parentAccount);

            midGroupAccount = setupGroupAccount(context, parentAccount, false, ReturnIdentifier.ROOT);
            TestSetupAccountHierarchy.addTestAccountBan(midGroupAccount);

            leafGroupAccount = setupGroupAccount(context, midGroupAccount, false, ReturnIdentifier.ROOT);
            TestSetupAccountHierarchy.addTestAccountBan(leafGroupAccount);
        }
        catch(IOException e)
        {
            String msg = "Failed setupAccountsWithHierarchy due to: " + e.getMessage();
            logDebugMsg(context, msg, e);
            fail("Failed setup due to: " + e.getMessage());
        }
        String returnedBAN = getSelectedBAN(banSelection, parentAccount, midGroupAccount, 
                leafGroupAccount, midGroupAccount, leafGroupAccount);
        return returnedBAN;
    }


    /**
     * Creates a Pool Group Hierarchy, with two subscriber members. (PREPAID)
     * 
     * TODO: Create a hierarchy for POSTPAID Pooled Accounts. 
     * @param context
     * @param parentBAN
     * @param selectBAN
     * @return
     */
    public static String setupPooledAccount(Context ctx,
            final String parentBAN,
            final ReturnIdentifier selectBAN) 
    {
        // Using the URL create Account
        String newParentAccount = "";
        String memberSubscriber = "";
        try
        {
            String poolMsisdn = preparePoolMsisdnAndSIMPackage(ctx);
            newParentAccount = createPooledAccount(ctx, parentBAN, SubscriberTypeEnum.PREPAID, poolMsisdn);
            TestSetupAccountHierarchy.addTestAccountBan(newParentAccount);

            //All Pooled Account Members are Non-Responsible
            boolean isResponsible = false;
            //Subscriber Account is a pool member
            boolean isPoolMember = true;
            
            setupIndividualSubscriberAccount(ctx, 
                    SubscriberTypeEnum.PREPAID, 
                    newParentAccount, 
                    isResponsible,
                    isPoolMember);

            memberSubscriber = setupIndividualSubscriberAccount(ctx, 
                    SubscriberTypeEnum.PREPAID, 
                    newParentAccount, 
                    isResponsible, 
                    isPoolMember);
        }
        catch(IOException e)
        {
            String msg = "Failed setupPooledAccount due to: " + e.getMessage();
            logDebugMsg(ctx, msg, e);
            fail("Failed setup due to: " + e.getMessage());
        }
        String returnedBAN = getSelectedBAN(selectBAN, newParentAccount, newParentAccount, 
                memberSubscriber, memberSubscriber, memberSubscriber);
        return returnedBAN;
    }


    /**
     * Change state of the Subscription to ACTIVE
     * @param ctx
     * @param subId
     */
    private static void activateSubscriber(Context ctx, String subId) 
    {
        try
        {
            Subscriber sub = SubscriberSupport.getSubscriber(ctx, subId);
            sub.setState(SubscriberStateEnum.ACTIVE);
            sub.populateSubscriberServices(ctx, false);
            Home home = (Home) ctx.get(SubscriberHome.class);
            home.store(sub);
        }
        catch(Exception e)
        {
            String msg = "Failed to Activate Subscriber id=" + subId;
            logDebugMsg(ctx, msg, e);
            fail("Cannot continue with the test if the Subscriber setup is incorrect. " + msg);
        }
    }

    /**
     * Return the correct identifier based on the selection type.
     * @param banSelection
     * @param parentAccount (Group Account)
     * @param leaderAccount (Subscriber Account/Group Account)
     * @param memberAccount (Subscriber Account/Group Account)
     * @param rootChild    (Subscriber Account/Group Account)
     * @param leafSubscriber (Subscriber Account/Group Account)
     * @return
     */
    private static String getSelectedBAN(
            final ReturnIdentifier banSelection, 
            final String parentAccount, 
            final String leaderAccount, 
            final String memberAccount, 
            final String rootChild, 
            final String leafSubscriber) 
    {
        String returnedBAN = "";
        if (banSelection.equals(ReturnIdentifier.ROOT))
        {
            returnedBAN = parentAccount;
        }
        else if (banSelection.equals(ReturnIdentifier.LEADER))
        {
            returnedBAN = leaderAccount;
        }
        else if (banSelection.equals(ReturnIdentifier.MEMBER))
        {
            returnedBAN = memberAccount;
        }
        else if (banSelection.equals(ReturnIdentifier.ROOT_CHILD))
        {
            returnedBAN = rootChild;
        }
        else if (banSelection.equals(ReturnIdentifier.LEAF))
        {
            returnedBAN = leafSubscriber;
        }
        return returnedBAN;
    }

    /**
     * Get next MSISDN identifier and create a Package to use (identified by Msisdn ID). 
     * @param context
     * @return
     */
    private static long prepareNonPoolMsisdnAndSIMPackage(Context context) 
    {
        long msisdn = getTestMsisdn(context);
        TestSetupAccountHierarchy.addTestMsisdn(String.valueOf(msisdn));

        //Create SIM Package.
        createSIMPackage(context, String.valueOf(msisdn));
        return msisdn;
    }
    
    /**
     * Get next MSISDN identifier and create a Package to use (identified by Msisdn ID)
     * for a pool account.
     * @param context
     * @return
     */
    private static String preparePoolMsisdnAndSIMPackage(Context ctx) 
    {
        String msisdn = String.valueOf(getTestMsisdn(ctx));
        TestSetupMobileNumbers.createPoolMsisdn(ctx, msisdn);

        TestSetupAccountHierarchy.addTestMsisdn(msisdn);

        //Create SIM Package.
        createSIMPackage(ctx, msisdn);
        return msisdn;
    }

    /**
     * Create unit test GSM Package with given ID 
     * @param context
     * @param packageId
     */
    private static void createSIMPackage(Context context, String packageId) 
    {
        TestSetupPackage.createTestPackage(context, packageId);
    }

    /**
     * Retrieve next test MSISDN unique identifier.
     * @param context
     * @return
     */
    private static long getTestMsisdn(Context context) 
    {
        long msisdn = 0;
        try
        {
            msisdn = IdentifierSequenceSupportHelper.get(context).getNextIdentifier(context, UNIT_TEST_MSISDN, null);
        }
        catch (Exception e)
        {
            String msg = "Failed to get unique Test Msisdn. " + e.getMessage();
            logDebugMsg(context, msg, e);
            fail(msg);
        }
        return msisdn;
    }

    /**
     * Retrieve next test Account unique identifier.  Usually used as the LASTNAME field in the Account
     * profile.  NOT meant to be used as the Account.BAN.
     * @param context
     * @return
     */
    private static long getTestUniqueAccountIdentifier(Context context)
    {
        long identifier = 0;
        try
        {
            identifier = IdentifierSequenceSupportHelper.get(context).getNextIdentifier(context, UNIT_TEST_UNIQUE_ACCOUNT_ID, null);
        }
        catch (Exception e)
        {
            String msg = "Failed to get unique Test Account Identifier (to be used as last name in the profile). " + e.getMessage();
            logDebugMsg(context, msg, e);
            fail(msg);
        }
        return identifier;
    }

    /**
     * Parse for the identifier of the newly created bean.
     * @param resultString
     * @return
     */
    private static String parseCreateResult(String resultString) 
    {
        String startToken = "<center><b>Saved Entry: ";
        int startPos = resultString.indexOf(startToken) + startToken.length();
        int endPos = resultString.indexOf("<", startPos);
        return resultString.substring(startPos, endPos);
    }

    /**
     * Create an account using the given parameters.  Return the identifier of the account that was created.
     * Cannot create Group Accounts with this method since you can't set the Group MSISDN with this method.
     * @param context
     * @param parentBAN
     * @param isResponsible
     * @param accountType
     * @param paidType
     * @return
     * @throws IOException
     */
    private static String createAccount(Context context, String parentBAN,
            boolean isResponsible, long accountType, SubscriberTypeEnum paidType) 
    throws IOException
    {
        String url = getCreateNonPooledAccountUrl(context, parentBAN, isResponsible, accountType, paidType);

        LogSupport.debug(context, XTestSetupAccountHierarchy.class, "createAccount() URL to send: " + url);

        String result = com.redknee.framework.xhome.web.service.Servicer.service(url);

        LogSupport.debug(context, XTestSetupAccountHierarchy.class, "createAccount() Session result: " + result);

        return parseCreateResult(result);
    }

    /**
     * Send the URL that will create the Pooled Account. Parse the result and return the BAN of the newly created Account.
     * @param ctx
     * @param parentBAN
     * @param paidType
     * @param poolMsisdn
     * @return
     * @throws IOException
     */
    private static String createPooledAccount(Context ctx, String parentBAN,
            SubscriberTypeEnum paidType, String poolMsisdn) 
    throws IOException
    {
        //For now we are only testing Pooling Wallets.
        ArrayList<Long> poolingSubscriptionTypes = new ArrayList<Long>();
        poolingSubscriptionTypes.add(TestSetupAccountHierarchy.getSubscriptionTypeId(SubscriptionTypeEnum.MOBILE_WALLET_INDEX));
        long initialBal = 500L;
        
        String url = getCreatePooledAccountUrl(ctx, parentBAN, TestSetupAccountHierarchy.ACCOUNT_TYPE_POOLED, 
                paidType, poolMsisdn, poolingSubscriptionTypes, initialBal);

        LogSupport.debug(ctx, XTestSetupAccountHierarchy.class, "createPooledAccount() URL to send: " + url);

        String result = com.redknee.framework.xhome.web.service.Servicer.service(url);

        LogSupport.debug(ctx, XTestSetupAccountHierarchy.class, "createPooledAccount() Session result: " + result);

        return parseCreateResult(result);
    }

    /**
     * Create then activate the subscription (if it is required).
     * @param context
     * @param subscriberType
     * @param msisdn
     * @param msisdn2
     * @throws IOException
     */
    private static void createActiveAirtimeSubscription(Context context,
            SubscriberTypeEnum subscriberType, long msisdn, long msisdn2) 
        throws IOException
    {
        String subId = createAirtimeSubscription(context, subscriberType, msisdn, msisdn);
        TestSetupAccountHierarchy.addTestSubId(subId);
        if (subscriberType.equals(SubscriberTypeEnum.PREPAID))
        {
            //The subscription was saved in Available state, we must activate the Subscriber
            activateSubscriber(context, subId);
        }
    }

    /**
     * Create then activate the subscription (if it is required).
     * @param context
     * @param subscriberType
     * @param msisdn
     * @param msisdn2
     * @throws IOException
     */
    private static void createActiveWalletSubscription(Context context,
            SubscriberTypeEnum paidType, 
            long msisdn,
            long subscriptionClassId,
            long subscriptionTypeId,
            boolean isPooledMember)
        throws IOException
    {
        String subId = createWalletSubscription(context, SubscriberTypeEnum.PREPAID, msisdn,
                TestSetupAccountHierarchy.DEFAULT_WALLET_SUBSCRIPTION_CLASS,
                TestSetupAccountHierarchy.getSubscriptionTypeId(SubscriptionTypeEnum.MOBILE_WALLET_INDEX),
                isPooledMember);
        TestSetupAccountHierarchy.addTestSubId(subId);
        
        //The PREPAID subscription was saved in Available state, we must activate the Subscriber
        activateSubscriber(context, subId);
    }

    /**
     * Create an AIRTIME subscriber subscription with the given attributes.
     * The Msisdn provided is expected to be an "External" msisdn which will be created, claimed and assigned to the subscription by the 
     * subscriber pipeline. 
     * 
     * Some other fields we might want to start supporting:
     * Price Plan, Services, Start Dates, End Dates, support Msisdn (PPSM), bundles, auxiliary Services
     * @param context
     * @param ban
     * @param id
     * @param type
     * @param msisdn
     * @param state
     * @param startDate
     * @param subscriptionType
     * @return
     * @throws IOException
     */
    private static String createAirtimeSubscription(Context context, 
            SubscriberTypeEnum subscriberType, 
            long msisdn,
            long packageId)
    throws IOException
    {
        final RemoteApplication basApp = StorageSupportHelper.get(context).retrieveRemoteBASAppConfig(context);
        String today = urlDateFormat.format(new Date());
        // TODO 2010-10-01 DateFormat access needs synchronization
        String futureDate = urlDateFormat.format(CalendarSupportHelper.get(context).findDateYearsAfter(20,new Date()));
    
        StringBuilder url = new StringBuilder();
        url.append("http://");
        url.append(basApp.getHostname());
        url.append(":9260/AppCrm/home?username=rkadm&password=rkadm");
        url.append("&.auxiliaryServices.0.paymentNum=0&.bundles.0.paymentNum=0&.auxiliaryServices._count=2&1_RK_TAB_SD=1_0&PreviewButtonSrc=none&.secondaryPricePlanStartDate=");  
        url.append(futureDate);  
        url.append("&.bundles.0.endDate=");  
        url.append(futureDate);  
        url.append("&errors=show&.pointsBundles._REORDER_KEY=1&.secondaryPricePlan=-1&.startDate=&.initialExpDateExt=0&showPaneName=none&.secondaryPricePlanEndDate=");  
        url.append(futureDate);  
        url.append("&.subscriberCategory=0&.initialBalance=%24");
        url.append(subscriberType.equals(SubscriberTypeEnum.PREPAID) ? "1000.00" : "0.00");
        url.append("&.supportMSISDN=&.packageId=");
        url.append(packageId);
        url.append("&SaveCMD.y=8&SaveCMD.x=25&.auxiliaryServices.0.endDate=");  
        url.append(futureDate);  
        url.append("&.dealerCode=Dealer1&.chargePpsm.e=y&.auxiliaryServices._REORDER_KEY=1&cmd=SubMenuSubProfileEdit&.servicesForDisplay._REORDER_KEY=1&.bundles._REORDER_KEY=1&.MSISDN=");
        url.append(msisdn);
        url.append("&.marketingCampaignBean.MarketingId=0&.subscriptionClass=");
        url.append(TestSetupAccountHierarchy.DEFAULT_AIRTIME_SUBSCRIPTION_CLASS);
        url.append("&.lastSatId=-1&.technology=0&.subscriberType=");
        url.append(subscriberType.getIndex());
        url.append("&.subscriptionType=");
        long subscriptionType = TestSetupAccountHierarchy.getSubscriptionTypeId(SubscriptionTypeEnum.AIRTIME_INDEX);
        url.append(subscriptionType);
        url.append("&defaulttabs=+1_2&.pricePlan=");
        url.append(TestSetupPricePlanAndServices.getUnitTestPricePlanId(subscriptionType, subscriberType));
        url.append("&.endDate=&.bundles.1.startDate=");  
        url.append(today);  
        url.append("&.msisdnEntryType=0&.bundles.1.paymentNum=0&.bundles.0.startDate=");  
        url.append(today);  
        url.append("&.reactivationFee=%240.00&.auxiliaryServices.0.selectionIdentifier=1&.maxRecharge=%240.00&.pointsBundles._count=2&.marketingCampaignBean.endDate=");  
        url.append(today);  
        url.append("&.bundles._count=2&.auxiliaryServices.0.startDate=");  
        url.append(today);  
        url.append("&.maxBalance=%240.00&.marketingCampaignBean.startDate=");  
        url.append(today);  
        url.append("&.vraFraudProfile.e=y&.bundles.1.endDate=");  
        url.append(futureDate); 
        int numServicesToInstall = 2;
        url.append("&.servicesForDisplay._count=");
        url.append(numServicesToInstall);
        url.append(getSubscriberServiceUrl(0, TestSetupPricePlanAndServices.getUnitTestServiceId(subscriptionType, ServiceTypeEnum.VOICE), today, futureDate));
        url.append(getSubscriberServiceUrl(1, TestSetupPricePlanAndServices.getUnitTestServiceId(subscriptionType, ServiceTypeEnum.GENERIC), today, futureDate));
    
        LogSupport.debug(context, XTestSetupAccountHierarchy.class, "createAirtimeSubscription() URL to send: " + url.toString());
    
        String result = com.redknee.framework.xhome.web.service.Servicer.service(url.toString());
    
        LogSupport.debug(context, XTestSetupAccountHierarchy.class, "createAirtimeSubscription() Session result: " + result);
    
        return parseCreateResult(result);
    }
    
    /**
     * Create an WALLET subscriber subscription with the given attributes.
     * The MSISDN provided is expected to be an "External" MSISDN which will be created, claimed and assigned to the subscription by the 
     * subscriber pipeline. 
     * @param ctx
     * @param paidType
     * @param msisdn
     * @param subscriptionClass
     * @param subscriptionTypeId -- identifier of the subscription type (not the enum)
     * @param isPooledMember  - Pooled members should have 0 initial balance
     * @return
     * @throws IOException
     */
    private static String createWalletSubscription(Context ctx, 
            SubscriberTypeEnum paidType, 
            long msisdn,
            long subscriptionClassId,
            long subscriptionTypeId,
            boolean isPooledMember)
    throws IOException
    {
        final RemoteApplication basApp = StorageSupportHelper.get(ctx).retrieveRemoteBASAppConfig(ctx);
        // TODO 2010-10-01 DateFormat access needs synchronization
        String today = urlDateFormat.format(new Date());
        String futureDate = urlDateFormat.format(CalendarSupportHelper.get(ctx).findDateYearsAfter(20,new Date()));

        StringBuilder url = new StringBuilder();
        url.append("http://");
        url.append(basApp.getHostname());
        url.append(":9260/AppCrm/home?username=rkadm&password=rkadm");

        url.append("&cmd=SubMenuSubProfileEdit&1_RK_TAB_SD=1_0&PreviewButtonSrc=none&.secondaryPricePlanStartDate=03%2F02%2F2029&errors=show&.secondaryPricePlan=-1&.startDate=");
        url.append("&.initialExpDateExt=0&showPaneName=none&.secondaryPricePlanEndDate=03%2F02%2F2029&.dealerCode=Dealer1&.lastSatId=-1&.technology=0");
        url.append("&defaulttabs=+1_0&.endDate=&.msisdnEntryType=0&.reactivationFee=%240.00&.maxRecharge=%240.00&.maxBalance=%240.00");
        url.append("&SaveCMD.x=61&.vraFraudProfile.e=y&SaveCMD.y=13");
        url.append("&.subscriberType=");
        url.append(paidType.getIndex());
        url.append("&.subscriptionClass=");
        url.append(subscriptionClassId);
        url.append("&.subscriptionType=");
        url.append(subscriptionTypeId);
        url.append("&.MSISDN=");
        url.append(msisdn);
        url.append("&.pricePlan=");
        url.append(TestSetupPricePlanAndServices.getUnitTestPricePlanId(subscriptionTypeId, paidType));
        url.append("&.initialBalance=%24");
        if (isPooledMember)
        {
            url.append("0.00");
        }
        else
        {
            url.append("500.00");
        }
        url.append("&.servicesForDisplay._REORDER_KEY=1");
        int numServicesToInstall = 1;
        url.append("&.servicesForDisplay._count=");
        url.append(numServicesToInstall);
        url.append(getSubscriberServiceUrl(0, TestSetupPricePlanAndServices.getUnitTestServiceId(subscriptionTypeId, ServiceTypeEnum.GENERIC), today, futureDate));
        
        LogSupport.debug(ctx, XTestSetupAccountHierarchy.class, "createWalletSubscription() URL to send: " + url.toString());
        
        String result = com.redknee.framework.xhome.web.service.Servicer.service(url.toString());
    
        LogSupport.debug(ctx, XTestSetupAccountHierarchy.class, "createWalletSubscription() Session result: " + result);
    
        return parseCreateResult(result);
    }

    /**
     * Create a URL that fills in the basic CRM Account Fields for a CREATE command
     * @param context
     * @param parentBAN
     * @param isResponsible
     * @param accountType
     * @param paidType
     * @return
     */
    private static String getCreateBasicAccountUrl(Context context, String parentBAN,
            boolean isResponsible, long accountType, SubscriberTypeEnum paidType)  
    {
        final RemoteApplication basApp = StorageSupportHelper.get(context).retrieveRemoteBASAppConfig(context);
        final long lastName = getTestUniqueAccountIdentifier(context);
        
        StringBuilder url = new StringBuilder();
        url.append("http://");
        url.append(basApp.getHostname());
        url.append(":9260/AppCrm/home?username=rkadm&password=rkadm");
        url.append("&.creditCategory=0&.initials=&1_RK_TAB_SD=1_6&PreviewButtonSrc=none&errors=show&.firstName=Account+Move+Unit+Test&.billingAddress3=&.billingAddress2=");
        if (isResponsible)
        {
            url.append("&.responsible=on");
        }
        url.append("&.billingAddress1=123+Unit+Test+Way&.billingCountry=United+Nation&.contactTel=7896543&.identificationList.2.idType=-1&.accountMgr=&showPaneName=none&.systemType=");
        url.append(paidType.getIndex());
        url.append("&.securityQuestionsAndAnswers.3.answer=&.identificationList.1._enabled=X&.responsible.e=y&.identificationList.1.idType=1&.accountName=&.billCycleID=991&.language=en&.lastName=");
        url.append(lastName);
        url.append("&.spid=1&.dealerCode=Dealer1&cmd=AcctSubSubMenus&.paymentMethodType=1&.securityQuestionsAndAnswers.2._enabled=X&.billingCity=Testland&.identificationList._count=3&.greeting=&.employerAddress=&.securityQuestionsAndAnswers.1.answer=no&.bankAccountName=&.identificationList.0.idType=0&.securityQuestionsAndAnswers.2.answer=maybe&.contactFax=&.contactName=Grover&.lastActId=-1&defaulttabs=+1_6&.creditCardPayment.e=y&.securityQuestionsAndAnswers._REORDER_KEY=1&.dateOfBirth=&.type=");
        url.append(accountType);
        url.append("&.billingMsgPreference=0&.role=");
        url.append(TestSetupAccountHierarchy.SUBSCRIBER_ACCOUNT_ROLE);
        url.append("&.bankPhone=&.identificationList.2.idNumber=&.securityQuestionsAndAnswers.2.question=maybe&.identificationList.1.idNumber=2222222&.securityQuestionsAndAnswers._count=4&.identificationList.0._enabled=X&.bankAddress2=&.securityQuestionsAndAnswers.3.question=&.bankAddress1=&.securityQuestionsAndAnswers.0.question=yes&.securityQuestionsAndAnswers.1._enabled=X&.state=0&.securityQuestionsAndAnswers.0.answer=yes&.bankName=&.bankAccountNumber=&.securityQuestionsAndAnswers.1.question=no&SaveCMD.x=37&.emailID=&.employer=&.securityQuestionsAndAnswers.0._enabled=X&.identificationList._REORDER_KEY=1&.billingProvince=Ontario&.parentBAN=");
        url.append(parentBAN);
        url.append("&SaveCMD.y=15&.identificationList.0.idNumber=1111111");
        
        return url.toString();
    }

    /**
     * Create a URL that creates a non-pooled Account
     * @param ctx
     * @param parentBAN
     * @param isResponsible
     * @param accountType
     * @param paidType
     * @return
     */
    private static String getCreateNonPooledAccountUrl(Context ctx,
            String parentBAN, boolean isResponsible, long accountType,
            SubscriberTypeEnum paidType) 
    {
        StringBuilder url = new StringBuilder();
        url.append(getCreateBasicAccountUrl(ctx, parentBAN, isResponsible, accountType, paidType));
        //Append empty Account Extensions
        url.append("&.accountExtensions.1.value.class=&.accountExtensions._count=1");
        return url.toString();
    }

    /**
     * Create a URL that creates a Pooled Account
     * @param ctx
     * @param parentBAN
     * @param accountType
     * @param paidType
     * @param poolMsisdn
     * @param poolingSubscriptionTypes
     * @param initialPoolBal
     * @return
     */
    private static String getCreatePooledAccountUrl(Context ctx,
            final String parentBAN, 
            final long accountType,
            SubscriberTypeEnum paidType,
            String poolMsisdn,
            final Collection<Long> poolingSubscriptionTypes,
            final long initialPoolBal) 
    {
        StringBuilder url = new StringBuilder();
        final boolean isResponsible = true; 
        url.append(getCreateBasicAccountUrl(ctx, parentBAN, isResponsible, accountType, paidType));
        //Add the Pool Extension
        url.append("&.accountExtensions._count=2");
        url.append("&.accountExtensions.1.value.class=Pool");
        url.append("&.accountExtensions.1.createMode=on");
        url.append("&.accountExtensions.1.value.obj.poolMSISDN.start=1");
        url.append("&.accountExtensions.1._enabled=X");
        url.append("&.accountExtensions.1.createMode.e=y");
        url.append("&.accountExtensions.1.value.obj.subscriptionPoolProperties._REORDER_KEY=0");
        int numberOfPools = 1 + poolingSubscriptionTypes.size();
        url.append("&.accountExtensions.1.value.obj.subscriptionPoolProperties._count=");
        url.append(numberOfPools);
        url.append("&.accountExtensions.1.value.obj.subscriptionPoolProperties.addRowCount=1");
        url.append("&.accountExtensions.1.value.obj.subscriptionPoolProperties.-1.subscriptionType=1");
        url.append("&.accountExtensions.1.value.obj.subscriptionPoolProperties.-1.initialPoolBalance=0");
        url.append("&.accountExtensions.1.value.obj.subscriptionPoolProperties.-1.provisioned=0");
        for(Long subscriptionType: poolingSubscriptionTypes)
        {
            int index = 0;
            url.append("&.accountExtensions.1.value.obj.subscriptionPoolProperties." + index + ".provisioned=0");
            url.append("&.accountExtensions.1.value.obj.subscriptionPoolProperties." + index + ".subscriptionType=");
            url.append(subscriptionType);
            url.append("&.accountExtensions.1.value.obj.subscriptionPoolProperties." + index + ".initialPoolBalance=");
            url.append(initialPoolBal);
            index++;
        }
        url.append("&.accountExtensions.1.value.obj.poolMSISDN.Msisdn=");
        url.append(poolMsisdn);
        url.append("&.accountExtensions.1.value.obj.poolMSISDN.MsisdnGroup=");
        url.append(TestSetupMobileNumbers.DEFAULT_POOLED_MSISDN_GROUP);
        url.append("&.accountExtensions.2.createMode.e=y");
        url.append("&.accountExtensions.2.value.class=");
        url.append("&.accountExtensions.2.createMode=on");

        return url.toString();
    }

    private static String getSubscriberServiceUrl(int i, long unitTestServiceId, String today, String futureDate) 
    {
        StringBuilder url = new StringBuilder();
        String label = "&.servicesForDisplay." + i;
        url.append(label + ".serviceId=");
        url.append(unitTestServiceId);
        url.append(label + ".startDate=" + today); 
        url.append(label + ".endDate=" + futureDate);
        url.append(label + "._enabled=X");
        return url.toString();
    }

    /**
     * Log Debug message about the exception that occurred.
     * @param ctx
     * @param msg
     * @param e
     */
    private static void logDebugMsg(Context ctx, String msg, Throwable e) 
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(XTestSetupAccountHierarchy.class, msg, e).log(ctx);
        }
    }


    public void testSetup()
    {
        testSetup(getContext());
    }

    public static void testSetup(Context context) 
    {
        if (!UnitTestSupport.isTestRunningInXTest(context))
        {
            fail("This test is meant to run as an XTest on a live system.  Running it off-line is invalid.");
        }
        TestSetupAccountHierarchy.testSetup(context);
        TestSetupPricePlanAndServices.testSetup(context);
    }

    public final static IdentifierEnum UNIT_TEST_MSISDN = new IdentifierEnum(999, "UNIT_TEST_MSISDN_ID", "Unit test msisdn");
    public final static IdentifierEnum UNIT_TEST_UNIQUE_ACCOUNT_ID = new IdentifierEnum(998, "UNIT_TEST_UNIQUE_ACCOUNT_ID", "unit test account");
    private static final SimpleDateFormat urlDateFormat = new SimpleDateFormat("MM'%2F'dd'%2F'yyyy");

    /**
     * Enumeration used to select which identifier to return.
     */
    public static enum ReturnIdentifier
    { 
        ROOT(0),    // Return the Identifier of the Root Account 
        LEADER(1),  // Return the Identifier of the Leader Subscriber Account (used in a Pooled Account)
        MEMBER(2),  // Return the Identifier of the Member Subscriber Account (used in a Pooled Account)
        ROOT_CHILD(3),  // Return the Identifier of a immediate Child of the Root Account
        LEAF(4);  // Return the Identifier of a leaf Subscriber Account (used in a Account Hierarchy).

        int index;
        ReturnIdentifier(int i)
        {
            index = i;
        }

        int getIndex()
        {
            return index;
        }
    }
}
