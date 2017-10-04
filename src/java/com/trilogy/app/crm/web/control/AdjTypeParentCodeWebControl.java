package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.ui.AdjustmentTypeN;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNHome;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNVersion;
import com.trilogy.app.crm.bean.ui.AdjustmentTypeNXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.LogSupport;

public class AdjTypeParentCodeWebControl extends CustomAdjKeyWebControl
{
	@Override
    public Home getHome(Context ctx)
    {
		LogSupport.debug(ctx, this, "[getHome]", new Exception("test")); 
		Object bean=ctx.get(AbstractWebControl.BEAN);
    	Home home = (Home)ctx.get(AdjustmentTypeNHome.class);
		int spid = ((AdjustmentTypeNVersion)bean).getSpid();
		EQ eqSpid = new EQ(AdjustmentTypeNXInfo.SPID, spid);
		EQ eqCategory = new EQ(AdjustmentTypeNXInfo.CATEGORY, true);  
		And and = new And();
		and.add(eqSpid);
		and.add(eqCategory);
		if(spid > 0)
		{
	        return super.getHome(ctx).where(ctx,and);
	    }
    	return super.getHome(ctx);
    }
}
