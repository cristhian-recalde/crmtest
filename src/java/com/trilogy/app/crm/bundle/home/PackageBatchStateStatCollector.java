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
package com.trilogy.app.crm.bundle.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.Package;
import com.trilogy.app.crm.bean.PackageBatchAware;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.PackageStateStats;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.xdb.Count;
import com.trilogy.framework.xhome.xdb.DefaultByGroupXProjection;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.MajorLogMsg;


public class PackageBatchStateStatCollector implements PackageProcessor
{

    public PackageBatchStateStatCollector(String batchId)
    {
        super();
        conPredicate = new EQ(GSMPackageXInfo.BATCH_ID, batchId);
    }


    public PackageBatchStateStatCollector(PackageBatchAware batchAware)
    {
        super();
        conPredicate = new EQ(GSMPackageXInfo.BATCH_ID, batchAware.getBatchId());
    }


    public Map<PackageStateEnum, PackageStateStats> getPacakgeStateSatistics(Context ctx,
            Class<? extends Package> packageBeanClass)
    {
        final Map<PackageStateEnum, PackageStateStats> sateCountMap = getPackageStateCountMapInstance();
        try
        {
            Home home = HomeSupportHelper.get(ctx).getHome(ctx, packageBeanClass);
            final Collection<ArrayList<?>> groupResult;
            {
                groupResult = (Collection<ArrayList<?>>) home.cmd(ctx, new DefaultByGroupXProjection(groupBylist,
                        operationlist, conPredicate));
            }
            for (ArrayList<?> resultList : groupResult)
            {
                long count = ((Number) resultList.get(0)).longValue();
                final PackageStateEnum packageStateEnum;
                {
                    short stateEnumIndex = ((Number) resultList.get(1)).shortValue();
                    packageStateEnum = PackageStateEnum.get(stateEnumIndex);
                }
                final PackageStateStats existingStat = sateCountMap.get(packageStateEnum);
                if (existingStat != null)
                {
                    existingStat.setPackageCount(existingStat.getPackageCount() + count);
                }
                else
                {
                    sateCountMap.put(packageStateEnum,getPackageStateStatsInstance(packageStateEnum));
                }
            }
        }
        catch (Throwable t)
        {
            handleException(ctx, "Error collecting Package State Statistics for Package of type ", t);
        }
        return sateCountMap;
    }


    @Override
    public Object processPackage(Context ctx, GSMPackage gsmPackage) throws PackageProcessingException
    {
        return getPacakgeStateSatistics(ctx, GSMPackage.class);
    }


    @Override
    public Object processPackage(Context ctx, TDMAPackage tdmaPackage) throws PackageProcessingException
    {
        return getPacakgeStateSatistics(ctx, TDMAPackage.class);
    }


    @Override
    public Object processPackage(Context ctx, VSATPackage vsatPackage) throws PackageProcessingException
    {
        return getPacakgeStateSatistics(ctx, VSATPackage.class);
    }


    private void handleException(Context ctx, Throwable t)
    {
        ExceptionListener excl = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (null != excl)
        {
            excl.thrown(t);
        }
        new MajorLogMsg(this, t.getMessage(), t).log(ctx);
    }


    private void handleException(Context ctx, String message, Throwable t)
    {
        handleException(ctx, new IllegalStateException(message, t));
    }

    private final Predicate conPredicate;
    private final static List<PropertyInfo> groupBylist = Arrays.asList(GSMPackageXInfo.STATE);
    private final static List operationlist = Arrays.asList(new Count(GSMPackageXInfo.PACK_ID, null));

    
    /**
     * Utility method to instantiate PackageStateStats with blank statistics
     * @param packageStateEnum
     * @return
     */
    public static PackageStateStats getPackageStateStatsInstance(PackageStateEnum packageStateEnum, long count)
    {
        final PackageStateStats blankStateStats;
        {
            blankStateStats = new PackageStateStats();
            blankStateStats.setPackageCount(count);
            blankStateStats.setPackageState((packageStateEnum));
        }
        return blankStateStats;
    }

    /**
     * Utility method to instantiate PackageStateStats with blank statistics
     * @param packageStateEnum
     * @return
     */
    public static PackageStateStats getPackageStateStatsInstance(PackageStateEnum packageStateEnum)
    {
       return getPackageStateStatsInstance(packageStateEnum, 0);
    }

    /**
     * Utility method to instantiate Map<State, PackageStateStats> with blank statistics
     * @return
     */
    public static Map<PackageStateEnum, PackageStateStats> getPackageStateCountMapInstance()
    {
        final Map<PackageStateEnum, PackageStateStats> blankMap;
        {
            blankMap = new HashMap<PackageStateEnum, PackageStateStats>();
            EnumCollection enumCollection = PackageStateEnum.COLLECTION;
            for (short i = 0; i < enumCollection.size(); i++)
            {
                PackageStateEnum key = (PackageStateEnum) enumCollection.get(i);
                PackageStateStats value = getPackageStateStatsInstance(key);
                blankMap.put(key, value);
            }
        }
        return blankMap;
    }
}
