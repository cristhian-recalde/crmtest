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
package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.SubscriberSupport;

public class SubscriberDeactivationViewBorder implements Border
{
    public void service(Context ctx, final HttpServletRequest req, final HttpServletResponse res,
            final RequestServicer delegate) throws ServletException, IOException
    {
        if (ctx.getInt("MODE", AbstractWebControl.DISPLAY_MODE) == AbstractWebControl.EDIT_MODE)
        {
            Subscriber subscriber = (Subscriber) WebController.getBean(ctx);
            if (subscriber != null)
            {
                try
                {
                    // we have to load the subscriber from the DB
                    subscriber = SubscriberSupport.getSubscriber(ctx, subscriber.getId());
                }
                catch (final HomeException exception)
                {
                    LogSupport.minor(ctx, this, "Unable to retreive subscriber from Home.", exception);
                }

                if (subscriber != null)
                {
                    if (subscriber.isInFinalState()
                            || subscriber.getState() == SubscriberStateEnum.DORMANT)
                    {
                        ctx = ctx.createSubContext();
                        ctx.put("MODE", AbstractWebControl.DISPLAY_MODE);
                    }
                }
            }
        }

        delegate.service(ctx, req, res);
    }
}
