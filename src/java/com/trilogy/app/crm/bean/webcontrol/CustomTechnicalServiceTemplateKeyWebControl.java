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
import com.trilogy.app.crm.bean.ServiceTypeEnum;

public class CustomTechnicalServiceTemplateKeyWebControl extends TechnicalServiceTemplateKeyWebControl{

	public CustomTechnicalServiceTemplateKeyWebControl()
	{
		super();
	}


	public CustomTechnicalServiceTemplateKeyWebControl(boolean autoPreview)
	{
		super(autoPreview);
	}

	public CustomTechnicalServiceTemplateKeyWebControl(int listSize,boolean autoPreview,Object optionalValue)
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
				filter.add(new EQ(TechnicalServiceTemplateXInfo.TYPE, ServiceTypeEnum.PACKAGE));
				filter.add(new EQ(TechnicalServiceTemplateXInfo.TEMPLATE_STATE, TemplateStateEnum.RELEASED));
				/*try{

					Collection<TechnicalServiceTemplate> technicalServiceTemplates = technicalServiceTemplateHome.select(ctx, filter);
					if(technicalServiceTemplates != null && !technicalServiceTemplates.isEmpty()){
						Iterator<TechnicalServiceTemplate> iterator = technicalServiceTemplates.iterator();
						while(iterator.hasNext()){
							TechnicalServiceTemplate technicalServiceTemplate = (TechnicalServiceTemplate) iterator.next();
							technicalServiceTemplateIdSet.add(technicalServiceTemplate.getID());
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}*/
				//return super.getHome(ctx).where(ctx, new In(TechnicalServiceTemplateXInfo.ID, technicalServiceTemplateIdSet));

				Home technicalServiceTransientHome = new TechnicalServiceTemplateTransientHome(ctx);
				try{
					Homes.copy(ctx, new WhereHome(ctx, technicalServiceTemplateHome, filter), technicalServiceTransientHome);
				}catch (Exception e) {
					e.printStackTrace();
				}
				return technicalServiceTransientHome;


				/*Home home = super.getHome(ctx);
				return home.where(ctx, new In(TechnicalServiceTemplateXInfo.ID, technicalServiceTemplateIdSet));*/

				/*And and = new And();
				and.add(new In(TechnicalServiceTemplateXInfo.ID, technicalServiceTemplateIdSet));
				return super.getHome(ctx).where(ctx, and);*/
			}
		}
		return super.getHome(ctx);
	}
}
