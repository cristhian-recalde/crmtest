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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.home.sub;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.state.ResumeSubscriberServiceAction;
import com.trilogy.app.crm.state.StateChange;
import com.trilogy.app.crm.state.SubscriberServiceStateAction;
import com.trilogy.app.crm.state.SuspendSubscriberServiceAction;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Decorator that responds to any subscriber state change and executes the agent from a particular state change
 * @author arturo.medina@redknee.com
 * 
 * Ported from CRM 7_4. Part of the Subscriber State Service Update provisioning module.
 * The original class was in a different package:
 * com.redknee.app.crm.home.SubscriberStateChangeUpdateHome
 * @since CRM 8.2
 * @author angie.li@redknee.com
 *
 */
public class SubscriberStateChangeUpdateHome extends HomeProxy
{

    /**
     * @param ctx
     * @param delegate
     */
    public SubscriberStateChangeUpdateHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
        initAgents(ctx);
    }


    /**
     * Instantiates the agents. So far the only needed are
     * Active-->Suspend
     * Suspend-->Active
     * In the future and for more service more state changes can be implemented
     * @param ctx the operating context
     */
    private void initAgents(Context ctx)
    {
        agents_ = new HashMap<StateChange, SubscriberServiceStateAction>();
        
        SuspendSubscriberServiceAction suspendAgent = new SuspendSubscriberServiceAction();
        ResumeSubscriberServiceAction resumeAgent = new ResumeSubscriberServiceAction();
        
        //Suspend agents for postpaid and prepaid
        addAgent(SubscriberStateEnum.ACTIVE, SubscriberStateEnum.SUSPENDED, suspendAgent);
        addAgent(SubscriberStateEnum.ACTIVE, SubscriberStateEnum.LOCKED, suspendAgent);
        addAgent(SubscriberStateEnum.ACTIVE, SubscriberStateEnum.EXPIRED, suspendAgent);
        
        //Resume activity for postpaid and prepaid
        addAgent(SubscriberStateEnum.SUSPENDED, SubscriberStateEnum.ACTIVE, resumeAgent);
        addAgent(SubscriberStateEnum.LOCKED, SubscriberStateEnum.ACTIVE, resumeAgent);
        addAgent(SubscriberStateEnum.EXPIRED, SubscriberStateEnum.ACTIVE, resumeAgent);
        
        /* Observation: CRM doesn't unselect (unprovision) the subscriber services when
         * changing the subscriber state to "Inactive".
         * @author angie.li
         * Assumption: Oct 21st 2008, we don't have adequate support of Subscriber Reactivation
         * and we are not supporting this feature.  Thus I am not creating a reactivationAgent.
         * Note for Future: For Service Refactoring (CRM 8.0) build, the removeAgent can be 
         * excluded from the installation as, upon Subscriber Deactivation, all of the 
         * Subscriber services are unprovisioned. For now, add the removeAgent.   
         * See the CRM 7.4 version of this class to view the implementation of the 
         * RemoveSubscriberServiceAction.
         * 
         * Update: Oct 22, 2009. @author angie.li
         * We have arrived in the future!  We have finally ported this feature from CRM 7.4
         * to CRM 8.2 and just as the comment above says, all subscriber services are 
         * unprovisioned during subscriber deactivation.
         * I have omitted the adding the remove agents triggered into action by deactivating
         * the subscription.*/
    }


    private void addAgent(SubscriberStateEnum from,
            SubscriberStateEnum to,
            SubscriberServiceStateAction agent)
    {
        StateChange key = new StateChange();
        key.setFrom(from);
        key.setTo(to);
        agents_.put(key, agent);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        
        if (oldSub != null || obj != null)
        {
            Subscriber newSub = (Subscriber) obj;
            SubscriberServiceStateAction agent = getStateChangeAgent(ctx, oldSub.getState(), newSub.getState());
            if (agent != null)
            {
                agent.execute(ctx, newSub);
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, " Agent to execute for state from " +
                            oldSub.getState() +
                            "to " + newSub.getState() +
                            " doesn't exist ignoring this call ");
                }
            }
        }
        else
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, " The old subscriber doesn't exists, ignoring this call ");
            }
        }
        
        return super.store(ctx, obj);
    }

    /**
     * returns the agent to execute
     * @param ctx
     * @param from
     * @param to
     * @return
     */
    private SubscriberServiceStateAction getStateChangeAgent(Context ctx, 
            SubscriberStateEnum from,
            SubscriberStateEnum to)
    {
        SubscriberServiceStateAction agent = null;
        StateChange key = new StateChange();
        key.setFrom(from);
        key.setTo(to);

        agent = agents_.get(key);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            if (agent != null)
            {
                LogSupport.debug(ctx, this, " The agent to execute is : " + agent.getClass().getName());
            }
            else
            {
                LogSupport.debug(ctx, this, " No agent to execute");
            }
        }
        
        return agent;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -8948277519623536313L;
    
    /**
     * The registered agents based on the state action.
     */
    private Map<StateChange, SubscriberServiceStateAction> agents_;

}
