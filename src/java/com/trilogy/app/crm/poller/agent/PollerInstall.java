/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE. ("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with REDKNEE.
 * 
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
// INSPECTED: 10/06/2003 GEA
package com.trilogy.app.crm.poller.agent;

import java.util.Collection;

import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.AgentHelper;
import com.trilogy.framework.core.cron.SchedulerConfigException;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.core.cron.TaskStatusEnum;
import com.trilogy.framework.core.heartbeat.HeartBeatListenerImpl;
import com.trilogy.framework.lifecycle.LifecycleAgent;
import com.trilogy.framework.lifecycle.LifecycleAgentControl;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.lifecycle.LifecycleSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.bean.ErPollerConfigHome;
import com.trilogy.app.crm.factory.PMContextFactory;
import com.trilogy.app.crm.poller.CRMPoller;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.cron.PollerHeartbeatAgent;
import com.trilogy.app.crm.poller.factory.CRMPollerContextFactory;
import com.trilogy.app.crm.poller.factory.ErPollerConfigContextFactory;
import com.trilogy.app.crm.poller.lifecycle.PollerLifecycleAgent;

/**
 * This Context agent installs and starts all the polling services, together with the heartbeat checks on those
 * services.
 * 
 * @author psperneac
 */
public class PollerInstall implements ContextAgent, Constants
{

   /**
    * If an instance of ErPollerConfig is in the context, then that poller is installed.
    * Otherwise, all the configured pollers are installed.
    * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
    */
   public void execute(Context ctx) throws AgentException
   {
       ErPollerConfig config = (ErPollerConfig) ctx.get(ErPollerConfig.class);
       if( config == null )
       {
           // Install all configured pollers
           Home erHome = (Home) ctx.get(ErPollerConfigHome.class);
           try
           {
               Collection<ErPollerConfig> configs = erHome.selectAll(ctx);
               for ( ErPollerConfig configObj : configs )
               {
                   if( configObj != null )
                   {
                       Context pollerInstallCtx = ctx.createSubContext();
                       pollerInstallCtx.setName("PollerInstallContext-" + configObj.ID());
                       ctx.put("PollerInstallContext-" + configObj.ID(), pollerInstallCtx);
                       
                       pollerInstallCtx.put(ErPollerConfig.class, configObj);
                       try
                       {
                           execute(pollerInstallCtx);   
                       }
                       catch( AgentException ae )
                       {
                           new MajorLogMsg(this, "Error installing poller " + configObj.ID() + ": " + ae.getMessage(), ae).log(ctx);
                       }
                   }
               }
           }
           catch (UnsupportedOperationException e)
           {
               new MajorLogMsg(this, e.getMessage(), e).log(ctx);
           }
           catch (HomeException e)
           {
               new MajorLogMsg(this, e.getMessage(), e).log(ctx);
           }
       }
       else
       {
           // Install the poller only if it isn't already installed.
           String agentId = PollerLifecycleAgent.class.getName() + ": Poller ID " + config.getId();
           String pollerCtxName = "PollerContext-" + config.getId();
           if( !isPollerAgentInstalled(ctx, agentId) )
           {
               Context pollerCtx = ctx.createSubContext(pollerCtxName);
               ctx.put(pollerCtxName, pollerCtx);

               // Use a context factory to get the pollers to use the most current poller config
               // This makes it possible to propogate config changes to pollers without a restart
               pollerCtx.put(
                       ErPollerConfig.class, 
                       new PMContextFactory(
                               ErPollerConfigContextFactory.class.getName(),
                               "create()",
                               new ErPollerConfigContextFactory(config)));

               // Use a context factory to create poller instances on demand
               pollerCtx.put(
                       CRMPoller.class, 
                       new PMContextFactory(
                               CRMPollerContextFactory.class.getName(),
                               "create()",
                               CRMPollerContextFactory.instance()));

               installHeartbeatServer(pollerCtx, agentId, config);
               installHeartbeatClient(pollerCtx, config);
               
               LifecycleAgent pollerLifecycle = new PollerLifecycleAgent(pollerCtx, agentId);
               pollerCtx.put(PollerLifecycleAgent.class, pollerLifecycle);
               pollerLifecycle.execute(pollerCtx);
           }
           else if( !isPollerAgentRunning(ctx, agentId) )
           {
               try
               {
                   LifecycleAgentControl lifecycleAgentControl = LifecycleSupport.getLifecycleAgentControl(ctx, agentId);
                   if( lifecycleAgentControl != null )
                   {
                       LifecycleAgent agent = lifecycleAgentControl.getLifecycleAgent();
                       if( agent instanceof PollerLifecycleAgent )
                       {
                           ((PollerLifecycleAgent)agent).restartPoller();
                       }
                   }
               }
               catch (HomeException e)
               {
                   new DebugLogMsg(this, HomeException.class.getSimpleName() + " occurred in " + PollerInstall.class.getSimpleName() + ".execute(): " + e.getMessage(), e).log(ctx);
               }
           }
           else
           {
               String msg = "Lifecycle agent '" + agentId + "' already installed.  Restart it manually to make it use the new poller configuration.";
               new InfoLogMsg(this,msg,null).log(ctx);
               throw new AgentException(msg);
           }
       }
   }

   public static String getPollerAgentName(ErPollerConfig pollerConfig)
   {
       return Constants.POLLERAGENT + pollerConfig.getId();
   }
   
   public static String getPollerTaskName(ErPollerConfig pollerConfig)
   {
       return Constants.POLLERTASK + pollerConfig.getId();
   }

   private boolean isPollerAgentInstalled(Context ctx, String agentId)
   { 
       try
       {
           LifecycleAgentControl lifecycleAgentControl = LifecycleSupport.getLifecycleAgentControl(ctx, agentId);
           if( lifecycleAgentControl != null )
           {
               LifecycleStateEnum currentState = lifecycleAgentControl.getCurrentState();
               if( !LifecycleStateEnum.UNKNOWN.equals(currentState) )
               {
                   return true;
               }
           }
       }
       catch (HomeException e)
       {
           new MajorLogMsg(this, e.getClass().getSimpleName() + " occurred in " + PollerInstall.class.getSimpleName() + ".execute(): " + e.getMessage(), e).log(ctx);
       }
       return false;
   }

   private boolean isPollerAgentRunning(Context ctx, String agentId)
   { 
       try
       {
           LifecycleAgentControl lifecycleAgentControl = LifecycleSupport.getLifecycleAgentControl(ctx, agentId);
           if( lifecycleAgentControl != null )
           {
               LifecycleStateEnum currentState = lifecycleAgentControl.getCurrentState();
               if( LifecycleStateEnum.START.equals(currentState)
                       ||  LifecycleStateEnum.RUNNING.equals(currentState)
                       ||  LifecycleStateEnum.INITIALIZING.equals(currentState)
                       ||  LifecycleStateEnum.STARTING.equals(currentState)
                       ||  LifecycleStateEnum.STOPPING.equals(currentState)
                       ||  LifecycleStateEnum.RELEASING.equals(currentState) )
               {
                   return true;
               }
               else if( LogSupport.isDebugEnabled(ctx) )
               {
                   new DebugLogMsg(this, "Poller agent '" + agentId + "' is installed and can be started safely.  Current State=" + currentState.getDescription(), null).log(ctx);
               }
           }
       }
       catch (HomeException e)
       {
           new MajorLogMsg(this, e.getClass().getSimpleName() + " occurred in " + PollerInstall.class.getSimpleName() + ".execute(): " + e.getMessage(), e).log(ctx);
       }
       return false;
   }

   /**
	 * Installs listeners that will restart the pollers if they stop.
	 * 
	 * @param ctx
	 */
   private void installHeartbeatServer(Context ctx, String agentId, ErPollerConfig config)
   {
      PollerContinuousListener pollerContinuousListener = new PollerContinuousListener(getPollerAgentName(config), agentId);
      pollerContinuousListener.setEnabled(false);
      ctx.put(PollerContinuousListener.class, pollerContinuousListener);
      HeartBeatListenerImpl.addContinuous(
         ctx,
         pollerContinuousListener);
   }
   

   /**
     * Installs clients that will check if the poller is still alive and report to the server
     */
   private void installHeartbeatClient(Context ctx, ErPollerConfig config)
   {       
      try
      {
         AgentEntry entry = new AgentEntry();
         String pollerAgentName = getPollerAgentName(config);
         entry.setName(pollerAgentName);
         entry.setAgent(new PollerHeartbeatAgent(pollerAgentName, PollerLifecycleAgent.getPollerThreadName(config), 60));
         entry.setContext(ctx);

         AgentHelper.add(ctx, entry);

         TaskEntry task = new TaskEntry();
         AgentHelper.makeAgentEntryConfig(task, pollerAgentName);
         task.setName(getPollerTaskName(config));
         task.setCronEntry("everyMinute");
         task.setDefaultStatus(TaskStatusEnum.DISABLED);
         task.setStatus(TaskStatusEnum.DISABLED);
         task.setDescription(Constants.POLLERDESC + config.getId());

         TaskHelper.add(ctx, task);
      }
      catch (SchedulerConfigException e)
      {
         new MajorLogMsg(this, e.getMessage(), e).log(ctx);
      }
   }
}
