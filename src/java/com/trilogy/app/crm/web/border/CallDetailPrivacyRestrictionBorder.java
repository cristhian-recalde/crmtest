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
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.home.calldetail.CallDetailPrivacyRestrictionHome;

/**
 * Add Privacy Restriction on CallDetail.
 *
 * @author kason.wong@redknee.com
 */
public class CallDetailPrivacyRestrictionBorder implements Border
{
    private Object homeKey_;

    /**
     * Default constructor uses default CallDetailHome class as the home key.
     */
    public CallDetailPrivacyRestrictionBorder()
    {
        this(CallDetailHome.class);
    }

    /**
     * This constructor allows you to specify a custom home key.
     *
     * @param homeKey
     */
    public CallDetailPrivacyRestrictionBorder(final Object homeKey)
    {
        homeKey_ = homeKey;
    }

    /**
     * {@inheritDoc}
     */
    public void service(
            final Context context,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final RequestServicer delegate)
        throws ServletException, IOException
    {
        final Context subCtx = context.createSubContext();
        subCtx.setName(this.getClass().getName());

        final Home home = new CallDetailPrivacyRestrictionHome((Home) subCtx.get(homeKey_));

        subCtx.put(homeKey_, home);

        delegate.service(subCtx, request, response);
    }

}
