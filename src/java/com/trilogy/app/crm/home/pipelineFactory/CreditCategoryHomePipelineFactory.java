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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.billing.message.BillingMessageAwareHomeDecorator;
import com.trilogy.app.crm.home.CreditCategoryAcctRelationCheckHome;
import com.trilogy.app.crm.home.CreditCategoryCodeSettingHome;
import com.trilogy.app.crm.home.CreditCategoryDunningSettingsChangeHome;
import com.trilogy.app.crm.home.CreditCategoryERLogHome;
//import com.trilogy.app.crm.home.SyncPTPHome;
import com.trilogy.app.crm.home.core.CoreCreditCategoryHomePipelineFactory;
import com.trilogy.app.crm.home.validator.LateFeeEarlyRewardExtensionValidator;
import com.trilogy.app.crm.home.validator.MaxSubscriptionsAllowedValidator;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CreditCategoryHomePipelineFactory extends CoreCreditCategoryHomePipelineFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        // Core already installed part of this pipeline
        Home home = (Home) ctx.get(CreditCategoryHome.class);
        
        home = new NotifyingHome(home);

        home = new CreditCategoryDunningSettingsChangeHome(ctx, home);
        
        home = new CreditCategoryCodeSettingHome(ctx, home);
        
        home = new RMIClusteredHome(ctx, CreditCategoryHome.class.getName(), home);
        
        //home = new SyncPTPHome(ctx, home);
        
        home = new BillingMessageAwareHomeDecorator().decorateHome(ctx, home);
        
        home = new CreditCategoryAcctRelationCheckHome(ctx, home);
        
        home = new CreditCategoryERLogHome( home);
        
        home = new SpidAwareHome(ctx, home);

		home =
		    new ValidatingHome(LateFeeEarlyRewardExtensionValidator.instance(),
		        home);

		home =  new ValidatingHome(MaxSubscriptionsAllowedValidator.instance(),
		                home);
		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home, CreditCategory.class);

		return home;
    }

}
