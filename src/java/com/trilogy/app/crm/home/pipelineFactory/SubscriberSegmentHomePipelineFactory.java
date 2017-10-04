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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.SubscriberSegment;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.account.SubscriberSegmentCreateHome;
import com.trilogy.app.crm.support.StorageSupportHelper;


/***
 *
 * @author chandrachud.ingale
 * @since  9.10
 */

public class SubscriberSegmentHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException,
            AgentException
    {
        LogSupport.info(ctx, this, "Installing the SubscriberSegment home ");
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, SubscriberSegment.class, "SUBSCRIBERSEGEMENT");
        home = new SpidAwareHome(ctx, home);
        home = new SubscriberSegmentCreateHome(ctx, home);
        return home;
    }
        
}
