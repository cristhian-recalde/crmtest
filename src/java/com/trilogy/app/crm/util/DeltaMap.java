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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Operations on Map Not thread safe; does not return clones but original values
 * 
 * @author simar.singh@redknee.com
 * 
 * @param <KEY>
 * @param <VALUE>
 */
public class DeltaMap<KEY, VALUE>
{

    public DeltaMap(Map<KEY, VALUE> first, Map<KEY, VALUE> second)
    {
        firstMap_ = Collections.unmodifiableMap(first);
        secondMap_ = Collections.unmodifiableMap(second);
        initialize();
    }


    private final void initialize()
    {
        keysAll_.addAll(firstMap_.keySet());
        keysAll_.addAll(secondMap_.keySet());
        for (KEY key : keysAll_)
        {
            final VALUE valueFromFirstMap = firstMap_.get(key);
            final VALUE valueFromSecondMap = secondMap_.get(key);
            if (null == valueFromFirstMap)
            {
                keysExclusiveToSecondMap_.add(key);
                exclusiveSecondMap_.put(key, valueFromSecondMap);
            }
            else if (null == valueFromSecondMap)
            {
                keysExclusiveToFirstMap_.add(key);
                exclusiveFirstMap_.put(key, valueFromFirstMap);
            }
            else
            {
                keysCommonToBoth_.add(key);
                if (valueFromFirstMap.equals(valueFromSecondMap))
                {
                    keysWithValuesSame_.add(key);
                    sameValuesMap_.put(key, valueFromFirstMap);
                }
                else
                {
                    keysWithValuesDifferent_.add(key);
                    differntValueFirstMap_.put(key, valueFromFirstMap);
                    differntValueSecondMap_.put(key, valueFromSecondMap);
                }
            }
        }
    }


    public Set<KEY> getKeysExclusiveToFirstMap()
    {
        return Collections.unmodifiableSet(keysExclusiveToFirstMap_);
    }


    public Set<KEY> getKeysExclusiveToSecondMap()
    {
        return Collections.unmodifiableSet(keysExclusiveToSecondMap_);
    }


    public Set<KEY> getKeysCommonToBoth()
    {
        return Collections.unmodifiableSet(keysCommonToBoth_);
    }


    public Set<KEY> getKeysWithValuesSame()
    {
        return Collections.unmodifiableSet(keysWithValuesSame_);
    }


    public Set<KEY> getKeysWithValuesDifferent()
    {
        return Collections.unmodifiableSet(keysWithValuesDifferent_);
    }


    public Map<KEY, VALUE> getFirstMap()
    {
        return firstMap_;
    }


    public Map<KEY, VALUE> getSecondMap()
    {
        return secondMap_;
    }


    public Map<KEY, VALUE> getExclusiveFirstMap()
    {
        return Collections.unmodifiableMap(exclusiveFirstMap_);
    }


    public Map<KEY, VALUE> getExclusiveSecondMap()
    {
        return Collections.unmodifiableMap(exclusiveSecondMap_);
    }


    public Map<KEY, VALUE> getSameValuesMap()
    {
        return Collections.unmodifiableMap(sameValuesMap_);
    }


    public Map<KEY, VALUE> getDifferntValueFirstMap()
    {
        return Collections.unmodifiableMap(differntValueFirstMap_);
    }


    public Map<KEY, VALUE> getDifferntValueSecondMap()
    {
        return Collections.unmodifiableMap(differntValueSecondMap_);
    }


    public Map<KEY, VALUE> unionOperation(ValueFunction<VALUE> valueFunction)
    {
        Map<KEY, VALUE> map = intersectOperation(valueFunction);
        map.putAll(exclusiveFirstMap_);
        map.putAll(exclusiveSecondMap_);
        return map;
    }


    public Map<KEY, VALUE> intersectOperation(ValueFunction<VALUE> valueFunction)
    {
        HashMap<KEY, VALUE> map = new HashMap<KEY, VALUE>();
        for (KEY key : keysCommonToBoth_)
        {
            map.put(key, valueFunction.function(firstMap_.get(key), secondMap_.get(key)));
        }
        return map;
    }

    /**
     * Interface that defines a function on two values of the same type
     * @author simar.singh@redknee.com
     *
     * @param <VALUETYPE>
     */
    public static interface ValueFunction<VALUETYPE extends Object>
    {

        public VALUETYPE function(VALUETYPE first, VALUETYPE second);
    }

    private final Map<KEY, VALUE> exclusiveFirstMap_ = new HashMap<KEY, VALUE>();
    private final Map<KEY, VALUE> exclusiveSecondMap_ = new HashMap<KEY, VALUE>();
    private final Map<KEY, VALUE> sameValuesMap_ = new HashMap<KEY, VALUE>();
    private final Map<KEY, VALUE> differntValueFirstMap_ = new HashMap<KEY, VALUE>();
    private final Map<KEY, VALUE> differntValueSecondMap_ = new HashMap<KEY, VALUE>();
    private final Set<KEY> keysExclusiveToFirstMap_ = new HashSet<KEY>();
    private final Set<KEY> keysExclusiveToSecondMap_ = new HashSet<KEY>();
    private final Set<KEY> keysCommonToBoth_ = new HashSet<KEY>();
    private final Set<KEY> keysWithValuesSame_ = new HashSet<KEY>();
    private final Set<KEY> keysWithValuesDifferent_ = new HashSet<KEY>();
    private final Set<KEY> keysAll_ = new HashSet<KEY>();
    private final Map<KEY, VALUE> firstMap_;
    private final Map<KEY, VALUE> secondMap_;
}
