/*
  AccountTreeWebControl
*/

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.beans.AncestorComposer;
import com.trilogy.framework.xhome.beans.Child;
import com.trilogy.framework.xhome.beans.ChildComposer;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.ColourSettings;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.tree.TreeWebControl;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;

import com.trilogy.app.crm.bean.Account;



/** A WebControl for displaying heirarchical entities. **/

public class AccountTreeWebControl
    extends TreeWebControl
{
   public AccountTreeWebControl(
      IdentitySupport  id,
      String           relationTableName,
      OutputWebControl outControl,
      boolean          outputFolders)
   {
	   super(id, relationTableName, outControl, outputFolders, true);
   }

   // copied from TreeWebControl
   public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
   {
      int        mode  = ctx.getInt("MODE", DISPLAY_MODE);
      String	 expandKey = (String)ctx.get("TableWebController.SelectedKey");
      ColourSettings colours = ColourSettings.getSettings(ctx);
      Collection beans = (Collection) obj;
          
      int numOfIndent = 0;
      boolean allRootExisting = false;
      Child root = null;
      
      if ( mode == HELP_MODE )
      {
         // either NOP or TODO
         return ;
      }

      for ( Iterator i =  beans.iterator() ; i.hasNext() ; )
      {   
           Child   child = (Child)i.next();
           Object  idObj = getIdentitySupport().ID(child);
           String  key   = getIdentitySupport().toStringID(idObj);
           
           if ( key.equals("")  )   // find real Root element in whole parent-children hierarchy
           {
                root = child;
                allRootExisting = true;
                                        
                break;  // only one root 
           }
      }

      Object parent       = null;
      Object parentKey    = null; 
      Object key          = null;
      Set allBranches    = new HashSet();
      
      // come from SearchBorder 
      Home origHome = (Home)ctx.get(ORIGNALHOME);

      if ( origHome != null ) // then we know this comes from SearchBorder
      {
            try
            {
                    // need to add root element first 
                    Child rootElem = (Child)origHome.find(ctx, "");
                    allBranches.add(rootElem);
            }
             catch(HomeException h)
            {
                new SeverityLogMsg(SeverityEnum.MINOR, this, h.getMessage(), h).log(ctx);
            }

            // for search purpose, beans contains individual entries returned by search engine
            for ( Iterator j = beans.iterator(); j.hasNext() ;)
            {
                    // find out all its parents 
                    Child child = (Child)j.next();
            
                    key = XBeans.getIdentifier(child);
                    allBranches.add(child);

                    parentKey = child.getParent();

                    while ( !parentKey.equals("") )  // not root, to avoid infinite loop because key and parent key of root are same empty string.
                    {
                        try
                        {
                            parent = origHome.find(ctx, parentKey);                
                   
                            if ( parent != null )
                            {
                                allBranches.add(parent);
                                parentKey = ((Child) parent).getParent();
                            }
                            else
                                break;
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
            }
      }


      if ( origHome != null ) //  then we know the flow came from searchborder
      {
          beans = allBranches;
      }
      
      ChildComposer childComposer = new ChildComposer(beans);
      AncestorComposer ancestorComposer = new AncestorComposer(beans);
      Collection expandItems = ancestorComposer.getAncestors(expandKey);

      out.println("<center>");
      out.print("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"1\" bordercolor=\"");
      out.print(colours.getDetailTitleBG());
      out.print("\" bgcolor=\"");
      out.print(colours.getDetailTitleBG());
    //  out.print("\"><tr><th colspan=\"2\" bgcolor=\"");
      out.print("\"><tr><th bgcolor=\"");
      out.print(colours.getDetailTitleBG());
      out.print("\"><font color=\"");
      out.print(colours.getDetailTitleText());
      out.print("\">" + getRelationTableName() + "</font></th></tr>");
     // out.print("<tr><td colspan=\"2\">");
      out.print("<tr><td>");
      out.print("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"0\" bordercolor=\"");
      //out.print(colours.getDetailTitleBG());
      out.print(colours.getDetailBG());
      out.print("\" bgcolor=\"");
      out.print(colours.getDetailBG());
      out.print("\">");

      if ( allRootExisting && root != null)
      {
          outputChildHtml(ctx, out, root, numOfIndent); 
          numOfIndent++;
      }

      outputChildren(ctx, out, childComposer, childComposer.getRoot(), numOfIndent, expandItems);
///////////
      // this is added for html displaying error, there should be smarter way to do so
      // but don't have enough time to dig into....
      if ( lastNodeIsAccount(beans))
      {
          out.println("</td></tr>");
          out.println("</table>");
      }
/////////////
      out.println("</table>");
      out.println("</td></tr>");
      out.println("</table></center>");
   }
   
   // copied from TreeWebControl
   protected void outputChildren(Context ctx, PrintWriter out, ChildComposer relationships, Collection children, int numOfIndent, Collection expandItems)
   {
      if ( children == null ) return;
      
      for ( Iterator i = children.iterator() ; i.hasNext() ; )
      {
         Child child = (Child) i.next();
         Object             idObj = getIdentitySupport().ID(child);
         String             key   = getIdentitySupport().toStringID(idObj);

         /* find out the root element ( RedkneeRoot in xmenu system ) which 
          * is supposed to have the same value for key and parentKey 
          */
         if  ( key.equals( child.getParent()))   
         {
            outputChildHtml(ctx, out, child, 0); 
            children.remove(child);
            numOfIndent++;

            break;  // only one
         }
      }

      for ( Iterator i = children.iterator() ; i.hasNext() ; )
      {
         Child child = (Child) i.next();
         
       	 outputChild(ctx, out, relationships, child, numOfIndent + 1, expandItems);
      }
   }
   
   // copied from TreeWebControl
   protected void outputChild(Context ctx, PrintWriter out, ChildComposer relationships, Child child, int numOfIndent, Collection expandItems)
   {
	  String editLink = (String)ctx.get("TableWebController.EditLink", "display");

	  if ((expandAll_) ||
		  (expandItems == null) ||	
		  (expandItems.size() == 0) ||
		  (child.getParent() == null) ||
		  (child.getParent().equals("")) ||
		  (expandItems.contains(child.getParent())))
	  {
           
          outputChildHtml(ctx, out, child, numOfIndent);

          if ( child instanceof Account)
          {
              Account node = (Account)child;
             // out.print("<tr><td colspan=\"");
             // out.print(2);
             // out.print("\"><table width=\"100%\" border=\"0\" id=\"foldinglist-"+ node.getBAN() +"\">");
              out.print("<tr><td>");
              out.print("<table width=\"100%\" border=\"0\" id=\"foldinglist-"+ node.getBAN() +"\">");
          }

	      if (expandAll_ || ((expandItems != null) && (expandItems.size() > 0)))
	      {
              outputChildren(ctx, out, relationships, relationships.getChildren(XBeans.getIdentifier(child)), numOfIndent, expandItems);
          }

          if (child instanceof Account)
          {
              out.println("</table></td></tr>");
          }
      }
   }

   
   protected void outputChildHtml(Context ctx, PrintWriter out, Child child, int numOfIndent)
   {
      out.println("<tr onclick=\"changeTRColor(this);\">");
      out.println("<td nowrap=\"nowrap\">");
      
      for ( int j = 0 ; j < numOfIndent ; j++ )
      {
         out.print("&nbsp;&nbsp;&nbsp;&nbsp;");
      }
      
      HttpServletRequest req = (HttpServletRequest) ctx.get(HttpServletRequest.class);
      
      if ( outputFolders_ )
      {
         out.println(getImage(ctx, child));
      }
      
      getOutputControl().toWeb(ctx, out, "", child);
     
      out.println("</td>");
      out.println("</tr>");
   }

   protected String getImage(Context ctx, Object child)
   {
     return("<img src=\"/images/closeFolder.gif\" alt=\"--\" />");
   }

   protected boolean lastNodeIsAccount(Collection beans)
   {
       boolean isAccount = false;
       
       Child child = null;

       for ( Iterator i = beans.iterator(); i.hasNext();)
       {
            child = (Child) i.next();
       }

      if ( child != null && child instanceof Account )  // last element in Collection is account
      {
            isAccount = true;
      }
 
      return isAccount;
   }

}
