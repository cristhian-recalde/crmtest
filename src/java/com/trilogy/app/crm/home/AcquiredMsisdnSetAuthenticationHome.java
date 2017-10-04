package com.trilogy.app.crm.home;

import java.util.Date;

import org.omg.CORBA.BooleanHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagement;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PinManagerSupport;


public class AcquiredMsisdnSetAuthenticationHome extends AdapterHome implements Adapter
{
    public static AcquiredMsisdnSetAuthenticationHome instance()
    {
        return instance_;
    }
    
    public AcquiredMsisdnSetAuthenticationHome(Context ctx, Home home)
    {
        super(ctx, home, AcquiredMsisdnSetAuthenticationHome.instance());
    }
    
    
    public AcquiredMsisdnSetAuthenticationHome()
    {
    }
    
    /**
     * INHERIT
     */
    public Object adapt(Context ctx,Object obj) throws HomeException
    {        
        AcquiredMsisdnPINManagement acquireMsisdn = (AcquiredMsisdnPINManagement) obj;
        Context subCtx = ctx.createSubContext();
        
        Msisdn msisdn = MsisdnSupport.getMsisdn(subCtx, acquireMsisdn.getMsisdn());
        
        if (msisdn != null)
        {
            SubscriptionType wirelineSubscriptionType = SubscriptionType.getSubscriptionType(ctx, SubscriptionTypeEnum.WIRE_LINE);
            if (wirelineSubscriptionType != null)
            {
                String subId = msisdn.getSubscriberID(subCtx, wirelineSubscriptionType.getId(), new Date());
                if (subId != null)
                {
                    acquireMsisdn.setWireLineSubscription(true);
                    try
                    {
                        BooleanHolder status = new BooleanHolder(false);
                        short resultCode = PinManagerSupport.queryAuthenticatedFlag(subCtx, acquireMsisdn.getMsisdn(),
                                Long.valueOf(wirelineSubscriptionType.getId()).intValue(), "", status);
                        if (resultCode != 0)
                        {
                            new MinorLogMsg(this, " Unable to find a subscription for msisdn " + acquireMsisdn.getMsisdn(),
                                    null).log(subCtx);
                        }
                        acquireMsisdn.setAuthenticated(status.value);
                    }
                    catch (HomeException homeEx)
                    {
                        new MinorLogMsg(this, " Unable to get authentication status for msisdn "
                                + acquireMsisdn.getMsisdn(), null).log(subCtx);
                    }
                }
            }
        }
        return obj;
        
    }
    
    /**
     * INHERIT
     */
    public Object unAdapt(Context ctx,Object obj) throws HomeException
    {
        return adapt(ctx,obj);
    }
    
    public final static AcquiredMsisdnSetAuthenticationHome instance_ = new AcquiredMsisdnSetAuthenticationHome();
}
