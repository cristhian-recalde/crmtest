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

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;

/**
 * Puts an HTMLExceptionListener into the context.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-28
 */
public class HTMLExceptionListenerSettingAgent extends ContextAgentProxy
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for ExceptionListenerSettingAgent.
	 */
	public HTMLExceptionListenerSettingAgent()
	{
		this(HTMLExceptionListenerSettingAgent.class);
	}

	/**
	 * Constructor for ExceptionListenerSettingAgent.
	 * 
	 * @param delegate
	 */
	public HTMLExceptionListenerSettingAgent(ContextAgent delegate)
	{
		this(HTMLExceptionListenerSettingAgent.class, delegate);
	}

	/**
	 * Constructor for ExceptionListenerSettingAgent.
	 */
	public HTMLExceptionListenerSettingAgent(Class cls)
	{
		super();
		cls_ = cls;
	}

	/**
	 * Constructor for ExceptionListenerSettingAgent.
	 * 
	 * @param delegate
	 */
	public HTMLExceptionListenerSettingAgent(Class cls, ContextAgent delegate)
	{
		super(delegate);
		cls_ = cls;
	}

	@Override
	public void execute(Context ctx) throws AgentException
	{
		MessageMgr mmgr = new MessageMgr(ctx, getCls());
		HTMLExceptionListener exceptionListener =
		    new HTMLExceptionListener(mmgr);
		ctx.put(ExceptionListener.class, exceptionListener);
		ctx.put(HTMLExceptionListener.class, exceptionListener);

		delegate(ctx);
	}

	protected Class getCls()
	{
		return cls_;
	}

	private final Class cls_;
}
