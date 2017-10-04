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
package com.trilogy.app.crm.transfer.membergroup;

import com.trilogy.framework.xhome.context.Context;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.transfer.contract.tfa.RMITransferContractImpl;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.transferfund.rmi.api.membergroup.MemberGroupService;
import com.trilogy.app.transferfund.rmi.data.AuthCredentials;
import com.trilogy.app.transferfund.rmi.data.ContractGroupMember;

/**
 * Tests the MemberGroup services.
 * @author
 *
 */
public class TestMemberGroupService extends ContextAwareTestCase
{

    public TestMemberGroupService(String name)
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
        final TestSuite suite = new TestSuite(TestMemberGroupService.class);
        return suite;
    }

    public void setUp()
    {
        super.setUp();

        final Context context = getContext();
        final RMINullMemberGroupExternalService nullService = new RMINullMemberGroupExternalService(context);
        context.put(MemberGroupService.class, nullService);
    }
    
    public static void setup()
    {
    }

    public void tearDown()  
    {
        super.tearDown();
    }
    
    public void testAddContractGroupMember()
    {
        Context ctx = getContext();
        MemberGroupService service = (MemberGroupService)ctx.get(MemberGroupService.class);
        assertNotNull(service);
        
        try
        {
            assertNull(service.addContractGroupMember(ctx, new AuthCredentials(), new ContractGroupMember(), ""));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    
    public void testRemoveMemberFromContractGroup()
    {
        Context ctx = getContext();
        MemberGroupService service = (MemberGroupService)ctx.get(MemberGroupService.class);
        assertNotNull(service);
        
        try
        {
            service.removeMemberFromContractGroup(new AuthCredentials(), 0, "", "");
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testDeleteContractGroupMember()
    {
        Context ctx = getContext();
        MemberGroupService service = (MemberGroupService)ctx.get(MemberGroupService.class);
        assertNotNull(service);
        
        try
        {
            assertEquals(service.deleteContractGroupMember(new AuthCredentials(), "", ""), 0);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    
    public void testRetrieveContractGroupMembers()
    {
        Context ctx = getContext();
        MemberGroupService service = (MemberGroupService)ctx.get(MemberGroupService.class);
        assertNotNull(service);
        
        try
        {
            assertNull(service.retrieveContractGroupMembers(new AuthCredentials(), 0, ""));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testRetrieveContractGroupsForMember()
    {
        Context ctx = getContext();
        MemberGroupService service = (MemberGroupService)ctx.get(MemberGroupService.class);
        assertNotNull(service);
        
        try
        {
            assertNull(service.retrieveContractGroupsForMember(new AuthCredentials(), "", ""));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
}
