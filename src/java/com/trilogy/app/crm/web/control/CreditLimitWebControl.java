/*
 * Created on Apr 7, 2004
 *
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author dzhang
 *
 * Customized WebControl for Subscriber's Credit Limit
 */
public class CreditLimitWebControl extends ProxyWebControl{
	   public CreditLimitWebControl()
	    {
	        super(XCurrencyWebControl.instance());
	    }

	    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	    {
	        try
	        {
	        	int mode  = ctx.getInt("MODE", DISPLAY_MODE);
	        	Object  value = obj;
	        	if(mode == CREATE_MODE)
	        	{
	        		Subscriber       sub   = (Subscriber)       ctx.get(AbstractWebControl.BEAN);
	                PricePlanVersion plan  = sub.getRawPricePlanVersion(ctx);
					
					if(plan==null)
					{
						if(LogSupport.isDebugEnabled(ctx))
						{
							new DebugLogMsg(this,"Cannot get raw PricePlan",null).log(ctx);
						}
						
						value=Long.valueOf(0);
					}
					else
					{
						value = Long.valueOf(plan.getCreditLimit());
					}
	        	}

	            getDelegate().toWeb(ctx, out, name,  value);
	        }
	        catch (HomeException e)
	        {
	            // not important  
	        }
	    }
	    
	    public Object fromWeb(Context ctx, ServletRequest req, String name)
	    {
	        String str = req.getParameter(name);
	      
	        if ( str == null )
	        {
	            throw new NullPointerException("Null Value");  
	        }
	      
	        if ( "".equals(str.trim()) ) 
	        {
	        	throw new IllegalArgumentException("Credit Limit can't be blank!");
	        }
	         
	        return getDelegate().fromWeb(ctx, req, name);
	    }
	}