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
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailTransientHome;
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
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CallDetailServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.CallDetailQueryResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.DetailedCallDetailQueryResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;

/**
 * Tests for API methods.
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class CallDetailsImplTest extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public CallDetailsImplTest(final String name)
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

        final TestSuite suite = new TestSuite(CallDetailsImplTest.class);

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
        impl_ = (CallDetailServiceSkeletonInterface) QueryExecutorsInvocationHandler.newInstance(ctx, CallDetailServiceSkeletonInterface.class);;
        
        try 
        {
        	//Setup default Service Provider
        	TestSetupAccountHierarchy.setupSpid(getContext());
        }
        catch (HomeException e)
        {
        	throw new IllegalStateException("Cannot continue with test. Failed SPID setup for test.", e);
        }

        home_ = new CallDetailTransientHome(ctx);
        
        ctx.put(CallDetailHome.class, home_);
        
        home1_ = new AccountTransientHome(ctx);
        ctx.put(AccountHome.class, home1_);
        
        home2_ = new SubscriberTransientHome(ctx);
        ctx.put(SubscriberHome.class, home2_);
        home_ = new CallDetailTransientHome(ctx);
        
        ctx.put(CallDetailHome.class, home_);
        
        home1_ = new AccountTransientHome(ctx);
        ctx.put(AccountHome.class, home1_);
        
        home2_ = new SubscriberTransientHome(ctx);
        ctx.put(SubscriberHome.class, home2_);

        /*
        home_ = new CallDetailXDBHome(ctx);
        
        ctx.put(CallDetailHome.class, home_);
        
        home1_ = new AccountXDBHome(ctx);
        ctx.put(AccountHome.class, home1_);
        
        home2_ = new SubscriberXDBHome(ctx);
        ctx.put(SubscriberHome.class, home2_);
        home_ = new CallDetailTransientHome(ctx);
        
        ctx.put(CallDetailHome.class, home_);
        
        home1_ = new AccountXDBHome(ctx);
        ctx.put(AccountHome.class, home1_);
        
        home2_ = new SubscriberXDBHome(ctx);
        ctx.put(SubscriberHome.class, home2_);

        */
        ctx.put(AuthSPI.class, new FakeAuthSPI());
        ctx.put(Principal.class, new User());

        header_ = new CRMRequestHeader();
    }

    public void testListCallDetails() throws HomeException, CRMExceptionFault, IOException, InstantiationException
    {
        final Context ctx = getContext();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start = CalendarSupportHelper.get(ctx).setDayBefore(start);
        short subType =0;        
        end = CalendarSupportHelper.get(ctx).setDayAfter(end);
        
        createAccount( ctx,"14", "Test", 1,1);
        createSubscriber( ctx,"14","14-1","407220000200",1);
      
        createCallDetail(ctx,2,"14","14-1",new Date(),123);
        createCallDetail(ctx,3,"14","14-1",new Date(),312);
        createCallDetail(ctx,4,"14","14-1",new Date(),234);
        createCallDetail(ctx,5,"14","14-1",new Date(),273);
        createCallDetail(ctx,6,"14","14-1",new Date(),728);
        createCallDetail(ctx,7,"14","14-1",new Date(),237);
        createCallDetail(ctx,8,"14","14-1",new Date(),1712);
        createCallDetail(ctx,9,"14","14-1",new Date(),7293);
        createCallDetail(ctx,10,"14","14-1",new Date(),234);
        SubscriptionReference subRef = getSubscriberReference();


        ContextLocator.setThreadContext(ctx);
 
        final CallDetailQueryResponse result = impl_.listCallDetails(header_,subRef, start, end,"", 4, true,null);

        assertNotNull(result);
        assertNotNull(result.getReferences());
        assertEquals(4, result.getReferences().length);
    }
    
    
    public void testAllListCallDetails() throws HomeException, CRMExceptionFault, IOException, InstantiationException
    {
        final Context ctx = getContext();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start = CalendarSupportHelper.get(ctx).setDayBefore(start);
        short subType =0;        
        end = CalendarSupportHelper.get(ctx).setDayAfter(end);
        
        createAccount( ctx,"14", "Test", 1,1);
        createSubscriber( ctx,"14","14-1","407220000200",1);
        
        
        createCallDetail(ctx,2,"14","14-1",new Date(),123);
        createCallDetail(ctx,3,"14","14-1",new Date(),312);
        createCallDetail(ctx,4,"14","14-1",new Date(),234);
        createCallDetail(ctx,5,"14","14-1",new Date(),273);
        createCallDetail(ctx,6,"14","14-1",new Date(),728);
        createCallDetail(ctx,7,"14","14-1",new Date(),237);
        createCallDetail(ctx,8,"14","14-1",new Date(),1712);
        createCallDetail(ctx,9,"14","14-1",new Date(),7293);
        createCallDetail(ctx,10,"14","14-1",new Date(),234);
        SubscriptionReference subRef = getSubscriberReference();


        ContextLocator.setThreadContext(ctx);
 
        final DetailedCallDetailQueryResponse result = impl_.listDetailedCallDetails(header_,subRef, start, end,"", Integer.valueOf(10), true, null);

        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(9, result.getResults().length);
    }

    public SubscriptionReference getSubscriberReference()
    {
        SubscriptionReference subRef = new SubscriptionReference();

        subRef.setIdentifier("14-1");
        subRef.setSpid(1);
        subRef.setMobileNumber("407220000200");
        subRef.setMobileNumberOwnership(Calendar.getInstance());
        subRef.setAccountID("14");
        subRef.setState(SubscriptionStateEnum.valueOf(1));
        return subRef;
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

    /**
     * Creates a call detail record and asserts that it is indeed created
     * 
     * @param ctx
     * @throws HomeException
     * @throws InstantiationException 
     * @throws IOException 
     */
    private long createCallDetail(Context ctx,long id, String ban,String subId,Date date, long amount) 
        throws HomeException, IOException, InstantiationException 
    {
        CallDetail cd=(CallDetail) XBeans.instantiate(CallDetail.class, ctx);
        cd.setId(id);
        cd.setBAN(ban);
        cd.setSubscriberID(subId);
        cd.setOrigMSISDN("407220000200");
        cd.setChargedMSISDN("407220000200");
        
        cd.setPostedDate(date);
        cd.setTranDate(date);
        cd.setCharge(amount);
        

        CallDetail cdOut=(CallDetail) home_.create(ctx,cd);
    
        return cd.getId();
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
    private Home home2_;
    private CallDetailServiceSkeletonInterface impl_;
    private CRMRequestHeader header_;
}
