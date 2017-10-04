package com.trilogy.app.crm.web.control;
/*
 * Created on Apr 7, 2004
 */

import java.io.PrintWriter;

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
 * A customized Web Control for Subscriber's deposit field only
 */
public class DepositWebControl extends ProxyWebControl
{
    public DepositWebControl()
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
        		Subscriber       sub   = (Subscriber) ctx.get(AbstractWebControl.BEAN);
            PricePlanVersion plan  = sub.getRawPricePlanVersion(ctx);
				
				if(plan==null)
				{
					if(LogSupport.isDebugEnabled(ctx))
					{
						new DebugLogMsg(this,"Cannot find rawPricePlan for subscriber",null).log(ctx);
					}
					
					value=Long.valueOf(0);
				}
				else
				{
					value = Long.valueOf(plan.getDeposit());
				}
        	}

            getDelegate().toWeb(ctx, out, name,  value);
        }
        catch (HomeException e)
        {
            // not important  
        }
    }
}


