/*
 *  AccountNotesNewBorder
 *
 *  Author : Lily Zou
 *  Date   : Feb.22, 2005
 *  
 *  Copyright (c) 2005, Redknee
 *  All rights reserved.
 */
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.Note;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;


/**
 * A Border for the AccountNotes screen which pre-sets the Account BAN
 * to that of the Account BAN which was searched for.
 * 
 */
public class AccountNotesNewBorder
    implements Border
{
   
   /** Key into Session Context for remembering previous Subscriber Id. **/
   public final Object ACCT_BAN = "AccountNotesNewBorder.BAN";
   
   
   public AccountNotesNewBorder()
   {
   }
   
   
   public void service(
            Context             ctx,
      final HttpServletRequest  req,
            HttpServletResponse res,
            RequestServicer     delegate)
      throws ServletException, IOException
   {
      ctx = ctx.createSubContext();
      
      String id = req.getParameter(".search.BAN");
      
      if ( id != null )
      {
         WebAgents.getSession(ctx).put(ACCT_BAN, id);
      }
      
      if ( ctx.has(ACCT_BAN) )
      {
         XBeans.putBeanFactory(ctx, Note.class, new ContextFactory()
         {
            public Object create(Context ctx)
            {
               Note note = new Note();
               
               note.setIdIdentifier((String) ctx.get(ACCT_BAN));
               
               return note;
            }
         });
      }

      delegate.service(ctx, req, res);
   }
   
}

