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
package com.trilogy.app.crm.provision;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Agent that installs generic services to the HLR.
 *
 * @author candy.wong@redknee.com
 */
public class GenericProvisionAgent extends CommonProvisionAgent
{

    /**
     * Installs generic HLR services to HLR. Context must contain the subscriber to be
     * installed keyed by Subscriber.class. Context must contain Service to retrieve
     * additional params needed associated with this service. Context must contain Account
     * of the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @throws AgentException
     *             Thrown if there are problems installing the generic HLR services.
     */
    public void execute(final Context ctx) throws AgentException
    {
        final Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class, null);
        if (subscriber == null)
        {
            throw new AgentException("System error: No subscriber to provision");
        }

        if (ctx.get(Account.class, null) == null)
        {
            throw new AgentException("System error: subscriber's account not found");
        }

        final Service service = (Service) ctx.get(Service.class, null);


        if (service != null && LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Provisioning service " + service.getName(), null).log(ctx);
        }

        callHlr(ctx, true,subscriber,service,null,null);
    }
}
