/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Updates the associated subscriber's IMSI on change
 * 
 * @author asim.mahmood@redknee.com
 * @since 8.5
 *
 */
public class IMSIChangeHome extends HomeProxy
{

 
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public IMSIChangeHome(Context ctx, Home home)
    {
        super(ctx, home);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        final GenericPackage newPackage = (GenericPackage) obj;
        
        obj = super.store(ctx, obj);
                
        updateImsiAsRequired(ctx, newPackage);

        return obj;
    }

    private void updateImsiAsRequired(Context ctx, final GenericPackage newPackage)
            throws HomeInternalException, HomeException
    {
        String newImsi;
        final GenericPackage oldPackage = OldGSMPackageLookupHome.getOldGSMPackage(ctx);
        
        if (oldPackage == null)
        {
            return;
        }
        
        //Only update IMSI while package is in use
        if (PackageStateEnum.IN_USE.equals(newPackage.getState()) &&
                PackageStateEnum.IN_USE.equals(oldPackage.getState()))
        {
            final Subscriber sub;
            {
                final And predicate;
                {
                    predicate = new And().add(new EQ(SubscriberXInfo.PACKAGE_ID, newPackage.getPackId())).add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
                }
                sub = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, predicate );
            }

            newImsi = getIMSI(newPackage);
            
            //Update the subscriber if its IMSI is different 
            if (sub != null && !SafetyUtil.safeEquals(sub.getIMSI(), newImsi))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    final String msg = String.format("Updating subscriber=%s IMSI from=%s to=%s", sub.getId(), sub.getIMSI(), newImsi);
                    new DebugLogMsg(this, msg, null).log(ctx);
                }
                
                sub.setIMSI(newImsi);
                HomeSupportHelper.get(ctx).storeBean(ctx, sub);
            }
        }
    }

    private String getIMSI(final GenericPackage genPackage)
    {
        String imsiOrMin = "";
        TechnologyEnum technology = genPackage.getTechnology();
        
        if ( TechnologyEnum.GSM.equals(technology))
        {
            GSMPackage gsmPackage = (GSMPackage) genPackage;
            imsiOrMin = gsmPackage.getIMSI();
        }
        else if ( TechnologyEnum.TDMA.equals(technology) ||
                TechnologyEnum.CDMA.equals(technology) )
        {
            TDMAPackage tdmaPackage = (TDMAPackage) genPackage;
            imsiOrMin = tdmaPackage.getMin();
        }
        return imsiOrMin;
    }


}
