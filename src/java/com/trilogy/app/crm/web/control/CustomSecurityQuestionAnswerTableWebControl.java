
package com.trilogy.app.crm.web.control;

/*
    SecurityQuestionAnswerTableWebControl

    Author : Kevin Greer (via xgen)
    Date   : Fri Oct 10 10:45:59 EDT 2008

    Copyright (c) Redknee Inc. (Migreated for testing purposes), 2006
        - all rights reserved
*/


import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebController;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletRequest;
import javax.servlet.http.*;
import javax.script.ScriptException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerIdentitySupport;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerWebControl;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerXInfo;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.framework.core.scripting.JSchemeExecutor;
import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.webcontrol.*;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.web.action.*;
import com.trilogy.framework.xhome.web.support.*;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.*;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xlog.log.LogSupport;

public class CustomSecurityQuestionAnswerTableWebControl
    extends    AbstractTableWebControl
{

  protected final static WebControl control__ = new SecurityQuestionAnswerWebControl();


  ////////////////////////////////////////////////////////// INSTANCE VARIABLES

  
  public static final WebControl question_wc = new TextFieldWebControl(30, 50);
  public static final WebControl answer_wc = new TextFieldWebControl(30, 50);


  ////////////////////////////////////////////////////////// CONSTRUCTOR

  public CustomSecurityQuestionAnswerTableWebControl()
  {
  }


   /////////////////////////////////////////////////////////////////////////
   //                                                         METHODS
  /////////////////////////////////////////////////////////////////////////

  
  public WebControl getQuestionWebControl() { return question_wc; }
  
  public WebControl getAnswerWebControl() { return answer_wc; }
  

  public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
  {

      Context    subCtx    = ctx.createSubContext();
      Context    secureCtx = subCtx;
      MessageMgr mmgr      = new MessageMgr(ctx, this);


     
    //In table mode so set the TABLE_MODE to true.  Used by individual web controls
    subCtx.put("TABLE_MODE", true);

    int mode = ctx.getInt("MODE", DISPLAY_MODE);

    if ( mode != DISPLAY_MODE )
    {
        secureCtx = subCtx.createSubContext();
        secureCtx.put("MODE", DISPLAY_MODE);
    }

   HttpServletRequest req      = (HttpServletRequest) ctx.get(HttpServletRequest.class);
   int                blanks   = 1;
   Collection         beans    = (Collection) obj;
   TableRenderer      renderer = tableRenderer(ctx);
   // get the list of common actions
   List               actions  = ActionMgr.getActions(ctx);

   
   // The check for ACTIONS is for legacy support and should be removed at some point
   boolean            show_actions = ctx.getBoolean("ACTIONS", true) && ActionMgr.isEnabled(actions);

   // don't propogate ACTIONS to sub-controls
   if ( show_actions )
   {
      ActionMgr.disableActions(subCtx);
   }

   Account account = null;
   
   if ( ctx.get(BEAN) instanceof ConvertAccountBillingTypeRequest)
   {
       ConvertAccountBillingTypeRequest conversion = (ConvertAccountBillingTypeRequest) ctx.get(BEAN);
       account = (Account) ctx.get(Account.class);
       
   }
   else
   {
       account = (Account) ctx.get(BEAN);
   }
   
   CRMSpid sp = getSpidProperty(ctx, account.getSpid());
   final int count = Math.max(sp.getMinNumSecurityQuestions(), beans.size() + ((( mode == EDIT_MODE || mode == CREATE_MODE ) && !ctx.getBoolean(ENABLE_ADDROW_BUTTON))? Math.max(0, blanks) : 0));

   if ( mode == EDIT_MODE || mode == CREATE_MODE )
   {
      // The Math.max() bit is so that if blanks is set to 0 that you can still add a row
      out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR + "_count\" value=\"" + count + "\" />");
      if(ctx.getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
          out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR + "_REORDER_KEY\" value=\"1\" />");
      else      
          out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR + "_REORDER_KEY\" value=\"0\" />");
   }

   // WIDHT=722

   renderer.Table(ctx,
      out,
      mmgr.get("SecurityQuestionAnswer.Label", SecurityQuestionAnswerXInfo.Label));

   out.println("<tr>");

   if ( mode == EDIT_MODE || mode == CREATE_MODE )
   {
       
       if(ctx.getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
       {
          // this is for the up/down arrows
          out.print("<th>&nbsp;</th>");
       }
       

       // this is for the checkbox, only if dynamic table update is not enabled
       if(!ctx.getBoolean(ENABLE_ADDROW_BUTTON))
            out.print("<th>&nbsp;</th>");
   }

   Link link = null;

   // default
   String img_src = "";

   
    ViewModeEnum question_mode             = getMode(subCtx, "SecurityQuestionAnswer.question");
    ViewModeEnum answer_mode               = getMode(subCtx, "SecurityQuestionAnswer.answer");
   if ( question_mode != ViewModeEnum.NONE )
   {
      link = new Link(ctx);

      // only toggle the sort order if this is the currently selected field
      if ( "Question".equals(req.getParameter("orderBy")) )
      {
         String order = req.getParameter("order");

         if ( order == null || "ASC".equals(order) )
         {
            link.add("order", "DESC");
         }
         else
         {
            link.remove("order");
         }
      }

      link.copy(req, SimpleSearchBorder.DEFAULT_NAME);
      link.add("orderBy", "Question");

      

      out.println("<th>"+         mmgr.get(
     "SecurityQuestionAnswer.question.Label",
     SecurityQuestionAnswerXInfo.QUESTION.getLabel(ctx)));
   out.println("</th>");
    

   }
    
   if ( answer_mode != ViewModeEnum.NONE )
   {
      link = new Link(ctx);

      // only toggle the sort order if this is the currently selected field
      if ( "Answer".equals(req.getParameter("orderBy")) )
      {
         String order = req.getParameter("order");

         if ( order == null || "ASC".equals(order) )
         {
            link.add("order", "DESC");
         }
         else
         {
            link.remove("order");
         }
      }

      link.copy(req, SimpleSearchBorder.DEFAULT_NAME);
      link.add("orderBy", "Answer");

      

      out.println("<th>"+         mmgr.get(
     "SecurityQuestionAnswer.answer.Label",
     SecurityQuestionAnswerXInfo.ANSWER.getLabel(ctx)));
   out.println("</th>");
    

   }
    
    if ( show_actions )
    {
       //out.println("<th>Actions</th>");
       out.println("<th>");
       out.println(mmgr.get("SummaryTable.Actions.Label","Actions"));
       out.println("</th>");
    }
    

    out.println("</tr>");

    Iterator i     = beans.iterator();
    int rowStart=0;

      

        if (( mode == EDIT_MODE || mode == CREATE_MODE ) && ! ctx.has(DISABLE_NEW) && ctx.getBoolean(ENABLE_ADDROW_BUTTON))
    {
      out.print("<tr style=\"display:none\">");

      //For the down only arrow-set
      renderer.TD(ctx,out);
      out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
      out.println("<img src=\"/images/list/up-dark.gif\"></img>");
      out.println("</td></tr><tr><td>");
      out.println("<img onclick=\"swapTableLines(this,-2,-1,'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/down.gif\"></img>");
      out.println("</td></tr></table>");
      renderer.TDEnd(ctx,out);

      //For the up arrow-set
      renderer.TD(ctx, out);
      out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
      out.println("<img onclick=\"swapTableLines(this,-2,-3,'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/up.gif\"></img>");
      out.println("</td></tr><tr><td>");
      out.println("<img src=\"/images/list/down-dark.gif\"></img>");
      out.println("</td></tr></table>");
      renderer.TDEnd(ctx, out);

      //For the bi-directional only arrow-set
      renderer.TD(ctx, out);
      out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
      out.println("<img onclick=\"swapTableLines(this,-2,-3,'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/up.gif\"></img>");
      out.println("</td></tr><tr><td>");
      out.println("<img onclick=\"swapTableLines(this,-2,-1,'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/down.gif\"></img>");
      out.println("</td></tr></table>");
      renderer.TDEnd(ctx, out);

      //For both black arrows
      renderer.TD(ctx, out);
      out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
      out.println("<img src=\"/images/list/up-dark.gif\"></img>");
      out.println("</td></tr><tr><td>");
      out.println("<img src=\"/images/list/down-dark.gif\"></img>");
      out.println("</td></tr></table>");
      renderer.TDEnd(ctx, out);

      out.print("</tr>");
      rowStart=-1;
    }


    final int start = rowStart;


    for (int j = start ; j < count; j++)
    {
      SecurityQuestionAnswer bean;


      boolean b=true;
      if ( j > -1 && j < beans.size() )
      {
         bean = (SecurityQuestionAnswer) i.next();
         b=true;
      }
      else
      {
         bean = new SecurityQuestionAnswer();
         b=false;
      }

      /*if (j < 0)
      {
          out.print("<tr style=\"display:none\">");
      }
      else
      { */
      	renderer.TR(ctx, out, bean, j);
      //}
      
      // icons for up/down
      if ( mode == EDIT_MODE || mode == CREATE_MODE )
      {
        if(ctx.getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
        {
          renderer.TD(ctx, out);
          out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
          if (ctx.getBoolean(ENABLE_ADDROW_BUTTON) && beans.size()==1 && j!= -1)
          {
              //For both black arrows when 1)Normal: NOWAY... 2)Dynamic -- displaying the only one row in table (not the hidden row)
              out.println("<img src=\"/images/list/up-dark.gif\"></img>");
              out.println("</td></tr><tr><td>");
              out.println("<img src=\"/images/list/down-dark.gif\"></img>");
          }
          else if(j==0)
          {
              out.println("<img src=\"/images/list/up-dark.gif\"></img>");
              out.println("</td></tr><tr><td>");
//              out.println("<img onclick=\"swapTableLines('"+name + "','" + SEPERATOR+"',"+j+","+(j+1)+",new Array('_enabled','question','answer'));\" src=\"/images/list/down.gif\"></img>");
              out.println("<img onclick=\"swapTableLines(this,"+j+","+(j+1)+",'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/down.gif\"></img>");
          }
          else if((j==count-1) && (j!= -1) )
          {
              out.println("<img onclick=\"swapTableLines(this,"+j+","+(j-1)+",'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/up.gif\"></img>");
              out.println("</td></tr><tr><td>");
              out.println("<img src=\"/images/list/down-dark.gif\"></img>");
          }
          else
          {
              out.println("<img onclick=\"swapTableLines(this,"+j+","+(j-1)+",'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/up.gif\"></img>");
//              out.println("<img onclick=\"swapTableLines('"+name + "','" + SEPERATOR+"',"+j+","+(j-1)+",new Array('_enabled','question','answer'));\" src=\"/images/list/up.gif\"></img>");
              out.println("</td></tr><tr><td>");
              out.println("<img onclick=\"swapTableLines(this,"+j+","+(j+1)+",'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/down.gif\"></img>");
//              out.println("<img onclick=\"swapTableLines('"+name + "','" + SEPERATOR+"',"+j+","+(j+1)+",new Array('_enabled','question','answer'));\" src=\"/images/list/down.gif\"></img>");
          }
          out.println("</td></tr></table>");
          renderer.TDEnd(ctx, out);
        }
      }
      

      // checkbox: shwon only in edit/create mode and the "AddRow" button is disable
      if (( mode == EDIT_MODE || mode == CREATE_MODE ) && !ctx.getBoolean(ENABLE_ADDROW_BUTTON))
      {
         outputCheckBox(ctx, out, name + SEPERATOR + j, bean, b);
      }

      subCtx.put(BEAN, bean);

      

      if ( question_mode != ViewModeEnum.NONE )
      {
      
         renderer.TD(ctx, out);
         
         // Some nested WebControls may want to know about the particular
         // bean property they are dealing with.
         subCtx.put(PROPERTY, SecurityQuestionAnswerXInfo.QUESTION);
           getQuestionWebControl().toWeb( ( question_mode == ViewModeEnum.READ_ONLY ) ? secureCtx : subCtx, out, name + SEPERATOR + j + SEPERATOR + "question", bean.getQuestion());
         renderer.TDEnd(ctx, out);
      

      }

      

      if ( answer_mode != ViewModeEnum.NONE )
      {
      
         renderer.TD(ctx, out);
         
         // Some nested WebControls may want to know about the particular
         // bean property they are dealing with.
         subCtx.put(PROPERTY, SecurityQuestionAnswerXInfo.ANSWER);
           getAnswerWebControl().toWeb( ( answer_mode == ViewModeEnum.READ_ONLY ) ? secureCtx : subCtx, out, name + SEPERATOR + j + SEPERATOR + "answer", bean.getAnswer());
         renderer.TDEnd(ctx, out);
      

      }

      
            if (show_actions)
            {
                List beanActions = ActionMgr.getActions(ctx, bean);
                if (beanActions == null)
                {
                   // use the backup actions from the home level
                   beanActions = actions;
                }
                ((WebActionSupport) ctx.get(WebActionSupport.class)).writeLinks(subCtx, beanActions, out, bean, SecurityQuestionAnswerIdentitySupport.instance());
            }
      
      if (( mode == EDIT_MODE || mode == CREATE_MODE ) && ! ctx.getBoolean(DISABLE_NEW) && ctx.getBoolean(ENABLE_ADDROW_BUTTON))
      {
          out.println("<TD>");
          out.println("<img onclick=\"removeRow(this,'"+name+"');\" src=\"ButtonRenderServlet?.src=abc &.label=Delete\"/>");
          //out.println("<input type=\"button\" name=\"Delete\" value=\"Delete\" onclick=\"removeRow(this,'"+name+"');\" />");
          out.println("</TD>");
      }
      renderer.TREnd(ctx, out);
    }
    if (( mode == EDIT_MODE || mode == CREATE_MODE ) && ctx.getBoolean(ENABLE_ADDROW_BUTTON) && !ctx.getBoolean(DISABLE_NEW))
    {
      ButtonRenderer        br           = (ButtonRenderer)ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
      out.println("<tr><td colspan=4>");
      
            out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
      
      
      out.println("<input type=\"text\" name=\""+name+".addRowCount\" size=\"3\" value=\"1\"/></td><td>");
      out.println("<img onclick=\"addRow(this,'"+name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"ButtonRenderServlet?.src=abc &.label=Add\"/>");
      out.println("</td></tr></table>");
      out.println("</td></tr>");
      //br.inputButton(out, ctx, WebController.class, "Help", false, "addRow(this,'"+name+"')");
      //out.println("</td>");
      //out.println("<tr ><td><input type=\"button\" value=\"Add Row\" onclick=\"addRow(this,'"+name+"');\" /></td><td><input type=\"text\" name=\""+name+".addRowCount\" value=\"1\"/></td></tr>");
    }

    renderer.TableEnd(ctx, out, (String) ctx.get(FOOTER));

    outputNewButton(ctx, out, name, beans, blanks, mode);

    // out.println("<center><font color=\"white\">" + beans.size() + " of " + beans.size() + " shown</font></center>");

  }


  public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
  {
      
     try
     {
        Collection beans = (Collection) obj;
        int        count = Integer.parseInt(req.getParameter(name + SEPERATOR + "_count"));

        for ( int i = 0 ; i < count ; i++ )
        {
           
           String hidden = req.getParameter(name + SEPERATOR + i + SEPERATOR + "SecurityQuestionAnswer.hidden");
           if (hidden != null)
           {
              try
              {
                 beans.add(JSchemeExecutor.instance().retrieveObject(ctx, hidden, ""));
              }
              catch(ScriptException e)
              {
                 // TODO: report
                 new com.redknee.framework.xlog.log.MajorLogMsg(this, e.getMessage(), e).log(ctx);
              }
           }
           else
           {
             String enabled = req.getParameter(name + SEPERATOR + i + SEPERATOR + "_enabled");

             if ( "X".equals(enabled) || ctx.getBoolean(DISABLE_NEW) || (ctx.getBoolean(ENABLE_ADDROW_BUTTON) && hasParameterForName(ctx,req,name + SEPERATOR + i + SEPERATOR) ))
             {
                try
                {
                   beans.add(control__.fromWeb(ctx, req, name + SEPERATOR + i));
                }
                catch (Exception t)
                {
                   // Don't let one bad apple spoil the whole lot, KGR
                   LogSupport.ignore(ctx,this,t.getMessage(),t);
                }
             }
          }
       }
    }
    catch (NumberFormatException e)
    {
    throw new NullPointerException("no data");
      // System.err.println("Unexpected missing counter for " + name);
    }
  }


  public Object fromWeb(Context ctx, ServletRequest req, String name)
  {
    Object obj = new ArrayList();

    fromWeb(ctx, obj, req, name);

    return obj;
  }

    private CRMSpid getSpidProperty(Context ctx, int spid)
      throws IllegalStateException
    {
      Home spidHome = ( Home ) ctx.get(CRMSpidHome.class);
      if (null == spidHome)
      {
          throw new IllegalStateException("System error: CRMSpidHome not found in context");
      }
    
      CRMSpid sp = null;
      try
      {
          sp = (CRMSpid)spidHome.find(ctx, Integer.valueOf(spid));
      }
      catch(Exception e)
      {
          throw new IllegalStateException(e);
      }
    
      if(null == sp)
      {
          throw new IllegalStateException("No SPID configuration defined for SPID [" + spid + "]");
      }
      return sp;
    }
}