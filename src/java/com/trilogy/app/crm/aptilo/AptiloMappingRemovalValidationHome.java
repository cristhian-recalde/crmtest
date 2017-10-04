/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.aptilo;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
//import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.CountVisitor;
import com.trilogy.framework.xhome.visitor.CountingVisitor;
import com.trilogy.service.aptilo.model.ServiceParametersMapping;

/**
 * Validates that no service is using ServiceParametersMapping
 * upon deletion.
 * 
 * @author asim.mahmood@redknee.com
 *
 */
public class AptiloMappingRemovalValidationHome extends HomeProxy
{

    public AptiloMappingRemovalValidationHome(Context ctx, Home home)
    {
        super(ctx, home);
    }

    @Override
    public void remove(Context ctx, Object obj) throws HomeException
    {
        ServiceParametersMapping mapping = (ServiceParametersMapping)obj;
        
        if (mapping == null)
        {
            throw new HomeException("Cannot remove a 'null' mapping.");
        }

        
       /* boolean exists  = HomeSupport.hasBeans(ctx, Service.class, new And()
                .add(new EQ(ServiceXInfo.TYPE, ServiceTypeEnum.WIMAX))
                .add(new EQ(ServiceXInfo.WIMAX_SERVICE, mapping.getServiceName())));
       
        if (exists) 
        {
            throw new HomeException("Cannot delete mapping [" + mapping.getServiceName() + "], its in use by a service.");
        }*/
        
        super.remove(ctx, obj);
    }
}
