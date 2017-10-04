/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.provision.agent.suspend;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.provision.VoicemailUnprovisionAgent;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @TODO: When you use differnt voicemail app , we need to implement this
 * 
 */
public class VoicemailSuspendAgent extends CommonSuspendAgent
{

    public void execute(Context ctx) throws AgentException
    {
        if (!LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY)) 
        {   
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this," Voicemail Suspend currently follows the same logic as Unprovision", null).log(ctx);
            }
        
            new VoicemailUnprovisionAgent().execute(ctx);
        }    
    }
}
