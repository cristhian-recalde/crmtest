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
package com.trilogy.app.crm.provision.soap;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionID;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryTransientHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;

import electric.util.holder.intOut;

/**
 * Tests PricePlanProvisionImpl methods.
 *
 * @author victor.stratan@redknee.com
 */
public class TestPricePlanProvisionImpl extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestPricePlanProvisionImpl(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        return new TestSuite(TestPricePlanProvisionImpl.class);
    }

    /**
     * {@inheritDoc}
     */
    protected void setUp()
    {
        super.setUp();

        (new com.redknee.app.crm.core.agent.BeanFactoryInstall()).execute(getContext());

        final Context ctx = getContext();
        impl = new PricePlanProvisionImpl(ctx);

        final Calendar cal = Calendar.getInstance();
        CalendarSupportHelper.get(ctx).clearTimeOfDay(cal);
        today_ = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);

        START_DATE = cal.getTime();

        cal.add(Calendar.YEAR, 1);
        END_DATE = cal.getTime();

        result = new intOut();

        Home msisdnHome = new MsisdnTransientHome(ctx);
        msisdnHome = new AdapterHome(
                ctx, 
                msisdnHome, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.Msisdn, com.redknee.app.crm.bean.core.Msisdn>(
                        com.redknee.app.crm.bean.Msisdn.class, 
                        com.redknee.app.crm.bean.core.Msisdn.class));
        ctx.put(MsisdnHome.class, msisdnHome);
        final Msisdn msisdn = new Msisdn();
        msisdn.setMsisdn(MSISDN);
        msisdn.setSpid(SPID);
        msisdn.setBAN(SUBSCRIBER_ID);
        msisdn.setTechnology(TechnologyEnum.GSM);
        msisdn.setState(MsisdnStateEnum.IN_USE);

        Home msisdnHistoryHome = new MsisdnMgmtHistoryTransientHome(ctx);
        msisdnHistoryHome = new AdapterHome(
                ctx, 
                msisdnHistoryHome, 
                new ExtendedBeanAdapter<com.redknee.app.crm.numbermgn.MsisdnMgmtHistory, com.redknee.app.crm.bean.core.MsisdnMgmtHistory>(
                        com.redknee.app.crm.numbermgn.MsisdnMgmtHistory.class, 
                        com.redknee.app.crm.bean.core.MsisdnMgmtHistory.class));
        ctx.put(MsisdnMgmtHistoryHome.class, msisdnHistoryHome);
        final MsisdnMgmtHistory msisdnHistory = new MsisdnMgmtHistory();
        msisdnHistory.setTerminalId(MSISDN);
        msisdnHistory.setBAN(SUBSCRIBER_ID);
        msisdnHistory.setSubscriberId(SUBSCRIBER_ID);
        try
        {
            msisdnHistory.setSubscriptionType(SubscriptionType.getINSubscriptionType(ctx).getId());
        }
        catch (HomeException e)
        {
            fail(e.getMessage());
        }
        msisdnHistory.setEvent(HistoryEventSupport.SUBID_MOD);
        msisdnHistory.setTimestamp(today_);

        subHome = new SubscriberTransientHome(ctx);
        ctx.put(SubscriberHome.class, subHome);
        final Subscriber sub = new Subscriber();
        sub.setId(SUBSCRIBER_ID);
        sub.setSpid(SPID);
        sub.setTechnology(TechnologyEnum.GSM);
        sub.setState(SubscriberStateEnum.ACTIVE);

        ppHome = new PricePlanTransientHome(ctx);
        ppHome = new AdapterHome(
                ctx, 
                ppHome, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
        ctx.put(PricePlanHome.class, ppHome);
        final PricePlan pp = new PricePlan();
        pp.setId(PP_ID);
        pp.setSpid(SPID);
        pp.setTechnology(TechnologyEnum.GSM);
        pp.setEnabled(true);

        ppvHome = new PricePlanVersionTransientHome(ctx);
        ppvHome = new AdapterHome(
                ctx, 
                ppvHome, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                        com.redknee.app.crm.bean.PricePlanVersion.class, 
                        com.redknee.app.crm.bean.core.PricePlanVersion.class));
        ctx.put(PricePlanVersionHome.class, ppvHome);
        final PricePlanVersion ppv = new PricePlanVersion();
        ppv.setId(PP_ID);
        ppv.setVersion(PPV_ID);
        ppv.setDeposit(10);

        try
        {
            msisdnHome.create(ctx, msisdn);
            msisdnHistoryHome.create(ctx, msisdnHistory);
            subHome.create(ctx, sub);
            ppHome.create(ctx, pp);
            ppvHome.create(ctx, ppv);
        }
        catch (HomeException e)
        {
            initResult = false;
            return;
        }
    }

    public void testProvisionSecondaryPricePlanFailNoMsisdn() throws SoapServiceException
    {
        assertTrue(initResult);

        impl.provisionSecondaryPricePlan(MSISDN + "8", PP_ID, START_DATE, END_DATE, result);

        assertEquals(PricePlanProvisionInterface.INVALID_MSISDN, result.value);
    }

    public void testProvisionSecondaryPricePlanFailNoSubscriber() throws SoapServiceException, HomeException
    {
        assertTrue(initResult);

        final Object sub = subHome.find(getContext(), SUBSCRIBER_ID);
        subHome.remove(getContext(), sub);

        impl.provisionSecondaryPricePlan(MSISDN, PP_ID, START_DATE, END_DATE, result);

        assertEquals(PricePlanProvisionInterface.INVALID_MSISDN, result.value);
    }

    public void testProvisionSecondaryPricePlanFailInvalidSubscriberState() throws SoapServiceException, HomeException
    {
        assertTrue(initResult);

        final Subscriber sub = (Subscriber) subHome.find(getContext(), SUBSCRIBER_ID);
        sub.setState(SubscriberStateEnum.INACTIVE);
        subHome.store(getContext(), sub);

        impl.provisionSecondaryPricePlan(MSISDN, PP_ID, START_DATE, END_DATE, result);

        assertEquals(PricePlanProvisionInterface.INVALID_SUBSCRIBER_STATE, result.value);
    }

    public void testProvisionSecondaryPricePlanFailNonExistentPP() throws SoapServiceException, HomeException
    {
        assertTrue(initResult);

        final Object pp = ppHome.find(getContext(), Long.valueOf(PP_ID));
        ppHome.remove(getContext(), pp);

        impl.provisionSecondaryPricePlan(MSISDN, PP_ID, START_DATE, END_DATE, result);

        assertEquals(PricePlanProvisionInterface.INVALID_PRICE_PLAN, result.value);
    }

    public void testProvisionSecondaryPricePlanFailPPWrongSPID() throws SoapServiceException, HomeException
    {
        assertTrue(initResult);

        final PricePlan pp = (PricePlan) ppHome.find(getContext(), Long.valueOf(PP_ID));
        pp.setSpid(SPID + 1);
        ppHome.store(getContext(), pp);

        impl.provisionSecondaryPricePlan(MSISDN, PP_ID, START_DATE, END_DATE, result);

        assertEquals(PricePlanProvisionInterface.INVALID_PRICE_PLAN, result.value);
    }

    public void testProvisionSecondaryPricePlanFailPPWrongTechnology() throws SoapServiceException, HomeException
    {
        assertTrue(initResult);

        final PricePlan pp = (PricePlan) ppHome.find(getContext(), Long.valueOf(PP_ID));
        pp.setTechnology(TechnologyEnum.TDMA);
        ppHome.store(getContext(), pp);

        impl.provisionSecondaryPricePlan(MSISDN, PP_ID, START_DATE, END_DATE, result);

        assertEquals(PricePlanProvisionInterface.INVALID_PRICE_PLAN, result.value);
    }

    public void testProvisionSecondaryPricePlanFailPPDisabled() throws SoapServiceException, HomeException
    {
        assertTrue(initResult);

        final PricePlan pp = (PricePlan) ppHome.find(getContext(), Long.valueOf(PP_ID));
        pp.setTechnology(TechnologyEnum.TDMA);
        ppHome.store(getContext(), pp);

        impl.provisionSecondaryPricePlan(MSISDN, PP_ID, START_DATE, END_DATE, result);

        assertEquals(PricePlanProvisionInterface.INVALID_PRICE_PLAN, result.value);
    }

    public void testProvisionSecondaryPricePlanFailPPNoVersion() throws SoapServiceException, HomeException
    {
        assertTrue(initResult);

        final Object ppv = ppvHome.find(getContext(), new PricePlanVersionID(PP_ID, PPV_ID));
        ppvHome.remove(getContext(), ppv);

        impl.provisionSecondaryPricePlan(MSISDN, PP_ID, START_DATE, END_DATE, result);

        assertEquals(PricePlanProvisionInterface.INVALID_PRICE_PLAN, result.value);
    }

    private PricePlanProvisionImpl impl;

    private Home subHome;
    private Home ppHome;
    private Home ppvHome;

    private static final String MSISDN = "9056252111";
    private static final int SPID = 2;
    private static final String SUBSCRIBER_ID = "22-11";
    private static final int PP_ID = 21;
    private static final int PPV_ID = 1;

    private Date today_ = null;
    private Date START_DATE = new Date();
    private Date END_DATE = new Date();

    private intOut result;

    private boolean initResult = true;
}
