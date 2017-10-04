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


import java.io.IOException;

import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.UserStorageTypeEnum;
import com.trilogy.app.crm.home.CRMGroupHomePipelineFactory;
import com.trilogy.app.crm.xhome.visitor.HomeMigrationVisitor;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.auth.bean.UserHome;
import com.trilogy.framework.xhome.auth.bean.UserXDBHome;
import com.trilogy.framework.xhome.auth.bean.UserXMLHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xlog.log.CritLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.core.home.PMHome;


/**
 * This class installs the users, groups and the authentication service of the app.
 * @author     Kevin Greer
 * @since    Aug 28, 2003
 */
public class AuthInstall
   extends    CoreSupport
   implements ContextAgent
{
   /**
    * Installs the users, the groups and our custome Ciphered authentication
    * system. 
    * @param ctx context where the components will be installed
    * @throws AgentException 
    */
   public void execute(Context ctx)
      throws AgentException
   {
      try
      {
          installCRMGroupHome(ctx);
          installCRMUserHome(ctx);
      }
      catch (Throwable t)
      {
         new CritLogMsg(this, "fail to install AppCrm AuthInstall ["+t.getMessage()+"]", t).log(ctx);
      }
   }

   /**
    * 
    * @param ctx
 * @throws HomeException 
 * @throws HomeInternalException 
    */
    public void installCRMUserHome(final Context ctx) throws HomeInternalException, HomeException
    {
        // Need to do that because we need to migrate in case the home was switch from XDB to Journal (or vice-versa) 
        GeneralConfig genConfig = (GeneralConfig) CoreSupport.bindBean(ctx, GeneralConfig.class);
        UserStorageTypeEnum storageType = genConfig.getUserStorageMechanism();
        LogSupport.info(ctx, this, "Checking User Profiles for migration to :"+ storageType.getDescription());
        
        // Original Journal Home
        Home jHome = CoreSupport.bindHome(ctx, User.class);
        if(LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, this, "Original (FWK) Journal Home for User: "+ jHome);
        
        // XDB Home
        Home xdbHome = new UserXDBHome(ctx, "CRMUSER");
        if(LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, this, "Journal Home for User: "+ jHome);
        
        
        Home migrationTmpHome = new UserXMLHome(ctx, CoreSupport.getFile(ctx, "User_Migration_Failed.xml"));
        
        Home src=null, dest=null;
        
        switch(storageType.getIndex())
        {
            case UserStorageTypeEnum.Journal_INDEX:
                src = xdbHome;
                Context subCtx = ctx.createSubContext();
                subCtx.put(UserHome.class, null);
                // Fresh journal Home...
                dest = CoreSupport.bindHome(subCtx, User.class);
                
                Homes.copy(jHome, dest, false, false);
                break;
            
            case UserStorageTypeEnum.Database_INDEX:
            default:
                src = jHome;
                dest = xdbHome;
                ;
        }
        
        /*
         * Migrate from Src to Dest
         */
        HomeMigrationVisitor v = new HomeMigrationVisitor(src, dest, migrationTmpHome, false, true);
        src.forEach(ctx, v);
        
        if(migrationTmpHome.selectAll(ctx).isEmpty())
        {
            // Successful!
            migrationTmpHome.drop();
        }
        
        com.redknee.framework.auth.spi.Install.setUserHome(ctx, dest);
        final Home newHome = (Home)ctx.get(UserHome.class);
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, ":: UserHome : "+ newHome);
            LogSupport.debug(ctx, this, ":: #Users in Home : "+ newHome.selectAll().size());
        }
        
        ctx.put(UserHome.class, new PMHome(ctx, "User Creation And Access", newHome));
    } 

/**
 * @param ctx
 * @throws HomeException
 * @throws IOException
 * @throws AgentException
 */
public void installCRMGroupHome(Context ctx) throws HomeException, IOException,
        AgentException
{
    // [CW] CRMGroup is clustered by all
      Home groupHome = new CRMGroupHomePipelineFactory().createPipeline(ctx, null);
      
      ctx.put(CRMGroupHome.class, groupHome);

     com.redknee.framework.auth.spi.Install.setGroupHome(
        ctx,
        new AdapterHome(groupHome, new Adapter()
        {
           /**
    		 * 
    		 */
    		private static final long serialVersionUID = -9192137567434116839L;

    		/** Used by find and select methods. **/
           public Object adapt(Context ctx11,Object obj)
              throws HomeException
           {
              return (Group) obj;
           }

           /** Used by create and store methods. **/
           public Object unAdapt(Context ctx11,Object obj)
              throws HomeException
           {
              throw new HomeException("GroupHome is Read-Only, use CRMGroup instead!");
           }
        }));
}

}

