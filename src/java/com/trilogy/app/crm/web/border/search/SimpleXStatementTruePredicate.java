package com.trilogy.app.crm.web.border.search;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;


public class SimpleXStatementTruePredicate extends SimpleXStatement implements Predicate
{
    public SimpleXStatementTruePredicate(final String str)
    {
        super(str);
    }

    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        return true;
    }
    
}
