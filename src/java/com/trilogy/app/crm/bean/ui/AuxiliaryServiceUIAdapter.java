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
package com.trilogy.app.crm.bean.ui;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.xhome.adapter.ReflectionBeanAdapter;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class AuxiliaryServiceUIAdapter extends ReflectionBeanAdapter<com.redknee.app.crm.bean.core.custom.AuxiliaryService, com.redknee.app.crm.bean.ui.AuxiliaryService>
{
    // Create a map of core-transient fields to GUI model property infos
    private static final Map<String, PropertyInfo> customCoreToUIMap;
    static
    {
        customCoreToUIMap = new HashMap<String, PropertyInfo>();
        customCoreToUIMap.put(
                com.redknee.app.crm.bean.core.custom.AuxiliaryService.AUXILIARY_SERVICE_EXTENSIONS_FIELD_NAME, 
                com.redknee.app.crm.bean.ui.AuxiliaryServiceXInfo.AUXILIARY_SERVICE_EXTENSIONS);
    }

    public AuxiliaryServiceUIAdapter(Context ctx)
    {
        super(ctx, 
                com.redknee.app.crm.bean.core.custom.AuxiliaryService.class, 
                customCoreToUIMap, 
                com.redknee.app.crm.bean.ui.AuxiliaryService.class);
    }

}
