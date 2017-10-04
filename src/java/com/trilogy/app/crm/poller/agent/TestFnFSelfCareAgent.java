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
package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;

public class TestFnFSelfCareAgent extends ContextAwareTestCase {

	public TestFnFSelfCareAgent(String name) {
		super(name);
	}

	public Context getContext() {
		return super.getContext();
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

        final TestSuite suite = new TestSuite(TestFnFSelfCareAgent.class);

        return suite;
    }

	public void setContext(Context context) {
		super.setContext(context);
	}

	protected void setUp() {
		super.setUp();
	}

	protected void tearDown()
	{
		super.tearDown();
	}

	/*
	 * Test method for 'com.redknee.app.crm.poller.FnFSelfCareAgent.FnFSelfCareAgent(CRMProcessor)'
	 */
	public void testFnFSelfCareAgent() {

	}

	/*
	 * Test method for 'com.redknee.app.crm.poller.FnFSelfCareAgent.execute(Context)'
	 */
	public void testExecute() {
		List list = getErList();
		assertNotNull("The processed ER list is null", list);

	}

	private List getErList() {
		List params = new ArrayList();
		final ProcessorInfo info = (ProcessorInfo)getContext().get(ProcessorInfo.class);
		try{
			List list = CRMProcessorSupport.makeArray(getContext(), params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),this);
			return list; 
		} catch ( FilterOutException e){
		}
 
		return null;
	}

	/*
	 * Test method for 'com.redknee.app.crm.poller.FnFSelfCareAgent.createCugAuxsvcAndAddSub(Context, List)'
	 */
	public void testCreateCugAuxsvcAndAddSub() {

	}

	/*
	 * Test method for 'com.redknee.app.crm.poller.FnFSelfCareAgent.removeSubFromCug(Context, String, long)'
	 */
	public void testRemoveSubFromCug() {

	}

	/*
	 * Test method for 'com.redknee.app.crm.poller.FnFSelfCareAgent.addSubToCug(Context, String, long)'
	 */
	public void testAddSubToCug() {

	}

	/*
	 * Test method for 'com.redknee.app.crm.poller.FnFSelfCareAgent.detachSubfromPlp(Context, String, long)'
	 */
	public void testDetachSubfromPlp() {

	}

	/*
	 * Test method for 'com.redknee.app.crm.poller.FnFSelfCareAgent.attachSubToPlp(Context, String, long)'
	 */
	public void testAttachSubToPlp() {

	}

	/*
	 * Test method for 'com.redknee.app.crm.poller.FnFSelfCareAgent.getEr1900FieldAtIndex(List, int)'
	 */
	public void testGetEr1900FieldAtIndex() 
	{
		
	}

}
