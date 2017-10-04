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
package com.trilogy.app.crm.poller.cron;

import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.heartbeat.HeartBeat;
import com.trilogy.framework.xhome.heartbeat.HeartBeatException;
import com.trilogy.framework.xhome.heartbeat.HeartBeatInternalException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * @author psperneac
 */
public class PollerHeartbeatAgent implements CronContextAgent
{
    // REVIEW(readability): Data members should be placed at the bottom of the
    // class.  GEA
	private Object key=null;
	private String name=null;
	private String description=null;
	private long interval=60;	// one minute

	/**
    * Constructs a poller heartbeat
    * 
	 * @param name the name of the poller
	 * @param key the key under which the poller is found in the 
	 * @param interval
	 */
	public PollerHeartbeatAgent(String name,Object key,long interval)
	{
		setName(name);
		setKey(key);
		setInterval(interval);
	}

	/**
	 * @see com.redknee.framework.core.cron.agent.CronContextAgent#stop()
	 */
	public void stop()
	{
	}

	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context ctx) throws AgentException
	{
		try
		{
			HeartBeat proxy = (HeartBeat) ctx.get(HeartBeat.class);
			if(checkPoller(ctx))
			{
				proxy.tick(getName(),getDescription(),getInterval());
			}
		}
		catch (HeartBeatInternalException e)
		{
			new MajorLogMsg(this,e.getMessage(), e).log(ctx);
		}
		catch (HeartBeatException e)
		{
			new MajorLogMsg(this,e.getMessage(), e).log(ctx);
		}
	}

	/**
	 * @param ctx
	 * @return
	 */
	private boolean checkPoller(Context ctx)
	{
		Thread th=(Thread) ctx.get(getKey());
		if(th==null || !th.isAlive())
		{
			return false;
		}
		
		return true;
	}

	/**
	 * @return
	 */
	private String getDescription()
	{
		return description;
	}

	/**
	 * @return
	 */
	private long getInterval()
	{
		return interval;
	}

	/**
	 * @return
	 */
	private String getName()
	{
		return name;
	}

	/**
	 * @return
	 */
	public Object getKey()
	{
		return key;
	}

	/**
	 * @param string
	 */
	public void setDescription(String string)
	{
		description = string;
	}

	/**
	 * @param l
	 */
	public void setInterval(long l)
	{
		interval = l;
	}

	/**
	 * @param object
	 */
	public void setKey(Object object)
	{
		key = object;
	}

	/**
	 * @param string
	 */
	public void setName(String string)
	{
		name = string;
	}

}
