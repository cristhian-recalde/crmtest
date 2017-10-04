/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * Only shows Adjustment Type categories.
 * 
 * @author cindy.wong@redknee.com
 * 
 */
public class AdjustmentTypeIsCategory implements Predicate
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create new predicate.
     */
    public AdjustmentTypeIsCategory()
    {
        // empty
    }

    /**
     * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        AdjustmentType adjustmentType = (AdjustmentType) obj;
        return adjustmentType.isCategory();
    }

}
