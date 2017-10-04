package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.ui.Msisdn;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * @author sbanerjee
 *
 */
public class SubscriberTypeOverridingHome extends HomeProxy implements Home
{

    /**
     * @param ctx
     * @param delegate
     */
    public SubscriberTypeOverridingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    
    @Override
    public Object create(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        Msisdn msisdn = (Msisdn)obj;
        
        if(GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
            msisdn.setSubscriberType(SubscriberTypeEnum.HYBRID);
        
        return super.create(ctx, msisdn);
    }

}
