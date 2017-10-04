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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * This class triggers the logic to set mandatory SCT fields are set to READ_ONLY mode.
 *
 * @author ling.tang@redknee.com
 */
public class SubscriberSatWebControl extends ProxyWebControl
{
    public SubscriberSatWebControl(final WebControl delegate)
    {
        super(delegate);
    }

    /*
     * Apply SAT, make sure SAT value is on the top of subscriber profile.
     *
     * (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#toWeb(com.redknee.framework.xhome.context.Context,
     *     java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        if (obj instanceof Subscriber)
        {
            final Subscriber sub = (Subscriber) obj;
            if (sub.getSatId() != Subscriber.DEFAULT_SATID)
            {
                SubscriberSupport.applyServiceActivationTemplateMandatoryMode(ctx, sub.getSatId());                
            }
        }

        super.toWeb(ctx, out, name, obj);
    }
}
