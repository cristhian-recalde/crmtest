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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;


/**
 * Provides a ContextAgent for provisioning EVDO services.
 *
 * @author gary.anderson@redknee.com
 */
public
class EVDOProvisionAgent
    implements ContextAgent
{
    /**
     * {@inheritDoc}
     *
     * The EVDO provisioning is actually a combination of provisioning to AAA
     * and to IPCG.
     *
     * This execute depends upon a Subscriber in the context with the key
     * Subscriber.class, and a Service in the context with the key
     * Service.class.
     */
    public void execute(final Context context)
        throws AgentException
    {
        // At the time of this writing, multople Data (IPCG) services are
        // permitted within a price plan, and the provisioning client handles
        // the case of preexisting profiles, so it always safe to attempt
        // provisioning here without a check.
        new IPCProvisionAgent().execute(context);
        new AAAProvisionAgent().execute(context);
    }
    
    
} // class
