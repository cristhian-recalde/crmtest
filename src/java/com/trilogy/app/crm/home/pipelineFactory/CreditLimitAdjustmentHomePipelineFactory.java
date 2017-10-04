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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;

import com.trilogy.app.crm.bean.CreditLimitAdjustment;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;

/**
 * Provides a class from which to create the pipeline of Home decorators that process a CreditLimitAdjustment travelling
 * between the application and the given delegate.
 *
 * @author cindy.wong@redknee.com
 */
public final class CreditLimitAdjustmentHomePipelineFactory implements PipelineFactory
{
    /**
     * Singleton instance.
     */
    private static final CreditLimitAdjustmentHomePipelineFactory INSTANCE =
        new CreditLimitAdjustmentHomePipelineFactory();

    /**
     * Creates a new <code>CreditLimitAdjustmentHomePipelineFactory</code>.
     */
    private CreditLimitAdjustmentHomePipelineFactory()
    {
        // empty constructor to prevent instantiation.
    }

    /**
     * Returns an instance of this class.
     *
     * @return An instance of <code>CreditLimitAdjustmentHomePipelineFactory</code>.
     */
    public static CreditLimitAdjustmentHomePipelineFactory getInstance()
    {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context context, final Context serverContext) throws HomeException
    {
        Home home = StorageSupportHelper.get(context).createHome(context, CreditLimitAdjustment.class, "CreditLimitAdjustment");

        home = new IdentifierSettingHome(context, home, IdentifierEnum.CREDIT_LIMIT_ADJUSTMENT_ID, null);

        home = new SortingHome(home);

        IdentifierSequenceSupportHelper.get(context).ensureNextIdIsLargeEnough(context, IdentifierEnum.CREDIT_LIMIT_ADJUSTMENT_ID, home);

        return home;
    }

}
