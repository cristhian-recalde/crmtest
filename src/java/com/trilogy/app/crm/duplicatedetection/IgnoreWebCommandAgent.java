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

import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;

import com.trilogy.app.crm.servlet.IgnoreCommandHttpServletRequestWrapper;

/**
 * Wraps the servlet request with one that ignores the listed web commands.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-29
 */
public class IgnoreWebCommandAgent extends ContextAgentProxy
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	public static final String ORIGINAL_REQUEST_KEY =
	    "IgnoreWebCommandAgent.originalHttpServletRequest";

	/**
	 * Constructor for IgnoreWebCommandAgent.
	 */
	public IgnoreWebCommandAgent()
	{
		this(new String[0]);
	}

	/**
	 * Constructor for IgnoreWebCommandAgent.
	 * 
	 * @param delegate
	 */
	public IgnoreWebCommandAgent(ContextAgent delegate)
	{
		this(delegate, new String[0]);
	}

	/**
	 * Constructor for IgnoreWebCommandAgent.
	 */
	public IgnoreWebCommandAgent(String... commands)
	{
		super();
		commands_ = commands;
	}

	/**
	 * Constructor for IgnoreWebCommandAgent.
	 * 
	 * @param delegate
	 */
	public IgnoreWebCommandAgent(ContextAgent delegate, String... commands)
	{
		super();
		commands_ = commands;
	}

	@Override
	public void execute(Context ctx) throws AgentException
	{
		HttpServletRequest request =
		    (HttpServletRequest) ctx.get(HttpServletRequest.class);
		ctx.put(ORIGINAL_REQUEST_KEY, request);
		HttpServletRequest newRequest =
		    new IgnoreCommandHttpServletRequestWrapper(request, commands_);
		ctx.put(HttpServletRequest.class, newRequest);

		delegate(ctx);
	}

	private final String[] commands_;
}
