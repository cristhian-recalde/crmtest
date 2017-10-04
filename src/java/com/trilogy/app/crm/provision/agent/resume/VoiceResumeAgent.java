/*
 *  VoiceUnprovisionAgent.java
 *
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

import com.trilogy.app.crm.provision.VoiceProvisionAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;

public class VoiceResumeAgent extends VoiceProvisionAgent
{

    /**
     * <p>
     * Resume voices services to HLR and AppEcp.
     * </p>
     * <p>
     * Context must contain the subscriber to be installed keyed by Subscriber.class
     * </p>
     * <p>
     * Context must contain Service to retrieve additional params needed associated with
     * this service
     * </p>
     * <p>
     * Context must contain AppEcpClient to provision AppEcp using CORBA
     * </p>
     * <p>
     * Context must contain Account of the subscriber
     * </p>
     *
     * @param ctx
     *            The operating context.
     * @throws AgentException
     *             Thrown if there are problems installing the services.
     */
    public void execute(final Context ctx) throws AgentException
    {
        provisionVoice(ctx,RESUME_AGENT);

    }


    
}
