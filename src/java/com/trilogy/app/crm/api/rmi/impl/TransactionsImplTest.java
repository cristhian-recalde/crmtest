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
package com.trilogy.app.crm.api.rmi.impl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.Permission;
import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.api.queryexecutor.QueryExecutorsInvocationHandler;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionTransientHome;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStateEnum;
import com.trilogy.util.crmapi.wsdl.v2_3.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.TransactionServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;

/**
 * Tests for API methods.
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class TransactionsImplTest extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TransactionsImplTest(final String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * {@inheritDoc}
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TransactionsImplTest.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        super.setUp();

        final Context ctx = getContext();
        impl_ = (TransactionServiceSkeletonInterface) QueryExecutorsInvocationHandler.newInstance(ctx, TransactionServiceSkeletonInterface.class);
        
        try 
        {
        	//Setup default Service Provider
        	TestSetupAccountHierarchy.setupSpid(getContext());
        }
        catch (HomeException e)
        {
        	throw new IllegalStateException("Cannot continue with test. Failed SPID setup for test.", e);
        }

        home_ = new TransactionTransientHome(ctx);
        
        ctx.put(TransactionHome.class, home_);
        
        home1_ = new AccountTransientHome(ctx);
        
        ctx.put(AccountHome.class, home1_);
        
        home2_ = new SubscriberTransientHome(ctx);
        
        ctx.put(SubscriberHome.class, home2_);
        
        // Set up an AdjustmentType home for testing.
        adjustmentTypeHome = new AdjustmentTypeTransientHome(ctx);
        
        ctx.put(AdjustmentTypeHome.class, adjustmentTypeHome);
        ctx.put(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_READ_ONLY_HOME, adjustmentTypeHome);
        ctx.put(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME, adjustmentTypeHome);
        
        ctx.put(AuthSPI.class, new FakeAuthSPI());
        ctx.put(Principal.class, new User());

        header_ = new CRMRequestHeader();

    }

    public void testListTransaction() throws CRMExceptionFault, HomeException, IOException, InstantiationException
    {
        final Context ctx = getContext();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start = CalendarSupportHelper.get(ctx).setDayBefore(start);
        short subType =0;        
        end = CalendarSupportHelper.get(ctx).setDayAfter(end);
        
        Date current = new Date();
        createAccount( ctx,"14", "Test", 1,1);
        createAdjustment( ctx,AdjustmentTypeEnum.StandardPayments_INDEX, 0, "Standard Payment");
        createTransaction(ctx, 14490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 17);
        createTransaction(ctx, 1390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200"  ,new Date(), "14-2", 2);
        createTransaction(ctx, 16490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 3);
        createTransaction(ctx, 18390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 4);
        createTransaction(ctx, 19490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 5);
        createTransaction(ctx, 24390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 6);
        createTransaction(ctx, 34490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 7);
        createTransaction(ctx, 44390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 8);        
        createTransaction(ctx, 54490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 9);
        createTransaction(ctx, 64390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 10);       
        createTransaction(ctx, 74490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 11);
        createTransaction(ctx, 84390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 12);       
        createTransaction(ctx, 94490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 13);
        createTransaction(ctx, 104390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 14);      
        createTransaction(ctx, 124490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 15);
        createTransaction(ctx, 134390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 16);
        ContextLocator.setThreadContext(ctx);

//        final TransactionQueryResult result = impl_.listAccountTransactions(header_, "14", start, end, Long.valueOf(11000), "", 10, true);
  //      final TransactionQueryResult result2 = impl_.listAccountTransactions(header_, "14", start, end, Long.valueOf(11000), result.getPageKey(), 10, true);
    //    assertNotNull(result);
      //  assertNotNull(result.getReferences());
        //assertEquals(10, result.getReferences().length);
    /*  This DOESN"T SEEM to work **** 
     * 
     *  System.out.println("KUMARAN *********************************result" + result.getPageKey());
        assertNotNull(result2);
        assertNotNull(result2.getReferences());
        assertEquals(6,result2.getReferences().length);
   */ }

    public void testAllListTransaction() throws HomeException, CRMExceptionFault, IOException, InstantiationException
    {
        final Context ctx = getContext();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start = CalendarSupportHelper.get(ctx).setDayBefore(start);
        short subType =0;        
        end = CalendarSupportHelper.get(ctx).setDayAfter(end);
        
        
        createAccount( ctx,"14", "Test", 1,1);
        createAdjustment( ctx,AdjustmentTypeEnum.StandardPayments_INDEX, 0, "Standard Payment");
        createTransaction(ctx, 14490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 1);
        createTransaction(ctx, 24390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 2);
        createTransaction(ctx, 34490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 3);
        createTransaction(ctx, 54390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 4);
        createTransaction(ctx, 64490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 5);
        createTransaction(ctx, 84390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 6);
        createTransaction(ctx, 94490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 7);
        createTransaction(ctx, 1104390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 8);        
        createTransaction(ctx, 214490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 9);
        createTransaction(ctx, 324390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 10);       
        createTransaction(ctx, 16230, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 11);
        createTransaction(ctx, 17990, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 12);       
        createTransaction(ctx, 18490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 13);
        createTransaction(ctx, 19390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 14);      
        createTransaction(ctx, 234490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 15);
        createTransaction(ctx, 178190, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 16);
        ContextLocator.setThreadContext(ctx);
/*
       // final DetailedTransactionQueryResult result = impl_.listDetailedAccountTransactions(header_, "14", start, end, Long.valueOf(11000), "", 10, true);
        //final DetailedTransactionQueryResult result2 = impl_.listDetailedAccountTransactions(header_, "14", start, end, Long.valueOf(11000), "10", 100, true);
        
        System.out.println(" listTransactions Result1 " + result.getResults().length + " result2 " + result2.getResults().length);
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(10, result.getResults().length);
*/
    }

    public void testCreateAccountTransaction() throws HomeException
    {
        
    }
    
    public void testAllSubscriberListTransaction() throws HomeException, CRMExceptionFault, IOException, InstantiationException
    {
        final Context ctx = getContext();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start = CalendarSupportHelper.get(ctx).setDayBefore(start);
        short subType =0;        
        end = CalendarSupportHelper.get(ctx).setDayAfter(end);
        
        
        createAccount( ctx,"14", "Test", 1,1);
        createSubscriber( ctx,"14","14-1","407220000100",1);
        createAdjustment( ctx,AdjustmentTypeEnum.StandardPayments_INDEX, 0, "Standard Payment");
        createTransaction(ctx, 14490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 1);
        createTransaction(ctx, 24390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 2);
        createTransaction(ctx, 34490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 3);
        createTransaction(ctx, 54390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 4);
        createTransaction(ctx, 64490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 5);
        createTransaction(ctx, 84390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 6);
        createTransaction(ctx, 94490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 7);
        createTransaction(ctx, 1104390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 8);        
        createTransaction(ctx, 214490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 9);
        createTransaction(ctx, 324390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 10);       
        createTransaction(ctx, 16230, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 11);
        createTransaction(ctx, 17990, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 12);       
        createTransaction(ctx, 18490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 13);
        createTransaction(ctx, 19390, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 14);      
        createTransaction(ctx, 234490, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000100" ,new Date(), "14-1", 15);
        createTransaction(ctx, 178190, AdjustmentTypeEnum.StandardPayments_INDEX, "14","407220000200" ,new Date(), "14-2", 16);
        ContextLocator.setThreadContext(ctx);

        SubscriptionReference subRef = new SubscriptionReference();

        subRef.setIdentifier("14-1");
        subRef.setSpid(1);
        subRef.setMobileNumber("407220000200");
        subRef.setMobileNumberOwnership(Calendar.getInstance());
        subRef.setAccountID("14");
        subRef.setState(SubscriptionStateEnum.valueOf(1));
/*
        final DetailedTransactionQueryResult result = impl_.listDetailedSubscriptionTransactions(header_, subRef, start, end, Long.valueOf(11000), "", Integer.valueOf(4), true);
        final DetailedTransactionQueryResult result2 = impl_.listDetailedSubscriptionTransactions(header_, subRef, start, end, Long.valueOf(11000), result.getPageKey(), 4, true);
       
        System.out.println("SIze of result " + result.getResults().length);
        System.out.println("Results 2 " + result2.getResults().length);
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(4, result.getResults().length);
*/
    }
    
   
    
    private void createTransaction(Context ctx, final long amount, final int adjType, final String ban,
                                    final String msisdn, Date receiveDate, String subId, int receiptNum ) throws RemoteException, HomeException
    {
        short subType = 0;
        final Transaction transA;
        try
        {
            transA = (Transaction) XBeans.instantiate(Transaction.class, ctx);
        }
        catch (Exception exception)
        {
            throw new HomeException("Cannot instantiate transaction bean", exception);
        }

        transA.setAmount(amount);
        transA.setSpid(1);
        transA.setAdjustmentType(adjType);
        transA.setAcctNum(ban);
        transA.setBAN(ban);
        transA.setGLCode("1");
        transA.setBalance(amount+100);

        transA.setMSISDN(msisdn);
        transA.setReceiveDate(receiveDate); 
        transA.setSubscriberID(subId);
        transA.setTransDate(receiveDate);
        transA.setTaxPaid(20);
        transA.setReceiptNum(receiptNum);
        transA.setSubscriberType(SubscriberTypeEnum.get(subType));

        home_.create(ctx,transA);
    }
    
    private void createAccount( Context ctx,
                                final String ban,
                                final String AcctName,
                                final int billCycleId,
                                final int spid) throws HomeException, IOException, InstantiationException
    {
        final short subType = 0;
        
        final Account acct = (Account) XBeans.instantiate(Account.class, ctx);
        acct.setBAN(ban);
        acct.setAccountName(AcctName);
        acct.setBillCycleID(1);
        acct.setCurrency("CDN");
        acct.setSpid(1);
        acct.setType(0);
        acct.setState( AccountStateEnum.get(subType));
        
        home1_.create(ctx, acct);
    }
    
    
    private void createSubscriber( Context ctx,
            final String ban,
            final String subId,
            final String msisdn,
            final int spid) throws HomeException, IOException, InstantiationException
    {
        final short subType = 0;
        final short state = 1;
        final Subscriber sub = (Subscriber) XBeans.instantiate(Subscriber.class, ctx);
        sub.setBAN(ban);
        sub.setId(subId);
        sub.setIMSI(msisdn);
        sub.setMSISDN(msisdn);
        sub.setSubscriberType(SubscriberTypeEnum.get(subType));
        sub.setState(SubscriberStateEnum.get(state));
        sub.setSpid(spid);
     
        home2_.create(ctx, sub);
    }

    
    private void createAdjustment( Context ctx,
                                 int adjCode,
                                 int parentCode,
                                 final String adjName)throws HomeException, IOException, InstantiationException
    {   
        final AdjustmentType adj = (AdjustmentType) XBeans.instantiate(AdjustmentType.class, ctx);
        // Set up several AdjustmentTypes for testing.
        adj.setCode(adjCode);
        adj.setParentCode(parentCode);
        adj.setName(adjName);
    
        adjustmentTypeHome.create(ctx, adj);
    }
    
    public static class FakeAuthSPI implements AuthSPI
    {
        public void login(final Context ctx, final String username, final String password) throws LoginException
        {
        }

        public void logout(final Context ctx)
        {
        }

        public boolean checkPermission(final Context ctx, final Principal principal, final Permission permission)
        {
            return true;
        }

        public void updatePassword(final Context ctx, final Principal principal, final String oldPassword,
                final String newPassword) throws IllegalStateException
        {
        }

        public void validatePrincipal(final Context ctx, final Principal oldValue, final Principal newValue)
            throws IllegalStateException
        {
        }

        @Override
        public void release()
        {
  
        }
    }

    private Home home_;
    private Home home1_;
    private AdjustmentTypeHome adjustmentTypeHome;
    private Home home2_;
    private TransactionServiceSkeletonInterface impl_;
    private CRMRequestHeader header_;
}
