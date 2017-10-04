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

/**
 * @author cindy.wong@redknee.com
 * @since 2010-09-29
 */
public class IgnoreParameterHttpServletRequestWrapper extends
    HttpServletRequestWrapper
{

	/**
	 * Constructor for IgnoreParamterHttpServletRequestWrapper.
	 * 
	 * @param request
	 *            Request to be wrapped.
	 * @param key
	 *            Ignored parameter key.
	 * @param values
	 *            Ignored parameter values.
	 */
	public IgnoreParameterHttpServletRequestWrapper(HttpServletRequest request,
	    String key, String... values)
	{
		super(request);
		key_ = key;
		values_ = values;
	}

	@Override
	public String getParameter(String key)
	{
		if (key_.equals(key))
		{
			String original = super.getParameter(key);
			for (String value : values_)
			{
				if (value.equals(original))
				{
					return null;
				}
			}
		}
		return super.getParameter(key);
	}

	private final String[] values_;
	private final String key_;
}
