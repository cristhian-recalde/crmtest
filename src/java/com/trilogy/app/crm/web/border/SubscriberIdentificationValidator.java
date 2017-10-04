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

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;


/**
 * Identification Validator at subscriber level. This is not implemented as a simple
 * Validator on Subscriber Home, because we want to avoid this validation when the
 * subscriber is not updated or created from GUI. Existing subscriber objects that are
 * updated automatialy should not be validated. Validator on Subscriber Home could be made
 * possible if it had a flag signaling if it is a GUI update. The problem with this
 * approach is in multinode deployment, because Context is not passed from E-Care to BAS
 * and the validation is run on the BAS node so it is hard to signal that it is web
 * update.
 * 
 * @author arturo.medina@redknee.com
 */
public final class SubscriberIdentificationValidator implements Border, Validator
{

    /**
     * Singleton instance.
     */
    private static SubscriberIdentificationValidator instance_;


    /**
     * Create a new instance of <code>SubscriberIdentificationValidator</code>.
     */
    private SubscriberIdentificationValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriberIdentificationValidator</code>.
     * 
     * @return An instance of <code>SubscriberIdentificationValidator</code>.
     */
    public static synchronized SubscriberIdentificationValidator instance()
    {
        if (instance_ == null)
        {
            instance_ = new SubscriberIdentificationValidator();
        }
        return instance_;
    }


    /**
     * Decorate subscriber home pipeline with this validator.
     * 
     * @param ctx
     *            The operating context.
     */
    public static void decorateHome(final Context ctx)
    {
        ctx.put(SubscriberHome.class, new ValidatingHome(instance(), (Home) ctx.get(SubscriberHome.class)));
    }


    /**
     * {@inheritDoc}
     */
    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res,
        final RequestServicer delegate) throws ServletException, IOException
    {
        final Context subCtx = ctx.createSubContext();
        decorateHome(subCtx);
        delegate.service(subCtx, req, res);
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
 
    }


    /**
     * @param ctx
     *            the operating context
     * @param id
     *            ID of the subscriber to retrieve
     * @return Subscriber object for the ID provided
     * @throws HomeException
     *             only from the underlying home.find() call
     */
    private Subscriber getOriginalSubscriber(final Context ctx, final String id) throws HomeException
    {
        Subscriber sub = null;
        final Home subHome = (Home) ctx.get(SubscriberHome.class);

        if (subHome != null)
        {
            sub = (Subscriber) subHome.find(ctx, id);
        }

        return sub;
    }
}
