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

package com.trilogy.app.crm.bas.promotion.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.bean.HandsetPromotionHistoryXDBHome;
import com.trilogy.app.crm.home.PipelineFactory;

/**
 * Creates the pipeline for <code>HandsetGenerationHistoryHome</code>.
 *
 * @author cindy.wong@redknee.com
 */
public class HandsetPromotionHistoryHomePipelineFactory implements PipelineFactory
{
    /**
     * Create a new instance of <code>HandsetPromotionHistoryHomePipelineFactory</code>.
     */
    protected HandsetPromotionHistoryHomePipelineFactory()
    {
        super();
    }

    /**
     * Returns an instance of <code>HandsetPromotionHistoryHomePipelineFactory</code>.
     *
     * @return An instance of <code>HandsetPromotionHistoryHomePipelineFactory</code>.
     */
    public static HandsetPromotionHistoryHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new HandsetPromotionHistoryHomePipelineFactory();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
        IOException, AgentException
    {
        Home home = new HandsetPromotionHistoryXDBHome(ctx, "HANDSETPROMOTIONHISTORY");

        home = new HandsetPromotionHistoryAdaptedHome(ctx, home);

        home = new AuditJournalHome(ctx, home);

        home = new ValidatingHome(HandsetPromotionHistoryDatesValidator.instance(), home);

        return home;
    }

    /**
     * Singleton instance.
     */
    private static HandsetPromotionHistoryHomePipelineFactory instance;
}
