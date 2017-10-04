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
package com.trilogy.app.crm.numbermgn;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.bean.VSATPackageHome;

/**
 * Returns the apropriate Home pipeline reference.
 *
 * @author victor.stratan@redknee.com
 */
public class PackageHomeRetriever implements PackageProcessor
{
    private static PackageHomeRetriever instance_ = new PackageHomeRetriever();

    private PackageHomeRetriever()
    {
    }

    public static PackageHomeRetriever getInstance()
    {
        return instance_;
    }

    public Object processPackage(final Context ctx, final GSMPackage card) throws PackageProcessingException
    {
        return ctx.get(GSMPackageHome.class);
    }

    public Object processPackage(final Context ctx, final TDMAPackage card) throws PackageProcessingException
    {
        return ctx.get(TDMAPackageHome.class);
    }

    @Override
    public Object processPackage(Context ctx, VSATPackage card) throws PackageProcessingException
    {
        // TODO Auto-generated method stub
        return ctx.get(VSATPackageHome.class);
    }
}
