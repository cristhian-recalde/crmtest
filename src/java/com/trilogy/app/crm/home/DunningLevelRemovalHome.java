/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.DunningLevelXInfo;
import com.trilogy.app.crm.dunning.DunningPolicy;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * @since 10.2
 * @author shyamrag.charuvil@redknee.com
 */
public class DunningLevelRemovalHome extends HomeProxy{

    public DunningLevelRemovalHome(Context ctx, Home home)
    {
        super(home);
        setContext(ctx);
    }
    
    @Override
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	DunningPolicy dunningPolicy = (DunningPolicy)obj;
    	Home recordHome = HomeSupportHelper.get(ctx).getHome(ctx, DunningLevel.class);
    	if(recordHome!=null)
    	{
    		recordHome.removeAll(ctx, new EQ(DunningLevelXInfo.DUNNING_POLICY_ID, dunningPolicy.getDunningPolicyId()));
    	}
        super.remove(ctx, obj);
    }
}
