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
package com.trilogy.app.crm.home.validator;

import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author bdhavalshankh
 * @since 9.5.1
 */
public class MaxSubscriptionsAllowedValidator implements Validator
{

	private static MaxSubscriptionsAllowedValidator instance =
	    new MaxSubscriptionsAllowedValidator();

	public static MaxSubscriptionsAllowedValidator instance()
	{
		return instance;
	}

	/**
	 * @param ctx
	 * @param obj
	 * @throws IllegalStateException
	 * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context,
	 *      java.lang.Object)
	 */
	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException
	{
		CreditCategory creditCategory = (CreditCategory) obj;
		CompoundIllegalStateException el = new CompoundIllegalStateException();
		if (creditCategory.getMaxSubscriptionsAllowed() == 0)
        {
            el.thrown(new IllegalPropertyArgumentException(CreditCategoryXInfo.MAX_SUBSCRIPTIONS_ALLOWED, " Maximum subscription allowed can not be zero"));
        }
		el.throwAll();
	}

}
