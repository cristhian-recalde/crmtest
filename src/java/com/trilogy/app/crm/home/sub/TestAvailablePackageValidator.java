/**
 *
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

package com.trilogy.app.crm.home.sub;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.GSMPackageTransientHome;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.TDMAPackageTransientHome;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Unit test for {@link TestAvailablePackageValidator}.
 * 
 * @author cindy.wong@redknee.com
 */
public class TestAvailablePackageValidator extends ContextAwareTestCase
{
    private final GSMPackage gsmAvailable_ = new GSMPackage();

    private final GSMPackage gsmInUse2_ = new GSMPackage();

    private final GSMPackage gsmHeld_ = new GSMPackage();

    private final GSMPackage gsmInUse_ = new GSMPackage();

    private final TDMAPackage tdmaAvailable_ = new TDMAPackage();

    private final TDMAPackage tdmaInUse2_ = new TDMAPackage();

    private final TDMAPackage tdmaHeld_ = new TDMAPackage();

    private final TDMAPackage tdmaInUse_ = new TDMAPackage();

    /**
     * Constructs a test case with the given name.
     * 
     * @param name
     *            The name of the test.
     */
    public TestAvailablePackageValidator(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by standard JUnit tools (i.e.,
     * those that do not provide a context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by the Redknee Xtest code,
     * which provides the application's operating context.
     * 
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestAvailablePackageValidator.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
     */
    @Override
    protected void setUp()
    {
        super.setUp();
        {
            final Home home = new GSMPackageTransientHome(getContext());
            gsmAvailable_.setPackId("GSM-1");
            gsmAvailable_.setTechnology(TechnologyEnum.GSM);
            gsmAvailable_.setState(PackageStateEnum.AVAILABLE);
            gsmInUse2_.setPackId("GSM-2");
            gsmInUse2_.setTechnology(TechnologyEnum.GSM);
            gsmInUse2_.setState(PackageStateEnum.IN_USE);
            gsmHeld_.setPackId("GSM-3");
            gsmHeld_.setTechnology(TechnologyEnum.GSM);
            gsmHeld_.setState(PackageStateEnum.HELD);
            gsmInUse_.setPackId("GSM-4");
            gsmInUse_.setTechnology(TechnologyEnum.GSM);
            gsmInUse_.setState(PackageStateEnum.IN_USE);
            try
            {
                home.create(getContext(), gsmAvailable_);
                home.create(getContext(), gsmInUse2_);
                home.create(getContext(), gsmHeld_);
                home.create(getContext(), gsmInUse_);
            }
            catch (HomeException exception)
            {
                fail(exception.getMessage());
            }
            getContext().put(GSMPackageHome.class, home);
        }
        {
            final Home home = new TDMAPackageTransientHome(getContext());
            tdmaAvailable_.setPackId("TDMA-1");
            tdmaAvailable_.setTechnology(TechnologyEnum.TDMA);
            tdmaAvailable_.setState(PackageStateEnum.AVAILABLE);
            tdmaInUse2_.setPackId("TDMA-2");
            tdmaInUse2_.setTechnology(TechnologyEnum.TDMA);
            tdmaInUse2_.setState(PackageStateEnum.IN_USE);
            tdmaHeld_.setPackId("TDMA-3");
            tdmaHeld_.setTechnology(TechnologyEnum.TDMA);
            tdmaHeld_.setState(PackageStateEnum.HELD);
            tdmaInUse_.setPackId("TDMA-4");
            tdmaInUse_.setTechnology(TechnologyEnum.TDMA);
            tdmaInUse_.setState(PackageStateEnum.IN_USE);
            try
            {
                home.create(getContext(), tdmaAvailable_);
                home.create(getContext(), tdmaInUse2_);
                home.create(getContext(), tdmaHeld_);
                home.create(getContext(), tdmaInUse_);
            }
            catch (HomeException exception)
            {
                fail(exception.getMessage());
            }
            getContext().put(TDMAPackageHome.class, home);
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.AvailablePackageValidator#validate}.
     */
    public void testValidateFromGSMToGSM()
    {
        final AvailablePackageValidator validator = new AvailablePackageValidator();

        final Subscriber oldSubscriber = new Subscriber();
        oldSubscriber.setPackageId(gsmInUse_.getPackId());
        oldSubscriber.setTechnology(gsmInUse_.getTechnology());
        getContext().put(Lookup.OLDSUBSCRIBER, oldSubscriber);

        final Subscriber newSubscriber = new Subscriber();
        newSubscriber.setPackageId(gsmInUse_.getPackId());
        newSubscriber.setTechnology(TechnologyEnum.GSM);

        // case 1: same package
        try
        {
            validator.validate(getContext(), newSubscriber);
        }
        catch (IllegalStateException exception)
        {
            fail(exception.getMessage());
        }

        // case 2: package available
        newSubscriber.setPackageId(gsmAvailable_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
        }
        catch (IllegalStateException exception)
        {
            fail(exception.getMessage());
        }

        // case 3: package held
        newSubscriber.setPackageId(gsmHeld_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 4: package in use
        newSubscriber.setPackageId(gsmInUse2_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 5: wrong technology
        newSubscriber.setPackageId(tdmaAvailable_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.AvailablePackageValidator#validate}.
     */
    public void testValidateFromGSMToTDMA()
    {
        final AvailablePackageValidator validator = new AvailablePackageValidator();

        final Subscriber oldSubscriber = new Subscriber();
        oldSubscriber.setPackageId(gsmInUse_.getPackId());
        oldSubscriber.setTechnology(gsmInUse_.getTechnology());
        getContext().put(Lookup.OLDSUBSCRIBER, oldSubscriber);

        final Subscriber newSubscriber = new Subscriber();
        newSubscriber.setPackageId(tdmaAvailable_.getPackId());
        newSubscriber.setTechnology(TechnologyEnum.TDMA);

        // case 1: package available
        try
        {
            validator.validate(getContext(), newSubscriber);
        }
        catch (IllegalStateException exception)
        {
            fail(exception.getMessage());
        }

        // case 2: package held
        newSubscriber.setPackageId(tdmaHeld_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 3: package in use
        newSubscriber.setPackageId(tdmaInUse_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 4: wrong technology
        newSubscriber.setPackageId(gsmAvailable_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 5: old package in old technology
        newSubscriber.setPackageId(gsmInUse_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.AvailablePackageValidator#validate}.
     */
    public void testValidateFromTDMAToTDMA()
    {
        final AvailablePackageValidator validator = new AvailablePackageValidator();

        final Subscriber oldSubscriber = new Subscriber();
        oldSubscriber.setPackageId(tdmaInUse_.getPackId());
        oldSubscriber.setTechnology(tdmaInUse_.getTechnology());
        getContext().put(Lookup.OLDSUBSCRIBER, oldSubscriber);

        final Subscriber newSubscriber = new Subscriber();
        newSubscriber.setPackageId(tdmaInUse_.getPackId());
        newSubscriber.setTechnology(TechnologyEnum.TDMA);

        // case 1: same package
        try
        {
            validator.validate(getContext(), newSubscriber);
        }
        catch (IllegalStateException exception)
        {
            fail(exception.getMessage());
        }

        // case 2: package available
        newSubscriber.setPackageId(tdmaAvailable_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
        }
        catch (IllegalStateException exception)
        {
            fail(exception.getMessage());
        }

        // case 3: package held
        newSubscriber.setPackageId(tdmaHeld_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 4: package in use
        newSubscriber.setPackageId(tdmaInUse2_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 5: wrong technology
        newSubscriber.setPackageId(gsmAvailable_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.AvailablePackageValidator#validate}.
     */
    public void testValidateFromTDMAToGSM()
    {
        final AvailablePackageValidator validator = new AvailablePackageValidator();

        final Subscriber oldSubscriber = new Subscriber();
        oldSubscriber.setPackageId(tdmaInUse_.getPackId());
        oldSubscriber.setTechnology(tdmaInUse_.getTechnology());
        getContext().put(Lookup.OLDSUBSCRIBER, oldSubscriber);

        final Subscriber newSubscriber = new Subscriber();
        newSubscriber.setPackageId(gsmAvailable_.getPackId());
        newSubscriber.setTechnology(TechnologyEnum.GSM);

        // case 1: package available
        try
        {
            validator.validate(getContext(), newSubscriber);
        }
        catch (IllegalStateException exception)
        {
            fail(exception.getMessage());
        }

        // case 2: package held
        newSubscriber.setPackageId(gsmHeld_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 3: package in use
        newSubscriber.setPackageId(gsmInUse_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 4: wrong technology
        newSubscriber.setPackageId(tdmaAvailable_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }

        // case 5: old package in old technology
        newSubscriber.setPackageId(tdmaInUse_.getPackId());
        try
        {
            validator.validate(getContext(), newSubscriber);
            fail("Validation should fail");
        }
        catch (IllegalStateException exception)
        {
            new DebugLogMsg(this, "Correct exception caught", exception);
        }
    }

}
