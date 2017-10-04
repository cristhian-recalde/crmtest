/*
   AccountPaymentAction

   Copyright (c) 2003, Redknee
   All rights reserved.

     Date          Author      Changes
   ----------     --------     -------
   Nov 11, 03     Lily Zou     Created
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
 * Link to New Transaction screen from Account screen.
 **/
public class AccountPaymentAction
   extends SimpleWebAction
{
   
   public AccountPaymentAction()
   {
      super("pay", "Adjustment");
   }
   
   
   public AccountPaymentAction(Permission permission)
   {
      this();
      setPermission(permission);
   }
   
   
   public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
   {
      if ( bean instanceof Account)
      {
         Account account = (Account) bean;
        
         link.remove("key");
         link.remove("query");
         link.addRaw(".MSISDN",  "0");
         link.addRaw(".BAN", String.valueOf(account.getBAN()));
         link.addRaw("cmd",      "AppCrmTransactionHistory");
         link.addRaw("CMD",      "New");
       
         link.writeLink(out, getLabel());
      }
      // this is for the subscriber profile retrieved from account view
      else if ( bean instanceof Subscriber )
      {
            new SubscriberPaymentAction().writeLink(ctx, out, bean, link);
      }
      else
      {
            return;
      }
   }
}
