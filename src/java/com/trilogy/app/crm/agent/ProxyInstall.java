/*
    ProxyInstall

    Author : Kevin Greer
    Date   : Jan 16, 2004

    Copyright (c) Redknee, 2004
        - all rights reserved
*/

package com.trilogy.app.crm.agent;

import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.web.util.IndexServlet;
import com.trilogy.framework.xhome.web.service.RemoteRequestServicer;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;


/**
 *  Install CRM Proxy.
 *  
 *  To use replace the regular Application.script with:
 *     new com.redknee.app.crm.agent.ProxyInstall("http://CORE_SERVER:9260/AppCrm/home?uri=http://PROXY_SERVER:9260/AppCrm/home").execute(ctx);
 *  where PROXY_SERVER is that name of the machine that the proxy is running on
 *  and CORE_SERVER is the real server.
 */
public class ProxyInstall
   implements ContextAgent, RequestServicer
{

    protected String server_;
   
    /**
     * 
     */
    public ProxyInstall(String server)
    {
       server_ = server;
    }

    
   ///////////////////////////////////////////////////// Impl ContextAgent
   
    public void execute(Context ctx)
      throws AgentException
    {
       ctx.put(IndexServlet.REQUEST_SERVICER_KEY, this);
    }
    
    
   ///////////////////////////////////////////////////// Impl RequestServicer
   
   public void service(Context ctx, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      HttpSession    session = req.getSession();
      StringBuilder   buf     = new StringBuilder(server_);

      if ("previewPDF".equals(req.getParameter("action")))
      {
          // This is a special requirement of the Invoice preview -- the context
          // type will be PDF, not HTML.
          res.setContentType("application/pdf");
      }
      else
      {
          res.setContentType("text/html");
      }
      
      // Let them know that this request is coming from the ECare site
      buf.append("&_ecare=y");
      
      // Logout
      if ( "Logout".equals(req.getParameter("cmd")) )
      {
         session.removeAttribute("username");
         session.removeAttribute("password");
      }
      else
      {
         // Login
         if ( req.getParameter("username") != null )
         {
            session.setAttribute("username", req.getParameter("username"));
         }
         
         if ( req.getParameter("password") != null )
         {
            session.setAttribute("password", req.getParameter("password"));
         }
      
         // Session Management
         if ( session.getAttribute("username") != null )
         {
            buf.append("&username=");
            buf.append(session.getAttribute("username"));
         }
         
         if ( session.getAttribute("password") != null )
         {
            buf.append("&password=");
            buf.append(session.getAttribute("password"));
         }
      }

      new RemoteRequestServicer(buf.toString()).service(ctx, req, res);
      PrintWriter out = res.getWriter();
      out.flush();
   }

    
}
