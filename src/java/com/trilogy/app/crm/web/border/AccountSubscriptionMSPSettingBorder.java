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
package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;


/**
 * This border sets the MSP flag in the context to that of the account/subscription in the session context if there is one.
 * 
 * It is useful for side-menus for beans that need to know what SPID it belongs to but don't really need the SPID in the
 * bean that it is showing.
 *
 * @author aaron.gourley@redknee.com
 * @since 9.0
 */
public class AccountSubscriptionMSPSettingBorder implements Border
{

    /**
     * {@inheritDoc}
     */
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        int spid = -1;
        if (MSP.getBeanSpid(ctx) == null)
        {
            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
            if (sub != null)
            {
                spid = sub.getSpid();
            }
            else
            {
                Account account = (Account) ctx.get(Account.class);
                if (account != null)
                {
                    spid = account.getSpid();
                }
            }
        }
        if (spid >= 0)
        {
            ctx = ctx.createSubContext();
            MSP.setBeanSpid(ctx, spid);
        }
        delegate.service(ctx, req, res);
    }

}
