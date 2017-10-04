/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequest;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestXInfo;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Predicate to determine whether a price plan version can be cancelled for activation. A
 * price plan version is cancel if there it is currentversion and it has requests.
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 * @since 8.5
 */
public class PricePlanVersionIsCancelActivationPredicate implements Predicate
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * 
     */
    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if (!(obj instanceof PricePlanVersion))
        {
            LogSupport.debug(ctx, this, "No price plan version supplied; proceeding...");
            return false;
        }
        boolean result = false;
        PricePlanVersion version = (PricePlanVersion) obj;
        try
        {
            if (version.isActivated())
            {
                PricePlan pp = version.getPricePlan(ctx);
                if (pp != null && (pp.getCurrentVersion() == version.getVersion()))
                {
                    if (!ctx.getBoolean(com.redknee.app.crm.priceplan.PricePlanVersionUpdateAgent.PRICE_PLAN_VERSION_UPDATE_IN_PROGRESS, false))
                    {
                        long count = HomeSupportHelper.get(ctx).getBeanCount(ctx, PricePlanVersionUpdateRequest.class,
                                new EQ(PricePlanVersionUpdateRequestXInfo.PRICE_PLAN_IDENTIFIER, version.getId()));
                        if (count > 0)
                        {
                            result = true;
                        }
                    }
                }
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Price Plan ");
                sb.append(version.getId());
                sb.append(" version ");
                sb.append(version.getVersion());
                if (result)
                {
                    sb.append(" can cancal version activation");
                }
                else
                {
                    sb.append(" is can not  cancel activation");
                }
                LogSupport.debug(ctx, this, sb.toString());
            }
        }
        catch (Exception ex)
        {
            LogSupport.minor(ctx, this, "Unable to get data from cancel activation ");
        }
        return result;
    }
}
