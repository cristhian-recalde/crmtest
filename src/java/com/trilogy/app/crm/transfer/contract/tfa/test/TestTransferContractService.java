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
package com.trilogy.app.crm.transfer.contract.tfa.test;

import com.trilogy.framework.xhome.context.Context;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.transfer.TransferContract;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
import com.trilogy.app.crm.transfer.contract.TransferContractFacade;
import com.trilogy.app.crm.transfer.contract.TransferContractNotFoundException;
import com.trilogy.app.crm.transfer.contract.TransferContractOwnerNotFoundException;
import com.trilogy.app.crm.transfer.contract.tfa.RMITransferContractImpl;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.transferfund.rmi.api.transfercontract.TransferContractService;

/**
 * Tests the TransferContract services.
 * @author arturo.medina@redknee.com
 *
 */
public class TestTransferContractService extends ContextAwareTestCase
{

    public TestTransferContractService(String name)
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
        final TestSuite suite = new TestSuite(TestTransferContractService.class);
        return suite;
    }

    public void setUp()
    {
        super.setUp();

        final Context context = getContext();
        final TransferContractFacade service = new RMITransferContractImpl();
        final RMINullTransferContractServices nullService = new RMINullTransferContractServices();

        context.put(TransferContractService.class, nullService);
        context.put(TransferContractFacade.class, service);
    }
    
    public void tearDown()  
    {
        //tear down here
        
        super.tearDown();
    }
    
    public void testCreateContractGroup()
    {
        Context ctx = getContext();
        TransferContractFacade service = (TransferContractFacade)ctx.get(TransferContractFacade.class);
        assertNull(service);
        
        try
        {
            assertNull(service.createTransferContract(ctx, new TransferContract()));
        }
        catch (TransferContractException e)
        {
            fail(e.getMessage());
        }
    }

    
    public void testDeleteTransferContract()
    {
        Context ctx = getContext();
        TransferContractFacade service = (TransferContractFacade)ctx.get(TransferContractFacade.class);
        assertNull(service);
        
        try
        {
            service.deleteTransferContract(ctx, 1);
        }
        catch (TransferContractException e)
        {
            fail(e.getMessage());
        }
        catch (TransferContractNotFoundException e)
        {
            fail(e.getMessage());
        }
    }

    public void testDeleteTransferContractsByOwner()
    {
        Context ctx = getContext();
        TransferContractFacade service = (TransferContractFacade)ctx.get(TransferContractFacade.class);
        assertNull(service);
        
        try
        {
            service.deleteTransferContractsByOwner(ctx, "1");
        }
        catch (TransferContractException e)
        {
            fail(e.getMessage());
        }
        catch (TransferContractOwnerNotFoundException e)
        {
            fail(e.getMessage());
        }
    }

    
    public void testRetrieveTransferContract()
    {
        Context ctx = getContext();
        TransferContractFacade service = (TransferContractFacade)ctx.get(TransferContractFacade.class);
        assertNull(service);
        
        try
        {
            service.retrieveTransferContract(ctx, 1);
        }
        catch (TransferContractException e)
        {
            fail(e.getMessage());
        }
        catch (TransferContractNotFoundException e)
        {
            fail(e.getMessage());
        }
    }

    public void retrieveTransferContractByOwner()
    {
        Context ctx = getContext();
        TransferContractFacade service = (TransferContractFacade)ctx.get(TransferContractFacade.class);
        assertNull(service);
        
        try
        {
            service.retrieveTransferContractByOwner(ctx, "1");
        }
        catch (TransferContractException e)
        {
            fail(e.getMessage());
        }
        catch (TransferContractOwnerNotFoundException e)
        {
            fail(e.getMessage());
        }
    }
    
}
