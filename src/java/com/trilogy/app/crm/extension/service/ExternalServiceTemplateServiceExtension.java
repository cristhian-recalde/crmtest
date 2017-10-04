package com.trilogy.app.crm.extension.service;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xenum.AbstractEnum;

public class ExternalServiceTemplateServiceExtension extends
		AbstractExternalServiceTemplateServiceExtension {

	public ExternalServiceTemplateServiceExtension() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void validateDependency(Context ctx) throws IllegalStateException {

	}

	@Override
	public boolean isValidForType(AbstractEnum enumType) {
		return true;
	}

}
