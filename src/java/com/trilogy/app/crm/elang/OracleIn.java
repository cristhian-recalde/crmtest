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
package com.trilogy.app.crm.elang;

import java.sql.SQLException;
import java.util.Set;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;

/**
 * Framework doesn't take in to account Oracle's restriction on the IN function.
 * There cannot be more than 999 elements in the IN parameters.
 *
 * NOTE using this in a condition to remove more than 2500 records might not work!. See TT#9121625042
 *
 * @author angie.li@redknee.com
 */
public class OracleIn extends com.redknee.framework.xhome.elang.In
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    transient OracleInXStatement xs_ = null;

    public OracleIn(PropertyInfo arg, Set set)
    {
        setArg(arg);
        setSet(set);
    }

    /**
     * The new Xstatement specific to Oracle
     */
    public String createStatement(Context ctx)
    {
        if ( xs_ == null )
        {
            xs_ = new OracleInXStatement(ctx, getArg(), getSet());
        }

        return xs_.createStatement(ctx);
    }
    
    /**
     * Overwrite the method to use the new OracleInXStatement
     */
    public void set(Context ctx, XPreparedStatement ps)
    throws SQLException
    {
        xs_.set(ctx, ps);
    }
}
