package com.trilogy.app.crm.calculator;

import com.trilogy.app.crm.api.rmi.GenericParameterParserUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;

public class ConextGenericParameterSimpleValueCalculator extends AbstractConextGenericParameterSimpleValueCalculator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object getValueAdvanced(Context ctx) {

		GenericParameter gParam = (GenericParameter) ctx.get(GenericParameter.class);
		if(gParam == null)
		{
			return null;
		}
		try {
			Object obj = GenericParameterParserUtil.parseGenericParameter(gParam.getName(), Class.forName(getGenericParameterType()), gParam);
			return obj;
		} catch (Exception e) {

			LogSupport.minor(ctx, this, "Excpetion occured while trying to get value from GenericParameter.");
			return null;
		}
	}
}
