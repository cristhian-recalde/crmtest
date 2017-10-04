/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.account;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.AccountsDiscount;

/**
 * Validator for AccountsDiscount [Service level discount]
 * @author shailesh.makhijani
 * @since 9.7.2
 * @see AccountDiscountClassValidator
 */
public class AccountsDiscountValidator implements Validator {

	public AccountsDiscountValidator() {
	}

	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException {
		
		Account account = (Account)obj ;
		
		// Applicable only for postpaid/Hybrid accounts
		if (account.isPrepaid()){
			return ;
		}
		
		if (account.getDiscountClass()!= Account.DEFAULT_DISCOUNTCLASS && account.getDiscountClass()!= AccountsDiscount.DEFAULT_DISCOUNTCLASS && account.getDiscountsClassHolder().size() != 0)
		{	
			throwError(ctx, AccountXInfo.DISCOUNT_CLASS, "Either specify Discount class or apply custom discount [service level]  ", null);
		}
		
		/**
		 * In future, we may support multiple discount classes for single account, as of now only 1 discount class per account is supported
		 */
		if (account.getDiscountsClassHolder().size() > 1) {
			throwError(ctx, AccountXInfo.DISCOUNTS_CLASS_HOLDER, "Multiple discount classes are not supported, please select only one discount class ", null);
		}
		
	}
	
	private void throwError(final Context ctx, final PropertyInfo property, final String msg, final Exception cause)
	        throws IllegalStateException
	    {
	        final CompoundIllegalStateException compoundException = new CompoundIllegalStateException();
	        final IllegalPropertyArgumentException propertyException = new IllegalPropertyArgumentException(property, msg);
	        if (cause != null)
	        {
	            propertyException.initCause(cause);
	        }
	        compoundException.thrown(propertyException);
	        compoundException.throwAll();
	    }

}
