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
package com.trilogy.app.crm.web.action;

import java.security.Permission;

import com.trilogy.app.crm.bean.payment.PaymentPlanStateEnum;
import com.trilogy.app.crm.bean.payment.PaymentPlanXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.action.EditAction;

/**
 * Extends the EditAction class but overwrites the isEnabled function.
 * We want the Edit action to be disabled if the Payment Plan state is
 * set to DEPRECATED.
 * @author Angie Li
 */
public class PaymentPlanActiveEditAction extends EditAction {

	public PaymentPlanActiveEditAction(Permission permission)
	{
		super(permission);
	}
	
	public boolean isEnabled(Context ctx, Object bean)
    {
        return new EQ(PaymentPlanXInfo.STATE, PaymentPlanStateEnum.ACTIVE).f(ctx, bean); 
    }
}
