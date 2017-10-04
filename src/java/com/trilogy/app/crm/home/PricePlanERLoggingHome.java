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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.log.EventRecord;
import com.trilogy.app.crm.log.PricePlanModificationEventRecord;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * This home proxy logs ERs for Price Plan modification.
 * 
 * ERs logged: 776
 * @author angie.li@redknee.com
 *
 */
public class PricePlanERLoggingHome extends HomeProxy 
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PricePlanERLoggingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * @(inheritDoc)
     */
    @Override
    public Object store(Context ctx, Object obj)
    throws HomeException, HomeInternalException
    {
        Context sCtx = ctx.createSubContext();
        
        /**
         * Log the ERs once the store action has finished.
         */
        PricePlan oldPricePlan = PricePlanSupport.getPlan(sCtx, ((PricePlan) obj).getId());
        PricePlan plan = (PricePlan) super.store(sCtx, obj);
        PricePlanVersion version = PricePlanSupport.getCurrentVersion(sCtx, plan.getId());
        
        if (oldPricePlan==null
        		|| 
        			(oldPricePlan.getNextVersion() == plan.getNextVersion() 
        				&& oldPricePlan.getCurrentVersion() == plan.getCurrentVersion())
        		|| 
        			!oldPricePlan.getState().equals(plan.getState()))
        {
            logER766(sCtx, plan, oldPricePlan, version, version);
        }
        
        return plan;
    }
    
    /**
     * Logs an event record 776
     *
     * @param plan The owning price plan.
     * @param newVersion The new version of the price plan.
     * @param previousVersion The previous version of the price plan.
     */
    private void logER766(
        final Context ctx,
        final PricePlan plan,
        final PricePlan oldPlan,
        final PricePlanVersion newVersion,
        final PricePlanVersion previousVersion) throws HomeException
    {
        final EventRecord record;
        
        if (previousVersion != null)
        {
            record =
                new PricePlanModificationEventRecord(oldPlan, plan, previousVersion, newVersion);
        }
        else
        {
            record = new PricePlanModificationEventRecord(oldPlan, plan, newVersion);
        }

        record.generate(ctx);
    }
}
