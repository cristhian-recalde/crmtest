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

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.agent.FacetInstall;
import com.trilogy.app.crm.bean.OnDemandSequence;
import com.trilogy.app.crm.bean.OnDemandSequenceHome;
import com.trilogy.app.crm.bean.OnDemandSequenceTransientHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.beans.DefaultFacetMgr;
import com.trilogy.framework.xhome.beans.DefaultFactoryFacetMgr;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.ParentClassFacetMgr;
import com.trilogy.framework.xhome.beans.SimpleFacetMgr;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


/**
*
*/
public class TestOnDemandSequence
    extends ContextAwareTestCase
{
    public static final int NEXT_ID = 987;

    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestOnDemandSequence(final String name)
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
        final TestSuite suite = new TestSuite(TestOnDemandSequence.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
        System.out.println("TestOnDemandSequence");
        Context ctx = getContext();
        Home home = new OnDemandSequenceTransientHome(ctx);
        home = new OnDemandSequenceResettingHome(home);
        ctx.put(OnDemandSequenceHome.class, home);
        
        ctx.put(FacetMgr.class, new DefaultFactoryFacetMgr(new ParentClassFacetMgr(new DefaultFacetMgr(SimpleFacetMgr.instance()))));
        try
        {
            new FacetInstall().execute(ctx);
        }
        catch (AgentException e)
        {
        }
        
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
        Context ctx = getContext();
        OnDemandSequence sequence = createOnDemandSequence(OnDemandSequenceManager.RECEIPT_SEQUENCE_KEY,1,200,true);
        HomeSupportHelper.get(ctx).createBean(ctx, sequence);
        
        Long identifier = OnDemandSequenceManager.acquireNextIdentifier(ctx, OnDemandSequenceManager.RECEIPT_SEQUENCE_KEY, 1);
        
        assertEquals(identifier.longValue(), 1);
        
        sequence = OnDemandSequenceManager.acquireNextOnDemandSequnceBlock(ctx, OnDemandSequenceManager.RECEIPT_SEQUENCE_KEY, 10);
        
        assertEquals(sequence.getNextNum(),101);
        assertEquals(sequence.getEndNum(),110);
        
    }
    
    public OnDemandSequence createOnDemandSequence(final String key, final long startNum, final long endNum, final boolean isYearlReset)
    {
        OnDemandSequence sequence = new OnDemandSequence();
        sequence.setIdentifier(key);
        sequence.setStartNum(startNum);
        sequence.setNextNum(startNum);
        sequence.setEndNum(endNum);
        sequence.setYearlyReset(isYearlReset);
        sequence.setLastResetDate(new Date());
        sequence.setLastModified(new Date());
        return sequence;
    }
}
