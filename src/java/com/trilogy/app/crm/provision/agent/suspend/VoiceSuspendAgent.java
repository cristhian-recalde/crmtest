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
package com.trilogy.app.crm.provision.agent.suspend;

import com.trilogy.app.crm.provision.VoiceUnprovisionAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
/**
 * 
 * @author ksivasubramaniam
 * @TODO Once we have the separate HLR commands for suspend resume, we will need to implement this function
 */
public class VoiceSuspendAgent extends CommonSuspendAgent
{

	/**
     * @param ctx
     * 
     */
    public void execute(Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, " Voice Suspend currently follows the same logic as Unprovision", null).log(ctx);
        }
        new VoiceUnprovisionAgent().execute(ctx);
    }
}
