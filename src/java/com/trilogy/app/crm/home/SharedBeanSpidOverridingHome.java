package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xhome.msp.SpidAwareXInfo;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Overrides SPID for spid-aware beans, on creation from GUI. 
 * 
 * This home proxy should ideally be used in GUI pipelines only.
 * 
 * @author sbanerjee
 *
 */
public class SharedBeanSpidOverridingHome<BEAN extends SpidAware>
    extends HomeProxy
    implements Home
{

    public SharedBeanSpidOverridingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        
        BEAN bean = (BEAN)obj;
        if(GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            ctx = ctx.createSubContext(Thread.currentThread().getName()+ " : SharedBean_Child_Ctx" );
            final int defaultSharedSpid = GeneralConfigSupport.getDefaultSharedSpid(ctx);
            ctx.put(Spid.class, null);
            ctx.put(MSP.BEAN_SPID_CPROPERTY, null);
            SpidAwareXInfo.SPID.set(bean, 
                   Integer.valueOf( defaultSharedSpid));
        }
        
        return super.create(ctx, bean);
    }
}
