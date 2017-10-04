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

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.framework.xhome.context.Context;

/**
 * Predicate responsible to filter adjustment types based on a parent category.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class AdjustmentTypeCategoryFilteredPredicate extends FilteredMultiSelectPredicate
{
    private static final long serialVersionUID = 1L;

    public AdjustmentTypeCategoryFilteredPredicate(String filterId, String selectedIds)
    {
        super(filterId, selectedIds);
    }
    
    public AdjustmentTypeCategoryFilteredPredicate(String filterId)
    {
        super(filterId);
    }

    public boolean f(final Context ctx, final Object obj)
    {
        AdjustmentType adjType = (AdjustmentType) obj;
        boolean result = true;
        
        // Filter category
        if (getFilterId()>0)
        {
            result = AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, adjType.getCode(),
                    AdjustmentTypeEnum.get((short) getFilterId()));
        }
        
        // Filter rest only if in category
        if (result)
        {
            result = !adjType.isCategory()
                && !getSelectedIds().contains(Integer.valueOf(adjType.getCode()))
                && !AdjustmentTypeSupportHelper.get(ctx).isInOneOfCategories(ctx, adjType.getCode(),
                        AdjustmentTypeEnum.RecurringCharges, AdjustmentTypeEnum.AuxiliaryBundles,
                        AdjustmentTypeEnum.AuxiliaryServices, AdjustmentTypeEnum.DiscountAuxiliaryServices,
                        AdjustmentTypeEnum.Payments);
        }
        
        return result;
    }

}

