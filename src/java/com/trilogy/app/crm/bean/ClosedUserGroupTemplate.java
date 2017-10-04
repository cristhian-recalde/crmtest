package com.trilogy.app.crm.bean;

import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


public class ClosedUserGroupTemplate extends AbstractClosedUserGroupTemplate implements LazyLoadBean
{

   
    private void changeDeprecatedValue(Context context, boolean value) throws HomeException
    {
        Home home = (Home) context
                .get(ClosedUserGroupTemplateHome.class);
        
        if (this.isDeprecated() != value)
        {
            this.setDeprecated(value);
            home.store(context, this);
        }

    }

    public void enable(Context context) throws HomeException
    {
        changeDeprecatedValue(context, false); 
    }

    
    public void disable(Context context) throws HomeException
    {
        changeDeprecatedValue(context, true); 
    }
    
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
                final AuxiliaryService auxiliaryService = CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(
                        ctx, getID());
                if (auxiliaryService != null)
                {
                    super.setSmartSuspension(auxiliaryService.getSmartSuspension());
                    super.setActivationFee(auxiliaryService.getActivationFee());
                    super.setAuxiliaryService(auxiliaryService.getIdentifier());
                    final AdjustmentType adjustment = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, auxiliaryService.getAdjustmentType());
                    if (adjustment != null)
                    {
                        super.setGlCode(adjustment.getGLCodeForSPID(ctx, auxiliaryService.getSpid()));
                    }
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
    
    public String getGlCode(final Context ctx)
    {
        if (ctx!=null && super.getAuxiliaryService()==-1)
        {
            lazyLoadAllProperties(ctx);
        }
        return super.getGlCode();
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
    
    public String getGlCode()
    {
        return getGlCode(getContext());
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
