/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.poller.event;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.poller.agent.URCSPromotionProvisioningAgent;


/**
 * Processor to set up poller for URCS Promotion Provisioning ER.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class URCSPromotionProvisioningProcessor extends URSProcessor
{    
    public URCSPromotionProvisioningProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, "URSPromotionProvisioning", "URSPromotionProvisioningERErrFile", queueSize, threads, new URCSPromotionProvisioningAgent(this));
    }
}
