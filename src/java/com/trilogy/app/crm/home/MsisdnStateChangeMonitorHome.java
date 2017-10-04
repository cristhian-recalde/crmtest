package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Only handles case when State changes to Available. For other state changes, the caller needs to take
 * care of both Spid and SpidGroup setting, as the caller would be knowing the actual/real subscription
 * SPID; and thus the caller himself should set the SPID-Group as well.
 * 
 * @author sbanerjee
 *
 */
public class MsisdnStateChangeMonitorHome extends HomeProxy implements Home
{

    public MsisdnStateChangeMonitorHome(Home msisdnHome)
    {
        super(msisdnHome);
    }
    
    @Override
    public Object store(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        if(!GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
            super.store(ctx, obj);
        
        final Msisdn newMsisdn = (Msisdn)obj;
        final String mobileNumber = newMsisdn.getMsisdn();
        final Msisdn oldMsisdn = (Msisdn)find(ctx, mobileNumber);
        
        if(oldMsisdn==null)
            throw new HomeException("Nothing to update, Msisdn doesn't exist with");
        
        if(stateChange(ctx, newMsisdn, oldMsisdn))
        {
            if(newMsisdn.getState() == MsisdnStateEnum.AVAILABLE)
            {
                newMsisdn.setSpid(oldMsisdn.getSpidGroup());
                newMsisdn.setSubscriberType(SubscriberTypeEnum.HYBRID);
            }
            
            // We can't handle the case: (oldMsisdn.getState() == MsisdnStateEnum.AVAILABLE)
        }
        
        return super.store(ctx, newMsisdn);
    }

    private boolean stateChange(Context ctx, Msisdn newMsisdn, Msisdn oldMsisdn)
    {
        return !SafetyUtil.safeEquals(newMsisdn.getState(), oldMsisdn.getState());
    }

}
