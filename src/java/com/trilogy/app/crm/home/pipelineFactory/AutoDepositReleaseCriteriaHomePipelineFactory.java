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
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.deposit.AutoDepositReleaseCriteriaValidator;
import com.trilogy.app.crm.home.AutoDepositReleaseCriteriaCreationHome;
import com.trilogy.app.crm.home.AutoDepositReleaseCriteriaRemoveProtectionHome;
import com.trilogy.app.crm.home.core.CoreAutoDepositReleaseCriteriaHomePipelineFactory;

/**
 * Provides a class from which to create the pipeline of Home decorators that process a AutoDepositReleaseCriteria
 * travelling between the application and the given delegate.
 *
 * @author cindy.wong@redknee.com
 */
public class AutoDepositReleaseCriteriaHomePipelineFactory extends CoreAutoDepositReleaseCriteriaHomePipelineFactory
{

    /**
     * Singleton instance.
     */
    private static AutoDepositReleaseCriteriaHomePipelineFactory instance_;


    /**
     * Create a new instance of <code>AutoDepositReleaseCriteriaHomePipelineFactory</code>.
     */
    protected AutoDepositReleaseCriteriaHomePipelineFactory()
    {
        // empty
    }


    /**
     * Returns an instance of <code>AutoDepositReleaseCriteriaHomePipelineFactory</code>.
     * 
     * @return An instance of <code>AutoDepositReleaseCriteriaHomePipelineFactory</code>.
     */
    public static AutoDepositReleaseCriteriaHomePipelineFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new AutoDepositReleaseCriteriaHomePipelineFactory();
        }
        return instance_;
    }

    /**
     * Add decorators to AutoDepositReleaseCriteriaHome.
     *
     * @param context
     *            The operating context.
     * @param rawHome
     *            The home to decorate.
     * @return The decorated home.
     */
    @Override
    public Home addDecorators(final Context context, final Home rawHome)
    {
        // add OM & ER on creation
        Home home = new AutoDepositReleaseCriteriaCreationHome(rawHome);

        // validates releaseSchedule
        home = new ValidatingHome(new AutoDepositReleaseCriteriaValidator(), home);

        // prevents criteria from being deleted when still in use
        home = new AutoDepositReleaseCriteriaRemoveProtectionHome(context, home);
        
        home = super.addDecorators(context, home);

        return home;
    }
}
