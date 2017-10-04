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

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.xhome.adapter.WriteOnlyReflectionBeanAdapter;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class ServiceUIAdapter extends WriteOnlyReflectionBeanAdapter<com.redknee.app.crm.bean.core.Service, com.redknee.app.crm.bean.ui.Service>
{
    // Create a map of core-transient fields to GUI model property infos
    private static final Map<String, PropertyInfo> customCoreToUIMap;
    static
    {
        customCoreToUIMap = new HashMap<String, PropertyInfo>();
        customCoreToUIMap.put(
                com.redknee.app.crm.bean.core.Service.SERVICE_EXTENSIONS_FIELD_NAME, 
                com.redknee.app.crm.bean.ui.ServiceXInfo.SERVICE_EXTENSIONS);
    }

    public ServiceUIAdapter(Context ctx)
    {
        super(ctx, 
                com.redknee.app.crm.bean.core.Service.class, 
                customCoreToUIMap, 
                com.redknee.app.crm.bean.ui.Service.class);
    }

}
