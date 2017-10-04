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
package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.WebController;

import com.trilogy.app.crm.bean.Account;

/**
 * @author cindy.wong@redknee.com
 * @since 9.1
 */
public class AccountRootPredicate implements Predicate
{

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean f(Context ctx, Object obj) throws AbortVisitException
	{
		boolean result = false;
		if (obj instanceof Account)
		{
			result = ((Account) obj).isRootAccount();
		}
		else if (ctx.has(Account.class))
        {
            result = ((Account) ctx.get(Account.class)).isRootAccount();
        }
		else
		{
			Object bean = (WebController.getBean(ctx));
			if (bean instanceof Account)
			{
				result = ((Account) bean).isRootAccount();
			}
		}
		return result;
	}
}
