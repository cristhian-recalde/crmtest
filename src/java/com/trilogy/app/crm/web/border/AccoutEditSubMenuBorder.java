/*
 *  AccoutEditSubMenuBorder.java
 *
 *  Author : lzou
 *  Date   : Mar 07, 2005
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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */ 
 
package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xhome.web.border.*;
import com.trilogy.framework.xhome.menu.*;
import com.trilogy.framework.xhome.web.xmenu.XMenuConfig;
import com.trilogy.framework.xhome.web.xmenu.border.SubMenuBorder;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.web.renderer.*;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.*;

/**
 * A Border for displaying a submenu vertically.
 **/
public class AccoutEditSubMenuBorder
   extends SubMenuBorder      
{
   
   public AccoutEditSubMenuBorder(String parentKey)
   {
      super(parentKey);
   }
   
   
   /** Template method for modifying Link.  Override in subclass as required. **/
   public void modifyLink(Context ctx,  Link link)
   {
      final ButtonRenderer brend = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());

       if ( brend.isButton(ctx, "Search") )
       {
             link.copy(ctx, ".search.BAN");
             link.copy(ctx, ".search.firstName");
             link.copy(ctx, ".search.lastName");
             link.copy(ctx, ".search.companyName");
             link.copy(ctx, ".search.IMSI");
             link.copy(ctx, "SearchCMD.x");
        }
   }


///////////////////////////////////////////// impl Border
   /*
   public void service(Context ctx, final HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
     throws ServletException, IOException
   {
      final PrintWriter out = res.getWriter();
      
      out.println("<table width=100%><tr><td valign=top>");
      
      Home home = (Home) ctx.get(XMenuHome.class);
      
      try
      {
         Collection col = home.where(ctx, new Predicate() { public boolean f(Context ctx, Object obj)
           throws AbortVisitException
         {
            XMenu menu = (XMenu) obj;
            
            return getParentKey().equals(menu.getParentKey());
         }
      }).selectAll();

      String cmd = req.getParameter("cmd");

      // need to hide subscriber management menus if account information is concerned
      // this implementation definitely needs to be improved upon submenu handling feature gets implemented in FW
      if ( cmd != null &&
              (cmd.equals("SubMenuAccountEdit") ||  cmd.equals("SubMenuAccountView") ||
                    cmd.equals("SubMenuAccountNotes") || cmd.equals("SubMenuAccountTransHistory") ||
                        cmd.equals("SubMenuAccountAdjment") || cmd.equals("SubMenuAccountInvoices")  ||
                            cmd.equals("SubMenuAccountTicket") || cmd.equals("SubMenuAccountHistory") ||
                                cmd.equals("SubMenuAccountTopology") || cmd.equals("AppCrmTransactionHistory")))
      {
            Collection subCol = new ArrayList();
        
            for ( Iterator i = col.iterator(); i.hasNext();)
            {
                XMenu menu = (XMenu)i.next();
                if ( menu.getKey().equals("SubMenuSubscriberMgm"))
                {
                    break;
                }
                else
                {
                    System.out.println("added: " + menu.getLabel());
                    subCol.add(menu);
                }
            }

            col = subCol;
      }
      
      out.println("<ul>");
      
      // I would have just called forEach() except then I would lose the
      // proper sort-order
      for ( Iterator i = col.iterator() ; i.hasNext() ; )
      {
         XMenu menu = (XMenu) i.next();
         
         out.print("<li>");
         
         if ( menu.getKey().equals(req.getParameter("cmd")) )
         {
            out.print("<b>");
            out.print(menu.getLabel());
            out.println("</b>");
         }
         else
         {
            XMenuConfig mcfg = menu.getConfig();
            if ( mcfg instanceof XMenuLinkConfig ) // Payment/Adjustment 
            {
                final ButtonRenderer brend   = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());

                out.print("<a href=\"");
                out.print(((XMenuLinkConfig)menu.getConfig()).getUrl());

                if ( brend.isButton(ctx, "Search") )
                {
                    out.print("&");
                    out.print(".search.BAN=" + req.getParameter(".search.BAN"));
                }
                
                out.print("\">");
                out.print(menu.getLabel());
                out.print("</a>");
            }
            else
            {
                Link link = new Link(ctx);
                link.add("cmd", menu.getKey());
            
                modifyLink(ctx, link);
            
                link.writeLink(out, menu.getLabel());
            }
         }
            
         out.println("</li>");
      }
      
      }
      catch (HomeException e)
      {
         // nop  
      }
      out.println("</ul>");
      
      out.println("</td><td align=left>");
      delegate.service(ctx, req, res);
      out.println("</td></tr></table>");
   }
   */
   
}


