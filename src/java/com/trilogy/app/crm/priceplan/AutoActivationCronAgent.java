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
package com.trilogy.app.crm.priceplan;

import java.util.Date;

import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.AgentHelper;
import com.trilogy.framework.core.cron.SchedulerConfigException;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.core.cron.TaskStatusEnum;
import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Provides a cron agent for automatically invoking the price plan
 * auto-activation code.
 *
 * @author gary.anderson@redknee.com
 */
public
class AutoActivationCronAgent
    implements ContextAware, CronContextAgent
{
    /**
     * The default name of the cron agent.
     */
    public static final
    String PRICEPLAN_AUTOACTIVATE_NAME = "Price Plan Version Automatic Activation";

    /**
     * The default description of the cron agent.
     */
    public static final
    String PRICEPLAN_AUTOACTIVATE_DESCRIPTION = "Price Plan Version Automatic Activation";


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
            entry.setName(PRICEPLAN_AUTOACTIVATE_NAME);
            entry.setAgent(new AutoActivationCronAgent(context));
            entry.setContext(context);

            AgentHelper.add(context, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, PRICEPLAN_AUTOACTIVATE_NAME);
            task.setName(PRICEPLAN_AUTOACTIVATE_NAME);
            task.setCronEntry("midnight");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(PRICEPLAN_AUTOACTIVATE_DESCRIPTION);

            TaskHelper.add(context, task);
        }
        catch (final SchedulerConfigException exception)
        {
            new MajorLogMsg(
                AutoActivationCronAgent.class.getName(),
                "Failed to install cron task for price plan auto-activation.",
                exception).log(context);
        }
    }


    /**
     * Creates a new AutoActivationCronAgent.
     *
     * @param context The operating context.
     */
    public AutoActivationCronAgent(final Context context)
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
            "Beginning the price plan auto-activation cron agent.",
            null).log(context);

        try
        {
            final AutoActivation activator = new AutoActivation(getContext(), new Date());
            activator.processAllActivations();
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

} // class
