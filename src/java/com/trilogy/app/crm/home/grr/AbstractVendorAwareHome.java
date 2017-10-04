package com.trilogy.app.crm.home.grr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfig;
import com.trilogy.app.crm.grr.VendorConfig;
import com.trilogy.app.crm.grr.VendorConfigHome;
import com.trilogy.app.crm.grr.VendorConfigXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.WhereHome;
import com.trilogy.framework.xhome.visitor.MapVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;

public abstract class AbstractVendorAwareHome extends WhereHome
{
	public AbstractVendorAwareHome(Context ctx, Home delegate)
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
			return getActiveVendorClause(ctx);
		}
    }
	
	public Set<String> getActiveVendors(Context ctx) throws HomeException,AgentException
	{
		Collection<VendorConfig> activeVendorConfigs = ((Home)ctx.get(VendorConfigHome.class)).selectAll(ctx);
		Set<String> activeVendors = new HashSet<String>((Collection) Visitors.forEach(ctx, activeVendorConfigs, new MapVisitor(VendorConfigXInfo.VENDOR_NAME)));
		return activeVendors;
	}
	
	public abstract Object getActiveVendorClause(Context ctx);
}
