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

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.PackageBulkTask;
import com.trilogy.app.crm.bean.PackageXInfo;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.bean.VSATPackageHome;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class PackageBatchDependencyCheckHome extends HomeProxy implements PackageProcessor
{

    public PackageBatchDependencyCheckHome(Home delegate)
    {
        super(delegate);
    }

    private static final long serialVersionUID = 1L;


    /**
     * Allow removal only if no packages that reference the batch exist
     */
    @Override
    public void remove(Context ctx, Object bean) throws HomeException, HomeInternalException
    {
        if (null != bean && !(bean instanceof PackageBulkTask))
        {
            throw new IllegalArgumentException("Expects an instance of [" + PackageBulkTask.class.getName()
                    + "] but got [" + ((bean == null) ? "null" : bean.getClass()) + "]");
        }
        PackageBulkTask task = (PackageBulkTask) bean;
        final String batchId = task.getBatchId();
        try
        {
            if (null != batchId && !batchId.isEmpty())
            {
                {
                    long gsmPackageCount = HomeSupportHelper.get(ctx).getBeanCount(ctx,
                            (Home) processPackage(ctx, gsmPackage), new EQ(PackageXInfo.BATCH_ID, batchId));
                    if (gsmPackageCount > 0)
                    {
                        throw (new HomeException("The batch [" + batchId + "] cannot be removed as ["
                                + gsmPackageCount + "] GSM Packages still still exist under it."));
                    }
                }
                {
                    long cdmaPackageCount = HomeSupportHelper.get(ctx).getBeanCount(ctx,
                            (Home) processPackage(ctx, cdmaTdmaPackage), new EQ(PackageXInfo.BATCH_ID, batchId));
                    if (cdmaPackageCount > 0)
                    {
                        throw (new HomeException("The batch [" + batchId + "] cannot be removed as ["
                                + cdmaPackageCount + "] CDMA-TDMA Packages still still exist under it."));
                    }
                }
                {
                    long vsatPackageCount = HomeSupportHelper.get(ctx).getBeanCount(ctx,
                            (Home) processPackage(ctx, vsatPackage), new EQ(PackageXInfo.BATCH_ID, batchId));
                    if (vsatPackageCount > 0)
                    {
                        throw (new HomeException("The batch with ID [" + batchId + "] cannot be removed as ["
                                + vsatPackageCount + "] VSAT Packages still still exist under it."));
                    }
                }
            }
            getDelegate().remove(ctx, bean);
        }
        catch (Throwable t)
        {
            if (t instanceof HomeException)
            {
                throw (HomeException) t;
            }
            else
            {
                throw new HomeException("Error in checking Dependencies of the Package Batch [" + task.getBatchId()
                        + "] ", t);
            }
        }
    }


    /**
     * Returns the GSM pacakge's Home that needs to be processed for state change
     */
    @Override
    public Object processPackage(Context ctx, GSMPackage arg1) throws PackageProcessingException
    {
        return (Home) ctx.get(GSMPackageHome.class);
    }


    /**
     * Returns the CDMA-TDMA pacakge's Home that needs to be processed for state change
     */
    @Override
    public Object processPackage(Context ctx, TDMAPackage arg1) throws PackageProcessingException
    {
        return (Home) ctx.get(TDMAPackageHome.class);
    }


    /**
     * Returns the VSAT pacakge's Home that needs to be processed for state change
     */
    @Override
    public Object processPackage(Context ctx, VSATPackage arg1) throws PackageProcessingException
    {
        return (Home) ctx.get(VSATPackageHome.class);
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

    private final GSMPackage gsmPackage = new GSMPackage();
    private final TDMAPackage cdmaTdmaPackage = new TDMAPackage();
    private final VSATPackage vsatPackage = new VSATPackage();
}
