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

import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.XDeepCloneable;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XStatementProxy;


/**
 * This XStatement can be used to automatically perform greater than
 * or less than queries depending on how the paging data is sorted.
 * 
 * This is frequently used by the API because all of the list methods
 * can return data in ascending or descending order, and many of them
 * support paging.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class PagingXStatement extends XStatementProxy implements Predicate, XDeepCloneable
{
    private static final long serialVersionUID = 1L;

    public PagingXStatement(Object arg1, Object arg2)
    {
        this(arg1, arg2, true);
    }
    
    public PagingXStatement(Object arg1, Object arg2, boolean ascending)
    {
        super(ascending ? new GT(arg1, arg2) : new LT(arg1, arg2));
    }

    /**
     * {@inheritDoc}
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if (getDelegate() instanceof Predicate)
        {
            return ((Predicate)getDelegate()).f(ctx, obj);
        }
        else
        {
            return false;   
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object deepClone() throws CloneNotSupportedException
    {
        if (getDelegate() instanceof XDeepCloneable)
        {
            return ((XDeepCloneable)getDelegate()).deepClone();
        }
        return clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        if (getDelegate() instanceof XCloneable)
        {
            return ((XDeepCloneable)getDelegate()).clone();
        }
        return super.clone();
    }
}
