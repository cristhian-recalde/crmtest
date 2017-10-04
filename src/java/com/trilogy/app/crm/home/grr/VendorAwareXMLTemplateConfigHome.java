package com.trilogy.app.crm.home.grr;

import java.util.Set;

import com.trilogy.app.crm.grr.XMLTemplateConfigXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * @author odeshpande
 *
 */
public class VendorAwareXMLTemplateConfigHome extends AbstractVendorAwareHome
{

	public VendorAwareXMLTemplateConfigHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
	
	@Override
	public Object getActiveVendorClause(Context ctx) {
		
		try{
			Set<String> activeVendors = getActiveVendors(ctx);
			return new In(XMLTemplateConfigXInfo.VENDOR,activeVendors);
		}catch (AgentException e)
        {
            return False.instance();
        }
        catch (HomeException e)
        {
            return False.instance();
        }
	}

}
