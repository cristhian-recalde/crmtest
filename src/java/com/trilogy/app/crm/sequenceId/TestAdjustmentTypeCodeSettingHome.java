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
package com.trilogy.app.crm.sequenceId;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

import java.util.Collection;


/**
 * A suite of test cases for AdjustmentTypeCodeSettingHome.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestAdjustmentTypeCodeSettingHome
    extends ContextAwareTestCase
{
    public static final int NEXT_ID = 987;

    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestAdjustmentTypeCodeSettingHome(final String name)
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

        final TestSuite suite = new TestSuite(TestAdjustmentTypeCodeSettingHome.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
    }


    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }


    /**
     * Tests that the create() method works according to the intent.
     */
    public void testCreate() throws HomeException
    {
        final Home home = new AdjustmentTypeCodeSettingHome(getContext(), null);

        Home thome = new FakeAdjustmentTypeHome();
        getContext().put(IdentifierSequenceHome.class, thome);

        final AdjustmentType adjustmentType = new AdjustmentType();
        home.create(getContext(), adjustmentType);

        assertEquals("The AdjustmentType code should change to the next identifier", NEXT_ID, adjustmentType.getCode());
    }
}

class FakeAdjustmentTypeHome implements AdjustmentTypeHome
{
    public Object findByPrimaryKey(Object object) throws HomeException
    {
        return null;
    }

    public Object findByPrimaryKey(Context context, Object object) throws HomeException
    {
        return null;
    }

    public Collection selectAll(Context context) throws HomeException
    {
        return null;
    }

    public Collection selectAll() throws HomeException
    {
        return null;
    }

    public void removeAll(Context context) throws HomeException
    {
    }

    public void removeAll() throws HomeException
    {
    }

    public Visitor forEach(Context context, Visitor visitor) throws HomeException
    {
        return null;
    }

    public Visitor forEach(Visitor visitor) throws HomeException
    {
        return null;
    }

    public Object create(Object object) throws HomeException
    {
        return null;
    }

    public Object store(Object object) throws HomeException
    {
        return null;
    }

    public Object find(Object object) throws HomeException
    {
        return find(getContext(), object);
    }

    public Collection select(Object object) throws HomeException
    {
        return null;
    }

    public void remove(Object object) throws HomeException
    {
    }

    public void removeAll(Object object) throws HomeException
    {
    }

    public Visitor forEach(Visitor visitor, Object object) throws HomeException
    {
        return null;
    }

    public void drop() throws HomeException
    {
    }

    public Object cmd(Object object) throws HomeException
    {
        return null;
    }

    public Home where(Context context, Object object)
    {
        return null;
    }

    public Object create(Context context, Object object) throws HomeException
    {
        return null;
    }

    public Object store(Context context, Object object) throws HomeException
    {
        return null;
    }

    public Object find(Context context, Object object) throws HomeException
    {
        IdentifierEnum identifier = IdentifierEnum.ADJUSTMENT_TYPE_CODE;
        if (identifier.getDescription().equals(object))
        {
            final IdentifierSequence sequence = new IdentifierSequence();
            sequence.setIdentifier(identifier.getDescription());
            sequence.setStartNum(50000);
            sequence.setEndNum(Integer.MAX_VALUE);
            sequence.setNextNum(TestAdjustmentTypeCodeSettingHome.NEXT_ID);
            return sequence;
        }
        return null;
    }

    public Collection select(Context context, Object object) throws HomeException
    {
        return null;
    }

    public void remove(Context context, Object object) throws HomeException
    {
    }

    public void removeAll(Context context, Object object) throws HomeException
    {
    }

    public Visitor forEach(Context context, Visitor visitor, Object object) throws HomeException
    {
        return null;
    }

    public void drop(Context context) throws HomeException
    {
    }

    public Object cmd(Context context, Object object) throws HomeException
    {
        if (object instanceof IncrementIdentifierCmd)
        {
            IncrementIdentifierCmd cmd = (IncrementIdentifierCmd) object;
            return Long.valueOf(((IdentifierSequence)find(context, cmd.identifierSequenceName)).getNextNum());
        }
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