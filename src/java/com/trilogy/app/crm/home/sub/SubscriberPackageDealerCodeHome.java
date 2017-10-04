/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;

/**
 * 
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 * @since 9.1.2
 */
public class SubscriberPackageDealerCodeHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    SubscriberPackageDealerCodeHome(Home delegate)
    {
        super(delegate);
    }


    /**
     * Sets subscriber dealer code to the value from the package. {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        Object ret = setDealerCode(ctx, obj);
        return super.create(ctx, ret);
    }


    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        Object ret = setDealerCode(ctx, obj);
        return super.store(ctx, ret);
    }


/**
 *  Updates package dealer code, if needed
 * @param ctx
 * @param obj
 * @return
 * @throws HomeException
 */
    private Object setDealerCode(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber sub = (Subscriber) obj;
        final TechnologyEnum technology = sub.getTechnology();
        if (technology == null)
        {
            final String msg = "Missing technology " + sub.getTechnology() + " while adapting Subscriber "
                    + sub.getId();
            Logger.major(ctx, this, msg);
            final HomeException ex = new HomeException(msg);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, ex);
            return sub;
        }
        else if (!technology.isPackageAware())
        {
            // unadapt only for package-aware subscriptions
            return sub;
        }
        else
        {
            final Account account = (Account) ctx.get(Account.class);
            if (account != null && account.isPooled(ctx))
            {
                // ignoring fake subscription that is created for Pooled acounts
                return sub;
            }
        }
        if (TechnologyEnum.GSM == sub.getTechnology())
        {
            final Home packHome = (Home) ctx.get(GSMPackageHome.class);
            final GSMPackage pk = (GSMPackage) packHome.find(ctx, sub.getPackageId());
            if (pk == null)
            {
                throw new HomeException("GSM Package not found");
            }
            if (sub.getDealerCode() != null && !sub.getDealerCode().equals("")
                    && !SafetyUtil.safeEquals(pk.getDealer(), sub.getDealerCode()))
            {
                pk.setDealer(sub.getDealerCode());
                packHome.store(ctx, pk);
            }
        }
        else if (TechnologyEnum.TDMA == sub.getTechnology() || TechnologyEnum.CDMA == sub.getTechnology())
        {
            final Home pkgHome = (Home) ctx.get(TDMAPackageHome.class);
            
            And and = new And();           
            and.add(new EQ(TDMAPackageXInfo.PACK_ID, sub.getPackageId()));
            and.add(new EQ(TDMAPackageXInfo.SPID, sub.getSpid()));                      
            
            final TDMAPackage pk = (TDMAPackage) pkgHome.find(ctx, and);            
            
            //final TDMAPackage pk = (TDMAPackage) pkgHome.find(ctx, sub.getPackageId());
            if (pk == null)
            {
                throw new HomeException(sub.getTechnology() + " Package not found");
            }
            if (sub.getDealerCode() != null && !sub.getDealerCode().equals("")
                    && !SafetyUtil.safeEquals(pk.getDealer(), sub.getDealerCode()))
            {
                pk.setDealer(sub.getDealerCode());
                pkgHome.store(pk);
            }
        }
        return sub;
    }
}
