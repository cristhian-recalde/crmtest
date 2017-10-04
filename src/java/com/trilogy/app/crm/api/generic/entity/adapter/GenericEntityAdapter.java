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

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;


public interface GenericEntityAdapter
{

	public AbstractBean getBean(Context ctx, Entity entity) throws Exception;
	
    public Object adapt(Context ctx, Entity entity, Object obj) throws EntityParsingException;

    public Object adapt(Context ctx, Entity entity) throws EntityParsingException;

    public Entity unAdapt(Context ctx, Object object) throws EntityParsingException;
    
    public Object getRetrieveCriteria(Context ctx, Entity entity, Object object, GenericParameter[] parameters) throws EntityParsingException;
    
    public PropertyAdapter getPropertyAdapter(Context ctx, PropertyInfo propertyInfo);
    
    public Object adaptQuery(Context ctx, Entity entity) throws EntityParsingException;
}
