package com.trilogy.app.crm.bundle.rateplan;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Filter the BundleProfileApiHome to select only bundles that are associated with the
 * input rate plans and are postpaid/prepaid.
 * 
 * @author Candy Wong
 */
public class AssociatedBundlesProxyWebControl extends ProxyWebControl
{
    public static final String CURRENT_PRICE_PLAN_VERSION = "Current Price Plan Version";

    public AssociatedBundlesProxyWebControl(WebControl delegate)
    {
        super(delegate);
    }

    public void toWeb(Context ctx, java.io.PrintWriter p1, String p2, Object p3)
    {
        // put the bean currently being edited into the context for
        // manipulation in the wrapContext method
        ctx.put(CURRENT_PRICE_PLAN_VERSION, p3);
        super.toWeb(ctx, p1, p2, p3);
    }

    public Context wrapContext(Context ctx)
    {
        // retrieve the price plan version currently being edited
        final PricePlanVersion ppv = (PricePlanVersion) ctx.get(CURRENT_PRICE_PLAN_VERSION);

        if (ppv == null)
        {
            return super.wrapContext(ctx);
        }

        final PricePlan pricePlan = (PricePlan) ctx.get(AbstractWebControl.BEAN);
        final SubscriberTypeEnum type = pricePlan.getPricePlanType();
        Context context = null;
        try
        {
            context = SubscriberBundleSupport.filterBundlesOnPricePlan(ctx, pricePlan, type, false);            
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this,"Exception while filtering the Bundles on Price Plan",e).log(ctx);
        }
        return context;
    }
}
