/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bean.ui;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

/**
 * Context factory for Charging Template UI
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class ChargingTemplateUIContextFactory implements ContextFactory
{
    public ChargingTemplateUIContextFactory(ChargingTemplate template)
    {
        this.template_ = template;
    }


    public Object create(Context fCtx)
    {
        if (bean_ == null)
        {
            try
            {
                bean_ = new ChargingTemplateUIAdapter(fCtx).adapt(fCtx, template_);
            }
            catch (HomeException e)
            {
                new DebugLogMsg(this, "Error occurred creating UI version of service: " + e.getMessage(), e).log(fCtx);
            }
        }
        return bean_;
    }
    
    private final ChargingTemplate template_;
    private Object bean_ = null;
}