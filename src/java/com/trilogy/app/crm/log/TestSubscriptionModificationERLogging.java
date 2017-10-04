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
import com.trilogy.framework.xlog.log.ERLogMsg;

import com.trilogy.app.crm.bean.BillingOptionEnum;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * @author victor.stratan@redknee.com
 * @since 8.5
 */
public class TestSubscriptionModificationERLogging extends ContextAwareTestCase
{

    /**
     * @param name
     */
    public TestSubscriptionModificationERLogging(final String name)
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
        final TestSuite suite = new TestSuite(TestSubscriptionModificationERLogging.class);
        return suite;
    }


    public void testLogFromBeanIsIdentical()
    {
        final Context ctx = getContext();
        final Calendar calendar = Calendar.getInstance();
        final CalendarSupport calSup = CalendarSupportHelper.get(ctx);
        calSup.clearTimeOfDay(calendar);

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date createDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date oldStartDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date newStartDate = calendar.getTime();
        Date oldEndDate = null;
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date newEndDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date oldDepositDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date newDepositDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date oldExpiryDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date newExpiryDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date oldMarketingStartDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date newMarketingStartDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date oldMarketingEndDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date newMarketingEndDate = calendar.getTime();

        calendar.add(Calendar.YEAR, -15);
        Date birthDate = calendar.getTime();

        ERLogMsg erOne = new ERLogMsg(
                762,
                700,
                "Subscriber Modification Event",
                1,
                new String[]
                    {
                        "user",
                        "9056250100",
                        "9056260100",
                        "9056250101",
                        "9056260101",
                        "9056250102",
                        "9056260102",
                        "9056250103",
                        "9056260103",
                        "9056250105",
                        "9056260105",
                        "1",
                        "2",
                        "16262",
                        "17262",
                        "123",
                        "124",
                        "1123",
                        "1124",
                        "1122",
                        "1225",
                        "1226",
                        "14",
                        String.valueOf(SubscriberStateEnum.ACTIVE_INDEX),
                        String.valueOf(SubscriberStateEnum.LOCKED_INDEX),
                        "15",
                        "4567",
                        "4568",
                        "8901",
                        "8902",
                        "16",
                        "GBP",
                        "EUR",
                        ERLogger.addDoubleQuotes("5,6"),
                        ERLogger.addDoubleQuotes("7,8"),
                        "17",
                        "73",
                        "74",
                        "9056250106",
                        "9056260106",
                        ERLogger.addDoubleQuotes("address Old one"),
                        ERLogger.addDoubleQuotes("address New one"),
                        ERLogger.addDoubleQuotes("address Old two"),
                        ERLogger.addDoubleQuotes("address New two"),
                        ERLogger.addDoubleQuotes("address Old three"),
                        ERLogger.addDoubleQuotes("address New three"),
                        ERLogger.addDoubleQuotes("address Old city"),
                        ERLogger.addDoubleQuotes("address New city"),
                        ERLogger.addDoubleQuotes("address Old province"),
                        ERLogger.addDoubleQuotes("address New province"),
                        ERLogger.addDoubleQuotes("address Old country"),
                        ERLogger.addDoubleQuotes("address New country"),
                        "91",
                        "92",
                        "82",
                        "83",
                        ERLogger.formatERDateDayOnly(oldDepositDate),
                        ERLogger.formatERDateDayOnly(newDepositDate),
                        ERLogger.formatERDateDayOnly(oldStartDate),
                        ERLogger.formatERDateDayOnly(newStartDate),
                        ERLogger.formatERDateDayOnly(oldEndDate),
                        ERLogger.formatERDateDayOnly(newEndDate),
                        "sw",
                        "en",
                        "9056250104",
                        "9056260104",
                        "17262-1",
                        ERLogger.formatERDateDayOnly(createDate),
                        "fn",
                        "ln",
                        BillingOptionEnum.SUMMARY.getDescription(),
                        ERLogger.formatERDateDayOnly(oldExpiryDate),
                        ERLogger.formatERDateDayOnly(newExpiryDate),
                        "7654",
                        "7655",
                        ERLogger.formatERDateDayOnly(birthDate),
                        "f",
                        "t",
                        "43",
                        "44",
                        "32",
                        "33",
                        ERLogger.formatERDateDayOnly(oldMarketingStartDate),
                        ERLogger.formatERDateDayOnly(newMarketingStartDate),
                        ERLogger.formatERDateDayOnly(oldMarketingEndDate),
                        ERLogger.formatERDateDayOnly(newMarketingEndDate),
                        "8891",
                        ERLogger.addDoubleQuotes("11,12"),
                        ERLogger.addDoubleQuotes("22,23,24"),
                        String.valueOf(false),
 "52", "54", "0", "1000"
                    });

        final SubscriptionModificationER bean = new SubscriptionModificationER();
        bean.setSpid(1);
        bean.setUserID("user");
        bean.setOldGroupMobileNumber("9056250100");
        bean.setNewGroupMobileNumber("9056260100");
        bean.setOldVoiceMobileNumber("9056250101");
        bean.setNewVoiceMobileNumber("9056260101");
        bean.setOldFaxMobileNumber("9056250102");
        bean.setNewFaxMobileNumber("9056260102");
        bean.setOldDataMobileNumber("9056250103");
        bean.setNewDataMobileNumber("9056260103");
        bean.setOldIMSI("9056250105");
        bean.setNewIMSI("9056260105");
        bean.setOldSPID(1);
        bean.setNewSPID(2);
        bean.setOldBAN("16262");
        bean.setNewBAN("17262");
        bean.setOldPricePlan(123);
        bean.setNewPricePlan(124);
        bean.setOldFreeMinutes(1123);
        bean.setNewFreeMinutes(1124);
        bean.setUsedFreeMinutes(1122);
        bean.setAdjustmentMinutes(1225);
        bean.setAdjustmentAmount(1226);
        bean.setPricePlanChangeResultCode(14);
        bean.setOldState(SubscriberStateEnum.ACTIVE_INDEX);
        bean.setNewState(SubscriberStateEnum.LOCKED_INDEX);
        bean.setStateChangeResultCode(15);
        bean.setOldDeposit(4567);
        bean.setNewDeposit(4568);
        bean.setOldCreditLimit(8901);
        bean.setNewCreditLimit(8902);
        bean.setCreditLimitResultCode(16);
        bean.setOldCurrency("GBP");
        bean.setNewCurrency("EUR");
        bean.setOldServices(ERLogger.addDoubleQuotes("5,6"));
        bean.setNewServices(ERLogger.addDoubleQuotes("7,8"));
        bean.setServicesChangeResultCode(17);
        bean.setOldBillingType(73);
        bean.setNewBillingType(74);
        bean.setOldPostpaidSupportMSISDN("9056250106");
        bean.setNewPostpaidSupportMSISDN("9056260106");
        bean.setOldAddress1(ERLogger.addDoubleQuotes("address Old one"));
        bean.setNewAddress1(ERLogger.addDoubleQuotes("address New one"));
        bean.setOldAddress2(ERLogger.addDoubleQuotes("address Old two"));
        bean.setNewAddress2(ERLogger.addDoubleQuotes("address New two"));
        bean.setOldAddress3(ERLogger.addDoubleQuotes("address Old three"));
        bean.setNewAddress3(ERLogger.addDoubleQuotes("address New three"));
        bean.setOldCity(ERLogger.addDoubleQuotes("address Old city"));
        bean.setNewCity(ERLogger.addDoubleQuotes("address New city"));
        bean.setOldRegion(ERLogger.addDoubleQuotes("address Old province"));
        bean.setNewRegion(ERLogger.addDoubleQuotes("address New province"));
        bean.setOldCountry(ERLogger.addDoubleQuotes("address Old country"));
        bean.setNewCountry(ERLogger.addDoubleQuotes("address New country"));
        bean.setOldSubscriptionDealerCode("91");
        bean.setNewSubscriptionDealerCode("92");
        bean.setOldDiscountClass(82);
        bean.setNewDiscountClass(83);
        bean.setOldDepositDate(oldDepositDate);
        bean.setNewDepositDate(newDepositDate);
        bean.setOldSubscriptionStartDate(oldStartDate);
        bean.setNewSubscriptionStartDate(newStartDate);
        bean.setOldSubscriptionEndDate(oldEndDate);
        bean.setNewSubscriptionEndDate(newEndDate);
        bean.setOldBillingLanguage("sw");
        bean.setNewBillingLanguage("en");
        bean.setOldPackageID("9056250104");
        bean.setNewPackageID("9056260104");
        bean.setSubscriptionID("17262-1");
        bean.setSubscriptionCreateDate(createDate);
        bean.setLastName("ln");
        bean.setFirstName("fn");
        bean.setBillingOption(BillingOptionEnum.SUMMARY_INDEX);
        bean.setOldExpiryDate(oldExpiryDate);
        bean.setNewExpiryDate(newExpiryDate);
        bean.setOldReactivationFee(7654);
        bean.setNewReactivationFee(7655);
        bean.setDateOfBirth(birthDate);
        bean.setOldAboveCreditLimit("f");
        bean.setNewAboveCreditLimit("t");
        bean.setOldSubscriptionCategory(43);
        bean.setNewSubscriptionCategory(44);
        bean.setOldMarketingCampain(32);
        bean.setNewMarketingCampain(33);
        bean.setOldMarketingCampainStartDate(oldMarketingStartDate);
        bean.setNewMarketingCampainStartDate(newMarketingStartDate);
        bean.setOldMarketingCampainEndDate(oldMarketingEndDate);
        bean.setNewMarketingCampainEndDate(newMarketingEndDate);
        bean.setLeftoverBalance(8891);
        bean.setOldAuxiliaryServices(ERLogger.addDoubleQuotes("11,12"));
        bean.setNewAuxiliaryServices(ERLogger.addDoubleQuotes("22,23,24"));
        bean.setPricePlanRestrictionOverridden(false);
		bean.setOldMonthlySpendLimit(52);
		bean.setNewMonthlySpendLimit(54);
        bean.setOldOverdraftBalanceLimit(0);
        bean.setNewOverdraftBalanceLimit(1000);

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
