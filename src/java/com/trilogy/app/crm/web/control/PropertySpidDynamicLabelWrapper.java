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

import java.util.Map;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgrSPI;
import com.trilogy.framework.xhome.msp.MSPMessageMgrSPI;
import com.trilogy.framework.xhome.msp.SpidAware;


/**
 * 
 * @author simar.singh@redknee.com
 * @Link (PropertyDynamicLabelMaps) - A calls that can wrap MessageMangerMessage label
 *       keys in Context based on supplied <property, spidLabelFunctions>
 * 
 */
public class PropertySpidDynamicLabelWrapper<BEAN extends AbstractBean & SpidAware>
        extends
            PropertyDynamicLabelWrapper<BEAN>
{

    public PropertySpidDynamicLabelWrapper(Map<PropertyInfo, BeanPropertyLabelFunction<BEAN>> spidPropertyLabelMap)
    {
        super(spidPropertyLabelMap);
    }


    public Context wrap(Context ctx, BEAN spidAwareBean)
    {
        final String spidLabelFrag = "." + spidAwareBean.getSpid();
        for (Map.Entry<PropertyInfo, BeanPropertyLabelFunction<BEAN>> entry : spidPropertyLabelMap_.entrySet())
        {
            final BeanPropertyLabelFunction<BEAN> labelFunction = entry.getValue();
            ctx.put(labelFunction.getBeanLabelKey(ctx, spidAwareBean) + spidLabelFrag, labelFunction.getBeanLabel(ctx,
                    spidAwareBean));
            ctx.put(labelFunction.getColumnLabelKey(ctx, spidAwareBean) + spidLabelFrag, labelFunction.getColumnLabel(ctx,
                    spidAwareBean));
        }
        return wrapMessageManger(ctx);
    }


    private Context wrapMessageManger(Context ctx)
    {
        MessageMgrSPI existingMgr = (MessageMgrSPI) ctx.get(MessageMgrSPI.class);
        ctx.put(MessageMgrSPI.class, new MSPMessageMgrSPI(existingMgr));
        return ctx;
    }
}
