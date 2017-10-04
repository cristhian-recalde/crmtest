
package com.trilogy.app.crm.web.control;

/*
    AccountIdentificationTableWebControl

    Author : Kevin Greer (via xgen)
    Date   : Tue Oct 14 14:32:17 EDT 2008

    Copyright (c) Redknee Inc. (Migreated for testing purposes), 2006
        - all rights reserved
*/


import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsHome;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.AccountIdentificationIdentitySupport;
import com.trilogy.app.crm.bean.account.AccountIdentificationTableWebControl;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.web.action.WebActionSupport;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.SimpleSearchBorder;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.LogSupport;

public class CustomAccountIdentificationTableWebControl
 extends
    AccountIdentificationTableWebControl
{

  public CustomAccountIdentificationTableWebControl()
  {
  }

  @Override
public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
  {
      Context    subCtx    = ctx.createSubContext();
      Context    secureCtx = subCtx;
      MessageMgr mmgr      = new MessageMgr(ctx, this);
      Spid spid = MSP.getBeanSpid(ctx);
      AccountIdentificationGroup accountIdentificationGroup = (AccountIdentificationGroup) ctx.get(BEAN);
      subCtx.put(AccountIdentificationGroup.class, accountIdentificationGroup);

      
      try
      {
          Home spidHome = (Home) ctx.get(SpidIdentificationGroupsHome.class);
          SpidIdentificationGroups identificationGroups =
              (SpidIdentificationGroups) spidHome.find(new EQ(
                  SpidIdentificationGroupsXInfo.SPID, Integer.valueOf(spid.getId())));
          
          if (identificationGroups!=null)
          {
              List<IdentificationGroup> groups = identificationGroups.getGroups();
              for (IdentificationGroup group : groups)
              {
                  if (group.getIdGroup() == accountIdentificationGroup.getIdGroup())
                  {
                      if (!group.isAcceptAny())
                      {
                          Or filter  = new Or();
                          for (String id : (Set<String>) group.getIdentificationList())
                          {
                              filter.add(new EQ(IdentificationXInfo.CODE, Integer.parseInt(id)));
                          }
                          Home identificationHome = (Home) ctx.get(IdentificationHome.class);
                          subCtx.put(IdentificationHome.class, identificationHome.where(ctx, filter));
                      }
                      break;
                  }
              }
          }
      }
      catch (Throwable t)
      {
          LogSupport.minor(ctx,  this, "Unable to filter identifications", t);
      }
     
     
    //In table mode so set the TABLE_MODE to true.  Used by individual web controls
    subCtx.put("TABLE_MODE", true);

    int mode = ctx.getInt("MODE", DISPLAY_MODE);

     secureCtx = subCtx.createSubContext();
     secureCtx.put("MODE", DISPLAY_MODE);

   HttpServletRequest req      = (HttpServletRequest) ctx.get(HttpServletRequest.class);
   int                blanks   = 0;
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
   
		SpidAware account = null;
   
   if ( ctx.get(BEAN) instanceof ConvertAccountBillingTypeRequest)
   {
       ConvertAccountBillingTypeRequest conversion = (ConvertAccountBillingTypeRequest) ctx.get(BEAN);
			account = (SpidAware) ctx.get(Account.class);
       
   }
   else if (ctx.get(BEAN) instanceof Account)
   {
			account = (SpidAware) ctx.get(BEAN);
   }
   else
   {
			account = (SpidAware) ctx.get(Account.class);
   }
   
   final int count = beans.size() + ((( mode == EDIT_MODE || mode == CREATE_MODE ) && !ctx.getBoolean(ENABLE_ADDROW_BUTTON))? Math.max(0, blanks) : 0);

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
      mmgr.get("AccountIdentification.Label", AccountIdentificationXInfo.Label));

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

   
    ViewModeEnum idType_mode               = getMode(subCtx, "AccountIdentification.idType");
    ViewModeEnum idNumber_mode             = getMode(subCtx, "AccountIdentification.idNumber");
		ViewModeEnum expiryDate_mode =
		    getMode(subCtx, "AccountIdentification.expiryDate");

   if ( idType_mode != ViewModeEnum.NONE )
   {
      link = new Link(ctx);

      // only toggle the sort order if this is the currently selected field
      if ( "IdType".equals(req.getParameter("orderBy")) )
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
      link.add("orderBy", "IdType");

      

      out.println("<th>" + mmgr.get("AccountIdentification.idType.Label", AccountIdentificationXInfo.ID_TYPE.getLabel(ctx)));

   out.println("</th>");
    

   }
    
   if ( idNumber_mode != ViewModeEnum.NONE )
   {
      link = new Link(ctx);

      // only toggle the sort order if this is the currently selected field
      if ( "IdNumber".equals(req.getParameter("orderBy")) )
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
      link.add("orderBy", "IdNumber");

      

      out.println("<th>" + mmgr.get(
                                       "AccountIdentification.idNumber.Label",
                                       AccountIdentificationXInfo.ID_NUMBER.getLabel(ctx)));

   out.println("</th>");
    

   }
    
		if (expiryDate_mode != ViewModeEnum.NONE)
		{
			link = new Link(ctx);

			// only toggle the sort order if this is the currently selected
			// field
			if ("ExpiryDate".equals(req.getParameter("orderBy")))
			{
				String order = req.getParameter("order");

				if (order == null || "ASC".equals(order))
				{
					link.add("order", "DESC");
				}
				else
				{
					link.remove("order");
				}
			}

			link.copy(req, SimpleSearchBorder.DEFAULT_NAME);
			link.add("orderBy", "ExpiryDate");

			out.println("<th TITLE=\""
			    + mmgr.get("AccountIdentification.expiryDate.Label",
			        AccountIdentificationXInfo.EXPIRY_DATE.getLabel(ctx))
			    + "\" >");
			mmgr.get(
                "AccountIdentification.idNumber.Label",
			    AccountIdentificationXInfo.ID_NUMBER.getLabel(ctx));
			out.println("</th>");

		}

		if (show_actions)
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
      renderer.TD(ctx,out);
      out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
      out.println("<img onclick=\"swapTableLines(this,-2,-3,'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/up.gif\"></img>");
      out.println("</td></tr><tr><td>");
      out.println("<img src=\"/images/list/down-dark.gif\"></img>");
      out.println("</td></tr></table>");
      renderer.TDEnd(ctx,out);

      //For the bi-directional only arrow-set
      renderer.TD(ctx,out);
      out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
      out.println("<img onclick=\"swapTableLines(this,-2,-3,'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/up.gif\"></img>");
      out.println("</td></tr><tr><td>");
      out.println("<img onclick=\"swapTableLines(this,-2,-1,'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/down.gif\"></img>");
      out.println("</td></tr></table>");
      renderer.TDEnd(ctx,out);

      //For both black arrows
      renderer.TD(ctx,out);
      out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
      out.println("<img src=\"/images/list/up-dark.gif\"></img>");
      out.println("</td></tr><tr><td>");
      out.println("<img src=\"/images/list/down-dark.gif\"></img>");
      out.println("</td></tr></table>");
      renderer.TDEnd(ctx,out);

      out.print("</tr>");
      rowStart=-1;
    }


    final int start = rowStart;


    for (int j = start ; j < count; j++)
    {
      AccountIdentification bean;


      boolean b=true;
      if ( j > -1 && j < beans.size() )
      {
         bean = (AccountIdentification) i.next();
         b=true;
      }
      else
      {
         bean = new AccountIdentification();
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
          renderer.TD(ctx,out);
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
//              out.println("<img onclick=\"swapTableLines('"+name + "','" + SEPERATOR+"',"+j+","+(j+1)+",new Array('_enabled','idType','idNumber'));\" src=\"/images/list/down.gif\"></img>");
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
//              out.println("<img onclick=\"swapTableLines('"+name + "','" + SEPERATOR+"',"+j+","+(j-1)+",new Array('_enabled','idType','idNumber'));\" src=\"/images/list/up.gif\"></img>");
              out.println("</td></tr><tr><td>");
              out.println("<img onclick=\"swapTableLines(this,"+j+","+(j+1)+",'" + name+"','"+WebSupport.fieldToId(ctx,name)+"');\" src=\"/images/list/down.gif\"></img>");
//              out.println("<img onclick=\"swapTableLines('"+name + "','" + SEPERATOR+"',"+j+","+(j+1)+",new Array('_enabled','idType','idNumber'));\" src=\"/images/list/down.gif\"></img>");
          }
          out.println("</td></tr></table>");
          renderer.TDEnd(ctx,out);
        }
      }
      

      // checkbox: shwon only in edit/create mode and the "AddRow" button is disable
      if (( mode == EDIT_MODE || mode == CREATE_MODE ) && (!ctx.getBoolean(ENABLE_ADDROW_BUTTON)))
      {
          Context checkBoxCtx = ctx;
          if (accountIdentificationGroup.getIdGroup()==AccountIdentificationGroup.DEFAULT_IDGROUP)
          {
              checkBoxCtx = ctx.createSubContext();
              checkBoxCtx.put(HIDE_CHECKBOX, Boolean.FALSE);
          }
          outputCheckBox(checkBoxCtx, out, name + SEPERATOR + j, bean, b);
          subCtx.put(PROPERTY, AccountIdentificationXInfo.ID_GROUP);
          getIdGroupWebControl().toWeb( subCtx, out, name + SEPERATOR + j + SEPERATOR + "idGroup", Integer.valueOf(bean.getIdGroup()));
      }

      subCtx.put(BEAN, bean);


      if ( idType_mode != ViewModeEnum.NONE )
      {
      
         renderer.TD(ctx,out);
         
         // Some nested WebControls may want to know about the particular
         // bean property they are dealing with.
         subCtx.put(PROPERTY, AccountIdentificationXInfo.ID_TYPE);
           getIdTypeWebControl().toWeb( ( idType_mode == ViewModeEnum.READ_ONLY) ? secureCtx : subCtx, out, name + SEPERATOR + j + SEPERATOR + "idType", Integer.valueOf(bean.getIdType()));
         renderer.TDEnd(ctx,out);
      

      }

      

      if ( idNumber_mode != ViewModeEnum.NONE )
      {
      
         renderer.TD(ctx,out);
         
         // Some nested WebControls may want to know about the particular
         // bean property they are dealing with.
         subCtx.put(PROPERTY, AccountIdentificationXInfo.ID_NUMBER);
           getIdNumberWebControl().toWeb( ( idNumber_mode == ViewModeEnum.READ_ONLY) ? secureCtx : subCtx, out, name + SEPERATOR + j + SEPERATOR + "idNumber", bean.getIdNumber());
         renderer.TDEnd(ctx,out);
      

      }

			if (expiryDate_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				// Some nested WebControls may want to know about the particular
				// bean property they are dealing with.
				subCtx.put(PROPERTY, AccountIdentificationXInfo.EXPIRY_DATE);
				getExpiryDateWebControl().toWeb(
				    (expiryDate_mode == ViewModeEnum.READ_ONLY) ? secureCtx
				        : subCtx, out,
				    name + SEPERATOR + j + SEPERATOR + "expiryDate",
				    bean.getExpiryDate());
				renderer.TDEnd(ctx,out);

			}
      
            if (show_actions)
            {
                List beanActions = ActionMgr.getActions(ctx, bean);
                if (beanActions == null)
                {
                   // use the backup actions from the home level
                   beanActions = actions;
                }
                ((WebActionSupport) ctx.get(WebActionSupport.class)).writeLinks(subCtx, beanActions, out, bean, AccountIdentificationIdentitySupport.instance());
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

}