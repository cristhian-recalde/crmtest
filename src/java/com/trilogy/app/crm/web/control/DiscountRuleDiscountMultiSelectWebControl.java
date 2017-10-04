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

import java.io.PrintWriter;
import java.util.Comparator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.MultiSelectWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceIdentitySupport;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.SubscriptionTypeAware;
import com.trilogy.app.crm.bean.ui.ChargingTemplate;

/**
 * 
 * @author ishan.batra
 * @since  9.9
 *
 *This class is to filter out discount services
 */
public class DiscountRuleDiscountMultiSelectWebControl extends ServiceMultiSelectWebControl
{

    @Override
    public Home getHome(Context ctx)
    {
       final Home originalHome = (Home) ctx.get(ServiceHome.class);
       return filterDiscountServices(ctx, filterSpid(ctx, originalHome));  
    }

    private static Home filterDiscountServices(final Context ctx, final Home originalHome)
    {        
    	final Predicate filter= new EQ(ServiceXInfo.SERVICE_SUB_TYPE, ServiceSubTypeEnum.DISCOUNT);
        return originalHome.where(ctx, filter);
    }    

}
