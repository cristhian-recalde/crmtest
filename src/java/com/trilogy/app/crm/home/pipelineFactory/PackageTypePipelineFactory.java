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

import com.trilogy.app.crm.bean.PackageType;
import com.trilogy.app.crm.bean.PackageTypeHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.sub.PackageStateTypeCheckHome;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * Creates the home decorators pipeline fo the PackageTypeHome
 *
 * @author sanjay.pagar
 * @since 9.6
 */
public class PackageTypePipelineFactory implements PipelineFactory
{
	 public PackageTypePipelineFactory() 
	    {
	        super();
	    }

	    public Home createPipeline(final Context ctx, final Context serverCtx)
	        throws RemoteException, HomeException, IOException, AgentException
	    {
	        // [CW] direct conn to DB
	        Home home = StorageSupportHelper.get(ctx).createHome(ctx, PackageType.class, "PACKAGETYPE");
	        home = new AuditJournalHome(ctx, home);
	        home = new SortingHome(home);
	        home = new SpidAwareHome(ctx, home);
	        home = new PackageStateTypeCheckHome(ctx, home);
	        ctx.put(PackageTypeHome.class, home);
	        return home;
	    }
}
