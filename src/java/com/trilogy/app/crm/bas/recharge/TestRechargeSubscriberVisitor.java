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

package com.trilogy.app.crm.bas.recharge;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.RecurringRecharge;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.core.agent.BeanFactoryInstall;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * JUnit/XTest test case for {@link RechargeSubscriberAuxServiceVisitor}.
 *
 * @author cindy.wong@redknee.com
 * @since 30-Apr-08
 */
public class TestRechargeSubscriberVisitor extends RechargeItemVisitorTestCase
{

    /**
     * Auxiliary service ID to use.
     */
    public static final long AUXILIARY_SERVICE_ID = 10;
    /**
     * VPN MSISDN.
     */
    public static final String VPN_MSISDN = "3904839213";

    /**
     * VPN group leader subscriber ID.
     */
    public static final String VPN_LEADER_ID = "46821-1";

    /**
     * VPN auxiliary service ID.
     */
    public static final long VPN_AUXILIARY_SERVICE_ID = 68;


    /**
     * Create a new instance of <code>TestRechargeSubscriberAuxServiceVisitor</code>.
     *
     * @param name
     *            Name of the test case.
     */
    public TestRechargeSubscriberVisitor(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     *
     * @return A new suite of Tests for execution
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
     *            The operating context
     * @return A new suite of Tests for execution
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestRechargeSubscriberVisitor.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    protected void setUp()
    {
        super.setUp();

        initSubscriberAuxSrv();
        initAuxiliaryService();
        
        (new com.redknee.app.crm.core.agent.BeanFactoryInstall()).execute(getContext());
        
        visitor_ = new RechargeSubscriberVisitor(BILLING_DATE, AGENT_NAME, ChargingCycleEnum.MONTHLY, 
                BILLING_DATE, BillCycleSupport.getDateOfBillCycleLastDay(CalendarSupportHelper.get(getContext()).findBillingDayOfMonth(BILLING_DATE), BILLING_DATE), true, true, false);
    }


    /**
     * Initialize VPN-related entities.
     */
    private void initSubscriberAuxSrv()
    {
        final Home home = new SubscriberAuxiliaryServiceTransientHome(getContext());

        getContext().put(SubscriberAuxiliaryServiceHome.class, home);
    }


    /**
     * Initialize AuxiliaryService.
     */
    private void initAuxiliaryService()
    {
        final Home home = new AuxiliaryServiceTransientHome(getContext());

        AuxiliaryService auxiliaryService = new AuxiliaryService();
        auxiliaryService.setIdentifier(AUXILIARY_SERVICE_ID);
        auxiliaryService.setActivationFee(ActivationFeeModeEnum.PRORATE);
        auxiliaryService.setAdjustmentType(ADJUSTMENT_TYPE_ID);
        auxiliaryService.setAdjustmentTypeDescription("Monthly auxiliary service adjustment type");
        auxiliaryService.setCharge(1000);
        auxiliaryService.setChargingModeType(ServicePeriodEnum.MONTHLY);
        auxiliaryService.setGLCode(GL_CODE);
        auxiliaryService.setName("Monthly auxiliary service");
        auxiliaryService.setSpid(SPID);
        auxiliaryService.setState(AuxiliaryServiceStateEnum.ACTIVE);
        auxiliaryService.setSubscriberType(SubscriberTypeEnum.HYBRID);
        auxiliaryService.setTaxAuthority(TAX_AUTHORITY_ID);
        auxiliaryService.setTechnology(TechnologyEnum.ANY);
        auxiliaryService.setType(AuxiliaryServiceTypeEnum.Basic);

        try
        {
            auxiliaryService = (AuxiliaryService) home.create(getContext(), auxiliaryService);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing AuxiliaryService");
        }
        getContext().put(AuxiliaryServiceHome.class, home);
    }

    private SubscriberAuxiliaryService createAssociation()
    {
        final SubscriberAuxiliaryService association = new SubscriberAuxiliaryService();
        association.setAuxiliaryServiceIdentifier(AUXILIARY_SERVICE_ID);
        association.setContext(getContext());
        association.setCreated(SUBSCRIBER_START_DATE);
        association.setEndDate(CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE));
        association.setIdentifier(AUXILIARY_SERVICE_ID);
        association.setProvisioned(true);
        association.setStartDate(SUBSCRIBER_START_DATE);
        association.setSubscriberIdentifier(SUBSCRIBER_ID);
        association.setType(AuxiliaryServiceTypeEnum.Basic);
        return association;
    }

    /**
     * Test
     * {@link RechargeSubscriberVisitor#applyAuxServicesAdjustment(Context, Subscriber, boolean)}
     * when the subscriber does not have the provided auxiliary service.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testApplyAuxServicesAdjustmentNotSubscribed() throws HomeException
    {
        final Context ctx = getContext();

        visitor_.applyAuxServicesAdjustment(ctx, getSubscriber(), 1, new HashMap<Class,List<Object>>(), new RecurringRecharge(), new HashMap<Class,List<Object>>());

        Collection<Transaction> transactions = HomeSupportHelper.get(ctx).getBeans(ctx, Transaction.class);

        assertNotNull("Result should be an empty Collection", transactions);
        assertEquals("Result should be an empty Collection", 0, transactions.size());
    }

    /**
     * Test
     * {@link RechargeSubscriberVisitor#applyAuxServicesAdjustment(Context, Subscriber, boolean)}
     * when the subscriber has the provided auxiliary service and it has not yet been charged.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testApplyAuxServicesAdjustmentSubscribed() throws HomeException
    {
        final Context ctx = getContext();

        final SubscriberAuxiliaryService association = createAssociation();
        HomeSupportHelper.get(ctx).createBean(ctx, association);

        visitor_.applyAuxServicesAdjustment(ctx, getSubscriber(), 1, new HashMap<Class,List<Object>>(), new RecurringRecharge(), new HashMap<Class,List<Object>>());

        final AuxiliaryService service = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, AUXILIARY_SERVICE_ID);
        final Collection<Transaction> transactions = HomeSupportHelper.get(ctx).getBeans(ctx, Transaction.class);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        final Transaction transaction = transactions.iterator().next();
        assertEquals(service.getCharge(), transaction.getAmount());
        assertEquals(service.getAdjustmentType(), transaction.getAdjustmentType());
    }


    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service and it has not yet been
     * charged, but the auxiliary service is charged weekly while the monthly recurring
     * charge is being generated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testApplyAuxServicesAdjustmentSubscribedWeekly() throws HomeException
    {
        final Context ctx = getContext();

        final SubscriberAuxiliaryService association = createAssociation();
        HomeSupportHelper.get(ctx).createBean(ctx, association);

        final AuxiliaryService service = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, AUXILIARY_SERVICE_ID);
        service.setChargingModeType(ServicePeriodEnum.WEEKLY);
        HomeSupportHelper.get(ctx).storeBean(ctx, service);

        visitor_ = new RechargeSubscriberVisitor(BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY, BILLING_DATE,
                BillCycleSupport.getLastDayOfBillingWeek(BILLING_DATE, BillCycleSupport.computeBillingDayOfWeek(BILLING_DATE)), true, true, false);

        visitor_.applyAuxServicesAdjustment(getContext(), getSubscriber(), 1, new HashMap<Class,List<Object>>(), new RecurringRecharge(), new HashMap<Class,List<Object>>());

        final Collection<Transaction> transactions = HomeSupportHelper.get(ctx).getBeans(ctx, Transaction.class);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        final Transaction transaction = transactions.iterator().next();
        assertEquals(service.getCharge(), transaction.getAmount());
        assertEquals(service.getAdjustmentType(), transaction.getAdjustmentType());
    }


    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service and it has not yet been
     * charged, but the auxiliary service is an one-time charge while the monthly
     * recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testApplyAuxServicesAdjustmentSubscribedOneTime() throws HomeException
    {
        final Context ctx = getContext();

        final SubscriberAuxiliaryService association = createAssociation();
        HomeSupportHelper.get(ctx).createBean(ctx, association);

        final AuxiliaryService service = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, AUXILIARY_SERVICE_ID);
        service.setChargingModeType(ServicePeriodEnum.ONE_TIME);
        HomeSupportHelper.get(ctx).storeBean(ctx, service);

        visitor_.applyAuxServicesAdjustment(getContext(), getSubscriber(), 1, new HashMap<Class,List<Object>>(), new RecurringRecharge(), new HashMap<Class,List<Object>>());

        final Collection<Transaction> transactions = HomeSupportHelper.get(ctx).getBeans(ctx, Transaction.class);

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }


    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service and it has not yet been
     * charged, but the auxiliary service is an one-time charge while the weekly recurring
     * charge is being generated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testApplyAuxServicesAdjustmentSubscribedWeeklyOneTime() throws HomeException
    {
        final Context ctx = getContext();

        final SubscriberAuxiliaryService association = createAssociation();
        HomeSupportHelper.get(ctx).createBean(ctx, association);

        final AuxiliaryService service = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, AUXILIARY_SERVICE_ID);
        service.setChargingModeType(ServicePeriodEnum.ONE_TIME);
        HomeSupportHelper.get(ctx).storeBean(ctx, service);

        visitor_ = new RechargeSubscriberVisitor(BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY, 
                BILLING_DATE, BillCycleSupport.getLastDayOfBillingWeek(BILLING_DATE, 
                        BillCycleSupport.computeBillingDayOfWeek(BILLING_DATE)), true, true, false);

        visitor_.applyAuxServicesAdjustment(getContext(), getSubscriber(), 1, new HashMap<Class,List<Object>>(), new RecurringRecharge(), new HashMap<Class,List<Object>>());

        final Collection<Transaction> transactions = HomeSupportHelper.get(ctx).getBeans(ctx, Transaction.class);

        assertNotNull(transactions);
        assertEquals(0, transactions.size());
    }


    /**
     * Visitor to use.
     */
    private RechargeSubscriberVisitor visitor_;

}
