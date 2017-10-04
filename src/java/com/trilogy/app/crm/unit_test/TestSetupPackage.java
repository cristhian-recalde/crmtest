package com.trilogy.app.crm.unit_test;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.PackageGroup;
import com.trilogy.app.crm.bean.PackageGroupHome;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

/**
 * TODO: move the installation of the Package homes into this class.
 * 
 * @author ali
 *
 */
public class TestSetupPackage extends ContextAwareTestCase 
{

    public static void createTestPackage(Context ctx, String packageId)
    {
        try
        {
            createTestPackageGroup(ctx);
            
            if (PackageSupportHelper.get(ctx).getGSMPackage(ctx, packageId) == null)
            {
                GSMPackage gsmPackage = new GSMPackage();
                gsmPackage.setPackId(packageId);
                gsmPackage.setSpid(TestSetupAccountHierarchy.SPID_ID);
                gsmPackage.setIMSI(packageId);
                gsmPackage.setPackageGroup(DEFAULT_PACKAGE_GROUP_NAME);
                gsmPackage.setTechnology(TechnologyEnum.GSM);
                gsmPackage.setState(PackageStateEnum.AVAILABLE);
                gsmPackage.setDealer("Dealer1");

                Home pHome = (Home) ctx.get(GSMPackageHome.class);
                pHome.create(gsmPackage);
            }
        }
        catch (HomeException e)
        {
            new DebugLogMsg(TestSetupPackage.class, "Failed to create Test Packages. " + e.getMessage(), e).log(ctx);
        }
    }
    
    public static void createTestPackageGroup(Context ctx)
    {
        Home gHome = (Home) ctx.get(PackageGroupHome.class);
        try
        {
            if (gHome.find(ctx, DEFAULT_PACKAGE_GROUP_NAME) == null)
            {
                PackageGroup group = new PackageGroup();
                group.setName(DEFAULT_PACKAGE_GROUP_NAME);
                group.setSpid(TestSetupAccountHierarchy.SPID_ID);
                group.setTechnology(TechnologyEnum.GSM);

                gHome.create(group);
            }
        }
        catch (HomeException e)
        {
            new DebugLogMsg(TestSetupPackage.class, "Failed to create Test Package Group. " + e.getMessage(), e).log(ctx);
        }
    }
    
    public static final String DEFAULT_PACKAGE_GROUP_NAME = "Unit Test Group"; 
    
}
