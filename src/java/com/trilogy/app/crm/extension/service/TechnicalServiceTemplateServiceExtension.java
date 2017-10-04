package com.trilogy.app.crm.extension.service;

import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class TechnicalServiceTemplateServiceExtension extends AbstractTechnicalServiceTemplateServiceExtension {

	public TechnicalServiceTemplateServiceExtension() {
	}

	@Override
	public String getSummary(Context arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	 public TechnicalServiceTemplate getService(Context ctx)
	    {
		 TechnicalServiceTemplate serviceTemplate = null;
	        try
	        {
	            Home home = (Home) ctx.get(TechnicalServiceTemplate.class);
	            serviceTemplate = (TechnicalServiceTemplate) home.find(ctx, this.getId());
	        }
	        catch (HomeException e)
	        {
	        }

	        if (serviceTemplate != null
	                && serviceTemplate.getID() == this.getId())
	        {
	            return serviceTemplate;
	        }

	        return null;
	    }
	
}
