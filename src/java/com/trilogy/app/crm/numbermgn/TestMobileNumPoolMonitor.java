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
package com.trilogy.app.crm.numbermgn;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.LnpReqirementEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupMobileNumbers;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;



/**
 * @author manda.subramanyam@redknee.com
 *
 */
public class TestMobileNumPoolMonitor extends ContextAwareTestCase 
{
    /**
     * Constructor method     
     */
    public TestMobileNumPoolMonitor(final String name)
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
        final TestSuite suite = new TestSuite(TestMobileNumPoolMonitor.class);

        return suite;
    }
    
    //  INHERIT
    public void setUp()
    {
        super.setUp();
        
        TestSetupMobileNumbers.setup(getContext());
        
        setupMsisdnGroup();
    }
    
    // INHERIT
    public void tearDown()
    {   
        Context ctx = getContext();
        Home msisdnGrpHome = (Home) ctx.get(MsisdnGroupHome.class);
        Home msisdnHome = (Home) ctx.get(MsisdnHome.class);
        MsisdnGroup mg = null;
        Msisdn msisdn =  null;
        try{
            mg =  (MsisdnGroup) msisdnGrpHome.find(ctx, Integer.valueOf(10001));
        }
        catch (Exception e) 
        {
            new MinorLogMsg(this,"Unable to find msisdn group",e).log(ctx);
        }
        try
        {
            msisdn = (Msisdn) msisdnHome.find(ctx, "1000020000");
        }
        catch (Exception e) 
        {
            new MinorLogMsg(this,"Unable to find msisdn ",e).log(ctx);
        }
        if (msisdn != null)
        {
            try
            {
                msisdnHome.remove(ctx, msisdn);
            }
            catch (Exception e)
            {
                new MinorLogMsg(this,"Exception while deleting the msisdn ",e).log(ctx);
            }    
        }
        if (mg != null)
        {
            try
            {
                msisdnGrpHome.remove(ctx, mg);
            }
            catch (Exception e)
            {
                new MinorLogMsg(this,"Exception while deleting the msisdn ",e).log(ctx);
            }            
        }
        
        super.tearDown();
    }
    
    /**
     * Create a dummy msisdn group
     *
     */
    public void setupMsisdnGroup()
    {
        Context ctx = getContext();
        Home msisdnGrpHome = (Home) ctx.get(MsisdnGroupHome.class);
        MsisdnGroup mg = new MsisdnGroup();
        mg.setId(10001);
        mg.setName("MytestMobileNumberGroup");
        mg.setTechnology(TechnologyEnum.GSM);
        mg.setMinSize(2);
        try
        {
            msisdnGrpHome.create(ctx, mg);
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Setup Error: " + e.getMessage(), e).log(ctx);
        }
    }
    
    /**
     * Monitor the Mobile number group at the time of msisdn creation   
     */
    public void testMobileNumberPoolMonitorAtCreation()throws HomeException
    {
        Context ctx = getContext();
        Home msisdnGrpHome = (Home) ctx.get(MsisdnGroupHome.class);
        Home msisdnHome = (Home) ctx.get(MsisdnHome.class);
        
        MsisdnGroup mg =  (MsisdnGroup) msisdnGrpHome.find(ctx, Integer.valueOf(10001));
        
        assertNotNull("Msisdn Group obj is not null", mg);
        
        long no_of_free = 0;
        if (mg != null)
        {
            no_of_free = mg.getAvailableMsisdns();
        }
        
        Msisdn newMsisdn = new Msisdn();
        newMsisdn.setMsisdn("1000020000");
        newMsisdn.setGroup(10001);
        newMsisdn.setTechnology(TechnologyEnum.GSM);
        newMsisdn.setSpid(1);
        newMsisdn.setState(MsisdnStateEnum.AVAILABLE);
        newMsisdn.setLnpRequired(LnpReqirementEnum.NOT_REQUIRED);
        newMsisdn.setSubscriberType(SubscriberTypeEnum.PREPAID);
        
        msisdnHome.create(ctx, newMsisdn);
        
        mg =  (MsisdnGroup) msisdnGrpHome.find(ctx, Integer.valueOf(10001));
        
        if (mg == null)
        {
            assertFalse("Unable to retrieve the msisdn group with Id = 10001 ", false);
        }
        
        assertNotSame(Long.valueOf(no_of_free), Long.valueOf(mg.getAvailableMsisdns()));
        
    }
    
    /**
     * Monitor the mobile number pool at the time of msisdn update
     *
     */
    public void testMobileNumberPoolMonitorAtUpdate() throws HomeException
    {
        Context ctx = getContext();
        Home msisdnGrpHome = (Home) ctx.get(MsisdnGroupHome.class);
        Home msisdnHome = (Home) ctx.get(MsisdnHome.class);
        
        MsisdnGroup mg =  (MsisdnGroup) msisdnGrpHome.find(ctx, Integer.valueOf(10001));
        
        assertNotNull("Msisdn Group obj is not null", mg);
        
        long no_of_free = 0, no_of_used = 0;
        
        if (mg != null)
        {
            no_of_free = mg.getAvailableMsisdns();
            no_of_used = mg.getAssignedMsisdns();
        }
        
        Msisdn msisdn = (Msisdn) msisdnHome.find(ctx, "1000020000");
        
        assertNotNull(msisdn);
        
        msisdn.setState(MsisdnStateEnum.IN_USE);
        msisdnHome.store(ctx, msisdn);
        
        mg = (MsisdnGroup) msisdnGrpHome.find(ctx, Integer.valueOf(10001));
        
        assertNotNull("Msisdn Group obj is not null", mg);
        
        assertNotSame("Available Msisdns after update ", Long.valueOf(no_of_free), Long.valueOf(mg.getAvailableMsisdns()));
        assertNotSame("Assaigned Msisdns after update ", Long.valueOf(no_of_used), Long.valueOf(mg.getAssignedMsisdns()));
    }
    
    /**
     * Monitor Mobile number pool at the time of msisdn remove.
     * @throws HomeException
     */
    public void testMobileNumberPoolMonitorAtRemove() throws HomeException
    {    
        Context ctx = getContext();
        Home msisdnGrpHome = (Home) ctx.get(MsisdnGroupHome.class);
        Home msisdnHome = (Home) ctx.get(MsisdnHome.class);
        
        MsisdnGroup mg =  (MsisdnGroup) msisdnGrpHome.find(ctx, Integer.valueOf(10001));
        Msisdn msisdn = (Msisdn) msisdnHome.find(ctx, "1000020000");
        
        long free = mg.getAvailableMsisdns();
        long inuse = mg.getAssignedMsisdns();
        
        assertNotNull(msisdn);
        
        if (msisdn != null && msisdn.getState().equals(MsisdnStateEnum.AVAILABLE))
        {
            msisdnHome.remove(ctx, msisdn);
        }
        else
        {
            msisdn.setState(MsisdnStateEnum.AVAILABLE);
            msisdnHome.store(ctx, msisdn);
            msisdnHome.remove(ctx, msisdn);
        }
        mg =  (MsisdnGroup) msisdnGrpHome.find(ctx, Integer.valueOf(10001));
        assertNotSame("Available Msisdns not equal = ", Long.valueOf(free), Long.valueOf(mg.getAvailableMsisdns()));
    }       
}

