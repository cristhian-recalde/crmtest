/*
 *  SubscriberEditSubMenuBorder.java
 *
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
import com.trilogy.framework.xhome.web.util.PopupLink;
import com.trilogy.framework.xhome.web.renderer.*;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.framework.xlog.log.*;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.support.*;

import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.*;

/**
 * A Border for displaying a submenu vertically.
 *
 * @author lzou
 **/
public class SubscriberEditSubMenuBorder  extends SubMenuBorder      
{
   public SubscriberEditSubMenuBorder(String parentKey)
   {
      super(parentKey);
   }
   
   /** Template method for modifying Link.  Override in subclass as required. **/
   public void modifyLink(Context ctx,  Link link)
   {
   }


///////////////////////////////////////////// impl Border
   
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
            Subscriber sub  = null;
            Account    parentAcct  = null;
            
            final ButtonRenderer brend   = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
            
            if ( brend.isButton(ctx, "Search") )
            {
                if ( req.getParameter(".search.MSISDN") != null )
                {
                    try 
                    {
                        sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, req.getParameter(".search.MSISDN"));
                    }
                    catch(HomeException e)
                    {
                        new com.redknee.framework.xlog.log.MinorLogMsg(this.getClass().getName(), "fail to find subscriber with msisdn=" + req.getParameter(".search.MSISDN"), e).log(ctx);
                        sub = null;
                    }
                }

                // try using other parameters from req, like .search.subscriberId for Move Subscriber Menu ITem
                if ( sub == null && req.getParameter(".search.subscriberId") != null )
                {
                    try 
                    {
                        sub = SubscriberSupport.lookupSubscriberForSubId(ctx, req.getParameter(".search.subscriberId"));
                    }
                    catch(HomeException e)
                    {
                        new com.redknee.framework.xlog.log.MinorLogMsg(AccountSupport.class.getName(), "fail to find subscriber with msisdn=" + req.getParameter(".search.subscriberId"), e).log(ctx);
                        //sub = null;
                    }
                }

                if ( sub != null )
                {
                    try
                    {
                        parentAcct = SubscriberSupport.lookupAccount(ctx, sub);
                    }
                    catch(Exception e)
                    {
                        // log
                        parentAcct = null;
                    }
                }

            }
            XMenuConfig mcfg = menu.getConfig();
            if ( mcfg instanceof XMenuLinkConfig ) // Payment/Adjustment 
            {
                out.print("<a href=\"");
                out.print(((XMenuLinkConfig)menu.getConfig()).getUrl());

             //   if ( brend.isButton(ctx, "Search") )
                {
                    out.print("&.MSISDN=" + req.getParameter(".search.MSISDN"));
                    
                    if ( parentAcct != null )
                    {
                        out.print("&.BAN=" + parentAcct.getBAN());
                    }
                    /*
                    if( sub != null )
                    {
                        out.print("&.search.refId=" + sub.getId());
                    } */
                }
                
                out.print("\">");
                out.print(menu.getLabel());
                out.print("</a>");
            }
            else
            {
                Link link = null;
                
                if ( menu.getKey().equals("SubMenuSubTicket"))
                {
                    link = new PopupLink(ctx);
                    link.addRaw("cmd","appTroubleticketBrowse" );
                }
                else
                {
                    link = new Link(ctx);
                    link.addRaw("cmd", menu.getKey());
                }
                
                
                //link.remove("key");
                //link.remove("query");
                //link.addRaw("SearchCMD.x", "11");
                
           //     if ( brend.isButton(ctx, "Search") )
                {
                    if ( sub != null )
                    {
                        link.addRaw(".search.subscriberID" , sub.getId()); // CallDetail, Transactions
                        link.addRaw(".search.subscriberId" , sub.getId()); // Subscrber Notes
                        link.addRaw(".search.refId" , sub.getId() ); // Subscrber Ticket
                        link.addRaw(".existingSubscriberIdentifier", sub.getId()); // Subscriber Move
                        link.addRaw(".search.subId", sub.getId());// Subscriber View
                    }
                    if ( parentAcct != null )
                    {
                        link.addRaw(".search.BAN" , parentAcct.getBAN());
                        link.addRaw(".oldAccountNumber",parentAcct.getBAN() ); // Subscriber Move
                    }

                    link.copy(ctx, "SearchCMD.x");
                }
                
                //modifyLink(ctx, link);
            
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
   
}


