package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.app.crm.bean.ui.PriceTemplateXInfo;
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
public class PriceTemplateNameCheckingHome extends HomeProxy {

	private static final long serialVersionUID = 1L;
	
	public PriceTemplateNameCheckingHome(Context ctx, Home home)
	{
		super(ctx, home);
	}
	
	@Override
	public Object create(Context ctx, Object obj) throws HomeException,HomeInternalException 
	{
		LogSupport.debug(ctx, this, "[PriceTemplateNameCheckingHome.create] Checking the Price Template Name while creating the Price Template");
		
		PriceTemplate currentTemplate = (PriceTemplate) obj;
		String priceTemplateName = currentTemplate.getName();
		
		EQ filter = new EQ(PriceTemplateXInfo.NAME, priceTemplateName);
		PriceTemplate availableTemplate = (PriceTemplate) getDelegate(ctx).find(filter);
		
		if(availableTemplate==null)
		{
			return super.create(ctx, obj);
		}else{
			LogSupport.info(ctx, this, "Price Template name must be Unique");
			throw new HomeException("Price Template name must be Unique");
		}
	}
	
	/*private  boolean containsAny(String str, char[] searchChars) {
        for (int i = 0; i < str.length(); i++) {
          char ch = str.charAt(i);
          for (int j = 0; j < searchChars.length; j++) {
              if (searchChars[j] == ch) {
                  return true;
              }
          }
      }
      return false;
  }*/
	
	@Override
	public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException {
		
		LogSupport.debug(ctx, this, "[PriceTemplateNameCheckingHome.store] Checking the Price Template Name while updating the Price Template");
		
		Boolean updateStatus = false;
		PriceTemplate currentTemplate = (PriceTemplate) obj;
		Long currentId = currentTemplate.getID();
		String currentName = currentTemplate.getName();
		EQ filter = new EQ(PriceTemplateXInfo.ID, currentId);
		PriceTemplate actualTemplate = (PriceTemplate) getDelegate(ctx).find(filter);
		String actualName = actualTemplate.getName();
		
		/*String searchChars = "%#&<>";
		if (containsAny(currentName,searchChars.toCharArray()) || (currentName.charAt(0) == ' ') || (currentName.charAt(0)== '_'))
		{
			throw new HomeException("Price Template name can't have character: '%' '#' '&' '<' '>' and can't start with '_' or blank space");
		}*/
		
		//The name is not updated
		if(currentName.equals(actualName)){
			updateStatus = true;
		}
		else{
			//Name is updated
			EQ eqFilter = new EQ(PriceTemplateXInfo.NAME, currentName);
			PriceTemplate service = (PriceTemplate) getDelegate(ctx).find(eqFilter);
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
			LogSupport.info(ctx, this, "Price Template name already present. Must be Unique.");
			throw new HomeException("Price Template name already present. Must be Unique.");
		}
		
		
	}

}
