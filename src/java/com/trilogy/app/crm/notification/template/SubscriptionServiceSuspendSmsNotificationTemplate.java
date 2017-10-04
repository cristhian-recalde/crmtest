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
package com.trilogy.app.crm.notification.template;

import java.util.Collections;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bas.recharge.SuspensionSupport;
import com.trilogy.app.crm.delivery.email.RepeatingTemplateTypeEnum;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class SubscriptionServiceSuspendSmsNotificationTemplate extends AbstractSubscriptionServiceSuspendSmsNotificationTemplate
{
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTemplateMessage(Context ctx)
    {
        String result = super.getTemplateMessage(ctx);

        Map services = (Map) ctx.get(RepeatingTemplateTypeEnum.SERVICE, Collections.emptyMap());
        Map packages = (Map) ctx.get(RepeatingTemplateTypeEnum.PACKAGE, Collections.emptyMap());
        Map bundles = (Map) ctx.get(RepeatingTemplateTypeEnum.BUNDLE, Collections.emptyMap());
        Map auxServices = (Map) ctx.get(RepeatingTemplateTypeEnum.AUXILIARY_SERVICE, Collections.emptyMap());

        if (SuspensionSupport.willUnsubscribe(ctx, packages, services, bundles, auxServices))
        {
            result += this.getUnprovisionFooter();
        }
        else
        {
            result += this.getFooter();
        }
        
        return result;
    }
}
