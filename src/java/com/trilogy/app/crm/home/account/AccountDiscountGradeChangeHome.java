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

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.discount.DiscountActivityUtils;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.app.crm.bean.DiscountActivityTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author Manish.Negi@redknee.com
 * @since 2016-10-12 
 * Purpose: To add entry into DISCOUNTACTIVITYTRIGGER table for the event type ACCOUNT_DISCOUNT_GRADE_CHANGE_EVENT 
 */

public class AccountDiscountGradeChangeHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	public AccountDiscountGradeChangeHome(final Context ctx, final Home home)
    {
        super(ctx, home);
    }
	
	@Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        //
		final Account newAccount = (Account) super.store(ctx, obj);
        
        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        //final Account newAccount = (Account)obj;
        
        LogSupport.info(ctx, this, "AccountDiscountGradeChangeHome :Old Discount Grade" + oldAccount.getDiscountGrade() + "value for the old account ["+newAccount.getBAN() + " ]");
        LogSupport.info(ctx, this, "AccountDiscountGradeChangeHome :New Discount Grade" + newAccount.getDiscountGrade() + " value for the new account ["+oldAccount.getBAN() + " ]");
        
        
        if (!(newAccount.getDiscountGrade().equals(oldAccount.getDiscountGrade()))) {
        	 DiscountActivityUtils.createTrigger(
            		 DiscountActivityTypeEnum.ACCOUNT_DISCOUNT_GRADE_CHANGE_EVENT, ctx, newAccount.getSpid(),newAccount.getBAN());
        
        }
        
       // return newAccount;
        return newAccount;
    }

}
