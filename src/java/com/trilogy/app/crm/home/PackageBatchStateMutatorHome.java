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
package com.trilogy.app.crm.home;

import java.util.Map;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.Package;
import com.trilogy.app.crm.bean.PackageBulkTask;
import com.trilogy.app.crm.bean.PackageBulkTaskXInfo;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.PackageStateStats;
import com.trilogy.app.crm.bean.PackageXInfo;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.bundle.home.PackageBatchStateStatCollector;
import com.trilogy.app.crm.home.PackageBatchState1147ER.Status;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.app.crm.util.DeltaMap;
import com.trilogy.app.crm.util.DeltaMap.ValueFunction;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class PackageBatchStateMutatorHome extends HomeProxy implements PackageProcessor
{

    public PackageBatchStateMutatorHome(Home delegate)
    {
        super(delegate);
    }

    private static final long serialVersionUID = 1L;


    @Override
    public Object store(Context ctx, Object bean) throws HomeException, HomeInternalException
    {
        if (null != bean && !(bean instanceof PackageBulkTask))
        {
            throw new IllegalArgumentException("Expects an instance of [" + PackageBulkTask.class.getName()
                    + "] but got [" + ((bean == null) ? "null" : bean.getClass()) + "]");
        }
        PackageBulkTask task = (PackageBulkTask) bean;
        if (task.isChangeState() && null != task.getBatchId() && !task.getBatchId().isEmpty())
        {
            try
            {
                executeBatchStateChange(ctx, task);
            }
            catch (Throwable t)
            {
                throw new HomeException(t);
            }
        }
        return getDelegate(ctx).store(bean);
    }


    @SuppressWarnings("unchecked")
    private void executeBatchStateChange(Context ctx, PackageBulkTask task) throws PackageProcessingException
    {
        Map<PackageStateEnum, PackageStateStats> initialStats = calculateCurrentStats(ctx, task);
        if(task.getBatchPin().isEmpty() || task.getBatchPin().equals(task.getBatchVerifyPin()))
        {
            CompoundIllegalStateException excl = new CompoundIllegalStateException();
            PackageStateEnum toState = task.getChangeStateTo();
            final String updateDML = getUpdateDML(task.getBatchId(), toState);
            final XDB xdb = (XDB) ctx.get(XDB.class);
            try
            {
                xdb.execute(
                        ctx,
                        "UPDATE "
                                + MultiDbSupportHelper.get(ctx)
                                        .getTableName(
                                                ctx,
                                                (getHome(
                                                        ctx,
                                                        (Class<? extends Package>) processPackage(ctx,
                                                                (GSMPackage) gsmPackage)))) + updateDML);
            }
            catch (Throwable t)
            {
                handleException(ctx, t);
                excl.thrown(t);
            }
            try
            {
                xdb.execute(
                        ctx,
                        "UPDATE "
                                + MultiDbSupportHelper.get(ctx).getTableName(
                                        ctx,
                                        (getHome(
                                                ctx,
                                                (Class<? extends Package>) processPackage(ctx,
                                                        (TDMAPackage) cdmaTdmaPackage)))) + updateDML);
            }
            catch (Throwable t)
            {
                handleException(ctx, t);
                excl.thrown(t);
            }
            try
            {
                xdb.execute(
                        ctx,
                        "UPDATE "
                                + MultiDbSupportHelper.get(ctx).getTableName(
                                        ctx,
                                        (getHome(
                                                ctx,
                                                (Class<? extends Package>) processPackage(ctx,
                                                        (VSATPackage) vsatPackage)))) + updateDML);
            }
            catch (Throwable t)
            {
                handleException(ctx, t);
                excl.thrown(t);
            }
            Map<PackageStateEnum, PackageStateStats> finalStats = calculateCurrentStats(ctx, task);
            if (excl.getSize() > 0)
            {
                new PackageBatchState1147ER(initialStats, task, finalStats).fail(ctx, true, Status.PARTIAL_COMPLETE);
            }
            else
            {
                new PackageBatchState1147ER(initialStats, task, finalStats).success(ctx);
            }
        } else
        {
            handleException(ctx, new IllegalStateException("State Change Operation Aborted. PIN Verification failed for Batch ID [" + task.getBatchId() + "]"));
            new PackageBatchState1147ER(initialStats, task, initialStats).fail(ctx, false, Status.ABORTED);
        }
    }


    private Home getHome(Context ctx, Class<? extends Package> packageClass) throws HomeException
    {
        return HomeSupportHelper.get(ctx).getHome(ctx, (Class<? extends Package>) packageClass);
    }


    /**
     * Generate a DML that work on a Package Table to set state of all packages belonging
     * to a given batch which are not in In-Use or Final (Stolen, Damaged) states.
     * 
     * @param batchId
     * @param toState
     * @return
     */
    private String getUpdateDML(String batchId, PackageStateEnum toState)
    {
        return " set " + PackageXInfo.STATE.getSQLName() + " = " + toState.getIndex() + " where "
                + PackageBulkTaskXInfo.BATCH_ID.getSQLName() + "= '" + batchId + "' and "
                + PackageXInfo.STATE.getSQLName() + " <> " + PackageStateEnum.IN_USE_INDEX + " and "
                + PackageXInfo.STATE.getSQLName() + " <> " + PackageStateEnum.STOLEN_INDEX + " and "
                + PackageXInfo.STATE.getSQLName() + " <> " + PackageStateEnum.DAMAGED_INDEX + "";
    }


    /**
     * Returns the GSM pacakge's Home that needs to be processed for state change
     */
    @Override
    public Object processPackage(Context ctx, GSMPackage arg1) throws PackageProcessingException
    {
        return GSMPackage.class;
    }


    /**
     * Returns the CDMA-TDMA pacakge's Home that needs to be processed for state change
     */
    @Override
    public Object processPackage(Context ctx, TDMAPackage arg1) throws PackageProcessingException
    {
        return TDMAPackage.class;
    }


    /**
     * Returns the VSAT pacakge's Home that needs to be processed for state change
     */
    @Override
    public Object processPackage(Context ctx, VSATPackage arg1) throws PackageProcessingException
    {
        return VSATPackage.class;
    }


    @SuppressWarnings("unchecked")
    private Map<PackageStateEnum, PackageStateStats> calculateCurrentStats(Context ctx, PackageBulkTask task)
            throws PackageProcessingException
    {
        final Map<PackageStateEnum, PackageStateStats> totalStats;
        {
            final PackageBatchStateStatCollector collector = new PackageBatchStateStatCollector(task);
            Map<PackageStateEnum, PackageStateStats> gsmStats;
            {
                gsmStats = collector.getPacakgeStateSatistics(ctx,
                        (Class<? extends Package>) processPackage(ctx, (GSMPackage) gsmPackage));
            }
            Map<PackageStateEnum, PackageStateStats> cdmaStats;
            {
                cdmaStats = collector.getPacakgeStateSatistics(ctx,
                        (Class<? extends Package>) processPackage(ctx, (TDMAPackage) cdmaTdmaPackage));
            }
            Map<PackageStateEnum, PackageStateStats> vsatStats;
            {
                vsatStats = collector.getPacakgeStateSatistics(ctx,
                        (Class<? extends Package>) processPackage(ctx, (VSATPackage) vsatPackage));
            }
            totalStats = new DeltaMap<PackageStateEnum, PackageStateStats>(
                    new DeltaMap<PackageStateEnum, PackageStateStats>(gsmStats, cdmaStats)
                            .unionOperation(FUNCTION_ADD_VALUE),
                    vsatStats).unionOperation(FUNCTION_ADD_VALUE);
        }
        return totalStats;
    }

    private static final ValueFunction<PackageStateStats> FUNCTION_ADD_VALUE = new ValueFunction<PackageStateStats>()
    {

        @Override
        public PackageStateStats function(PackageStateStats first, PackageStateStats second)
        {
            final PackageStateStats packageStateStats;
            {
                packageStateStats = new PackageStateStats();
                packageStateStats.setPackageCount(first.getPackageCount() + second.getPackageCount());
                packageStateStats.setPackageState(first.getPackageState());
            }
            return packageStateStats;
        }
    };


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

    private final GSMPackage gsmPackage = new GSMPackage();
    private final TDMAPackage cdmaTdmaPackage = new TDMAPackage();
    private final VSATPackage vsatPackage = new VSATPackage();
}
