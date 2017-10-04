/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.billing.message.BillingMessageAwareHomeDecorator;
import com.trilogy.app.crm.home.PricePlanERLoggingHome;
import com.trilogy.app.crm.home.PricePlanFieldSettingHome;
import com.trilogy.app.crm.home.URCSPricePlanMgmtHome;
import com.trilogy.app.crm.home.core.CorePricePlanHomePipelineFactory;
import com.trilogy.app.crm.priceplan.PricePlanValidator;
import com.trilogy.app.crm.secondarybalance.validator.SingleSecondaryBalanceBundleValidator;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.technology.TechnologyAwareHome;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 
 */
public class PricePlanHomePipelineFactory extends CorePricePlanHomePipelineFactory
{
    /**
     * @{inheritDoc}
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
    AgentException
    {
        // Base home should already be installed by AppCrmCore
        Home home = (Home) ctx.get(PricePlanHome.class);

        home = new NotifyingHome(home);

        //Since CRM 8.2, Price Plan/Rate Plan relationship has to be pushed to URCS.
        home = new URCSPricePlanMgmtHome(ctx, home);
        home = new PricePlanERLoggingHome(ctx, home);
        home = new TechnologyAwareHome(ctx, home);
        home = new SpidAwareHome(ctx, home);
        
        home = new PricePlanFieldSettingHome(ctx, home);
        // commenting the LRU cache because it causes problem a PPV is created on ecare
        //home = new LRUCachingHome(StorageSupportHelper.get(ctx).getCacheConfig(ctx, PricePlan.class/*50*/).getSize(), true, home);
        
        CompoundValidator validator = new CompoundValidator();
        validator.add(new PricePlanValidator());
        
        home = new ValidatingHome(validator, home);

        // this has to be near the end because it instruments the pipeline
        home = new BillingMessageAwareHomeDecorator().decorateHome(ctx, home);

		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home, PricePlan.class);

		return home;
    }

}
