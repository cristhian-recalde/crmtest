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
package com.trilogy.app.crm.migration;

import com.trilogy.app.crm.priceplan.AutoPricePlanMigration;
import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.AgentHelper;
import com.trilogy.framework.core.cron.SchedulerConfigException;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.core.cron.TaskStatusEnum;
import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * @author skushwaha
 *
 * This corn task changes price plan for all the 
 * subscriber belonging to a price-plan to a new price plan on completion 
 * of promotion duration. 
 *  
 */
public class PricePlanMigrationCronAgent implements ContextAgent, CronContextAgent
{
    /**
     * The default name of the cron agent.
     */
    public static final
    String PRICEPLAN_MIGRATION_NAME = "Price Plan Migration";

    /**
     * The default description of the cron agent.
     */
    public static final
    String PRICEPLAN_MIGRATION_DESCRIPTION = "Price Plan Migration";


    /**
     * Installs this cron agent using its default settings.
     *
     * @param context The operating context.
     */
    public static void install(final Context context)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(PRICEPLAN_MIGRATION_NAME);
            entry.setAgent(new PricePlanMigrationCronAgent(context));
            entry.setContext(context);

            AgentHelper.add(context, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, PRICEPLAN_MIGRATION_NAME);
            task.setName(PRICEPLAN_MIGRATION_NAME);
            task.setCronEntry("midnight");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(PRICEPLAN_MIGRATION_DESCRIPTION);

            TaskHelper.add(context, task);
        }
        catch (final SchedulerConfigException exception)
        {
            new MajorLogMsg(
                PricePlanMigrationCronAgent.class.getName(),
                "Failed to install cron task for price plan migration.",
                exception).log(context);
        }
    }


    /**
     * Creates a new PricePlanMigrationCronAgent.
     *
     * @param context The operating context.
     */
    public PricePlanMigrationCronAgent(final Context context)
    {
        contextSupport_.setContext(context);
    }
   

    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return contextSupport_.getContext();
    }


    /**
     * {@inheritDoc}
     */
    public void setContext(final Context context)
    {
        contextSupport_.setContext(context);
    }


    /**
     * {@inheritDoc}
     */
    public void execute(final Context context)
    {
        new InfoLogMsg(
            this,
            "Beginning the price plan migration cron agent.",
            null).log(context);

        try
        {
            final AutoPricePlanMigration activator = new AutoPricePlanMigration(context);
            activator.processPricePlanMigration();
        }
        finally
        {
            new InfoLogMsg(
                this,
                "Price plan auto-activation cron agent completed processing.",
                null).log(context);
        }
    }


    /**
     * {@inheritDoc}
     */
    public void stop()
    {
        // TODO - 2004-10-07 - Is this necessary?
    }


    /**
     * Provides ContextAware support for this class.  ContextAwareSupport is
     * abstract, so we must create a derivation of it.
     */
    private final ContextAware contextSupport_ = new ContextAwareSupport() { };
}
