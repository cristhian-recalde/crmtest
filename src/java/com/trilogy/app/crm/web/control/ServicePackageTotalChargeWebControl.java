/*
 * Created on Dec 14, 2005 3:58:53 PM
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.core.ServicePackage;

/**
 * @author psperneac
 */
public class ServicePackageTotalChargeWebControl extends ProxyWebControl
{
    public ServicePackageTotalChargeWebControl(WebControl delegate)
    {
        super(delegate);
    }

    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        ServicePackage pack=(ServicePackage) obj;

        pack.updateTotalCharge(ctx);

        super.toWeb(ctx, out, name, obj);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
