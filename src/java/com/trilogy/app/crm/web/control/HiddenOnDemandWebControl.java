package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.HiddenOnDemandable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class HiddenOnDemandWebControl 
extends ProxyWebControl
{

	   public HiddenOnDemandWebControl(WebControl delegate, PropertyInfo infor)
	   {
	      super(delegate);
	      this.infor = infor;
	   }

	   // TODO: make this more efficient 
	   public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	   {

		  if ( ctx.get(AbstractWebControl.BEAN) instanceof HiddenOnDemandable)
		  {
			  Collection hiddenFields = ((HiddenOnDemandable) ctx.get(AbstractWebControl.BEAN)).getHiddenFields();
			  if ( hiddenFields.contains(this.infor))
			  {
			      return ;
			  }
		  }  
			  getDelegate().toWeb(ctx, out, name, obj); 

	   }

	   
	   public Object fromWeb(Context ctx, ServletRequest req, String name)
	   {
			  if ( ctx.get(AbstractWebControl.BEAN) instanceof HiddenOnDemandable)
			  {
				  Collection hiddenFields = ((HiddenOnDemandable) ctx.get(AbstractWebControl.BEAN)).getHiddenFields();
				  if ( hiddenFields.contains(this.infor))
				  {
					  throw new NullPointerException();
				  }
			  }   
			  
			  return getDelegate().fromWeb(ctx, req, name);
			  
			 
	   }


	   public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
	   {
	      // NOP
	   }

	   PropertyInfo infor; 
}
