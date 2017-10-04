/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.api.generic.entity.adapter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;
import com.trilogy.app.crm.api.generic.entity.adapter.PropertyAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.AbstractGenericEntityAdapter.EnumClassAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.AbstractGenericEntityAdapter.PremitiveClassAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.AbstractGenericEntityAdapter.SimpleBeanAdapter;


/**
 * @author alok.sohani
 * @since 9_5_4
 *        Custom Msisdn Generic entity adapter to convert subscriberType=PREPAID
 *        from DCRM to subscriberType = POSTPAID in BSS & subscriberType=POSTPAID from
 *        DCRM to subscriberType = PREPAID in BSS and vice versa.
 * 
 */
public class CustomMsisdnGenericEntityAdapter extends AbstractGenericEntityAdapter implements GenericEntityAdapter
{

    @Override
    public PropertyAdapter getPropertyAdapter(Context ctx, PropertyInfo property)
    {
        PropertyAdapter propertyAdapter = null;
        if (Msisdn.class.isAssignableFrom(property.getBeanClass())
                && AbstractEnum.class.isAssignableFrom(property.getType())
                && property.getSQLName().equals("subscriberType"))
        {
            propertyAdapter = new PropertyAdapter()
            {

                public void adaptQuery(And and, PropertyInfo info, Object value)
                {
                    and.add(new EQ(info, new Short(String.valueOf(value))));
                }


                public void adaptProperty(Context ctx, Object bean, GenericParameter[] genericParameters,
                        PropertyInfo property)
                {
                    try
                    {
                        Method method;
                        method = property.getType().getMethod("get", new Class[]
                            {Short.TYPE});
                        Short value = null;
                        String paramName = property.getSQLName();
                        for (GenericParameter param : genericParameters)
                        {
                            if (param.getName().equals(paramName))
                            {
                                value = (Short) param.getValue();
                                switch (value)
                                {
                                case 0:
                                    value = 1;
                                    break;
                                case 1:
                                    value = 0;
                                    break;
                                case 2:
                                    value = 2;
                                    break;
                                }
                                property.set(bean, method.invoke(null, value));
                                break;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
            };
        }
        else
        {
            return super.getPropertyAdapter(ctx, property);
        }
        return propertyAdapter;
    }


    @Override
    protected GenericParameter createGenericParameter(String name, Object value)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(name);
        if (name.equals("subscriberType"))
        {
            Short shortValue = new Short(String.valueOf(value));
            switch (shortValue)
            {
            case 0:
                value = 1;
                break;
            case 1:
                value = 0;
                break;
            case 2:
                value = 2;
                break;
            }
        }
        parameter.setValue(value);
        return parameter;
    }


    @Override
    public Object getRetrieveCriteria(Context ctx, Entity entity, Object object, GenericParameter[] parameters)
            throws EntityParsingException
    {
        // TODO Auto-generated method stub
        return True.instance();
    }
}