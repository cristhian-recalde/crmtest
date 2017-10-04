/*
*  AccountPathBorder.java
*
*  Author : kgreer
*  Date   : Apr 07, 2005
*
* This code is a protected work and subject to domestic and international
* copyright law(s). A complete listing of authors of this work is readily
* available. Additionally, source code is, by its very nature, confidential
* information and inextricably contains trade secrets and other information
* proprietary, valuable and sensitive to Redknee, no unauthorised use,
* disclosure, manipulation or otherwise is permitted, and may only be used
* in accordance with the terms of the licence agreement entered into with
* Redknee Inc. and/or its subsidiaries.
*
* Copyright ? Redknee Inc. and its subsidiaries. All Rights Reserved.
*/ 
 
package com.trilogy.app.crm.web.acctmenu;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;


/**
* Border or RequestServicer to display path of current Account and ancestor Accounts.
*
* @author  kgreer
**/
public class AccountPathBorder
   extends    WebAgents
   implements Border, RequestServicer
{

   public final static String ACCOUNT_IMAGE    = "/images/org/javalobby/icons/16x16/Folder.gif";
   public final static String SUBSCRIBER_IMAGE = "/images/org/javalobby/icons/16x16/User.gif";
   
   
   public AccountPathBorder()
   {
   }
   
   
   ///////////////////////////////////////////// impl RequestServicer
   
   public void service(Context ctx, HttpServletRequest req, HttpServletResponse res)
     throws ServletException, IOException
   {
      Account    account  = (Account) getSession(ctx).get(Account.class);
      LinkedList accounts = new LinkedList();
      Home       home     = (Home) ctx.get(AccountHome.class);
      PrintWriter out  = getWriter(ctx);
      Link        link = new Link(ctx);
      out.println("<div align=\"left\">");
      if ( account != null )
      {
         try
         {
            account = (Account) home.find(account);
         }
         catch (HomeException e)
         {
            // doesn't matter
         }
      }
      
      // Refresh account from Home in case it has updated
      // This will happen after an account has been moved
      //
      // BTW: It would be more clever if instead of putting the real 
      // Account in the Context I instead put a ContextFactory
      // which performed the refresh for me.
      if ( account != null )
      {
         try
         {
            account = (Account) home.find(account);
         }
         catch (HomeException e)
         {
            // doesn't matter
         }
      }
      
      while ( account != null )
      {
         accounts.addFirst(account);
         
         String parent = account.getParentBAN();
         
         if ( parent == null )
         {
            break;
         }
         
         try
         {
            account = (Account) home.find(parent);
         }
         catch (HomeException e)
         {
            break;  
         }
      }

      if ( accounts.size() > 0 )
      {
       
         
//         out.print("<font size=\"+1\">");
         
         // TODO: make cmd configurable
         link.add("cmd", "SubMenuAccountEdit");
         
         out.print("<img src=\"" + ACCOUNT_IMAGE + "\"/>&nbsp;");
         
         for ( Iterator i = accounts.iterator() ; i.hasNext() ; )
         {
            account = (Account) i.next();
            
            link.add("key", account.getBAN());
            
            out.print(" / ");
            link.writeLink(out, account.getBAN());
         }
         
//         out.print("</font>");

         Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
         
         if ( sub != null )
         {
            out.print(" / <img src=\"" + SUBSCRIBER_IMAGE + "\"/>&nbsp;");

            link.add("key", sub.getId());
            link.add("cmd", "SubMenuSubProfileEdit");
            
            link.writeLink(out, sub.getId());
         }
         
         out.println("<br/><br/></div>");
      }
   }

      
   ///////////////////////////////////////////// impl Border
   
   public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
     throws ServletException, IOException
   {
      service(ctx, req, res);
      
      delegate.service(ctx, req, res);
   }

}



