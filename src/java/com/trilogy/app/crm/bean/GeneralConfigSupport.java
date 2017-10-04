package com.trilogy.app.crm.bean;

import com.trilogy.framework.xhome.context.Context;

/**
 * @author sbanerjee
 *
 */
public class GeneralConfigSupport
{
    public static GeneralConfig getGeneralConfig(Context ctx)
    {
        return (GeneralConfig)ctx.get(GeneralConfig.class);
    }
    
    public static boolean isAllowedSharedMsisdnAcrossSpids(Context ctx)
    {
        if(getGeneralConfig(ctx)!=null)
            return getGeneralConfig(ctx).isAllowSharedMsisdnAcrossSpids();
        
        return false;
    }
    
    
    public static int getDefaultSharedSpid(Context ctx)
    {
        return getGeneralConfig(ctx).getDefaultSharedSpid();
    }
}
