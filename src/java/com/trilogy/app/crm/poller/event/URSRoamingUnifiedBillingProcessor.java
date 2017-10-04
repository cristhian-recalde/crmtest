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

import com.trilogy.app.crm.poller.agent.URSUnifiedBillingAgent;
import com.trilogy.framework.xhome.context.Context;


/**
 * This class is called to process every one of the ER501 parsed from
 * the URS Roaming ER files.  Roaming ER501s differ from regular ER501s
 * in that the chargedMSISDN field actually contains the IMSI.
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class URSRoamingUnifiedBillingProcessor extends URSProcessor
{
    public URSRoamingUnifiedBillingProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, "URSUnifiedBilling", "URSUnifiedBillingERErrFile", queueSize, threads, new URSUnifiedBillingAgent(this, true));
    }
}
