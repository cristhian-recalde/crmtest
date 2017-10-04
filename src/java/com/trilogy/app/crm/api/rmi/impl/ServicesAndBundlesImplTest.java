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
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
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
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanReference;


/**
 * Tests for API methods.
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class ServicesAndBundlesImplTest extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public ServicesAndBundlesImplTest(final String name)
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

        final TestSuite suite = new TestSuite(ServicesAndBundlesImplTest.class);

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
        try
        {
            impl_ = new ServicesAndBundlesImpl(ctx);
        }
        catch (RemoteException e)
        {
            throw new IllegalStateException("cannot continue with test", e);
        }
        
        try 
        {
        	//Setup default Service Provider
        	TestSetupAccountHierarchy.setupSpid(getContext());
        }
        catch (HomeException e)
        {
        	throw new IllegalStateException("Cannot continue with test. Failed SPID setup for test.", e);
        }

        home_ = new PricePlanTransientHome(ctx);
        
        ctx.put(PricePlanHome.class, home_);
        
        home3_ = new PricePlanVersionTransientHome(ctx);
        
        ctx.put(PricePlanVersionHome.class, home3_);
        
        home1_ = new AccountTransientHome(ctx);
        
        ctx.put(AccountHome.class, home1_);
        
        home2_ = new SubscriberTransientHome(ctx);
        
        ctx.put(SubscriberHome.class, home2_);
        
        
        ctx.put(AuthSPI.class, new FakeAuthSPI());
        ctx.put(Principal.class, new User());

        header_ = new CRMRequestHeader();

    }




    public void testListPricePlanTransaction() throws HomeException, CRMExceptionFault, IOException, InstantiationException
    {
        final Context ctx = getContext();

        Date start = new Date();
        Date end = new Date();
        start = CalendarSupportHelper.get(ctx).getDayBefore(start);   
        end = CalendarSupportHelper.get(ctx).getDayAfter(end);
        
        
        createAccount( ctx,"14", "Test", 1,1);
        createSubscriber( ctx,"14","14-1","407220000100",1);
        createPricePlan(ctx,1,"PP1",1);
        createPricePlanVersion(ctx,1,1);
        createPricePlan(ctx,2,"PP1",1);
        createPricePlanVersion(ctx,2,1);
        createPricePlan(ctx,3,"PP1",1);
        createPricePlanVersion(ctx,3,1);
        createPricePlan(ctx,4,"PP1",1);
        createPricePlanVersion(ctx,4,1);
        createPricePlan(ctx,5,"PP1",1);
        createPricePlanVersion(ctx,5,5);
        createPricePlan(ctx,6,"PP1",1);
        createPricePlanVersion(ctx,6,1);
        createPricePlan(ctx,7,"PP1",1);
        createPricePlanVersion(ctx,7,1);
        createPricePlan(ctx,8,"PP1",1);
        createPricePlanVersion(ctx,8,1);
        createPricePlan(ctx,10,"PP1",1);
        createPricePlanVersion(ctx,10,1);
        
        
        ContextLocator.setThreadContext(ctx);

        final PricePlanReference[] result = impl_.listPricePlans(header_, 1,PaidTypeEnum.POSTPAID.getValue(), null, true,null);
      

        assertNotNull(result);
        System.out.println("Size of result " + result.length);
        assertEquals(9, result.length);
        int stversion = result[0].getCurrentVersion().intValue();
        assertEquals(1,stversion);
        

    }

    public void testGetAllPricePlanTransaction() throws HomeException, CRMExceptionFault, IOException, InstantiationException
    {
        final Context ctx = getContext();

        Date start = new Date();
        Date end = new Date();
        start = CalendarSupportHelper.get(ctx).getDayBefore(start);
        end = CalendarSupportHelper.get(ctx).getDayAfter(end);
        
        
        createAccount( ctx,"14", "Test", 1,1);
        createSubscriber( ctx,"14","14-1","407220000100",1);
        createPricePlan(ctx,1,"PP1",1);
        createPricePlanVersion(ctx,1,1);
        
        createPricePlan(ctx,2,"PP2",1);
        createPricePlanVersion(ctx,2,1);
        
        createPricePlan(ctx,3,"PP3",1);
        createPricePlanVersion(ctx,3,1);
        
        createPricePlan(ctx,4,"PP4",1);
        createPricePlanVersion(ctx,4,1);
        
        createPricePlan(ctx,5,"PP5",5);
        createPricePlanVersion(ctx,5,5);
        
        createPricePlan(ctx,6,"PP6",1);
        createPricePlanVersion(ctx,6,1);
        
        createPricePlan(ctx,7,"PP7",1);
        createPricePlanVersion(ctx,7,1);
        
        createPricePlan(ctx,8,"PP8",1);
        createPricePlanVersion(ctx,8,1);
        createPricePlan(ctx, 10, "PP10", 1);
        createPricePlanVersion(ctx, 10, 1);
        ContextLocator.setThreadContext(ctx);
        final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan[] result = impl_
                .listDetailedPricePlans(header_, 1, PaidTypeEnum.POSTPAID.getValue(), null, true, null);
        assertNotNull(result);
        System.out.println("Size of result " + result.length);
        assertEquals(9, result.length);
        int stversion = result[0].getCurrentVersion().intValue();
        assertEquals(1,stversion);
        

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

    
    private void createPricePlan( Context ctx,
                                  int id,
                                  String name,
                                  int version)throws HomeException, IOException, InstantiationException
    {   
        final PricePlan pp = (PricePlan) XBeans.instantiate(PricePlan.class, ctx);
        // Set up several AdjustmentTypes for testing.
        short subType = 0;
        pp.setCurrentVersion(version);
        pp.setId(id);
        pp.setName(name);
        
        pp.setPricePlanType( SubscriberTypeEnum.get(subType)); 
        pp.setEnabled(true);
        pp.setSpid(1);
    
        home_.create(ctx, pp);
    }
    
    
    private void createPricePlanVersion( Context ctx,
                                         int id,
                                         int version)throws HomeException, IOException, InstantiationException
    {   
        final PricePlanVersion ppv = (PricePlanVersion) XBeans.instantiate(PricePlanVersion.class, ctx);
        // Set up several AdjustmentTypes for testing.
        ppv.setActivation(new Date());
        ppv.setId(id);
        ppv.setVersion(version);
        
        home3_.create(ctx, ppv);

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
    private Home home3_;
    private Home home2_;
    private ServicesAndBundlesImpl impl_;
    private CRMRequestHeader header_;
}
