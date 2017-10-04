/*
 * Created on Jul 4, 2005
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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author psperneac
 *
 */
public class TracerImpl implements Tracer
{

	public void trace(Context ctx, String message)
	{
		if(LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this,message,null).log(ctx);
		}
	}

	public void trace(Context ctx, String message, Throwable th)
	{
		if(LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this,message,null).log(ctx);
		}
	}
}
