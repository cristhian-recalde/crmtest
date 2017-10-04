package com.trilogy.app.crm.priceplan.validator;

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.DependencyGroup;
import com.trilogy.app.crm.bean.DependencyGroupHome;
import com.trilogy.app.crm.bean.DependencyGroupTransientHome;
import com.trilogy.app.crm.bean.DependencyGroupTypeEnum;
import com.trilogy.app.crm.bean.PrerequisiteGroup;
import com.trilogy.app.crm.bean.PrerequisiteGroupHome;
import com.trilogy.app.crm.bean.PrerequisiteGroupTransientHome;
import com.trilogy.app.crm.bean.PricePlanGroup;
import com.trilogy.app.crm.bean.PricePlanGroupHome;
import com.trilogy.app.crm.bean.PricePlanGroupTransientHome;
import com.trilogy.app.crm.unit_test.TestContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * JUnit tests for the Price Plan Validation feature.
 * 
 * Convention:
 *   The test methods are groups of tests for a particular behaviour.  
 *   Tests are separated for Subscriber and Price Plan validation, as they
 *   potentially behave differently.  The tests implemented include 
 *   behaviour for:
 *     - Inclusive Dependency
 *     - Exclusive Dependency
 *     - Independent (Arbitrary) Dependency (even though as a dependency it doesn't enforce any validation)
 *     - Prerequisite Precondition: Inclusive, Independent (Arbitrary), Exclusive
 *     - Prerequisite Postcondition: Inclusive, Independent (Arbitrary), Exclusive  
 *       
 * Assumptions:
 *   I have assumed that for testing purposes, Bundles are the same as Services.  
 *   So tests have only been written using Services.  However, the distinction has
 *   to remain between Auxiliary Services and Services.  Auxiliary Services are not
 *   to be validated upon Price Plan Validation, since they can't be chosen in to 
 *   the selection set.  
 *   
 * Clarifications:
 *   Although Auxiliary Services cannot be selected at Price Plan Creation time,
 *   for an Inclusive Dependency Rule to apply the full set must match.  For example,
 *   An Inclusive dependency rule (ALL: Service 1, Service 2, AuxService 1)
 *   Price Plan Creation Selection: Service 1.  This selection should force the
 *   selection of Service 2, but not that of AuxService 1, at the time of price plan 
 *   creation.
 *   However, the same Inclusive Dependency used as a prerequisite should only match
 *   if the intersection of the rule set and the selection set is 1.
 * 
 * References:
 *  -PricePlanGroupValidator class for how to fill in the selection Set.
 *  -PricePlanGroupList class the validator.
 *  -DependencyGroupAdaptor, PricePlanGroupAdaptor, PrerequisiteGroupAdaptor classes for 
 *   what is stored in the respective beans
 * 
 * @author ali
 *
 */
public class TestPricePlanGroupList extends TestContextAwareTestCase 
{

    public TestPricePlanGroupList(String name)
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
        final TestSuite suite = new TestSuite(TestPricePlanGroupList.class);
        return suite;
    }
    
    public void setUp()
    {
        super.setUp();
        setupHomes();
    }
    
    //  INHERIT
    public void tearDown()
    {
        //tear down here
        teadownHomes();
        super.tearDown();
    }
    
    private void setupHomes()
    {
        getContext().put(DependencyGroupHome.class, new DependencyGroupTransientHome(getContext()));
        getContext().put(PrerequisiteGroupHome.class, new PrerequisiteGroupTransientHome(getContext()));
        getContext().put(PricePlanGroupHome.class, new PricePlanGroupTransientHome(getContext()));
    }
    
    private void teadownHomes()
    {
        try
        {
            ((Home) getContext().get(DependencyGroupHome.class)).removeAll();
            ((Home) getContext().get(PrerequisiteGroupHome.class)).removeAll();
            ((Home) getContext().get(PricePlanGroupHome.class)).removeAll();
        }
        catch (HomeException e)
        {
            //Do nothing
        }
    }
    
    
    
    public void testValidateInclusiveDependency() 
    {
        try
        {
            // Setup: Rule[ALL:1000,4000,a1100]
            String set = "1000,4000";
            String auxServices = "1100";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Price Plan Validation: Rule[ALL:1000,4000,a1100].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveDependency result: " + result);
                }
                // Inclusive dependency rule allows the intersection of the validated_set and the and_predicate_set to be Zero.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:1000,4000,a1100].  Selection[]");
            }
            
            try
            {
                // Price Plan Validation: Rule[ALL:1000,4000,a1100].  Selection[2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveDependency result: " + result);
                }
                // Inclusive dependency rule allows the intersection of the validated_set and the and_predicate_set to be Zero.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:1000,4000,a1100].  Selection[2000]");
            }
            
            try
            {
                // Price Plan Validation: Rule[ALL:1000,4000,a1100].  Selection[1000,2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveDependency result: " + result);
                }
                fail("Failed: Rule[ALL:1000,4000,a1100].  Selection[1000,2000]");
            }
            catch (IllegalStateException ise)
            {
                // Failed to include service 4000. For Price Plan validation, Auxiliary Service need not be selected.  Validation failed.
                assertTrue(true);
            }
            
            try
            {
                // Price Plan Validation: Rule[ALL:1000,4000,a1100].  Selection[1000,2000,4000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveDependency result: " + result);
                }
                //For Price Plan validation, Auxiliary Service need not be selected.  Validation is satisfied. 
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:1000,4000,a1100].  Selection[1000,2000,4000]");
            }
            
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Tests the empty Inclusive Rule
     */
    public void testValidateInclusiveRuleSpecialCase1()
    {
        try
        {
            // Setup: Rule[ALL:] 
            String set = "";
            String auxServices = "";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Subscriber Validation: Rule[ALL:].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:].  Selection[]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ALL:].  Selection[6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:].  Selection[6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ALL:].  Selection[4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:].  Selection[4000,6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ALL:].  Selection[1000,4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:].  Selection[1000,4000,6000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testValidateIndependentRule()
    {
        try
        {
            // Setup: Rule[ATLEASTONE:1000,4000,a1100] 
            String set = "1000,4000";
            String auxServices = "1100";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:1000,4000,a1100].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRule result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:1000,4000,a1100].  Selection[]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:1000,4000,a1100].  Selection[6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRule result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:1000,4000,a1100].  Selection[6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:1000,4000,a1100].  Selection[4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRule result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:1000,4000,a1100].  Selection[4000,6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:1000,4000,a1100].  Selection[1000,4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRule result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:1000,4000,a1100].  Selection[1000,4000,6000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Tests the empty Independent Rule
     */
    public void testValidateIndependentRuleSpecialCase1()
    {
        try
        {
            // Setup: Rule[ATLEASTONE:] 
            String set = "";
            String auxServices = "";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRuleSpecialCase1 result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:].  Selection[]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:].  Selection[6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRuleSpecialCase1 result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:].  Selection[6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:].  Selection[4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRuleSpecialCase1 result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:].  Selection[4000,6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:].  Selection[1000,4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRuleSpecialCase1 result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:].  Selection[1000,4000,6000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testValidateExclusiveDependency()
    {
        try
        {
            // Setup: Rule[ONLYONE:1000,4000,a1100] 
            String set = "1000,4000";
            String auxServices = "1100";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.XOR, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Price Plan Validation: Rule[ONLYONE:1000,4000,a1100]  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveDependency result: " + result);
                }
                // Exclusive dependency rule allows the intersection of the validated_set and the and_predicate_set to be Zero.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[]");
            }
            
            try
            {
                // Price Plan Validation: Rule[ONLYONE:1000,4000,a1100].  Selection[2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveDependency result: " + result);
                }
                // Exclusive dependency rule allows the intersection of the validated_set and the and_predicate_set to be Zero.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[2000]");
            }
            
            try
            {
                // Price Plan Validation: Rule[ONLYONE:1000,4000,a1100].  Selection[4000,2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("2000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveDependency result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // Only one service chosen
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[4000,2000]");
            }
            
            try
            {
                // Price Plan Validation: Rule[ONLYONE:1000,4000,a1100].  Selection[1000,2000,4000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveDependency result: " + result);
                }
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[1000,2000,4000]");
            }
            catch (IllegalStateException ise)
            {
                //Chose more than one service in exclusive set.
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Tests the empty Exclusive Rule
     */
    public void testValidateExclusiveRuleSpecialCase1()
    {
        try
        {
            // Setup: Rule[ALL:] 
            String set = "";
            String auxServices = "";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.XOR, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:].  Selection[]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:].  Selection[6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:].  Selection[6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:].  Selection[4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:].  Selection[4000,6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:].  Selection[1000,4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:].  Selection[1000,4000,6000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testSubscriberValidateInclusiveDependency() 
    {
        try
        {
            // Setup: ALL [1000,4000,a1100] 
            String set = "1000,4000";
            String auxServices = "1100";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Price Plan Validation: Rule[ALL:1000,4000,a1100].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateInclusiveDependency result: " + result);
                }
                // Inclusive dependency rule allows the intersection of the validated_set and the and_predicate_set to be Zero.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:1000,4000,a1100]].  Selection[]");
            }
            
            try
            {
                // Price Plan Validation: Rule[ALL:1000,4000,a1100].  Selection[2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateInclusiveDependency result: " + result);
                }
                // Inclusive dependency rule allows the intersection of the validated_set and the and_predicate_set to be Zero.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:1000,4000,a1100].  Selection[2000]");
            }
            
            try
            {
                // Price Plan Validation: Rule[ALL:1000,4000,a1100]. Selection[1000,2000,a1100]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("a1100");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateInclusiveDependency result: " + result);
                }
                fail("Failed: Rule[ALL:1000,4000,a1100].  Selection[1000,2000,a1100]");
            }
            catch (IllegalStateException ise)
            {
                // Failed to include service 4000 and a1100.  Validation failed.
                assertTrue(true);
            }
            
            try
            {
                // Subscriber Validation: Rule[ALL:1000,4000,a1100].  Selection[1000,2000,4000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateInclusiveDependency result: " + result);
                }
                fail("Failed: Rule[ALL:1000,4000,a1100].  Selection[1000,2000,4000]");
            }
            catch (IllegalStateException ise)
            {
                // The Auxiliary Service was not chosen.  Validation fails.
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
            
            try
            {
                // Subscriber Validation: Rule[ALL:1000,4000,a1100].  Selection[a1100]
                HashSet selectionSet = new HashSet();
                selectionSet.add("a1100");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateInclusiveDependency result: " + result);
                }
                fail("Failed: Rule[ALL:1000,4000,a1100].  Selection[a1100]");
            }
            catch (IllegalStateException ise)
            {
                // The Auxiliary Service was not chosen.  Validation fails.
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
            
            try
            {
                // Price Plan Validation: Rule[ALL:1000,4000,a1100].  Selection[1000,2000,4000,a1100]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("4000");
                selectionSet.add("a1100");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateInclusiveDependency result: " + result);
                }
                //Validation is satisfied. 
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:1000,4000,a1100].  Selection[1000,2000,4000,a1100]");
            }
            
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Tests the empty Inclusive Rule
     */
    public void testSubscriberValidateInclusiveRuleSpecialCase1()
    {
        try
        {
            // Setup: Rule[ALL:] 
            String set = "";
            String auxServices = "";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Subscriber Validation: Rule[ALL:].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:].  Selection[]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ALL:].  Selection[6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:].  Selection[6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ALL:].  Selection[4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:].  Selection[4000,6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ALL:].  Selection[1000,4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateInclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ALL:].  Selection[1000,4000,6000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testSubscriberValidateIndependentRule()
    {
        try
        {
            // Setup: Rule[ATLEASTONE:1000,4000,a1100] 
            String set = "1000,4000";
            String auxServices = "1100";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:1000,4000,a1100].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateIndependentRule result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:1000,4000,a1100].  Selection[]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:1000,4000,a1100].  Selection[6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateIndependentRule result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:1000,4000,a1100].  Selection[6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:1000,4000,a1100].  Selection[4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateIndependentRule result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:1000,4000,a1100].  Selection[4000,6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:1000,4000,a1100].  Selection[4000,6000,a1100]
                HashSet selectionSet = new HashSet();
                selectionSet.add("4000");
                selectionSet.add("6000");
                selectionSet.add("a1100");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateIndependentRule result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:1000,4000,a1100].  Selection[4000,6000,a1100]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Tests the empty Independent Rule
     */
    public void testSubscriberValidateIndependentRuleSpecialCase1()
    {
        try
        {
            // Setup: Rule[ATLEASTONE:] 
            String set = "";
            String auxServices = "";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRuleSpecialCase1 result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:].  Selection[]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:].  Selection[6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRuleSpecialCase1 result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:].  Selection[6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:].  Selection[4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRuleSpecialCase1 result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:].  Selection[4000,6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ATLEASTONE:].  Selection[1000,4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateIndependentRuleSpecialCase1 result: " + result);
                }
                // Independent rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ATLEASTONE:].  Selection[1000,4000,6000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testSubscriberValidateExclusiveDependency()
    {
        try
        {
            // Setup: Rule[ONLYONE:1000,4000,a1100] 
            String set = "1000,4000";
            String auxServices = "1100";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.XOR, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:1000,4000,a1100]  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateExclusiveDependency result: " + result);
                }
                // Exclusive dependency rule allows the intersection of the validated_set and the and_predicate_set to be Zero.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:1000,4000,a1100].  Selection[2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateExclusiveDependency result: " + result);
                }
                // Exclusive dependency rule allows the intersection of the validated_set and the and_predicate_set to be Zero.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[2000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:1000,4000,a1100].  Selection[4000,2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("2000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateExclusiveDependency result: " + result);
                }
                // Only one service chosen. Validation is satisfied.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[4000,2000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:1000,4000,a1100].  Selection[2000,a1100]
                HashSet selectionSet = new HashSet();
                selectionSet.add("2000");
                selectionSet.add("a1100");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateExclusiveDependency result: " + result);
                }
                // Only one service chosen. Validation is satisfied.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[2000,a1100]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:1000,4000,a1100].  Selection[1000,2000,4000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateExclusiveDependency result: " + result);
                }
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[1000,2000,4000]");
            }
            catch (IllegalStateException ise)
            {
                //Chose more than one service in exclusive set.
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:1000,4000,a1100].  Selection[a1100,2000,4000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("a1100");
                selectionSet.add("2000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidateExclusiveDependency result: " + result);
                }
                fail("Failed: Rule[ONLYONE:1000,4000,a1100].  Selection[a1100,2000,4000]");
            }
            catch (IllegalStateException ise)
            {
                //Chose more than one service in exclusive set.
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Tests the empty Exclusive Rule
     */
    public void testSubscriberValidateExclusiveRuleSpecialCase1()
    {
        try
        {
            // Setup: Rule[ALL:] 
            String set = "";
            String auxServices = "";
            String bundleServices = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.XOR, set, auxServices, bundleServices);
            
            // Set of Prerequisites
            set = String.valueOf(dp1.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, "", set);
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:].  Selection[]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:].  Selection[6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:].  Selection[6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:].  Selection[4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:].  Selection[4000,6000]");
            }
            
            try
            {
                // Subscriber Validation: Rule[ONLYONE:].  Selection[1000,4000,6000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidateExclusiveRuleSpecialCase1 result: " + result);
                }
                // Empty Inclusive  rule as a dependency doesn't enforce any validation
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: Rule[ONLYONE:].  Selection[1000,4000,6000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testValidatePrerequisitesIndependent()
    {
        try
        {
            // Setup: IF [ATLEASTONE:1000,8000,9000] THEN [ATLEASTONE:2000,6000,7000]
            String set = "1000,8000,9000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set);
            set = "2000,6000,7000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);

            try
            {
                //Test precondition: empty selection. no precondition match. no validation.
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesIndependent result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // No preconditions were satisfied, so validation should not have applied
                fail("Failed: IF [ATLEASTONE:1000,8000,9000] THEN [ATLEASTONE:2000,6000,7000].  Selection[]");
            }
            
            try
            {
                //Test precondition: no precondition match. no validation.
                HashSet selectionSet = new HashSet();
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesIndependent result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // No preconditions were satisfied, so validation should not have applied
                fail("Failed: IF [ATLEASTONE:1000,8000,9000] THEN [ATLEASTONE:2000,6000,7000].  Selection[3000]");
            }
            
            try
            {
                //Test precondition: precondition match. validation of empty selection set fails
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesIndependent result: " + result);
                }
                fail("Failed: IF [ATLEASTONE:1000,8000,9000] THEN [ATLEASTONE:2000,6000,7000].  Selection[1000]");
            }
            catch (IllegalStateException ise)
            {
                //Post condition not satisfied
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
            
            try
            {
                //Test postcondition: precondition match. validation of selection set fails
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesIndependent result: " + result);
                }
                fail("Failed: IF [ATLEASTONE:1000,8000,9000] THEN [ATLEASTONE:2000,6000,7000].  Selection[1000,4000]");
            }
            catch (IllegalStateException ise)
            {
                //Post condition not satisfied
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
            
            try
            {
                //Test: precondition and post condition match. validation succeeds
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesIndependent result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ATLEASTONE:1000,8000,9000] THEN [ATLEASTONE:2000,6000,7000].  Selection[1000,2000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
        
    public void testValidatePrerequisitesIndependentSpecialCase1()
    {
        try
        {
            // Setup: IF [ATLEASTONE:1000,4000] THEN [ATLEASTONE:]
            String set = "1000,4000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set);
            set = "";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);

            try
            {
                //Test precondition: empty selection. no precondition match. no validation.
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesIndependentSpecialCase1 result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // No preconditions were satisfied, so validation should not have applied
                fail("Failed: IF [ATLEASTONE:1000,4000] THEN [ATLEASTONE:].  Selection[1000]");
            }
            
            try
            {
                //Test precondition: empty selection. no precondition match. no validation.
                HashSet selectionSet = new HashSet();
                selectionSet.add("9000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesIndependentSpecialCase1 result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // No preconditions were satisfied, so validation should not have applied
                fail("Failed: IF [ATLEASTONE:1000,4000] THEN [ATLEASTONE:].  Selection[1000]");
            }
            
            try
            {
                //Test precondition: empty selection. no precondition match. no validation.
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesIndependentSpecialCase1 result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // No preconditions were satisfied, so validation should not have applied
                fail("Failed: IF [ATLEASTONE:1000,4000] THEN [ATLEASTONE:].  Selection[1000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testValidatePrerequisitesInclusivePrecondition() 
    {
        try
        {
            // Setup: IF [ALL:1000,4000] THEN [ATLEASTONE:2000]
            String set = "1000,4000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set);
            set = "2000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePrecondition result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                //Pre condition not satisfied, no validation occurred.
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                fail("Failed: IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[]");
            }
            
            try
            {
                // IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[9000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("9000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePrecondition result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                //Pre condition not satisfied, no validation occurred.
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                fail("Failed: IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[9000]");
            }
            
            try
            {
                // IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[1000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePrecondition result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                //Pre condition not satisfied, no validation occurred.
                fail("Failed: IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[1000]");
            }
            
            try
            {
                // IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[1000,4000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePrecondition result: " + result);
                }
                fail("Failed: IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[1000,4000]");
            }
            catch (IllegalStateException ise)
            {
                //Post condition not satisfied
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Price Plan Validation test:
     * Tests the Independent Dependency Rule as a Precondition when the Rule set includes Auxiliary Services.
     * See Class Description for more details.
     */
    public void testValidatePrerequisitesInclusivePreconditionSpecialCase1() 
    {
        try
        {
            // Setup: IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000]
            String set = "1000,4000";
            String auxSet = "1100";
            String bundleSet = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set, auxSet, bundleSet);
            set = "2000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePrecondition result: " + result);
                }
                // Precondition not satisfied.  Validation not attempted.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                fail("Failed: IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000]");
            }
            
            try
            {
                /* This is the closest that the Price Plan validation will get to satisfying all the conditions.
                 * Since Aux Services can't be chosen at the Price Plan Creation time, this prerequisite 
                 * can't be satisfied during Price Plan validation.
                 */
                // IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000,2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePrecondition result: " + result);
                }
                // Precondition not satisfied.  Validation not attempted.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                fail("Failed: IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    
    
    public void testValidatePrerequisitesInclusivePostcondition()
    {
        try
        {
            // IF [ALL:1000] THEN [ALL:2000,3000]
            String set = "1000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set);
            set = "2000,3000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.AND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePostcondition result: " + result);
                }
                fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000].  Selection[1000]");
            }
            catch (IllegalStateException ise)
            {
                /* The post condition: intersection between the validated_set and the and_predicate_set is empty.
            	 * The post condition must be met */
                assertTrue(true);
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePostcondition result: " + result);
                }
                fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000].  Selection[1000,3000]");
            }
            catch (IllegalStateException ise)
            {
                // The postcondition fails
                assertTrue(true);
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePostcondition result: " + result);
                }
                // All conditions were satisfied.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000].  Selection[1000,2000,3000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Price Plan Validation test:
     * Tests the Inclusive Dependency Rule as a Precondition when the Rule set includes Auxiliary Services.
     * See Class Description for more details.
     */
    public void testValidatePrerequisitesInclusivePostconditionSpecialCase1() 
    {
        try
        {
        	// IF [ALL:1000] THEN [ALL:2000,3000,a1100]
            String set = "1000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set);
            set = "2000,3000";
            String auxSet = "1100";
            String bundleSet = "";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.AND, set, auxSet, bundleSet);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,2000,3000]
            	/* For price plan validation, the post condition is fulfilled by as much that matches (all services 
            	 * and bundles are enforced, auxiliary services are ignored */ 
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesInclusivePrecondition result: " + result);
                }
                /* Precondition satisfied.  Postcondition failed. Price Plans can't select Aux Services, 
                 * so we match all after ignoring Aux Services */
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // We need a more descriptive/specific exception so we can be sure what the failure was about
            	fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,2000,3000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testValidatePrerequisitesExclusivePrecondition()
    {
        try
        {
            // Setup: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  
            String set = "1000,4000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.XOR, set);
            set = "2000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePrecondition result: " + result);
                }
                // No precondition match. No Validation needed
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[]");
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePrecondition result: " + result);
                }
                // No precondition match. No Validation needed
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[6000]");
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("5000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePrecondition result: " + result);
                }
                // Failed precondition. No Validation needed
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[1000,2000,4000]");
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePrecondition result: " + result);
                }
                //Satisfied all conditions.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[1000,2000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testValidatePrerequisitesExclusivePreconditionSpecialCase1()
    {
        try
        {
            // Setup: IF [ONLYONE:1000,4000,a1100] THEN [ATLEASTONE:2000].  
            String set = "1000,4000";
            String auxSet = "1100";
            String bundleSet = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.XOR, set, auxSet, bundleSet);
            set = "2000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePreconditionSpeceialCase1 result: " + result);
                }
                // No precondition match. No Validation needed
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[]");
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("5000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePreconditionSpeceialCase1 result: " + result);
                }
                // Failed precondition. No Validation needed
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[1000,2000,4000]");
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePreconditionSpeceialCase1 result: " + result);
                }
                //Satisfied all conditions.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[1000,2000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testValidatePrerequisitesExclusivePostcondition()
    {
        try
        {
            // Set of Services for Dependency Group 1
            String set = "1000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set);
            set = "2000,3000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.XOR, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Match nothing in the postcondition
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePostcondition result: " + result);
                }
                fail("Failed: IF [ATLEASTONE:1000] THEN [ONLYONE:2000,3000].  Selection[1000]");
            }
            catch (IllegalStateException ise)
            {
                //Fails postcondition
                assertTrue(true);
            }
            
            try
            {
                //Fail to fully satisfy the postcondition
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePostcondition result: " + result);
                }
                fail("Failed: IF [ATLEASTONE:1000] THEN [ONLYONE:2000,3000].  Selection[1000,2000,3000]");
            }
            catch (IllegalStateException ise)
            {
                //Fails postcondition
                assertTrue(true);
            }
            
            try
            {
                //Fully satisfy the postcondition
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePostcondition result: " + result);
                }
                //Satisfies all conditions
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ATLEASTONE:1000] THEN [ONLYONE:2000,3000].  Selection[1000,3000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Price Plan Validation test:
     * Tests the Exclusive Dependency Rule as a Precondition when the Rule set includes Auxiliary Services.
     * See Class Description for more details.
     */
    public void testValidatePrerequisitesExclusivePostconditionSpecialCase1() 
    {
        try
        {
        	// IF [ATLEASTONE:1000] THEN [XOR:2000,3000,a1100]
            String set = "1000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set);
            set = "2000,3000";
            String auxSet = "1100";
            String bundleSet = "";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.XOR, set, auxSet, bundleSet);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            try
            {
                // IF [ATLEASTONE:1000] THEN [XOR:2000,3000,a1100].  Selection[1000,2000,3000]
            	/* For price plan validation, the post condition is fulfilled by as much that matches (all services 
            	 * and bundles are enforced, auxiliary services are ignored */ 
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePostconditionSpecialCase1 result: " + result);
                }
                //Post condition failed.
                fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,2000,3000]");
            }
            catch (IllegalStateException ise)
            {
                assertTrue(true);
            }
            
            try
            {
                // IF [ATLEASTONE:1000] THEN [XOR:2000,3000,a1100].  Selection[1000,2000,3000]
            	/* For price plan validation, the post condition is fulfilled by as much that matches (all services 
            	 * and bundles are enforced, auxiliary services are ignored */ 
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.PRICEPLAN_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePostconditionSpecialCase1 result: " + result);
                }
                /* Precondition satisfied.  Postcondition passed. Price Plans can't select Aux Services, 
                 * so we match all after ignoring Aux Services. Match all there rest after ignoring Aux Services */
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // We need a more descriptive/specific exception so we can be sure what the failure was about
            	fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,2000,3000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test for Subscriber Validation
     */
    public void testSubscriberValidationPrerequisitesIndependent()
    {
        try
        {
            // Setup: IF [ATLEASTONE:1000] THEN [ATLEASTONE:2000]
            String set = "1000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set);
            set = "2000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);

            try
            {
                //Test precondition: empty selection. no precondition match. no validation.
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesIndependent result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // No preconditions were satisfied, so validation should not have applied
                fail("Failed: IF [ATLEASTONE:1000] THEN [ATLEASTONE:2000].  Selection[]");
            }
            
            try
            {
                //Test precondition: no precondition match. no validation.
                HashSet selectionSet = new HashSet();
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesIndependent result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // No preconditions were satisfied, so validation should not have applied
                fail("Failed: IF [ATLEASTONE:1000] THEN [ATLEASTONE:2000].  Selection[3000]");
            }
            
            try
            {
                //Test precondition: precondition match. validation of empty selection set fails
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesIndependent result: " + result);
                }
                fail("Failed: IF [ATLEASTONE:1000] THEN [ATLEASTONE:2000].  Selection[1000]");
            }
            catch (IllegalStateException ise)
            {
                //Post condition not satisfied
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
            
            try
            {
                //Test postcondition: precondition match. validation of selection set fails
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesIndependent result: " + result);
                }
                fail("Failed: IF [ATLEASTONE:1000] THEN [ATLEASTONE:2000].  Selection[1000,6000]");
            }
            catch (IllegalStateException ise)
            {
                //Post condition not satisfied
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
            
            try
            {
                //Test: precondition and post condition match. validation succeeds
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesIndependent result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ATLEASTONE:1000] THEN [ATLEASTONE:2000].  Selection[1000,2000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test for Subscriber Validation
     */
    public void testSubscriberValidationPrerequisitesInclusivePrecondition() 
    {
        try
        {
            // Setup: IF [ALL:1000,4000] THEN [ATLEASTONE:2000]
            String set = "1000,4000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set);
            set = "2000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[]
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesInclusivePrecondition result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                //Pre condition not satisfied, no validation occurred.
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                fail("Failed: IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[]");
            }
            
            try
            {
                // IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[9000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("9000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesInclusivePrecondition result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                //Pre condition not satisfied, no validation occurred.
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                fail("Failed: IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[9000]");
            }
            
            try
            {
                // IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[1000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesInclusivePrecondition result: " + result);
                }
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                //Pre condition not satisfied, no validation occurred.
                fail("Failed: IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[1000]");
            }
            
            try
            {
                // IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[1000,4000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesInclusivePrecondition result: " + result);
                }
                fail("Failed: IF [ALL:1000,4000] THEN [ATLEASTONE:2000].  Selection[1000,4000]");
            }
            catch (IllegalStateException ise)
            {
                //Post condition not satisfied
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                assertTrue(true);
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Subscriber Validation Test:
     * Tests the Independent Dependency Rule as a Precondition when the Rule set includes Auxiliary Services.
     * See Class Description for more details.
     */
    public void testSubscriberValidatePrerequisitesInclusivePreconditionSpecialCase1() 
    {
        try
        {
            // Setup: IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000]
            String set = "1000,4000";
            String auxSet = "1100";
            String bundleSet = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set, auxSet, bundleSet);
            set = "2000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidatePrerequisitesInclusivePreconditionSpecialCase1 result: " + result);
                }
                // Precondition not satisfied.  Validation not attempted.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                fail("Failed: IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000]");
            }
            
            try
            {
                // IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000,2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidatePrerequisitesInclusivePreconditionSpecialCase1 result: " + result);
                }
                // Precondition not satisfied.  Validation not attempted.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // We need a more descriptive/specific exception so we can be sure what the failure was about
                fail("Failed: IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000]");
            }
            
            try
            {
                // IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000,2000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("2000");
                selectionSet.add("a1100");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidatePrerequisitesInclusivePreconditionSpecialCase1 result: " + result);
                }
                // All Conditions Satisfied. Validation Succeeds
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ALL:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,4000,2000,a1100]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test for Subscriber Validation
     */
    public void testSubscriberValidationPrerequisitesInclusivePostcondition()
    {
        try
        {
            // IF [ALL:1000] THEN [ALL:2000,3000]
            String set = "1000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set);
            set = "2000,3000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.AND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesInclusivePostcondition result: " + result);
                }
                fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000].  Selection[1000]");
            }
            catch (IllegalStateException ise)
            {
                // The postcondition: intersection between the validated_set and the and_predicate_set is empty.
                assertTrue(true);
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesInclusivePostcondition result: " + result);
                }
                fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000].  Selection[1000,3000]");
            }
            catch (IllegalStateException ise)
            {
                // The postcondition fails
                assertTrue(true);
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesInclusivePostcondition result: " + result);
                }
                // All conditions were satisfied.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000].  Selection[1000,2000,3000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Subscriber Validation test:
     * Tests the Inclusive Dependency Rule as a Precondition when the Rule set includes Auxiliary Services.
     * See Class Description for more details.
     */
    public void testSubscriberValidatePrerequisitesInclusivePostconditionSpecialCase1() 
    {
        try
        {
        	// IF [ALL:1000] THEN [ALL:2000,3000,a1100]
            String set = "1000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.AND, set);
            set = "2000,3000";
            String auxSet = "1100";
            String bundleSet = "";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.AND, set, auxSet, bundleSet);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,2000,3000]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidatePrerequisitesInclusivePostconditionSpecialCase1 result: " + result);
                }
                // Precondition satisfied.  Postcondition failed. 
                fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,2000,3000]");
            }
            catch (IllegalStateException ise)
            {
                // We need a more descriptive/specific exception so we can be sure what the failure was about
            	assertTrue(true);
            }
            
            try
            {
                // IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,2000,3000,a1100]
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("3000");
                selectionSet.add("a1100");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidatePrerequisitesInclusivePostconditionSpecialCase1 result: " + result);
                }
                // All Conditions Satisfied. 
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
            	fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,2000,3000,a1100]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test for Subscriber Validation
     */
    public void testSubscriberValidationPrerequisitesExclusivePrecondition()
    {
        try
        {
            // Setup: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  
            String set = "1000,4000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.XOR, set);
            set = "2000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                HashSet selectionSet = new HashSet();
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesExclusivePrecondition result: " + result);
                }
                // No precondition match. No Validation needed
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[]");
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("6000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesExclusivePrecondition result: " + result);
                }
                // No precondition match. No Validation needed
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[6000]");
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("4000");
                selectionSet.add("5000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesExclusivePrecondition result: " + result);
                }
                // Failed precondition. No Validation needed
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[1000,2000,4000]");
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesExclusivePrecondition result: " + result);
                }
                //Satisfied all conditions.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000] THEN [ATLEASTONE:2000].  Selection[1000,2000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test for Subscriber Validation. With selection of Auxiliary Services
     */
    public void testSubscriberValidatePrerequisitesExclusivePreconditionSpecialCase1()
    {
        try
        {
            // Setup: IF [ONLYONE:1000,4000,a1100] THEN [ATLEASTONE:2000].  
            String set = "1000,4000";
            String auxSet = "1100";
            String bundleSet = "";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.XOR, set, auxSet, bundleSet);
            set = "2000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.IND, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("a1100");
                selectionSet.add("5000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePreconditionSpeceialCase1 result: " + result);
                }
                // Failed precondition. No Validation needed
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[1000,a1100,5000]");
            }
            
            try
            {
                HashSet selectionSet = new HashSet();
                selectionSet.add("a1100");
                selectionSet.add("2000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testValidatePrerequisitesExclusivePreconditionSpeceialCase1 result: " + result);
                }
                //Satisfied all conditions.
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ONLYONE:1000,4000,a1100] THEN [ATLEASTONE:2000].  Selection[a1100,2000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test for Subscriber Validation
     */
    public void testSubscriberValidationPrerequisitesExclusivePostcondition()
    {
        try
        {
            // Set of Services for Dependency Group 1
            String set = "1000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set);
            set = "2000,3000";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.XOR, set);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            
            try
            {
                // Match nothing in the postcondition
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesExclusivePostcondition result: " + result);
                }
                fail("Failed: IF [ATLEASTONE:1000] THEN [ONLYONE:2000,3000].  Selection[1000]");
            }
            catch (IllegalStateException ise)
            {
                //Fails postcondition. Must have at least one of the set selected.
                assertTrue(true);
            }
            
            try
            {
                //Fail to fully satisfy the postcondition
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesExclusivePostcondition result: " + result);
                }
                fail("Failed: IF [ATLEASTONE:1000] THEN [ONLYONE:2000,3000].  Selection[1000,2000,3000]");
            }
            catch (IllegalStateException ise)
            {
                //Fails postcondition
                assertTrue(true);
            }
            
            try
            {
                //Fully satisfy the postcondition
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("3000");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidationPrerequisitesExclusivePostcondition result: " + result);
                }
                //Satisfies all conditions
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                fail("Failed: IF [ATLEASTONE:1000] THEN [ONLYONE:2000,3000].  Selection[1000,3000]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Subscriber Validation test:
     * Tests the Exclusive Dependency Rule as a Precondition when the Rule set includes Auxiliary Services.
     * See Class Description for more details.
     */
    public void testSubscriberValidatePrerequisitesExclusivePostconditionSpecialCase1() 
    {
        try
        {
        	// IF [ATLEASTONE:1000] THEN [XOR:2000,3000,a1100]
            String set = "1000";
            DependencyGroup dp1 = createDependencyGroup(DEPGROUP_1, DependencyGroupTypeEnum.IND, set);
            set = "2000,3000";
            String auxSet = "1100";
            String bundleSet = "";
            DependencyGroup dp2 = createDependencyGroup(DEPGROUP_2, DependencyGroupTypeEnum.XOR, set, auxSet, bundleSet);
            
            PrerequisiteGroup prg = createPrerequisiteGroup(PREREQGROUP_1, DEPGROUP_1, DEPGROUP_2);
            
            // Set of Prerequisites
            set = String.valueOf(prg.getIdentifier());
            PricePlanGroup ppg = createPricePlanGroup(PPGROUP_1, set, "");
            
            PricePlanGroupList ppgl = new PricePlanGroupList(getContext(),PPGROUP_1,null,null);
            try
            {
                // IF [ATLEASTONE:1000] THEN [XOR:2000,3000,a1100].  Selection[1000,2000,a1100]
            	/* For price plan validation, the post condition is fulfilled by as much that matches (all services 
            	 * and bundles are enforced, auxiliary services are ignored */ 
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("2000");
                selectionSet.add("a1100");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidatePrerequisitesExclusivePostconditionSpecialCase1 result: " + result);
                }
                //Post condition failed.
                fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,2000,a1100]");
            }
            catch (IllegalStateException ise)
            {
                assertTrue(true);
            }
            
            try
            {
                // IF [ATLEASTONE:1000] THEN [XOR:2000,3000,a1100].  Selection[1000,a1100]
            	/* For price plan validation, the post condition is fulfilled by as much that matches (all services 
            	 * and bundles are enforced, auxiliary services are ignored */ 
                HashSet selectionSet = new HashSet();
                selectionSet.add("1000");
                selectionSet.add("a1100");
                boolean result = ppgl.validate(selectionSet, selectionSet,PricePlanGroupList.SUBSCRIBER_VALIDATION);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "testSubscriberValidatePrerequisitesExclusivePostconditionSpecialCase1 result: " + result);
                }
                /* Precondition satisfied.  Postcondition passed. Price Plans can't select Aux Services, 
                 * so we match all after ignoring Aux Services. Match all there rest after ignoring Aux Services */
                assertTrue(result);
            }
            catch (IllegalStateException ise)
            {
                // We need a more descriptive/specific exception so we can be sure what the failure was about
            	fail("Failed: IF [ALL:1000] THEN [ALL:2000,3000,a1100].  Selection[1000,a1100]");
            }
        }
        catch (HomeException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    private DependencyGroup createDependencyGroup(
            long id, 
            DependencyGroupTypeEnum type, 
            String services)
    throws HomeException
    {
        return createDependencyGroup(id, type, services, DependencyGroup.DEFAULT_AUXSET, DependencyGroup.DEFAULT_BUNDLESERVICESET);
    }
    
    private DependencyGroup createDependencyGroup(
            long id, 
            DependencyGroupTypeEnum type, 
            String services, 
            String auxServices, 
            String bundleServices)
        throws HomeException
    {
        DependencyGroup dp = new DependencyGroup();
        dp.setIdentifier(id);
        dp.setType(type);
        dp.setServicesset(services);
        dp.setAuxSet(auxServices);
        dp.setBundleserviceset(bundleServices);
        
        Home home = (Home) getContext().get(DependencyGroupHome.class);
        home.create(getContext(), dp);
        
        return dp;
    }
    
    private PrerequisiteGroup createPrerequisiteGroup(long id, long precondition, long postcondition)
        throws HomeException
    {
        PrerequisiteGroup prg = new PrerequisiteGroup();
        prg.setIdentifier(id);
        prg.setPrereq_service(precondition);
        prg.setDependency_list(postcondition);
        
        Home home = (Home) getContext().get(PrerequisiteGroupHome.class);
        home.create(getContext(), prg);
        
        return prg;
    }
    
    private PricePlanGroup createPricePlanGroup(long id, String prerequisites, String dependencies)
        throws HomeException
    {
        PricePlanGroup ppg = new PricePlanGroup();
        ppg.setIdentifier(id);
        ppg.setPrereq_group_list(prerequisites);
        ppg.setDepend_group_list(dependencies);
        
        Home home = (Home) getContext().get(PricePlanGroupHome.class);
        home.create(getContext(), ppg);
        
        return ppg;
    }

    /* Price Plan Group IDs: For the purpose of this test the convention 
     * is that they are the numbers from 1-9 */
    final static Long PPGROUP_1 = Long.valueOf(1);
    /* Dependency Group IDs: For the purpose of this test the convention 
     * is that they are the numbers from 100-999 */
    final static long DEPGROUP_1 = Long.valueOf(100);
    final static long DEPGROUP_2 = Long.valueOf(200);
    /* Prerequisite Group IDs: For the purpose of this test the convention 
     * is that they are the numbers from 10-99 */
    final static long PREREQGROUP_1 = Long.valueOf(10);
    /* Services/Auxiliary Services/Bundles: For the purpose of this test the convention 
     * is that they are the numbers from 1000-9999 */
}
