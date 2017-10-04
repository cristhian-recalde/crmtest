/**
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bas.tps.PricePlanUnitEnum;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateHome;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateTransientHome;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.SctAuxiliaryService;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * @author msubramanyam
 * 
 */
public class TestAuxiliaryServicesToSCT extends ContextAwareTestCase
{
    /**
     * @param name
     */
    public TestAuxiliaryServicesToSCT(String name)
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
        final TestSuite suite = new TestSuite(TestAuxiliaryServicesToSCT.class);
        return suite;
    }


    @Override
    protected void setUp()
    {
        super.setUp();
        
        Context ctx = getContext();
        
        ctx.put(PricePlanHome.class, new PricePlanTransientHome(ctx));
        createTestPricePlans();

        ctx.put(ServiceActivationTemplateHome.class, new ServiceActivationTemplateTransientHome(ctx));

        // We need the test SCT even if an existing home is in the context.  We'll clean up in the tearDown().
        createTestSCTs();
        
        ctx.put(AuxiliaryServiceHome.class, new AuxiliaryServiceTransientHome(ctx));
        createTestAuxServices();

        ctx.put(SctAuxiliaryServiceHome.class, new SctAuxiliaryServiceTransientHome(ctx));
    }
    
    
    @Override
    protected void tearDown()
    {
        super.tearDown();
    }


    public void testAddAuxServicesToSCT()
    {
        try
        {
            Home home = (Home)getContext().get(ServiceActivationTemplateHome.class);
            assertNotNull(home);

            Home auxHome = null;
            Home sctAuxHome = null;
            ServiceActivationTemplate sct = (ServiceActivationTemplate) home.find(getContext(),
                    Long.valueOf(100010));
            assertNotNull(sct);
            if (sct != null)
            {
                auxHome = (Home) getContext().get(AuxiliaryServiceHome.class);
                sctAuxHome = (Home) getContext().get(SctAuxiliaryServiceHome.class);
                Collection coll = auxHome.where(getContext(), "SPID=" + 1).selectAll();
                assertNotNull(coll);
                if (coll != null && coll.size() > 1)
                {
                    int i = 1;
                    int collSize = coll.size();
                    Iterator iter = coll.iterator();
                    while (iter.hasNext())
                    {
                        AuxiliaryService auxService = (AuxiliaryService) iter.next();
                        SctAuxiliaryService sctAux = new SctAuxiliaryService();
                        sctAux.setAuxiliaryServiceIdentifier(auxService.getIdentifier());
                        sctAux.setCreated(new Date());
                        sctAux.setIdentifier(2002001 + i);
                        sctAux.setPaymentNum(6);
                        //sctAux.setStartDate(new Date());
                        //sctAux.setEndDate(CalendarSupportHelper.get(getContext()).findDateMonthsAfter(sctAux.getPaymentNum(), sctAux.getStartDate()));
                        //sctAux.setProvisioned(false);
                        sctAux.setSctIdentifier(sct.getIdentifier());
                        sctAuxHome.create(getContext(), sctAux);
                        if (i <= collSize && i == 2)
                        {
                            break;
                        }
                        i++;
                    }
                }
            }

            assertNotNull(sctAuxHome);
            assertTrue("No SCT Auxiliary Services were created", sctAuxHome.selectAll(getContext()).size() > 0);
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail("Unexpected HomeException: " + e.getMessage());
        }
    }

    private void createTestPricePlans()
    {
        int spid = 1;
        Home ppHome = (Home)getContext().get(PricePlanHome.class);
        
        PricePlan pp1 = new PricePlan();
        PricePlan pp2 = new PricePlan();
        
        pp1.setEnabled(true);
        pp1.setId(1L);
        pp1.setName("TestPP1");
        pp1.setSpid(spid);
        pp1.setPricePlanType(SubscriberTypeEnum.PREPAID);
        
        pp2.setEnabled(true);
        pp2.setId(2L);
        pp2.setName("TestPP2");
        pp2.setSpid(spid);
        pp1.setPricePlanType(SubscriberTypeEnum.PREPAID);
        
        try
        {
            ppHome.create(getContext(), pp1);
            ppHome.create(getContext(), pp2);
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail("Error creating test price plans: " + e.getMessage());
        }
    }


    private void createTestAuxServices()
    {
        AuxiliaryService aux = new AuxiliaryService();
        aux.setIdentifier(11001L);
        aux.setSpid(1);
        
        AuxiliaryService aux2 = new AuxiliaryService();
        aux2.setIdentifier(11002L);
        aux2.setSpid(1);
        
        Home auxHome = (Home) getContext().get(AuxiliaryServiceHome.class);
        try
        {
            auxHome.create(getContext(), aux);
            auxHome.create(getContext(), aux2);
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail("Error creating test Auxiliary Service: " + e.getMessage());
        }
    }
    

    private void createTestSCTs()
    {
        int spid = 1;
        Home sctHome = (Home)getContext().get(ServiceActivationTemplateHome.class);
        Collection pricePlanColl = null;
        try
        {
            pricePlanColl = PricePlanSupport.getPricePlanList(getContext(), spid);
        }
        catch (HomeException e)
        {
            e.printStackTrace();
        }
        assertNotNull(pricePlanColl);
        PricePlan pp1 = null, pp2 = null;
        if (pricePlanColl != null && pricePlanColl.size() >= 2)
        {
            Iterator iter = pricePlanColl.iterator();
            while (iter.hasNext())
            {
                PricePlan plan = (PricePlan) iter.next();
                if (plan.getEnabled())
                {
                    if (pp1 == null)
                    {
                        pp1 = plan;
                    }
                    else
                    {
                        pp2 = plan;
                        break;
                    }
                }
            }
            assertNotNull(pp1);
            assertNotNull(pp2);
            assertNotSame(pp1, pp2);
        }
        ServiceActivationTemplate sct = new ServiceActivationTemplate();
        sct.setIdentifier(100010L);
        sct.setName("TestSCT");
        sct.setSpid(spid);
        sct.setInitialBalance(1000);
        sct.setMaxBalance(2000);
        sct.setMaxRecharge(1500);
        sct.setPricePlan(pp1.getId());
        sct.setPrimaryPricePlanPeriods(6);
        sct.setPrimaryPricePlanUnit(PricePlanUnitEnum.MONTHS);
        sct.setReactivationFee(20);
        sct.setSecondaryPricePlan(pp2.getId());
        sct.setSecondaryPricePlanPeriods(10);
        sct.setSecondaryPricePlanUnit(PricePlanUnitEnum.MONTHS);
        try
        {
            sctHome.create(getContext(), sct);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Error creating test SCT: " + e.getMessage());
        }
    }
}
