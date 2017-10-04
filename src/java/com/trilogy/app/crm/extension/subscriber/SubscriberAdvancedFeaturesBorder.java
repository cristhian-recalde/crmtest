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
package com.trilogy.app.crm.extension.subscriber;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

/**
 * Border responsible to put the subscriber advanced features object in the context.
 */
public class SubscriberAdvancedFeaturesBorder implements Border
{

    public void service(Context ctx, final HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        ctx = ctx.createSubContext();
        Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
        if (subscriber==null || subscriber.isInFinalState())
        {
            ctx.put("MODE", Integer.valueOf(OutputWebControl.DISPLAY_MODE));
        }
        else
        {
            ctx.put("MODE", Integer.valueOf(OutputWebControl.EDIT_MODE));
        }
        SubscriberAdvancedFeatures bean = subscriber.getAdvancedFeatures();
        bean.setContext(ctx);
        ctx.put(SubscriberAdvancedFeatures.class, bean);
        ExtensionSupportHelper.get(ctx).setParentBean(ctx, bean);
        delegate.service(ctx, req, res);
    }
}
