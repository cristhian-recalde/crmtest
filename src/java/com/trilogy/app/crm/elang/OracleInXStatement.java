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

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.xdb.InXStatement;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * Framework doesn't take in to account Oracle's restriction on the IN function.
 * There cannot be more than 999 elements in the IN parameters.
 * 
 * @author angie.li
 *
 */
public class OracleInXStatement extends InXStatement 
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public OracleInXStatement(Context ctx, PropertyInfo info, Collection collection)
    {
        super(ctx, info, collection);
    }

    public OracleInXStatement(Context ctx, String statement, Collection collection)
    {
        super(ctx, statement, collection);
    }
    
    public String createStatement(Context ctx)
    {
        if (components_ != null && components_.size() > 0)
        {
            int counter = 0;

            StringBuilder buf = new StringBuilder();

            buf.append(getStatement());
            buf.append(" IN (");

            for (Iterator i = iterator(); i.hasNext(); )
            {
                counter++;
                XStatement xstmt = (XStatement)i.next();

                if (counter == 999)
                {
                    buf.append(") OR ");
                    buf.append(getStatement());
                    buf.append(" IN (");
                }
                
                buf.append(xstmt.createStatement(ctx));

                if (i.hasNext() && counter != 998)
                {
                    buf.append(",");
                }
                
                if (counter == 999)
                {
                    counter = 0;  //reset counter
                }
            }

            buf.append(")");

            return buf.toString();
        }
        else
        {
            return ((False)False.instance()).createStatement(ctx);
        }
    }

}
