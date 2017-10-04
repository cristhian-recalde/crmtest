package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.Common;

public class ReasonSelectWebControl
   extends PrimitiveWebControl
{   
    public static String SUSPENDED_KEY  = "Suspended";
    public static String INACTIVE_KEY   = "Inactive";
    public static int    size_          = 1;

    public ReasonSelectWebControl()
    {
        super();
    }
    
   /**
    *  Description of the Method
    *
    *@param  ctx   Description of Parameter
    *@param  out   Description of Parameter
    *@param  name  Description of Parameter
    *@param  obj   Description of Parameter
    */
   public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
   {
      int  mode = ctx.getInt("MODE", DISPLAY_MODE);
      String reason  = obj.toString();
      List   reasons = new ArrayList();
        
      Home noteTypeHome = ( Home ) ctx.get(NoteTypeHome.class);
      Account account = (Account)ctx.get(AbstractWebControl.BEAN);
      
      
      
      NoteType noteType = null;
    
      if (account != null )
      {
          if(LogSupport.isDebugEnabled(ctx))
        {   
           new DebugLogMsg(this,"------------->" + account.getBAN(),null).log(ctx);
        }
          
          if ( AccountStateEnum.INACTIVE.equals( account.getState() ))
          {    
              try
              {
                     noteType = ( NoteType)noteTypeHome.find(ctx,INACTIVE_KEY);
              }
              catch(HomeException e)
              {
              }
          }
          else if ( AccountStateEnum.SUSPENDED.equals(account.getState()))
          {
              try
              {
                     noteType = ( NoteType )noteTypeHome.find(ctx,SUSPENDED_KEY);
              }
              catch(HomeException e)
              {
              }
          }
      }

      if ( noteType != null )
      {
          reasons = noteType.getReasons();
      }      

      // TODO: find out last note and display it ( that value should be reason there )
      Home noteHome = (Home) ctx.get(Common.ACCOUNT_NOTE_HOME);    
      
      if ( noteHome  != null )
      {
            //Note latestNote = findLatestNote(ctx, account);
      }
        
      
      switch (mode)
      {
         case EDIT_MODE:
         case CREATE_MODE:
            out.println("<select name=\"" + name + "\" size=\"" + size_ + "\">");
            
            for ( Iterator i = reasons.iterator() ; i.hasNext() ; )
            {
               Reason r = (Reason)i.next();

               out.print("<option value=\""+ r.getReasoncode()+"\"");
               if ( r.getReasoncode().equals(reason) )
               {
                  out.print(" selected=\"selected\"");
               }
               out.println(">" + r.getDesc() + "</option>");
            }
            out.println("</select>");
         break;

         case DISPLAY_MODE:
         default:
            out.print(obj.toString());
      }
   }


       public Object fromWeb(Context ctx, ServletRequest req, String name)
           throws NullPointerException
       {
           if ( req.getParameter(name) == null )
           {
               throw new NullPointerException("Null Select Value");
           }
      
           //returns an Enum object containing the index and description
           return req.getParameter(name);
       }
   
   protected Note findLatestNote(Context ctx, Account account)
   {
        return new Note(); 
   }
}
