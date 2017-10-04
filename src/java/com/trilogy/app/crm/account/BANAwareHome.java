/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.account;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


/**
 * This class adapts creation of BANAware enities
 * First use: AccountAttachmentHome chain.
 * 
 * @author Simar Singh
 * @date Jan 12, 2009
 */
public class BANAwareHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new BANAwareHome decorator.
     * 
     * @param context
     * @param delegate
     *            The Home to which we delegate.
     */
    
    
    
    public Object create(Context ctx, Object obj) throws HomeException
    {
        if(obj instanceof BANAware)
        {
            final BANAware banAware = (BANAware) obj;
            if(banAware.getBAN()==null || "".equals(banAware.getBAN()))
            {
                //set ban according to context if not there
                accountalizeBean(ctx, banAware);
            }
        }
        
        return super.create(ctx, obj);
    }
    

    public BANAwareHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    public static BANAware accountalizeBean(Context ctx, final BANAware banAware)
    {
        if (ctx.has(Account.class))
        {
            Account account = (Account) ctx.get(Account.class);
            banAware.setBAN(account.getBAN());
        }
        else if (ctx.has(Subscriber.class))
        {
            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
            banAware.setBAN(sub.getBAN());
        }
        else if (ctx.has(AbstractWebControl.class))
        {
            Object obj = ctx.get(AbstractWebControl.class);
            if (obj instanceof BANAware)
            {
                banAware.setBAN(((BANAware) obj).getBAN());
            }
        }
        return banAware;
    }
}
