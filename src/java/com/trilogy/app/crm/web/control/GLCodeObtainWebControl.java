/*
 *  GLCodeObtainWebControl
 *
 *  Author : Lily Zou
 *  Date   : Oct.14 , 2003
 *
 *  Copyright (c) Redknee, 2003
 *    - all rights reserved
 */
 
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/** Webcontrol to populate GL code in Transaction by looking up the selected Adjustment Type. **/
public class GLCodeObtainWebControl
   extends TextFieldWebControl
{
   
   protected WebControl delegate_ = new TextFieldWebControl(12);
   
   public GLCodeObtainWebControl()
   {
   }
   
   public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
   {
      try
      {
         Transaction        trans       = (Transaction) ctx.get(AbstractWebControl.BEAN);
         
         //if(LogSupport.isDebugEnabled(ctx))
         //{   
         //   new DebugLogMsg(this,"...." + trans,null).log(ctx);
         //}
         
         int                adjust_code = trans.getAdjustmentType();
         // not used (Paul)
         //int                mode        = ctx.getInt("MODE", DISPLAY_MODE);

         AdjustmentType     adjust_type = null;
         AdjustmentInfo		adjust_info = null;
         Home               home        = (Home)ctx.get(AdjustmentTypeHome.class);
        
         if ( home != null)
         {
            adjust_type = (AdjustmentType) home.find(ctx, Integer.valueOf(adjust_code));
            if (adjust_type != null)
            {
            	adjust_info = (AdjustmentInfo) adjust_type.getAdjustmentSpidInfo().get(
            		Integer.valueOf(trans.getSpid()));
            }
         }
         
         if ( adjust_info != null )
         {
            delegate_.toWeb(ctx, out, name, adjust_info.getGLCode());
         }
         else
         {
            delegate_.toWeb(ctx, out, name, "");

         }
      }
      catch (HomeException e)
      {
      }
   }
}


