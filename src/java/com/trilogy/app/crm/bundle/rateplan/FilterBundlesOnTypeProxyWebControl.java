package com.trilogy.app.crm.bundle.rateplan;

import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;

/**
 * Filter the BundleProfileApiHome to select only bundles that are associated with the
 * input rate plans and are postpaid/prepaid.
 * 
 * @author Candy Wong
 */
public class FilterBundlesOnTypeProxyWebControl extends ProxyWebControl
{
    public static final String CURRENT_SERVICE_PACKAGE_VERSION = "Current Service Package Version";

    public FilterBundlesOnTypeProxyWebControl(WebControl delegate)
    {
        super(delegate);
    }

    public void toWeb(Context ctx, java.io.PrintWriter p1, String p2, Object p3)
    {
        // put the bean currently being edited into the context for
        // manipulation in the wrapContext method
        ctx.put(CURRENT_SERVICE_PACKAGE_VERSION, p3);
        super.toWeb(ctx, p1, p2, p3);
    }

    public Context wrapContext(Context ctx)
    {
        // retrieve the service package version currently being edited
        final ServicePackageVersion ppv = (ServicePackageVersion) ctx.get(CURRENT_SERVICE_PACKAGE_VERSION);

        if (ppv == null)
        {
            return super.wrapContext(ctx);
        }

        final ServicePackage servicePackage = (ServicePackage) ctx.get(AbstractWebControl.BEAN);
        final SubscriberTypeEnum type = servicePackage.getType();

        Context subCtx = ctx.createSubContext();

        Home bundleHome = (Home) subCtx.get(BundleProfileHome.class);

        final BundleSegmentEnum segment;
        if (type.equals(SubscriberTypeEnum.POSTPAID))
        {
            segment = BundleSegmentEnum.POSTPAID;
        }
        else
        {
            segment = BundleSegmentEnum.PREPAID;
        }

        // filter the BundleProfileApiHome to bundles that matches the
        // Service Package type
        bundleHome = bundleHome.where(subCtx, new EQ(BundleProfileXInfo.SEGMENT, segment));

        // save the home to the subCtx
        subCtx.put(BundleProfileHome.class, bundleHome);

        return subCtx;
    }
}
