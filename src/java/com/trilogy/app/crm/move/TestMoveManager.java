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
package com.trilogy.app.crm.move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionTestClient;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.CompoundMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CRMChargingCycleSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.transaction.TransactionReceiveDateComparator;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.app.crm.unit_test.TestSetupIdentifierSequence;
import com.trilogy.app.crm.unit_test.TestSetupInvoiceCalculation;
import com.trilogy.app.crm.unit_test.TestSetupPricePlanAndServices;
import com.trilogy.app.crm.unit_test.TestSetupSubscriberServices;
import com.trilogy.app.crm.unit_test.UnitTestSupport;

/**
 * Unit Test for Account and Subscription Move.
 * 
 * We'll break down the unit tests in the following way:
 * 1) Test Setup Validation
 * 2) Test Move Manager Validation
 * 3) Test Move Manager Successful Move cases
 * 4) Test Move Manager with Failing Move Cases and Rollback
 * 
 * 
 * Things to verify after a move has been completed:
 * a) Account Hierarchy
 * b) MSISDN Management History records 
 * c) Transactions - Deposit Releases, new Charges for Subscriptions, refunds for subscriptions
 * d) Subscriber Services - check that all services have been moved to the new subscriber
 * e) Subscription Expiry Extension if it is a Prepaid Subscriber being moved out of a Pooled account.
 * f) Suspended entities must move as well
 * 
 * In particular for Money Wallets:
 * - wallets don't expire, so no expiry extension
 * - wallets are prepaid, so no deposit
 * - wallets do have services and aux services but no provisioning related to them (i.e. service fees only)
 * - wallets do not have bundles
 * - pooling is going to be supported for wallets
 * - suspended entities have to be moved for wallets also
 * 
 * 
 * @author angie.li@redknee.com
 * @since 8.1
 *
 */
public class TestMoveManager extends ContextAwareTestCase 
{
    public TestMoveManager(String name)
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
        final TestSuite suite = new TestSuite(TestMoveManager.class);
        return suite;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
     */
    @Override
    protected void setUp() 
    {
        super.setUp();
        
        //Setup all Licenses
        getContext().put(PaymentPlanSupport.DISABLE_LICENSE_KEY, Boolean.TRUE);
        
        //Setup Homes
        //Install only if running off-line
        if (!UnitTestSupport.isTestRunningInXTest(getContext()))
        {
            TestSetupIdentifierSequence.setup(getContext());
            TestSetupInvoiceCalculation.setup(getContext());
            // Install a BMGT test client in the context.
            getContext().put(UrcsClientInstall.SUBSCRIBER_PROFILE_PROVISION_CLIENT_KEY, new MoveSubscriberProfileProvisionTestClient());
        }

        TestSetupPricePlanAndServices.setup(getContext());
        TestSetupAccountHierarchy.setup(getContext(), false);
        
        //Install only if running off-line
        if (!UnitTestSupport.isTestRunningInXTest(getContext()))
        {
            Home cacheAcctHome = (Home) getContext().get(Common.ACCOUNT_CACHED_HOME, getContext().get(AccountHome.class));
            /*
             * For this Unit test, we will register an extra home decorator to the AccountHome 
             * pipeline to keep track of the identifiers of the newly created Accounts.
             * This will help us in the verification process.
             */
            cacheAcctHome = new HomeProxy(cacheAcctHome)
            {
                @Override
                public Object create(Context ctx, Object obj)
                throws HomeException
                {
                    Account createdAccount = (Account) getDelegate().create(ctx, obj);
                    IdentifierTracker.getInstance().logNew(createdAccount.getBAN());
                    return createdAccount;
                }
            };
            getContext().put(Common.ACCOUNT_CACHED_HOME, cacheAcctHome);

            //Provision Subscribers with default Services for unit test
            TestSetupSubscriberServices.setup(getContext(), false);
        }
        else
        {
            //Running offline
            //Provision Subscribers with default Services for unit test
            try
            {
                TestSetupSubscriberServices.provisionSubscriberServices(getContext());
            }
            catch (Exception e)
            {
                new DebugLogMsg(TestMoveManager.class, "Error while setting up default SubscriberServices. " + e.getMessage(), e).log(getContext());
            }
        }
        
        //Set the Test start time during each setup so that we might count the transactions properly.
        TEST_START_TIME = new Date();
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#tearDown()
     */
    @Override
    protected void tearDown() 
    {
        super.tearDown();
    }

    /**
     * Used to track the identifiers of newly created accounts.
     */
    private static class IdentifierTracker
    {
        private static IdentifierTracker instance_ = null;
        //List of account BAN of accounts created by the Move process.
        ArrayList<String> newIdentifiers = null;
        
        private IdentifierTracker()
        {
            if (newIdentifiers == null)
            {
                newIdentifiers = new ArrayList<String>();
            }
        }
        
        static IdentifierTracker getInstance()
        {
            if (instance_ == null)
            {
                instance_ = new IdentifierTracker();
            }
            return instance_;
        }
        
        void logNew(String identifier)
        {
            newIdentifiers.add(identifier);
        }
        
        ArrayList<String> getList()
        {
            return newIdentifiers;
        }
    }

    /**
     * Return the Individual Account Move Request
     * @param accountToMove  identifier of the account to move
     * @param newParentBAN  identifier of the destined parent account for accountToMove 
     * @return
     */
    protected static AccountMoveRequest getAccountMoveRequest(String accountToMove, String newParentBAN)
    {
        AccountMoveRequest request = new AccountMoveRequest();
        request.setExistingBAN(accountToMove);
        request.setNewParentBAN(newParentBAN);
        return request;
    }
    
    protected SubscriptionMoveRequest getSubscriptionMoveRequest(String oldSubId, String newParentBAN)
    {
        SubscriptionMoveRequest request = new SubscriptionMoveRequest();
        request.setOldSubscriptionId(oldSubId);
        request.setNewBAN(newParentBAN);
        return request;
    }
    
    /**
     * Return the most updated Msisdn Management History Record for the given Mobile Number 
     * and Subscription Type
     * @param ctx
     * @param msisdn
     * @param subscriptionType
     * @return
     * @throws HomeException
     */
    protected static MsisdnMgmtHistory findLatestMsisdnHistory(Context ctx, String msisdn, long subscriptionType)
        throws HomeException
    {
        final Predicate filter = new And()
            .add(new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn))
            .add(new EQ(MsisdnMgmtHistoryXInfo.SUBSCRIPTION_TYPE, subscriptionType))
            .add(new EQ(MsisdnMgmtHistoryXInfo.LATEST, Boolean.TRUE));

        final Home historyHome = (Home) ctx.get(MsisdnMgmtHistoryHome.class);
        MsisdnMgmtHistory latestHistory = (MsisdnMgmtHistory)historyHome.find(ctx, filter); 
        return latestHistory;
    }
    
    /**
     * Return the collection of Transactions created during the test execution for 
     * the given Subscription.
     * @param ctx
     * @param subscriberID  - subscription identifier.
     * @param predicate
     * @return
     * @throws HomeException
     */
    protected static Collection<Transaction> findNewlyCreatedTransactions(Context ctx, 
            final String subscriberID, 
            final Predicate predicate)
            throws HomeException
    {
        final Home transactionHome = new SortingHome(ctx, (Home) ctx.get(TransactionHome.class),
                new TransactionReceiveDateComparator(false));

        final And where = new And();
        where.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subscriberID));
        if (predicate != null)
        {
            where.add(predicate);
        }

        final Collection<Transaction> transactions;
        transactions = transactionHome.select(ctx, where);
        return transactions;
    }
    
    /**
     * Retrieve all Subscribers by BAN and index them by SubscriptionType.
     * @param ctx
     * @param BAN
     * @return
     * @throws HomeException
     */
    protected Map<SubscriptionType, Subscriber> getCurrentSubscriptions(Context ctx, String BAN)
        throws HomeException
    {
        Map<SubscriptionType, Subscriber> map = new HashMap<SubscriptionType, Subscriber>();
        Collection<Subscriber> subscribers = AccountSupport.getImmediateChildrenSubscribers(ctx, BAN);
        for(Subscriber sub : subscribers)
        {
            SubscriptionType subscriptionType = sub.getSubscriptionType(ctx);
            if (subscriptionType == null)
            {   
                throw new HomeException("Subscription Type " + sub.getSubscriptionType() + " does not exist.");
            }
            map.put(subscriptionType, sub);
        }
        return map;
    }
    
    /**
     * Return the newly created account.
     * 
     * TODO: BUT, suppose an Group Account move to a new Group Account umbrella is performed;
     * this means that Subscriber accounts will be moved and generated.
     * Test case fails to catch that.
     * 
     * @param ctx
     * @param originalAccountBAN
     * @return
     */
    protected Account getNewMovedAccount(Context ctx, String originalAccountBAN) 
        throws HomeException
    {
        ArrayList<String> newBANs = IdentifierTracker.getInstance().getList();
        if (newBANs.size() == 1)
        {
            return AccountSupport.getAccount(ctx, newBANs.get(0));
        }
        else if (newBANs.size() == 0)
        {
            return AccountSupport.getAccount(ctx, originalAccountBAN);
        }
        return null;
    }

    /**
     * Executes the move for the given account to the given destination account.
     * @param ctx
     * @param movingAccount
     * @param destinationAccount
     */
    protected static AccountMoveRequest executeMove(Context ctx, 
            final String movingAccount,
            final String destinationAccount) 
        throws MoveException
    {
        //Create Move Request
        AccountMoveRequest request = getAccountMoveRequest(movingAccount, destinationAccount);
        
        //Execute Account Move
        MoveManager manager = new MoveManager();
        manager.validate(ctx, request);
        return (AccountMoveRequest)manager.move(ctx, request);
    }

    /**
     * Checks to see if the number of expected subscribers were created.
     * @param number
     */
    protected void verifyNumberOfNewAccountsCreated(int number) 
    {
        assertTrue(IdentifierTracker.getInstance().getList().size() == number);
    }
    
    protected static void verifyAllElementsMoved(
            Context ctx,
            final CompoundMoveRequest processedRequest,
            CollectionOfComparableEntities originalEntities) 
    {
        try
        {
            Iterator processedMoves = processedRequest.getRequests().iterator();
            while (processedMoves.hasNext())
            {
                MoveRequest requestToVerify = (MoveRequest) processedMoves.next();

                if (requestToVerify instanceof AccountMoveRequest)
                {
                    verifyAccountMove(ctx, (AccountMoveRequest)requestToVerify, originalEntities);
                }
                else if(requestToVerify instanceof SubscriptionMoveRequest)
                {
                    verifySubscriptionMove(ctx, (SubscriptionMoveRequest)requestToVerify, originalEntities);
                }
            }
        }
        catch (HomeException e)
        {
            fail("Failed test case, verifyAllElementsMoved has failed with errors. " + e.getMessage());
        }
    }

    /**
     * Verify all details of the moved account.
     * @param ctx
     * @param newAccountId - the identifier of the new Account 
     * @param destinationParentAccountId -  identifier of the Account under which the newAccountId has moved.
     * @param previousSubscribers - collection of Subscribers of newAccountId prior to the move
     * @throws HomeException
     */
    private static void verifyAccountMove(Context ctx, 
            final AccountMoveRequest requestToVerify,
            final CollectionOfComparableEntities originalEntities) 
        throws HomeException
    {
        String newAccountBAN = requestToVerify.getNewBAN();
        //Get the same account before the move.
        EQ banPredicate = new EQ(AccountXInfo.BAN, requestToVerify.getExistingBAN()); 
        Account originalAccount = CollectionSupportHelper.get(ctx).findFirst(ctx, originalEntities.getOriginalAccounts(), banPredicate);

        if(isCandidateForMove(originalAccount))
        {
            //Verify basic Responsible Account move logic
            if (originalAccount.isResponsible())
            {
                assertEquals("Since the old account was responsible no new Account (nor BAN) should be assigned.", 
                        originalAccount.getBAN(), newAccountBAN);
            }

            //Retrieve the newly Moved Account and verify basic properties.
            Account newlyMovedAccount = verifyAccountProperties(ctx, newAccountBAN, 
                    originalAccount.isIndividual(ctx), 
                    originalAccount.isPooled(ctx), 
                    originalAccount.isResponsible(), false);

            //Verify Persistent account Data
            assertTrue("Moved Account Data doesn't match. ", compareMovedAccount(ctx, originalAccount, newlyMovedAccount));

            //TODO: Verify anything else?  New Notes?
        }
    }

    /**
     * Returns TRUE if the given account is allowed to move
     * @param originalAccount
     * @return
     */
    protected static boolean isCandidateForMove(Account originalAccount) 
    {
        // Deactivated Accounts are not to be moved.
        return !originalAccount.getState().equals(AccountStateEnum.INACTIVE);
    }

    /**
     * Verify that all the relevant Subscription data has been moved:
     * Subscription beans, new Transactions, SubscriberServices, SubspendedEntities, Bundles. 
     * 
     * @param ctx
     * @param previousSubscribers
     * @param sub
     * @throws HomeException
     */
    private static void verifySubscriptionMove(Context ctx,
            final SubscriptionMoveRequest requestToVerify,
            final CollectionOfComparableEntities originalEntities) 
        throws HomeException
    {
        String newSubscriptionID = requestToVerify.getNewSubscriptionId();
        //Get the same subscription before the move.
        Subscriber originalSubscription = originalEntities.getOriginalSubscriber(ctx, requestToVerify.getOldSubscriptionId());
        
        if (isCandidateForMove(originalSubscription))
        {
            //Verify all Subscription Data was copied correctly.
            Subscriber newlyMovedSubRecord = SubscriberSupport.getSubscriber(ctx, newSubscriptionID);
            assertTrue("Moved Subscriber Data doesn't match. Old Sub ID = " + originalSubscription + 
                    ", New Sub ID=" + newSubscriptionID, 
                    compareSubscriberDataEquals(ctx, originalSubscription, newlyMovedSubRecord,
                            originalSubscription.getAccount(ctx).isResponsible()));

            //Assert MSISDN Management History has been updated with the latest BAN
            MsisdnMgmtHistory lastRecord = findLatestMsisdnHistory(ctx, newlyMovedSubRecord.getMSISDN(), 
                    newlyMovedSubRecord.getSubscriptionType());
            assertEquals(newSubscriptionID, lastRecord.getSubscriberId());

            //Verify SubscriberServices are correctly assigned.
            verifySubscriberServicesForNewAccount(ctx, newlyMovedSubRecord, 
                    originalEntities.getOriginalSubscriberServicesForSub(ctx, originalSubscription.getId()));
            
            //Verify Suspended Entities have moved as well

            //Verify Bundles

            //Assert new Transactions are correct.  There must be charges for All Services plus a balance carry over check 
            verifyTransactionsForMovedAccounts(ctx, newlyMovedSubRecord, originalSubscription, originalEntities);


            //Verify Subscriber Expiry
        }

    }


    /**
     * Compare all SubscriberServices records for the given Subscriber.
     * The reason that we are gathering all the SubscriberServices and not just the Provisioned
     * services, is that CRM must make sure the Subscriber "copy" (move) is exactly
     * as it was before the move.  That means including suspended or not yet provisioned
     * Services.
     * @param ctx
     * @param originalSubscribers_
     * @return
     */
    private static void verifySubscriberServicesForNewAccount(Context ctx,
            final Subscriber newlyMovedSubRecord,
            final Collection<SubscriberServices> originalSubscriberServices) 
    {
        try
        {
            Predicate predicate = new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, newlyMovedSubRecord.getId());
            Home home = (Home) ctx.get(SubscriberServicesHome.class);
            Collection<SubscriberServices> newSubscriberServicesRecords = home.where(ctx, predicate).selectAll();
            //Gather the Subscriber services for this particular subscriber.
            
            assertEquals("Number of Subscriber Services Records differs between old and new Subscriptions.", 
                    newSubscriberServicesRecords.size(), originalSubscriberServices.size());
            //Iterate through the collection of original Subscriber services and ensure that there is one in the new Collection.
            for (SubscriberServices original : originalSubscriberServices)
            {
                Predicate thisService = new EQ(SubscriberServicesXInfo.SERVICE_ID, original.getServiceId());  
                SubscriberServices newRecord = CollectionSupportHelper.get(ctx).findFirst(ctx, newSubscriberServicesRecords, thisService);
                assertNotNull("Missed moving SubscriberService record to new Subscription:" + original.toString(), newRecord);
                assertEquals("Subscriber Services start dates don't match.", original.getStartDate(), newRecord.getStartDate());
                assertEquals("Subscriber Services end dates don't match.", original.getEndDate(), newRecord.getEndDate());
                /* We are taking a chance by comparing the State of the subscriber services, since it is a live test and external applications
                 * may not be connected.  Still we have to assume the setup is flawless.  A failure to match in this case could require
                 * setup to be done and the test to be restarted.  Try not to setup tests that purposely exercise provisioning a Service
                 * that failed to be provisioned.
                */
                assertEquals("State of the subscriber services do not match. ", original.getProvisionedState(), newRecord.getProvisionedState());
            }
        }
        catch(HomeException e)
        {
            fail("Error while verifying Move of Subscriber Services. " + e.getMessage());
        }
    }

    /**
     * Returns true if the given Subscriber is allowed to Move.
     * @param originalSubscription
     * @return
     */
    protected static boolean isCandidateForMove(Subscriber originalSubscription) 
    {
        //Subscriber Move is not performed for deactivated Subscriptions
        return !originalSubscription.getState().equals(SubscriberStateEnum.INACTIVE);
    }

    private static void verifyTransactionsForMovedAccounts(Context ctx,
            final Subscriber newSub,
            final Subscriber originalSub,
            final CollectionOfComparableEntities originalEntities)
    throws HomeException
    {
        Collection<Transaction> col = findNewlyCreatedTransactions(ctx, newSub.getId(), null);
        if (newSub.getAccount(ctx).isResponsible())
        {
            assertTrue("No new Transaction were to be created for this move. ", col.size() == 0);
        }
        else
        {
            if(spidHasCarryOverBalance(ctx, newSub.getSpid()))
            {
                //Only one balance carry over transaction will be generated to debit and credit the subscribers
                verifyCarryOverBalance(ctx, newSub, originalSub, originalEntities.getOutstandingBalance(ctx, originalSub.getId()));
            }
            else
            {
                /* The prorated service fees will be refunded to the old subscription and
                 * the prorated service fees will be charged to the new subscription.
                 */
                verifyProratedChargeAndRefund(ctx, newSub, originalSub);
            }
        }
    }

    private static void verifyProratedChargeAndRefund(
            Context ctx,
            final Subscriber newSub, 
            final Subscriber originalSub) 
        throws HomeException
    {
        //Get the ratio for Monthly proration. For Unit tests we assume Monthly billed Services 
        final double ratio = CRMChargingCycleSupport.instance().getHandler(ChargingCycleEnum.MONTHLY).calculateRate(ctx, new Date(), SubscriberSupport.getBillCycleDay(ctx,
                originalSub), -1 //Invalid spid 
                );
        
        //For each service Fee verify that the refund was made in the old subscriber
        //For each service Fee verify that the charge was made in the new subscriber
    }

    private static void verifyCarryOverBalance(Context ctx, 
            final Subscriber newSub,
            final Subscriber originalSub,
            final long originalOutstandingBalance) 
    {
        //Check Credit Transaction to the original Subscriber
        verifyTransactionAmount(ctx, originalSub.getId(), AdjustmentTypeEnum.SubscriberTransferCredit_INDEX, 
                originalOutstandingBalance);
        //Check Debit Transaction to the new Subscriber
        verifyTransactionAmount(ctx, newSub.getId(), AdjustmentTypeEnum.SubscriberTransferDebit_INDEX, originalOutstandingBalance);
    }
    
    /**
     * Checks the transaction of the correct amount is in the system.
     * @param ctx
     * @param subId
     * @param type
     * @param amount
     */
    private static void verifyTransactionAmount(Context ctx, 
            final String subId,
            final int type, 
            final long amount)
    {
        Collection<Transaction> debitTrans = CoreTransactionSupportHelper.get(ctx).getTransactionsForSubAdjustment(ctx,
                subId, type, null);
        assertEquals("There is more than Transaction for this Adjustment Type=" + type, 
                1, debitTrans.size());
        for (Transaction trans: debitTrans)
        {
            assertEquals("Amount of transaction is incorrect.", amount, trans.getAmount());
        }
    }

    /**
     * Compares Account Data with the exception of Account BAN (Identifier) and Parent BAN which should be 
     * compared outside of this method. And LastModified will not be the same if a new copy of the Account is
     * created, so naturally it is not compared.
     * The method was copied from AbstractAccount.persistentEquals.
     * @return TRUE if the account data matches.
     */
    private static boolean compareMovedAccount(final Context ctx,
            final Account previousAccount, 
            final Account other) 
        throws HomeException
    {
    
        if ( previousAccount == null || other == null)
        {
            throw new HomeException("Null Accounts cannot be compared");
        }
    
        boolean result = true;
        
        result &= SafetyUtil.safeEquals(previousAccount.getAccountName(), other.getAccountName());
        result &= ( previousAccount.getSpid() == other.getSpid() );
        result &= ( previousAccount.getRole() == other.getRole() );
        result &= ( previousAccount.getType() == other.getType() );
        result &= ( previousAccount.getSystemType() == other.getSystemType() );                      
        result &= ( previousAccount.getState() == other.getState() );
        result &= SafetyUtil.safeEquals(previousAccount.getPromiseToPayDate(), other.getPromiseToPayDate());
        result &= SafetyUtil.safeEquals(previousAccount.getPromiseToPayStartDate(), other.getPromiseToPayStartDate());
        result &= ( previousAccount.getCurrentNumPTPTransitions() == other.getCurrentNumPTPTransitions() );
        result &= ( previousAccount.getPtpTermsTightened() == other.getPtpTermsTightened() );
        result &= SafetyUtil.safeEquals(previousAccount.getFirstName(), other.getFirstName());
        result &= SafetyUtil.safeEquals(previousAccount.getLastName(), other.getLastName());
        result &= SafetyUtil.safeEquals(previousAccount.getAccountMgr(), other.getAccountMgr());
        result &= SafetyUtil.safeEquals(previousAccount.getDebtCollectionAgencyId(), other.getDebtCollectionAgencyId());
        result &= ( previousAccount.getResponsible() == other.getResponsible() );
        result &= ( previousAccount.getVpn() == other.getVpn() );
        result &= ( previousAccount.getIcm() == other.getIcm() );
        result &= SafetyUtil.safeEquals(previousAccount.getGreeting(), other.getGreeting());
        result &= ( previousAccount.getCreditCategory() == other.getCreditCategory() );
        result &= ( previousAccount.getOriginalCreditCategory() == other.getOriginalCreditCategory() );
        result &= SafetyUtil.safeEquals(previousAccount.getDealerCode(), other.getDealerCode());
        result &= ( previousAccount.getDiscountClass() == other.getDiscountClass() );
        result &= ( previousAccount.getTaxAuthority() == other.getTaxAuthority() );
        result &= ( previousAccount.getTaxExemption() == other.getTaxExemption() );
        result &= SafetyUtil.safeEquals(previousAccount.getLanguage(), other.getLanguage());
        result &= SafetyUtil.safeEquals(previousAccount.getCurrency(), other.getCurrency());
        result &= ( previousAccount.getBillCycleID() == other.getBillCycleID() );
        result &= ( previousAccount.getPaymentPlan() == other.getPaymentPlan() );
        result &= ( previousAccount.getPaymentPlanAmount() == other.getPaymentPlanAmount() );
        result &= SafetyUtil.safeEquals(previousAccount.getPaymentPlanStartDate(), other.getPaymentPlanStartDate());
        result &= ( previousAccount.getPaymentPlanCurrCount() == other.getPaymentPlanCurrCount() );
        result &= ( previousAccount.getPaymentPlanInstallmentsCharged() == other.getPaymentPlanInstallmentsCharged() );
        result &= ( previousAccount.getPaymentPlanMonthlyAmount() == other.getPaymentPlanMonthlyAmount() );
        result &= ( previousAccount.getBillingMsgPreference() == other.getBillingMsgPreference() );
        result &= SafetyUtil.safeEquals(previousAccount.getBillingMessage(), other.getBillingMessage());
        result &= ( previousAccount.getPaymentMethodType() == other.getPaymentMethodType() );
        result &= SafetyUtil.safeEquals(previousAccount.getPaymentMethod(), other.getPaymentMethod());
        result &= SafetyUtil.safeEquals(previousAccount.getCreditCardNumber(), other.getCreditCardNumber());
        result &= SafetyUtil.safeEquals(previousAccount.getExpiryDate(), other.getExpiryDate());
        result &= SafetyUtil.safeEquals(previousAccount.getHolderName(), other.getHolderName());
        result &= SafetyUtil.safeEquals(previousAccount.getDebitBankTransit(), other.getDebitBankTransit());
        result &= SafetyUtil.safeEquals(previousAccount.getDebitAccountNumber(), other.getDebitAccountNumber());
        result &= SafetyUtil.safeEquals(previousAccount.getBillingAddress1(), other.getBillingAddress1());
        result &= ( previousAccount.getCategory() == other.getCategory() );
        result &= SafetyUtil.safeEquals(previousAccount.getContactName(), other.getContactName());
        result &= SafetyUtil.safeEquals(previousAccount.getProfileAttachmentKey(), other.getProfileAttachmentKey());
        result &= SafetyUtil.safeEquals(previousAccount.getCompanyName(), other.getCompanyName());
        result &= SafetyUtil.safeEquals(previousAccount.getInCollectionDate(), other.getInCollectionDate());
        result &= SafetyUtil.safeEquals(previousAccount.getBankID(), other.getBankID());
        result &= ( previousAccount.getContract() == other.getContract() );
        result &= SafetyUtil.safeEquals(previousAccount.getContractStartDate(), other.getContractStartDate());
        result &= SafetyUtil.safeEquals(previousAccount.getContractEndDate(), other.getContractEndDate());
        result &= ( previousAccount.getUseIfNoSubCreditInfo() == other.getUseIfNoSubCreditInfo() );
        
        // TODO: Perform a separate verification of all Pooled fields (at the subscriber level?)
        
        // Validate transient fields that we are depending on an adapter to populate for us.
        result &= contactInfoEntriesEqual(ctx, previousAccount, other);
        result &= securityQuestionsEqual(previousAccount, other);
        result &= identificationEntriesEqual(previousAccount, other);
        result &= SafetyUtil.safeEquals(previousAccount.getCreditCardInfo(), other.getCreditCardInfo());
        
        if (previousAccount.isResponsible()
                && !previousAccount.isMom(ctx))
        {
            // These fields should only be the same when moving responsible, non-VPN accounts
            result &= previousAccount.getNextSubscriberId() == other.getNextSubscriberId();
        }
        
        return result;
    }

    private static boolean contactInfoEntriesEqual(Context ctx, Account previousAccount, Account other)
    {
        boolean result = false;
        
        final Home home = (Home) ctx.get(ContactHome.class);
        if (home != null)
        {
            try
            {
                final Collection<Contact> oldContacts = home.select(ctx, new EQ(ContactXInfo.ACCOUNT, previousAccount.getBAN()));
                final Collection<Contact> newContacts = home.select(ctx, new EQ(ContactXInfo.ACCOUNT, other.getBAN()));
                result = (oldContacts.size() == newContacts.size());
                if (result)
                {
                    for (Contact oldContact : oldContacts)
                    {
                        result = false;
                        for (Contact newContact : newContacts)
                        {
                            if (SafetyUtil.safeEquals(newContact.getAccount(), other.getBAN()))
                            {
                                oldContact.setId(newContact.getId());
                                oldContact.setAccount(newContact.getAccount());
                                if (SafetyUtil.safeEquals(oldContact, newContact))
                                {
                                    result = true;
                                    break;
                                }
                            }
                        }
                        if (!result)
                        {
                            break;
                        }
                    }
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(TestMoveManager.class, "Unable to load Contact Info", e).log(ctx);
            }
        }
        else
        {
            new MinorLogMsg(TestMoveManager.class, "Unable to load Contact Info: ContactHome not installed.", null).log(ctx);
        }
        
        return result;
    }

    private static boolean securityQuestionsEqual(final Account previousAccount, final Account other)
    {
        List<SecurityQuestionAnswer> oldQuestions = previousAccount.getSecurityQuestionsAndAnswers();
        List<SecurityQuestionAnswer> newQuestions = other.getSecurityQuestionsAndAnswers();
        
        boolean result = (oldQuestions.size() == newQuestions.size());
        if (result)
        {
            for (SecurityQuestionAnswer oldQuestion : oldQuestions)
            {
                result = false;
                try
                {
                    SecurityQuestionAnswer oldQuestionClone = (SecurityQuestionAnswer) oldQuestion.clone();
                    for (SecurityQuestionAnswer newQuestion : newQuestions)
                    {
                        if (SafetyUtil.safeEquals(newQuestion.getBAN(), other.getBAN()))
                        {
                            oldQuestionClone.setId(newQuestion.getId());
                            oldQuestionClone.setBAN(newQuestion.getBAN());
                            if (SafetyUtil.safeEquals(oldQuestionClone, newQuestion))
                            {
                                result = true;
                                break;
                            }
                        }
                    }
                }
                catch (CloneNotSupportedException e)
                {
                }
                if (!result)
                {
                    break;
                }
            }
        }
        return result;
    }

    private static boolean identificationEntriesEqual(final Account previousAccount, final Account other)
    {
        List<AccountIdentification> oldIdentifications = previousAccount.getIdentificationList();
        List<AccountIdentification> newIdentifications = other.getIdentificationList();
        boolean result = (oldIdentifications.size() == newIdentifications.size());
        if (result)
        {
            for (AccountIdentification oldIdentification : oldIdentifications)
            {
                result = false;
                try
                {
                    AccountIdentification oldIdentificationClone = (AccountIdentification) oldIdentification.clone();
                    for (AccountIdentification newIdentification : newIdentifications)
                    {
                        if (SafetyUtil.safeEquals(newIdentification.getBAN(), other.getBAN()))
                        {
                            oldIdentificationClone.setId(newIdentification.getId());
                            oldIdentificationClone.setBAN(newIdentification.getBAN());
                            if (SafetyUtil.safeEquals(oldIdentificationClone, newIdentification))
                            {
                                result = true;
                                break;
                            }
                        }
                    }
                }
                catch (CloneNotSupportedException e)
                {
                }
                if (!result)
                {
                    break;
                }
            }
        }
        return result;
    }

    /*
     * Unit Tests
     */
    
    /**
     * Compares all Subscriber Data with the exception of Subscriber BAN and Identifier.
     * And LastModified will not be the same if a new copy of the Subscriber is
     * created, so naturally it is not compared.
     * The method was copied from AbstractSubscriber.persistentEquals.
     * @return TRUE if the subscriber data matches.
     */
    private static boolean compareSubscriberDataEquals(
            final Context ctx,
            final Subscriber oldSub, 
            final Subscriber other,
            final boolean responsibleMove)
        throws HomeException
    {
        
        if ( oldSub == null || other == null )
        {
            throw new HomeException("Null Subscribers cannot be compared.");
        }

        boolean result = true;

        result &= ( !responsibleMove || SafetyUtil.safeEquals(oldSub.getBAN(), other.getBAN()) );
        result &= ( !responsibleMove || SafetyUtil.safeEquals(oldSub.getId(), other.getId()) );
        result &= ( oldSub.getSpid() == other.getSpid() );
        result &= ( oldSub.getSubscriptionClass() == other.getSubscriptionClass() );
        result &= ( oldSub.getSubscriptionType() == other.getSubscriptionType() );
        result &= ( oldSub.getTechnology() == other.getTechnology() );
        result &= ( oldSub.getSubscriberType() == other.getSubscriberType() );
        result &= SafetyUtil.safeEquals(oldSub.getDateCreated(), other.getDateCreated());
        result &= ( oldSub.getReasonCode() == other.getReasonCode() );
        result &= ( oldSub.getVraFraudProfile() == other.getVraFraudProfile() );
        result &= ( oldSub.getQuotaType() == other.getQuotaType() );
        result &= ( oldSub.getQuotaLimit() == other.getQuotaLimit() );
        result &= SafetyUtil.safeEquals(oldSub.getMSISDN(), other.getMSISDN());
        result &= SafetyUtil.safeEquals(oldSub.getFaxMSISDN(), other.getFaxMSISDN());
        result &= SafetyUtil.safeEquals(oldSub.getDataMSISDN(), other.getDataMSISDN());
        result &= SafetyUtil.safeEquals(oldSub.getIMSI(), other.getIMSI());
        result &= SafetyUtil.safeEquals(oldSub.getPackageId(), other.getPackageId());
        result &= ( oldSub.getHlrId() == other.getHlrId() );
        // TODO 2009-11-09 this field moved to VpnAuxiliarySubscriber bean
        //result &= SafetyUtil.safeEquals(oldSub.getVpnEntityId(), other.getVpnEntityId());
        result &= ( oldSub.getState() == other.getState() );
        result &= SafetyUtil.safeEquals(oldSub.getStartDate(), other.getStartDate());
        result &= SafetyUtil.safeEquals(oldSub.getEndDate(), other.getEndDate());
        result &= SafetyUtil.safeEquals(oldSub.getBillingLanguage(), other.getBillingLanguage());
        result &= ( oldSub.getBillingOption() == other.getBillingOption() );
        result &= ( oldSub.getDeposit(ctx) == other.getDeposit(ctx) );
        result &= SafetyUtil.safeEquals(oldSub.getNextDepositReleaseDate(), other.getNextDepositReleaseDate());
        result &= ( oldSub.getCreditLimit(ctx) == other.getCreditLimit(ctx) );
        result &= ( oldSub.isAboveCreditLimit() == other.isAboveCreditLimit() );
        result &= SafetyUtil.safeEquals(oldSub.getDepositDate(), other.getDepositDate());
        result &= ( oldSub.getInitialBalance() == other.getInitialBalance() );
        result &= ( oldSub.getMaxBalance() == other.getMaxBalance() );
        result &= ( oldSub.getMaxRecharge() == other.getMaxRecharge() );
        result &= ( oldSub.getReactivationFee() == other.getReactivationFee() );
        result &= SafetyUtil.safeEquals(oldSub.getExpiryDate(), other.getExpiryDate());
        result &= ( oldSub.getPreExpirySmsSent() == other.getPreExpirySmsSent() );
        result &= ( oldSub.getPricePlan() == other.getPricePlan() );
        result &= ( oldSub.getPricePlanVersion() == other.getPricePlanVersion() );
        result &= ( oldSub.getSecondaryPricePlan() == other.getSecondaryPricePlan() );
        result &= SafetyUtil.safeEquals(oldSub.getSecondaryPricePlanStartDate(), other.getSecondaryPricePlanStartDate());
        result &= SafetyUtil.safeEquals(oldSub.getSecondaryPricePlanEndDate(), other.getSecondaryPricePlanEndDate());
        result &= ( oldSub.getFirstInitCltc() == other.getFirstInitCltc() );
        result &= SafetyUtil.safeEquals(oldSub.getBundles(), other.getBundles());
        result &= SafetyUtil.safeEquals(oldSub.getPointsBundles(), other.getPointsBundles());
        result &= ( oldSub.getSubscriberCategory() == other.getSubscriberCategory() );
        result &= SafetyUtil.safeEquals(oldSub.getMarketingCampaignBean(), other.getMarketingCampaignBean());
        result &= SafetyUtil.safeEquals(oldSub.getProvisionedBundles(), other.getProvisionedBundles());
        result &= ( oldSub.getWeeklyRecurringCharge() == other.getWeeklyRecurringCharge() );
        result &= ( oldSub.getAuxiliaryServicesProcessed() == other.getAuxiliaryServicesProcessed() ) ;
        
        return result;
     }


    private static boolean spidHasCarryOverBalance(Context ctx, int spidId)
        throws HomeException
    {
        CRMSpid spid = SpidSupport.getCRMSpid(ctx, spidId);
        return spid.isCarryOverBalanceOnMove();
    }

    /**
     * ACCOUNT5_BAN (individual responsible) is under ACCOUNT4_BAN(group, responsible) 
     * @param context
     */
    protected void setupScenario1(Context context) 
    {
        try
        {
            //Move ACCOUNT5_BAN under ACCOUNT4_BAN (to place it in a hierarchy) before trying to move it out.
            Account account = AccountSupport.getAccount(context, TestSetupAccountHierarchy.ACCOUNT5_BAN);
            account.setParentBAN(TestSetupAccountHierarchy.ACCOUNT4_BAN);
            Home home = (Home) context.get(AccountHome.class);
            home.store(account);
        }
        catch(HomeException e)
        {
            fail("Failed test setup of scenario 1. " + e.getMessage());
        }
    }

    public static Account verifyAccountProperties(Context ctx, 
            final String ban, 
            final boolean isIndividual,
            final boolean isPooled, 
            final boolean isResponsible, 
            final boolean isPooledMember) 
    {
        Account account = null;
        
        try
        {
            account = AccountSupport.getAccount(ctx, ban);
    
            if (isIndividual)
            {
                assertTrue("Account " + account.getBAN() + " is supposed to be individual. ", account.isIndividual(ctx));
            }
            else
            {
                assertFalse("Account " + account.getBAN() + " is not supposed to be individual. ", account.isIndividual(ctx));
            }
            if (isPooled)
            {
                assertTrue("Account " + account.getBAN() + " is supposed to be pooled.", account.isPooled(ctx));
            }
            else
            {
                assertFalse("Account " + account.getBAN() + " is not supposed to be pooled.", account.isPooled(ctx));    
            }
            if (isPooledMember)
            {
                assertTrue("Account " + account.getBAN() + " is supposed to be pool member.", account.getParentAccount(ctx).isPooled(ctx));
            }
            if (isResponsible)
            {
                assertTrue("Account " + account.getBAN() + " is supposed to be Responsible.", account.isResponsible());
            }
            else
            {
                assertFalse("Account " + account.getBAN() + " is not supposed to be Responsible.", account.isResponsible());
            }
        }
        catch (HomeException e)
        {
            String msg = "Failed to verify Properties of Account " + ban;
            LogSupport.debug(ctx, TestMoveManager.class, msg, e);
            fail(msg);
        }
        return account;
    }

    public void testSetup()
    {
        TestSetupAccountHierarchy.testSetup(getContext());
    }
    
    public void testSetupValidate()
    {
        //If no Account Cached home exists, there should be an exception thrown.
        getContext().remove(Common.ACCOUNT_CACHED_HOME);
        //Validate a test case expected to pass.
        {
            AccountMoveRequest request = getAccountMoveRequest(TestSetupAccountHierarchy.ACCOUNT5_BAN, 
                    TestSetupAccountHierarchy.ACCOUNT1_BAN);            
            MoveManager manager = new MoveManager();
            try
            {
                manager.validate(getContext(), request);
                fail("Test should fail, since Account cache was not set in the context.");
            }
            catch(IllegalStateException e)
            {
                // this exception is expected
            }
        }
    }
    
    public void testAccountMoveValidate()
    {
        // Validate Missing Parameters Error Case
        {
            AccountMoveRequest request = new AccountMoveRequest();
            request.setExistingBAN(TestSetupAccountHierarchy.ACCOUNT5_BAN);
            request.setNewBAN(TestSetupAccountHierarchy.ACCOUNT1_BAN);
            
            MoveManager manager = new MoveManager();
            try
            {
                manager.validate(getContext(), request);
                fail("Test should fail, since New Parent BAN was not set.");
            }
            catch(CompoundIllegalStateException e)
            {
                /* this exception is expected, since it is a responsible account not in a hierarchy
                 * and no new Parent BAN was set.
                 */
            }
        }
        
        // Validate Non Existent Original Account
        {
            AccountMoveRequest request = getAccountMoveRequest("23456",
                    TestSetupAccountHierarchy.ACCOUNT1_BAN);
            
            MoveManager manager = new MoveManager();
            try
            {
                manager.validate(getContext(), request);
                fail("Test should fail, since old BAN does not exist in the system.");
            }
            catch(CompoundIllegalStateException e)
            {
                // this exception is expected. Thrown by NotesAccountMoveProcessor
            }
        }
        
        // Validate Non Existent New Parent Account
        {
            AccountMoveRequest request = getAccountMoveRequest(TestSetupAccountHierarchy.ACCOUNT5_BAN,
                    "23456");
            
            MoveManager manager = new MoveManager();
            try
            {
                manager.validate(getContext(), request);
                fail("Test should fail, since new BAN does not exist in the system.");
            }
            catch(CompoundIllegalStateException e)
            {
                // this exception is expected. Thrown by NotesAccountMoveProcessor
            }
        }
        
        //Validate a test case expected to pass.
        {
            AccountMoveRequest request = getAccountMoveRequest(TestSetupAccountHierarchy.ACCOUNT5_BAN, 
                    TestSetupAccountHierarchy.ACCOUNT1_BAN);

            MoveManager manager = new MoveManager();
            manager.validate(getContext(), request);
        }
        
    }
    
    /**
     * Validating moving out of a hierarchy
     */
    public void testAccountMoveValidate2()
    {
        //Move ACCOUNT5_BAN under ACCOUNT4_BAN (to place it in a hierarchy) before trying to move it out.
        setupScenario1(getContext());
        
        // Validate Missing Parameters Valid Case
        {
            AccountMoveRequest request = new AccountMoveRequest();
            request.setExistingBAN(TestSetupAccountHierarchy.ACCOUNT5_BAN);
            request.setNewBAN(TestSetupAccountHierarchy.ACCOUNT1_BAN);

            MoveManager manager = new MoveManager();
            try
            {
                manager.validate(getContext(), request);
            }
            catch(CompoundIllegalStateException e)
            {
                fail("Test should pass, since account is moving out of a hierarchy.");
            }
        }
    }
    
    /**
     * An Individual Account is Responsible.
     * CRM doesn't create a new copy of the Responsible accounts to move under the destination Account.
     * It simply links the responsible account to the Parent Account.
     */
    public void testMoveIndividualAccountIntoGroup() throws HomeException, MoveException
    {
        // Move Individual Subscriber Account into Group Account
        Context ctx = getContext().createSubContext();
        String movingAccount = TestSetupAccountHierarchy.ACCOUNT5_BAN;
        String destinationAccount = TestSetupAccountHierarchy.ACCOUNT1_BAN;
        verifyAccountProperties(ctx, destinationAccount, false, false, true, false); 

        //Obtain records to compare with after move.
        //Get the Account and subscribers before the move
        Account previousAccount = verifyAccountProperties(ctx, movingAccount, true, false, true, false);
        Map<SubscriptionType, Subscriber> previousSubscribers = getCurrentSubscriptions(ctx, movingAccount);

        //Execute Move
        executeMove(ctx, movingAccount, destinationAccount);

        //Assert no new accounts were created for this responsible account move.
        verifyNumberOfNewAccountsCreated(0);
        
        //A responsible account must keep the same BAN when it is moved.
        //verifyMovedAccount(ctx, movingAccount, destinationAccount, 
        //        previousAccount, previousSubscribers);
        assertTrue("Change this to verify the CompoundMoveRequest from Move Manager.", true);
    }

    /**
     * An Individual Account is Responsible.
     * CRM doesn't create a new copy of the Responsible accounts to move under the destination Account.
     * It simply links the responsible account to the Parent Account.
     */
    public void testMoveIndividualAccountIntoPooled() throws HomeException, MoveException
    {
        // Move Individual Subscriber Account into Pooled Account  
        Context ctx = getContext().createSubContext();
        String movingAccount = TestSetupAccountHierarchy.ACCOUNT5_BAN;
        String destinationAccount = TestSetupAccountHierarchy.ACCOUNT3_BAN;
        verifyAccountProperties(ctx, destinationAccount, false, true, true, false); 

        //Obtain records to compare with after move.
        //Get the Account and subscribers before the move
        Account previousAccount = verifyAccountProperties(ctx, movingAccount, true, false, true, false); 
        Map<SubscriptionType, Subscriber> previousSubscribers = getCurrentSubscriptions(ctx, movingAccount);

        //Execute Move
        executeMove(ctx, movingAccount, destinationAccount);

        //Assert no new accounts were created for this responsible account move.
        verifyNumberOfNewAccountsCreated(0);
        
        //A responsible account must keep the same BAN when it is moved.
        /* verifyMovedAccount(ctx, movingAccount, destinationAccount, 
                previousAccount, previousSubscribers);
                */
        assertTrue("Change this to verify the CompoundMoveRequest from Move Manager.", true);
    }
    
    /**
     * Move A Non-Responsible Subscriber Account 
     */
    public void testMoveNonResponsibleAccount() throws HomeException, MoveException
    {
        // Move Non Responsible Subscriber Account into Group Account
        Context ctx = getContext().createSubContext();
        String movingAccount = TestSetupAccountHierarchy.getIndividualAccountIdentifier(TestSetupAccountHierarchy.SUB7_ID);
        String destinationAccount = TestSetupAccountHierarchy.ACCOUNT1_BAN;
        verifyAccountProperties(ctx, destinationAccount, false, false, true, false);

        //Obtain records to compare with after move.
        //Get the Account and subscribers before the move
        Account previousAccount = verifyAccountProperties(ctx, movingAccount, true, false, false, false);
        Map<SubscriptionType, Subscriber> previousSubscribers = getCurrentSubscriptions(ctx, movingAccount);

        //Execute Move
        AccountMoveRequest completedRequest = executeMove(ctx, movingAccount, destinationAccount);

        //Assert no new accounts were created for this responsible account move.
        verifyNumberOfNewAccountsCreated(1);
        
        //A responsible account must keep the same BAN when it is moved.
        /*verifyMovedAccount(ctx, completedRequest.getNewBAN(), destinationAccount, 
                previousAccount, previousSubscribers);*/
        assertTrue("Change this to verify the CompoundMoveRequest from Move Manager.", true);

        // Move Group Subscriber Account into Pooled Account        
    }

    Date TEST_START_TIME = null;

    class MoveSubscriberProfileProvisionTestClient extends SubscriberProfileProvisionTestClient
    {
        /**
         * @{inheritDoc}
         */
        @Override
        public Parameters querySubscriberAccountProfile(Context context, Account subscriberAccount)
                throws HomeException, SubscriberProfileProvisionException
        {
            if (AccountStateEnum.INACTIVE.equals(subscriberAccount.getState()))
            {
                return super.querySubscriberAccountProfile(context, subscriberAccount);
            }
            else
            {
                return new Parameters()
                .subscriberID(subscriberAccount.getBAN())
                .spid(TestSetupAccountHierarchy.SPID_ID);
            }
        }

        /**
         * @{inheritDoc}
         */
        @Override
        public Parameters querySubscriptionProfile(Context context, Subscriber subscription) throws HomeException,
                SubscriberProfileProvisionException
        {
            if (SubscriberStateEnum.INACTIVE.equals(subscription.getState()))
            {
                return super.querySubscriptionProfile(context, subscription);
            }
            else
            {
                return new Parameters()
                .subscriberID(subscription.getBAN())
                .msisdn(subscription.getMSISDN())
                .spid(TestSetupAccountHierarchy.SPID_ID);
            }
        }
    }//MoveSubscriberProfileProvisionTestClient
    
    /**
     * This class will gather and store all the entities that we have interest to compare
     * after the Move is accomplished.
     * @author angie.li@redknee.com
     *
     */
    public static class CollectionOfComparableEntities 
    {
        /**
         * Constructor begins the entity gathering process.
         * @param ctx
         * @param originalAccount
         */
        CollectionOfComparableEntities(Context ctx, Account originalAccount)
        {
            originalAccounts_ = getAllMoveCandidateAccounts(ctx, originalAccount);
            originalSubscribers_ = getAllMoveCandidateSubscribers(ctx, originalAccount);
            originalSubscriberServices_ = getAllExistingSubscriberServices(ctx, originalSubscribers_);
            outstandingBalance_ = getTotalOwing(ctx, originalSubscribers_);
            testStartTime_ = new Date();
        }
        
        /**
         * Return the collection of original Accounts.
         * @return
         */
        Collection<Account> getOriginalAccounts()
        {
            return originalAccounts_;
        }
        
        /**
         * Return the original Subscriber for the given Subscriber identified.
         * @param ctx
         * @param subId
         * @return
         */
        Subscriber getOriginalSubscriber(Context ctx, String subId)
        {
            EQ idPredicate = new EQ(SubscriberXInfo.ID, subId); 
            Subscriber originalSubscription = CollectionSupportHelper.get(ctx).findFirst(ctx, originalSubscribers_, idPredicate);
            return originalSubscription;
        }
        
        /**
         * Return the original Subscriber Services for the given Subscriber identified.
         * @param ctx
         * @param subId
         * @return
         */
        Collection<SubscriberServices> getOriginalSubscriberServicesForSub(Context ctx, String subId)
        {
            Predicate predicate = new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId);
            Collection<SubscriberServices> originalSubscriberServices = CollectionSupportHelper.get(ctx).findAll(ctx, 
                    originalSubscriberServices_, predicate);
            return originalSubscriberServices;
        }
        
        /**
         * Return the outstanding balance for the given subscriber identified.
         * @param ctx
         * @param subId
         * @return
         */
        long getOutstandingBalance(Context ctx, String subId)
        {
            Long balance = outstandingBalance_.get(subId);
            long returnedBal = 0;
            if (balance != null)
            {
                returnedBal = balance.longValue();
            }
            return returnedBal;
        }
        
        Date getTestStartTime()
        {
            return testStartTime_;
        }
        
        /**
         * Return all the Accounts (Group and Subscriber accounts) from the hierarchy of the 
         * given Parent Account, regardless of the account state.
         * @param ctx
         * @param parentAccount
         * @return
         */
        private Collection<Account> getAllMoveCandidateAccounts(Context ctx, final Account parentAccount) 
        {
            Collection<Account> returnedCol = new ArrayList<Account>();
            try
            {
                returnedCol.add(parentAccount);
                Collection<Account> childrenAccounts = parentAccount.getImmediateChildrenAccounts(ctx);
                for(Account childAccount: childrenAccounts)
                {
                    returnedCol.addAll(getAllMoveCandidateAccounts(ctx, childAccount));
                }
            }
            catch (Exception e)
            {
                logDebugMsg(ctx, "Failed to retrieve Account records. " + e.getMessage(), e);
            }
            return returnedCol;
        }

        /**
         * Return all the Subscriber Subscriptions in the hierarchy of the given Parent Account, 
         * regardless of their state.
         * @param ctx
         * @param parentAccount
         * @return
         */
        private Collection<Subscriber> getAllMoveCandidateSubscribers(Context ctx,
                final Account parentAccount) 
        {
            Collection<Subscriber> returnedCol = new ArrayList<Subscriber>();
            try
            {
                Collection<Subscriber> immediateChildSubs = AccountSupport.getImmediateChildrenSubscribers(ctx, parentAccount);
                returnedCol.addAll(immediateChildSubs);
                
                Collection<Account> childrenAccounts = parentAccount.getImmediateChildrenAccounts(ctx);
                for(Account childAccount: childrenAccounts)
                {
                    returnedCol.addAll(getAllMoveCandidateSubscribers(ctx, childAccount));
                }
            }
            catch (Exception e)
            {
                logDebugMsg(ctx, "Failed to retrieve Subscriber records. " + e.getMessage(), e);
            }
            return returnedCol;
        }

        /**
         * Get all SubscriberServices records for the given Subscribers (eligible for move)
         * The reason that we are gathering all the SubscriberServices and not just the Provisioned
         * services, is that CRM must make sure the Subscriber "copy" (move) is exactly
         * as it was before the move.  That means including suspended or not yet provisioned
         * Services.
         * @param ctx
         * @param originalSubscribers
         * @return
         */
        private Collection<SubscriberServices> getAllExistingSubscriberServices(
                Context ctx, 
                Collection<Subscriber> originalSubscribers) 
                {
            Collection<SubscriberServices> returnedCol = new ArrayList();
            try
            {
                HashSet subIds = new HashSet();
                for(Subscriber sub: originalSubscribers)
                {
                    if (TestMoveManager.isCandidateForMove(sub))
                    {
                        subIds.add(sub.getId());
                    }
                }
                Predicate predicate = new In(SubscriberServicesXInfo.SUBSCRIBER_ID, subIds);
                Home home = (Home) ctx.get(SubscriberServicesHome.class);
                returnedCol = home.where(ctx, predicate).selectAll();
            }
            catch (Exception e)
            {
                logDebugMsg(ctx, "Failed to retrieve Subscriber Services records. " + e.getMessage(), e);
            }
            return returnedCol;
        }
        
        private HashMap<String, Long> getTotalOwing(Context ctx,
                Collection<Subscriber> originalSubscribers_) 
        {
            HashMap<String, Long> values = new HashMap<String, Long>();
            try
            {
                for(Subscriber sub: originalSubscribers_)
                {
                    /*boolean isTaxExempted = false;
                    SubscriberInvoiceCalculation calc = SubscriberSupport.getInvoiceCalculations(ctx, isTaxExempted, sub, sub.getAccount(ctx));
                    long outstandingBalance = calc.getTotal() + calc.getPaymentsReceived();
                    values.put(sub.getId(), outstandingBalance);*/
                }
            }
            catch (Exception e)
            {
                logDebugMsg(ctx, "Failed to retrieve Subscriber Invoice calculation." + e.getMessage(), e);
            }
            assertEquals(originalSubscribers_.size(), values.size());
            return values;
        }

        /**
         * Log Debug message about the exception that occurred.
         * @param ctx
         * @param msg
         * @param e
         */
        private void logDebugMsg(Context ctx, String msg, Throwable e) 
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(CollectionOfComparableEntities.class, msg, e).log(ctx);
            }
        }
        
        
        /**
         * Collection of the original Account profiles (prior to the execution of the test)
         */
        Collection<Account> originalAccounts_ = null;
        /**
         * Collection of the original Subscriber (Subscription) profiles (prior to the execution of the test)
         */
        Collection<Subscriber> originalSubscribers_ = null;
        /**
         * Collection of the original SubscriberServices profiles (prior to the execution of the test)
         */
        Collection<SubscriberServices> originalSubscriberServices_ = null;
        /**
         * Collection of the Outstanding Balance of each Subscription (prior to the execution of the test).
         * Used to verify the carry over balance.
         */
        HashMap<String, Long> outstandingBalance_ = null;
        /**
         * Timestamp of when the unit test setup data was finished generating.  This 
         * will be used to differentiate the data generated by the test execution from the setup data.
         */
        Date testStartTime_ = null;
        
    }//CollectionOfComparableEntities
}//TestMoveManager
