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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.XStatementProxy;


/**
 * True predicate for account required fields.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class TrueAccountRequiredFieldPredicate extends SimpleDeepClone implements AccountRequiredFieldPredicate
{
    private final static AccountRequiredFieldPredicate instance__ = new TrueAccountRequiredFieldPredicate();
    
    public static AccountRequiredFieldPredicate instance()
    {
      return instance__;
    }
    


    /**
     * This ensures that all serialization (including from RMI) doesn't result in the creation of
     * multiple instances of the same Object value thereby breaking the == operator.
     */
    public boolean f(Context ctx, Object obj)
    {
        return true;
    }

    public String createStatement(Context ctx)
    {
        return "1 = 1";
    }

    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }

}
