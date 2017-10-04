/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.bean.ServiceCategoryKeyWebControl;
import com.trilogy.app.crm.bean.ServiceCategoryXInfo;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


public class CustomServiceCategoryKeyWebControl extends ServiceCategoryKeyWebControl
{

    public CustomServiceCategoryKeyWebControl()
    {
        super();
    }


    public CustomServiceCategoryKeyWebControl(boolean autoPreview)
    {
        super(autoPreview);
    }
    public CustomServiceCategoryKeyWebControl(int listSize,boolean autoPreview,Object optionalValue)
    {
    	super(listSize, autoPreview, optionalValue);
    }

    @Override
    public Home getHome(Context ctx)
    {
    	Object bean=ctx.get(AbstractWebControl.BEAN);
    	int spid=0;
    	if(bean instanceof Service){
    		spid = ((Service)bean).getSpid();
    		
    		if(spid > 0){
    	        return super.getHome(ctx).where(ctx,
    	                new And().add(new EQ(ServiceCategoryXInfo.SPID, spid)));
    	    	}
    	}
    	return super.getHome(ctx);
    }
}
