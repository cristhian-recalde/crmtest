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
package com.trilogy.app.crm.api.queryexecutor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.axis2.databinding.ADBBean;

import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class GenericParametersAdapter<T extends Object> implements Adapter
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GenericParametersAdapter(final Class<T> clazz)
    {
        this(clazz, "");
    }

    public GenericParametersAdapter(Class<T> clazz, String prefix)
    {
        class_ = clazz;
        prefix_ = prefix + ((prefix.isEmpty() || prefix.endsWith(SEPARATOR))?"":SEPARATOR);
    }
    @Override
    @SuppressWarnings("unchecked")
    public Object adapt(Context ctx, Object obj)
    {
        Collection<GenericParameter> result = new ArrayList<GenericParameter>();
        if (ADBBean.class.isAssignableFrom(class_))
        {
            adapt(ctx, (ADBBean) obj, (Class<ADBBean>) class_, prefix_, result);
        }
        else
        {
            setValue(ctx, (T) obj, class_, "", prefix_, result);
        }
        return result.toArray(new GenericParameter[]{});
    }
    
    /**
     * Adapts a bean of type T into into generic parameters
     * @param ctx Context
     * @param bean Bean being extracted to be inserted into the generic parameters result
     * @param clazz Bean class
     * @param prefix Prefix to be used for all properties in this bean when adding to the generic parameters collection
     * @param result GenericParamaters result
     */
    @SuppressWarnings("unchecked")
    private <S extends ADBBean> void adapt(Context ctx, S bean, Class<S> clazz, String prefix, Collection<GenericParameter> result)
    {
        Method[] methods = clazz.getMethods();
        for (Method method : methods)
        {
            // Iterate throw all bean properties: getXXX()
            if (method.getName().startsWith(GET_PREFIX) && !(method.getName().equals(GET_CLASS_METHOD_NAME))
                    && (method.getParameterTypes() == null || method.getParameterTypes().length == GET_NUMBER_OF_PARAMETERS))
            {
                String fieldName = method.getName().substring(GET_PREFIX.length());
                
                // Only retrieving field if it's set.
                if (isFieldSet(bean, fieldName))
                {
                    try
                    {
                        // Get bean property value
                        Object value = method.invoke(bean);
    
                        // Insert property value into result array if it's not empty
                        if (value!=null)
                        {
                            setValue(ctx, value, (Class<Object>) method.getReturnType(), prefix, fieldName, result);
                        }
                    }
                    catch (Throwable t)
                    {
                        LogSupport.info(ctx, this, "Unable to execute method " + method.getName() + " on object of type " + class_.getName(), t);
                    }
                }
            }
        }
    }
    
    /**
     * 
     * @param ctx Context
     * @param value Property value to be added to result
     * @param clazz Class type of property value being added
     * @param prefix Prefix to be used for when adding value to the generic parameters set
     * @param name Name of this property
     * @param result Generic parameters result
     */
    @SuppressWarnings("unchecked")
    private <S extends Object> void setValue(Context ctx, S value, Class<S> clazz, String prefix, String name, Collection<GenericParameter> result)
    {
        // If value is an array, we should add all elements in the array to the result set. To do so, we recursively
        // call the setValue method for each object in the array. The prefix now is the prefix plus the name of the
        // field and the name is the array index being processed.
        // I.E.: Identification.Identification.1, Identification.Identification.2, ...
        if (clazz.isArray())
        {
            for (int i=0;i<Array.getLength(value); i++)
            {
                setValue(ctx, Array.get(value, i), (Class<Object>) clazz.getComponentType(), prefix + name + SEPARATOR, String.valueOf(i), result);
            }
        }

        // If value is an ADBBean, we recursively call adapt method on the value, so that all the fields in this ADBBean
        // bean will be extracted. The prefix now is the prefix plus the name of the field.
        else if (ADBBean.class.isAssignableFrom(clazz))
        {
            adapt(ctx, (ADBBean) value, (Class<ADBBean>) clazz, prefix + name + SEPARATOR, result);
        }
        // If value is a simple Object, we add it to the ExecuteSet result.
        else 
        {
            GenericParameter parameter = new GenericParameter();
            parameter.setName(prefix + name);
            parameter.setValue(value);
            
            result.add(parameter);
        }
        
    }

    @Override
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        GenericParameterParser parser = new GenericParameterParser((GenericParameter[]) obj);
        T result = null;
        if (ADBBean.class.isAssignableFrom(class_))
        {
            result = (T) unAdapt(ctx, parser, (Class<? extends ADBBean>) class_, prefix_);
        }
        else
        {
            try
            {
                result = getValue(ctx, parser, "", prefix_, class_);
            }
            catch (Throwable t)
            {
                // Ignored.
            }
        }
        if (result==null)
        {
            result = instantiateBean(ctx, class_);
        }
        
        return result;
    }
    
    /**
     * Unadapts the generic parameters with the given prefix into a result of type S.
     * @param ctx Context
     * @param parser Parser with generic parameters.
     * @param clazz Class S of the bean being extracted.
     * @param prefix Prefix to be used when retrieving data from generic parameters
     * @return Object of class S with values extracted from generic parameters
     */
    @SuppressWarnings("unchecked")
    private <S extends ADBBean> S unAdapt(Context ctx, GenericParameterParser parser, Class<S> clazz, String prefix)
    {
        S result = null;
        boolean setMethods = false;
        Method[] methods = clazz.getMethods();
        for (Method method : methods)
        {
            // Iterate throw all bean properties: setXXX(value)
            if (method.getName().startsWith(SET_PREFIX)
                    && (method.getParameterTypes() != null && method.getParameterTypes().length == SET_NUMBER_OF_PARAMETERS))
            {
                setMethods = true;
                String name = method.getName().substring(SET_PREFIX_LENGTH);
                Object value = null;
                try
                {
                    // Foe each method in the bean, extract the value of the expected parameter type from the
                    // ExecuteResult, instantiate an object of the bean type, and invoke the setXXX method passing the
                    // extracted value.
                    value = getValue(ctx, parser, prefix, name, method.getParameterTypes()[0]);
                    
                    if (value!=null)
                    {
                        if (result==null)
                        {
                            result = instantiateBean(ctx, (Class<S>) clazz);
                        }
                        method.invoke(result, value);
                    }
                }
                catch (Throwable t)
                {
                    LogSupport.info(ctx, this, "Unable to execute method " + method.getName() + " on object of type " + class_.getName(), t);
                }
            }
        }
        
        // If no methods of type setXXX(value), this is probably an ADBBean enum.
        if (!setMethods)
        {
            try
            {
                // Try to return ADBBean enum value by trying to extract Long property Value for this object from ExecuteSet. 
                Object value = getValue(ctx, parser, prefix, VALUE_PROPERTY_NAME, Long.class);
                if (value!=null)
                {
                    // Make sure the bean has a getValue() method.
                    Method method = clazz.getMethod(GET_VALUE_METHOD_NAME);
                    if (method!=null)
                    {
                        Field[] fields = clazz.getFields();
                        // Iterate through class public properties of the class type until finding the one which corresponds to the
                        // extracted value.
                        for (Field field : fields)
                        {
                            if (field.getName().startsWith(VALUE_PREFIX) && clazz.isAssignableFrom(field.getType()))
                            {
                                S fieldObject = (S) field.get(clazz);
                                if (value.equals(method.invoke(fieldObject)))
                                {
                                    result = fieldObject;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            catch (Throwable t)
            {
                LogSupport.info(ctx, this, "Unable to retrieve value for Enum ADBBean on object of type " + class_.getName(), t);
            }
        }
        
        return result;
    }

    /**
     * Gets value from parser based on prefix and name passed and converts it to class clazz.
     * @param ctx Context
     * @param parser Parser with generic parameters.
     * @param prefix Prefix to be used when retrieving data from generic parameters.
     * @param name Name of the field being extracted.
     * @param clazz Class S of the field being extracted.
     * @return Object of class S with values extracted from generic parameters.
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    private <S extends Object> S getValue(Context ctx, GenericParameterParser parser, String prefix, String name, Class<S> clazz) throws Throwable
    {
        S result = null;

        // If expected value is an array, extract values of the array type from the ExecuteResult before creating the array.
        if (clazz.isArray())
        {
            result = getArrayValue(ctx, parser, prefix, name, clazz, clazz.getComponentType());
        }
        // If expected value is an ADBBean, we recursively call unadapt method to retrieve the Object of type S from the
        // ExecuteSet. The prefix now is the prefix plus the name of the field.
        else if (ADBBean.class.isAssignableFrom(clazz))
        {
            result = (S) unAdapt(ctx, parser, (Class<ADBBean>) clazz, prefix + name + SEPARATOR);
        }
        // If expected value is an Object, extract it from the ExecuteResult parameters parser.
        else
        {
            result = parser.getParameter(prefix + name, clazz);
        }
        
        return result;
    }

    /**
     * Gets an array of type S (S = Y[]) from the parser.
     * @param ctx Context
     * @param parser Parser with generic parameters.
     * @param prefix Prefix to be used when retrieving data from generic parameters
     * @param name Name of the field being extracted.
     * @param clazz Class S of the bean being extracted.
     * @param componentTypeClass Class Y (S = Y[]) which represents the type of the elements in the array of type S.
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    private <S extends Object, Y extends Object> S getArrayValue(Context ctx,
            GenericParameterParser parser, String prefix, String name, Class<S> clazz, Class<Y> componentTypeClass)
            throws Throwable
    {
        Collection<Y> values = new ArrayList<Y>();
        int i = 0;
        Y value = null;
        do
        {
            // While we are able to extract the value of the expected array component type from the
            // ExecuteResult, add it to the result Array. The prefix is the prefix plus the name of the
            // field and the name is the index being processed. Whenever we can't return a value, it means
            // we reached the end of our array.
            value = getValue(ctx, parser, prefix + name + SEPARATOR, String.valueOf(i), componentTypeClass);
            if (value!=null)
            {
                values.add(value);
            }
            i++;
        }
        while (value != null);

        // If values were extracted from result, return array. Otherwise, return null so that empty array won't be added
        // to the result bean.
        if (values.size()>0)
        {
            return (S) values.toArray((Y[]) Array.newInstance(componentTypeClass, 0));
        }
        else
        {
            return null;
        }
    }


    /**
     * Instantiates a new bean of the given class.
     * 
     * @param ctx Context
     * @param clazz Class for which the bean should be instantiated
     * @return The new bean.
     * @throws HomeException
     */
    @SuppressWarnings("unchecked")
    private <S extends Object> S instantiateBean(Context ctx, Class<S> clazz) throws HomeException
    {
        S result;
        try
        {
            try
            {
                result = (S) XBeans.instantiate(clazz, ctx);
            }
            catch (Throwable t)
            {
                result = clazz.newInstance();
            }
        }
        catch (Throwable t)
        {
            throw new HomeException("Unable to instantiate class " + clazz);
        }
        return result;
    }

    private static <BEAN extends ADBBean> boolean isFieldSet(BEAN source, String fieldName)
    {
        if (source == null || fieldName == null)
        {
            return false;
        }
        
        try
        {
            String trackerFieldName = "local" + fieldName + "Tracker";

            Class<? extends ADBBean> sourceBeanClass = source.getClass();
            Field tracker = getField(trackerFieldName, sourceBeanClass);
            if (tracker != null)
            {
                boolean wasTrackerAccessible = true;
                if (!tracker.isAccessible())
                {
                    tracker.setAccessible(true);
                    wasTrackerAccessible = false;
                }
                try
                {
                    return tracker.getBoolean(source);
                }
                finally
                {
                    if (!wasTrackerAccessible)
                    {
                        tracker.setAccessible(false);
                    }
                }
            }
        }
        catch (Exception e)
        {
            // Ignore.  Assume tracker value was true.
        }
        
        return true;
    }
    
    private static Field getField(final String fieldName, Class clazz) 
    {
        for (Field field = null; clazz != null; clazz = clazz.getSuperclass())
        {
            try
            {
                field = clazz.getDeclaredField(fieldName);
            }
            catch (Throwable t)
            {
                // NOP
            }
            if (field != null)
            {
                return field;
            }
        }
        return null;
    }
    
    private static final String SET_PREFIX = "set";
    private static final int SET_PREFIX_LENGTH = SET_PREFIX.length();
    private static final long SET_NUMBER_OF_PARAMETERS = 1;
    
    private static final String GET_PREFIX = "get";
    private static final long GET_NUMBER_OF_PARAMETERS = 0;
    
    private static final String GET_VALUE_METHOD_NAME = "getValue";
    private static final String VALUE_PROPERTY_NAME = "Value";
    private static final String VALUE_PREFIX = "value";
    
    private static final String GET_CLASS_METHOD_NAME = "getClass"; 
    
    private static final String SEPARATOR = ".";

    private final Class<T> class_;
    private final String prefix_;
    
}
