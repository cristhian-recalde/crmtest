package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.ServiceCategory;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateHome;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.app.crm.integration.pc.PriceTemplateSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author AChatterjee
 * 
 * This home validates whether the particular service category is used while 
 * creating any technical service template, and doesn't allow deletion if used.
 *
 */
public class ServiceCategoryDeleteValidationHome extends HomeProxy  {

	private static final long serialVersionUID = 1L;

	public ServiceCategoryDeleteValidationHome(Context ctx, Home home) {

		super(ctx, home);
	}

	@Override
	public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException {

		if (isUsedInAnyTechnicalService(ctx,((ServiceCategory)obj)))
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, ServiceCategoryDeleteValidationHome.class, ("Cannot delete, this Service Category is being used by Technical Service."));
			}
			LogSupport.info(ctx, this, "Cannot delete, this Service Category is being used by Technical Service.");
			throw new HomeException("Cannot delete, this Service Category is being used by Technical Service.");
		}
		super.remove(ctx, obj);
	}

	private boolean isUsedInAnyTechnicalService(Context ctx, ServiceCategory serviceCategory) throws HomeInternalException, HomeException {
		Home home = (Home)ctx.get(TechnicalServiceTemplateHome.class);
		Object technicalService = null;

		technicalService = home.find(ctx, new EQ(TechnicalServiceTemplateXInfo.SERVICE_CATEGORY, serviceCategory.getCategoryId()));
		return (technicalService != null);
	}

}
