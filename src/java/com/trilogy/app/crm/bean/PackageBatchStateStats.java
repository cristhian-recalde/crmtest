/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bean;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.app.crm.bundle.home.PackageBatchStateStatCollector;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;


/**
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class PackageBatchStateStats extends AbstractPackageBatchStateStats implements PackageProcessor
{

    private static final long serialVersionUID = 1L;


    public PackageBatchStateStats()
    {
    }


    public PackageBatchStateStats(PackageBatchAware batchAware)
    {
        setBatchId(batchAware.getBatchId());
    }

    public Map<PackageStateEnum, PackageStateStats> getGsmPackageBatchStateStats(Context ctx)
    {
        return new PackageBatchStateStatCollector(this).getPacakgeStateSatistics(ctx, GSMPackage.class);
    }


    public Map<PackageStateEnum, PackageStateStats> getCdmaPackageBatchStateStats(Context ctx)
    {
        return new PackageBatchStateStatCollector(this).getPacakgeStateSatistics(ctx, TDMAPackage.class);
    }


    public Map<PackageStateEnum, PackageStateStats> getVsatPackageBatchStateStats(Context ctx)
    {
        return new PackageBatchStateStatCollector(this).getPacakgeStateSatistics(ctx, VSATPackage.class);
    }


    @Override
    public Map<PackageStateEnum, PackageStateStats> getGsmPackageBatchStateStats()
    {
        final Context ctx = ContextLocator.locate();
        if (null != ctx)
        {
            return getGsmPackageBatchStateStats(ctx);
        }
        {
            return new HashMap<PackageStateEnum, PackageStateStats>();
        }
    }


    @Override
    public Map<PackageStateEnum, PackageStateStats> getCdmaPackageBatchStateStats()
    {
        final Context ctx = ContextLocator.locate();
        if (null != ctx)
        {
            return getCdmaPackageBatchStateStats(ctx);
        }
        {
            return new HashMap<PackageStateEnum, PackageStateStats>();
        }
    }


    @Override
    public Map<PackageStateEnum, PackageStateStats> getVsatPackageBatchStateStats()
    {
        final Context ctx = ContextLocator.locate();
        if (null != ctx)
        {
            return getVsatPackageBatchStateStats(ctx);
        }
        {
            return new HashMap<PackageStateEnum, PackageStateStats>();
        }
    }

    @Override
    public Object processPackage(Context ctx, GSMPackage arg1) throws PackageProcessingException
    {
        return getGsmPackageBatchStateStats(ctx);
    }


    @Override
    public Object processPackage(Context ctx, TDMAPackage arg1) throws PackageProcessingException
    {
        return getCdmaPackageBatchStateStats(ctx);
    }


    @Override
    public Object processPackage(Context ctx, VSATPackage arg1) throws PackageProcessingException
    {
        return getVsatPackageBatchStateStats(ctx);
    }

}
