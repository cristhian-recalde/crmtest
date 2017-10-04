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
package com.trilogy.app.crm.web.control;

import java.util.Comparator;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeIdentitySupport;
import com.trilogy.app.crm.filter.AdjustmentTypeCategoryFilteredPredicate;

/**
 * Multi select web control used to display adjustment types.
 * @author Marcio Marques
 * @since 8.5
 * 
 */
public class AdjustmentTypeMultiSelectWebControl extends FilteredMultiSelectWebControl
{
    private static Comparator COMPARATOR = new Comparator(){

        @Override
        public int compare(Object arg0, Object arg1)
        {
            AdjustmentType b0= (AdjustmentType) arg0;
            AdjustmentType b1= (AdjustmentType) arg1;
            
            if (b0==null || b1==null || b0.getCode()==b1.getCode())
            {
                return 0;
            }
            else if (b0.getCode()>b1.getCode())
            {
                return 1;
            }
            else
            {
                return -1;
            }

        }
        
    };
    public AdjustmentTypeMultiSelectWebControl()
    {
      super(AdjustmentTypeHome.class, AdjustmentTypeIdentitySupport.instance(), new AdjustmentTypeKeyTreeWebControl(AdjustmentTypeHome.class,false, true, true, false),
                new AdjustmentTypeDescriptionOutputWebControl(), new AdjustmentTypeCategoryFilteredPredicate("-1", ""));
      setComparator(COMPARATOR);    
    }
    public AdjustmentTypeMultiSelectWebControl(AdjustmentTypeEnum... filteredCategories)
    {
      super(AdjustmentTypeHome.class, AdjustmentTypeIdentitySupport.instance(), new AdjustmentTypeKeyTreeWebControl(AdjustmentTypeHome.class, true, true, false, filteredCategories),
                new AdjustmentTypeDescriptionOutputWebControl(), new AdjustmentTypeCategoryFilteredPredicate("-1", ""));
      setComparator(COMPARATOR);    
    }
}