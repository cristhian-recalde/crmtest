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

package com.trilogy.app.crm.web.control;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Unit test for AdjustmentTypeCategoryProxyWebControl.
 *
 * @author cindy.wong@redknee.com
 */
public class TestAdjustmentTypeCategoryProxyWebControl extends ContextAwareTestCase
{

    /**
     * Creates a new <code>TestAdjustmentTypeCategoryProxyWebControl</code>.
     *
     * @param name Name of the test suite
     */
    public TestAdjustmentTypeCategoryProxyWebControl(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by standard JUnit tools (i.e.,
     * those that do not provide a context).
     *
     * @return A new suite of Tests for execution
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by the Redknee Xtest code,
     * which provides the application's operating context.
     *
     * @param context The operating context
     * @return A new suite of Tests for execution
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestAdjustmentTypeCategoryProxyWebControl.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();
        getContext().put(AdjustmentTypeHome.class, new AdjustmentTypeTransientHome(getContext()));
    }

    /**
     * Test method for
     * {@link com.redknee.app.crm.web.control.AdjustmentTypeCategoryProxyWebControl#wrapContext(
     * com.redknee.framework.xhome.context.Context)}.
     */
    public final void testWrapContextContext()
    {
        final AdjustmentType parent = new AdjustmentType();
        parent.setCode(AdjustmentTypeEnum.StandardPayments.getIndex());
        parent.setCategory(false);
        final AdjustmentType child = new AdjustmentType();
        child.setCode(50000);
        child.setParentCode(AdjustmentTypeEnum.StandardPayments.getIndex());
        final AdjustmentType unrelated = new AdjustmentType();
        unrelated.setCode(AdjustmentTypeEnum.ReactivationFee.getIndex());
        try
        {
            ((Home) getContext().get(AdjustmentTypeHome.class)).create(parent);
            ((Home) getContext().get(AdjustmentTypeHome.class)).create(child);
            ((Home) getContext().get(AdjustmentTypeHome.class)).create(unrelated);
        }
        catch (HomeException exception)
        {
            fail(exception.getMessage());
        }
        final AdjustmentTypeCategoryProxyWebControl webControl = new AdjustmentTypeCategoryProxyWebControl(null,
            AdjustmentTypeEnum.StandardPayments);
        final Context context = webControl.wrapContext(getContext());
        final Home home = (Home) context.get(AdjustmentTypeHome.class);
        Collection types = null;
        try
        {
            types = home.selectAll();
            assertEquals("There should be 2 types in home", types.size(), 2);
        }
        catch (Exception exception)
        {
            fail(exception.getMessage());
        }
        boolean hasParent = false;
        boolean hasChild = false;
        for (Object object : types)
        {
            if (((AdjustmentType)object).getCode() == unrelated.getCode())
            {
                fail("Unrelated adjustment type should not exist in subcontext");
            }
            else if (((AdjustmentType)object).getCode() == parent.getCode())
            {
                hasParent = true;
            }
            else if (((AdjustmentType)object).getCode() == child.getCode())
            {
                hasChild = true;
            }
        }

        if (!hasParent)
        {
            fail("Parent should exist in subcontext");
        }
        
        if (!hasChild)
        {
            fail("Child should exist in subscontext");
        }
    }

}
