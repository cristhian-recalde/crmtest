// INSPECTED: 01/10/03  MLAM
// INSPECTED: 07/10/03  LZOU

/*
   TransactionsAction

   Copyright (c) 2003, Redknee
   All rights reserved.

   Date        Author          Changes
   ----        ------          -------
   Sept 30 03  Kevin Greer     Created
*/

package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.BucketHistorySearch;


/**
 * Link to Bucket History screen from Account screen.
 **/
public class BucketHistoryAction
   extends SimpleWebAction
{
	public final String prefix=".search";

   public BucketHistoryAction()
   {
      super("buckethistory", "Bucket History");
   }


   public BucketHistoryAction(Permission permission)
   {
      this();
      setPermission(permission);
   }


   @Override
public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
   {
   	  link.remove("key");
   	  link.remove("query");
      link.addRaw("cmd",  "AppCrmBucketHistory");
      link.addRaw("mode", "display");
      //link.addRaw("CMD",  "Search");	// The presence of CMD causes the filter not to work
      link.addRaw("SearchCMD.x",  "11"); // This shouldn't be required, maybe bug in DefaultButtonRenderer

      if ( bean instanceof Subscriber )
      {
         Subscriber sub = (Subscriber) bean;

         link.addRaw(prefix+OutputWebControl.SEPERATOR+BucketHistorySearch.SUBSCRIBERID_PROPERTY,
         		String.valueOf(sub.getId()));
         link.addRaw(prefix+OutputWebControl.SEPERATOR+BucketHistorySearch.BAN_PROPERTY,
         		String.valueOf(sub.getBAN()));
      }
      else
      {
         return;
      }

      link.writeLink(out, getLabel());
   }
}
