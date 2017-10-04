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

package com.trilogy.app.crm.adjustmenttype;

/**
 * Bean for adjustment type enhanced GUI property.
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class AdjustmentTypeEnhancedGUIProperty extends
        AbstractAdjustmentTypeEnhancedGUIProperty
{
    public void setParent(Object parent)
    {
        // nop
    }

    public Object getParent()
    {
        final Object parent;
        if (getParentCode() == 0)
        {
            parent = "";
        }
        else
        {
            parent = Integer.valueOf(getParentCode());
        }

        return parent;
    }


    /**
     * A comparator that sorts AdjustmentType by name.
     */
    public int compareTo(Object bean)
    {
        AdjustmentTypeEnhancedGUIProperty other = (AdjustmentTypeEnhancedGUIProperty)bean;
       return getName().compareTo(other.getName());
    }
}
