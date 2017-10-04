/*
 * MCommerceProcessor.java
 * 
 * Author : danny.ng@redknee.com Date : Mar 03, 2006
 * 
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
package com.trilogy.app.crm.poller.event;

import com.trilogy.app.crm.poller.agent.OCGChargingAgent;
import com.trilogy.framework.xhome.context.Context;

/**
 * This class is called to process every one of the ER1251 parsed from
 * the MCommerce ER files
 * 
 * @author danny.ng@redknee.com
 * @since Mar 03, 2006
 */
public class OCGChargingProcessor extends CRMProcessor
{
    public OCGChargingProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx,"OCGTransaction", "OCGTransactionERErrFile", queueSize, threads, new OCGChargingAgent(this));
    }
}
