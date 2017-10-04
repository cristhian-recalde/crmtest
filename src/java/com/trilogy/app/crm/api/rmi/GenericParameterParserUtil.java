package com.trilogy.app.crm.api.rmi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameterArray;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

public class GenericParameterParserUtil {

	/**
     * 
     * @param xmlLevels : xml level tokens , this would be used to locate data inside XML ( for generic parameters ) 
     * @param xmlLevelIndex : this should initially be invoked with 0
     * @param objects : This should be of type GenericParameter
     * @return :Its type should be GenericParameter 
     * 
     * @author kabhay
     */
    
    public static List<GenericParameter> parseParameters(String[] xmlLevels, int xmlLevelIndex , Object[] objects)
    {
    	/*
    	 * E.g. : xmlLevels = backTickSepartedString.split("`"); // backTickSepartedString = "xyz`abc"
    	 * 	
    	 * 	<name>priceplan</name>
    	 * 	<value >
    	 *		<name>service-id</name>
    	 *		<value>1</value>
    	 *		<name>service-id</name>
    	 *		<value>2</value>
    	 *	</value>
    	 *
    	 *	if  backtickSepratedString = "priceplan`service-id", below code would go to servuce-id level and extract all the service-ids ,prepae list of that and return it.
    	 *  
    	 *  
    	 */
    	
    	List<GenericParameter> params_return = new ArrayList<GenericParameter>();
    	
    	
    	if(xmlLevels.length <= 0)
    	{
    		return new ArrayList();
    	}
    	
    	
    	for(Object param  :  objects)
    	{
    		
    		GenericParameter gParam = (GenericParameter) param;
    		if(gParam.getName().equals(xmlLevels[xmlLevelIndex]))
    		{

    			if(xmlLevelIndex == xmlLevels.length-1)
    			{
    				/*
    				 * We are here that means we have located the data inside XML. Iterate through it, create a list and return it.
    				 */
    				
    				params_return.add(gParam);
    				continue;
    				
    			}else
    			{
    				xmlLevelIndex++;
        			if(xmlLevelIndex > xmlLevels.length-1)
        			{
        				/*
        				 * If we are here that means we did not find the requirement elements 
        				 * 
        				 * return empty list then
        				 */
        				break;
        			}
        			
        			Object value = gParam.getValue();
        			if(GenericParameterArray.class.isInstance(gParam.getValue()))
        			{
        				GenericParameterArray gpa = (GenericParameterArray) value;
        				
        				return parseParameters(xmlLevels,xmlLevelIndex,gpa.getSubElement());
        				
        			}
        			
    			}
    			
    		}
    	}
    	
    	return params_return;
    }
    
	public static <T> T parseGenericParameter(String parameterName,
			Class<T> expectedClass, GenericParameter parameter)
			throws CRMExceptionFault {
		if(parameter == null)
    	{
    		return null;
    	}
        Object value = parameter.getValue();
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
    
    public static <T extends Object> T parseNumber(String parameterName, Class<T> expectedClass, Number numberValue) throws CRMExceptionFault
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
    
    public static <T extends Object> T parseDate(String parameterName, Class<T> expectedClass, Date dateValue) throws CRMExceptionFault
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
    
    public static <T extends Object> T parseCalendar(String parameterName, Class<T> expectedClass, Calendar calendarValue) throws CRMExceptionFault
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

    public static <T extends Object> T parseString(String parameterName, Class<T> expectedClass, String stringValue) throws CRMExceptionFault
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
    
    
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss";

}
