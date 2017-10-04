package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.agent.CronStatCollectorAgent;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.AgentHelper;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.XCronLifecycleAgentControlConfig;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

public class EnabledTaskEntryPredicate implements Predicate
{

    private static final long serialVersionUID = 1L;
    
    public static Predicate getInstance()
    {
        if (instance_ == null)
        {
            instance_ = new EnabledTaskEntryPredicate();
        }
        
        return instance_;
    }
    
    private EnabledTaskEntryPredicate()
    {
        
    }

    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        boolean enabled = true;
        TaskEntry taskEntry = (TaskEntry) obj;
        if (taskEntry.getAgent() instanceof XCronLifecycleAgentControlConfig)
        {
            XCronLifecycleAgentControlConfig agent = (XCronLifecycleAgentControlConfig) taskEntry.getAgent();
            enabled = isCronLifecycleAgentControlConfigEnabled(ctx, agent);
        }
        
        return enabled;
    }
    
    private boolean isCronLifecycleAgentControlConfigEnabled(Context ctx, XCronLifecycleAgentControlConfig agent)
    {
        boolean enabled = true;
        if (agent!=null)
        {
            AgentEntry agentEntry = AgentHelper.retrieve(ctx, agent.getAgent());
            enabled = isAgentEntryEnabled(ctx, agentEntry);
        }
        return enabled;
    }

    private boolean isAgentEntryEnabled(Context ctx, AgentEntry agent)
    {
        boolean enabled = true;
        if (agent!=null)
        {
            ContextAgent contextAgent = agent.getAgent();
            if (contextAgent instanceof LifecycleAgentScheduledTask)
            {
                enabled = isLifecycleAgentScheduledTaskEnabled(ctx, (LifecycleAgentScheduledTask) contextAgent);
            }
            else if (contextAgent instanceof CronStatCollectorAgent && ((CronStatCollectorAgent) contextAgent).getDelegate() instanceof LifecycleAgentScheduledTask)
            {
                enabled = isLifecycleAgentScheduledTaskEnabled(ctx, (LifecycleAgentScheduledTask) ((CronStatCollectorAgent) contextAgent).getDelegate());
            }
        }
        return enabled;
    }
    
    private boolean isLifecycleAgentScheduledTaskEnabled(Context ctx, LifecycleAgentScheduledTask agent)
    {
        boolean enabled = true;
        if (agent!=null)
        {
            enabled = agent.isEnabled(ctx);
        }
        return enabled;
    }
    
    private static Predicate instance_ = new EnabledTaskEntryPredicate();
}
