package com.trilogy.app.crm.integration.pc;


import java.util.Collection;
//import java.util.HashSet;
import java.util.Iterator;
//import java.util.List;
//import java.util.Set;

import com.trilogy.app.crm.bean.IdentifierEnum;
//import com.trilogy.app.crm.bean.Service;
//import com.trilogy.app.crm.bean.ui.CompatibilitySpecs;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Sets ID and identifier for this service using an
 * IdentifierSequence. ID is used in SQLServer.
 * 
 * IdentifierSettingHome is for IdentifierAware interface and
 * does not set ID. Hence IdentifierSettingHome is not sufficient.
 * 
 * @author ameya.bhurke@redknee.com
 *
 */
public class TechnicalServiceTemplateSettingHome extends HomeProxy 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//private static final long startValue = 1;
	
	private static final Long TECHNICAL_SERVICE_TEMPLATE_ID_START_NUMBER = 200000000L;
	
	private static final Long TECHNICAL_SERVICE_TEMPLATE_ID_END_NUMBER = 299999999L;
	
	public TechnicalServiceTemplateSettingHome(Context ctx, Home delegate)
	{
		super(ctx, delegate);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		TechnicalServiceTemplate service = (TechnicalServiceTemplate)obj;
		String serviceName = service.getName();
		
		LogSupport.debug(ctx, this, "[TechnicalServiceTemplateSettingHome.create] Creating the Technical Service template serviceId:" + service.getID() + " & serviceName:" + serviceName);
	    
		if (this.delegate_ != null) {
			Collection<TechnicalServiceTemplate> techServices = this.delegate_.selectAll(ctx);
			Iterator itr = techServices.iterator();
			String currentServ = "";
			
			while(itr.hasNext()) {
				TechnicalServiceTemplate techServ = (TechnicalServiceTemplate) itr.next();
				currentServ = techServ.getName();
				
				if (serviceName.equals(currentServ)) {
					LogSupport.info(ctx, this, "[TechnicalServiceTemplateSettingHome.create] Technical Service name must be Unique");
					throw new HomeException("Technical Service name must be Unique");
				}
			}
		}
		
		String searchChars = "%#&<>";
		
		if (containsAny(serviceName,searchChars.toCharArray()) || (serviceName.charAt(0) == ' ') || (serviceName.charAt(0)== '_')) {
			throw new HomeException("Technical Service name can't have character: '%' '#' '&' '<' '>' and can't start with '_' or blank space");
		}
		
		long serviceID = getNextIdentifier(ctx);
		if(service.getID() == 0) {
			service.setID(serviceID);
			service.setIdentifier(serviceID);
		}
		
		if (LogSupport.isDebugEnabled(ctx)) { 
			LogSupport.debug(ctx, this, "[TechnicalServiceTemplateSettingHome.create] Technical Service Template ID set to: " + service.getID());
		}
		
		return super.create(ctx, obj);
	}

	private long getNextIdentifier(Context ctx) throws HomeException {
		IdentifierSequenceSupportHelper
			.get((Context)ctx)
			.ensureSequenceExists(ctx, IdentifierEnum.TECHNICALSERVICETEMPLATE_ID, TECHNICAL_SERVICE_TEMPLATE_ID_START_NUMBER, TECHNICAL_SERVICE_TEMPLATE_ID_END_NUMBER);
		
		return IdentifierSequenceSupportHelper
				.get((Context)ctx)
				.getNextIdentifier(ctx, IdentifierEnum.TECHNICALSERVICETEMPLATE_ID, null);
	}
	 
	private  boolean containsAny(String str, char[] searchChars) {
		
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			
			for (int j = 0; j < searchChars.length; j++) {
				if (searchChars[j] == ch) {
					return true;
				}
			}
		}
	
		return false;
	}
 
	@Override
	public Object store(Context ctx, Object obj) throws HomeException {
		
		//TODO implementation of uniqueness of name
		TechnicalServiceTemplate currentTemplate = (TechnicalServiceTemplate)obj;
		Long currentId = currentTemplate.getID();
		String currentName = currentTemplate.getName();
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, this, "[TechnicalServiceTemplateSettingHome.store] Checking the Price Template Name while Updating the Technical Service template currentId:" + currentId + " & currentName:" + currentName);
		}
	    
		Boolean updateStatus = false;
		EQ filter = new EQ(TechnicalServiceTemplateXInfo.ID, currentId);
		TechnicalServiceTemplate actualTemplate = (TechnicalServiceTemplate) getDelegate(ctx).find(filter);
			
		String actualName = actualTemplate.getName();
		String searchChars = "%#&<>";
			
		if (containsAny(currentName,searchChars.toCharArray()) || (currentName.charAt(0) == ' ') || (currentName.charAt(0)== '_')){
			throw new HomeException("Technical Service name can't have character: '%' '#' '&' '<' '>' and can't start with '_' or blank space");
		}
		
		//The name is not updated
		if(currentName.equals(actualName)) {
			updateStatus = true;
		}
		else{
			//Name is updated
			EQ eqFilter = new EQ(TechnicalServiceTemplateXInfo.NAME, currentName);
			TechnicalServiceTemplate technicalService = (TechnicalServiceTemplate) getDelegate(ctx).find(eqFilter);
		
			//Name is present in DB. May be assigned to another technical service or to the same technical service.
			if(technicalService != null) {
				//The name belongs to the same service, Hence can be updated
				if(currentId == technicalService.getID()){
					updateStatus = true;
				}
			} else {
				//Name is  not present in DB. Can be updated
				updateStatus = true;
			}
		}
			
		if(updateStatus) {
			return super.store(ctx, obj);
		} else {
			LogSupport.info(ctx, this, "[TechnicalServiceTemplateSettingHome.store] Price Template name already present. Must be Unique.");
			throw new HomeException("Price Template name already present. Must be Unique.");
		}
	}

}
