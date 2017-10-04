package com.trilogy.app.crm.bean;

import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;


public class BirthdayPlan extends AbstractBirthdayPlan implements LazyLoadBean
{
    
    public boolean lazyLoad(Context ctx, PropertyInfo property)
    {
        if (property != null)
        {
            if (property.getBeanClass().isAssignableFrom(this.getClass()))
            {
                PMLogMsg pm =
                    new PMLogMsg(LazyLoadBean.class.getName(), this.getClass()
                        .getSimpleName()
                        + ".lazyLoad("
                        + property.getName()
                        + ")");
                lazyLoadAllProperties(ctx);
                pm.log(ctx);
            }
        }
        return false;
    }

    
    public boolean lazyLoadAllProperties(Context ctx)
    {
        boolean result = false;
        if (getID()>0)
        {
            try
            {
                final AuxiliaryService auxiliaryService = CallingGroupSupport.getAuxiliaryServiceForBirthdayPlan(
                        ctx, getID());
                if (auxiliaryService != null)
                {
                    super.setSmartSuspension(auxiliaryService.getSmartSuspension());
                    super.setActivationFee(auxiliaryService.getActivationFee());
                    super.setAuxiliaryService(auxiliaryService.getIdentifier());
                    result = true;
                }
            }
            catch (Exception e)
            {
                LogSupport.major(ctx, FFClosedUserGroupSupport.class, 
                        "Unable to retrieve Auxilliary service for CUG " + getID(), e);
            }
        }
        return result;
    }
    
    public boolean getSmartSuspension(final Context ctx)
    {
        if (ctx!=null && super.getAuxiliaryService()==-1)
        {
            lazyLoadAllProperties(ctx);
        }
        return super.getSmartSuspension();
    }
    
    public ActivationFeeModeEnum getActivationFee(final Context ctx)
    {
        if (ctx!=null && super.getAuxiliaryService()==-1)
        {
            lazyLoadAllProperties(ctx);
        }
        return super.getActivationFee();
    }
    
    public long getAuxiliaryService(final Context ctx)
    {
        if (ctx!=null && super.getAuxiliaryService()==-1)
        {
            lazyLoadAllProperties(ctx);
        }
        return super.getAuxiliaryService();
    }

    public boolean getSmartSuspension()
    {
        return getSmartSuspension(getContext());
    }
    
    public ActivationFeeModeEnum getActivationFee()
    {
        return getActivationFee(getContext());
    }
    
    public long getAuxiliaryService()
    {
        return getAuxiliaryService(getContext());
    }

    public Context getContext()
    {
        return ctx_;
    }

    
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }

    private Context ctx_;

}
