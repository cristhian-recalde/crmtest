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
package com.trilogy.app.crm.web.agent;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgentProxy;


/**
 * Provides a bridge to set-up the Context used by the WebAgent.
 *
 * @author gary.anderson@redknee.com
 */
public class WebAgentBridge
    extends WebAgentProxy
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context parentContext)
        throws AgentException
    {
        final Context context = parentContext.createSubContext();
        context.setName("WebAgentBridge");

        try
        {
            ServletBridge.populateContext(
                context,
                ServletBridge.getRequest(context),
                ServletBridge.getResponse(context));

            delegate(context);
        }
        finally
        {
            ServletBridge.depopulateContext(context);
        }
    }


    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
}
