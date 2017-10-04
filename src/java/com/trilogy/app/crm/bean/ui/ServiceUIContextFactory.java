package com.trilogy.app.crm.bean.ui;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.core.Service;

/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class ServiceUIContextFactory implements ContextFactory
{
    public ServiceUIContextFactory(Service service)
    {
        this.service_ = service;
    }


    public Object create(Context fCtx)
    {
        if (bean_ == null)
        {
            try
            {
                bean_ = new ServiceUIAdapter(fCtx).adapt(fCtx, service_);
            }
            catch (HomeException e)
            {
                new DebugLogMsg(this, "Error occurred creating UI version of service: " + e.getMessage(), e).log(fCtx);
            }
        }
        return bean_;
    }
    
    private final Service service_;
    private Object bean_ = null;
}