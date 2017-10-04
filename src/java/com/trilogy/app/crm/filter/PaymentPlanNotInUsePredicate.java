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
package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.payment.PaymentPlan;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author ali
 *
 * Returns TRUE if Payment Plan is not used by any accounts/subscribers.  
 * Returns FALSE otherwise.
 */
public class PaymentPlanNotInUsePredicate extends SimpleDeepClone implements  Predicate, XStatement 
{

	public PaymentPlanNotInUsePredicate() {}
	
	@Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
	{
        boolean result = true;
        
	    if (obj instanceof Long)
	    {
	        result = !isPaymentPlanInUse(ctx, (Long)obj);
	    }
	    else if (obj instanceof PaymentPlan)
	    {
	        result = !isPaymentPlanInUse(ctx, ((PaymentPlan) obj).getId());
	    }
		 		
		return result; 
	}
	
    @Override
    public String createStatement(Context ctx)
    {
        return ((False) (False.instance())).createStatement(ctx);
    }


    @Override
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
        ((False) (False.instance())).set(ctx, ps);
    }

    /**
     * @param ctx
     * @param paymentPlanID
     * @return
     */
    private boolean isPaymentPlanInUse(Context ctx, long paymentPlanID)
    {
        boolean accountUsingPaymentPlanExists = false;
        try
        {
            accountUsingPaymentPlanExists = HomeSupportHelper.get(ctx).hasBeans(
                    ctx, 
                    Account.class, 
                    new EQ(AccountXInfo.PAYMENT_PLAN, Long.valueOf(paymentPlanID)));
        }
        catch (final HomeException hEx)
        {
            new MinorLogMsg(this, "Could not retrieve accounts with Payment Plan id=" + paymentPlanID,
                    null).log(ctx);
        }

        return accountUsingPaymentPlanExists;
    }

	
}
