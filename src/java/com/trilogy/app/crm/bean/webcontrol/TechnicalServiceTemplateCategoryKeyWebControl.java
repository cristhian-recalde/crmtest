package com.trilogy.app.crm.bean.webcontrol;
import com.trilogy.app.crm.bean.ServiceCategoryKeyWebControl;
import com.trilogy.app.crm.bean.ServiceCategoryXInfo;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class TechnicalServiceTemplateCategoryKeyWebControl extends ServiceCategoryKeyWebControl
{

    public TechnicalServiceTemplateCategoryKeyWebControl()
    {
        super();
    }


    public TechnicalServiceTemplateCategoryKeyWebControl(boolean autoPreview)
    {
        super(autoPreview);
    }
    public TechnicalServiceTemplateCategoryKeyWebControl(int listSize,boolean autoPreview,Object optionalValue)
    {
    	super(listSize, autoPreview, optionalValue);
    }

    @Override
    public Home getHome(Context ctx)
    {
    	Object bean=ctx.get(AbstractWebControl.BEAN);
    	int spid=0;
    	if(bean instanceof TechnicalServiceTemplate){
    		spid = ((TechnicalServiceTemplate)bean).getSpid();
    		
    		if(spid > 0){
    	        return super.getHome(ctx).where(ctx,
    	                new And().add(new EQ(ServiceCategoryXInfo.SPID, spid)));
    	    	}
    	}
    	return super.getHome(ctx);
    }
}
