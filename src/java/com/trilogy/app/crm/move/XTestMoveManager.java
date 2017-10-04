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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.move.TestMoveManager.CollectionOfComparableEntities;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.CompoundMoveRequest;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.PaymentPlanSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.app.crm.unit_test.TestSetupPricePlanAndServices;
import com.trilogy.app.crm.unit_test.UnitTestSupport;
import com.trilogy.app.crm.unit_test.xtest.XTestSetupAccountHierarchy;
import com.trilogy.app.crm.unit_test.xtest.XTestSetupAccountHierarchy.ReturnIdentifier;

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
public class XTestMoveManager extends ContextAwareTestCase 
{
    public XTestMoveManager(String name)
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
        final TestSuite suite = new TestSuite(XTestMoveManager.class);
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
        //Setup all Licenses
        context.put(PaymentPlanSupport.DISABLE_LICENSE_KEY, Boolean.TRUE);

        //Setup Homes
        XTestSetupAccountHierarchy.setup(context);
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
            new DebugLogMsg(XTestMoveManager.class, msg, e).log(ctx);
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
    }

    /**
     * Delegates to XTestSetupAccountHierarchy utility method
     * @param ctx
     * @param banSelection
     * @return
     */
    private String setupGroupAccount(Context ctx, ReturnIdentifier banSelection) 
    {
        return XTestSetupAccountHierarchy.setupGroupAccount(ctx, banSelection);
    }

    /**
     * Delegates to XTestSetupAccountHierarchy utility method
     * to setup an Individual, Non pooled member Subscriber Account.
     * @param ctx
     * @param subscriberType
     * @param parentBAN
     * @param isResponsible
     * @return
     */
    private String setupIndividualSubscriberAccount(Context ctx,
            final SubscriberTypeEnum subscriberType, 
            final String parentBAN,
            final boolean isResponsible) 
    {
        return XTestSetupAccountHierarchy.setupIndividualSubscriberAccount(ctx, subscriberType, parentBAN, 
                isResponsible, false);
    }

    /**
     * Delegates to XTestSetupAccountHierarchy utility method
     * @param context
     * @param banSelection
     * @return
     */
    private String setupAccountsWithHierarchy(Context context,
            ReturnIdentifier banSelection) 
    {
        return XTestSetupAccountHierarchy.setupAccountsWithHierarchy(context, banSelection);
    }

    /**
     * Delegates to XTestSetupAccountHierarchy utility method
     * @param ctx
     * @param parentBAN
     * @param selectBAN
     * @return
     */
    private String setupPooledAccount(Context ctx, String parentBAN,
            ReturnIdentifier selectBAN) 
    {
        return XTestSetupAccountHierarchy.setupPooledAccount(ctx, parentBAN, selectBAN);
    }
    
    

    private void verifyAllElementsMoved(Context ctx,
            final CompoundMoveRequest processedRequest,
            final CollectionOfComparableEntities originalEntities) 
    {
        TestMoveManager.verifyAllElementsMoved(ctx, processedRequest, originalEntities);
    }

    private Account verifyAccountProperties(Context ctx, 
            final String ban, 
            final boolean isIndividual,
            final boolean isPooled, 
            final boolean isResponsible,
            final boolean isPoolMember) 
    {
        return TestMoveManager.verifyAccountProperties(ctx, ban, isIndividual, isPooled, isResponsible, isPoolMember);
    }

    /**
     * Executes the move for the given account to the given destination account.
     * Returns CompoundMoveRequest with all the moves that were performed.
     * @param ctx
     * @param movingAccount
     * @param destinationAccount
     */
    protected static CompoundMoveRequest executeMove(Context ctx, 
            final String movingAccount,
            final String destinationAccount) 
        throws MoveException
    {
        //Create Move Request
        AccountMoveRequest request = TestMoveManager.getAccountMoveRequest(movingAccount, destinationAccount);
        
        //Execute Account Move
        MoveManager manager = new MoveManager();
        manager.validate(ctx, request);
        manager.move(ctx, request);
        return manager.getProcessedRequests();
    }
    

    public void testSetup()
    {
        if (!UnitTestSupport.isTestRunningInXTest(getContext()))
        {
            fail("This test is meant to run as an XTest on a live system.  Running it off-line is invalid.");
        }
        
        XTestSetupAccountHierarchy.testSetup(getContext());
    }
    
    
    public void testAccountMoveValidate()
    {
        Context ctx = getContext();
        String destinationAccount = setupGroupAccount(ctx, ReturnIdentifier.ROOT);
        String originalAccount = setupIndividualSubscriberAccount(ctx, SubscriberTypeEnum.PREPAID, "", true);
        String nonExistentAccount = "923456";
        try
        {    
            //Ensure that nonExistentAccount is not in the system.
            assertNull("Error in setup. Account " + nonExistentAccount + "is not supposed to exist in the system.",
                    AccountSupport.getAccount(ctx, nonExistentAccount));
        }
        catch (Exception e)
        {
            logDebugMsg(ctx, "Failed to verify if Account " + nonExistentAccount 
                    + "did not exist in the system.  This test might fail.", e);
        }
        
        // Validate Missing Parameters Error Case
        {
            AccountMoveRequest request = new AccountMoveRequest();
            request.setExistingBAN(destinationAccount);
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
            AccountMoveRequest request = TestMoveManager.getAccountMoveRequest(nonExistentAccount,
                    destinationAccount);
            
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
            AccountMoveRequest request = TestMoveManager.getAccountMoveRequest(originalAccount,
                    nonExistentAccount);
            
            MoveManager manager = new MoveManager();
            try
            {
                manager.validate(getContext(), request);
                fail("Test should fail, since new Parent BAN does not exist in the system.");
            }
            catch(CompoundIllegalStateException e)
            {
                // this exception is expected. Thrown by NotesAccountMoveProcessor
            }
        }
        
        //Validate a test case expected to pass.
        {
            AccountMoveRequest request = TestMoveManager.getAccountMoveRequest(originalAccount, destinationAccount);

            MoveManager manager = new MoveManager();
            manager.validate(getContext(), request);
        }
        
    }
    
    /**
     * Test moving an Individual Account (responsible) under a new Group Account
     */
    public void testMoveIndividualAccount()
    {
        Context ctx = getContext().createSubContext();
        String destinationBAN = setupGroupAccount(ctx, ReturnIdentifier.ROOT);
        String originalAccount = setupIndividualSubscriberAccount(ctx, SubscriberTypeEnum.PREPAID, "", true);
        
        verifyAccountProperties(ctx, destinationBAN, false, false, true, false); 
        Account oldOriginalAccount = verifyAccountProperties(ctx, originalAccount, true, false, true, false);
               
        try
        {
            //Gather all entities to compare after the move.
            CollectionOfComparableEntities originalEntities = new CollectionOfComparableEntities(ctx, oldOriginalAccount); 

            String msg = "Moving Individual Responsible Account " + originalAccount + " under new Parent BAN " + destinationBAN;
            logDebugMsg(ctx, msg, null);
            CompoundMoveRequest processedRequest = executeMove(ctx, originalAccount, destinationBAN);
            
            //There should have been no errors with this move setup.
            assertFalse(processedRequest.hasErrors(ctx));
            
            verifyAllElementsMoved(ctx, processedRequest, originalEntities);
        }
        catch(MoveException e)
        {
            logDebugMsg(ctx, "Failed to execute move. " + e.getMessage(), e);
            fail("Failed to execute move. " + e.getMessage());
        }
    }
    
    /**
     * Test moving a non-responsible subscriber Account under a new Group Account
     */
    public void testMoveNonResponsibleSubscriberAccount()
    {
        Context ctx = getContext().createSubContext();
        String destinationBAN = setupGroupAccount(ctx, ReturnIdentifier.ROOT);
        //Setup Non-Responsible Account
        String originalAccount = setupGroupAccount(ctx, ReturnIdentifier.LEAF);
        
        verifyAccountProperties(ctx, destinationBAN, false, false, true, false); 
        Account oldOriginalAccount = verifyAccountProperties(ctx, originalAccount, true, false, false, false);
               
        try
        {
            //Gather all entities to compare after the move.
            CollectionOfComparableEntities originalEntities = new CollectionOfComparableEntities(ctx, oldOriginalAccount); 

            String msg = "Moving Non-Responsible Account " + originalAccount + " under new Parent BAN " + destinationBAN;
            logDebugMsg(ctx, msg, null);
            CompoundMoveRequest processedRequest = executeMove(ctx, originalAccount, destinationBAN);
            
            //There should have been no errors with this move setup.
            assertFalse(processedRequest.hasErrors(ctx));
            
            verifyAllElementsMoved(ctx, processedRequest, originalEntities);        
        }
        catch(MoveException e)
        {
            logDebugMsg(ctx, "Failed to execute move. " + e.getMessage(), e);
            fail("Failed to execute move. " + e.getMessage());
        }
    }
    
    /**
     * Test moving a non-responsible Group Account under a new Group Account
     */
    public void testMoveNonResponsibleGroupAccount()
    {
        Context ctx = getContext().createSubContext();
        String destinationAccount = setupGroupAccount(ctx, ReturnIdentifier.ROOT);
        //Setup Account Hierarchy (2 levels).  Move the mid layer out.
        String nonResponsibleAccount = setupAccountsWithHierarchy(ctx, ReturnIdentifier.ROOT_CHILD);
        
        verifyAccountProperties(ctx, destinationAccount, false, false, true, false); 
        Account oldOriginalAccount = verifyAccountProperties(ctx, nonResponsibleAccount, false, false, false, false);
        
        try
        {
            //Gather all entities to compare after the move.
            CollectionOfComparableEntities originalEntities = new CollectionOfComparableEntities(ctx, oldOriginalAccount); 

            String msg = "Moving Non-Responsible Group Account " + nonResponsibleAccount + " under new Parent BAN " + destinationAccount;
            logDebugMsg(ctx, msg, null);
            CompoundMoveRequest processedRequest = executeMove(ctx, nonResponsibleAccount, destinationAccount);
            
            //There should have been no errors with this move setup.
            assertFalse(processedRequest.hasErrors(ctx));
            
            verifyAllElementsMoved(ctx, processedRequest, originalEntities);  
        }
        catch(MoveException e)
        {
            logDebugMsg(ctx, "Failed to execute move. " + e.getMessage(), e);
            fail("Failed to execute move. " + e.getMessage());
        }
    }
    
    /**
     * Test moving a Subscriber account out of a pooled account into a group account
     */
    public void testMoveOutOfPooledAccount()
    {
        Context ctx = getContext().createSubContext();
        String destinationAccount = setupGroupAccount(ctx, ReturnIdentifier.ROOT);
        String nonLeaderAccount = setupPooledAccount(ctx, "", ReturnIdentifier.MEMBER);
        
        verifyAccountProperties(ctx, destinationAccount, false, false, true, false); 
        Account oldOriginalAccount = verifyAccountProperties(ctx, nonLeaderAccount, true, false, false, true);
        
        try
        {
            //Gather all entities to compare after the move.
            CollectionOfComparableEntities originalEntities = new CollectionOfComparableEntities(ctx, oldOriginalAccount); 

            String msg = "Moving Non-Responsible Account " + nonLeaderAccount 
                    + " out from a Pooled Account to Group Account Parent BAN " + destinationAccount;
            logDebugMsg(ctx, msg, null);
            CompoundMoveRequest processedRequest = executeMove(ctx, nonLeaderAccount, destinationAccount);
            
            //There should have been no errors with this move setup.
            assertFalse(processedRequest.hasErrors(ctx));
            
            verifyAllElementsMoved(ctx, processedRequest, originalEntities); 
        }
        catch(MoveException e)
        {
            logDebugMsg(ctx, "Failed to execute move. " + e.getMessage(), e);
            fail("Failed to execute move. " + e.getMessage());
        }
    }
    
    
    /**
     * Test moving a Subscriber account out of a group account into a pooled account 
     */
    public void testMoveIntoPooledAccount()
    {
        Context ctx = getContext().createSubContext();
        String destinationAccount = setupPooledAccount(ctx, "", ReturnIdentifier.ROOT);
        String subscriberAccount = setupGroupAccount(ctx, ReturnIdentifier.LEAF);
        
        verifyAccountProperties(ctx, destinationAccount, false, true, true, false); 
        Account oldOriginalAccount = verifyAccountProperties(ctx, subscriberAccount, true, false, false, false);
        
        try
        {
            //Gather all entities to compare after the move.
            CollectionOfComparableEntities originalEntities = new CollectionOfComparableEntities(ctx, oldOriginalAccount); 

            String msg = "Moving Non-Responsible Pooled Account " + subscriberAccount 
                    + " out from a Group Account to a Pooled Account, new Parent BAN=" + destinationAccount;
            logDebugMsg(ctx, msg, null);
            CompoundMoveRequest processedRequest = executeMove(ctx, subscriberAccount, destinationAccount);
            
            //There should have been no errors with this move setup.
            assertFalse(processedRequest.hasErrors(ctx));
            
            verifyAllElementsMoved(ctx, processedRequest, originalEntities); 
        }
        catch(MoveException e)
        {
            logDebugMsg(ctx, "Failed to execute move. " + e.getMessage(), e);
            fail("Failed to execute move. " + e.getMessage());
        }
    }
    
    /**
     * Test moving a Subscriber account out of a group account into a pooled account 
     */
    public void testMoveBetweenPooledAccount()
    {
        Context ctx = getContext().createSubContext();
        String destinationAccount = setupPooledAccount(ctx, "", ReturnIdentifier.ROOT);
        String nonLeaderAccount = setupPooledAccount(ctx, "", ReturnIdentifier.MEMBER);
        
        verifyAccountProperties(ctx, destinationAccount, false, true, true, false); 
        Account oldOriginalAccount = verifyAccountProperties(ctx, nonLeaderAccount, true, false, false, true);
        
        try
        {
            //Gather all entities to compare after the move.
            CollectionOfComparableEntities originalEntities = new CollectionOfComparableEntities(ctx, oldOriginalAccount); 

            String msg = "Moving Non-Responsible Account " + nonLeaderAccount 
                    + " out from a Pooled Account to Pooled Account, Parent BAN " + destinationAccount;
            logDebugMsg(ctx, msg, null);
            CompoundMoveRequest processedRequest = executeMove(ctx, nonLeaderAccount, destinationAccount);
            
            //There should have been no errors with this move setup.
            assertFalse(processedRequest.hasErrors(ctx));
            
            verifyAllElementsMoved(ctx, processedRequest, originalEntities); 
        }
        catch(MoveException e)
        {
            logDebugMsg(ctx, "Failed to execute move. " + e.getMessage(), e);
            fail("Failed to execute move. " + e.getMessage());
        }
    }

}
