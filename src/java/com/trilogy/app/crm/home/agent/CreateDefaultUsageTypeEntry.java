/*
 * Created on May 17, 2005
 *
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
package com.trilogy.app.crm.home.agent;

import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Creates the UsageType entry with id #1 if it doesn't exist
 * 
 * @author psperneac
 *
 */
public class CreateDefaultUsageTypeEntry implements ContextAgent
{

	public void execute(Context ctx) throws AgentException
	{
		Home hh=(Home) ctx.get(UsageTypeXDBHome.class);
		
		if(hh==null)
		{
			return;
		}
		
		try
		{
			UsageType u= (UsageType) hh.find(ctx, Long.valueOf(1));
			
			if(u==null)
			{
				u=new UsageType();
				u.setId(1);
				u.setDescription("default");
            u.setInvoiceDetailDescription("default");
				u.setInvoiceSummaryDescription("default");
				
				hh.create(ctx,u);
			}
			else if(u.getState()==UsageTypeStateEnum.DEPRECATED_INDEX)
			{
				
				u.setState(UsageTypeStateEnum.ACTIVE_INDEX);
				hh.store(ctx,u);
			}
		}
		catch (HomeException e)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,e.getMessage(),e).log(ctx);
			}
		}
	}

}
