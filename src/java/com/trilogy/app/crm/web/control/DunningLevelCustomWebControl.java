package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.DunningLevelXInfo;
import com.trilogy.app.crm.dunning.LevelInfo;
import com.trilogy.app.crm.dunning.LevelInfoXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.facets.java.lang.StringWebControl;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.snippet.context.ContextUtils;

public class DunningLevelCustomWebControl implements WebControl{
	
	public DunningLevelCustomWebControl() 
	{
		super();
		stringWebControl_ = new StringWebControl();
	}

    private StringWebControl stringWebControl_; 

	@Override
	public Object fromWeb(Context ctx,
			ServletRequest paramServletRequest, String paramString)
			throws NullPointerException 
	{
		Account account = ContextUtils.getBeanInContextByType(ctx, AbstractWebControl.BEAN, Account.class);
		long policyId = account.getDunningPolicyId();
		int dunningLevelId = account.getLastDunningLevel();
		Integer level= null;
		
		if(-1 == policyId || 0 == dunningLevelId)
		{
			level = 0;
		}
		else
		{
			And and = new And();
			and.add(new EQ(DunningLevelXInfo.DUNNING_POLICY_ID, policyId));
			and.add(new EQ(DunningLevelXInfo.ID,dunningLevelId));
			try
			{
				DunningLevel dunningLevel = HomeSupportHelper.get(ctx).findBean(ctx, DunningLevel.class, and);
				level = dunningLevel.getLevel();
			} 
			catch (HomeException e) 
			{
				LogSupport.major(ctx, this, "Unable to find Dunning Level Object for Dunning Policy Id="+policyId);
			}
		}

		return level;
	}

	@Override
	public void toWeb(Context ctx, PrintWriter out,
			String name, Object obj) 
	{
		Account account = ContextUtils.getBeanInContextByType(ctx, AbstractWebControl.BEAN, Account.class);
		long policyId = account.getDunningPolicyId();
		int dunningLevelId = account.getLastDunningLevel();
		name = "--";

		And and = new And();
		and.add(new EQ(DunningLevelXInfo.DUNNING_POLICY_ID, policyId));
		and.add(new EQ(DunningLevelXInfo.ID,dunningLevelId));
		try
		{
			DunningLevel dunningLevel = HomeSupportHelper.get(ctx).findBean(ctx, DunningLevel.class, and);
			if(dunningLevel != null)
			{
				LevelInfo levelInfo = HomeSupportHelper.get(ctx).findBean(ctx, LevelInfo.class, new EQ(LevelInfoXInfo.ID,dunningLevel.getLevel()));
				if(levelInfo != null)
				name = levelInfo.getName();
			}
		} 
		catch (HomeException e) 
		{
			LogSupport.major(ctx, this, "Unable to find Dunning Level Object for Dunning Policy Id="+policyId);
		}

		WebControl wc = stringWebControl_;
        if( wc != null)
        {
            wc.toWeb(ctx, out, name, name);
        }
        else
        {
            throw new NullPointerException("Unable to find a delegate WebControl in DataTypeSelectionWebControl");
        }  	
	}

	/**
	 * {@inheritDoc}
	 */
	public void fromWeb(Context ctx, Object obj, ServletRequest req, String name) 
	{
		
	}
}
