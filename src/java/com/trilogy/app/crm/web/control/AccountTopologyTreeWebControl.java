package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.beans.Child;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountSubscriberIdentitySupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.web.acctmenu.AccountPathBorder;


public class AccountTopologyTreeWebControl
   implements WebControl
{
   private WebControl delegate;
   
   public AccountTopologyTreeWebControl()
   {
      delegate = new AccountTreeWebControl(AccountSubscriberIdentitySupport.instance(),
            "Topology",    // table title
            new OutputWebControl()
            {
               public void toWeb(Context ctx, PrintWriter out, String nae, Object obj)
               {
                  if (obj instanceof Account)
                  {
                     Account node = (Account) obj;

                     // output cute little icons just to be fancy
                     out.println("|--&nbsp;");
                     //out.println("<img src=\"../images/closeFolder.gif\" alt=\"--\"/>");
                     out.println("<img id=\"acctimg-"+ node.getBAN() + "\" src=\"/images/openFolder.gif\" alt=\"--\"/>");

                     Link link = new Link(ctx);
                     link.add("key", node.getBAN());
                     link.add("cmd", "SubMenuAccountEdit");

                     out.print("<a href=\"");
                     link.write(out);
                     out.print("\" id=\"foldheader-"+ node.getBAN() +"\"");
                     out.print("\" class=\"link topology account "+ node.getState() +"\"");
                     
                     out.print(">");
                     
                                         
                     if (node.getFirstName() != null)
                     {
                        out.print(node.getFirstName()+" ");
                     }
                     if (node.getLastName() != null)
                     {
                        out.print(node.getLastName()+" ");
                     }

                     out.print( "["+node.getBAN()+"]");
                     
                     out.print("</a>");
                  }
                  else if (obj instanceof Subscriber)
                  {
                     Subscriber node = (Subscriber) obj;

                     if(node.isPooledGroupLeader(ctx) && !ApiSupport.authorizeUser(ctx, POOLED_SUBSCRIPTION_SHOW_PERMISSION))
                     {
                         String subcriptionTyString = null;
                         try
                         {
                             SubscriptionType subscriptionType = node.getSubscriptionType(ctx);
                             if (subscriptionType == null)
                             {
                                 throw new HomeException("Subscription Type " + node.getSubscriptionType() + " not found for subscription " + node.getId());
                             }
                             subcriptionTyString = subscriptionType.getName();
                         } catch (Throwable t)
                         {
                             //catching throwable is not very good
                             //but dont want to show surprises on the gui
                             new MinorLogMsg(this,"Could not find SubscriptionTypeBean for type-id: " + subcriptionTyString,t).log(ctx);
                             subcriptionTyString = String.valueOf(" ID: " + node.getSubscriptionType());
                         }
                         out.println("|--&nbsp;<font color=\"#008F00\"> Pool &nbsp; (" + subcriptionTyString + ")</font>");
                         return;
                     }
                     // output cute little icons just to be fancy
                     //out.println("<img src=\"../images/subscriber.gif\" alt=\"--\"/>");
                     out.println("|--&nbsp;<img src=\"" + AccountPathBorder.SUBSCRIBER_IMAGE + "\" alt=\"--\"/>");

                     // TODO 2008-08-22 name no longer part of subscriber
                     //if (node.getLastName() != null)
                     //{
                     //   out.print(node.getLastName());
                     //}
                     //if (node.getFirstName() != null)
                     //{
                     //   out.print(", "+node.getFirstName()+" ");
                     //}
                     Link link = new Link(ctx);
                     link.add("key", node.getId());
                     link.remove("query");
                     link.add("cmd",  "SubMenuSubProfileEdit");
                     out.print("<a href=\"");
                     link.write(out);
                     out.print("\"  class=\"link topology subscription "+ node.getState() +"\"");  //style="color:red"
                     out.print(">");
                     
                     out.print("<font color=\"#");
                     
                     if (SubscriberStateEnum.ACTIVE.equals(node.getState()))
                     {
                        out.print("008F00");
                     }
                     else if (SubscriberStateEnum.INACTIVE.equals(node.getState()) ||
                              SubscriberStateEnum.SUSPENDED.equals(node.getState()))
                     {
                        out.print("BF0000");
                     }
                     else if (SubscriberStateEnum.PENDING.equals(node.getState()))
                     {
                        out.print("FF8C00");
                     }
            
                     out.print("\">");

                     
                     out.print( "["+ node.getMSISDN()+"]");
                     
                     out.println("</font>");
                     
                     out.print("</a>");
                     
                  }
                  else
                  {
                     new MinorLogMsg(this, "unsupported child in tree "+obj.getClass().getName(), null).log(ctx);
                     out.print("Unsupported entry in Topology!");
                  }
               }
            },
            false    // do not output default folders icon
            );
   }

   
   public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
   {
      List list = null;

      if ( obj instanceof Account )
      {
            obj = ((Account)obj).getBAN();
      }

      if ( obj instanceof List )
      {
         // assuming the incoming object is a list of decendents for a root account
         // UGLY IMPLEMENTION BEGINS //
         list = (List) obj;
         // root *should* be the first item in collection
         // force the root to have null parent because TreeWebControl's ChildComposer only stops at null parent
         // a GTAC ticket 4100410964 is opened against the trunk of Framework
         ((Child)(list.get(0))).setParent(null);
         // UGLY IMPLEMENTION ENDS //
      }
      else if ( obj instanceof String )
      {

         // assuming the incoming object is the BAN of the root account
         // new implementation: need to display the whole hyerachy all the time,
         // no matter what account currently is looked at.
      	String nodeBAN = (String)obj;
      	
      	// 1. find root parent of this node, if itself is root then return itself
        Account nodeAcct = null;
        
        try
        {
            nodeAcct = AccountSupport.getAccount(ctx, nodeBAN);
        }
        catch(HomeException e)
        {
            new MinorLogMsg(this, "fail to build topology tree", e).log(ctx);
        }

        if ( nodeAcct != null )
        {
      	    Account rootParent = null;
            
            try
            {
                rootParent = nodeAcct.getRootAccount(ctx);
            }
  	        catch(HomeException e)
            {
                new MinorLogMsg(this, "fail to build topology tree", e).log(ctx);
            }

            if ( rootParent != null )
            {
      	        // 2 find all children of this node ( including this node itself )
                try
                {
      	            list = AccountSupport.getTopology(ctx, rootParent);
                }
                catch(HomeException e)
                {
                    new MinorLogMsg(this, "fail to build topology tree", e).log(ctx);
                }

            }
        }
      }
      else
      {
         new MinorLogMsg(this, "Input for tree is weird "+obj.getClass().getName(), null).log(ctx);
      }
      try
      {
         if (list != null)
         {
            //pathDisplayWC.toWeb(ctx, out, "accountPath", "" );
            getDelegate().toWeb(ctx, out, name, list);
         }
      }
      catch (Throwable t)
      {
         new MinorLogMsg(this, "fail to AccountTopologyTreeWebControl", t).log(ctx);
      }
   }

   
   public Object fromWeb(Context ctx, ServletRequest req, String name)
      throws NullPointerException
   {
      return null;
   }

   
   public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
   {
   }

   
   public WebControl getDelegate()
   {
      return delegate;
   }
   
   public final static SimplePermission POOLED_SUBSCRIPTION_SHOW_PERMISSION = new SimplePermission(
   "special.app.crm.pool.show.subscription");
}
