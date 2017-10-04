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

import java.util.Set;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CRMAdjustmentTypeSupport extends DefaultAdjustmentTypeSupport
{
    protected static AdjustmentTypeSupport CRM_instance_ = null;
    public static AdjustmentTypeSupport instance()
    {
        if (CRM_instance_ == null)
        {
            CRM_instance_ = new CRMAdjustmentTypeSupport();
        }
        return CRM_instance_;
    }

    protected CRMAdjustmentTypeSupport()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AdjustmentTypeEnum> getHiddenAdjustmentTypes(Context ctx)
    {
        Set<AdjustmentTypeEnum> hiddenTypes = super.getHiddenAdjustmentTypes(ctx);
        
        SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);

        String strCats = sysCfg.getAdjTypeCat().trim();
        if (strCats != null && strCats.length() > 0)
        {
            String[] arrCat = strCats.split(",");
            for (String strCat : arrCat)
            {
                hiddenTypes.add(AdjustmentTypeEnum.get(Short.valueOf(strCat)));
            }
        }
        
        return hiddenTypes;
    }

}
