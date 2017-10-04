package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * @author sbanerjee
 *
 */
public class MsisdnMultiSpidSharingHome extends HomeProxy implements Home
{

    public MsisdnMultiSpidSharingHome(Home msisdnHome)
    {
        super(msisdnHome);
    }
    
    @Override
    public Object create(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        Msisdn msisdn = (Msisdn)obj;
        
        if(GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            msisdn.setSpidGroup(GeneralConfigSupport.getDefaultSharedSpid(ctx));
            msisdn.setSyncAll(true);
        }
        
        return super.create(ctx, msisdn);
    }

}
