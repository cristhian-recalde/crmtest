package com.trilogy.app.crm.bean.webcontrol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServiceCategoryXInfo;
import com.trilogy.app.crm.bean.UnifiedCatalogSpidConfig;
import com.trilogy.app.crm.bean.ui.PricingVersion;
import com.trilogy.app.crm.bean.ui.PricingVersionHome;
import com.trilogy.app.crm.bean.ui.PricingVersionXInfo;
import com.trilogy.app.crm.bean.ui.ServicePricingXInfo;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateHome;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateKeyWebControl;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateTransientHome;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateWebControl;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xhome.home.WhereHome;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.app.crm.bean.TemplateStateEnum;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;

/** 
* @author dinesh.valsatwar@redknee.com
* @since 11.0.3
*/
public class CustomDeviceProductTemplateKeyWebControl extends TechnicalServiceTemplateKeyWebControl{

	public CustomDeviceProductTemplateKeyWebControl()
	{
		super();
	}


	public CustomDeviceProductTemplateKeyWebControl(boolean autoPreview)
	{
		super(autoPreview);
	}

	public CustomDeviceProductTemplateKeyWebControl(int listSize,boolean autoPreview,Object optionalValue)
	{
		super(listSize, autoPreview, optionalValue);
	}

	@Override
	public Home getHome(Context ctx)
	{
		Object bean=ctx.get(AbstractWebControl.BEAN);
		int spid=0;
		if(bean instanceof UnifiedCatalogSpidConfig){
			spid = ((UnifiedCatalogSpidConfig)bean).getSpid();
			Home technicalServiceTemplateHome = (Home) ctx.get(TechnicalServiceTemplateHome.class);
			if(spid > 0){
				HashSet<Long> technicalServiceTemplateIdSet = new HashSet<Long>();

				And filter = new And();
				filter.add(new EQ(TechnicalServiceTemplateXInfo.SPID, spid));
				filter.add(new EQ(TechnicalServiceTemplateXInfo.SERVICE_SUB_TYPE, ServiceSubTypeEnum.RESOURCE ));
				filter.add(new EQ(TechnicalServiceTemplateXInfo.TEMPLATE_STATE, TemplateStateEnum.RELEASED));				
								Home technicalServiceTransientHome = new TechnicalServiceTemplateTransientHome(ctx);
				try{
					Homes.copy(ctx, new WhereHome(ctx, technicalServiceTemplateHome, filter), technicalServiceTransientHome);
				}catch (Exception e) {
					e.printStackTrace();
				}
				return technicalServiceTransientHome;
			}
		}
		return super.getHome(ctx);
	}
}
