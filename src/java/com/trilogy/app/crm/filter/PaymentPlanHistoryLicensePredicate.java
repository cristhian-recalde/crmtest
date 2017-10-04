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

import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.AbstractFalse;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author Angie Li
 *
 * Predicate returns TRUE when Disable Payment Plan History feature License is enabled.
 */
public class PaymentPlanHistoryLicensePredicate extends SimpleDeepClone implements Predicate, XStatement
{
    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        boolean result = PaymentPlanSupportHelper.get(ctx).isHistoryEnabled(ctx);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Validate if Disable Payment Plan History feature is enabled: " + result, null).log(ctx);
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
        ((False) (False.instance())).set(ctx,ps);
        
    }
}
