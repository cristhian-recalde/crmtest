/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.web.control.PropertyDynamicLabelWrapper.AbstractPropertyLabelFunction;
import com.trilogy.app.crm.web.control.PropertyDynamicLabelWrapper.BeanPropertyLabelFunction;


/**
 * @author simar.singh@redknee.com
 * @Link (PropertyDynamicLabelWrapper) A class with static constants and methods to hold
 *       the SPID Configurable Bean/Column Property Label Maps
 * 
 * 
 * 
 */
public class PropertyDynamicLabelMaps<BEAN extends AbstractBean>
{

    public static final Map<PropertyInfo, BeanPropertyLabelFunction<CRMSpid>> CALL_DETAIL_SPID_COMPONENTS;
    static
    {
        CALL_DETAIL_SPID_COMPONENTS = Collections.unmodifiableMap(getCallDetailSpidComponentRatingMap());
    }


    public static Map<PropertyInfo, BeanPropertyLabelFunction<CRMSpid>> getCallDetailSpidComponentRatingMap()
    {
        final HashMap<PropertyInfo, BeanPropertyLabelFunction<CRMSpid>> spidPropertyLabelMap;
        {
            spidPropertyLabelMap = new HashMap<PropertyInfo, BeanPropertyLabelFunction<CRMSpid>>();
            spidPropertyLabelMap.put(CallDetailXInfo.COMPONENT_CHARGE1, new AbstractPropertyLabelFunction<CRMSpid>(
                    CallDetailXInfo.COMPONENT_CHARGE1)
            {

                public String getLabel(Context ctx, CRMSpid crmSpid)
                {
                    return crmSpid.getChargingComponentsConfig(ctx).getComponentFirst().getName();
                }
            });
            spidPropertyLabelMap.put(CallDetailXInfo.COMPONENT_CHARGE2, new AbstractPropertyLabelFunction<CRMSpid>(
                    CallDetailXInfo.COMPONENT_CHARGE2)
            {

                public String getLabel(Context ctx, CRMSpid crmSpid)
                {
                    return crmSpid.getChargingComponentsConfig(ctx).getComponentSecond().getName();
                }
            });
            spidPropertyLabelMap.put(CallDetailXInfo.COMPONENT_CHARGE3, new AbstractPropertyLabelFunction<CRMSpid>(
                    CallDetailXInfo.COMPONENT_CHARGE3)
            {

                public String getLabel(Context ctx, CRMSpid crmSpid)
                {
                    return crmSpid.getChargingComponentsConfig(ctx).getComponentThird().getName();
                }
            });
        }
        return spidPropertyLabelMap;
    }
}
