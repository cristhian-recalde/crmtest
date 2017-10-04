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
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.home.sub.CreditLimitAdjustmentCreationHome;
import com.trilogy.app.crm.home.sub.CreditLimitPermissionValidator;

/**
 * Add Credit Limit Adjustment-related decorators to subscriber home pipeline.
 *
 * @author cindy.wong@redknee.com
 */
public class CreditLimitAdjustmentBorder implements Border
{

    /**
     * Adds Credit Limit Adjustment-related decorators to the subscriber home pipeline.
     *
     * @param context
     *            The operating context.
     * @param request
     *            The servlet request.
     * @param response
     *            The servlet response.
     * @param delegate
     *            The delegate for this action.
     * @throws ServletException
     *             Thrown by delegate.
     * @throws IOException
     *             Thrown by delegate.
     * @see com.redknee.framework.xhome.web.border.Border#service_
     */
    public final void service(final Context context, final HttpServletRequest request,
        final HttpServletResponse response, final RequestServicer delegate) throws ServletException, IOException
    {
        final Context subcontext = context.createSubContext();
        subcontext.put(SubscriberHome.class, new ValidatingHome(CreditLimitPermissionValidator.getInstance(),
            new CreditLimitAdjustmentCreationHome(subcontext, (Home) subcontext.get(SubscriberHome.class))));
        delegate.service(subcontext, request, response);
    }
}
