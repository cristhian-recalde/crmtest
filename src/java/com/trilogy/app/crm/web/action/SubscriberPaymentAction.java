// INSPECTED: 07/10/03 LZOU


/*
   SubscriberPaymentAction

   Copyright (c) 2003, Redknee
   All rights reserved.

   Date        Author          Changes
   ----        ------          -------
   Sept 30 03  Kevin Greer     Created
*/
// INSPECTED: 10/07/2003  GEA
package com.trilogy.app.crm.web.action;

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
 * Link to New Transaction screen from Subscriber screen.
 **/
public class SubscriberPaymentAction
   extends SimpleWebAction
{
   
   public SubscriberPaymentAction()
   {
      super("pay", "Adjustment");
   }
   
   
   public SubscriberPaymentAction(Permission permission)
   {
      this();
      setPermission(permission);
   }
   
   
   public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
   {
      try
      {
         Subscriber sub = (Subscriber) bean;
         
         link.remove("key");
         link.remove("query");
         link.addRaw(".MSISDN",  String.valueOf(sub.getMSISDN()));
         link.addRaw(".BAN", String.valueOf(sub.getBAN()));
         link.addRaw("cmd",      "AppCrmTransactionHistory");
         link.addRaw("CMD",      "New");
         
         link.writeLink(out, getLabel());
      }
      catch (ClassCastException e)
      {
         // This is expected to happen when the Action is called from the Parent Account
         // It is designed this way because it is easier to register with the Account
         // WebController through XMenus that it is with the embedded WebControllerWebControl
         // Because the ActionMgr is inherited in the Context they will both get it but we
         // just ignore it for Accounts (via the ClassCastException).
      }
   }
}
