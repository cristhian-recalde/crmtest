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
package com.trilogy.app.crm.support.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for CollectionSupport.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestCollectionSupport
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestCollectionSupport(final String name)
    {
        super(name);

        emptyList_ = Collections.unmodifiableList(new ArrayList());
        emptyMap_ = Collections.unmodifiableMap(new TreeMap());
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

        final TestSuite suite = new TestSuite(TestCollectionSupport.class);

        return suite;
    }


    // INHERIT
    @Override
    public void setUp()
    {
        super.setUp();
    }


    // INHERIT
    @Override
    public void tearDown()
    {
        super.tearDown();
    }


    /**
     * Tests that the and() method works according to the intent.
     */
    public void testAnd()
    {
        // Null parameters should throw exceptions.
        try
        {
            CollectionSupportHelper.get(getContext()).and(getContext(),null, True.instance());
            fail("Null objects collection should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).and(getContext(),emptyList_, null);
            fail("Null predicate should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        final Collection allTrue = Collections.nCopies(5, Boolean.TRUE);
        assertEquals("allTrue size", 5, allTrue.size());

        final BooleanCountingPredicate predicate = new BooleanCountingPredicate();

        assertTrue(
            "All trues return true.",
            CollectionSupportHelper.get(getContext()).and(getContext(),allTrue, predicate));

        // First false.
        {
            final List firstFalse = new ArrayList(allTrue);
            assertEquals("firstFalse size.", 5, firstFalse.size());
            firstFalse.set(0, Boolean.FALSE);

            assertFalse(
                "All trues but first return false.",
                CollectionSupportHelper.get(getContext()).and(getContext(),firstFalse, predicate));
        }

        // Last false.
        {
            final List lastFalse = new ArrayList(allTrue);
            assertEquals("lastFalse size.", 5, lastFalse.size());
            lastFalse.set(4, Boolean.FALSE);

            assertFalse(
                "All trues but last return false.",
                CollectionSupportHelper.get(getContext()).and(getContext(),lastFalse, predicate));
        }

        // Quick return.
        {
            final List notAllBooleans = new ArrayList(allTrue);
            assertEquals("notAllBooleans size.", 5, notAllBooleans.size());
            notAllBooleans.set(3, "NotABoolean");

            try
            {
                CollectionSupportHelper.get(getContext()).and(getContext(),notAllBooleans, predicate);
                fail("Impure collection should have thrown ClassCastException.");
            }
            catch (final ClassCastException exception)
            {
                // EMPTY
            }

            notAllBooleans.set(2, Boolean.FALSE);

            try
            {
                assertFalse(
                    "and() should return early.",
                    CollectionSupportHelper.get(getContext()).and(getContext(),notAllBooleans, predicate));
            }
            catch (final ClassCastException exception)
            {
                fail("and() should have returned early.");
            }
        }
    }


    /**
     * Tests that the booleanPredicate is initialized, takes a Boolean as a
     * parameter and returns it's value, or throws ClassCastException if the
     * parameter is not a Boolean.
     */
    public void testBooleanCountingPredicate()
    {
        final BooleanCountingPredicate predicate = new BooleanCountingPredicate();
        assertEquals("Initial count should be zero.", 0, predicate.getCount());

        assertTrue("Should return true.", predicate.f(getContext(),Boolean.TRUE));
        assertEquals(
            "Count after first application should be one.",
            1, predicate.getCount());

        assertFalse("Should return false.", predicate.f(getContext(),Boolean.FALSE));
        assertEquals(
            "Count after second application should be two.",
            2, predicate.getCount());

        try
        {
            predicate.f(getContext(),"NotABoolean");
            fail("predicate should have thrown an exception.");
        }
        catch (final ClassCastException exception)
        {
            // EMPTY
        }

        assertEquals(
            "Count after exception should still be two.",
            2, predicate.getCount());

    }


    /**
     * Tests that the eternally empty will remain empty.
     */
    public void testEmptyCollections()
    {
        assertNotNull("The empty list should be initialized.", emptyList_);
        assertEquals("Size of empty list.", 0, emptyList_.size());

        try
        {
            emptyList_.add("TEST");
            fail("Attempt to add item should have thrown UnsupportedOperationException.");
        }
        catch (final UnsupportedOperationException exception)
        {
            // EMPTY
        }

        assertNotNull("The empty map should be initialized.", emptyMap_);
        assertEquals("Size of empty map.", 0, emptyMap_.size());

        try
        {
            emptyMap_.put("TEST", "TEST");
            fail("Attempt to add item should have thrown UnsupportedOperationException.");
        }
        catch (final UnsupportedOperationException exception)
        {
            // EMPTY
        }
    }


    /**
     * Tests the findAll() method.
     */
    public void testFindAll()
    {
        try
        {
            CollectionSupportHelper.get(getContext()).findAll(getContext(),
                null,
                True.instance(),
                emptyList_);

            fail("A null objects collection should result in IllegalArgumentException thrown.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).findAll(getContext(),
                emptyList_,
                null,
                emptyList_);

            fail("A null predicate should result in IllegalArgumentException thrown.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).findAll(getContext(),
                emptyList_,
                True.instance(),
                null);

            fail("A null matches collection should result in IllegalArgumentException thrown.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        final List numbers = new ArrayList();
        numbers.add(new Double(3.14159));
        numbers.add(new Double(-2.17));
        numbers.add(new Double(-13.4));
        numbers.add(new Double(9.713));
        numbers.add(new Double(42.0));

        {
            final Predicate positiveNumberPredicate =
                new Predicate()
                {
                    public boolean f(Context ctx,final Object object)
                    {
                        final Double value = (Double)object;
                        return value.doubleValue() > 0.0;
                    }
                };

            final List positiveNumbers = new ArrayList();
            positiveNumbers.add(new Double(100.1));

            CollectionSupportHelper.get(getContext()).findAll(getContext(),numbers, positiveNumberPredicate, positiveNumbers);

            assertEquals("Number of positive numbers.", 4, positiveNumbers.size());
            assertEquals("Element 0", new Double(100.1), positiveNumbers.get(0));
            assertEquals("Element 1", new Double(3.14159), positiveNumbers.get(1));
            assertEquals("Element 2", new Double(9.713), positiveNumbers.get(2));
            assertEquals("Element 3", new Double(42.0), positiveNumbers.get(3));
        }

        {
            final Predicate negativeNumberPredicate =
                new Predicate()
                {
                    public boolean f(Context ctx,final Object object)
                    {
                        final Double value = (Double)object;
                        return value.doubleValue() < 0.0;
                    }
                };

            final List negativeNumbers = new ArrayList();
            negativeNumbers.add(new Double(-77.2));

            CollectionSupportHelper.get(getContext()).findAll(getContext(),numbers, negativeNumberPredicate, negativeNumbers);

            assertEquals("Number of negative numbers.", 3, negativeNumbers.size());
            assertEquals("Element 0", new Double(-77.2), negativeNumbers.get(0));
            assertEquals("Element 1", new Double(-2.17), negativeNumbers.get(1));
            assertEquals("Element 2", new Double(-13.4), negativeNumbers.get(2));
        }

        {
            final Predicate zeroNumberPredicate =
                new Predicate()
                {
                    public boolean f(Context ctx,final Object object)
                    {
                        final Double value = (Double)object;
                        return value.doubleValue() == 0.0;
                    }
                };

            final List zeroNumbers = new ArrayList();
            CollectionSupportHelper.get(getContext()).findAll(getContext(),numbers, zeroNumberPredicate, zeroNumbers);

            assertEquals("Should be no zero numbers.", 0, zeroNumbers.size());
        }

        assertNotNull(
            "Should not return null.",
            CollectionSupportHelper.get(getContext()).findAll(getContext(),emptyList_, True.instance()));

        assertNotNull(
            "Should not return null.",
            CollectionSupportHelper.get(getContext()).findAll(getContext(),emptyList_, False.instance()));
    }


    /**
     * Tests the findFirst() method.
     */
    public void testFindFirst()
    {
        try
        {
            CollectionSupportHelper.get(getContext()).findFirst(getContext(),null, True.instance());
            fail("A null collection should result in IllegalArgumentException thrown.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).findFirst(getContext(),emptyList_, null);
            fail("A null predicate should result in IllegalArgumentException thrown.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        final Collection allFalse = Collections.nCopies(5, Boolean.FALSE);
        assertEquals("allTrue size", 5, allFalse.size());

        // Find none.
        {
            final BooleanCountingPredicate predicate = new BooleanCountingPredicate();
            assertNull(
                "Shouldn't find anything.",
                CollectionSupportHelper.get(getContext()).findFirst(getContext(),allFalse, predicate));

            assertEquals(
                "All objects should have been procesed.",
                5, predicate.getCount());
        }

        // Find particular object.
        {
            final List thirdTrue = new ArrayList(allFalse);
            thirdTrue.set(2, Boolean.TRUE);
            thirdTrue.set(4, Boolean.TRUE);

            final BooleanCountingPredicate predicate = new BooleanCountingPredicate();

            final Boolean result =
                (Boolean)CollectionSupportHelper.get(getContext()).findFirst(getContext(),thirdTrue, predicate);

            assertNotNull("An object should have been found.", result);
            assertTrue("The object found should be TRUE.", result.booleanValue());
            assertEquals(
                "Three objects should have been processed", 3, predicate.getCount());
        }
    }


    /**
     * Tests the mapKeys() methods.
     */
    public void testMapKeys()
    {
        final Function valueMaker =
            new Function()
            {
                public Object f(Context ctx,final Object object)
                {
                    final Integer key = (Integer)object;
                    return new Double(key.intValue() * 3.14);
                }
            };

        assertEquals(
            "The value is the key multiplied by 3.14.",
            new Double(-6.28), valueMaker.f(getContext(), Integer.valueOf(-2)));
        assertEquals(
            "The value is the key multiplied by 3.14.",
            new Double(-3.14), valueMaker.f(getContext(), Integer.valueOf(-1)));
        assertEquals(
            "The value is the key multiplied by 3.14.",
            new Double(0.0), valueMaker.f(getContext(), Integer.valueOf(0)));
        assertEquals(
            "The value is the key multiplied by 3.14.",
            new Double(3.14), valueMaker.f(getContext(), Integer.valueOf(1)));
        assertEquals(
            "The value is the key multiplied by 3.14.",
            new Double(6.28), valueMaker.f(getContext(), Integer.valueOf(2)));
        assertFalse(
            "The value is the key multiplied by 3.14.",
            new Double(1.23).equals(valueMaker.f(getContext(), Integer.valueOf(2))));

        try
        {
            CollectionSupportHelper.get(getContext()).mapKeys(getContext(),
                null,
                valueMaker,
                emptyMap_);

            fail("A null keys parameter should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).mapKeys(getContext(),
                emptyList_,
                null,
                emptyMap_);

            fail("A null valueMaker parameter should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).mapKeys(getContext(),
                emptyList_,
                valueMaker,
                null);

            fail("A null map parameter should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        final List keys = new ArrayList();
        keys.add(Integer.valueOf(1));
        keys.add(Integer.valueOf(0));
        keys.add(Integer.valueOf(-2));
        keys.add(Integer.valueOf(2));
        keys.add(Integer.valueOf(-1));
        keys.add(Integer.valueOf(2));

        assertEquals("Key collection size.", 6, keys.size());

        final Map map = new TreeMap();
        map.put(Integer.valueOf(1000), new Double(999.999));

        CollectionSupportHelper.get(getContext()).mapKeys(getContext(),keys, valueMaker, map);

        assertEquals("Map size.", 6, map.size());

        assertEquals("Lookup.", new Double(-6.28), map.get(Integer.valueOf(-2)));
        assertEquals("Lookup.", new Double(-3.14), map.get(Integer.valueOf(-1)));
        assertEquals("Lookup.", new Double(0), map.get(Integer.valueOf(0)));
        assertEquals("Lookup.", new Double(3.14), map.get(Integer.valueOf(1)));
        assertEquals("Lookup.", new Double(6.28), map.get(Integer.valueOf(2)));
        assertEquals("Lookup.", new Double(999.999), map.get(Integer.valueOf(1000)));

        assertNull("Lookup of absent key.", map.get(Integer.valueOf(9)));

        assertNotNull(
            "Should not produce null map.",
            CollectionSupportHelper.get(getContext()).mapKeys(getContext(),emptyList_, valueMaker));
    }


    /**
     * Tests the mapValues() methods.
     */
    public void testMapValues()
    {
        final Function keyMaker =
            new Function()
            {
                public Object f(Context ctx,final Object object)
                {
                    final Double value = (Double)object;
                    return Integer.valueOf(value.intValue());
                }
            };

        assertEquals(
            "Key should be the value truncated.",
            Integer.valueOf(1), keyMaker.f(getContext(),new Double(1.23)));
        assertEquals(
            "Key should be the value truncated.",
            Integer.valueOf(2), keyMaker.f(getContext(),new Double(2.34)));
        assertEquals(
            "Key should be the value truncated.",
            Integer.valueOf(3), keyMaker.f(getContext(),new Double(3.45)));
        assertEquals(
            "Key should be the value truncated.",
            Integer.valueOf(4), keyMaker.f(getContext(),new Double(4.56)));
        assertEquals(
            "Key should be the value truncated.",
            Integer.valueOf(5), keyMaker.f(getContext(),new Double(5.67)));

        try
        {
            CollectionSupportHelper.get(getContext()).mapValues(getContext(),
                null,
                keyMaker,
                emptyMap_);

            fail("A null values parameter should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).mapValues(getContext(),
                emptyList_,
                null,
                emptyMap_);

            fail("A null keyMaker parameter should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).mapValues(getContext(),
                emptyList_,
                keyMaker,
                null);

            fail("A null map parameter should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        final List values = new ArrayList();
        values.add(new Double(1.34));
        values.add(new Double(3.56));
        values.add(new Double(2.31));
        values.add(new Double(4.33));
        values.add(new Double(9.13));
        values.add(new Double(2.56));

        assertEquals("Value collection size.", 6, values.size());

        final Map map = new TreeMap();
        map.put(Integer.valueOf(1000), new Double(999.999));

        CollectionSupportHelper.get(getContext()).mapValues(getContext(),values, keyMaker, map);

        assertEquals("Map size.", 6, map.size());

        assertEquals("Lookup.", new Double(1.34), map.get(Integer.valueOf(1)));
        assertEquals("Lookup.", new Double(2.56), map.get(Integer.valueOf(2)));
        assertEquals("Lookup.", new Double(3.56), map.get(Integer.valueOf(3)));
        assertEquals("Lookup.", new Double(4.33), map.get(Integer.valueOf(4)));
        assertEquals("Lookup.", new Double(9.13), map.get(Integer.valueOf(9)));
        assertEquals("Lookup.", new Double(999.999), map.get(Integer.valueOf(1000)));

        assertNull("Lookup of absent key.", map.get(Integer.valueOf(0)));

        assertNotNull(
            "Should not produce null map.",
            CollectionSupportHelper.get(getContext()).mapValues(getContext(),emptyList_, keyMaker));
    }


    /**
     * Tests the or() method.
     */
    public void testOr()
    {
        // Null parameters should throw exceptions.
        try
        {
            CollectionSupportHelper.get(getContext()).or(getContext(),null, True.instance());
            fail("Null objects collection should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).or(getContext(),emptyList_, null);
            fail("Null predicate should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        final Collection allFalse = Collections.nCopies(5, Boolean.FALSE);
        assertEquals("allFalse size", 5, allFalse.size());

        final BooleanCountingPredicate predicate = new BooleanCountingPredicate();

        assertFalse(
            "All falses return false.",
            CollectionSupportHelper.get(getContext()).or(getContext(),allFalse, predicate));

        // First true.
        {
            final List firstTrue = new ArrayList(allFalse);
            assertEquals("firstTrue size.", 5, firstTrue.size());
            firstTrue.set(0, Boolean.TRUE);

            assertTrue(
                "All falses but first return true.",
                CollectionSupportHelper.get(getContext()).or(getContext(),firstTrue, predicate));
        }

        // Last true.
        {
            final List lastTrue = new ArrayList(allFalse);
            assertEquals("lastTrue size.", 5, lastTrue.size());
            lastTrue.set(4, Boolean.TRUE);

            assertTrue(
                "All falses but last return true.",
                CollectionSupportHelper.get(getContext()).or(getContext(),lastTrue, predicate));
        }

        // Quick return.
        {
            final List notAllBooleans = new ArrayList(allFalse);
            assertEquals("notAllBooleans size.", 5, notAllBooleans.size());
            notAllBooleans.set(3, "NotABoolean");

            try
            {
                CollectionSupportHelper.get(getContext()).or(getContext(),notAllBooleans, predicate);
                fail("Impure collection should have thrown ClassCastException.");
            }
            catch (final ClassCastException exception)
            {
                // EMPTY
            }

            notAllBooleans.set(2, Boolean.TRUE);

            try
            {
                assertTrue(
                    "or() should return early.",
                    CollectionSupportHelper.get(getContext()).or(getContext(),notAllBooleans, predicate));
            }
            catch (final ClassCastException exception)
            {
                fail("or() should have returned early.");
            }
        }
    }


    /**
     * Tests the true and false predicates.
     */
    public void testPredicates()
    {
        assertTrue(
            "TRUE_PREDICATE.f() always returns true.",
            True.instance().f(getContext(),null));

        assertFalse(
            "FALSE_PREDICATE.f() always returns false.",
            True.instance().f(getContext(),null));
    }


    /**
     * Tests the processIf() methods (and indirectly the process() method).
     */
    public void testProcessIf()
    {
        final Function function =
            new Function()
            {
                public Object f(Context ctx,final Object object)
                {
                    return object;
                }
            };

        try
        {
            CollectionSupportHelper.get(getContext()).processIf(getContext(),
                null,
                function,
                True.instance(),
                emptyList_);

            fail("A null objects collection should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).processIf(getContext(),
                emptyList_,
                null,
                True.instance(),
                emptyList_);

            fail("A null function parameter should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).processIf(getContext(),
                emptyList_,
                function,
                null,
                emptyList_);

            fail("A null predicate parameter should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        try
        {
            CollectionSupportHelper.get(getContext()).processIf(getContext(),
                emptyList_,
                function,
                True.instance(),
                null);

            fail("A null results collection should throw IllegalArgumentException.");
        }
        catch (final IllegalArgumentException exception)
        {
            // EMPTY
        }

        {
            final Collection allTrue = Collections.nCopies(5, Boolean.TRUE);
            assertEquals("allTrue size", 5, allTrue.size());

            final BooleanCountingPredicate predicate = new BooleanCountingPredicate();

            final List results = new ArrayList();
            results.add(Boolean.TRUE);

            CollectionSupportHelper.get(getContext()).processIf(getContext(),allTrue, function, predicate, results);

            assertEquals("Result collection size.", 6, results.size());
            assertEquals("Number of objects processed.", 5, predicate.getCount());
            assertTrue("Results all true.", CollectionSupportHelper.get(getContext()).and(getContext(),results, predicate));
        }

        {
            final Collection allFalse = Collections.nCopies(5, Boolean.FALSE);
            assertEquals("allFalse size", 5, allFalse.size());

            final BooleanCountingPredicate predicate = new BooleanCountingPredicate();

            final List results = new ArrayList();
            results.add(Boolean.FALSE);

            CollectionSupportHelper.get(getContext()).processIf(getContext(),allFalse, function, predicate, results);

            assertEquals("Result collection size.", 1, results.size());
            assertEquals("Number of objects processed.", 5, predicate.getCount());
            assertFalse("Results all false.", CollectionSupportHelper.get(getContext()).or(getContext(),results, predicate));
        }

        {
            final List objects = new ArrayList();
            objects.add(Boolean.TRUE);
            objects.add(Boolean.FALSE);
            objects.add(Boolean.FALSE);
            objects.add(Boolean.TRUE);
            objects.add(Boolean.FALSE);
            objects.add(Boolean.TRUE);
            objects.add(Boolean.TRUE);

            assertEquals("Objects collection size.", 7, objects.size());

            final BooleanCountingPredicate truesPredicate = new BooleanCountingPredicate();

            final List trues = new ArrayList();
            trues.add(Boolean.TRUE);

            CollectionSupportHelper.get(getContext()).processIf(getContext(),objects, function, truesPredicate, trues);

            assertEquals("Trues collection size.", 5, trues.size());
            assertEquals("Number of objects processed.", 7, truesPredicate.getCount());
            assertTrue("Results all true.", CollectionSupportHelper.get(getContext()).and(getContext(),trues, truesPredicate));
        }
    }


    /**
     * The eternally empty list.
     */
    private final List emptyList_;

    /**
     * The eternally empty map.
     */
    private final Map emptyMap_;


    /**
     * A Predicate that takes a Boolean as a parameter and returns it's value,
     * and keeps an count of the number of objects processed.
     */
    private static class BooleanCountingPredicate
        implements Predicate
    {
        public boolean f(Context ctx,final Object object)
        {
            final Boolean value = (Boolean)object;
            ++count_;

            return value.booleanValue();
        }

        public int getCount()
        {
            return count_;
        }

        private int count_ = 0;

    } // inner-class

} // class
