 /** This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
*/
package com.trilogy.app.crm.poller.event;

import com.trilogy.app.crm.poller.agent.FnFSelfCareAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This class is called to process every one of the ER1900 parsed from
 * the SELFCARECUG ER files
 * 
 * @author abaid
 * 
 */
public class FnFSelfCareProcessor extends CRMProcessor
{
    public static final String FNF_FROM_SELFCARE_ER_POLLER = "FNF from SELFCARE ER poller";

    public FnFSelfCareProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx,"FnFSelfCare", "FnFSelfCare", queueSize, threads, new FnFSelfCareAgent(this));
     }
}
