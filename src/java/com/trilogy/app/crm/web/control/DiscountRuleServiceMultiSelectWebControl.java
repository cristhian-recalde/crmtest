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
package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;

/**
 * 
 * @author ishan.batra
 * @since  9.9
 *
 * This class is to filter out all the services excluding discount services and all other sub types
 */
public class DiscountRuleServiceMultiSelectWebControl extends ServiceMultiSelectWebControl
{

    @Override
    public Home getHome(Context ctx)
    {
       final Home originalHome = (Home) ctx.get(ServiceHome.class);
       return filterServices(ctx, filterSpid(ctx, originalHome));  
    }

    private static Home filterServices(final Context ctx, final Home originalHome)
    {        
    	final Predicate filter= new EQ(ServiceXInfo.SERVICE_SUB_TYPE, ServiceSubTypeEnum.NA);
        return originalHome.where(ctx, filter);
    }    

}
