/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved. 
 *
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.DateTimeWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;


/**
 * Provides a web control decorator to display the next day date as the
 * default value of the property.
 *
 * @author jimmy.ng@redknee.com
 */
public class AccountPromiseToPayStartDateWebControl
    extends ProxyWebControl
{
    /**
     * Create a new AccountPromiseToPayDateWebControl.
     */
    public AccountPromiseToPayStartDateWebControl()
    {
        super(new DateTimeWebControl());
    }
    
    
    /**
     * INHERIT
     */
    public void toWeb(
        final Context ctx,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        Object value = obj;
        
        Account account = (Account)ctx.get(AbstractWebControl.BEAN);
        
        if(account.getState() == AccountStateEnum.PROMISE_TO_PAY)
        {
            try
            {
                Account oldAccount = AccountSupport.getAccount(ctx, account.getBAN());
                if (oldAccount.getState() != AccountStateEnum.PROMISE_TO_PAY && oldAccount.getCurrentNumPTPTransitions() == 0)
                    value = new Date();
            }
            catch(Exception e)
            {
                //noop
            }
        }
/*
        if (value == null)
        {
          //value = CalendarSupportHelper.get(ctx).getDayAfter(new Date());
            value = new Date();
        }
*/
        getDelegate().toWeb(ctx, out, name, value);
    }
} // class
