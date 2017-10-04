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
package com.trilogy.app.crm.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import com.trilogy.app.crm.util.DeltaMap.ValueFunction;

/**
 * Test {@DeltaMap}
 * @author simar.singh@redknee.com
 *
 */
public class TestDeltaMap extends TestCase
{

    public static final String ONE = "ONE";
    public static final String TWO = "TWO";
    public static final String THREE = "THREE";
    public static final String FOUR = "FOUR";
    public static final String FIVE = "FIVE";
    public static final String SIX = "SIX";
    public static final String SEVEN = "SEVEN";
    public static final String EIGHT = "EIGHT";
    public static final String NINE = "NINE";
    public static final String TEN = "TEN";
    public static final String ELEVEN = "ELEVEN";
    public static final String UNO = "UNO";
    public static final String DOS = "DOS";
    public static final String TRES = "TRES";
    public final Set<Integer> keysOnlyInFirstMap;
    public final Set<Integer> keysOnlyInSecondMap;
    public final Set<Integer> keysInBoth;
    public final Set<Integer> keysWithValuesDifferent;
    public final Set<Integer> keysWithValueSame;
    final Map<Integer, String> firstMap;
    final Map<Integer, String> secondMap;
    public final DeltaMap<Integer, String> deltaMap;


    public TestDeltaMap()
    {
        final Map<Integer, String> mapFirst;
        {
            mapFirst = new HashMap<Integer, String>();
            mapFirst.put(1, ONE);
            mapFirst.put(2, TWO);
            mapFirst.put(3, THREE);
            mapFirst.put(5, FIVE);
            mapFirst.put(8, EIGHT);
            mapFirst.put(11, ELEVEN);
        }
        final Map<Integer, String> mapSecond;
        {
            mapSecond = new HashMap<Integer, String>();
            mapSecond.put(1, UNO);
            mapSecond.put(2, DOS);
            mapSecond.put(3, TRES);
            mapSecond.put(4, FOUR);
            mapSecond.put(5, FIVE);
            mapSecond.put(6, SIX);
        }
        firstMap = Collections.unmodifiableMap(mapFirst);
        secondMap = Collections.unmodifiableMap(mapSecond);
        keysOnlyInFirstMap = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(8, 11)));
        keysOnlyInSecondMap = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(4, 6)));
        keysInBoth = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(1, 2, 3, 5)));
        keysWithValuesDifferent = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(1, 2, 3)));
        keysWithValueSame = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(5)));
        deltaMap = new DeltaMap<Integer, String>(firstMap, secondMap);
    }


    @Test
    public void testMapsStructure()
    {
        assertTrue(deltaMap.getFirstMap().equals(firstMap));
        assertTrue(deltaMap.getSecondMap().equals(secondMap));
    }


    @Test
    public void testKeysExclusiveToFirstMap()
    {
        assertEquals(deltaMap.getKeysExclusiveToFirstMap(), (keysOnlyInFirstMap));
        assertEquals(deltaMap.getExclusiveFirstMap().keySet(), (keysOnlyInFirstMap));
    }


    @Test
    public void testKeysExclusiveToSecondMap()
    {
        assertEquals(deltaMap.getKeysExclusiveToSecondMap(), keysOnlyInSecondMap);
        assertEquals(deltaMap.getExclusiveSecondMap().keySet(), keysOnlyInSecondMap);
    }


    @Test
    public void testKeysWithValuesDifferent()
    {
        assertEquals(deltaMap.getKeysWithValuesDifferent(), keysWithValuesDifferent);
        assertEquals(deltaMap.getDifferntValueFirstMap().keySet(), keysWithValuesDifferent);
        assertEquals(deltaMap.getDifferntValueSecondMap().keySet(), keysWithValuesDifferent);
    }


    @Test
    public void testKeysWithValuesSame()
    {
        assertEquals(deltaMap.getKeysWithValuesSame(), keysWithValueSame);
        assertEquals(deltaMap.getSameValuesMap().keySet(), keysWithValueSame);
    }
    
    @Test
    public void testIntersectMapsOperation()
    {
        final Map<Integer, Integer> mapFirst;
        {
            mapFirst = new HashMap<Integer, Integer>();
            mapFirst.put(1, 1);
            mapFirst.put(2, 2);
            mapFirst.put(3, 3);
            mapFirst.put(4, 4);
            mapFirst.put(5, 5);
        }
        final Map<Integer, Integer> mapSecond;
        {
            mapSecond = new HashMap<Integer, Integer>();
            mapSecond.put(1, 10);
            mapSecond.put(2, 20);
            mapSecond.put(3, 30);
            mapSecond.put(4, 40);
            mapSecond.put(5, 50);
            mapSecond.put(6, 60);
            mapSecond.put(7, 70);
        }
        Map<Integer, Integer> intersectMap = new DeltaMap<Integer, Integer>(mapFirst, mapSecond)
                .intersectOperation(new ValueFunction<Integer>()
                {

                    @Override
                    public Integer function(Integer first, Integer second)
                    {
                        return first + second;
                    }
                });
        final Map<Integer, Integer> expectedIntersecMap;
        {
            expectedIntersecMap = new HashMap<Integer, Integer>();
            expectedIntersecMap.put(1, 11);
            expectedIntersecMap.put(2, 22);
            expectedIntersecMap.put(3, 33);
            expectedIntersecMap.put(4, 44);
            expectedIntersecMap.put(5, 55);
        }
        assertEquals(intersectMap, expectedIntersecMap);
    }

    @Test
    public void testUnionMapsOperation()
    {
        final Map<Integer, Integer> mapFirst;
        {
            mapFirst = new HashMap<Integer, Integer>();
            mapFirst.put(1, 1);
            mapFirst.put(2, 2);
            mapFirst.put(3, 3);
            mapFirst.put(4, 4);
            mapFirst.put(5, 5);
        }
        final Map<Integer, Integer> mapSecond;
        {
            mapSecond = new HashMap<Integer, Integer>();
            mapSecond.put(1, 10);
            mapSecond.put(2, 20);
            mapSecond.put(3, 30);
            mapSecond.put(4, 40);
            mapSecond.put(5, 50);
            mapSecond.put(6, 60);
            mapSecond.put(7, 70);
        }
        Map<Integer, Integer> unionMap = new DeltaMap<Integer, Integer>(mapFirst, mapSecond)
                .unionOperation(new ValueFunction<Integer>()
                {

                    @Override
                    public Integer function(Integer first, Integer second)
                    {
                        return first + second;
                    }
                });
        final Map<Integer, Integer> expectedUnionMap;
        {
            expectedUnionMap = new HashMap<Integer, Integer>();
            expectedUnionMap.put(1, 11);
            expectedUnionMap.put(2, 22);
            expectedUnionMap.put(3, 33);
            expectedUnionMap.put(4, 44);
            expectedUnionMap.put(5, 55);
            expectedUnionMap.put(6, 60);
            expectedUnionMap.put(7, 70);
        }
        assertEquals(unionMap, expectedUnionMap);
    }
    
    @Test
    public void testMapAudits()
    {
        final Map<Integer, String> rebuildFirstMap;
        {
            Map<Integer, String> map = new HashMap<Integer, String>();
            map.putAll(deltaMap.getExclusiveFirstMap());
            map.putAll(deltaMap.getDifferntValueFirstMap());
            map.putAll(deltaMap.getSameValuesMap());
            rebuildFirstMap = Collections.unmodifiableMap(map);
        }
        final Map<Integer, String> rebuildSecondMap;
        {
            Map<Integer, String> map = new HashMap<Integer, String>();
            map.putAll(deltaMap.getExclusiveSecondMap());
            map.putAll(deltaMap.getDifferntValueSecondMap());
            map.putAll(deltaMap.getSameValuesMap());
            rebuildSecondMap = Collections.unmodifiableMap(map);
        }
        assertEquals(firstMap, rebuildFirstMap);
        assertEquals(secondMap, rebuildSecondMap);
    }
}