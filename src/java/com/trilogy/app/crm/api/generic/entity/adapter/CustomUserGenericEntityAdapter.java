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

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;


/**
 * @author ankit.nagpal
 * @since 9_8
 * 
 * TT 14072558006 - DP place dealers were getting created through dealer management in the wrong BSS user group.
 * In previous version we had user table and then we changed it with CRMUSer, so the group column was changed to 
 * groupField column in the database. So to map that field correctly we added the adapter
 * 
 * 
 */
public class CustomUserGenericEntityAdapter extends AbstractGenericEntityAdapter implements GenericEntityAdapter
{
	
	@Override
    public PropertyAdapter getPropertyAdapter(Context ctx, PropertyInfo property)
    {
        PropertyAdapter propertyAdapter = null;
        if (User.class.isAssignableFrom(property.getBeanClass())
                && (property.getName().equals("group") && (property.getSQLName().equals("groupField"))))
        {
            propertyAdapter = new PropertyAdapter()
            {

                public void adaptQuery(And and, PropertyInfo info, Object value)
                {
                    and.add(new EQ(info, value));
                }


                public void adaptProperty(Context ctx, Object bean, GenericParameter[] genericParameters,
                        PropertyInfo property)
                {
                    try
                    {
                        String value = null;
                        String paramName = property.getSQLName();
                        for (GenericParameter param : genericParameters)
                        {
                            if (param.getName().equals("group") && paramName.equals("groupField"))
                            {
                                value = (String) param.getValue();
                                property.set(bean, value);
                                break;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                    	LogSupport.minor(ctx,this,"Unable to set the group of the dealer.",e);
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
    public Entity unAdapt(Context ctx, Object obj)
            throws EntityParsingException
    {
        User usr = (User)obj;
        
        /*
         * For now, we make it null, as the extension class may not be present
         * in the class-path of RMI clients.
         */
        usr.setExtension(null);
        return super.unAdapt(ctx, obj);
    }

    @Override
    public Object getRetrieveCriteria(Context ctx, Entity entity, Object object, GenericParameter[] parameters)
            throws EntityParsingException
    {
        return True.instance();
    }
}