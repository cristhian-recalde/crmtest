package com.trilogy.app.crm.bundle.support;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bundle.SubcriberBucketModelBundleManager;
import com.trilogy.app.crm.bundle.SubcriberBucketModelBundleManagerV21;
import com.trilogy.app.crm.support.DefaultBundleSupport;

/**
 * Support class for shared methods
 *
 */
public class CRMBundleSupport extends DefaultBundleSupport
{
    protected static CRMBundleSupport instance_ = null;
    public static CRMBundleSupport instance()
    {
        if (instance_ == null)
        {
            instance_ = new CRMBundleSupport();
        }
        return instance_;
    }

    protected CRMBundleSupport()
    {
    }

    @Override
    public SubcriberBucketModelBundleManager getSubscriberBucketModel(Context ctx)
    {
        SysFeatureCfg bean = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        String handlerName = bean.getBundleManagerBucketHandler().trim();
        SubcriberBucketModelBundleManager handler = null;

        try
        {
            Class bmHandler = Class.forName(handlerName);
            handler = (SubcriberBucketModelBundleManager) bmHandler.newInstance();

        }
        catch (Throwable e)
        {
            LogSupport.major(ctx, this,
                    "Error while getting the bm model handler. Installing the default handler : ",
                    e);
            handler = new SubcriberBucketModelBundleManagerV21();
        }
        return handler;
    }
}

