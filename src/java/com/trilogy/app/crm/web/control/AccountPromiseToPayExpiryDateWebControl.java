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
import java.util.Date;
import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.DateTimeWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;



/**
 * @author jke
 */
public class AccountPromiseToPayExpiryDateWebControl
	extends ProxyWebControl
{

    public AccountPromiseToPayExpiryDateWebControl()
    
    {
        super(new DateTimeWebControl());
    }

    public Object fromWeb(Context ctx, ServletRequest req, String name)
    	throws IllegalPropertyArgumentException
    {
        Account account = (Account)ctx.get(AbstractWebControl.BEAN);
        if(account.getState() == AccountStateEnum.PROMISE_TO_PAY)
        {
            Object da = req.getParameter(name);
            if(da != null && da.toString().trim().equals(""))
            {
                throw new IllegalPropertyArgumentException("Promise To Pay Expiry Date", "Please select the date.");
            }
        }
        
        return super.fromWeb(ctx, req, name);
    }
    
} 

    // class
