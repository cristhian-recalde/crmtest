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

import java.util.Calendar;
import java.util.Date;

import com.trilogy.app.crm.bean.SubModificationSchedule;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;


/**
 * @author ankit.nagpal
 * @since 9_9
 * 
 * 
 */
public class CustomSubModificationScheduleGenericEntityAdapter extends AbstractGenericEntityAdapter implements GenericEntityAdapter
{
	
	@Override
    public PropertyAdapter getPropertyAdapter(Context ctx, PropertyInfo property)
    {
        PropertyAdapter propertyAdapter = null;
        if (SubModificationSchedule.class.isAssignableFrom(property.getBeanClass())
                && (property.getSQLName().equals("scheduledTime") || property.getSQLName().equals("createdDate")))
        {
            propertyAdapter = new PropertyAdapter()
            {

                public void adaptQuery(And and, PropertyInfo info, Object value)
                {
                    and.add(new EQ(info, ((Calendar) value).getTime()));
                }


                public void adaptProperty(Context ctx, Object bean, GenericParameter[] genericParameters,
                        PropertyInfo property)
                {
                    try
                    {
                        Date value = null;
                        String paramName = property.getSQLName();
                        for (GenericParameter param : genericParameters)
                        {
                            if (param.getName().equals(paramName))
                            {
                                value = ((Calendar) param.getValue()).getTime();
                                property.set(bean, value);
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
    public Object getRetrieveCriteria(Context ctx, Entity entity, Object object, GenericParameter[] parameters)
            throws EntityParsingException
    {
        return True.instance();
    }
}