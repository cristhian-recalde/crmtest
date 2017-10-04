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
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * 
 * @author simar.singh@redknee.com
 * @Link (PropertyDynamicLabelMaps) - A calls that can wrap MessageMangerMessage label
 *       keys in Context based on supplied <property, spidLabelFunctions>
 * 
 */
public class PropertyDynamicLabelWrapper<BEAN extends AbstractBean>
{

    public PropertyDynamicLabelWrapper(Map<PropertyInfo, BeanPropertyLabelFunction<BEAN>> spidPropertyLabelMap)
    {
        spidPropertyLabelMap_ = spidPropertyLabelMap;
    }


    public Context wrap(Context ctx, BEAN bean)
    {
        for (Map.Entry<PropertyInfo, BeanPropertyLabelFunction<BEAN>> entry : spidPropertyLabelMap_.entrySet())
        {
            final BeanPropertyLabelFunction<BEAN> labelFunction = entry.getValue();
            ctx.put(labelFunction.getBeanLabelKey(ctx, bean), labelFunction.getBeanLabel(ctx, bean));
            ctx.put(labelFunction.getColumnLabelKey(ctx, bean), labelFunction.getColumnLabel(ctx, bean));
        }
        return ctx;
    }

    public final Map<PropertyInfo, BeanPropertyLabelFunction<BEAN>> spidPropertyLabelMap_;

    public static interface BeanPropertyLabelFunction<BEAN extends AbstractBean>
    {

        String getLabel(Context ctx, BEAN bean);


        String getKey(Context ctx, BEAN bean);


        String getColumnLabel(Context ctx, BEAN bean);


        String getColumnLabelKey(Context ctx, BEAN bean);


        String getBeanLabel(Context ctx, BEAN bean);


        String getBeanLabelKey(Context ctx, BEAN bean);
    }
    /**
     * 
     * @author ssimar This class is meant to be implemented by Concrete Label Function
     * @param <BEAN>
     */
    public static abstract class AbstractPropertyLabelFunction<BEAN extends AbstractBean>
            implements
                BeanPropertyLabelFunction<BEAN>
    {

        public final PropertyInfo propertyInfo;


        public AbstractPropertyLabelFunction(PropertyInfo pInfo)
        {
            this.propertyInfo = pInfo;
        }


        @Override
        public String getBeanLabel(Context ctx, BEAN bean)
        {
            try
            {
                return getLabel(ctx, bean);
            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Exception while fetching label for Property[ " + propertyInfo.toString() + " ]",
                        t).log(ctx);
                return propertyInfo.getLabel(ctx);
            }
        }


        @Override
        public String getColumnLabel(Context ctx, BEAN bean)
        {
            try
            {
                return getLabel(ctx, bean);
            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Exception while fetching label for Property[ " + propertyInfo.toString() + " ]",
                        t).log(ctx);
                return propertyInfo.getColumnLabel(ctx);
            }
        }


        @Override
        public String getBeanLabelKey(Context ctx, BEAN bean)
        {
            return getKey(ctx, bean) + ".Label";
        }


        @Override
        public String getColumnLabelKey(Context ctx, BEAN bean)
        {
            return getKey(ctx, bean) + ".ColumnLabel";
        }


        @Override
        public String getKey(Context ctx, BEAN bean)
        {
            return propertyInfo.toString();
        }
    }
}
