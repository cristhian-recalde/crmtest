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
package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * Predicate returns true if the subscriber in the context is of type Prepaid An example
 * of where this will be used is in the Subscriber Balance History Screen
 * 
 * @author angie.li@redknee.com
 */
public class PrepaidPredicate extends SimpleDeepClone implements Predicate, XStatement
{

    private static final long serialVersionUID = 1L;


    public PrepaidPredicate()
    {
    }


    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        Account account = (Account) ctx.get(Account.class);
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        return ((sub != null) && sub.isPrepaid()) || (account != null && account.isPrepaid());
    }



    public String createStatement(Context ctx)
    {
        return ((False) (False.instance())).createStatement(ctx);
    }


    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
        ((False) (False.instance())).set(ctx, ps);
    }
}
