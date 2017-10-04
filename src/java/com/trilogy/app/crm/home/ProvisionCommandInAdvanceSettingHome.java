package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


public class ProvisionCommandInAdvanceSettingHome extends HomeProxy
{
    public ProvisionCommandInAdvanceSettingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
    
    @Override
    public Object create(final Context ctx, final Object obj)
        throws HomeException
    {
        ProvisionCommand command = (ProvisionCommand) obj;
        if (command.getName().equals(HLRConstants.PRV_CMD_TYPE_INACTIVE)
        		|| command.getName().equals(HLRConstants.PRV_CMD_TYPE_BULK_SERVICE_UPDATE))
        {
            command.setInAdvanceCmd(false);
        }
        return super.create(ctx, obj);
    }
    
}
