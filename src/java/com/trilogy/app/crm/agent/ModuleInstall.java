/*
 *  AuthInstall.java
 *
 *  Author : Kevin Greer
 *  Date   : Aug 28, 2003
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.agent;


import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.CritLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.module.ModuleManager;


/**
 * This class installs the modules
 * @author     Kumaran
 */
public class ModuleInstall
   extends    CoreSupport
   implements ContextAgent
{
   /**
    * Installs the modules
    * system. 
    * @param ctx context where the components will be installed
    * @throws AgentException 
    */
   public void execute(Context ctx)
      throws AgentException
   {
      try
      {
          new InfoLogMsg(this,"Install modules",null).log(ctx);
          try
          {
              ModuleManager.loadModules(ctx);
          }
          catch (HomeException e)
          {
              LogSupport.minor(ctx, this, "Loading Modules Failed", e);
          }


      }
      catch (Throwable t)
      {
         new CritLogMsg(this, "fail to install AppCrm AuthInstall ["+t.getMessage()+"]", t).log(ctx);
      }
   }

}

