package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.HiddenOnDemandable;
import com.trilogy.app.crm.bean.ReadOnlyOnDemandable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class ReadOnlyOnDemandWebControl extends ProxyWebControl
{

	   public ReadOnlyOnDemandWebControl(WebControl delegate, PropertyInfo infor)
	   {
	      super(delegate);
	      this.infor = infor;
	   }

	   // TODO: make this more efficient 
	   public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	   {

		  if ( ctx.get(AbstractWebControl.BEAN) instanceof ReadOnlyOnDemandable)
		  {
			  Collection readOnlyfields = ((ReadOnlyOnDemandable) ctx.get(AbstractWebControl.BEAN)).getReadOnlyFields();
			  if ( readOnlyfields.contains(this.infor))
			  {
				  int mode = ctx.getInt("MODE", DISPLAY_MODE);
				      
				      // every mode becomes display mode
				  if ( mode != DISPLAY_MODE )
				  {
					  ctx = ctx.createSubContext();
				      ctx.put("MODE", new Integer(DISPLAY_MODE));           
				   }
			  } 
		    }  
		  
		  getDelegate().toWeb(ctx, out, name, obj); 
		    
	   }

	   
	   public Object fromWeb(Context ctx, ServletRequest req, String name)
	   {

		   return delegate_.fromWeb(ctx, req, name);
	   }


	   public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
	   {
	      // NOP
	   }

	   PropertyInfo infor; 
}
