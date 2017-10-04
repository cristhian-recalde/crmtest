
/*
   SwitchSubAction
   
   Copyright (c) 2003, Redknee
   All rights reserved.

   Date        Author          Changes
   ----        ------          -------
   Nov 03      Lily Zou        Created
*/

package com.trilogy.app.crm.web.action;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.web.action.*;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.util.Link;
import java.io.PrintWriter;
import java.security.Permission;
import java.security.Principal;
import java.util.*;


/**
 * Link to  Call Detail screen from Account screen.
 **/
public class SwitchSubAction
   extends SimpleWebAction
{
   
   public SwitchSubAction()
   {
      super("switchSub", "Switch Account");
   }
   
   
   public SwitchSubAction(Permission permission)
   {
      this();
      setPermission(permission);
   }
   
   
   public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
   {
   	  link.remove("key");
   	  link.remove("query");
      link.addRaw("cmd",  "appCRMMoveSubMenu");
      link.addRaw("mode", "Edit");
      //link.addRaw("SearchCMD.x",  "13"); // This shouldn't be required, maybe bug in DefaultButtonRenderer

      if ( bean instanceof Subscriber)
      {
         Subscriber sub = (Subscriber) bean;
         
         link.addRaw(".subscriberId", String.valueOf(sub.getId()));
         link.addRaw(".oldAccountBAN", String.valueOf(sub.getBAN()));
      }
      else
      {
         return;
      }
         
      link.writeLink(out, getLabel());
   }
}
