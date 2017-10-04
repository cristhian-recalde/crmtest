/*
 * Created on Dec 20, 2005 1:56:56 PM
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
package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.home.ServicePackageDeprecateAwareHome;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Wraps the home in a home that hodes the deprecate service packages
 *
 * @author psperneac
 */
public class ServicePackageHomeSetupBorder implements Border
{
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate) throws ServletException, IOException
    {
        Context subCtx=ctx.createSubContext();
        subCtx.put(ServicePackageHome.class,
                new ServicePackageDeprecateAwareHome(ctx,(Home) ctx.get(ServicePackageHome.class)));

        delegate.service(subCtx,req,res);
    }
}
