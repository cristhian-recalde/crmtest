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

import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;


/**
 * Multi select web control used to display services filtering transfer services.
 * @author Marcio Marques
 * @since 8.6
 * 
 */
public class ChargingTemplateServiceMultiSelectWebControl extends ServiceMultiSelectWebControl
{
    public ChargingTemplateServiceMultiSelectWebControl()
    {
        super();
    }

    @Override
    public Home getHome(Context ctx)
    {
        return filterTransfer(ctx, super.getHome(ctx));
    }
    
    private Home filterTransfer(Context ctx, Home home)
    {
        Not filter = new Not(new EQ(ServiceXInfo.TYPE, ServiceTypeEnum.TRANSFER));
        return home.where(ctx, filter);
    }
    
  
}
