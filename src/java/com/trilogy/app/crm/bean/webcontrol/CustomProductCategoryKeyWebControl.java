package com.trilogy.app.crm.bean.webcontrol;


import com.trilogy.app.crm.bean.ui.Product;
import com.trilogy.app.crm.bean.ui.ServiceProduct;
import com.trilogy.app.crm.bean.ServiceCategoryKeyWebControl;
import com.trilogy.app.crm.bean.ServiceCategoryXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


public class CustomProductCategoryKeyWebControl extends ServiceCategoryKeyWebControl{
	
	public CustomProductCategoryKeyWebControl()
    {
        super();
    }


    public CustomProductCategoryKeyWebControl(boolean autoPreview)
    {
        super(autoPreview);
    }
    public CustomProductCategoryKeyWebControl(int listSize,boolean autoPreview,Object optionalValue)
    {
    	super(listSize, autoPreview, optionalValue);
    }

    @Override
    public Home getHome(Context ctx)
    {
    	Object bean=ctx.get(AbstractWebControl.BEAN);
    	int spid=0;
    	if(bean instanceof ServiceProduct){
    		spid = ((ServiceProduct)bean).getSpid();
    		
    		if(spid > 0){
    	        return super.getHome(ctx).where(ctx,
    	                new And().add(new EQ(ServiceCategoryXInfo.SPID, spid)));
    	    	}
    	}
    	return super.getHome(ctx);
    }
}
