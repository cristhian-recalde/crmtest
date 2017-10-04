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
package com.trilogy.app.crm.provision.agent.resume;

import com.trilogy.app.crm.provision.EVDOProvisionAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Provides a ContextAgent for unprovisioning EVDO services.
 *
 * @author gary.anderson@redknee.com
 */
public
class EVDOResumeAgent
    implements ContextAgent
{
    /**
     * {@inheritDoc}
     *
     * The EVDO suspension is actually a combination of suspension to AAA
     * and to IPCG.
     *
     * This execute depends upon a Subscriber in the context with the key
     * Subscriber.class, and a Service in the context with the key
     * Service.class.
     */
    public void execute(final Context context)
        throws AgentException
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this," EVDO Resume currently follows the same logic as Provision", null).log(context);
        }
        new EVDOProvisionAgent().execute(context);
    }

    
} // class
