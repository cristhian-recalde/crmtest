package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
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
 */
public class TechnicalServiceNameCheckingHome extends HomeProxy {
	
	private static final long serialVersionUID = 1L;

	public TechnicalServiceNameCheckingHome(Context ctx, Home home)
	{
		super(ctx, home);
	}
	
	@Override
	public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException {
		
		TechnicalServiceTemplate currentService = (TechnicalServiceTemplate) obj;
		Long currentId = currentService.getID();
		String currentName = currentService.getName();
		
		LogSupport.debug(ctx, this, "[TechnicalServiceNameCheckingHome.store] Checking the Technical Service Name while updating the Technical Service Template - currentId:" + currentId + " & serviceName:" + currentName);
		
		Boolean updateStatus = false;
		EQ filter = new EQ(TechnicalServiceTemplateXInfo.ID, currentId);
		TechnicalServiceTemplate actualService = (TechnicalServiceTemplate) getDelegate(ctx).find(filter);
		String actualName = actualService.getName();
		
		//The name is not updated
		if(currentName.equals(actualName)){
			updateStatus = true;
		}
		else{
			//Name is updated
			EQ eqFilter = new EQ(TechnicalServiceTemplateXInfo.NAME, currentName);
			TechnicalServiceTemplate service = (TechnicalServiceTemplate) getDelegate(ctx).find(eqFilter);
			//Name is present in DB. May be assigned to another technical service or to the same technical service.
			if(service != null){
				//The name belongs to the same service, Hence can be updated
				if(currentId == service.getID()){
					updateStatus = true;
				}
			}else{
				//Name is  not present in DB. Can be updated
				updateStatus = true;
			}
		}
		
		if(updateStatus){
			return super.store(ctx, obj);
		}else{
			throw new HomeException("Technical Service Template name already present. Must be Unique!");
		}
		
		
	}

}
