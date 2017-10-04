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
package com.trilogy.app.crm.api.rmi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * Class responsible to parse generic parameters array and return the field we're looking for.
 * @author Marcio Marques
 *
 */
public class GenericParameterParser
{
    public GenericParameterParser(GenericParameter[] parameters)
    {
        parameters_ = new HashMap<String, Object>();
        if (parameters!=null)
        {
            for (GenericParameter parameter : parameters)
            {
                parameters_.put(parameter.getName(), parameter.getValue());
            }
        }
        
    }
    
    public boolean containsParam(String parameterName)
    {
        return this.parameters_.containsKey(parameterName);
    }
    
    public <T extends Object> T getParameter(String parameterName, Class<T> expectedClass, T defaultValue) throws CRMExceptionFault
    {
        T result = getParameter(parameterName, expectedClass);
        if (result==null)
        {
            result = defaultValue;
        }
        return result;
    }
    
    public <T extends Object> T getParameter(String parameterName, Class<T> expectedClass) throws CRMExceptionFault
    {
        Object value = parameters_.get(parameterName);
        final T result;
        
        if (value == null)
        {
            result = null;
        }
        else if (expectedClass.isAssignableFrom(value.getClass()))
        {
            result = (T) value;
        }
        else if (value instanceof String)
        {
            String stringValue = (String) value;
            result = parseString(parameterName, expectedClass, stringValue);
        }
        else if (value instanceof Number)
        {
            Number numberValue = (Number) value;
            result = parseNumber(parameterName, expectedClass, numberValue);
        }
        else if (value instanceof Boolean && boolean.class.isAssignableFrom(expectedClass))
        {
            result = (T) value;
        }
        else if (value instanceof Date)
        {
            Date dateValue = (Date) value;
            result = parseDate(parameterName, expectedClass, dateValue);
        }
        else if (value instanceof Calendar)
        {
            Calendar calendarValue = (Calendar) value;
            result = parseCalendar(parameterName, expectedClass, calendarValue);
        }
        else
        {
            result = null;
            RmiApiErrorHandlingSupport.simpleValidation("parameters", "Invalid format for generic parameter "
                    + parameterName + ". Expected type " + expectedClass.getClass() + " but found " + value.getClass());
        }
        
        return result;
        
    }
    
    public <T extends Object> T parseNumber(String parameterName, Class<T> expectedClass, Number numberValue) throws CRMExceptionFault
    {
        T result = null;
        
        if (expectedClass.isAssignableFrom(Short.class) || expectedClass.isAssignableFrom(short.class))
        {
            result = (T) Short.valueOf(numberValue.shortValue());
        }
        else if (expectedClass.isAssignableFrom(Integer.class) || expectedClass.isAssignableFrom(int.class))
        {
            result = (T) Integer.valueOf(numberValue.intValue());
        }
        else if (expectedClass.isAssignableFrom(Long.class) || expectedClass.isAssignableFrom(long.class))
        {
            result = (T) Long.valueOf(numberValue.longValue());
        }
        else if (expectedClass.isAssignableFrom(Byte.class) || expectedClass.isAssignableFrom(byte.class))
        {
            result = (T) Byte.valueOf(numberValue.byteValue());
        }
        else if (expectedClass.isAssignableFrom(Double.class) || expectedClass.isAssignableFrom(double.class))
        {
            result = (T) Double.valueOf(numberValue.doubleValue());
        }
        else if (expectedClass.isAssignableFrom(Float.class) || expectedClass.isAssignableFrom(float.class))
        {
            result = (T) Float.valueOf(numberValue.floatValue());
        }
        else if (expectedClass.isAssignableFrom(Boolean.class) || expectedClass.isAssignableFrom(boolean.class))
        {
            result = numberValue.longValue()==0?(T)Boolean.FALSE:(T)Boolean.TRUE;
        }
        else
        {
            RmiApiErrorHandlingSupport.simpleValidation("parameters", "Invalid format for generic parameter "
                    + parameterName + ". Expected type " + expectedClass.getClass() + " but found " + numberValue.getClass());
        }
        
        return result;
        
    }
    
    public <T extends Object> T parseDate(String parameterName, Class<T> expectedClass, Date dateValue) throws CRMExceptionFault
    {
        T result = null;
        
        if (expectedClass.isAssignableFrom(Long.class))
        {
            result = (T) Long.valueOf(dateValue.getTime());
        }
        else if (expectedClass.isAssignableFrom(Calendar.class))
        {
            result = (T) CalendarSupportHelper.get().dateToCalendar(dateValue);
        }
        else
        {
            RmiApiErrorHandlingSupport.simpleValidation("parameters", "Invalid format for generic parameter " + parameterName + ". Expected type " + expectedClass.getSimpleName());
        }
        
        return result;
    }
    
    public <T extends Object> T parseCalendar(String parameterName, Class<T> expectedClass, Calendar calendarValue) throws CRMExceptionFault
    {
        T result = null;
        
        if (expectedClass.isAssignableFrom(Long.class))
        {
            result = (T) Long.valueOf(calendarValue.getTimeInMillis());
        }
        else if (expectedClass.isAssignableFrom(Date.class))
        {
            result = (T) CalendarSupportHelper.get().calendarToDate(calendarValue);
        }
        else
        {
            RmiApiErrorHandlingSupport.simpleValidation("parameters", "Invalid format for generic parameter " + parameterName + ". Expected type " + expectedClass.getSimpleName());
        }
        
        return result;
    }

    public <T extends Object> T parseString(String parameterName, Class<T> expectedClass, String stringValue) throws CRMExceptionFault
    {
        T result = null;
        
        if (expectedClass.isAssignableFrom(Short.class) || expectedClass.isAssignableFrom(short.class))
        {
            result = (T) Short.valueOf(Short.parseShort(stringValue));
        }
        else if (expectedClass.isAssignableFrom(Integer.class) || expectedClass.isAssignableFrom(int.class))
        {
            result = (T) Integer.valueOf(Integer.parseInt(stringValue));
        }
        else if (expectedClass.isAssignableFrom(Long.class) || expectedClass.isAssignableFrom(long.class))
        {
            result = (T) Long.valueOf(Long.parseLong(stringValue));
        }
        else if (expectedClass.isAssignableFrom(Byte.class) || expectedClass.isAssignableFrom(byte.class))
        {
            result = (T) Byte.valueOf(Byte.parseByte(stringValue));
        }
        else if (expectedClass.isAssignableFrom(Double.class) || expectedClass.isAssignableFrom(Double.class))
        {
            result = (T) Double.valueOf(Double.parseDouble(stringValue));
        }
        else if (expectedClass.isAssignableFrom(Float.class) || expectedClass.isAssignableFrom(float.class))
        {
            result = (T) Float.valueOf(Float.parseFloat(stringValue));
        }
        else if (expectedClass.isAssignableFrom(Boolean.class) || expectedClass.isAssignableFrom(boolean.class))
        {
            if (stringValue.equals("y") || stringValue.equals("true"))
            {
                result = (T) Boolean.TRUE;
            }
            else if (stringValue.equals("n") || stringValue.equals("false"))
            {
                result = (T) Boolean.FALSE;
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation("parameters", "Invalid format for generic parameter " + parameterName + ". Expected type " + expectedClass.getSimpleName());
            }
        }
        else if (expectedClass.isAssignableFrom(Date.class))
        {
            try
            {
                DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
                result = (T) (Date)formatter.parse((String) stringValue);  
            }
            catch (ParseException e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("parameters", "Invalid format for generic parameter " + parameterName + ". Expected Date or String in format " + DATE_FORMAT);
            }
        }
        else if (expectedClass.isAssignableFrom(Calendar.class))
        {
            try
            {
                DateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
                result = (T) Calendar.getInstance();
                ((Calendar) result).setTime((Date)formatter.parse((String) stringValue));
            }
            catch (ParseException e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("parameters", "Invalid format for generic parameter " + parameterName + ". Expected Calendar or String in format " + DATE_TIME_FORMAT);
            }
            
        }
        else
        {
            RmiApiErrorHandlingSupport.simpleValidation("parameters", "Invalid format for generic parameter " + parameterName + ". Expected type " + expectedClass.getSimpleName());
        }
        
        return result;
    }
    
    
    Map<String, Object> parameters_;
    
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss";
}
