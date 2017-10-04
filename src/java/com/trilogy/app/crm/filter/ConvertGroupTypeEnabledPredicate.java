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
package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


public class ConvertGroupTypeEnabledPredicate extends SimpleDeepClone implements Predicate, XStatement
{

    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        Account account = null;
        Account parentAccount = null;
    
        if (ctx.get(Account.class)!=null)
        {
            account = (Account) ctx.get(Account.class);
        }

        if (account!=null)
        {
            try
            {
                parentAccount = account.getParentAccount(ctx);
            }
            catch (HomeException e)
            {
            }
        }
        
        return ((account==null || !GroupTypeEnum.GROUP_POOLED.equals(account.getGroupType()))) && 
                (parentAccount==null || !parentAccount.isPooled(ctx));
    }

    @Override
    public String createStatement(Context ctx)
    {
        return ((True)True.instance()).createStatement(ctx);
    }

    @Override
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
        ((True)True.instance()).set(ctx, ps);
    }



}
