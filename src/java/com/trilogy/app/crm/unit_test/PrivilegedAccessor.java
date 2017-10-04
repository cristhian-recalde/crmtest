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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * This class is a modified version of PrivilegedAccessor provided by Charlie
 * Hubbard and Prashant Dhokte.  I have modified it to conform to Redknee coding
 * standards, removed access to private fields, and allowed access to the
 * methods that perform look-ups based on parameter type (needed when methods to
 * test have primitive parameter types).
 *
 * This class is used to access a method or field of an object no matter what
 * the access modifier of the method or field.  The syntax for accessing fields
 * and methods is out of the ordinary because this class uses reflection to peel
 * away protection.
 *
 * @author Charlie Hubbard (chubbard@iss.net)
 * @author Prashant Dhokte (pdhokte@iss.net)
 * @author gary.anderson@redknee.com
 */
public final
class PrivilegedAccessor
{
    /**
     * Prevents instantiation of this utility class.
     */
    private PrivilegedAccessor()
    {
        // Empty
    }


    /**
     * Calls a method on the given object instance.
     *
     * @param instance The object instance.
     * @param methodName The name of the method to invoke.
     *
     * @return The return value of the invoked method.
     *
     * @see PrivilegedAccessor#invokeMethod(Object, String, Object[])
     *
     * @exception NoSuchMethodException Thrown if the method cannot be found.
     * @exception InvocationTargetException Thrown if the invoked method throws an
     * exception.
     */
    public static Object invokeMethod(
        final Object instance,
        final String methodName)
        throws NoSuchMethodException, InvocationTargetException
    {
        return invokeMethod(instance, methodName, null);
    }


    /**
     * Calls a method on the given object instance with the given argument.
     * Note that this convenience method will not work on methods that have a
     * primitive valued parameter.  For example,
     *
     * <pre>
     *   Adder.addToValue(value: int): int
     * </pre>
     *
     * cannot be invoked with
     *
     * <pre>
     *   PrivilegedAccessor.invokeMethod(new Adder(4), "addToValue", Integer.valueOf(2));
     * </pre>
     *
     * because the parameter passed in is of type Integer.class, not
     * Integer.TYPE.
     *
     * Also, the actual types of the given parameter objects must exactly match
     * the declared types of the parameters in the method signature for a match
     * to be found.  If in doubt, use the {@link #getMethod getMethod()} method
     * directly.
     *
     * @param instance The object instance.
     * @param methodName The name of the method to invoke.
     * @param arg The argument to pass to the method.
     *
     * @return The return value of the invoked method.
     *
     * @see PrivilegedAccessor#invokeMethod(Object, String, Object[])
     *
     * @exception NoSuchMethodException Thrown if the method cannot be found.
     * @exception InvocationTargetException Thrown if the invoked method throws an
     * exception.
     */
    public static Object invokeMethod(
        final Object instance,
        final String methodName,
        final Object arg)
        throws NoSuchMethodException, InvocationTargetException
    {
        final Object[] args = new Object[1];
        args[0] = arg;

        return invokeMethod(instance, methodName, args);
    }


    /**
     * Calls a method on the given object instance with the given arguments.
     * Note that this convenience method will not work on methods that have
     * primitive valued parameters.  For example,
     *
     * <pre>
     *   Value.isBetweenValues(lower: int, upper: int): boolean
     * </pre>
     *
     * cannot be invoked with
     *
     * <pre>
     *   PrivilegedAccessor.invokeMethod(new Value(4), "isBetweenValues",
             new Object[] { Integer.valueOf(2), Integer.valueOf(10)});
     * </pre>
     *
     * because the parameter passed in is of type Integer.class, not
     * Integer.TYPE.
     *
     * Also, the actual types of the given parameter objects must exactly match
     * the declared types of the parameters in the method signature for a match
     * to be found.  If in doubt, use the {@link #getMethod getMethod()} method
     * directly.
     *
     * @param instance The object instance.
     * @param methodName The name of the method to invoke.
     * @param args An array of objects to pass as arguments.
     *
     * @return The return value of the invoked method.
     *
     * @see PrivilegedAccessor#invokeMethod(Object, String, Object)
     *
     * @exception NoSuchMethodException Thrown if the method cannot be found.
     * @exception InvocationTargetException Thrown if the invoked method throws an
     * exception.
     */
    public static Object invokeMethod(
        final Object instance,
        final String methodName,
        final Object[] args)
        throws NoSuchMethodException, InvocationTargetException
    {
        Class[] classTypes = null;

        if (args != null)
        {
            classTypes = new Class[args.length];

            for (int i = 0; i < args.length; ++i)
            {
                if (args[i] != null)
                {
                    classTypes[i] = args[i].getClass();
                }
            }
        }

        try
        {
            return getMethod(instance, methodName, classTypes).invoke(instance, args);
        }
        catch (final IllegalAccessException exception)
        {
            final IllegalStateException newException
                = new IllegalStateException("Unexpected inability to access the method.");
            newException.initCause(exception);
            throw newException;
        }
    }


    /**
     * Return the named method with a method signature matching classTypes
     * from the given object.
     *
     * @param instance The object instance.
     * @param methodName The name of the method to invoke.
     * @param classTypes An array of argument types.
     *
     * @return The method requested.
     *
     * @exception NoSuchMethodException Thrown if the method cannot be found.
     */
    public static Method getMethod(
        final Object instance,
        final String methodName,
        final Class[] classTypes)
        throws NoSuchMethodException
    {
        final Method accessMethod = getMethod(instance.getClass(), methodName, classTypes);
        return accessMethod;
    }


    /**
     * Return the named method with a method signature matching classTypes
     * from the given class.
     *
     * @param thisClass The target object's type.
     * @param methodName The name of the method to invoke.
     * @param classTypes An array of argument types.
     *
     * @return The method requested.
     *
     * @exception NoSuchMethodException Thrown if the method cannot be found.
     */
    public static Method getMethod(
        final Class thisClass,
        final String methodName,
        final Class[] classTypes)
        throws NoSuchMethodException
    {
        if (thisClass == null)
        {
            throw new NoSuchMethodException("Invalid method : " + methodName);
        }

        try
        {
            final Method accessMethod = thisClass.getDeclaredMethod(methodName, classTypes);
            accessMethod.setAccessible(true);
            return accessMethod;
        }
        catch (final NoSuchMethodException e)
        {
            return getMethod(thisClass.getSuperclass(), methodName, classTypes);
        }
    }

} // class
