package com.trilogy.app.crm.extension.service;

import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.extension.ExtendedAssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xenum.AbstractEnum;

public class BlackWhiteTemplateServiceExtension extends
		AbstractBlackWhiteTemplateServiceExtension {

	public BlackWhiteTemplateServiceExtension() {
	}

	@Override
	public void validateDependency(Context ctx) throws IllegalStateException {
		
	}

	@Override
	public boolean isValidForType(AbstractEnum enumType) {
		return true;
	}
}
