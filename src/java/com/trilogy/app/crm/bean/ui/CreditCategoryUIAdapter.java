package com.trilogy.app.crm.bean.ui;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.xhome.adapter.WriteOnlyReflectionBeanAdapter;


public class CreditCategoryUIAdapter  extends WriteOnlyReflectionBeanAdapter<com.redknee.app.crm.bean.core.CreditCategory, com.redknee.app.crm.bean.ui.CreditCategory>
{
    // Create a map of core-transient fields to GUI model property infos
    private static final Map<String, PropertyInfo> CUSTOM_CORE_TO_UI_MAP;
    static
    {
        CUSTOM_CORE_TO_UI_MAP = new HashMap<String, PropertyInfo>();
        CUSTOM_CORE_TO_UI_MAP.put(
                com.redknee.app.crm.bean.core.CreditCategory.CREDIT_CATEGORY_EXTENSIONS_FIELD_NAME, 
                com.redknee.app.crm.bean.ui.CreditCategoryXInfo.CREDIT_CATEGORY_EXTENSIONS);
    }

    public CreditCategoryUIAdapter(Context ctx)
    {
        super(ctx, 
                com.redknee.app.crm.bean.core.CreditCategory.class, 
                CUSTOM_CORE_TO_UI_MAP, 
                com.redknee.app.crm.bean.ui.CreditCategory.class);
    }
}
