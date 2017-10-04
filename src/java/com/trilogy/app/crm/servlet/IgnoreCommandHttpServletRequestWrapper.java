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
package com.trilogy.app.crm.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.trilogy.framework.xhome.webcontrol.WebController;

/**
 * Extends the HttpServletRequestWrapper to ignore a particular XMenu command.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-27
 */
public class IgnoreCommandHttpServletRequestWrapper extends
    HttpServletRequestWrapper
{

	public IgnoreCommandHttpServletRequestWrapper(HttpServletRequest request,
	    String... cmds)
	{
		super(request);
		request_ = request;
		cmds_ = cmds;
	}

	@Override
	public String getParameter(String key)
	{
		for (String cmd : getCmds())
		{
			if (WebController.isCmd(cmd, getHttpServletRequest()))
			{
				return null;
			}
		}
		return super.getParameter(key);
	}

	protected String[] getCmds()
	{
		return cmds_;
	}

	protected HttpServletRequest getHttpServletRequest()
	{
		return request_;
	}

	private final HttpServletRequest request_;
	private final String[] cmds_;
}
