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
package com.trilogy.app.crm.home;

import java.util.Collection;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.PackageGroup;
import com.trilogy.app.crm.bean.PackageGroupHome;
import com.trilogy.app.crm.bean.PackageGroupXInfo;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.bean.VSATPackage;
import com.trilogy.app.crm.bean.VSATPackageXInfo;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.MissingRequireValueException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

/**
 * Validates the presence of IMSI or MIN depending on the technology.
 *
 * @author rajith.attapattu@redknee.com
 */
public final class PackageValidator implements Validator
{
    private static final PackageValidator INSTANCE = new PackageValidator();

    private PackageValidator()
    {
    }

    /**
     * Returns the singleton instance.
     * @return the singleton instance
     */
    public static PackageValidator instance()
    {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        PropertyInfo groupProperty = null;
        String pkgGroupName = null;
        
        final GenericPackage pack = (GenericPackage) obj;
        
        final short techIndex = pack.getTechnology().getIndex();
        if(techIndex == TechnologyEnum.GSM_INDEX)
        {
            final GSMPackage pkg = (GSMPackage) obj;
            if ((pkg.getIMSI() == null) || (pkg.getIMSI().trim().equals("")))
            {
                cise.thrown(new MissingRequireValueException(GSMPackageXInfo.IMSI));
            }

            groupProperty = GSMPackageXInfo.PACKAGE_GROUP;
            pkgGroupName = pkg.getPackageGroup();
            validatePackageGroup(ctx, pkgGroupName,pack.getTechnology(), pack.getSpid(),cise);
            
        }
        else if (techIndex == TechnologyEnum.TDMA_INDEX
                || techIndex == TechnologyEnum.CDMA_INDEX)
        {
            final TDMAPackage pkg = (TDMAPackage) obj;
            if ((pkg.getMin() == null) || (pkg.getMin().trim().equals("")))
            {
                cise.thrown(new MissingRequireValueException(TDMAPackageXInfo.MIN));
            }

            groupProperty = TDMAPackageXInfo.PACKAGE_GROUP;
            pkgGroupName = pkg.getPackageGroup();
            validatePackageGroup(ctx, pkgGroupName,pack.getTechnology(), pack.getSpid(),cise);
        }
        else if (techIndex == TechnologyEnum.VSAT_PSTN_INDEX)
        {
            final VSATPackage pkg = (VSATPackage) obj;

            groupProperty = VSATPackageXInfo.PACKAGE_GROUP;
            pkgGroupName = pkg.getPackageGroup();
            validatePackageGroup(ctx, pkgGroupName,pack.getTechnology(), pack.getSpid(),cise);
        }
        
        if (groupProperty != null && pkgGroupName != null
                && HomeOperationEnum.CREATE.equals(ctx.get(HomeOperationEnum.class)))
        {
            try
            {
                if (!HomeSupportHelper.get(ctx).hasBeans(
                        ctx, 
                        PackageGroup.class, 
                        new EQ(PackageGroupXInfo.NAME, pkgGroupName)))
                {
                    cise.thrown(new IllegalPropertyArgumentException(groupProperty, "Package group '" + pkgGroupName + "' not found."));
                }
            }
            catch (HomeException e)
            {
                cise.thrown(new IllegalPropertyArgumentException(groupProperty, "Error retrieving package group '" + pkgGroupName + "'."));
            }
        }
        
        cise.throwAll();
    }
    

    private void validatePackageGroup(Context ctx, String pkgGrpName, TechnologyEnum techEnum, int spid,
            CompoundIllegalStateException cise)
    {
        Home pkgGrpHome = (Home) ctx.get(PackageGroupHome.class);
        Collection coll = null;
        try
        {
            And and = new And();
            and.add(new EQ(PackageGroupXInfo.TECHNOLOGY, techEnum));
            and.add(new EQ(PackageGroupXInfo.SPID, spid));
            and.add(new EQ(PackageGroupXInfo.NAME, pkgGrpName));
            coll = pkgGrpHome.where(ctx, and).selectAll(ctx);
            if (coll == null || coll.size() == 0)
            {
                cise.thrown(new IllegalPropertyArgumentException(TDMAPackageXInfo.PACKAGE_GROUP,
                        " Invalid packageGroup " + pkgGrpName));
            }
        }
        catch (UnsupportedOperationException e)
        {
            cise.thrown(new IllegalPropertyArgumentException(TDMAPackageXInfo.PACKAGE_GROUP, " Invalid packageGroup "
                    + pkgGrpName));
        }
        catch (HomeException e)
        {
            cise.thrown(new IllegalPropertyArgumentException(TDMAPackageXInfo.PACKAGE_GROUP, " Invalid packageGroup "
                    + pkgGrpName));
        }
    }
}
