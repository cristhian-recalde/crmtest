// INSPECTED: 22/09/03 MLAM

/*
   DisplayCheckBoxWebControl

	-- created: Lily Zou
*/

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.webcontrol.CheckBoxWebControl;


/**
 *  the same as CheckBoxWebControl except that it 
 *  will display checkbox under display mode
 **/
 
//REVIEW(readability): tab should be set to have 4 spaces (currently has 3) MLAM

public class DisplayCheckBoxWebControl
   extends CheckBoxWebControl
{

   public DisplayCheckBoxWebControl()
   {
	   super("true", "false");
   }

   protected void display(PrintWriter out, Object obj)
   {
      //out.print(((Boolean) obj).booleanValue() ? trueText_ : falseText_);
      if ( ((Boolean) obj).booleanValue() )
      {
           out.println("<input type=\"checkbox\" name=\"abc\" checked=\"checked\" />");
      }
      else
      {
           out.println("<input type=\"checkbox\" name=\"abc\" />");
      }
   }

}
