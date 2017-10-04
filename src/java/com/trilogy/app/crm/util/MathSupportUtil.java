/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.util;

import java.math.BigDecimal;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.RoundingStrategyEnum;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * 
 * @author bpandey
 * 
 */
public class MathSupportUtil
{

    public static long round(Context ctx, int spid, final double amount)
    {
        return round(ctx, spid, BigDecimal.valueOf(amount));
    }


    public static long round(Context ctx, int spid, final BigDecimal amount)
    {
        CRMSpid spidBean = null;
        long roundedAmount = 0;
        short roundingStrategy = RoundingStrategyEnum.ROUND_TO_NEAREST_INDEX;
        try
        {
            spidBean = SpidSupport.getCRMSpid(ctx, spid);
            if (spidBean != null)
            {
                if (amount.signum() == -1)
                {
                    roundingStrategy = spidBean.getNegativeChargeStrategy().getIndex();
                }
                else
                {
                    roundingStrategy = spidBean.getPositiveChargeStrategy().getIndex();
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, MathSupportUtil.class.getName(), "Failed to fetch spid : " + spid
                    + ". Going to apply default rule for rounding.");
        }
        if (roundingStrategy == RoundingStrategyEnum.ROUND_TO_ZERO_INDEX)
        {
            roundedAmount = roundToZero(amount);
        }
        else
        {
            roundedAmount = roundToNearest(amount);
        }
        return roundedAmount;
    }


    private static long roundToZero(BigDecimal chargeAmount)
    {
        return chargeAmount.setScale(0, BigDecimal.ROUND_DOWN).longValue();
    }


    private static long roundToNearest(BigDecimal chargeAmount)
    {
        long amount = 0;
        if (chargeAmount.signum() == -1)
        {
            amount = -1 * (chargeAmount.abs().setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
        }
        else
        {
            amount = chargeAmount.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        }
        return amount;
    }
}
