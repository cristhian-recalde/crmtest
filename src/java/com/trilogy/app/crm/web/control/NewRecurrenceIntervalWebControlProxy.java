package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.ServiceFee2N;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.OptionalLongWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * 
 * @author kabhay
 *
 */
public class NewRecurrenceIntervalWebControlProxy extends OptionalLongWebControl 
{

	public NewRecurrenceIntervalWebControlProxy() {
		super();
	}

	public NewRecurrenceIntervalWebControlProxy(long defaultValue) {
		super(defaultValue);
	}

	public NewRecurrenceIntervalWebControlProxy(WebControl delegate,
			long defaultValue) {
		super(delegate, defaultValue);
	}

	public NewRecurrenceIntervalWebControlProxy(WebControl delegate,
			WebControl emptyWebControl, long defaultValue) {
		super(delegate, emptyWebControl, defaultValue);
	}

	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj) 
	{
		ServiceFee2N sf2 = (ServiceFee2N) ctx.get(AbstractWebControl.BEAN);
		boolean displayBlank = false;
		if(sf2 != null)
		{
			if(!sf2.getServicePeriod().equals(ServicePeriodEnum.MULTIDAY) && !sf2.getServicePeriod().equals(ServicePeriodEnum.MULTIMONTHLY) )
			{
				/*
				 * display blank value for interval when service-period is not multi-day and multi-month
				 */
				displayBlank = true;
				
			}
		}
	     
		long             value = ((Number) obj).longValue();
	    int              mode  = ctx.getInt("MODE", DISPLAY_MODE);

	       // Use a TextFieldWebControl to output ""
	       if ( isDefault(ctx, value) )
	       {
	          empty_wc_.toWeb(ctx, out, name, "");
	       }else if(displayBlank)
	       {
	    	   empty_wc_.toWeb(ctx, out, name, "");
	       }
	       else
	       {
	          getDelegate().toWeb(ctx, out, name, obj);
	       }
	}
	
	
	
	
}
