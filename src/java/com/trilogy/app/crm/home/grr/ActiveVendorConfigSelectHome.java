package com.trilogy.app.crm.home.grr;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfig;
import com.trilogy.app.crm.grr.VendorConfigXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.WhereHome;

/**
 * 
 * @author odeshpande
 *
 */
public class ActiveVendorConfigSelectHome extends WhereHome
{
	public ActiveVendorConfigSelectHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
	
	public Object getWhere(Context ctx)
    {
		boolean viewInactiveVendors = ((GeneralConfig) ctx.get(GeneralConfig.class)).isViewInactiveVendors();
		if(viewInactiveVendors)
		{
			return True.instance();
		}else
		{
			return new EQ(VendorConfigXInfo.IS_ACTIVE,true);
		}
		
    }
	
}
