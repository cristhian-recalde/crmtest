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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.DunningLevelHome;
import com.trilogy.app.crm.dunning.DunningLevelXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @since 10.2
 * @author sapan.modi@redknee.com
 */
public class DunningLevelIdGenerator extends HomeProxy {

	private static final long serialVersionUID = 1L;
	private static final Map<Long,Integer> dunningLevelIdMap = new HashMap<Long,Integer>();
	
	public DunningLevelIdGenerator(final Context ctx, final Home bindHome)
	{
		super(ctx,bindHome);
	}

	@Override
	public Object create(final Context ctx, final Object obj) throws HomeException
	{
		DunningLevel dunningLevel = (DunningLevel) obj;
		
		dunningLevel.setId(getDunningLevelId(ctx, dunningLevel.getDunningPolicyId()));
		
		return super.create(ctx, dunningLevel);
	}

	@Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
		DunningLevel dunningLevel = (DunningLevel) obj;

		return super.store(ctx, dunningLevel);
    }
	
	private int getDunningLevelId(final Context ctx,final long dunningPolicyId)
	{
		int maxLevel=0;
		
		if(dunningLevelIdMap.containsKey(dunningPolicyId))
		{
			maxLevel=dunningLevelIdMap.get(dunningPolicyId);
			dunningLevelIdMap.put(dunningPolicyId, maxLevel+1);
		}
		else
		{
			try 
			{
				final Home home = (Home) ctx.get(DunningLevelHome.class);
				final Home filteredHome = home.where(ctx, new EQ(DunningLevelXInfo.DUNNING_POLICY_ID,dunningPolicyId));
		        
		        Collection<DunningLevel> levels = filteredHome.selectAll(ctx);
		        if(levels !=null && levels.size()>0)
		        {
			        for(DunningLevel dunningLevel:levels)
			        {
			        	if(maxLevel<dunningLevel.getId())
			        	{
			        		maxLevel=dunningLevel.getId();
			        		dunningLevelIdMap.put(dunningPolicyId,maxLevel+1);
			        	}
			        }
		        }
		        else
		        {
		        	dunningLevelIdMap.put(dunningPolicyId,1);
		        }
				
			} catch (HomeException e) {
				
				String cause = "Unable to retrieve Levels";
 	            StringBuilder sb = new StringBuilder();
 	            sb.append(cause);
 	            sb.append(": ");
 	            sb.append(e.getMessage());
 	            LogSupport.major(ctx, this, sb.toString(), e);
			}
			
		}
		return maxLevel+1;
	}

}
