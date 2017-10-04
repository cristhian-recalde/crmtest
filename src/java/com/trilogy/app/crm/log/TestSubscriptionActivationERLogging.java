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
package com.trilogy.app.crm.log;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.ERLogMsg;

import com.trilogy.app.crm.bean.ActivationReasonCode;
import com.trilogy.app.crm.bean.ActivationReasonCodeHome;
import com.trilogy.app.crm.bean.ActivationReasonCodeTransientHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * @author victor.stratan@redknee.com
 * @since 8.5
 */
public class TestSubscriptionActivationERLogging extends ContextAwareTestCase
{

    /**
     * @param name
     */
    public TestSubscriptionActivationERLogging(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to
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
     * Creates a new suite of Tests for execution. This method is intended to
     * be invoked by the Redknee XTest code, which provides the application's
     * operating context.
     * 
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestSubscriptionActivationERLogging.class);
        return suite;
    }


    public void testLogFromBeanIsIdentical()
    {
        final Context ctx = getContext();
        final Calendar calendar = Calendar.getInstance();
        final CalendarSupport calSup = CalendarSupportHelper.get(ctx);
        calSup.clearTimeOfDay(calendar);

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date startDate = calendar.getTime();
        Date endDate = null;

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date depositDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date expiryDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date marketingStartDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date marketingEndDate = calendar.getTime();

        calendar.add(Calendar.YEAR, -15);
        Date birthDate = calendar.getTime();

        final ActivationReasonCode reasonCode = new ActivationReasonCode();
        reasonCode.setReasonID(64);
        reasonCode.setSpid(1);
        reasonCode.setMessage("activation");
        final ActivationReasonCodeHome home = new ActivationReasonCodeTransientHome(ctx);
        ctx.put(ActivationReasonCodeHome.class, home);
        
        try
        {
            home.create(ctx, reasonCode);
        }
        catch (HomeException e)
        {
            fail("unable to setup test");
        }

        final ERLogMsg erOne = new ERLogMsg(761, 700, "Subscriber Activation Event", 1, new String[]
            {
                "user",
                "9056250100",
                "9056250101",
                "9056250102",
                "9056250103",
                "16262-11",
                "9056250104",
                "9056250105",
                "16262",
                "123",
                SubscriberStateEnum.ACTIVE.getDescription(),
                "4567",
                "8901",
                "GBP",
                "ln",
                "fn",
                ERLogger.formatERDateDayOnly(startDate),
                ERLogger.formatERDateDayOnly(endDate),
                "11",
                ERLogger.addDoubleQuotes("5,6"),
                "78",
                "90",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "1234",
                "7",
                "56",
                "9056250106",
                ERLogger.addDoubleQuotes("address one"),
                ERLogger.addDoubleQuotes("address two"),
                ERLogger.addDoubleQuotes("address three"),
                ERLogger.addDoubleQuotes("address city"),
                ERLogger.addDoubleQuotes("address province"),
                ERLogger.addDoubleQuotes("address country"),
                "91",
                "82",
                ERLogger.formatERDateDayOnly(depositDate),
                "73",
                "sw",
                "55",
                "activation",
                ERLogger.formatERDateDayOnly(expiryDate),
                "54321",
                "65",
                "7654",
                "8765" ,
                "98765",
                ERLogger.formatERDateDayOnly(birthDate),
                "43",
                "32",
                ERLogger.formatERDateDayOnly(marketingStartDate),
                ERLogger.formatERDateDayOnly(marketingEndDate),
                "77",
                "99",
                String.valueOf(false),
                "0",
            });

        final SubscriptionActivationER bean = new SubscriptionActivationER();
        bean.setSpid(1);
        bean.setUserID("user");
        bean.setGroupMobileNumber("9056250100");
        bean.setVoiceMobileNumber("9056250101");
        bean.setFaxMobileNumber("9056250102");
        bean.setDataMobileNumber("9056250103");
        bean.setSubscriptionID("16262-11");
        bean.setPackageID("9056250104");
        bean.setIMSI("9056250105");
        bean.setBAN("16262");
        bean.setPricePlan(123);
        bean.setState(SubscriberStateEnum.ACTIVE_INDEX);
        bean.setDeposit(4567);
        bean.setInitialCreditLimit(8901);
        bean.setCurrency("GBP");
        bean.setLastName("ln");
        bean.setFirstName("fn");
        bean.setSubscriptionStartDate(startDate);
        bean.setSubscriptionEndDate(endDate);
        bean.setBillCycleDay(11);
        bean.setServices(ERLogger.addDoubleQuotes("5,6"));
        bean.setDealerCode("78");
        bean.setCreditCategory(90);
        bean.setTransactionResultCode(1);
        bean.setUpsResultCode(2);
        bean.setEcareResultCode(3);
        bean.setEcpResultCode(4);
        bean.setSmsbResultCode(5);
        bean.setHlrResultCode(6);
        bean.setFirstChargeAmount(1234);
        bean.setFirstChargeResultCode(7);
        bean.setPricePlanVersion(56);
        bean.setPostpaidSupportMSISDN("9056250106");
        bean.setAddress1(ERLogger.addDoubleQuotes("address one"));
        bean.setAddress2(ERLogger.addDoubleQuotes("address two"));
        bean.setAddress3(ERLogger.addDoubleQuotes("address three"));
        bean.setCity(ERLogger.addDoubleQuotes("address city"));
        bean.setProvince(ERLogger.addDoubleQuotes("address province"));
        bean.setCountry(ERLogger.addDoubleQuotes("address country"));
        bean.setSubscriptionDealerCode("91");
        bean.setDiscountClass(82);
        bean.setDepositDate(depositDate);
        bean.setBillingType(73);
        bean.setBillingLanguage("sw");
        bean.setBillingOption(55);
        bean.setActivationReasonCode(64);
        bean.setExpiryDate(expiryDate);
        bean.setInitialBalance(54321);
        bean.setInitialExpiryDateExtention(65);
        bean.setReactivationFee(7654);
        bean.setMaxBalanceAmount(8765);
        bean.setMaxRechargeAmount(98765);
        bean.setDateOfBirth(birthDate);
        bean.setSubscriptionCategory(43);
        bean.setMarketingCampain(32);
        bean.setMarketingCampainStartDate(marketingStartDate);
        bean.setMarketingCampainEndDate(marketingEndDate);
        bean.setSubscriptionType(77);
        bean.setSubscriptionClass(99);
        bean.setPricePlanRestrictionOverridden(false);
        bean.setOverdraftBalanceLimit(0);

        final ERLogMsg erTwo = new ERLogMsg(ctx, bean);

        String[] erOneFields = erOne.getFields();
        String[] erTwoFields = erTwo.getFields();
        assertEquals("Logging ERs field counts are different", erOneFields.length, erTwoFields.length);
        for (int i = 0 ; i < erTwoFields.length; i++)
        {
            assertEquals("Fields are different", erOneFields[i], erTwoFields[i]);
        }
    }
}
