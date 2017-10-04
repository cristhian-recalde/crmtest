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

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.relationship.NoRelationshipRemoveHome;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.account.AccountRole;
import com.trilogy.app.crm.bean.account.AccountRoleHome;
import com.trilogy.app.crm.bean.account.AccountRoleXInfo;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;

/**
 * Prevents the AccountRoleHome.remove action if the AccountHome selected for
 * removal is referenced in Group Account entities.
 * 
 * @author angie.li
 *
 */
public class AccountRoleHomePipelineFactory extends HomeProxy 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
     * Singleton instance.
     */
    private static AccountRoleHomePipelineFactory instance_;
    
    /**
     * Create a new instance of <code>AccountRoleHomePipelineFactory</code>.
     */
    protected AccountRoleHomePipelineFactory()
    {
        // empty
    }

    /**
     * Returns an instance of <code>AccountRoleHomePipelineFactory</code>.
     *
     * @return An instance of <code>AccountRoleHomePipelineFactory</code>.
     */
    public static AccountRoleHomePipelineFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new AccountRoleHomePipelineFactory();
        }
        return instance_;
    }

    /**
     * @{inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) 
        throws RemoteException, HomeException, IOException, AgentException
    {
        Home home = CoreSupport.bindHome(ctx, AccountRole.class, true);
        home = new NotifyingHome(home);
        home = new SortingHome(ctx, home);
        home = new RMIClusteredHome(ctx, AccountRoleHome.class.getName(), home);
        home = new NoRelationshipRemoveHome(ctx, AccountRoleXInfo.ID,
                AccountXInfo.ROLE, AccountHome.class,
                "This Account Role is in use.  Cannot delete this Account Role.", home);
		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home, AccountRole.class);

        return home;
    }
}
