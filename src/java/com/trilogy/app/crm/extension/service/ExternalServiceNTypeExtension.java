package com.trilogy.app.crm.extension.service;

import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ui.ProductXInfo;
import com.trilogy.app.crm.extension.DependencyValidatableExtension;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.TypeDependentExtension;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.AbstractEnum;

public class ExternalServiceNTypeExtension extends	AbstractExternalServiceNTypeExtension implements
		DependencyValidatableExtension, TypeDependentExtension {

	private static final long serialVersionUID = -5126602926019013575L;

	/**
* 
*/
	public ExternalServiceNTypeExtension() {
	}

	public void validateDependency(Context ctx) throws IllegalStateException {
		CompoundIllegalStateException cise = new CompoundIllegalStateException();

		// Validate whether or not this extension is allowed to be contained
		// within the parent bean.
		ExtensionAware parentBean = this.getParentBean(ctx);
		if (parentBean instanceof Product) {
			ServiceProduct service = (ServiceProduct) parentBean;
			if (!(ServiceTypeEnum.GENERIC.equals(service.getType()) || ServiceTypeEnum.EXTERNAL_PRICE_PLAN
					.equals(service.getType()))) {
				cise.thrown(new IllegalArgumentException(this.getName(ctx)
						+ " extension only allowed for "
						+ ServiceTypeEnum.GENERIC + " and "
						+ ServiceTypeEnum.EXTERNAL_PRICE_PLAN + " services."));
			}
		}

		cise.throwAll();
	}

	@Override
	public boolean isValidForType(AbstractEnum serviceType) {
		return ServiceTypeEnum.GENERIC.equals(serviceType)
				|| ServiceTypeEnum.EXTERNAL_PRICE_PLAN.equals(serviceType);
	}

	/**
	 * Enhanced to use bean in context if available. {@inheritDoc}
	 */
	@Override
	public Product getService(Context ctx) {
		
		Product service = BeanLoaderSupportHelper.get(ctx).getBean(ctx,Product.class);
		if (service != null	&& (AbstractServiceExtension.DEFAULT_SERVICEID == this
						.getServiceId() || SafetyUtil.safeEquals(
						service.getProductId(), this.getServiceId()))) {
			return service;
		}

		if (AbstractServiceExtension.DEFAULT_SERVICEID == this.getServiceId()) {
			return null;
		}

		try {
			return HomeSupportHelper.get(ctx).findBean(ctx, Product.class,
					new EQ(ProductXInfo.PRODUCT_ID, getServiceId()));
		} catch (HomeException e) {
		}

		if (service != null
				&& SafetyUtil.safeEquals(service.getProductId(), this.getServiceId())) {
			return service;
		}

		return null;
	}
}
