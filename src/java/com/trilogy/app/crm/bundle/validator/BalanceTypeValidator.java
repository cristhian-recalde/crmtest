/*
 * BalanceTypeValidator.java
 *
 * Author : victor.stratan@redknee.com
 * Date: Dec 19, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bundle.validator;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.app.crm.bundle.BalanceApplication;
import com.trilogy.app.crm.bundle.BalanceApplicationHome;
import com.trilogy.app.crm.bundle.BalanceApplicationXInfo;

public class BalanceTypeValidator implements Validator
{
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        HomeOperationEnum op = (HomeOperationEnum) ctx.get(HomeOperationEnum.class, HomeOperationEnum.CREATE);
        BalanceApplication app = (BalanceApplication) obj;
        CompoundIllegalStateException el = new CompoundIllegalStateException();

        Home home = (Home) ctx.get(BalanceApplicationHome.class);
        Object result = null;
        try
        {
            EQ eq = new EQ(BalanceApplicationXInfo.UNIT_TYPE, app.getUnitType());
            if (op == HomeOperationEnum.STORE)
            {
                EQ idCondition = new EQ(BalanceApplicationXInfo.APPLICATION_ID, Long.valueOf(app.getApplicationId()));
                BalanceApplication old = (BalanceApplication) home.find(ctx, idCondition);
                if (old == null || old.getUnitType() != app.getUnitType())
                {
                    // check Unit Type on STORE only if Unit Type changed
                    result = home.find(ctx, eq);
                }
            }
            else
            {
                result = home.find(ctx, eq);
            }
        } catch (HomeException e)
        {
            el.thrown(new IllegalPropertyArgumentException(BalanceApplicationXInfo.UNIT_TYPE, "Cannot search BalanceApplicationHome."));
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
        if (result != null)
        {
            el.thrown(new IllegalPropertyArgumentException(BalanceApplicationXInfo.UNIT_TYPE, "Cannot have 2 Balance Types with the same unit type."));
        }

        el.throwAll();
    }
}
