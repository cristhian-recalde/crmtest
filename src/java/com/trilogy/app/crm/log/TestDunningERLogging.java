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

import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * @author victor.stratan@redknee.com
 * @since 8.5
 */
public class TestDunningERLogging extends ContextAwareTestCase
{

    /**
     * @param name
     */
    public TestDunningERLogging(final String name)
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
        final TestSuite suite = new TestSuite(TestDunningERLogging.class);
        return suite;
    }


    public void testLogFromBeanIsIdentical()
    {
        final Context ctx = getContext();
        final Calendar calendar = Calendar.getInstance();
        final CalendarSupport calSup = CalendarSupportHelper.get(ctx);
        calSup.clearTimeOfDay(calendar);

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date toPayDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Date dueDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date toBeDunned = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date toBeArrears = calendar.getTime();

        final ERLogMsg erOne = new ERLogMsg(776, 700, "Dunning Action Event", 1, new String[]
            {
                    "9056262911",
                    "16262",
                    AccountStateEnum.ACTIVE.getDescription(),
                    AccountStateEnum.NON_PAYMENT_SUSPENDED.getDescription(),
                    ERLogger.formatERDateDayOnly(toPayDate),
                    "10",
                    ERLogger.formatERDateDayOnly(toBeDunned),
                    ERLogger.formatERDateDayOnly(toBeArrears),
                    ERLogger.formatERDateDayOnly(dueDate)
            });

        final DunningActionER bean = new DunningActionER();
        bean.setSpid(1);
        bean.setVoiceMobileNumber("9056262911");
        bean.setBAN("16262");
        bean.setOldAccountState(AccountStateEnum.ACTIVE_INDEX);
        bean.setNewAccountState(AccountStateEnum.NON_PAYMENT_SUSPENDED_INDEX);
        bean.setPromiseToPayDate(toPayDate);
        bean.setResultCode(10);
        bean.setToBeDunnedDate(toBeDunned);
        bean.setToBeInArrearsDate(toBeArrears);
        bean.setDueDate(dueDate);

        final ERLogMsg erTwo = new ERLogMsg(ctx, bean);

        assertEquals(erOne.getErId(), erTwo.getErId());
        assertEquals(erOne.getErClass(), erTwo.getErClass());
        assertEquals(erOne.getSID(), erTwo.getSID());
        assertEquals(erOne.getSPID(), erTwo.getSPID());

        String[] erOneFields = erOne.getFields();
        String[] erTwoFields = erTwo.getFields();
        assertEquals("Logging ERs field counts are different", erOneFields.length, erTwoFields.length);
        for (int i = 0; i < erTwoFields.length; i++)
        {
            assertEquals("Fields are different", erOneFields[i], erTwoFields[i]);
        }
    }
}
