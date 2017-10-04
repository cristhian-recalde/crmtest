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

import com.trilogy.app.crm.bean.PPVModificationRequest;
import com.trilogy.app.crm.bundle.rateplan.ServiceRatePlanValidator;
import com.trilogy.app.crm.home.PricePlanVersionBundlesValidator;
import com.trilogy.app.crm.home.PricePlanVersionModificationValidator;
import com.trilogy.app.crm.home.core.CorePricePlanHomePipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;


/**
 * Home pipeline factory for PPVModificationRequest
 *
 * @author Marcio Marques
 * @since 9.2
 */
public class PricePlanVersionModificationRequestHomePipelineFactory extends CorePricePlanHomePipelineFactory
{
    /**
     * @{inheritDoc}
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
    AgentException
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, PPVModificationRequest.class, "PPVMODIFICATIONREQUEST");
        
        home = new AuditJournalHome(ctx, home);

        CompoundValidator validators = new CompoundValidator();
        validators.add(new PricePlanVersionModificationValidator());
        validators.add(new PricePlanVersionBundlesValidator());
        validators.add(new ServiceRatePlanValidator());

        home = new ValidatingHome(validators, home);

		return home;
    }

}
