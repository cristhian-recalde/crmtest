/*
 * Created on Sep 21, 2005
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
package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberResultSetAdapter;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;

/**
 * Subscriber visitor to be used with xdb.forEach on subscriber home  
 * 
 * @author psperneac
 *
 */
public class SubscriberHomeAdaptedVisitor implements Visitor
{
	protected Subscriber result;

	public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
	{
		try
		{
			// controlled disabling for LogSupport.isDebugEnabled
			Context ctx1=ctx.createSubContext();
			ctx1.put(LogSupport.SEVERITY_THRESHOLD, SeverityEnum.INFO);

			result=(Subscriber) SubscriberResultSetAdapter.instance().f(ctx1,(XResultSet) obj);

			throw new AbortVisitException("got the first one, don't need more.");
		}
		catch (SQLException e)
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
	public Subscriber getResult()
	{
		return result;
	}

	/**
	 * @param result The result to set.
	 */
	public void setResult(Subscriber result)
	{
		this.result = result;
	}
}
