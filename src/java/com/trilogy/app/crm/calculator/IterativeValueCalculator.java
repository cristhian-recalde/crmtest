package com.trilogy.app.crm.calculator;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.api.rmi.GenericParameterListParser;
import com.trilogy.app.crm.api.rmi.GenericParameterParserUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameterArray;

public class IterativeValueCalculator extends AbstractIterativeValueCalculator{

	public static final String ITERABLE_OBJECT = "IterableObject";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IterativeValueCalculator()
	{
		super();
	}

	@Override
	public Object getValueAdvanced(Context ctx)
	{
		ValueCalculator delegate = getDelegate();
		StringBuilder sb = new StringBuilder();

		String paramName = getIterableParameterName();
		String[] tokens = paramName.split("`");

		List<GenericParameter> iterableObjectsList = null;
		if(getParseFromRoot())
		{
			GenericParameterListParser parser = (GenericParameterListParser) ctx.get(GenericParameterListParser.class);
			if(parser == null)
			{
				return null;
			}

			iterableObjectsList = GenericParameterParserUtil.parseParameters(tokens, 0, parser.getParameters().toArray(new GenericParameter[0]));

		}
		else
		{
			GenericParameter parentGenericParameter = (GenericParameter) ctx.get(GenericParameter.class);
			if(parentGenericParameter == null)
			{
				return null;
			}

			Object parentValue = parentGenericParameter.getValue();
			if(parentValue instanceof GenericParameterArray)
			{
				GenericParameterArray pArray = (GenericParameterArray) parentValue;
				iterableObjectsList = GenericParameterParserUtil.parseParameters(tokens, 0, pArray.getSubElement());
			}
			else
			{
				return null;
			}
		}

		if(iterableObjectsList != null)
		{
			for(GenericParameter iterableObject : iterableObjectsList)
			{
				if(iterableObject != null)
				{
					Context subContext = ctx.createSubContext();
					subContext.put(GenericParameter.class, iterableObject);


					Object returnedObj = delegate.getValueAdvanced(subContext);

					sb.append(returnedObj.toString());
				}
			}
		}
		return sb.toString();
	}
}
