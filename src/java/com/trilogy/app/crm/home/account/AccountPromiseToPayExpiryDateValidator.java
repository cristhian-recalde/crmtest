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
package com.trilogy.app.crm.home.account;

import java.util.Date;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;

/**
 * Validate account promise to pay date if the account is entering PTP.
 * 
 * @author cindy.wong@redknee.com
 * @since 2011-04-27
 */
public class AccountPromiseToPayExpiryDateValidator implements Validator
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException
	{
	    CompoundIllegalStateException cise = new CompoundIllegalStateException();

		// always validate when account is in PTP.
		final Account newAccount = (Account) obj;
		if (SafetyUtil.safeEquals(AccountStateEnum.PROMISE_TO_PAY,
		    newAccount.getState()))
		{
			Date ptpDate = newAccount.getPromiseToPayDate();
			if (ptpDate == null || ptpDate.getTime() == 0)
			{
				cise.thrown(new IllegalPropertyArgumentException(
				    AccountXInfo.PROMISE_TO_PAY_DATE,
				    "Promise to Pay Expiry Date must be set when account is in Promise-to-Pay state."));
			}
		}

		cise.throwAll();
	}

}
