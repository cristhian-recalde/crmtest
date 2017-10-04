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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameterArray;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * Class responsible to parse generic parameters array and return the field we're looking for.
 * Slightly modified the GenericParameterParser class. Instead of Map, this maintains the parameters as list. 
 * This way, the same parameter can be repetitively sent by the client and can be consumed.
 * @author Marcio Marques
 * @author sgaidhani - 
 *
 */
public class GenericParameterListParser
{
    public GenericParameterListParser(GenericParameter[] parameters)
    {
        parameters_ = new ArrayList<GenericParameter>();
        if (parameters!=null)
        {
            for (GenericParameter parameter : parameters)
            {
                parameters_.add(parameter);
            }
        }
        
    }
    
    
    public boolean containsParam(String parameterName)
    {
    	return findParameterByNameAndIndex(parameterName, 0) == null ? false : true;
    }

	private GenericParameter findParameterByNameAndIndex(String parameterName, int index) {
		int cnt = 0;
		for(GenericParameter param : parameters_)
    	{
			
    		if(param.getName().equals(parameterName))
    		{
    			if(cnt == index)
    			{
    				return param;
    			}
    			else
    			{
    				cnt++;
    			}
    		}
    	}
		return null;
	}
    
    public <T extends Object> T getParameter(String parameterName, Class<T> expectedClass, T defaultValue, int index) throws CRMExceptionFault
    {
        T result = getParameter(parameterName, expectedClass, index);
        if (result==null)
        {
            result = defaultValue;
        }
        return result;
    }
    
    public <T extends Object> T getParameter(String parameterName, Class<T> expectedClass, int index) throws CRMExceptionFault
    {
    	GenericParameter parameter = findParameterByNameAndIndex(parameterName, index);
    	return GenericParameterParserUtil.parseGenericParameter(parameterName, expectedClass, parameter);
        
    }

    
    public List<GenericParameter> getParameters()
    {
    	return parameters_;
    }
    
    
    List<GenericParameter> parameters_;

}
