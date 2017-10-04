package com.trilogy.app.crm.xdb;

import com.trilogy.app.crm.ContextHelper;
import com.trilogy.app.crm.bean.HlrCommandTemplate;
import com.trilogy.app.crm.bean.HlrCommandTemplateHome;
import com.trilogy.app.crm.bean.HlrProfile;
import com.trilogy.app.crm.bean.HlrProfileHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public abstract class HlrCommandXDBSupport 
{

	public static HlrCommandTemplate getTemplateByID(Long id)
	throws HomeException 
	{
		Home home = (Home)ContextHelper.getContext().get(HlrCommandTemplateHome.class); 
		
		return (HlrCommandTemplate) home.find(id); 
	}
	
	public static HlrProfile getHlrProfile(Long id)
	throws HomeException 
	{
		Home home = (Home)ContextHelper.getContext().get(HlrProfileHome.class); 
		
		return (HlrProfile) home.find(id); 
	}

	
}
