/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.duplicatedetection;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;

/**
 * Conditionally displays errors in the HTMLExceptionListener stored in the
 * context.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-28
 */
public class HTMLExceptionListenerErrorDisplayAgent extends ContextAgentProxy
{

	/**
	 * Constructor for HTMLExceptionListenerErrorDisplayAgent.
	 */
	public HTMLExceptionListenerErrorDisplayAgent()
	{
		this(HTMLExceptionListenerErrorDisplayAgent.class);
	}

	/**
	 * Constructor for HTMLExceptionListenerErrorDisplayAgent.
	 * 
	 * @param delegate
	 */
	public HTMLExceptionListenerErrorDisplayAgent(ContextAgent delegate)
	{
		this(delegate, HTMLExceptionListenerErrorDisplayAgent.class);
	}

	/**
	 * Constructor for HTMLExceptionListenerErrorDisplayAgent.
	 */
	public HTMLExceptionListenerErrorDisplayAgent(Object key)
	{
		this(key, true);
	}

	/**
	 * Constructor for HTMLExceptionListenerErrorDisplayAgent.
	 * 
	 * @param delegate
	 */
	public HTMLExceptionListenerErrorDisplayAgent(ContextAgent delegate,
	    Object key)
	{
		this(delegate, key, true);
	}

	/**
	 * Constructor for HTMLExceptionListenerErrorDisplayAgent.
	 */
	public HTMLExceptionListenerErrorDisplayAgent(Object key,
	    boolean defaultCondition)
	{
		super();
		conditionKey_ = key;
		defaultCondition_ = defaultCondition;
	}

	/**
	 * Constructor for HTMLExceptionListenerErrorDisplayAgent.
	 * 
	 * @param delegate
	 */
	public HTMLExceptionListenerErrorDisplayAgent(ContextAgent delegate,
	    Object key, boolean defaultCondition)
	{
		super(delegate);
		conditionKey_ = key;
		defaultCondition_ = defaultCondition;
	}

	@Override
	public void execute(Context ctx) throws AgentException
	{
		HTMLExceptionListener exceptionListener =
		    (HTMLExceptionListener) ctx.get(ExceptionListener.class);

		if (ctx.getBoolean(
		    DuplicateAccountDetectionSearchAgentV2.DISPLAY_ERROR, false)
		    && exceptionListener != null)
		{
			/*
			 * Display exceptions encountered.
			 */
			if (ctx.getBoolean(conditionKey_, defaultCondition_))
			{
				exceptionListener.toWeb(ctx,
				    (PrintWriter) ctx.get(PrintWriter.class), null, null);
			}
		}
		delegate(ctx);
	}

	private final Object conditionKey_;
	private final boolean defaultCondition_;
}
