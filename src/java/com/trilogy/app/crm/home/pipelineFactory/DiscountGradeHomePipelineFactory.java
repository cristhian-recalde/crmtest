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

import com.trilogy.app.crm.bean.DiscountGrade;
import com.trilogy.app.crm.home.DiscountGradeFieldsValidator;
import com.trilogy.app.crm.home.DiscountGradeSetIdentifierHome;
import com.trilogy.app.crm.home.DiscountGradeUpdateHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author vikash.kumar@redknee.com
 * @since 2016-10-06
 * Purpose: Pipeline factory for the DiscountGrade.
 */
public class DiscountGradeHomePipelineFactory implements PipelineFactory
{
	public DiscountGradeHomePipelineFactory()
	{
		super();
	}
	
    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) 
    		throws RemoteException, HomeException, IOException, AgentException
    {   
        LogSupport.info(ctx, this, "Installing the Discount Grade home ");
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, DiscountGrade.class, "DISCOUNTGRADE");
        home = new DiscountGradeUpdateHome(ctx, home);
        home = new ValidatingHome(new DiscountGradeFieldsValidator(), null, home);
        home = new DiscountGradeSetIdentifierHome(ctx, home);

        return home;
        
    }
}
