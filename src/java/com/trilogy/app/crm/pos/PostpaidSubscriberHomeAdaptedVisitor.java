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
package com.trilogy.app.crm.pos;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberResultSetAdapter;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;

/**
 * 
 * Gets all The limited subscribers
 * 
 * @author amedina
 *
 */
public class PostpaidSubscriberHomeAdaptedVisitor implements Visitor
{

	public PostpaidSubscriberHomeAdaptedVisitor(Context ctx)
	{
		result_ = new SubscriberTransientHome(ctx); 
	}
	
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException 
	{
		try
		{
			// controlled disabling for LogSupport.isDebugEnabled
			Context ctx1=ctx.createSubContext();
			ctx1.put(LogSupport.SEVERITY_THRESHOLD, SeverityEnum.INFO);

			Subscriber sub = (Subscriber) SubscriberResultSetAdapter.instance().f(ctx1,(XResultSet) obj);
			result_.create(ctx, sub);
		}
		catch (SQLException e)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,e.getMessage(),e).log(ctx);
			}
			throw new AgentException(e);
		}
		catch (HomeException e) 
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,e.getMessage(),e).log(ctx);
			}
			throw new AgentException(e);
		}
	}
	
	/**
	 * @return Returns the result.
	 */
	public Home getResult()
	{
		return result_;
	}

	protected Home result_;


}
