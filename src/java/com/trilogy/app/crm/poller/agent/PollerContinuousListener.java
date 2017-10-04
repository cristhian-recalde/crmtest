/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.
 *
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
// INSPECTED: 10/07/2003  GEA
package com.trilogy.app.crm.poller.agent;

import com.trilogy.framework.core.heartbeat.listeners.HeartBeatContinuousListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.heartbeat.Node;

import com.trilogy.app.crm.poller.lifecycle.PollerLifecycleAgent;

/**
 * This listener will try to restart the poller if it fails. If the heartbeat server is remote
 * the pollers (key) will have to be installed on the server too as RemoteContextAgent(s) so
 * it will restart them on the machines where they were running.   
 * @author psperneac
 */
public class PollerContinuousListener implements HeartBeatContinuousListener
{
	protected String name_;
	protected String agentId_;
    protected boolean enabled_ = true;
	
	/**
    * Constructor
	 * @param name the name of the node
	 * @param key the key under which the ContextAgent is in the context
	 */
	public PollerContinuousListener(String name,String agentId)
	{
		setName(name);
		setAgentId(agentId);
	}

	/**
    * This is called all the time. It has to check if the poller
    * missed it's time, and it acts on it then.
	 * @see com.redknee.framework.core.heartbeat.listeners.HeartBeatContinuousListener#heartbeat(com.redknee.framework.xhome.context.Context, com.redknee.framework.xhome.heartbeat.Node)
	 */
	public void heartbeat(Context ctx, Node node)
	{
	    // Enabled/Disabled check implemented here because HeartBeatListenerImpl provides no
	    // way to remove a listener
	    if( isEnabled() )
	    {
	        // 1. the interval sisnce it didn't report. we hope this is negative
	        long interval=System.currentTimeMillis()-node.getNextReport().getTime();
	        
	        // 2. we wait for 2 intervals, before reporting an error.
	        long threshold=2*(node.getNextReport().getTime()-node.getLastReport().getTime());
	        
	        // if the node missed it's report
	        if(interval>threshold)
	        {
	            PollerLifecycleAgent pollerLifecycle = (PollerLifecycleAgent)ctx.get(PollerLifecycleAgent.class);
	            pollerLifecycle.restartPoller();
	        }
	    }
	}

    public boolean isEnabled()
    {
        return enabled_;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled_ = enabled;
    }

	/**
	 * @see com.redknee.framework.core.heartbeat.listeners.HeartBeatContinuousListener#getName()
	 */
	public String getName()
	{
		return name_;
	}

    public void setName(String name)
    {
        this.name_ = name;
    }

	public String getAgentId()
	{
		return agentId_;
	}

	public void setAgentId(String agentId)
	{
		this.agentId_ = agentId;
	}

}
