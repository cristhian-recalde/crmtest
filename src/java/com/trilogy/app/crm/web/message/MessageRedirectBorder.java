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
package com.trilogy.app.crm.web.message;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.language.MessageMgrSPI;
import com.trilogy.framework.xhome.language.DummyMessageMgrSPI;

/**
 * Modify the Label for the MT Call Detail screens.
 *
 * @author victor.stratan@redknee.com
 */
public class MessageRedirectBorder implements Border
{
    /**
     * {@inheritDoc}
     */
    public void service(
            final Context ctx,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final RequestServicer delegate)
        throws ServletException, IOException
    {
        final Context subCtx = ctx.createSubContext();
        subCtx.setName(this.getClass().getName());

        final MessageMgrSPI spi = (MessageMgrSPI) ctx.get(MessageMgrSPI.class, DummyMessageMgrSPI.instance());
        final MessageMgrRedirect redirectMgr = new MessageMgrRedirect(spi);
        ctx.put(MessageMgrSPI.class, redirectMgr);

        redirectMgr.setRedirects(redirect_);

        delegate.service(subCtx, request, response);
    }

    public MessageRedirectBorder addRedirect(final String sourceKey, final String destinationKey)
    {
        redirect_.put(sourceKey, destinationKey);
        return this;
    }

    private Map redirect_ = new HashMap();
}
