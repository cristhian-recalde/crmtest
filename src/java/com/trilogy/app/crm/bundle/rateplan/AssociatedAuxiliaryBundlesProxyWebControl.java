package com.trilogy.app.crm.bundle.rateplan;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;

/**
 * Filter the BundleProfileApiHome to select only bundles that are associated with the
 * input rate plans and are postpaid/prepaid and are auxiliary bundles.
 * 
 * @author victor.stratan@redknee.com
 */
public class AssociatedAuxiliaryBundlesProxyWebControl extends ProxyWebControl
{

    public AssociatedAuxiliaryBundlesProxyWebControl(WebControl delegate)
    {
        super(delegate);
    }

    public Context wrapContext(Context ctx)
    {
        final Subscriber subscriber = (Subscriber) ctx.get(AbstractWebControl.BEAN);
        final Context subCtx = ctx.createSubContext();
        if(null != subscriber)
        {
        	LogSupport.minor(ctx, this, "Value of Subscriber ::: "+subscriber);
            Home bundleHome = (Home) subCtx.get(BundleProfileHome.class);
            final SubscriberTypeEnum type = subscriber.getSubscriberType();
            final BundleSegmentEnum segment = type == SubscriberTypeEnum.POSTPAID ? BundleSegmentEnum.POSTPAID : BundleSegmentEnum.PREPAID;
            final EQ condition = new EQ(BundleProfileXInfo.SEGMENT, segment);
            
            //If Pool ID is populated for a subscription then removing group bundles from being displayed on the ratings tab.
            And and = new And();
            if(!subscriber.getPoolID().equals(null) && !"".equals(subscriber.getPoolID()))
            {
              LogSupport.minor(ctx, this, "Value of Subscriber.getPoolID is not null");	
              and.add(new NEQ(BundleProfileXInfo.GROUP_CHARGING_SCHEME, GroupChargingTypeEnum.GROUP_BUNDLE));
            }
            and.add(condition);

            // filter the BundleProfileApiHome to bundles that matches subscriber type
            bundleHome = bundleHome.where(subCtx, and);
            // save the home to the subCtx
            subCtx.put(BundleProfileHome.class, bundleHome);
        }
        else
        {
            LogSupport.minor(ctx, this, "Unable to get Subscriber from AbstractWebControl.");
        }
        
        return subCtx;
    }
}
