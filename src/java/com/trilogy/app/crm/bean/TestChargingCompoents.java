/*
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
package com.trilogy.app.crm.bean;

import java.util.StringTokenizer;

import com.trilogy.app.crm.bean.core.ChargingComponents;
import com.trilogy.app.crm.bean.core.ChargingComponentsConfig;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the Component Rating Related Implemetation
 * @author simar.singh@redknee.com
 * 
 */
public class TestChargingCompoents extends TestCase
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestChargingCompoents.class);
        return suite;
    }


    public TestChargingCompoents()
    {
    }


    /**
     * This test will enforce maintenance of unit-test should the number of components in the implements change.
     */
    public void testUunitTestConsitency()
    {
        assertEquals("Verifying the number of components in source", ChargingComponentPlaceEnum.COLLECTION.size(),
                MAX_NUMBER_OF_COMPONENTS);
        assertEquals("Verifying the number of components in test data", new StringTokenizer(TEST_ALL_COMPONENTS_STRING, ";")
                .countTokens(), MAX_NUMBER_OF_COMPONENTS);
    }
    
    /**
     * 
     * Ensure Components can be built String (parsing)
     * 
     * @author simar.singh@redknee.com
     */
    @org.junit.Test
    public void testComponentsBuildingFromString()
    {
        final ChargingComponents actual;
        {
            actual = new ChargingComponents().setAllComponentsFromString(getTrueComponentString(),
                    getTestConfiguration());
        }
        final ChargingComponents expected;
        {
            expected = getTrueComponents();
        }
        assertEquals("Component Building from String", expected, actual);
    }


    /**
     * 
     * Ensure Components which are disable get failed at parsing
     * 
     * @author simar.singh@redknee.com
     */
    @org.junit.Test
    public void testEnableDisableComponent()
    {
        final ChargingComponentsConfig configuration;
        {
            configuration = getTestConfiguration();
            configuration.getComponentFirst().setEnabled(false);
        }
        boolean illegalStateExceptionRaised = false;
        try
        {
            new ChargingComponents().setAllComponentsFromString(getTrueComponentString(), configuration);
        }
        catch (IllegalStateException e)
        {
            illegalStateExceptionRaised = true;
        }
        assertTrue("Expected Exception [" + IllegalStateException.class.getSimpleName() + "] ",
                illegalStateExceptionRaised);
    }


    /**
     * 
     * Ensure ChargingComponentPlaceEnum works on component correctly
     * 
     * @author simar.singh@redknee.com
     */
    @org.junit.Test
    public void testEnumeratedComponents()
    {
        final ChargingComponents components;
        {
            components = getTrueComponents();
        }
        {
            final ComponentCharge actualFirst;
            {
                actualFirst = getTrueFirstComponent();
            }
            final ComponentCharge expectedFirst;
            {
                expectedFirst = ChargingComponentPlaceEnum.FIRST.getComponent(components);
            }
            assertEquals("Component Building from String", expectedFirst, actualFirst);
        }
        {
            final ComponentCharge actualSecond;
            {
                actualSecond = getTrueSecondComponent();
            }
            final ComponentCharge expectedSecond;
            {
                expectedSecond = ChargingComponentPlaceEnum.SECOND.getComponent(components);
            }
            assertEquals("Component Building from String", expectedSecond, actualSecond);
        }
        {
            final ComponentCharge actualThird;
            {
                actualThird = getTrueThirdComponent();
            }
            final ComponentCharge expectedThird;
            {
                expectedThird = ChargingComponentPlaceEnum.THIRD.getComponent(components);
            }
            assertEquals("Component Building from String", expectedThird, actualThird);
        }
    }


    /**
     * @author simar.singh@redknee.com
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }


    static ChargingComponentsConfig getTestConfiguration()
    {
        final ChargingComponentsConfig config = new ChargingComponentsConfig();
        {
            final ChargingComponentMetaData firstComponentmetadata;
            {
                firstComponentmetadata = new ChargingComponentMetaData();
                firstComponentmetadata.setEnabled(true);
                firstComponentmetadata.setID(1);
                firstComponentmetadata.setName("ONE");
            }
            final ChargingComponentMetaData secondComponentmetadata;
            {
                secondComponentmetadata = new ChargingComponentMetaData();
                secondComponentmetadata.setEnabled(true);
                secondComponentmetadata.setID(2);
                secondComponentmetadata.setName("TWO");
            }
            final ChargingComponentMetaData thridChargingComponentMetaData;
            {
                thridChargingComponentMetaData = new ChargingComponentMetaData();
                thridChargingComponentMetaData.setEnabled(true);
                thridChargingComponentMetaData.setID(3);
                thridChargingComponentMetaData.setName("THREE");
            }
            config.setComponentFirst(firstComponentmetadata);
            config.setComponentSecond(secondComponentmetadata);
            config.setComponentThird(thridChargingComponentMetaData);
        }
        return config;
    }


    private static ChargingComponents getTrueComponents()
    {
        final ChargingComponents components = new ChargingComponents();
        {
            components.setFirst(getTrueFirstComponent());
            components.setSecond(getTrueSecondComponent());
            components.setThird(getTrueThirdComponent());
        }
        return components;
    }


    static ComponentCharge getTrueFirstComponent()
    {
        final ComponentCharge componentCharge = new ComponentCharge();
        {
            componentCharge.setCharge(100);
            componentCharge.setRate(10);
            componentCharge.setGlCode("ONE");
        }
        return componentCharge;
    }


    static ComponentCharge getTrueSecondComponent()
    {
        final ComponentCharge componentCharge = new ComponentCharge();
        {
            componentCharge.setCharge(200);
            componentCharge.setRate(20);
            componentCharge.setGlCode("TWO");
        }
        return componentCharge;
    }


    static ComponentCharge getTrueThirdComponent()
    {
        final ComponentCharge componentCharge = new ComponentCharge();
        {
            componentCharge.setCharge(300);
            componentCharge.setRate(30);
            componentCharge.setGlCode("THREE");
        }
        return componentCharge;
    }


    static String getTrueComponentString()
    {
        return TEST_ALL_COMPONENTS_STRING;
    }

    private static final String TEST_ALL_COMPONENTS_STRING = "1|10|ONE|100;2|20|TWO|200;3|30|THREE|300";
    private static final int MAX_NUMBER_OF_COMPONENTS = 3;
}
