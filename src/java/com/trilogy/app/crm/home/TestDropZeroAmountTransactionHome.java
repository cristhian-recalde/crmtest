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

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AbstractHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryTransientHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * A suite of test cases for DropZeroAmountTransactionHome.
 *
 * @author vincci.cheng@redknee.com
 */
public class TestDropZeroAmountTransactionHome extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestDropZeroAmountTransactionHome(final String name)
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
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestDropZeroAmountTransactionHome.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();

        SysFeatureCfg sysCfg = new SysFeatureCfg();
        sysCfg.setDropZeroAmountTransaction(true);

        getContext().put(SysFeatureCfg.class, sysCfg);
    }


    /**
     * {@inheritDoc}
     */
    public void tearDown()
    {
        super.tearDown();
    }


    /**
     * Tests that the proxy home will not store zero amount transactions
     */
    public void testZeroAmountTransaction_prepaid() throws HomeException
    {
        FakeClientHome fakeHome = new FakeClientHome();
        Home home = new DropZeroAmountTransactionHome(fakeHome);

        final Calendar cal = Calendar.getInstance();
        final Date today = CalendarSupportHelper.get(getContext()).clearTimeOfDay(cal).getTime();

        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, getContext());
        }
        catch (Exception exception)
        {
            throw new HomeException("Cannot instantiate transaction bean", exception);
        }

        transaction.setReceiptNum(9999);
        transaction.setAmount(0L);
        transaction.setMSISDN("123456789");
        transaction.setSubscriptionTypeId(SubscriptionType.getINSubscriptionType(getContext()).getId());

        Msisdn msisdn = new Msisdn();
        msisdn.setMsisdn("123456789");
        msisdn.setSpid(1);
        msisdn.setBAN("1234");
        msisdn.setSubscriberType(SubscriberTypeEnum.PREPAID);

        final MsisdnMgmtHistory msisdnHistory = new MsisdnMgmtHistory();
        msisdnHistory.setTerminalId("123456789");
        msisdnHistory.setBAN("1234");
        msisdnHistory.setSubscriberId("1234-5");
        msisdnHistory.setEvent(HistoryEventSupport.SUBID_MOD);
        msisdnHistory.setSubscriptionType(SubscriptionType.getINSubscriptionType(getContext()).getId());
        msisdnHistory.setTimestamp(today);

        Subscriber sub = new Subscriber();
        sub.setId("1234-5");
        sub.setBAN("1234");
        sub.setSubscriberType(SubscriberTypeEnum.PREPAID);
        
        getContext().put(MsisdnHome.class, new FakeHome(getContext(), msisdn));
        final Home msisdnHistoryhome = new MsisdnMgmtHistoryTransientHome(getContext());
        msisdnHistoryhome.create(getContext(), msisdnHistory);
        getContext().put(MsisdnMgmtHistoryHome.class, msisdnHistoryhome);
        getContext().put(SubscriberHome.class, new FakeHome(getContext(), sub));

        home.create(getContext(), transaction);

        assertEquals(0, fakeHome.createCalls);
    }


    /**
     * Tests that the proxy home will not store zero amount transactions
     */
    public void testZeroAmountTransaction_postpaid() throws HomeException
    {
        FakeClientHome fakeHome = new FakeClientHome();
        Home home = new DropZeroAmountTransactionHome(fakeHome);

        final Calendar cal = Calendar.getInstance();
        final Date today = CalendarSupportHelper.get(getContext()).clearTimeOfDay(cal).getTime();

        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, getContext());
        }
        catch (Exception exception)
        {
            throw new HomeException("Cannot instantiate transaction bean", exception);
        }

        transaction.setReceiptNum(9999);
        transaction.setAmount(0L);
        transaction.setMSISDN("123456789");
        transaction.setSubscriptionTypeId(SubscriptionType.getINSubscriptionType(getContext()).getId());

        Msisdn msisdn = new Msisdn();
        msisdn.setMsisdn("123456789");
        msisdn.setSpid(1);
        msisdn.setBAN("1234");
        msisdn.setSubscriberType(SubscriberTypeEnum.POSTPAID);

        final MsisdnMgmtHistory msisdnHistory = new MsisdnMgmtHistory();
        msisdnHistory.setTerminalId("123456789");
        msisdnHistory.setBAN("1234");
        msisdnHistory.setSubscriberId("1234-5");
        msisdnHistory.setEvent(HistoryEventSupport.SUBID_MOD);
        msisdnHistory.setSubscriptionType(SubscriptionType.getINSubscriptionType(getContext()).getId());
        msisdnHistory.setTimestamp(today);

        Subscriber sub = new Subscriber();
        sub.setId("1234-5");
        sub.setBAN("1234");
        sub.setSubscriberType(SubscriberTypeEnum.POSTPAID);

        getContext().put(MsisdnHome.class, new FakeHome(getContext(), msisdn));
        final Home msisdnHistoryhome = new MsisdnMgmtHistoryTransientHome(getContext());
        msisdnHistoryhome.create(getContext(), msisdnHistory);
        getContext().put(MsisdnMgmtHistoryHome.class, msisdnHistoryhome);
        getContext().put(SubscriberHome.class, new FakeHome(getContext(), sub));

        home.create(getContext(), transaction);

        assertEquals(1, fakeHome.createCalls);
    }


    /**
     * Tests that the proxy home will store non-zero amount transactions
     *
     * @throws HomeException Thrown if there is an unanticipated exception raied while
     *                       accessing Home data in the context.
     */
    public void testNonZeroAmountTransaction() throws HomeException
    {
        FakeClientHome fakeHome = new FakeClientHome();
        Home home = new DropZeroAmountTransactionHome(fakeHome);

        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, getContext());
        }
        catch (Exception exception)
        {
            throw new HomeException("Cannot instantiate transaction bean", exception);
        }

        transaction.setReceiptNum(9999);
        transaction.setAmount(1L);

        home.create(getContext(), transaction);

        assertEquals(1, fakeHome.createCalls);
    }
}

class FakeClientHome implements Home
{
    public int createCalls = 0;

    public Object create(Object object) throws HomeException, HomeInternalException
    {
        return create(getContext(), object);
    }

    public Object store(Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Object find(Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Collection select(Object object) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
        return null;
    }

    public void remove(Object object) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public void removeAll(Object object) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public Visitor forEach(Visitor visitor, Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public void drop() throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public Object cmd(Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Object findByPrimaryKey(Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Object findByPrimaryKey(Context context, Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Collection selectAll(Context context) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
        return null;
    }

    public Collection selectAll() throws HomeException, HomeInternalException, UnsupportedOperationException
    {
        return null;
    }

    public void removeAll(Context context) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public void removeAll() throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public Visitor forEach(Context context, Visitor visitor) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Visitor forEach(Visitor visitor) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Home where(Context context, Object object)
    {
        return null;
    }

    public Object create(Context context, Object object) throws HomeException, HomeInternalException
    {
        createCalls++;
        return null;
    }

    public Object store(Context context, Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Object find(Context context, Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Collection select(Context context, Object object) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
        return null;
    }

    public void remove(Context context, Object object) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public void removeAll(Context context, Object object) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public Visitor forEach(Context context, Visitor visitor, Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public void drop(Context context) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public Object cmd(Context context, Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Context getContext()
    {
        return null;
    }

    public void setContext(Context context)
    {
    }
}

class FakeHome extends AbstractHome
{
    Object obj;

    public FakeHome(Context ctx, Object obj)
    {
        super(ctx);
        this.obj = obj;
    }

    public Object create(Context context, Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Object store(Context context, Object object) throws HomeException, HomeInternalException
    {
        return null;
    }

    public Object find(Context context, Object object) throws HomeException, HomeInternalException
    {
        return obj;
    }

    public Collection select(Context context, Object object) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
        return null;
    }

    public void remove(Context context, Object object) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public void removeAll(Context context, Object object) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
    }

    public Visitor forEach(Context context, Visitor visitor, Object object) throws HomeException, HomeInternalException
    {
        return null;
    }
}
