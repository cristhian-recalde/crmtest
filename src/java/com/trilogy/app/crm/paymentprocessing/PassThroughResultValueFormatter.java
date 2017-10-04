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
package com.trilogy.app.crm.paymentprocessing;

import com.trilogy.framework.xhome.context.Context;

/**
 * Return the result value as is.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.6
 */
public class PassThroughResultValueFormatter implements ResultValueFormatter
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final PassThroughResultValueFormatter instance =
	    new PassThroughResultValueFormatter();

	public static PassThroughResultValueFormatter instance()
	{
		return instance;
	}

	@Override
	public Object formatValue(Context ctx, Object result)
	{
		return result;
	}

}
