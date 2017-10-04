/*
 * Created on Jul 28, 2005
 */
package com.trilogy.app.crm.visitor;


import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author rattapattu
 */
public class RowCountVisitor implements Visitor
{
    private int count = 0;
    private String countColumnAlias;
 
    public RowCountVisitor(String countColumnAlias)
    {
        this.countColumnAlias = countColumnAlias;
    }
    
    public void visit(Context ctx, Object obj) throws AgentException,
            AbortVisitException
    {
        XResultSet rs = (XResultSet)obj;
        try
        {
            count = rs.getInt(countColumnAlias);
        }
        catch (Exception e)
        {
            LogSupport.debug(ctx,this,"Error getting count",e);
        }
    }

    public int getCount()
    {
        return count;
    }
    public String getCountColumnAlias()
    {
        return countColumnAlias;
    }
    public void setCount(int count)
    {
        this.count = count;
    }
    public void setCountColumnAlias(String countColumnAlias)
    {
        this.countColumnAlias = countColumnAlias;
    }
}
