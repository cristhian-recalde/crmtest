/*
 * Created on Jan 26, 2004
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFCDRED
 * BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */

package com.trilogy.app.crm.provision.corba;

import java.security.Permission;
import java.security.Principal;

import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author psperneac
 */
public class ECareServiceFactoryImpl
   extends ECareServiceFactoryPOA
   implements ContextAware
{
   protected Context context;
   protected Permission permission_ =
      new SimplePermission(Common.PROVISIONERS_GROUP_PERMISSION);

   /**
    * Constructor. Saves the context locally.
    * 
    * @param ctx
    */
   public ECareServiceFactoryImpl(Context ctx)
   {
      super();
      setContext(ctx);
   }

   /**
    * @see com.redknee.framework.xhome.context.ContextAware#getContext()
    */
   public Context getContext()
   {
      return context;
   }

   /**
    * @see com.redknee.framework.xhome.context.ContextAware#setContext(com.redknee.framework.xhome.context.Context)
    */
   public void setContext(Context context)
   {
      this.context = context;

   }

   /**
    * @param login
    * @param passwd
    * @return 
    * @throws AuthError
    * 
    * @see com.redknee.app.crm.provision.corba.ECareServiceFactoryOperations#createEcareService(java.lang.String,java.lang.String)
    */
   public ECareService createEcareService(String login, String passwd)
   {
      String uid = authenticateUser(login, passwd);

      ECareServiceImpl servant = new ECareServiceImpl(getContext(), uid);
      try
      {
         org.omg.CORBA.Object obj =
            _default_POA().servant_to_reference(servant);
         return ECareServiceHelper.narrow(obj);
      }
      catch (ServantNotActive e)
      {
         new MinorLogMsg(this, e.getMessage(), e).log(getContext());
      }
      catch (WrongPolicy e)
      {
         new MinorLogMsg(this, e.getMessage(), e).log(getContext());
      }

      return null;
   }

   /**
    * checks if the combination login password actually works and if that user 
    * has the permission to provision the system.
    * 
    * @param login
    * @param passwd
    * @return
    */
    private String authenticateUser(String login, String passwd)
    {
        try
        {
            AuthMgr auth = new AuthMgr(getContext().createSubContext());
            auth.login(login, passwd);
            if (auth.check(permission_))
            {
                return login;
            }
        }
        catch (LoginException e)
        {
        }
        return null;
    }
}
