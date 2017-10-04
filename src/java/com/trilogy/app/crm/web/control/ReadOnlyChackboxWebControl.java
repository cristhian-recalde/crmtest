/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.security.Permission;
import java.security.Principal;

import javax.servlet.ServletRequest;


import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;

/**
 * @author lxia
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReadOnlyChackboxWebControl    extends PrimitiveWebControl
{


       protected Permission permission; 
	   
	   public ReadOnlyChackboxWebControl()
	   {	    
	   }
	   
	   public ReadOnlyChackboxWebControl(String perm)
	   {		
		  if (perm != null)
		  {	  
			  this.permission = new SimplePermission(perm);
		  }	  
	   }



	   public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	   {
	      int mode = ctx.getInt("MODE", DISPLAY_MODE);

	      switch ( mode )
	      {

	         case CREATE_MODE:
	         case EDIT_MODE:

	            
	            if ( ((Boolean) obj).booleanValue())
	            {
	            	if ( !hasPermission(ctx))
	            	{	
	            		out.println("<input type=\"hidden\" name=\"" + name + ".e\" value=\"y\" />");
	            		out.println("<input type=\"checkbox\" name=\"" + name + "\" checked=\"checked\" disabled=true/>");
	            	} else 
	            	{
	            		out.println("<input type=\"hidden\" name=\"" + name + ".e\" value=\"\n\" />");
	                    out.println(
	                            "<input type=\"checkbox\" name=\"" + name +"\" "
	                            + ((((Boolean) obj).booleanValue()) 
	                                 ? " checked=\"checked\"" 
	                                 : "")
	                            + "/>");                      
	            	}
	            	
	            }
	            else
	            {
	            	out.println("<input type=\"hidden\" name=\"" + name + ".e\" value=\"n\" />");
	               out.println("<input type=\"checkbox\" name=\"" + name + "\" />");
	            }
	            
	         break;

	      default:
		      out.print(((Boolean) obj).booleanValue() ? "True" : "False");

	      }
	   }

	   public Object fromWeb(Context ctx, ServletRequest req, String name)
	   {
	      if ( req.getParameter(name) != null )
	      {
	         return Boolean.TRUE;
	      }
	      
	      // The value could have been specified but wasn't so it is false
	      if ( req.getParameter(name + ".e") != null )
	      {
	    	  if (req.getParameter(name + ".e").equals("y"))
	    		  return Boolean.TRUE; 
	    	  else 
	    		  return Boolean.FALSE;
	      }
	      
	      // The value wasn't specified at all (neither true nor false)
	      throw new NullPointerException("Checkbox value not specified.");
	   }
	   
	   
	   private boolean hasPermission(Context ctx)
	   {
	       return FrameworkSupportHelper.get(ctx).hasPermission(ctx, permission);
	   }
	   
	}