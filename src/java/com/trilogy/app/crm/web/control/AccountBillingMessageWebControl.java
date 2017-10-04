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

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.TextAreaWebControl;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillingMessagePreferenceEnum;

/**
 * @author jke
 */
public class AccountBillingMessageWebControl extends TextAreaWebControl
{
    private boolean prev_override_status = false;
    public AccountBillingMessageWebControl(int numl, int numc)
    {
        super(numl, numc);
    }
    
	@Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
       if (obj != null)
       {
           if (obj instanceof Account)
           {
              Account acc = (Account) obj;
              String billmsg = acc.getBillingMessage();

              if(billmsg == null)
            {
                acc.setBillingMessage("");
            }
           }
       }
       super.toWeb(ctx, out, name, obj);
    }
	
    
	@Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
		throws NullPointerException
	{
		
			String billPref = req.getParameter(".billingMsgPreference");
			if(billPref == null)
            {
                billPref = "0";
            }
			int bp = Integer.valueOf(billPref).intValue();

		    String msg = req.getParameter(name);
		    if(bp == BillingMessagePreferenceEnum.OVERRIDE.getIndex() && (msg != null && msg.trim().length() == 0))  
		    {
		            throw new IllegalPropertyArgumentException("Billing Message", "Please fill in Account Billing Message. ");
		    }
		    else //if(req.getParameter(name) != null)
		    {
		        if(bp != BillingMessagePreferenceEnum.OVERRIDE.getIndex())
                {
                    prev_override_status = false;
                }
		        
				return super.fromWeb(ctx, req, name);
		    }
	    
	}

}
