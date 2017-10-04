/*
 *  CarryOverWebControl
 *
 *  Author : Kevin Greer
 *  Date   : Dec 21, 2003
 *
 *  Copyright (c) Redknee, 2003
 *    - all rights reserved
 */
 
package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.bean.core.ServiceFee2;

import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xhome.webcontrol.*;
import javax.servlet.ServletRequest;
import java.util.*;
import java.io.PrintWriter;


/** Subclass of CheckBoxWebControl for displaying ServiceFee2.CarryOver property iff the Service allows. **/
public class CarryOverWebControl
   extends CheckBoxWebControl
{

   public CarryOverWebControl()
   {
   }
   
                   
   public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
   {
      if ( allowCarryOver(ctx) )
      {
         super.toWeb(ctx, out, name, obj);
      }
      else
      {
         out.println("&nbsp;");  
      }
   }
   
   
   public Object fromWeb(Context ctx, ServletRequest req, String name)
	{
      if ( allowCarryOver(ctx) )
      {
         return super.fromWeb(ctx, req, name);
      }
	return Boolean.FALSE;
   }
   
   
   public boolean allowCarryOver(Context ctx)
   {
      try
      {
         ServiceFee2 fee     = (ServiceFee2) ctx.get(AbstractWebControl.BEAN);
         Home        home    = (Home)        ctx.get(ServiceHome.class);
         Service     service = (Service)     home.find(ctx, Long.valueOf(fee.getServiceId()));

         return service.isAllowCarryOver();
      }
      // Expecting either Null or Home Exceptions
      catch (Exception e)
      {
         return false;
      }
   }

}


