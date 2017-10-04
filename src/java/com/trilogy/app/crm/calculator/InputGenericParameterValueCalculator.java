/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.calculator;

import com.trilogy.app.crm.api.rmi.GenericParameterListParser;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameterArray;

public class InputGenericParameterValueCalculator extends AbstractInputGenericParameterValueCalculator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object getValueAdvanced(Context ctx) {

		GenericParameterListParser parser = (GenericParameterListParser) ctx.get(GenericParameterListParser.class);

		Object obj  = null;

		try 
		{
			String paramName = getGenericParameterName();
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this, "parameterName :" + paramName);
			}
			String[] paremeterNames = paramName.split("`");

			if(paremeterNames.length > 1)
			{
				Object[] objArray = null;
				int i = 0;
				while(i < (paramName.length()))
				{
					String structureName = paremeterNames[i];

					if(i==0)
					{
						GenericParameterArray objects = parser.getParameter(structureName, GenericParameterArray.class,0);
						if(objects == null)
						{
							break; 
						}
						
						objArray = objects.getSubElement();
					}
					else if(i == (paremeterNames.length -1))
					{
						int cnt = 0;
						for(Object objSubElement : objArray)
						{
							if(objSubElement instanceof GenericParameter)
							{
								GenericParameter genericParam  = (com.redknee.util.crmapi.wsdl.v2_0.types.GenericParameter) objSubElement;
								if(genericParam.getName().equals(structureName))
								{
										Object value = genericParam.getValue();
										if(LogSupport.isDebugEnabled(ctx))
											{
												LogSupport.debug(ctx, this, paramName + " evaluated to be :" + value);
											}
										return value;
								}
							}
						}
					}
					else
					{
						for(Object objSubElement : objArray)
						{
							if(objSubElement instanceof GenericParameter)
							{
								GenericParameter genericParam  = (GenericParameter) objSubElement;
								if(genericParam.getName().equals(structureName))
								{

									Object paramArrayObj = genericParam.getValue();

									if(paramArrayObj instanceof GenericParameterArray)
									{
										GenericParameterArray arrayObjects =  (GenericParameterArray)paramArrayObj;
										objArray = arrayObjects.getSubElement();
										break;
									}
								}
							}
						}
					}
					i++;
				}
			}
			else
			{
				obj = parser.getParameter(getGenericParameterName(), Class.forName(getGenericParameterType()),0);
			}
		} 
		//Even though here exception is caught, it is just eaten up and null would be returned.
		//The Decorator Value Calculator can decide the further action on this.
		catch (Exception e)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				StringBuilder buf = new StringBuilder();
				buf.append("Exception occured while trying to retrieve the Generic Parameter :");
				buf.append("Name :");
				buf.append(getGenericParameterName());
				buf.append(", ObjectType :");
				buf.append(getGenericParameterType());

				LogSupport.debug(ctx, this, "" + getGenericParameterName(), e);
			}
		}

		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "Evaluation Complete. Calculated value is :" + obj);
		}
		return obj;
	}
}
