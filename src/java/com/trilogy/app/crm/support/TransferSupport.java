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

package com.trilogy.app.crm.support;

import java.util.HashSet;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;

public class TransferSupport
{
    public static HashSet<Integer> getMMAdjustmentTypes(Context ctx)
    {
        HashSet<Integer> ret = new HashSet<Integer>();

        try
        {
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsContributorDebit));
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsContributorSurplus));
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsContributorDiscount));
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsRecipientCredit));
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsRecipientSurplus));
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsRecipientDiscount));
        }
        catch(Exception e)
        {
            LogSupport.minor(ctx, TransferSupport.class.getName(), "Error retreiving adjustment types.", e);
        }

        return ret;
    }

    public static boolean isContributorAmountAdjustmentType(Context ctx, int adjutmentTypeCode)
    {
        return adjutmentTypeCode == AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsContributorDebit);
    }

    public static boolean isContributorSurplusDiscountAdjustmentType(Context ctx, int adjustmentTypeCode)
    {
        return adjustmentTypeCode == AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsContributorSurplus)
            || adjustmentTypeCode == AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsContributorDiscount);
    }

    public static boolean isRecipientAmountAdjustmentType(Context ctx, int adjutmentTypeCode)
    {
        return adjutmentTypeCode == AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsRecipientCredit);
    }

    public static boolean isRecipientSurplusDiscountAdjustmentType(Context ctx, int adjustmentTypeCode)
    {
        return adjustmentTypeCode == AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsRecipientSurplus)
            || adjustmentTypeCode == AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.TransferFundsRecipientDiscount);
    }

    
    public static HashSet<Integer> getMMContributorAdjustmentTypes(Context ctx)
    {
        HashSet<Integer> ret = new HashSet<Integer>();
        try
        {
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                    AdjustmentTypeEnum.TransferFundsContributorDebit));
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                    AdjustmentTypeEnum.TransferFundsContributorSurplus));
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                    AdjustmentTypeEnum.TransferFundsContributorDiscount));
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, TransferSupport.class.getName(), "Error retreiving adjustment types.", e);
        }
        return ret;
    }
    public static HashSet<Integer> getMMRecipientAdjustmentTypes(Context ctx)
    {
        HashSet<Integer> ret = new HashSet<Integer>();
        try
        {
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                    AdjustmentTypeEnum.TransferFundsRecipientCredit));
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                    AdjustmentTypeEnum.TransferFundsRecipientSurplus));
            ret.add(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                    AdjustmentTypeEnum.TransferFundsRecipientDiscount));
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, TransferSupport.class.getName(), "Error retreiving adjustment types.", e);
        }
        return ret;
    }
    public static final String OPERATOR_ID = "Operator";
    public static final String EXTERNAL_ID = "External";
}