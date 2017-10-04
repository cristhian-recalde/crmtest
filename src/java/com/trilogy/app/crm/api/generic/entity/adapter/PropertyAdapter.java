package com.trilogy.app.crm.api.generic.entity.adapter;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;

import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;

public interface PropertyAdapter {
	
    	public void adaptProperty(Context ctx, Object bean, GenericParameter[] genericParameters, PropertyInfo  property);
    	public void adaptQuery(And and, PropertyInfo info, Object value);

}
