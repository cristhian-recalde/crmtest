/*
 *  AccoutViewSubMenuBorder.java
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
import com.trilogy.framework.xhome.web.xmenu.border.SubMenuBorder;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * A Border for displaying a submenu vertically.
 **/
public class AccoutViewSubMenuBorder 
    extends SubMenuBorder    
{
   
   public AccoutViewSubMenuBorder(String parentKey)
   {
      super(parentKey);
   }
   
   
   /** Template method for modifying Link.  Override in subclass as required. **/
   public void modifyLink(Context ctx, Link link)
   {
        // http://jubjub:9260/AppCrm/home?cmd=appCRMAccountMenu&key=0107&action=view
 /*       
       java.util.Enumeration reqEnums = req.getParameterNames();

       for (Enumeration e = req.getParameterNames() ; e.hasMoreElements() ;) 
       {
            String obj = (String)e.nextElement();
            System.out.println( obj + "  : " + req.getParameter(obj));
       }
*/
       HttpServletRequest     req   = (HttpServletRequest)  ctx.get(HttpServletRequest.class);
       HttpSession  session         = req.getSession();

       if ( session.getAttribute("ACCOUNT_KEY") != null )
       {
            link.add("action", "view");
            
            link.add("key", req.getParameter("key"));
       }
   }
}


