package com.trilogy.app.crm.bundle.web;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleAdjustmentItem;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.LongWebControl;
import com.trilogy.framework.xlog.log.LogSupport;


public class BundleAdjustmentMapUnitWebControl extends MapUnitWebControl
{
    private LongWebControl webControl_;
    public BundleAdjustmentMapUnitWebControl(int width)
    {
       super(width);
       webControl_ = new LongWebControl(width);
    }

    public void toWeb(Context ctx, java.io.PrintWriter out, String name, Object obj)
    {
        BundleAdjustmentItem item = (BundleAdjustmentItem) ctx.get(AbstractWebControl.BEAN);
        BundleProfile bundle = null;
        try
        {
            if (item.getBundleProfile()>0)
            {
                bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, item.getBundleProfile());
            }
        }
        catch (Throwable t)
        {
            LogSupport.minor(ctx, this, "Unable to retrieve bundle profile " + item.getBundleProfile() + ": " + t.getMessage(), t);
        }
        
        if (bundle!=null)
        {
            Context subContext = ctx.createSubContext();
            subContext.put(AbstractWebControl.BEAN, bundle);
            super.toWeb(subContext, out, name, obj);
        }
        else
        {
            webControl_.toWeb(ctx, out, name, obj);
        }
    }

    public Object fromWeb(Context ctx, javax.servlet.ServletRequest req, String name)
    {
        BundleAdjustmentItem item = (BundleAdjustmentItem) ctx.get(AbstractWebControl.BEAN);
        BundleProfile bundle = null;
        
        try
        {
            if (item.getBundleProfile()>0)
            {
                bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, item.getBundleProfile());
            }
        }
        catch (Throwable t)
        {
            LogSupport.minor(ctx, this, "Unable to retrieve bundle profile " + item.getBundleProfile() + ": " + t.getMessage(), t);
        }

        if (bundle!=null)
        {
            Context subContext = ctx.createSubContext();
            subContext.put(AbstractWebControl.BEAN, bundle);
            return super.fromWeb(subContext, req, name);
        }
        else
        {
            return webControl_.fromWeb(ctx, req, name);
        }
    }    
}
