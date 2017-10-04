package com.trilogy.app.crm.calculator;

import java.util.List;

import com.trilogy.app.crm.api.rmi.GenericParameterParserUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameterArray;

public class ConextGenericParameterComplexValueCalculator extends AbstractConextGenericParameterComplexValueCalculator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object getValueAdvanced(Context ctx) {

		GenericParameter ctxGenericParam = (GenericParameter) ctx.get(GenericParameter.class);
		if(ctxGenericParam == null)
		{
			return null;
		}

		Object gPValue = ctxGenericParam.getValue();
		String token = getInternalParameterName();
		if(gPValue != null && gPValue instanceof GenericParameterArray)
		{
			GenericParameterArray gPArray = (GenericParameterArray) gPValue;
			Object[] subElements = gPArray.getSubElement();

			if(subElements == null)
			{
				return null;
			}
			List<GenericParameter> paramList = GenericParameterParserUtil.parseParameters(token.split("`"), 0, subElements);
			if(paramList == null || paramList.size() < 1)
			{
				return null;
			}

			GenericParameter returnGenericParam = paramList.get(0);
			
			if(returnGenericParam == null)
			{
				return null;
			}

			try {
				Object obj = GenericParameterParserUtil.parseGenericParameter(returnGenericParam.getName(), Class.forName(getGenericParameterType()), returnGenericParam);
				return obj;
			} catch (Exception e) {

				LogSupport.minor(ctx, this, "Excpetion occured while trying to get value from GenericParameter.");
				return null;
			}
		}
		else
		{
			return null;
		}
	}
}
