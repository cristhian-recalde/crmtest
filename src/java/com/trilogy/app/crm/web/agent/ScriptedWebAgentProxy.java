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

import javax.script.ScriptException;

import com.trilogy.framework.core.scripting.ScriptExecutor;
import com.trilogy.framework.core.scripting.support.ScriptExecutorFactory;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.agent.WebAgentProxy;

/**
 * Provides a scripted WebAgent proxy.
 *
 * @author gary.anderson@redknee.com
 */
public class ScriptedWebAgentProxy
    extends AbstractScriptedWebAgentProxy
{

    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx) throws AgentException
    {
        ScriptExecutor executor = ScriptExecutorFactory.create(getScriptLanguage());
        WebAgentProxy proxy = null;
        try
        {
            proxy = (WebAgentProxy) executor.retrieveObject(ctx, getScript(), "");
        }
        catch (final ScriptException exception)
    {
            throw new AgentException("Failed to create WebAgentProxy: " + exception.getMessage(), exception);
        }
        catch (final ClassCastException exception)
        {
            throw new AgentException(
                "Script returned something that is not a WebAgentProxy: " + exception.getMessage(),
                exception);
        }

        if (proxy == null)
        {
            throw new AgentException("Script returned null: " + ctx.get(ScriptExecutor.RESULT));
        }

        proxy.setDelegate(getDelegate());

        proxy.execute(ctx);
    }
}
