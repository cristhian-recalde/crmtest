/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.PackageGroup;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.bean.VSATPackageXInfo;
import com.trilogy.app.crm.numbermgn.PackageProcessingException;
import com.trilogy.app.crm.numbermgn.PackageProcessor;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Limitations: 1) CSR is only able to 'modify' existing UIM Groups if the group is not in
 * use by Package IDs that are in the 'Available' state. 2) CSR cannot delete UIM Groups
 * that belong to Package IDs in the 'Available' state.
 * 
 * @author vincci.cheng@redknee.com
 * @author simar.singh@redknee.com
 * 
 * This class has been made to implements PackageProcessor to enforce compile time adherence for mandating processing of all concrete Package Types 
 */
public class PackageGroupHomeProxy extends HomeProxy implements PackageProcessor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public PackageGroupHomeProxy(Context ctx, Home delegate)
    {
        super(delegate);
        setContext(ctx);
    }


    public Object store(Context ctx, Object obj) throws HomeException
    {
        failIfPackagesExist(ctx,(PackageGroup)obj);
        return super.store(ctx, obj);
        
    }


    public void remove(Context ctx, Object obj) throws HomeException
    {
        failIfPackagesExist(ctx,(PackageGroup)obj);
        super.remove(ctx, obj);
    }
    
    /*
    * This class has been made to implements PackageProcessor to enforce compile time adherence for mandating processing of all concrete Package Types 
    */
    @Override
    public Object processPackage(Context ctx, GSMPackage gsmPackage) throws PackageProcessingException
    {
        if (null != gsmPackage)
        {
            throw new PackageProcessingException(
                    "GSM Packages exist for the Package Group. The record cannot be manipulated");
        }
        else
        {
            return Boolean.TRUE;
        }
    }

    /*
     * This class has been made to implements PackageProcessor to enforce compile time adherence for mandating processing of all concrete Package Types 
     */
    @Override
    public Object processPackage(Context arg0, TDMAPackage tdmaPackage) throws PackageProcessingException
    {
        if (null != tdmaPackage)
        {
            throw new PackageProcessingException(
                    "TDMA Packages exist for the Package Group. The record cannot be manipulated");
        }
        else
        {
            return Boolean.TRUE;
        }
    }

    /*
     * This class has been made to implements PackageProcessor to enforce compile time adherence for mandating processing of all concrete Package Types 
     */
    @Override
    public Object processPackage(Context arg0, VSATPackage vsatPackage) throws PackageProcessingException
    {
        if (null != vsatPackage)
        {
            throw new PackageProcessingException(
                    "VSAT Packages exist for the Package Group. The record cannot be manipulated");
        }
        else
        {
            return Boolean.TRUE;
        }
    }
    
    /**
     * 
     * @param ctx
     * @param group
     * @throws HomeException - If there is an error or if there are packages that exist for the Group
     */
    private void failIfPackagesExist(Context ctx, PackageGroup group) throws HomeException
    {
        try
        {
            if (TechnologyEnum.GSM == group.getTechnology())
            {
                processPackage(ctx, gsmPacakgeExists(ctx, group));
            }
            else if (TechnologyEnum.TDMA == group.getTechnology() || TechnologyEnum.CDMA == group.getTechnology())
            {
                processPackage(ctx, tdmaPackagesExist(ctx, group));
            }
            else if (TechnologyEnum.VSAT_PSTN == group.getTechnology())
            {
                processPackage(ctx, vsatPackageExists(ctx, group));
            }
        }
        catch (Throwable t)
        {
            if (t instanceof HomeException)
            {
                throw (HomeException) t;
            }
            else
            {
                throw new HomeException(t.getMessage(), t);
            }
        }
    }


    /**
     * Indicates whether or not there are any available packages in the given group.
     * 
     * @param group
     *            The group in which to look for available packages.
     * 
     * @return True if there are any available packages in the given group; false
     *         otherwise.
     * 
     * @exception HomeException
     *                Thrown if there is a problem accessing Home data.
     */
    private TDMAPackage tdmaPackagesExist(Context ctx, final PackageGroup group) throws HomeException
    {
        return (HomeSupportHelper.get(ctx)).findBean(ctx, TDMAPackage.class, new EQ(
                TDMAPackageXInfo.PACKAGE_GROUP, group.getName()));
    }


    private GSMPackage gsmPacakgeExists(Context ctx, final PackageGroup group) throws HomeException
    {
        return (HomeSupportHelper.get(ctx)).findBean(ctx, GSMPackage.class, new EQ(
                GSMPackageXInfo.PACKAGE_GROUP, group.getName()));
    }


    private VSATPackage vsatPackageExists(Context ctx, final PackageGroup group) throws HomeException
    {
        return (HomeSupportHelper.get(ctx)).findBean(ctx, VSATPackage.class, new EQ(
                VSATPackageXInfo.PACKAGE_GROUP, group.getName()));
    }

}
