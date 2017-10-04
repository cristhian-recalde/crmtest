package com.trilogy.app.crm.dunning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.ProvinceXInfo;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xlog.log.LogSupport;

public class DunningPolicy extends AbstractDunningPolicy
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<DunningLevel> levels_ = null;
	
	
	public DunningPolicy() {}

	public List<DunningLevel> getAllLevels(final Context ctx)
	{
		if(levels_ != null)
			return levels_;
		else
		{
		   try {
			final Home home = (Home) ctx.get(DunningLevelHome.class); 
	        if (home == null)
	        {
	            throw new HomeException("Subscriber home not found in context!");
	        }
	        final Home filteredHome = home.where(ctx, new EQ(DunningLevelXInfo.DUNNING_POLICY_ID,getDunningPolicyId()));

	        
	        Collection<DunningLevel> levels = filteredHome.selectAll(ctx);
	        levels_ = new ArrayList<DunningLevel>(levels);
		    

		    
			} catch (HomeInternalException e) {
				levels_ = Collections.EMPTY_LIST;
				String cause = "Unable to retrieve Levels";
 	            StringBuilder sb = new StringBuilder();
 	            sb.append(cause);
 	            sb.append(": ");
 	            sb.append(e.getMessage());
 	            LogSupport.major(ctx, this, sb.toString(), e);
			
			} catch (HomeException e) {
				levels_ = Collections.EMPTY_LIST;
				String cause = "Unable to retrieve Levels";
 	            StringBuilder sb = new StringBuilder();
 	            sb.append(cause);
 	            sb.append(": ");
 	            sb.append(e.getMessage());
 	            LogSupport.major(ctx, this, sb.toString(), e);
				
			}
			
			return levels_;
		}
	}
	
	public boolean isDunningExempt(Context ctx)
	{
		if(this.getAllLevels(ctx).size() == 0)
		{
			return true;
		}else
			return false;
	}
	
	public int getLastLevelIndex(final Context ctx)
	{
		return getAllLevels(ctx).size() - 1;
	}
	
	public DunningLevel getLevelAt(final Context ctx, int index)
	{
	    return getAllLevels(ctx).get(index);
	}
	
	public DunningLevel getLevel(Context ctx,int levelId)
	{
		DunningLevel level = null;
		try {
			final Home home = (Home) ctx.get(DunningLevelHome.class);
			level = (DunningLevel)home.find(ctx, new And().add(new EQ(DunningLevelXInfo.DUNNING_POLICY_ID,getDunningPolicyId()))
					.add(new EQ(DunningLevelXInfo.ID,levelId)));
		}catch(HomeException e)
		{
			
		}
		if(level == null)
		{
			level = getAllLevels(ctx).get(levelId-1);
		}
		return level;
	}
}
