package com.trilogy.app.crm.home.grr;

import java.util.Set;

import com.trilogy.app.crm.grr.ClientToXMLTemplateConfig;
import com.trilogy.app.crm.grr.ClientToXMLTemplateConfigXInfo;
import com.trilogy.app.crm.grr.XMLTemplateConfigXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class ClientToXMLTemplateConfigHomeProxy extends AbstractVendorAwareHome
{
	public ClientToXMLTemplateConfigHomeProxy(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
	
	@Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
		ClientToXMLTemplateConfig config = (ClientToXMLTemplateConfig)obj;
		String vendor = extractVendorFromTemplateID(config);
		config.setVendor(vendor);
		return super.create(ctx, config);
    }
	@Override
	public Object getActiveVendorClause(Context ctx) {
		try{
			Set<String> activeVendors = getActiveVendors(ctx);
			return new In(ClientToXMLTemplateConfigXInfo.VENDOR,activeVendors);
		}catch (AgentException e)
        {
            return False.instance();
        }
        catch (HomeException e)
        {
            return False.instance();
        }
	}
	
	private String extractVendorFromTemplateID(ClientToXMLTemplateConfig config)
	{
		String templateId = config.getTemplateID();
		return templateId.substring(0, templateId.indexOf("_"));
	}

}
