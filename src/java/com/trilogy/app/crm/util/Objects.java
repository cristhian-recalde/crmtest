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
import java.util.Map;
import java.util.Set;


/**
 * A generic heterogeneous result holder
 * 
 * @author simar.singh@redkee.com
 * 
 */
public class Objects
{

    public Objects()
    {
        this.resultMap_ = new HashMap<Class<?>, Object>();
    }


    public Objects(Map<Class<?>, Object> resultMap)
    {
        this.resultMap_ = resultMap;
    }


    /**
     * 
     * @param <T>
     * @param type
     * @param instance
     *            of type T
     */
    public <T> Objects putObject(Class<T> type, T instance)
    {
        resultMap_.put(type, instance);
        return this;
    }


    /**
     * 
     * @param <T>
     * @param type
     * @return instance of type T
     */
    public <T> T getObject(Class<T> type)
    {
        return type.cast(resultMap_.get(type));
    }


    /**
     * 
     * @param <T>
     * @param type
     * @return
     */
    public <T> boolean hasObject(Class<T> type, Class<T>... types)
    {
        if (resultMap_.containsKey(type))
        {
            if (null != types && types.length > 0)
            {
                for (Class<?> classType : types)
                {
                    if (!resultMap_.containsKey(classType))
                    {
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }


    public void putAll(Objects objects)
    {
        resultMap_.putAll(objects.resultMap_);
    }


    public Set<Class<?>> getAllTypes()
    {
        return Collections.unmodifiableSet(resultMap_.keySet());
    }

    final protected Map<Class<?>, Object> resultMap_;
}
