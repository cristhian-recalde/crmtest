/*
 *  SubscriberNotesNewBorder
 *
 *  Author : Kevin Greer
 *  Date   : Jan 26, 2004
 *  
 *  Copyright (c) 2004, Redknee
 *  All rights reserved.
 */
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.Note;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;


/**
 * A Border for the SubscriberNotes screen which pre-sets the Subscriber Id
 * to that of the Subscriber Id which was searched for.
 * 
 * @author Kevin Greer
 */
public class SubscriberNotesNewBorder
    implements Border
{
   
   /** Key into Session Context for remembering previous Subscriber Id. **/
   public final Object SUB_ID = "SubscriberNotesNewBorder.SubscriberId";
   
   
   public SubscriberNotesNewBorder()
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
      
      String id = req.getParameter(".search.subscriberId");
      
      if ( id != null )
      {
         Session.getSession(ctx).put(SUB_ID, id);
      }
      
      if ( ctx.has(SUB_ID) )
      {
         XBeans.putBeanFactory(ctx, Note.class, new ContextFactory()
         {
            public Object create(Context _ctx)
            {
               Note note = new Note();
               
               note.setIdIdentifier((String) _ctx.get(SUB_ID));
               
               return note;
            }
         });
      }

      delegate.service(ctx, req, res);
   }
   
}

