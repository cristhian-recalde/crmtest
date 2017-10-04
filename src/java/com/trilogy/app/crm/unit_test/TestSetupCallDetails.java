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
package com.trilogy.app.crm.unit_test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeCmdEnum;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.util.time.Time;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.calldetail.BillingCategoryEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailTransientHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailXDBHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.utils.TransientHomeXDBCmdEmulator;
import com.trilogy.app.crm.xhome.adapter.TransientFieldResetAdapter;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * Unit test sets up the some call details for the account in TestSetupAccountHierarchy, 
 * for use in general unit testing.
 * 
 * Installs the XDBHome depending on the parameter "installXDB" in the setup(ctx, boolean) 
 * method.  By default installs transient homes to run unit tests.
 * 
 * This test is meant to be run offline.
 * @author Angie Li 
 *
 */
public class TestSetupCallDetails extends ContextAwareTestCase
{

    public TestSetupCallDetails(String name)
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
        final TestSuite suite = new TestSuite(TestSetupCallDetails.class);
        return suite;
    }


    /**
     * By default, install the transient homes.
     */
    @Override
    public void setUp()
    {
        super.setUp();
        setup(getContext(), false, true);
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
     * Creates account in an account hierarchy
     * @param ctx
     * @param installFakeCalls TODO
     */
    public static void setup(Context ctx, final boolean installXDB, boolean installFakeCalls)
    {
        if (ctx.getBoolean(TestSetupCallDetails.class, true))
        {
            setupCallDetailHome(ctx, installXDB);
            setupTypes();
            idCounter_ = 0;
            transdate = origTransdate;

            if(installFakeCalls)
            {
            	setupFakeCallDetails(ctx);
            }
            
            //Avoid overwriting the setup in multiple calls to do this setup.
            ctx.put(TestSetupCallDetails.class, false);
        }
        else
        {
            LogSupport.debug(ctx, TestSetupCallDetails.class.getName(), "Skipping TestSetupCallDetails setup again.");
        }
    }

    /**
     * Setup Face Call Details for the Test Account Hierarchy.
     * @param ctx
     */
    private static void setupFakeCallDetails(Context ctx) 
    {
    	//Subscriber 1
        createAllCallDetailTypes(ctx, TestSetupAccountHierarchy.ACCOUNT1_BAN, TestSetupAccountHierarchy.SUB1_ID, 
                TestSetupAccountHierarchy.SUB1_MSISDN, TestSetupAccountHierarchy.SUB2_MSISDN, TestSetupAccountHierarchy.SPID_ID,
                SubscriberTypeEnum.POSTPAID);

        //Subscriber 2
        createAllCallDetailTypes(ctx, TestSetupAccountHierarchy.ACCOUNT2_BAN, TestSetupAccountHierarchy.SUB2_ID, 
                TestSetupAccountHierarchy.SUB2_MSISDN, TestSetupAccountHierarchy.SUB1_MSISDN, TestSetupAccountHierarchy.SPID_ID,
                SubscriberTypeEnum.POSTPAID);

        //Subscriber 3
        createAllCallDetailTypes(ctx, TestSetupAccountHierarchy.ACCOUNT2_BAN, TestSetupAccountHierarchy.SUB3_ID, 
                TestSetupAccountHierarchy.SUB3_MSISDN, TestSetupAccountHierarchy.SUB1_MSISDN, TestSetupAccountHierarchy.SPID_ID,
                SubscriberTypeEnum.POSTPAID);
	}

	public void testSetup()
    {
        testSetup(getContext());
    }
    
    public static void testSetup(Context ctx)
    {
        Home home = (Home)ctx.get(CallDetailHome.class);
        try
        {
            Collection col = home.where(ctx, new EQ(CallDetailXInfo.SUBSCRIBER_ID, TestSetupAccountHierarchy.SUB1_ID)).selectAll();
            assertTrue(col.size() > 0);
            col = home.where(ctx, new EQ(CallDetailXInfo.SUBSCRIBER_ID, TestSetupAccountHierarchy.SUB2_ID)).selectAll();
            assertTrue(col.size() > 0);
            col = home.where(ctx, new EQ(CallDetailXInfo.SUBSCRIBER_ID, TestSetupAccountHierarchy.SUB3_ID)).selectAll();
            assertTrue(col.size() > 0);
        }
        catch (HomeException e)
        {
            fail("No Call Details found for subscribers." + e.getMessage());
        }
    }
    
    private static void setupCallDetailHome(Context ctx, final boolean installXDB)
    {
        if (installXDB)
        {
            //Overwrite
            Home home = new CallDetailXDBHome(ctx, DB_TABLE_CALLDETAIL);
            enableAutoIncrement(ctx,home);
            ctx.put(CallDetailHome.class, home);
        }
        else
        {
            Home home = new TransientFieldResettingHome(ctx, new CallDetailTransientHome(ctx));
            home = new TransientHomeXDBCmdEmulator(ctx, home);
            enableAutoIncrement(ctx,home);
            ctx.put(CallDetailHome.class, home);
        }

    }


    private static void setupTypes()
    {
        types_ = new ArrayList<Short>();
        types_.add(BillingCategoryEnum.DOMESTIC_INDEX);
        types_.add(BillingCategoryEnum.INTERNATIONAL_INDEX);
        types_.add(BillingCategoryEnum.DATA_INDEX);
        types_.add(BillingCategoryEnum.ROAMING_INCOMING_INDEX);
        types_.add(BillingCategoryEnum.ROAMING_OUTGOING_INDEX);
        types_.add(BillingCategoryEnum.MCOMMERCE_INDEX);
        types_.add(BillingCategoryEnum.SMS_INDEX);
    }
    
    private static void createAllCallDetailTypes(
            Context ctx, 
            String ban, 
            String subID, 
            String origMSISDN, 
            String destMSISDN,
            int spid,
            SubscriberTypeEnum subType)
    {
        for (int i=0 ; i< types_.size(); i++)
        {
            short cat = types_.get(i);
            try
            {
                createCallDetails(ctx, ban, subID, origMSISDN, destMSISDN, spid, subType, cat);
            }
            catch(HomeException e)
            {
                new DebugLogMsg("TestSetupCallDetails", "Failed to create call details in this category=" + cat, e).log(ctx);
            }
        }
    }
    
    private static void createCallDetails(
            Context ctx, 
            String ban, 
            String subID, 
            String origMSISDN, 
            String destMSISDN,
            int spid,
            SubscriberTypeEnum subType, 
            short billCat)
    throws HomeException
    {
        Home home = (Home) ctx.get(CallDetailHome.class);
        for (int i = 0; i<numberOfCallDetails; i++)
        {
            CallDetail t = new CallDetail();

            t.setTranDate(transdate);
            t.setPostedDate(transdate);
            transdate = CalendarSupportHelper.get(ctx).findDateDaysAfter(1, transdate);
            t.setChargedMSISDN(origMSISDN);
            String origMsisdn = origMSISDN;
            t.setBAN(ban); 
            t.setSubscriberID(subID);
            t.setOrigMSISDN(origMsisdn);
            t.setDestMSISDN(destMSISDN);
            t.setCallingPartyLocation("LocZoneDesc");
            t.setDuration(new Time(0, 0, 12));
            t.setFlatRate(10);
            t.setVariableRate(10);
            t.setVariableRateUnit(RateUnitEnum.SEC);
            t.setCharge(100 * i+1);
            t.setUsageType(2);
            t.setUsedMinutes(60000);
            t.setSpid(spid);
            t.setRatePlan("RP1410080");
            t.setRatingRule("14040");
            t.setCallID("100"+i);
            t.setCallType(CallTypeEnum.ORIG);
            t.setBucketRateID(CallDetail.DEFAULT_BUCKETRATEID);
            t.setSubscriberType(subType);
            t.setBillingOption("Not_discard");
            t.setAir(99999);
            t.setToll(99999);
            t.setTax(99999);
            t.setBillingCategory(billCat);
            t.setId(idCounter_);
            idCounter_++;
            home.create(ctx, t);
        }
    }
    
    public static void completelyTearDown(Context ctx)
    {
        try
        {
            Home home = (Home) ctx.get(CallDetailHome.class);
            home.removeAll();
        }
        catch (HomeException e)
        {
            new DebugLogMsg("TestSetupCallDetails", "Failed to delete all call details." , e).log(ctx);
        }
    }
    
    /**
     * Enables autoincrementing in transient homes.
     * From StorageInstall class.
     * @param ctx registry
     * @param home the home to enable
     */
    private static void enableAutoIncrement(Context ctx,Home home)
    {
        try
        {
            home.cmd(ctx,HomeCmdEnum.AUTOINC_ENABLE);
        }
        catch (HomeException e)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg("TestSetupTransaction",e.getMessage(),e).log(ctx);
            }
        }
    }

    private static Date origTransdate = new Date(1136869201000L); //January 10, 2006
    private static Date transdate;
    private static final int numberOfCallDetails = 10;
    private static ArrayList<Short> types_ = null;
    private static int idCounter_ = 0;
    private static final String DB_TABLE_CALLDETAIL = "UNITTESTCALLDETAIL";
}
