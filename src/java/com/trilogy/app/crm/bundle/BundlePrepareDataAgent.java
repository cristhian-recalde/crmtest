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
package com.trilogy.app.crm.bundle;


import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.pipe.PipelineAgent;

/**
 * Prepares the data needed to execute the Bundle adjustment
 *
 * @author psperneac
 */
public class BundlePrepareDataAgent extends PipelineAgent
{
    /**
     * @param delegate
     */
    public BundlePrepareDataAgent(ContextAgent delegate)
    {
        super(delegate);
    }

    /**
     * @param ctx A context
     * @throws AgentException thrown if one of the services fails to initialize
     */

    public void execute(Context ctx) throws AgentException
    {
        BundleAdjustment form = (BundleAdjustment) ctx.get(BundleAdjustment.class);
        Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
        if (subscriber == null)
        {
            throw new AgentException("No subscriber found in context");
        }

        List bundles = new ArrayList();

        for (Iterator i = form.getItems().iterator(); i.hasNext();)
        {
            BundleAdjustmentItem item = (BundleAdjustmentItem) i.next();
            if (item.getAmount() < 0)
            {
                throw new AgentException("Cannot have amount < 0");
            }

            if (bundles.contains(Long.valueOf(item.getBundleProfile())))
            {
                throw new AgentException("Cannot apply bundle id +" + item.getBundleProfile() + " twice");
            }

            bundles.add(Long.valueOf(item.getBundleProfile()));
        }

        if (form.getAgent() == null || form.getAgent().length() == 0)
        {
            final String csrIdentifier;
            {
                final User principal = (User) ctx.get(Principal.class, new User());
                csrIdentifier = principal.getId();
            }

            form.setAgent(csrIdentifier);

        }

        pass(ctx, this, "The subscriber has been set");
    }
}
