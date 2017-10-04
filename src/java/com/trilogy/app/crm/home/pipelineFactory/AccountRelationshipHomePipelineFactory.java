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
import com.trilogy.app.crm.bean.account.AccountRelationship;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author vikash.kumar@redknee.com
 * @since 2016-10-10
 * @purpose Constructs the AccountRelationship home pipeline.
 */
public class AccountRelationshipHomePipelineFactory implements PipelineFactory
{
	public AccountRelationshipHomePipelineFactory()
	{
		super();
	}
	
    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) 
    		throws RemoteException, HomeException, IOException, AgentException
    {
    	Home home = StorageSupportHelper.get(ctx).createHome(ctx, AccountRelationship.class, "ACCOUNTRELATIONSHIP");
        
        return home;
    }
}
