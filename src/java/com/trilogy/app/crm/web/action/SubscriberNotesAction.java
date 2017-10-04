
/*
   AccountCallDetailsAction
   
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
public class SubscriberNotesAction
   extends SimpleWebAction
{
   
   public SubscriberNotesAction()
   {
      super("notes", "Notes");
   }
   
   
   public SubscriberNotesAction(Permission permission)
   {
      this();
      setPermission(permission);
   }
   
   
   public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
   {
   	  link.remove("key");
   	  link.remove("query");
      link.addRaw("cmd",  "appCRMSubscriberNotesMenu");
      link.addRaw("SearchCMD.x",  "13"); // This shouldn't be required, maybe bug in DefaultButtonRenderer

      if ( bean instanceof Subscriber)
      {
         Subscriber sub = (Subscriber) bean;
         
         link.addRaw(".search.subscriberId", String.valueOf(sub.getId()));
      }
      else
      {
         return;
      }
         
      link.writeLink(out, getLabel());
   }
}
