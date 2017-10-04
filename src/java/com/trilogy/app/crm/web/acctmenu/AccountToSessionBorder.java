/*
 *  AccountToSessionBorder.java
 *
 *  Author : kgreer
 *  Date   : Apr 06, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */ 
 
package com.trilogy.app.crm.web.acctmenu;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xhome.session.Session;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;


/**
 * Border to be used as WebController Detail Border to save selected Account is Session.
 *
 * @author  kgreer
 **/
public class AccountToSessionBorder
   implements Border
{

   public AccountToSessionBorder()
   {
   }
   
   
   ///////////////////////////////////////////// impl Border
   
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
        throws ServletException, IOException
    {
        Account account = (Account) WebController.getBean(ctx);
        Context session = Session.getSession(ctx);
      
        if ( account != null && !account.getBAN().isEmpty())
        {
            Account oldAccount = (Account) session.get(Account.class);

            // we always want to update the account in the session incase some of the attributes in the account have changed
            // DJL
//            if ( oldAccount == null || ! oldAccount.getBAN().equals(account.getBAN()) )
            session.put(Account.class, account);
 
            try
            {
                // Clear the Subscriber when you switch Accounts
                // or for individual account, set it to the Subscriber if found
                if (account.isIndividual(ctx))
                {
                    Subscriber sub = (Subscriber)account.subscribers(ctx).find(True.instance());
                    if (sub != null)
                    {
                        sub.setContext(ctx);
                    }
                    session.put(Subscriber.class, sub);
                }
                else if(null == oldAccount  || !account.getBAN().equals(oldAccount.getBAN()))
                {
                    session.remove(Subscriber.class);
                }
            }
            catch (HomeException e)
            {
                session.remove(Subscriber.class);
            }
        }

        delegate.service(ctx, req, res);
    }
}