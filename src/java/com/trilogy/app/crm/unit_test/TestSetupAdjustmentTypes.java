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
package com.trilogy.app.crm.unit_test;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeStateEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeTransientHome;
import com.trilogy.app.crm.bean.AdjustmentTypeXMLHome;
import com.trilogy.app.crm.bean.SystemAdjustTypeMapping;
import com.trilogy.app.crm.bean.SystemAdjustTypeMappingHome;
import com.trilogy.app.crm.bean.SystemAdjustTypeMappingTransientHome;
import com.trilogy.app.crm.unit_test.utils.TransientAdjustmentTypeIdentifierSettingHome;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * Unit test sets up default Adjustment Types in a transient home for Unit Test use.
 * The System Default Adjustment Types are loaded using the XML defined at.
 * (This is the script used to load/restore Adjustment Types in new deployments.) 
 * 
 * Additionally, this unit test creates the SystemAdjustTypeMapping for default
 * adjustment types.
 * 
 * 
 * @author Angie Li
 *
 */
public class TestSetupAdjustmentTypes extends ContextAwareTestCase 
{

    
    public TestSetupAdjustmentTypes(String name)
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
        final TestSuite suite = new TestSuite(TestSetupAdjustmentTypes.class);
        return suite;
    }

    @Override
    public void setUp()
    {
        super.setUp();
        setup(getContext());

    }

    //  INHERIT
    @Override
    public void tearDown()
    {
        //tear down here
        completelyTeardown(getContext());
        
        super.tearDown();
    }


    /**
     * Creates account in an account hierarchy
     * @param ctx
     */
    public static void setup(Context ctx)
    {
        if (ctx.getBoolean(TestSetupAdjustmentTypes.class, true))
        {
            TestSetupIdentifierSequence.setup(ctx);
            setupAdjustmentTypeHome(ctx);
            setupAdjustmentTypes(ctx);
            
            //Prevent setup from running and overwriting.
            ctx.put(TestSetupAdjustmentTypes.class, false);
        }
        else
        {
            LogSupport.debug(ctx, TestSetupAdjustmentTypes.class, "Skipping TestSetupAdjustmentTypes setup again.");
        }
    }
    
    public void testSetup()
    {
        testSetup(getContext());
    }

    /** 
     * Verify the setup succeeded
     * @param ctx
     */
    public static void testSetup(Context ctx)
    {
        TestSetupIdentifierSequence.testSetup(ctx);
        try
        {
            final int totalAdjustmentTypes = AdjustmentTypeEnum.COLLECTION.size();
            
            Home home = (Home) ctx.get(AdjustmentTypeHome.class);
            Collection col = home.selectAll();
            assertEquals(totalAdjustmentTypes, col.size());
            
            //Verify a subset of the relationships
            //Payment Plan
            verifyAdjustmentRelation(ctx, AdjustmentTypeEnum.PaymentPlanLoanCredit_INDEX, AdjustmentTypeEnum.PaymentPlan_INDEX);
            verifyAdjustmentRelation(ctx, AdjustmentTypeEnum.PaymentPlanLoanAdjustment_INDEX, AdjustmentTypeEnum.PaymentPlan_INDEX);
            verifyAdjustmentRelation(ctx, AdjustmentTypeEnum.PaymentPlanLoanAllocation_INDEX, AdjustmentTypeEnum.PaymentPlan_INDEX);
            verifyAdjustmentRelation(ctx, AdjustmentTypeEnum.PaymentPlanLoanReversal_INDEX, AdjustmentTypeEnum.PaymentPlan_INDEX);
            verifyAdjustmentRelation(ctx, AdjustmentTypeEnum.PaymentPlanLoanPayment_INDEX, AdjustmentTypeEnum.StandardPayments_INDEX);
            
            Home sHome = (Home) ctx.get(SystemAdjustTypeMappingHome.class);
            Collection<SystemAdjustTypeMapping> mapping = sHome.selectAll();
            assertEquals(totalAdjustmentTypes, mapping.size()); 
            for(SystemAdjustTypeMapping element: mapping)
            {
                assertEquals("SystemAdjustTypeMapping is incorrectly configured.", element.getSysAdjustmeType(), element.getAdjType());
            }
            
        }
        catch (Exception e)
        {
            fail("Failed Setup. " + e.getMessage());
        }
    }
    
    /**
     * Delete all Adjustment Types from the "test" system.
     * @param ctx
     */
    public static void completelyTeardown(Context ctx)
    {
        try 
        {
            Home home = (Home) ctx.get(AdjustmentTypeHome.class);
            home.removeAll();
            
            home = (Home)ctx.get(SystemAdjustTypeMappingHome.class);
            home.removeAll();
        } 
        catch (Exception e) 
        {
            LogSupport.debug(ctx, TestSetupAdjustmentTypes.class.getName(), 
                    "Failed to delete all Adjustment Types and System Adjustment Type Mapping. " + e.getMessage(), e);
        }
    }


    /**
     * Install the AdjustmentTypeHome and SystemAdjustTypeMappingHome
     * @param ctx
     */
    private static void setupAdjustmentTypeHome(Context ctx)
    {
        try
        {
            Home aHome = new TransientFieldResettingHome(ctx, new AdjustmentTypeTransientHome(ctx));
            aHome = new TransientAdjustmentTypeIdentifierSettingHome(ctx, aHome);
            aHome = new HomeProxy(ctx, aHome)
            {
                /* This Home decorator creates a SystemAdjustTypeMapping when
                 * AdjustmentTypes are created.
                 * For all Created Adjustment Types, create System Adjustment Type 
                 * Mapping record and apply default configuration. 
                 * Although strictly speaking on the those AdjustmentTypes with 
                 * AdjustmentTypeEnums must be set in the mapping, we'll do it for all 
                 * AdjustmentTypes created.*/            
                @Override
                public Object create(Context ctx, Object obj)
                    throws HomeException
                {
                    AdjustmentType adj = (AdjustmentType) super.create(ctx, obj); 
                    try
                    {
                        Home sHome = (Home) ctx.get(SystemAdjustTypeMappingHome.class);
                        SystemAdjustTypeMapping mapping = new SystemAdjustTypeMapping();
                        mapping.setSysAdjustmeType(adj.getCode());
                        mapping.setAdjType(adj.getCode());
                        
                        sHome.create(mapping);
                        LogSupport.debug(ctx, TestSetupAdjustmentTypes.class.getName(),
                                "Successfully created SystemAdjustTypeMapping [" + mapping + "]");
                    }
                    catch (Exception e)
                    {
                        throw new HomeException("Failed to generate default SystemAdjustTypeMapping " 
                                + adj.getCode() + " due to " + e.getMessage(), e);
                    }
                    return adj;
                }
            };
            aHome = new SortingHome(ctx, aHome, new Comparator()
            {
                //Sort in ascending order by AdjustmentType.code
                public int compare(Object obj1, Object obj2)
                {
                    AdjustmentType bean = (AdjustmentType) obj1;
                    AdjustmentType other = (AdjustmentType) obj2;
                    if (bean.getCode() > other.getCode())
                    {
                        return 1;
                    }
                    else if (bean.getCode() < other.getCode())
                    {
                        return -1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            }); // Just for ease of logging reading
            ctx.put(AdjustmentTypeHome.class, aHome);
        }
        catch(Exception e)
        {
            fail("Failed setup AdjustmentTypeHome. " + e.getMessage());
        }
        
        Home sysHome = new TransientFieldResettingHome(ctx, new SystemAdjustTypeMappingTransientHome(ctx));
        sysHome = new SortingHome(ctx, sysHome, new Comparator()
        {
            //Sort in ascending order by AdjustmentType.code
            public int compare(Object obj1, Object obj2)
            {
                SystemAdjustTypeMapping bean = (SystemAdjustTypeMapping) obj1;
                SystemAdjustTypeMapping other = (SystemAdjustTypeMapping) obj2;
               if (bean.getSysAdjustmeType() > other.getSysAdjustmeType())
               {
                   return 1;
               }
               else if (bean.getSysAdjustmeType() < other.getSysAdjustmeType())
               {
                   return -1;
               }
               else
               {
                   return 0;
               }
            }
        }); // Just for ease of logging reading
        ctx.put(SystemAdjustTypeMappingHome.class, sysHome);
    }


    /**
     * Create Adjustment Type profiles for all Adjustment codes in the 
     * Adjustment Type Enum Collection.
     * @param ctx
     */
    private static void setupAdjustmentTypes(Context ctx)
    {
        try
        {
            loadFromFile(ctx);
            
            Home home = (Home) ctx.get(AdjustmentTypeHome.class);
            Collection<AdjustmentType> all = home.selectAll();
            for (AdjustmentType adj:all)
            {
                adj.setAdjustmentSpidInfo(getDefaultSpidInfo());
                home.store(adj);
                LogSupport.debug(ctx, TestSetupAdjustmentTypes.class.getName(),
                        "Successfully set default parameters for adj=" + adj.getCode());
            }
        }
        catch (Exception e)
        {
            fail("Failed to setup Adjustment Types. "  + e.getMessage());
        }
    }
    

    /**
     * Create an active Adjustment Type and System Adjustment Type Mapping using 
     * the given information.
     * Example use: create Recurring Charge adjustment type for Services.
     * @param ctx
     * @param identifier Adjustment Type Code
     * @param parentId  Parent Adjustment Type Code
     * @param name Name of the Adjustment Type
     */
    public static void createAdjustmentType(Context ctx, int identifier, int parentId, String name)
    {
        AdjustmentType adj = new AdjustmentType();
        adj.setName(name);
        adj.setDesc(name);
        adj.setCode(identifier);
        adj.setParentCode(parentId);
        adj.setState(AdjustmentTypeStateEnum.ACTIVE);
        adj.setAdjustmentSpidInfo(getDefaultSpidInfo());
        try
        {
            Home home = (Home) ctx.get(AdjustmentTypeHome.class);
            home.create(ctx, adj);
        }
        catch(Exception e)
        {
            LogSupport.debug(ctx, TestSetupAdjustmentTypes.class.getName(), 
                    "Failed to create Adjustment Type="  + adj.getCode() + ": " + adj.getName(), e);
        }
        /*
         * Matching System Adjustment Type Mapping is created by Home decorator in AdjustmentType pipeline
         */
    }
     

    private static Home createXMLHome(Context ctx)
    {
        // Cannot use UnitTestSupport.isTestRunningInXTest() as currently our test cases cannot 
        // be run with the application context (we use TestSuite.suite() instead of TestSuite.suite(ctx) 
        // in XTest Test configuration)
        String filename = CoreSupport.getProjectHome(ctx) + File.separator + ADJUSTMENT_TYPE_RELATIVE_LOCATION;
        File f = new File(filename);
        if (f.exists() && f.isFile())
        {
            return new AdjustmentTypeXMLHome(ctx, filename);
        }
        else
        {
            f = new File(XML_INPUT_FILE);
            if (f.exists() && f.isFile())
            {
                return new AdjustmentTypeXMLHome(ctx, XML_INPUT_FILE);
            }
            else
            {
                throw new IllegalStateException("Cannot locate file: " + XML_INPUT_FILE + " or file: " + filename);
            }
        }
    }

    private static void loadFromFile(Context ctx) throws HomeException
    {
        LogSupport.debug(ctx, TestSetupAdjustmentTypes.class.getName(),
                "Loading into the Sytem from file " + XML_INPUT_FILE);

        Home home = (Home) ctx.get(AdjustmentTypeHome.class);
        Home xmlHome = createXMLHome(ctx);

        Homes.copy(xmlHome, home);
        
        LogSupport.debug(ctx, TestSetupAdjustmentTypes.class.getName(),
                "Completed loading from the file " + XML_INPUT_FILE);
    }
    
    
    /**
     * Used in unit tests to verify the given adjustment type relationship
     * @param ctx
     * @param adjustmentTypeId identifier for adjustment type
     * @param parentId the correct parent code for the adjustment type
     */
    public static void verifyAdjustmentRelation(
            final Context ctx, 
            final short adjustmentTypeId, 
            final short parentId)
    {
         try
         {
             Home home = (Home) ctx.get(AdjustmentTypeHome.class);
             AdjustmentType adj = (AdjustmentType) home.find(ctx, Integer.valueOf(adjustmentTypeId));
             assertNotNull("Adjustment Type=" + adj + " doesn't exist in the system. ", adj);
             String msg = " Adj=" + adjustmentTypeId + " has ParentId=" + adj.getParentCode();
             assertTrue("Adjustment Type Hierarchy is incorrectly configured." + msg + " but should have ParentId=" + parentId,
                     adj.getParentCode() == parentId);
             
             LogSupport.debug(ctx, TestSetupAdjustmentTypes.class.getName(), "TEST: Verified "+ msg);
         }
         catch (Exception e)
         {
             fail("Failed to retrieve adjustment type " 
                     + adjustmentTypeId + ". Error: " + e.getMessage());
         }
    }

    /**
     * Default SPID INFO for all Adjustment Types in this unit test.
     * @return
     */
    private static Map getDefaultSpidInfo()
    {
        if (spidInfo_ == null)
        {
            spidInfo_ = new HashMap();
            AdjustmentInfo adjInfo= new AdjustmentInfo();
            adjInfo.setSpid(TestSetupAccountHierarchy.SPID_ID);
            adjInfo.setInvoiceDesc("Unit Test Adjustment");
            adjInfo.setGLCode(DEFAULT_GLCODE);
            adjInfo.setTaxAuthority(DEFAULT_TAX_AUTH);
            spidInfo_.put(TestSetupAccountHierarchy.SPID_ID, adjInfo);
        }
        return spidInfo_;
    }
    
    
    private static Map spidInfo_ = null;
    public static final String DEFAULT_GLCODE = "UnitTest_GLCode";
    public static final int DEFAULT_TAX_AUTH = 1;
    private static final String ADJUSTMENT_TYPE_RELATIVE_LOCATION = "cfg/new_deployment/xml/com.redknee.app.crm.bean.AdjustmentType.xml";
    private static final String XML_INPUT_FILE = "/redknee/app/crm/trunk/customer/redknee/common/" + ADJUSTMENT_TYPE_RELATIVE_LOCATION;

}
