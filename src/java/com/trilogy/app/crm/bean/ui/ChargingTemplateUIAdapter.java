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

import com.trilogy.app.crm.xhome.adapter.WriteOnlyReflectionBeanAdapter;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;

/**
 * Adapts between the UI charging template object and the model charging template object.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class ChargingTemplateUIAdapter  extends WriteOnlyReflectionBeanAdapter<com.redknee.app.crm.bean.ChargingTemplate, com.redknee.app.crm.bean.ui.ChargingTemplate>
{
    // Create a map of core-transient fields to GUI model property infos
    private static final Map<String, PropertyInfo> CUSTOM_CORE_TO_UI_MAP;
    static
    {
        CUSTOM_CORE_TO_UI_MAP = new HashMap<String, PropertyInfo>();
    }

    public ChargingTemplateUIAdapter(Context ctx)
    {
        super(ctx, 
                com.redknee.app.crm.bean.ChargingTemplate.class, 
                CUSTOM_CORE_TO_UI_MAP, 
                com.redknee.app.crm.bean.ui.ChargingTemplate.class);
    }
}
