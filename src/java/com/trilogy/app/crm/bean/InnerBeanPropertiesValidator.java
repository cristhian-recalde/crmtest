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
package com.trilogy.app.crm.bean;

import java.util.List;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validatable;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author simar.singh@redknee.com
 * Validates the validatable inner beans of the home-bean.
 * The class goes just into the inner directly inside the home-bean (one-level)
 * It does not validate any beans inside the property beans
 * It does not validate beans inside a Collection Property of a home-bean(having inner-type)
 * 
 */
public class InnerBeanPropertiesValidator implements Validator
{

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        if (obj instanceof AbstractBean)
        {
            final XInfo xinfo = (XInfo) XBeans.getInstanceOf(ctx, obj.getClass(), XInfo.class);
            if (null != xinfo)
            {
                final CompoundIllegalStateException exceptionCompounder = new CompoundIllegalStateException();
                final List<PropertyInfo> propertyList = (List<PropertyInfo>) xinfo.getProperties(ctx);
                for (PropertyInfo property : propertyList)
                {
                    if (Validatable.class.isAssignableFrom(property.getType()))
                    {
                        try
                        {
                            ((Validatable) property.get(obj)).validate(ctx);
                        }
                        catch (Throwable t)
                        {
                            exceptionCompounder.thrown(t);
                        }
                    }
                }
                exceptionCompounder.throwAll();
            }
            else
            {
                throw new IllegalStateException("Facet Manager could not return XInfo for Bean [" + obj.getClass()
                        + "]");
            }
        }
        else
        {
            throw new IllegalStateException("The object of type [" + obj.getClass()
                    + "] is not a bean and hance cannot be validated.");
        }
    }
}
