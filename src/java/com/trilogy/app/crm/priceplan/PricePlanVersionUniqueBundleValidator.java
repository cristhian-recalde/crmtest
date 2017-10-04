package com.trilogy.app.crm.priceplan;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageXInfo;

/**
 * Ensure one bundle is only ones
 * in selected bundles or inside selected packages
 * 
 * @author victor.stratan@redknee.com
 */
public class PricePlanVersionUniqueBundleValidator implements Validator
{
    public void validate(Context ctx, Object obj)
    {
        final PricePlanVersion ppv = (PricePlanVersion) obj;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();

        try
        {
            final Map bundles = ppv.getServicePackageVersion().getBundleFees();
            final Map packages = ppv.getServicePackageVersion().getPackageFees();

            final HashSet dublicates = new HashSet();

            Home home = (Home) ctx.get(ServicePackageHome.class);
            // Set obtained from keySet() is not serializable, so put values in a new set
            home = home.where(ctx, new In(ServicePackageXInfo.ID, new HashSet(packages.keySet())));
            home.forEach(new CollectUniqueBundlesVisitor(bundles, dublicates, el));

            Iterator iter = bundles.keySet().iterator();
            while (iter.hasNext())
            {
                Long key = (Long) iter.next();
                if (dublicates.contains(key))
                {
                    el.thrown(new IllegalPropertyArgumentException(
                            PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION,
                            "BundleID " + key + " is duplicate in selected bundles."));
                }
            }
        } catch (HomeException e)
        {
            el.thrown(new IllegalArgumentException("Cannot access " + ServicePackageHome.class.getName() + " home!"));
            new MajorLogMsg(this, "Cannot access " + ServicePackageHome.class.getName() + " home!", e).log(ctx);
        } finally
        {
            el.throwAll();
        }
    }
}
