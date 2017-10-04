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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.account.SubscriptionTypeKeyWebControl;

/**
 * Web control to hide subscription type from account rows during duplicate
 * detection.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class DuplicateDetectionResultSubscriptionTypeWebControl extends
    SubscriptionTypeKeyWebControl
{

	/**
	 * Constructor for DuplicateDetectionResultSubscriptionTypeWebControl.
	 */
	public DuplicateDetectionResultSubscriptionTypeWebControl()
	{
		super();
	}

	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		if (obj instanceof Number)
		{
			Number number = (Number) obj;
			// ignore negative values
			if (number != null && number.longValue() < 0)
			{
				return;
			}
		}

		super.toWeb(ctx, out, name, obj);
	}
}
