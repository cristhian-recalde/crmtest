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

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.Package;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.core.TDMAPackage;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.AbstractXInfo;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;

public class CustomPackageGenericEntityAdapter extends AbstractGenericEntityAdapter
implements GenericEntityAdapter
{
    @Override
    public AbstractBean getBean(Context ctx, Entity entity) throws Exception
    {
        Package bean =  (Package) super.getBean(ctx, entity);
        short technology = 0;
        
        GenericParameter[] params = entity.getProperty();
        if(params!= null)
        {
        	for(GenericParameter param : params)
        	{
        		if(param.getName().equals("technology"))
        		{
        			bean.setTechnology(TechnologyEnum.get((Short) param.getValue()));
        			technology = (Short) param.getValue();
        		}
        	}
        }
        
        switch(technology)
        {
        case(TechnologyEnum.CDMA_INDEX):
        	return  XBeans.instantiate(TDMAPackage.class, ctx);

        default :
        	throw new Exception("This technology type is not supported.");
        }
    }
    
    
	@Override
	public Object adapt(Context ctx, Entity entity, Object obj)
			throws EntityParsingException {

		return super.adapt(ctx, entity, obj);

	}
	
	public Entity unAdapt(Context ctx, Object obj) throws EntityParsingException
    {
        Entity entity = new Entity();
        String type = obj.getClass().getName();
        entity.setType(type);
        String typeXInfo = "com.redknee.app.crm.bean.TDMAPackageXInfo";
        try
        {
            AbstractXInfo xInfo = (AbstractXInfo) XBeans.instantiate(typeXInfo, ctx);
            List<GenericParameter> paramList = new ArrayList<GenericParameter>();
            AbstractBean bean = (AbstractBean) obj;
            List<PropertyInfo> propertieslist = xInfo.getProperties(ctx);
            for (PropertyInfo property : propertieslist)
            {
            	if(property.getName().equals( "technology"))
            	{
            		paramList.add(createGenericParameter(property.getName(), ((TechnologyEnum)property.get(bean)).getIndex()));
            	}
            	else if(property.getName() .equals( "state"))
            	{
            		paramList.add(createGenericParameter(property.getName(), ((PackageStateEnum)property.get(bean)).getIndex()));
            	}
            	else
            	{
            		paramList.add(createGenericParameter(property.getName(), property.get(bean)));
            	}
            }
            entity.setProperty(paramList.toArray(new GenericParameter[paramList.size()]));
        }
        catch (ClassNotFoundException e)
        {
            throw new EntityParsingException("XInfo class not found " + typeXInfo, e);
        }
        return entity;
    }

	@Override
	public Object getRetrieveCriteria(Context ctx, Entity entity,
			Object object, GenericParameter[] parameters)
			throws EntityParsingException {
		return True.instance();
	}
}
